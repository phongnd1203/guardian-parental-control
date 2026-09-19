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
    const { childId } = body;

    if (!childId) {
      return errorResponse("INVALID_INPUT", "childId is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Get child
    const { data: child, error: childError } = await serviceClient
      .from("children")
      .select("id, family_id, name, status")
      .eq("id", childId)
      .maybeSingle();

    if (childError || !child) {
      return errorResponse("CHILD_NOT_FOUND", "Child not found.", 404);
    }

    // 2. Verify caller is OWNER
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", child.family_id)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || callerMember.role !== "OWNER") {
      return errorResponse("OWNER_REQUIRED", "Only the family Owner can delete a child profile.", 403);
    }

    const now = new Date().toISOString();

    // 3. Revoke all active devices under this child
    await serviceClient
      .from("devices")
      .update({
        protection_status: "UNPAIRED",
        unpaired_at: now,
        auth_user_id: null,
      })
      .eq("child_id", childId);

    // 4. Cancel any open pairing sessions
    await serviceClient
      .from("device_pairing_sessions")
      .update({ cancelled_at: now })
      .eq("child_id", childId)
      .is("claimed_at", null)
      .is("cancelled_at", null);

    // 5. Soft-delete child
    const { error: updateError } = await serviceClient
      .from("children")
      .update({
        status: "ARCHIVED",
        archived_at: now,
        deleted_at: now,
      })
      .eq("id", childId);

    if (updateError) {
      return errorResponse("DELETE_FAILED", updateError.message, 500);
    }

    // 6. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: child.family_id,
      actor_type: "USER",
      actor_id: user.id,
      action: "CHILD_DELETED",
      entity_type: "CHILD",
      entity_id: child.id,
      metadata: { name: child.name },
    });

    return jsonResponse({
      success: true,
      childId: child.id,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
