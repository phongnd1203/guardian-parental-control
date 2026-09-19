import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { corsHeaders } from "./_shared/cors.ts";
import { errorResponse, jsonResponse } from "./_shared/response.ts";
import { getServiceClient, getUserClient } from "./_shared/supabase.ts";

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
    const { invitationId } = body;

    if (!invitationId) {
      return errorResponse("INVALID_INPUT", "invitationId is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Get invitation
    const { data: invite, error: inviteError } = await serviceClient
      .from("family_invitations")
      .select("*")
      .eq("id", invitationId)
      .maybeSingle();

    if (inviteError || !invite) {
      return errorResponse("INVITATION_NOT_FOUND", "Invitation not found.", 404);
    }

    // 2. Check caller permission
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", invite.family_id)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || (callerMember.role !== "OWNER" && callerMember.role !== "PARENT")) {
      return errorResponse("FORBIDDEN", "Only Owner or Parent can cancel invitations.", 403);
    }

    // 3. Mark CANCELLED
    await serviceClient
      .from("family_invitations")
      .update({
        status: "CANCELLED",
        cancelled_at: new Date().toISOString(),
      })
      .eq("id", invite.id);

    // 4. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: invite.family_id,
      actor_type: "USER",
      actor_id: user.id,
      action: "MEMBER_INVITATION_CANCELLED",
      entity_type: "INVITATION",
      entity_id: invite.id,
      metadata: { email: invite.email },
    });

    return jsonResponse({ success: true });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
