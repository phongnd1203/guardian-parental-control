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
    const { familyId, memberId } = body;

    if (!familyId || !memberId) {
      return errorResponse("INVALID_INPUT", "familyId and memberId are required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Get target member
    const { data: targetMember } = await serviceClient
      .from("family_members")
      .select("*")
      .eq("id", memberId)
      .eq("family_id", familyId)
      .maybeSingle();

    if (!targetMember) {
      return errorResponse("MEMBER_NOT_FOUND", "Member not found.", 404);
    }

    // 2. Check caller permission
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", familyId)
      .eq("user_id", user.id)
      .maybeSingle();

    const isSelfRemove = targetMember.user_id === user.id;
    const isOwner = callerMember?.role === "OWNER";

    if (!isOwner && !isSelfRemove) {
      return errorResponse("FORBIDDEN", "Only Owner can remove other members.", 403);
    }

    if (targetMember.role === "OWNER") {
      return errorResponse("LAST_OWNER_CANNOT_LEAVE", "Owner cannot leave without transferring ownership first.", 400);
    }

    // 3. Delete member
    const { error: deleteError } = await serviceClient
      .from("family_members")
      .delete()
      .eq("id", memberId);

    if (deleteError) {
      return errorResponse("REMOVE_FAILED", deleteError.message, 500);
    }

    // 4. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: familyId,
      actor_type: "USER",
      actor_id: user.id,
      action: "MEMBER_REMOVED",
      entity_type: "MEMBER",
      entity_id: memberId,
      metadata: { removedUserId: targetMember.user_id, removedRole: targetMember.role },
    });

    return jsonResponse({ success: true, memberId });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
