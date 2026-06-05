-- ============================================================
--  Android Map Directory — PostgreSQL schema (Supabase)
--
--  Anggota 2: Database & Data Engineer
--  Versi    : 2.0 — DDL lengkap dengan 4 tabel
--
--  Run this in the Supabase Dashboard → SQL Editor (schema.sql first,
--  then seed.sql).
--
--  ARSITEKTUR: klien (Android / PWA) TIDAK menyentuh tabel langsung.
--  Client  →  REST API (Next.js /api/*)  →  Supabase (server-side).
--  Hanya server (route handler) yang mengakses DB, memakai service_role key.
-- ============================================================

-- ────────────────────────────────────────────────────────────────
-- 0.  Clean re-create (safe to run repeatedly during development)
-- ────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS favorites CASCADE;
DROP TABLE IF EXISTS reviews   CASCADE;
DROP TABLE IF EXISTS places    CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- ────────────────────────────────────────────────────────────────
-- 1.  CATEGORIES
--     Lookup table — every place belongs to exactly one category.
--     `slug` is the machine-readable key used in API filters.
--     `icon` maps to a Lucide / Material icon name in the front end.
-- ────────────────────────────────────────────────────────────────
CREATE TABLE categories (
  id    SERIAL       PRIMARY KEY,
  name  VARCHAR(80)  NOT NULL,
  slug  VARCHAR(80)  NOT NULL UNIQUE,          -- e.g. 'cafe', 'atm'
  icon  VARCHAR(80)                            -- e.g. 'coffee', 'credit-card'
);

COMMENT ON TABLE  categories          IS 'Kategori tempat (kafe, kantin, ATM, dll.)';
COMMENT ON COLUMN categories.slug     IS 'Identifier unik untuk filter API (URL-safe, lowercase)';
COMMENT ON COLUMN categories.icon     IS 'Nama ikon Lucide/Material untuk UI';

-- ────────────────────────────────────────────────────────────────
-- 2.  PLACES
--     Core table — each row is a campus-area location shown on the map.
--     Coordinates are REQUIRED and validated at the DB level.
-- ────────────────────────────────────────────────────────────────
CREATE TABLE places (
  id            SERIAL          PRIMARY KEY,
  category_id   INTEGER         REFERENCES categories(id) ON DELETE SET NULL,
  name          VARCHAR(150)    NOT NULL,
  latitude      DECIMAL(10, 7)  NOT NULL
                  CHECK (latitude  BETWEEN -90  AND  90),
  longitude     DECIMAL(10, 7)  NOT NULL
                  CHECK (longitude BETWEEN -180 AND 180),
  address       VARCHAR(255),
  opening_hours VARCHAR(50),
  description   TEXT,
  rating        DECIMAL(2, 1)
                  CHECK (rating BETWEEN 0 AND 5),
  photo_url     VARCHAR(500),
  created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes for fast lookups
CREATE INDEX idx_places_category ON places(category_id);
CREATE INDEX idx_places_name     ON places(name);

COMMENT ON TABLE  places              IS 'Tempat/lokasi di sekitar kampus yang ditampilkan di peta';
COMMENT ON COLUMN places.latitude     IS 'Lintang (WGS 84), -90 s.d. 90';
COMMENT ON COLUMN places.longitude    IS 'Bujur (WGS 84), -180 s.d. 180';
COMMENT ON COLUMN places.rating       IS 'Rata-rata rating 0.0–5.0 (dihitung dari reviews)';
COMMENT ON COLUMN places.photo_url    IS 'URL foto utama tempat (CDN/Supabase Storage)';

-- ────────────────────────────────────────────────────────────────
-- 3.  REVIEWS
--     User-submitted ratings & comments for a place.
--     Soft-linked to user_id (device ID or auth UID).
-- ────────────────────────────────────────────────────────────────
CREATE TABLE reviews (
  id         SERIAL       PRIMARY KEY,
  place_id   INTEGER      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  user_id    VARCHAR(120),                               -- device-id or auth uid
  rating     INTEGER      NOT NULL
               CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_place ON reviews(place_id);

COMMENT ON TABLE  reviews           IS 'Ulasan dan rating dari pengguna untuk setiap tempat';
COMMENT ON COLUMN reviews.user_id   IS 'Identifier pengguna (device ID / Firebase UID)';
COMMENT ON COLUMN reviews.rating    IS 'Rating integer 1–5 bintang';

-- ────────────────────────────────────────────────────────────────
-- 4.  FAVORITES
--     User's bookmarked places. Each (place_id, user_id) pair is unique.
-- ────────────────────────────────────────────────────────────────
CREATE TABLE favorites (
  id         SERIAL       PRIMARY KEY,
  place_id   INTEGER      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  user_id    VARCHAR(120) NOT NULL,                      -- device-id or auth uid
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_favorites_place_user ON favorites(place_id, user_id);
CREATE INDEX        idx_favorites_user       ON favorites(user_id);

COMMENT ON TABLE  favorites           IS 'Daftar tempat favorit per pengguna';
COMMENT ON COLUMN favorites.user_id   IS 'Identifier pengguna (wajib, karena favorit per-user)';

-- ============================================================
--  END OF SCHEMA
--  Jalankan seed.sql setelah file ini berhasil dieksekusi.
-- ============================================================
