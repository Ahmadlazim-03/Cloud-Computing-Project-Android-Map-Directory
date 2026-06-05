/**
 * Server-side Supabase client (PostgREST data API).
 *
 * The Android app NEVER talks to Supabase directly — only this Next.js backend
 * does, using the SERVICE_ROLE key. That key bypasses Row Level Security, so it
 * must stay server-only: it lives in SUPABASE_SERVICE_ROLE_KEY (NOT a
 * NEXT_PUBLIC_* var) and is never sent to the client or the mobile app.
 *
 * On Vercel a warm Lambda may handle many requests, so we cache one client on
 * `globalThis` to avoid re-creating it per request.
 */
const { createClient } = require('@supabase/supabase-js');

function createSupabase() {
  const url = process.env.SUPABASE_URL;
  const key = process.env.SUPABASE_SERVICE_ROLE_KEY;

  if (!url || !key) {
    console.error(
      '[supabase] SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY belum di-set. ' +
        'Copy .env.example -> .env.local (lokal) atau set di Vercel env vars.'
    );
  }

  return createClient(url, key, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
}

const globalForSupabase = globalThis;
const supabase = globalForSupabase.__amdSupabase || createSupabase();
if (process.env.NODE_ENV !== 'production') {
  globalForSupabase.__amdSupabase = supabase;
}

/**
 * Connectivity probe used by /api/health. Throws on failure.
 * NOTE: a `head:true` count query returns no error even if the table is
 * missing, so we do a real (tiny) SELECT that surfaces PGRST/connection errors.
 */
async function pingDb() {
  const { error } = await supabase.from('categories').select('id').limit(1);
  if (error) throw new Error(error.message);
}

module.exports = { supabase, pingDb };
