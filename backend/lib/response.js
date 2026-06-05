/**
 * Consistent JSON envelopes + CORS for every API route handler.
 *
 * Success:  { success: true,  data, meta? }
 * Error:    { success: false, error: { code, message } }
 */
const { NextResponse } = require('next/server');

function corsHeaders() {
  const origin = process.env.CORS_ORIGIN || '*';
  return {
    'Access-Control-Allow-Origin': origin,
    'Access-Control-Allow-Methods': 'GET,POST,PUT,OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, X-API-Key',
    'Access-Control-Max-Age': '86400',
  };
}

function jsonSuccess(data, { status = 200, meta } = {}) {
  const body = { success: true, data };
  if (meta) body.meta = meta;
  return NextResponse.json(body, { status, headers: corsHeaders() });
}

function jsonError(status, code, message) {
  return NextResponse.json(
    { success: false, error: { code, message } },
    { status, headers: corsHeaders() }
  );
}

/** Response for CORS preflight (OPTIONS) requests. */
function jsonPreflight() {
  return new NextResponse(null, { status: 204, headers: corsHeaders() });
}

/**
 * Typed error that route handlers can `throw`; withErrorHandling turns it
 * into the standard error envelope.
 */
class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
  static badRequest(message) {
    return new ApiError(400, 'BAD_REQUEST', message);
  }
  static unauthorized(message = 'API key tidak valid.') {
    return new ApiError(401, 'UNAUTHORIZED', message);
  }
  static notFound(message = 'Data tidak ditemukan.') {
    return new ApiError(404, 'NOT_FOUND', message);
  }
  static internal(message = 'Terjadi kesalahan pada server.') {
    return new ApiError(500, 'SERVER_ERROR', message);
  }
}

/**
 * Wraps an async route handler so thrown ApiErrors become proper responses
 * and any unexpected error becomes a clean 500 (logged server-side).
 */
function withErrorHandling(handler) {
  return async (req, ctx) => {
    try {
      return await handler(req, ctx);
    } catch (err) {
      if (err instanceof ApiError) {
        return jsonError(err.status, err.code, err.message);
      }
      console.error('[error]', err);
      return jsonError(500, 'SERVER_ERROR', 'Terjadi kesalahan pada server.');
    }
  };
}

module.exports = {
  jsonSuccess,
  jsonError,
  jsonPreflight,
  corsHeaders,
  ApiError,
  withErrorHandling,
};
