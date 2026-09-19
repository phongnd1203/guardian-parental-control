import { corsHeaders } from "./cors.ts";

export interface StandardErrorResponse {
  error: {
    code: string;
    message: string;
    requestId: string;
  };
}

export function errorResponse(code: string, message: string, status = 400): Response {
  const requestId = crypto.randomUUID();
  return new Response(
    JSON.stringify({
      error: {
        code,
        message,
        requestId,
      },
    }),
    {
      status,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    }
  );
}

export function jsonResponse(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
