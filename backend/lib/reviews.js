/**
 * Data access for the `reviews` table (optional feature, via Supabase).
 * Adding a review also refreshes the parent place's cached average rating.
 */
const { supabase } = require('./supabase');

const COLS = 'id, place_id, user_id, rating, comment, created_at';

async function findReviewsByPlace(placeId) {
  const { data, error } = await supabase
    .from('reviews')
    .select(COLS)
    .eq('place_id', placeId)
    .order('created_at', { ascending: false });
  if (error) throw new Error(error.message);
  return data || [];
}

/**
 * Insert a review, then recompute the place's average rating.
 * PostgREST has no multi-statement transaction, so we do it in two steps;
 * the average is derived from all reviews so it stays consistent on retry.
 */
async function createReview(placeId, { user_id, rating, comment }) {
  const { data: inserted, error: insertErr } = await supabase
    .from('reviews')
    .insert({
      place_id: placeId,
      user_id: user_id ?? null,
      rating,
      comment: comment ?? null,
    })
    .select(COLS)
    .single();
  if (insertErr) throw new Error(insertErr.message);

  // Recompute average from all reviews of this place and cache it on places.
  const { data: all, error: listErr } = await supabase
    .from('reviews')
    .select('rating')
    .eq('place_id', placeId);
  if (listErr) throw new Error(listErr.message);

  if (all && all.length) {
    const avg = all.reduce((sum, r) => sum + Number(r.rating), 0) / all.length;
    const rounded = Math.round(avg * 10) / 10; // 1 decimal place
    const { error: updErr } = await supabase
      .from('places')
      .update({ rating: rounded, updated_at: new Date().toISOString() })
      .eq('id', placeId);
    if (updErr) throw new Error(updErr.message);
  }

  return inserted;
}

module.exports = { findReviewsByPlace, createReview };
