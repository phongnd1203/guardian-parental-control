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
      return errorResponse("UNAUTHORIZED", "Authenticated parent user required.", 401);
    }

    const body = await req.json().catch(() => ({}));
    const { token } = body;

    if (!token || typeof token !== "string") {
      return errorResponse("INVALID_INPUT", "Invitation token is required.", 400);
    }

    const tokenSha256 = await hashToken(token.trim());
    const serviceClient = getServiceClient();

    // 1. Find invitation by token_hash
    const { data: invite, error: inviteError } = await serviceClient
      .from("family_invitations")
      .select("*")
      .eq("token_hash", tokenSha256)
      .maybeSingle();

    if (inviteError || !invite) {
      return errorResponse("INVITATION_NOT_FOUND", "Invitation not found or invalid token.", 404);
    }

    if (invite.status === "ACCEPTED") {
      return errorResponse("INVITATION_ALREADY_USED", "This invitation has already been accepted.", 409);
    }

    if (invite.status === "CANCELLED") {
      return errorResponse("INVITATION_CANCELLED", "This invitation has been cancelled.", 410);
    }

    if (invite.status === "EXPIRED" || new Date(invite.expires_at).getTime() < Date.now()) {
      return errorResponse("INVITATION_EXPIRED", "This invitation has expired.", 410);
    }

    // 2. Check if user is already a member
    const { data: existingMember } = await serviceClient
      .from("family_members")
      .select("id")
      .eq("family_id", invite.family_id)
      .eq("user_id", user.id)
      .maybeSingle();

    if (existingMember) {
      return errorResponse("MEMBER_ALREADY_EXISTS", "You are already a member of this family.", 409);
    }

    // 3. Add to family_members
    const { error: memberError } = await serviceClient
      .from("family_members")
      .insert({
        family_id: invite.family_id,
        user_id: user.id,
        role: invite.role,
      });

    if (memberError) {
      return errorResponse("ACCEPT_INVITE_FAILED", memberError.message, 500);
    }

    // 4. Update invitation to ACCEPTED
    await serviceClient
      .from("family_invitations")
      .update({
        status: "ACCEPTED",
        accepted_by: user.id,
        accepted_at: new Date().toISOString(),
      })
      .eq("id", invite.id);

    // 5. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: invite.family_id,
      actor_type: "USER",
      actor_id: user.id,
      action: "MEMBER_JOINED",
      entity_type: "MEMBER",
      entity_id: user.id,
      metadata: { role: invite.role, invitationId: invite.id },
    });

    return jsonResponse({
      success: true,
      familyId: invite.family_id,
      role: invite.role,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
