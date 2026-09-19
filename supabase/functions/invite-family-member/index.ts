import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { corsHeaders } from "./_shared/cors.ts";
import { errorResponse, jsonResponse } from "./_shared/response.ts";
import { getServiceClient, getUserClient, hashToken } from "./_shared/supabase.ts";

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const userClient = getUserClient(req);
    const { data: { user }, error: authError } = await userClient.auth.getUser();

    if (authError || !user || user.is_anonymous) {
      return errorResponse("UNAUTHORIZED", "Authentication required.", 401);
    }

    const body = await req.json().catch(() => ({}));
    const { familyId, email, role } = body;

    if (!familyId || !email || !role) {
      return errorResponse("INVALID_INPUT", "familyId, email and role are required.", 400);
    }

    if (role !== "PARENT" && role !== "VIEWER") {
      return errorResponse("INVALID_INPUT", "Role must be PARENT or VIEWER.", 400);
    }

    const normalizedEmail = String(email).trim().toLowerCase();
    const serviceClient = getServiceClient();

    // 1. Verify caller has OWNER or PARENT role in this family
    const { data: callerMember, error: callerError } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", familyId)
      .eq("user_id", user.id)
      .single();

    if (callerError || !callerMember || (callerMember.role !== "OWNER" && callerMember.role !== "PARENT")) {
      return errorResponse("FORBIDDEN", "Only Owner or Parent can invite members.", 403);
    }

    // 2. Check if user with this email is already a member
    const { data: existingUser } = await serviceClient
      .from("family_members")
      .select("id, user:user_id(email)")
      .eq("family_id", familyId);

    // 3. Check existing pending invitation
    const { data: existingInvite } = await serviceClient
      .from("family_invitations")
      .select("id")
      .eq("family_id", familyId)
      .eq("email", normalizedEmail)
      .eq("status", "PENDING")
      .maybeSingle();

    if (existingInvite) {
      return errorResponse("INVITATION_ALREADY_EXISTS", "A pending invitation already exists for this email.", 409);
    }

    // 4. Generate high-entropy raw token & SHA-256 hash
    const rawToken = crypto.randomUUID().replace(/-/g, "") + crypto.randomUUID().replace(/-/g, "");
    const tokenSha256 = await hashToken(rawToken);
    const expiresAt = new Date(Date.now() + 72 * 3600 * 1000).toISOString();

    const { data: invitation, error: inviteError } = await serviceClient
      .from("family_invitations")
      .insert({
        family_id: familyId,
        email: normalizedEmail,
        role,
        token_hash: tokenSha256,
        status: "PENDING",
        expires_at: expiresAt,
        invited_by: user.id,
      })
      .select()
      .single();

    if (inviteError || !invitation) {
      return errorResponse("INVITE_FAILED", inviteError?.message || "Failed to create invitation.", 500);
    }

    // 5. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: familyId,
      actor_type: "USER",
      actor_id: user.id,
      action: "MEMBER_INVITED",
      entity_type: "INVITATION",
      entity_id: invitation.id,
      metadata: { email: normalizedEmail, role },
    });

    return jsonResponse({
      invitation: {
        id: invitation.id,
        familyId: invitation.family_id,
        email: invitation.email,
        role: invitation.role,
        status: invitation.status,
        expiresAt: invitation.expires_at,
        inviteUrl: `https://guardian.example.com/invite/${rawToken}`,
      },
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
