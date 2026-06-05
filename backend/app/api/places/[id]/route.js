/**
 * GET /api/places/{id}  -> one place object (includes reviews[])
 * PUT /api/places/{id}  -> (admin) update a place; requires X-API-Key
 */
import { findPlaceById, updatePlace } from '@/lib/places';
import { findReviewsByPlace } from '@/lib/reviews';
import { findCategoryById } from '@/lib/categories';
import { assertApiKey } from '@/lib/apiKey';
import { parseNumber, validatePlacePayload } from '@/lib/validate';
import {
  jsonSuccess,
  jsonPreflight,
  withErrorHandling,
  ApiError,
} from '@/lib/response';

export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

function parseId(ctx) {
  const id = parseNumber(ctx?.params?.id);
  if (id === null || !Number.isInteger(id) || id <= 0) {
    throw ApiError.badRequest('ID tempat tidak valid.');
  }
  return id;
}

export const GET = withErrorHandling(async (req, ctx) => {
  const id = parseId(ctx);
  const sp = new URL(req.url).searchParams;
  const lat = parseNumber(sp.get('lat'));
  const lng = parseNumber(sp.get('lng'));

  const place = await findPlaceById(id, { lat, lng });
  if (!place) {
    throw ApiError.notFound('Tempat tidak ditemukan.');
  }

  // Attach reviews (empty array if none / table unused).
  place.reviews = await findReviewsByPlace(id);
  return jsonSuccess(place);
});

export const PUT = withErrorHandling(async (req, ctx) => {
  assertApiKey(req);
  const id = parseId(ctx);

  let body;
  try {
    body = await req.json();
  } catch {
    throw ApiError.badRequest('Body JSON tidak valid.');
  }

  const errors = validatePlacePayload(body, { partial: true });
  if (errors.length) {
    throw ApiError.badRequest(errors.join(' '));
  }

  if (body.category_id !== undefined) {
    const category = await findCategoryById(parseNumber(body.category_id));
    if (!category) {
      throw ApiError.badRequest('category_id tidak ditemukan.');
    }
  }

  // Normalize provided numeric fields.
  const patch = { ...body };
  if (patch.name !== undefined) patch.name = String(patch.name).trim();
  if (patch.category_id !== undefined) patch.category_id = parseNumber(patch.category_id);
  if (patch.latitude !== undefined) patch.latitude = parseNumber(patch.latitude);
  if (patch.longitude !== undefined) patch.longitude = parseNumber(patch.longitude);
  if (patch.rating !== undefined) patch.rating = parseNumber(patch.rating);

  const updated = await updatePlace(id, patch);
  if (!updated) {
    throw ApiError.notFound('Tempat tidak ditemukan.');
  }
  return jsonSuccess(updated);
});

export function OPTIONS() {
  return jsonPreflight();
}
