## Prompt — Anggota 2 (Database & Data + Admin Web)

```
PERAN: Saya data & database engineer di proyek kuliah Cloud Computing.

PROYEK: "Android Map Directory" — direktori tempat kampus berbasis peta. Koordinat
latitude & longitude adalah inti aplikasi (untuk marker & navigasi).

DATABASE: PostgreSQL (atau MySQL). Server/API dibangun anggota lain memakai skema ini,
jadi skema harus stabil dan terdokumentasi.

MODEL DATA yang harus saya buat:
- categories(id PK, name, slug unik, icon)
- places(id PK, category_id FK->categories, name NOT NULL, latitude decimal(10,7) NOT NULL,
  longitude decimal(10,7) NOT NULL, address, opening_hours, description, rating decimal(2,1),
  photo_url, created_at, updated_at)
- reviews(id PK, place_id FK->places, user_id, rating int 1..5, comment, created_at)
- favorites(id PK, place_id FK->places, user_id, created_at)
Relasi: categories 1—N places 1—N reviews/favorites.

YANG SAYA BUTUHKAN, bertahap:
1. Skrip SQL pembuatan tabel lengkap (DDL) dengan PRIMARY KEY, FOREIGN KEY, indeks pada
   category_id, dan constraint: latitude antara -90..90, longitude antara -180..180,
   rating 0..5, slug unik.
2. ERD ringkas (boleh format teks/mermaid) + kamus data (penjelasan tiap kolom & tipe).
3. Data seed REALISTIS minimal 20–30 tempat di sekitar kampus dengan kategori beragam
   (kafe, kantin, fotokopi, ATM, parkir, kos, layanan kampus). Setiap tempat WAJIB punya
   koordinat valid. (Beri saya format agar saya bisa mengisi koordinat asli; sediakan
   contoh terisi + cara cek koordinat di Google Maps.)
4. Seed kategori beserta slug yang dipakai filter API (mis. cafe, canteen, photocopy, atm).
5. Aturan validasi data di level database + saran validasi tambahan di backend.

ADMIN WEB (fitur tambahan):
6. Halaman web sederhana (HTML + sedikit JS, boleh juga form di framework backend) untuk
   CRUD tempat: list, tambah, edit. Form harus mengirim ke endpoint backend:
   POST /api/places dan PUT /api/places/{id} dengan header X-API-Key.
   Sertakan field: name, category, address, latitude, longitude, opening_hours,
   description, photo_url. Validasi koordinat di sisi form juga.
7. Tampilkan daftar tempat dari GET /api/places di halaman admin.

OUTPUT:
- File SQL (skema + seed), ERD, kamus data, dan folder admin web.
- Checklist verifikasi data: jumlah tempat >=20, semua koordinat terisi & valid,
  setiap tempat punya kategori.

Tampilkan SQL lengkap, jelaskan tiap tabel, lalu lanjut ke ERD, seed, dan admin web.
Mulai dari DDL skema.
```

---