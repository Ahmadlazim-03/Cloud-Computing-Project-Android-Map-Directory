/**
 * Great-circle distance via the Haversine formula. Returns METERS (rounded),
 * matching the `distance_m` field in the API contract.
 *
 * NOTE: distance sorting/calculation is done in SQL for efficiency (see
 * lib/places.js). This JS version is kept for tests and ad-hoc use.
 */
const EARTH_RADIUS_M = 6371000;

const toRad = (deg) => (deg * Math.PI) / 180;

function haversineMeters(lat1, lng1, lat2, lng2) {
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(EARTH_RADIUS_M * c);
}

module.exports = { haversineMeters, EARTH_RADIUS_M };
