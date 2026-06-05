/**
 * Data access for the `places` table (via Supabase / PostgREST).
 *
 * Builds the response object exactly as the API contract requires:
 *   { id, name, category:{id,name,slug}, address, latitude, longitude,
 *     opening_hours, description, rating, photo_url, distance_m }
 *
 * The directory holds only ~15–30 places, so for distance sorting we fetch the
 * filtered set, compute Haversine distance in JS, then sort + paginate in JS.
 * This keeps the code simple and correct without a custom Postgres function.
 */
const { supabase } = require('./supabase');
const { haversineMeters } = require('./haversine');

// PostgREST embed: pull the parent category through the category_id FK.
const SELECT_COLS = `
  id, name, address, latitude, longitude,
  opening_hours, description, rating, photo_url, created_at, updated_at,
  category:categories ( id, name, slug, icon )
`;

/** PostgREST `or()` is a raw filter string — strip chars that could break it. */
function sanitizeForFilter(value) {
  return String(value).replace(/[,()*\\%"]/g, ' ').trim();
}

/** Map a raw row (with embedded category) into the contract-shaped place. */
function mapPlaceRow(row, { distanceM = undefined } = {}) {
  // PostgREST returns a to-one embed as an object, but be defensive.
  const cat = Array.isArray(row.category) ? row.category[0] : row.category;
  const place = {
    id: row.id,
    name: row.name,
    category: cat ? { id: cat.id, name: cat.name, slug: cat.slug } : null,
    address: row.address,
    latitude: row.latitude !== null ? Number(row.latitude) : null,
    longitude: row.longitude !== null ? Number(row.longitude) : null,
    opening_hours: row.opening_hours,
    description: row.description,
    rating: row.rating !== null ? Number(row.rating) : null,
    photo_url: row.photo_url,
  };
  if (distanceM !== undefined) place.distance_m = distanceM;
  return place;
}

/** Compute distance_m for a row when user coords are known (else null). */
function distanceForRow(row, lat, lng) {
  if (row.latitude === null || row.longitude === null) return null;
  return haversineMeters(lat, lng, Number(row.latitude), Number(row.longitude));
}

/**
 * List places with optional filter / search / distance-sort / pagination.
 * @returns {Promise<{items: object[], total: number}>}
 */
async function listPlaces({ category, q, lat, lng, sort, limit, offset }) {
  let qb = supabase.from('places').select(SELECT_COLS);

  if (category) qb = qb.eq('category_id', category.id);
  if (q) {
    const safe = sanitizeForFilter(q);
    if (safe) qb = qb.or(`name.ilike.*${safe}*,address.ilike.*${safe}*`);
  }

  const { data, error } = await qb;
  if (error) throw new Error(error.message);

  const hasCoords = lat !== null && lng !== null;
  let rows = (data || []).map((row) => ({
    row,
    distanceM: hasCoords ? distanceForRow(row, lat, lng) : undefined,
  }));

  // Sort the full filtered set, then paginate, so distance/rating order is
  // correct across pages (not just within one page).
  rows.sort((a, b) => {
    if (sort === 'distance' && hasCoords) {
      const da = a.distanceM ?? Infinity;
      const db = b.distanceM ?? Infinity;
      if (da !== db) return da - db;
      return a.row.name.localeCompare(b.row.name);
    }
    if (sort === 'rating') {
      const ra = a.row.rating ?? -Infinity;
      const rb = b.row.rating ?? -Infinity;
      if (ra !== rb) return rb - ra; // highest first
      return a.row.name.localeCompare(b.row.name);
    }
    return a.row.name.localeCompare(b.row.name);
  });

  const total = rows.length;
  const page = rows.slice(offset, offset + limit);
  const items = page.map(({ row, distanceM }) => mapPlaceRow(row, { distanceM }));

  return { items, total };
}

/** Fetch one place. If lat/lng given, include distance_m. */
async function findPlaceById(id, { lat = null, lng = null } = {}) {
  const { data, error } = await supabase
    .from('places')
    .select(SELECT_COLS)
    .eq('id', id)
    .maybeSingle();
  if (error) throw new Error(error.message);
  if (!data) return null;

  const hasCoords = lat !== null && lng !== null;
  const distanceM = hasCoords ? distanceForRow(data, lat, lng) : undefined;
  return mapPlaceRow(data, { distanceM });
}

/** Insert a new place; returns the created place (contract shape). */
async function createPlace(data) {
  const { data: inserted, error } = await supabase
    .from('places')
    .insert({
      category_id: data.category_id,
      name: data.name,
      latitude: data.latitude,
      longitude: data.longitude,
      address: data.address ?? null,
      opening_hours: data.opening_hours ?? null,
      description: data.description ?? null,
      rating: data.rating ?? null,
      photo_url: data.photo_url ?? null,
    })
    .select('id')
    .single();
  if (error) throw new Error(error.message);
  return findPlaceById(inserted.id);
}

/**
 * Update an existing place with only the provided fields (partial update).
 * Returns the updated place, or null if id not found.
 */
async function updatePlace(id, data) {
  const allowed = [
    'category_id',
    'name',
    'latitude',
    'longitude',
    'address',
    'opening_hours',
    'description',
    'rating',
    'photo_url',
  ];
  const patch = {};
  for (const key of allowed) {
    if (data[key] !== undefined) patch[key] = data[key];
  }

  if (Object.keys(patch).length === 0) {
    // Nothing to change; just return the current row (or null if missing).
    return findPlaceById(id);
  }

  patch.updated_at = new Date().toISOString();

  const { data: updated, error } = await supabase
    .from('places')
    .update(patch)
    .eq('id', id)
    .select('id');
  if (error) throw new Error(error.message);
  if (!updated || updated.length === 0) return null;
  return findPlaceById(id);
}

async function placeExists(id) {
  const { data, error } = await supabase
    .from('places')
    .select('id')
    .eq('id', id)
    .maybeSingle();
  if (error) throw new Error(error.message);
  return Boolean(data);
}

module.exports = {
  listPlaces,
  findPlaceById,
  createPlace,
  updatePlace,
  placeExists,
  mapPlaceRow,
};
