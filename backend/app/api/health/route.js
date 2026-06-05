/**
 * GET /api/health  -> liveness + database connectivity check.
 */
import { pingDb } from '@/lib/supabase';
import {
  jsonSuccess,
  jsonError,
  jsonPreflight,
  withErrorHandling,
} from '@/lib/response';

// The Supabase client uses the Node.js runtime (not Edge); never cache this.
export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

export const GET = withErrorHandling(async () => {
  let dbOk = false;
  try {
    await pingDb();
    dbOk = true;
  } catch (err) {
    console.error('[health] DB check failed:', err.message);
  }

  if (!dbOk) {
    return jsonError(503, 'DB_UNAVAILABLE', 'Database tidak dapat dijangkau.');
  }

  return jsonSuccess({
    status: 'ok',
    db: 'ok',
    time: new Date().toISOString(),
  });
});

export function OPTIONS() {
  return jsonPreflight();
}
