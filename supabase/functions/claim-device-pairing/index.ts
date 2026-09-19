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

    if (authError || !user) {
      return errorResponse("UNAUTHORIZED", "Child app device authentication required.", 401);
    }

    const body = await req.json().catch(() => ({}));
    const { pairingToken, manualCode, device: deviceInfo } = body;

    if (!pairingToken && !manualCode) {
      return errorResponse("INVALID_INPUT", "pairingToken or manualCode is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Locate pairing session
    let query = serviceClient.from("device_pairing_sessions").select("*");
    if (pairingToken) {
      const tokenHash = await hashToken(String(pairingToken).trim());
      query = query.eq("token_hash", tokenHash);
    } else {
      const codeHash = await hashToken(String(manualCode).trim());
      query = query.eq("manual_code_hash", codeHash);
    }

    const { data: session, error: sessionError } = await query.maybeSingle();

    if (sessionError || !session) {
      return errorResponse("PAIRING_CODE_INVALID", "Invalid pairing code or token.", 404);
    }

    if (session.claimed_at) {
      return errorResponse("PAIRING_CODE_ALREADY_USED", "This pairing session has already been claimed.", 409);
    }

    if (session.cancelled_at) {
      return errorResponse("PAIRING_CODE_INVALID", "This pairing session was cancelled.", 410);
    }

    if (new Date(session.expires_at).getTime() < Date.now()) {
      return errorResponse("PAIRING_CODE_EXPIRED", "The pairing code has expired.", 410);
    }

    // 2. Register device
    const deviceName = deviceInfo?.name?.trim() || `${deviceInfo?.manufacturer || "Child"} ${deviceInfo?.model || "Device"}`.trim();

    const { data: device, error: deviceError } = await serviceClient
      .from("devices")
      .insert({
        family_id: session.family_id,
        child_id: session.child_id,
        auth_user_id: user.id,
        name: deviceName || "Child Device",
        manufacturer: deviceInfo?.manufacturer || null,
        model: deviceInfo?.model || null,
        android_version: deviceInfo?.androidVersion ? String(deviceInfo.androidVersion) : null,
        api_level: deviceInfo?.apiLevel ? Number(deviceInfo.apiLevel) : null,
        app_version: deviceInfo?.appVersion ? String(deviceInfo.appVersion) : null,
        app_build: deviceInfo?.appBuild ? Number(deviceInfo.appBuild) : null,
        protection_status: "ACTIVE",
        last_seen_at: new Date().toISOString(),
      })
      .select()
      .single();

    if (deviceError || !device) {
      return errorResponse("DEVICE_REGISTRATION_FAILED", deviceError?.message || "Failed to register device.", 500);
    }

    // 3. Create initial permission status record
    await serviceClient.from("device_permission_status").insert({
      device_id: device.id,
    });

    // 4. Mark session claimed
    await serviceClient
      .from("device_pairing_sessions")
      .update({
        claimed_at: new Date().toISOString(),
        claimed_device_id: device.id,
      })
      .eq("id", session.id);

    // 5. Audit log
    await serviceClient.from("family_audit_logs").insert({
      family_id: session.family_id,
      actor_type: "DEVICE",
      actor_id: user.id,
      action: "DEVICE_PAIRED",
      entity_type: "DEVICE",
      entity_id: device.id,
      metadata: { childId: session.child_id, model: device.model },
    });

    return jsonResponse({
      deviceId: device.id,
      childId: device.child_id,
      familyId: device.family_id,
      pairedAt: device.paired_at,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
