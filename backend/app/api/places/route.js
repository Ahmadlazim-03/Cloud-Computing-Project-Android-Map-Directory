/**
 * GET  /api/places   -> list with filter / search / sort / pagination
 * POST /api/places    -> (admin) create a place; requires X-API-Key
 */
import { listPlaces, createPlace } from '@/lib/places';
import { findCategoryByIdOrSlug, findCategoryById } from '@/lib/categories';
import { assertApiKey } from '@/lib/apiKey';
import { parseNumber, clampInt, validatePlacePayload } from '@/lib/validate';
import {
  jsonSuccess,
  jsonPreflight,
  withErrorHandling,
  ApiError,
} from '@/lib/response';

export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

const ALLOWED_SORTS = ['distance', 'rating', 'name'];

export const GET = withErrorHandling(async (req) => {
  const sp = new URL(req.url).searchParams;

  const categoryParam = sp.get('category');
  const q = (sp.get('q') || '').trim() || null;
  const lat = parseNumber(sp.get('lat'));
  const lng = parseNumber(sp.get('lng'));
  let sort = (sp.get('sort') || '').toLowerCase();
  if (!ALLOWED_SORTS.includes(sort)) sort = 'name';

  const limit = clampInt(sp.get('limit'), { def: 50, min: 1, max: 100 });
  const offset = clampInt(sp.get('offset'), { def: 0, min: 0, max: 1000000 });

  // Resolve category filter (by id or slug). Unknown category -> empty result.
  let category = null;
  if (categoryParam) {
    category = await findCategoryByIdOrSlug(categoryParam);
    if (!category) {
      return jsonSuccess([], { meta: { total: 0, limit, offset } });
    }
  }

  const { items, total } = await listPlaces({
    category,
    q,
    lat,
    lng,
    sort,
    limit,
    offset,
  });

  return jsonSuccess(items, { meta: { total, limit, offset } });
});

export const POST = withErrorHandling(async (req) => {
  assertApiKey(req);

  let body;
  try {
    body = await req.json();
  } catch {
    throw ApiError.badRequest('Body JSON tidak valid.');
  }

  const errors = validatePlacePayload(body, { partial: false });
  if (errors.length) {
    throw ApiError.badRequest(errors.join(' '));
  }

  // category_id must reference an existing category.
  const category = await findCategoryById(parseNumber(body.category_id));
  if (!category) {
    throw ApiError.badRequest('category_id tidak ditemukan.');
  }

  const created = await createPlace({
    category_id: category.id,
    name: String(body.name).trim(),
    latitude: parseNumber(body.latitude),
    longitude: parseNumber(body.longitude),
    address: body.address ?? null,
    opening_hours: body.opening_hours ?? null,
    description: body.description ?? null,
    rating: body.rating !== undefined ? parseNumber(body.rating) : null,
    photo_url: body.photo_url ?? null,
  });

  return jsonSuccess(created, { status: 201 });
});

export function OPTIONS() {
  return jsonPreflight();
}
