-- ============================================================
--  Android Map Directory — PostgreSQL schema (Supabase)
--  Run this in the Supabase Dashboard → SQL Editor (schema.sql first,
--  then seed.sql).
--
--  ARSITEKTUR: klien (PWA / web) TIDAK menyentuh tabel-tabel ini langsung.
--  PWA  →  REST API (Next.js /api/*)  →  Supabase (server-side).
--  Hanya server (route handler) yang mengakses DB, memakai service_role key.
-- ============================================================

-- Clean re-create (safe to run repeatedly during development).
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS places CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- ---------- categories ----------
CREATE TABLE categories (
  id    SERIAL PRIMARY KEY,
  name  VARCHAR(80)  NOT NULL,
  slug  VARCHAR(80)  NOT NULL UNIQUE,
  icon  VARCHAR(80)
);

-- ---------- places ----------
CREATE TABLE places (
  id            SERIAL PRIMARY KEY,
  category_id   INTEGER REFERENCES categories(id) ON DELETE SET NULL,
  name          VARCHAR(150)   NOT NULL,
  latitude      DECIMAL(10, 7) NOT NULL CHECK (latitude  BETWEEN -90  AND 90),
  longitude     DECIMAL(10, 7) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
  address       VARCHAR(255),
  opening_hours VARCHAR(50),
  description   TEXT,
  rating        DECIMAL(2, 1)  CHECK (rating BETWEEN 0 AND 5),
  photo_url     VARCHAR(500),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_places_category ON places(category_id);
CREATE INDEX idx_places_name     ON places(name);

-- ---------- reviews (optional) ----------
CREATE TABLE reviews (
  id         SERIAL PRIMARY KEY,
  place_id   INTEGER NOT NULL REFERENCES places(id) ON DELETE CASCADE,
  user_id    VARCHAR(120),
  rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_place ON reviews(place_id);
