-- ============================================================
--  Seed data — categories + 25 campus places + sample reviews/favorites.
--
--  Koordinat di sekitar area kampus Surabaya (~ -7.27, 112.79).
--  Koordinat ini adalah contoh — silakan ganti dengan koordinat asli
--  dari Google Maps (lihat panduan di bawah).
--
--  CARA CEK KOORDINAT DI GOOGLE MAPS:
--  1. Buka https://maps.google.com
--  2. Klik kanan pada lokasi yang diinginkan
--  3. Klik koordinat yang muncul (akan otomatis ter-copy)
--  4. Format: latitude, longitude  (contoh: -7.2756123, 112.7946234)
--  5. Pastikan latitude antara -90 s.d. 90
--  6. Pastikan longitude antara -180 s.d. 180
--
--  Run di Supabase SQL Editor SETELAH schema.sql.
-- ============================================================

TRUNCATE favorites RESTART IDENTITY CASCADE;
TRUNCATE reviews   RESTART IDENTITY CASCADE;
TRUNCATE places    RESTART IDENTITY CASCADE;
TRUNCATE categories RESTART IDENTITY CASCADE;

-- ────────────────────────────────────────────────────────────────
-- SEED KATEGORI (7 kategori)
-- Slug dipakai sebagai filter API:  GET /api/places?category=cafe
-- ────────────────────────────────────────────────────────────────
INSERT INTO categories (name, slug, icon) VALUES
  ('Kafe',           'cafe',           'coffee'),
  ('Kantin',         'kantin',         'utensils'),
  ('Fotokopi',       'fotokopi',       'printer'),
  ('Kos',            'kos',            'home'),
  ('Parkir',         'parkir',         'parking'),
  ('ATM',            'atm',            'credit-card'),
  ('Layanan Kampus', 'layanan-kampus', 'building');

-- ────────────────────────────────────────────────────────────────
-- SEED TEMPAT (25 tempat realistis)
-- Setiap tempat WAJIB memiliki koordinat valid.
-- ────────────────────────────────────────────────────────────────
INSERT INTO places
  (category_id, name, latitude, longitude, address, opening_hours, description, rating, photo_url)
VALUES
  -- ══════════ KAFE (4 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='cafe'),
   'Kafe Literasi',
   -7.2756123, 112.7946234,
   'Jl. Kampus No. 1, Keputih',
   '08:00-22:00',
   'Kafe nyaman di dekat perpustakaan dengan Wi-Fi kencang dan colokan di setiap meja. Cocok untuk mengerjakan tugas.',
   4.5,
   'https://picsum.photos/seed/cafe1/400/300'),

  ((SELECT id FROM categories WHERE slug='cafe'),
   'Ngopi Santuy',
   -7.2789456, 112.7921345,
   'Jl. Teknik Kimia No. 5, Sukolilo',
   '09:00-23:00',
   'Tempat nongkrong mahasiswa yang affordable. Menu kopi mulai 8 ribu, banyak colokan listrik.',
   4.2,
   'https://picsum.photos/seed/cafe2/400/300'),

  ((SELECT id FROM categories WHERE slug='cafe'),
   'Kopi Senja',
   -7.2721890, 112.7983456,
   'Jl. Arif Rahman Hakim No. 12, Keputih',
   '10:00-22:00',
   'Spesialis kopi susu dan dessert. Interior instagramable dengan sentuhan industrial.',
   4.6,
   'https://picsum.photos/seed/cafe3/400/300'),

  ((SELECT id FROM categories WHERE slug='cafe'),
   'Teh & Cerita',
   -7.2742345, 112.7958123,
   'Jl. Gebang Lor No. 20, Keputih',
   '10:00-22:00',
   'Minuman teh premium dan camilan ringan. Suasana tenang untuk diskusi kelompok.',
   4.1,
   'https://picsum.photos/seed/cafe4/400/300'),

  -- ══════════ KANTIN (4 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='kantin'),
   'Kantin Pusat',
   -7.2768234, 112.7935678,
   'Gedung Pusat Lt. 1, Kampus',
   '07:00-17:00',
   'Kantin utama kampus dengan lebih dari 15 tenant. Menu bervariasi dari nasi padang hingga bakso.',
   4.0,
   'https://picsum.photos/seed/kantin1/400/300'),

  ((SELECT id FROM categories WHERE slug='kantin'),
   'Kantin Teknik',
   -7.2802345, 112.7910567,
   'Area Fakultas Teknik, Gedung B Lt. 1',
   '07:30-16:30',
   'Nasi campur dan es teh favorit anak teknik. Porsi besar harga mahasiswa.',
   3.9,
   'https://picsum.photos/seed/kantin2/400/300'),

  ((SELECT id FROM categories WHERE slug='kantin'),
   'Warung Bu Tatik',
   -7.2735890, 112.7968234,
   'Jl. Gebang Lor No. 8, Keputih',
   '06:00-21:00',
   'Warteg legendaris dekat kampus. Porsi besar, harga bersahabat, lauk bervariasi setiap hari.',
   4.3,
   'https://picsum.photos/seed/kantin3/400/300'),

  ((SELECT id FROM categories WHERE slug='kantin'),
   'Pojok Mie Ayam',
   -7.2725678, 112.7975345,
   'Jl. Arif Rahman Hakim No. 30, Keputih',
   '09:00-20:00',
   'Mie ayam dan bakso porsi mahasiswa. Kuah kaldu gurih, mie homemade.',
   4.2,
   'https://picsum.photos/seed/kantin4/400/300'),

  -- ══════════ FOTOKOPI (3 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='fotokopi'),
   'Fotokopi Cepat',
   -7.2759123, 112.7952456,
   'Jl. Kampus No. 3, Keputih',
   '08:00-20:00',
   'Fotokopi, jilid skripsi, dan print warna. Bisa pesan via WhatsApp untuk antar.',
   4.1,
   'https://picsum.photos/seed/foto1/400/300'),

  ((SELECT id FROM categories WHERE slug='fotokopi'),
   'Copy Center 24',
   -7.2744567, 112.7929890,
   'Jl. Keputih Tegal No. 2',
   '24 jam',
   'Fotokopi dan print 24 jam. Sangat berguna saat deadline tugas tengah malam.',
   4.4,
   'https://picsum.photos/seed/foto2/400/300'),

  ((SELECT id FROM categories WHERE slug='fotokopi'),
   'Print & Scan Hemat',
   -7.2795234, 112.7960678,
   'Jl. Gebang Wetan No. 15, Keputih',
   '09:00-21:00',
   'Print dokumen, scan, laminating, dan cetak foto. Harga paling murah di sekitar kampus.',
   3.8,
   'https://picsum.photos/seed/foto3/400/300'),

  -- ══════════ KOS (3 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='kos'),
   'Kos Putri Melati',
   -7.2810456, 112.7942123,
   'Jl. Keputih Gg. 1 No. 10',
   '24 jam',
   'Kos putri bersih. Kamar mandi dalam, AC, dekat kampus. Harga mulai 800rb/bulan.',
   4.2,
   'https://picsum.photos/seed/kos1/400/300'),

  ((SELECT id FROM categories WHERE slug='kos'),
   'Kos Putra Mandiri',
   -7.2728901, 112.7905678,
   'Jl. Gebang Kidul No. 22',
   '24 jam',
   'Kos putra, parkir luas untuk motor dan mobil, free Wi-Fi 20 Mbps.',
   4.0,
   'https://picsum.photos/seed/kos2/400/300'),

  ((SELECT id FROM categories WHERE slug='kos'),
   'Kos Eksklusif Cendana',
   -7.2773456, 112.7989012,
   'Jl. Cendana No. 4, Keputih',
   '24 jam',
   'Kos eksklusif ber-AC, dapur bersama, laundry, dan security 24 jam.',
   4.7,
   'https://picsum.photos/seed/kos3/400/300'),

  -- ══════════ PARKIR (3 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='parkir'),
   'Parkir Gedung Rektorat',
   -7.2762345, 112.7938012,
   'Depan Gedung Rektorat',
   '06:00-22:00',
   'Area parkir utama kampus. Tersedia parkir motor dan mobil, dijaga petugas.',
   3.7,
   'https://picsum.photos/seed/parkir1/400/300'),

  ((SELECT id FROM categories WHERE slug='parkir'),
   'Parkir Perpustakaan',
   -7.2751678, 112.7949345,
   'Samping Perpustakaan Pusat',
   '07:00-21:00',
   'Parkir teduh di bawah pohon, dekat perpustakaan dan gedung kuliah.',
   3.9,
   'https://picsum.photos/seed/parkir2/400/300'),

  ((SELECT id FROM categories WHERE slug='parkir'),
   'Parkir Fakultas Sains',
   -7.2806890, 112.7950234,
   'Area Fakultas Sains, Gedung C',
   '06:00-21:00',
   'Parkir motor luas untuk mahasiswa sains dan teknik. Gratis.',
   3.8,
   'https://picsum.photos/seed/parkir3/400/300'),

  -- ══════════ ATM (3 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='atm'),
   'ATM Center BNI',
   -7.2766123, 112.7944567,
   'Lobi Gedung Pusat, Lt. 1',
   '24 jam',
   'ATM tarik tunai dan setor tunai BNI. Tersedia 3 mesin.',
   4.1,
   'https://picsum.photos/seed/atm1/400/300'),

  ((SELECT id FROM categories WHERE slug='atm'),
   'ATM Mandiri',
   -7.2748345, 112.7931890,
   'Jl. Kampus No. 7, dekat Kantin Pusat',
   '24 jam',
   'ATM Mandiri dengan fitur setor tunai. Biasanya tidak antri pagi hari.',
   4.0,
   'https://picsum.photos/seed/atm2/400/300'),

  ((SELECT id FROM categories WHERE slug='atm'),
   'ATM BCA',
   -7.2784567, 112.7972123,
   'Area Komersial Kampus, Blok D',
   '24 jam',
   'ATM BCA dengan setor tunai dan tarik tunai. Paling ramai saat awal bulan.',
   4.2,
   'https://picsum.photos/seed/atm3/400/300'),

  -- ══════════ LAYANAN KAMPUS (5 tempat) ══════════
  ((SELECT id FROM categories WHERE slug='layanan-kampus'),
   'Perpustakaan Pusat',
   -7.2753234, 112.7948890,
   'Jl. Kampus No. 1, Gedung Perpustakaan',
   '08:00-20:00',
   'Perpustakaan utama kampus dengan 3 lantai. Ruang baca, diskusi, dan koleksi jurnal digital.',
   4.6,
   'https://picsum.photos/seed/lib1/400/300'),

  ((SELECT id FROM categories WHERE slug='layanan-kampus'),
   'Klinik Kampus',
   -7.2771890, 112.7926456,
   'Gedung Kesehatan Mahasiswa, Lt. 1',
   '08:00-16:00',
   'Layanan kesehatan dasar gratis untuk mahasiswa aktif. Tersedia dokter umum dan obat dasar.',
   4.3,
   'https://picsum.photos/seed/clinic1/400/300'),

  ((SELECT id FROM categories WHERE slug='layanan-kampus'),
   'BAAK (Administrasi Akademik)',
   -7.2760567, 112.7933234,
   'Gedung Rektorat Lt. 2, Ruang 201',
   '08:00-15:00',
   'Pengurusan KRS, transkrip nilai, surat keterangan, dan administrasi akademik lainnya.',
   3.6,
   'https://picsum.photos/seed/baak1/400/300'),

  ((SELECT id FROM categories WHERE slug='layanan-kampus'),
   'Masjid Kampus Al-Hikmah',
   -7.2779234, 112.7955678,
   'Jl. Kampus No. 9, Area Pusat',
   '04:00-21:00',
   'Masjid utama kampus. Area wudhu luas, tersedia mukena dan Al-Quran. Sholat Jumat teratur.',
   4.8,
   'https://picsum.photos/seed/masjid1/400/300'),

  ((SELECT id FROM categories WHERE slug='layanan-kampus'),
   'Pusat Kegiatan Mahasiswa (PKM)',
   -7.2745678, 112.7940123,
   'Gedung PKM, Kampus',
   '08:00-21:00',
   'Gedung untuk kegiatan UKM, organisasi mahasiswa, dan acara kampus. Tersedia ruang rapat.',
   4.0,
   'https://picsum.photos/seed/pkm1/400/300');

-- ────────────────────────────────────────────────────────────────
-- SEED REVIEWS (contoh ulasan)
-- ────────────────────────────────────────────────────────────────
INSERT INTO reviews (place_id, user_id, rating, comment) VALUES
  (1,  'device-001', 5, 'Wi-Fi kencang, cocok buat ngerjain tugas sampai malam.'),
  (1,  'device-002', 4, 'Kopinya enak tapi agak ramai sore hari.'),
  (4,  'device-003', 4, 'Teh nya enak-enak, suasana tenang.'),
  (5,  'device-001', 4, 'Murah meriah, porsi pas buat mahasiswa.'),
  (5,  'device-004', 3, 'Lumayan tapi siang kadang habis menunya.'),
  (9,  'device-002', 5, 'Print cepat dan hasilnya bagus, harga bersahabat.'),
  (21, 'device-005', 5, 'Perpustakaan terbaik, koleksi lengkap.'),
  (24, 'device-001', 5, 'Masjid bersih dan nyaman, parkir luas.');

-- ────────────────────────────────────────────────────────────────
-- SEED FAVORITES (contoh favorit)
-- ────────────────────────────────────────────────────────────────
INSERT INTO favorites (place_id, user_id) VALUES
  (1,  'device-001'),  -- Kafe Literasi
  (3,  'device-001'),  -- Kopi Senja
  (5,  'device-001'),  -- Kantin Pusat
  (21, 'device-001'),  -- Perpustakaan Pusat
  (1,  'device-002'),  -- Kafe Literasi
  (7,  'device-002'),  -- Warung Bu Tatik
  (24, 'device-003');  -- Masjid Kampus

-- ============================================================
--  VERIFIKASI CEPAT — jalankan query ini untuk mengecek data:
-- ============================================================
--  SELECT COUNT(*) AS total_places FROM places;                     -- >= 25
--  SELECT COUNT(*) AS places_without_category FROM places
--    WHERE category_id IS NULL;                                      -- = 0
--  SELECT COUNT(*) AS invalid_coords FROM places
--    WHERE latitude NOT BETWEEN -90 AND 90
--       OR longitude NOT BETWEEN -180 AND 180;                       -- = 0
--  SELECT c.name, COUNT(p.id) AS jumlah
--    FROM categories c LEFT JOIN places p ON p.category_id = c.id
--    GROUP BY c.name ORDER BY jumlah DESC;                           -- semua > 0
-- ============================================================
