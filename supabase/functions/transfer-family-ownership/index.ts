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
    const { familyId, newOwnerMemberId } = body;

    if (!familyId || !newOwnerMemberId) {
      return errorResponse("INVALID_INPUT", "familyId and newOwnerMemberId are required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Verify caller is current OWNER
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("*")
      .eq("family_id", familyId)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || callerMember.role !== "OWNER") {
      return errorResponse("OWNER_REQUIRED", "Only the current Owner can transfer ownership.", 403);
    }

    // 2. Verify target member is in this family and is PARENT
    const { data: targetMember } = await serviceClient
      .from("family_members")
      .select("*")
      .eq("id", newOwnerMemberId)
      .eq("family_id", familyId)
      .maybeSingle();

    if (!targetMember) {
      return errorResponse("MEMBER_NOT_FOUND", "Target member not found in this family.", 404);
    }

    if (targetMember.id === callerMember.id) {
      return errorResponse("INVALID_OPERATION", "You are already the owner.", 400);
    }

    // 3. Update families.owner_user_id
    await serviceClient
      .from("families")
      .update({ owner_user_id: targetMember.user_id })
      .eq("id", familyId);

    // 4. Update target role to OWNER
    await serviceClient
      .from("family_members")
      .update({ role: "OWNER" })
      .eq("id", targetMember.id);

    // 5. Update caller role to PARENT
    await serviceClient
      .from("family_members")
      .update({ role: "PARENT" })
      .eq("id", callerMember.id);

    // 6. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: familyId,
      actor_type: "USER",
      actor_id: user.id,
      action: "OWNERSHIP_TRANSFERRED",
      entity_type: "FAMILY",
      entity_id: familyId,
      metadata: { previousOwnerId: user.id, newOwnerUserId: targetMember.user_id },
    });

    return jsonResponse({
      success: true,
      newOwnerUserId: targetMember.user_id,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
