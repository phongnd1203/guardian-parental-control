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

    if (authError || !user) {
      return errorResponse("UNAUTHORIZED", "User is not authenticated.", 401);
    }

    if (user.is_anonymous) {
      return errorResponse("FORBIDDEN", "Child devices cannot create a family.", 403);
    }

    const body = await req.json().catch(() => ({}));
    const rawName = body.name;

    if (!rawName || typeof rawName !== "string") {
      return errorResponse("INVALID_INPUT", "Family name is required.", 400);
    }

    const familyName = rawName.trim();
    if (familyName.length < 2 || familyName.length > 50) {
      return errorResponse("INVALID_INPUT", "Family name must be between 2 and 50 characters.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Create family
    const { data: family, error: familyError } = await serviceClient
      .from("families")
      .insert({
        name: familyName,
        owner_user_id: user.id,
      })
      .select()
      .single();

    if (familyError || !family) {
      return errorResponse("FAMILY_CREATION_FAILED", familyError?.message || "Failed to create family.", 500);
    }

    // 2. Add creator as OWNER in family_members
    const { error: memberError } = await serviceClient
      .from("family_members")
      .insert({
        family_id: family.id,
        user_id: user.id,
        role: "OWNER",
      });

    if (memberError) {
      // rollback family
      await serviceClient.from("families").delete().eq("id", family.id);
      return errorResponse("FAMILY_CREATION_FAILED", memberError.message, 500);
    }

    // 3. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: family.id,
      actor_type: "USER",
      actor_id: user.id,
      action: "FAMILY_CREATED",
      entity_type: "FAMILY",
      entity_id: family.id,
      metadata: { name: family.name },
    });

    return jsonResponse({
      family: {
        id: family.id,
        name: family.name,
        ownerUserId: family.owner_user_id,
        createdAt: family.created_at,
        updatedAt: family.updated_at,
      },
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
