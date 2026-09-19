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
    const { familyId } = body;

    if (!familyId) {
      return errorResponse("INVALID_INPUT", "familyId is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Verify caller is OWNER
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", familyId)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || callerMember.role !== "OWNER") {
      return errorResponse("OWNER_REQUIRED", "Only the Owner can delete a family.", 403);
    }

    const now = new Date().toISOString();

    // 2. Unpair all devices
    await serviceClient
      .from("devices")
      .update({
        protection_status: "UNPAIRED",
        unpaired_at: now,
        auth_user_id: null,
      })
      .eq("family_id", familyId);

    // 3. Cancel all pending invitations
    await serviceClient
      .from("family_invitations")
      .update({
        status: "CANCELLED",
        cancelled_at: now,
      })
      .eq("family_id", familyId)
      .eq("status", "PENDING");

    // 4. Soft-delete family
    const { error: deleteError } = await serviceClient
      .from("families")
      .update({ deleted_at: now })
      .eq("id", familyId);

    if (deleteError) {
      return errorResponse("DELETE_FAILED", deleteError.message, 500);
    }

    // 5. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: familyId,
      actor_type: "USER",
      actor_id: user.id,
      action: "FAMILY_DELETED",
      entity_type: "FAMILY",
      entity_id: familyId,
    });

    return jsonResponse({
      success: true,
      familyId,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
