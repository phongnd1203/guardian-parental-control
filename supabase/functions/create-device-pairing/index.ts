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
      return errorResponse("UNAUTHORIZED", "Authentication required.", 401);
    }

    const body = await req.json().catch(() => ({}));
    const { childId } = body;

    if (!childId) {
      return errorResponse("INVALID_INPUT", "childId is required.", 400);
    }

    const serviceClient = getServiceClient();

    // 1. Get child & family
    const { data: child, error: childError } = await serviceClient
      .from("children")
      .select("id, family_id, status")
      .eq("id", childId)
      .maybeSingle();

    if (childError || !child) {
      return errorResponse("CHILD_NOT_FOUND", "Child not found.", 404);
    }

    if (child.status !== "ACTIVE") {
      return errorResponse("INVALID_OPERATION", "Cannot pair device to an archived or deleted child profile.", 400);
    }

    // 2. Verify caller role (OWNER or PARENT)
    const { data: callerMember } = await serviceClient
      .from("family_members")
      .select("role")
      .eq("family_id", child.family_id)
      .eq("user_id", user.id)
      .maybeSingle();

    if (!callerMember || (callerMember.role !== "OWNER" && callerMember.role !== "PARENT")) {
      return errorResponse("FORBIDDEN", "Only Owner or Parent can create a device pairing session.", 403);
    }

    // 3. Generate pairingToken (high-entropy) and manualCode (6-digit)
    const rawPairingToken = crypto.randomUUID().replace(/-/g, "") + crypto.randomUUID().replace(/-/g, "");
    const rawManualCode = String(Math.floor(100000 + Math.random() * 900000));

    const tokenHash = await hashToken(rawPairingToken);
    const manualCodeHash = await hashToken(rawManualCode);

    // 10 minutes expiry
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000).toISOString();

    const { data: session, error: sessionError } = await serviceClient
      .from("device_pairing_sessions")
      .insert({
        family_id: child.family_id,
        child_id: child.id,
        token_hash: tokenHash,
        manual_code_hash: manualCodeHash,
        created_by: user.id,
        expires_at: expiresAt,
      })
      .select()
      .single();

    if (sessionError || !session) {
      return errorResponse("PAIRING_SESSION_FAILED", sessionError?.message || "Failed to create session.", 500);
    }

    return jsonResponse({
      pairingSessionId: session.id,
      pairingToken: rawPairingToken,
      manualCode: rawManualCode,
      expiresAt: session.expires_at,
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : String(err);
    return errorResponse("INTERNAL_SERVER_ERROR", message, 500);
  }
});
