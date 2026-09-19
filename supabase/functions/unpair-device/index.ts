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
    const { deviceId } = body;

    if (!deviceId) {
      return errorResponse("INVALID_INPUT", "deviceId is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Get device
    const { data: device, error: deviceError } = await serviceClient
      .from("devices")
      .select("id, family_id, child_id, name")
      .eq("id", deviceId)
      .maybeSingle();

    if (deviceError || !device) {
      return errorResponse("DEVICE_NOT_FOUND", "Device not found.", 404);
    }

    // 2. Check caller is OWNER or PARENT
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", device.family_id)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || (callerMember.role !== "OWNER" && callerMember.role !== "PARENT")) {
      return errorResponse("FORBIDDEN", "Only Owner or Parent can unpair a device.", 403);
    }

    // 3. Mark device unpaired
    const { error: updateError } = await serviceClient
      .from("devices")
      .update({
        protection_status: "UNPAIRED",
        unpaired_at: new Date().toISOString(),
        auth_user_id: null,
      })
      .eq("id", deviceId);

    if (updateError) {
      return errorResponse("UNPAIR_FAILED", updateError.message, 500);
    }

    // 4. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: device.family_id,
      actor_type: "USER",
      actor_id: user.id,
      action: "DEVICE_UNPAIRED",
      entity_type: "DEVICE",
      entity_id: device.id,
      metadata: { childId: device.child_id, name: device.name },
    });

    return jsonResponse({
      success: true,
      deviceId: device.id,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
