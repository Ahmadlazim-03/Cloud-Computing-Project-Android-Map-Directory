/**
 * GET  /api/places/{id}/reviews -> list reviews for a place
 * POST /api/places/{id}/reviews -> add a review (optional feature, no API key)
 *
 * Body for POST: { rating: 1..5 (required), comment?, user_id? }
 */
import { createReview, findReviewsByPlace } from '@/lib/reviews';
import { placeExists } from '@/lib/places';
import { parseNumber } from '@/lib/validate';
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

export const GET = withErrorHandling(async (_req, ctx) => {
  const id = parseId(ctx);
  if (!(await placeExists(id))) {
    throw ApiError.notFound('Tempat tidak ditemukan.');
  }
  const reviews = await findReviewsByPlace(id);
  return jsonSuccess(reviews);
});

export const POST = withErrorHandling(async (req, ctx) => {
  const id = parseId(ctx);
  if (!(await placeExists(id))) {
    throw ApiError.notFound('Tempat tidak ditemukan.');
  }

  let body;
  try {
    body = await req.json();
  } catch {
    throw ApiError.badRequest('Body JSON tidak valid.');
  }

  const rating = parseNumber(body.rating);
  if (rating === null || !Number.isInteger(rating) || rating < 1 || rating > 5) {
    throw ApiError.badRequest('Field "rating" wajib bilangan bulat 1..5.');
  }

  const review = await createReview(id, {
    user_id: body.user_id ? String(body.user_id) : null,
    rating,
    comment: body.comment ? String(body.comment) : null,
  });

  return jsonSuccess(review, { status: 201 });
});

export function OPTIONS() {
  return jsonPreflight();
}
