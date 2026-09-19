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
    const { familyId, memberId, newRole } = body;

    if (!familyId || !memberId || !newRole) {
      return errorResponse("INVALID_INPUT", "familyId, memberId and newRole are required.", 400);
    }

    if (newRole !== "PARENT" && newRole !== "VIEWER") {
      return errorResponse("INVALID_INPUT", "newRole must be PARENT or VIEWER.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Check caller is OWNER
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", familyId)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || callerMember.role !== "OWNER") {
      return errorResponse("OWNER_REQUIRED", "Only the family Owner can change member roles.", 403);
    }

    // 2. Get target member
    const { data: targetMember } = await serviceClient
      .from("family_members")
      .select("*")
      .eq("id", memberId)
      .eq("family_id", familyId)
      .maybeSingle();

    if (!targetMember) {
      return errorResponse("MEMBER_NOT_FOUND", "Member not found in this family.", 404);
    }

    if (targetMember.role === "OWNER") {
      return errorResponse("FORBIDDEN", "Cannot change Owner role via this action. Use ownership transfer.", 400);
    }

    const oldRole = targetMember.role;

    // 3. Update role
    const { error: updateError } = await serviceClient
      .from("family_members")
      .update({ role: newRole })
      .eq("id", memberId);

    if (updateError) {
      return errorResponse("UPDATE_FAILED", updateError.message, 500);
    }

    // 4. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: familyId,
      actor_type: "USER",
      actor_id: user.id,
      action: "MEMBER_ROLE_CHANGED",
      entity_type: "MEMBER",
      entity_id: memberId,
      metadata: { oldRole, newRole },
    });

    return jsonResponse({
      success: true,
      memberId,
      oldRole,
      newRole,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
