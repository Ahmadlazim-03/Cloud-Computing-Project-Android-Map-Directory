/**
 * Data access for the `categories` table (via Supabase / PostgREST).
 */
const { supabase } = require('./supabase');

const COLS = 'id, name, slug, icon';

async function findAllCategories() {
  const { data, error } = await supabase
    .from('categories')
    .select(COLS)
    .order('name', { ascending: true });
  if (error) throw new Error(error.message);
  return data || [];
}

async function findCategoryById(id) {
  const { data, error } = await supabase
    .from('categories')
    .select(COLS)
    .eq('id', id)
    .maybeSingle();
  if (error) throw new Error(error.message);
  return data || null;
}

async function findCategoryBySlug(slug) {
  const { data, error } = await supabase
    .from('categories')
    .select(COLS)
    .eq('slug', slug)
    .maybeSingle();
  if (error) throw new Error(error.message);
  return data || null;
}

/** Resolve a category by numeric id OR by slug (for the ?category= filter). */
async function findCategoryByIdOrSlug(value) {
  const asNumber = Number(value);
  if (Number.isInteger(asNumber) && asNumber > 0) {
    const byId = await findCategoryById(asNumber);
    if (byId) return byId;
  }
  return findCategoryBySlug(String(value));
}

module.exports = {
  findAllCategories,
  findCategoryById,
  findCategoryBySlug,
  findCategoryByIdOrSlug,
};
