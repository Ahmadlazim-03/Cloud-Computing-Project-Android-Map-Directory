/**
 * Input-validation helpers. Coordinates are the heart of this app, so they
 * are validated strictly (a bad coordinate = a broken map marker / route).
 */

function isFiniteNumber(v) {
  return typeof v === 'number' && Number.isFinite(v);
}

/** Parse a value (string from query/body, or number) into a finite number, or null. */
function parseNumber(v) {
  if (v === undefined || v === null || v === '') return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function isValidLatitude(lat) {
  return isFiniteNumber(lat) && lat >= -90 && lat <= 90;
}

function isValidLongitude(lng) {
  return isFiniteNumber(lng) && lng >= -180 && lng <= 180;
}

/** Clamp an integer into [min, max], falling back to `def` when not parseable. */
function clampInt(value, { def, min, max }) {
  const n = parseNumber(value);
  if (n === null) return def;
  const i = Math.trunc(n);
  if (i < min) return min;
  if (i > max) return max;
  return i;
}

/**
 * Validate a place payload for POST (partial=false) or PUT (partial=true).
 * @returns {string[]} error messages (empty array = valid)
 */
function validatePlacePayload(body, { partial = false } = {}) {
  const errors = [];
  const has = (k) => body[k] !== undefined && body[k] !== null;

  if (!partial || has('name')) {
    if (!body.name || String(body.name).trim() === '') {
      errors.push('Field "name" wajib diisi.');
    }
  }
  if (!partial || has('category_id')) {
    const cat = parseNumber(body.category_id);
    if (cat === null || !Number.isInteger(cat) || cat <= 0) {
      errors.push('Field "category_id" wajib berupa bilangan bulat positif.');
    }
  }
  if (!partial || has('latitude')) {
    if (!isValidLatitude(parseNumber(body.latitude))) {
      errors.push('Field "latitude" harus angka antara -90 dan 90.');
    }
  }
  if (!partial || has('longitude')) {
    if (!isValidLongitude(parseNumber(body.longitude))) {
      errors.push('Field "longitude" harus angka antara -180 dan 180.');
    }
  }
  if (has('rating')) {
    const r = parseNumber(body.rating);
    if (r === null || r < 0 || r > 5) {
      errors.push('Field "rating" harus angka antara 0 dan 5.');
    }
  }
  return errors;
}

module.exports = {
  isFiniteNumber,
  parseNumber,
  isValidLatitude,
  isValidLongitude,
  clampInt,
  validatePlacePayload,
};
