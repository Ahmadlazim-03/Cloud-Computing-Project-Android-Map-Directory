/**
 * GET /api/categories -> { success, data:[{id,name,slug,icon}] }
 */
import { findAllCategories } from '@/lib/categories';
import { jsonSuccess, jsonPreflight, withErrorHandling } from '@/lib/response';

export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

export const GET = withErrorHandling(async () => {
  const categories = await findAllCategories();
  return jsonSuccess(categories);
});

export function OPTIONS() {
  return jsonPreflight();
}
