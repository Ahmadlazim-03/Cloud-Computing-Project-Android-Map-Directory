/**
 * Guards write endpoints. The request must carry the secret in the
 * "X-API-Key" header. The key lives only in server env (API_KEY) and is
 * never shipped to the Android app.
 *
 * Throws ApiError (caught by withErrorHandling) on failure.
 */
const { ApiError } = require('./response');

function assertApiKey(req) {
  const expected = process.env.API_KEY;
  if (!expected) {
    throw ApiError.internal('API_KEY belum dikonfigurasi di server.');
  }
  const provided = req.headers.get('x-api-key');
  if (!provided || provided !== expected) {
    throw ApiError.unauthorized('Header X-API-Key salah atau tidak ada.');
  }
}

module.exports = { assertApiKey };
