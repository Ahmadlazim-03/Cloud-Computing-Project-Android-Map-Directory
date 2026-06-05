# Android Map Directory — Backend API

REST API untuk aplikasi **Android Map Directory** (proyek Cloud Computing). Klien
(**PWA / web-based**, bukan aplikasi Android native) **tidak** mengakses database
langsung — semua data lewat REST API ini.

- **Framework:** Next.js 14 (App Router, route handlers sebagai serverless functions)
- **Database:** Supabase (PostgreSQL), diakses **server-side** via `@supabase/supabase-js`
- **Deploy:** Vercel
- **UI:** Tailwind CSS (halaman dokumentasi API sederhana di `/`)

### Arsitektur (REST API — PRD poin #2)

```
┌──────────────┐   HTTPS / JSON    ┌──────────────────────┐  server-side   ┌──────────────┐
│  PWA (web)   │ ────────────────► │  REST API (Next.js)  │ ─────────────► │   Supabase   │
│  UI·GPS·Map  │ ◄──────────────── │   /api/*  route hdl  │ ◄───────────── │  PostgreSQL  │
└──────────────┘   response JSON   └──────────────────────┘  service_role  └──────────────┘
```

- Klien PWA **hanya** memanggil endpoint `/api/*` (REST, JSON) — **tidak pernah** membaca
  database atau memanggil Supabase secara langsung. (GPS via Geolocation API browser,
  peta via library JS, rute via URL Google Maps.)
- `@supabase/supabase-js` berjalan **hanya di server** (di dalam route handler), memakai
  **service_role key** yang **tidak pernah** dikirim ke klien. Cara server mengambil data
  dari DB adalah detail internal di balik REST API.
- Karena PWA berjalan di browser, **CORS** wajib aktif (sudah ditangani + handler `OPTIONS`).

> **Catatan stack.** Kontrak di PRD menyebut struktur "Express", tetapi target deploy
> adalah **Vercel + Next.js + Supabase**. Express adalah server long-running yang tidak
> cocok untuk Vercel, jadi endpoint diimplementasikan sebagai **Next.js Route Handlers**
> (`app/api/.../route.js`) yang berjalan sebagai serverless function. Kontrak API, format
> response, model data, dan aturan keamanan **identik** dengan PRD.

---

## 1. Kontrak API

Base URL lokal: `http://localhost:3000` · Base URL produksi: `https://<proyek>.vercel.app`

| Method | Endpoint | Fungsi | Auth |
|---|---|---|---|
| GET | `/api/health` | Cek server + database hidup | — |
| GET | `/api/categories` | Daftar kategori | — |
| GET | `/api/places` | Daftar tempat (filter/search/sort/pagination) | — |
| GET | `/api/places/{id}` | Detail satu tempat (+ `reviews`) | — |
| POST | `/api/places` | (Admin) tambah tempat | `X-API-Key` |
| PUT | `/api/places/{id}` | (Admin) ubah tempat | `X-API-Key` |
| GET | `/api/places/{id}/reviews` | Daftar review | — |
| POST | `/api/places/{id}/reviews` | Tambah review (opsional) | — |

**Query params `/api/places`:** `category` (slug/id), `q` (kata kunci pada name & address),
`lat`, `lng`, `sort` (`distance`\|`rating`\|`name`), `limit` (default 50, maks 100),
`offset` (default 0).

- Jika `lat` & `lng` diberikan, tiap tempat berisi `distance_m` (Haversine, meter) dan
  `sort=distance` mengurutkan dari terdekat.

**Format sukses:** `{ "success": true, "data": ..., "meta"?: { total, limit, offset } }`

**Format error (konsisten):**
```json
{ "success": false, "error": { "code": "NOT_FOUND", "message": "Tempat tidak ditemukan." } }
```
Kode HTTP: `200` / `201` / `400` / `401` / `404` / `500` (`503` saat DB tak terjangkau di `/api/health`).

**Objek `place`:**
```json
{
  "id": 1, "name": "Kafe Literasi",
  "category": { "id": 1, "name": "Kafe", "slug": "cafe" },
  "address": "Jl. Kampus No. 1", "latitude": -7.2756, "longitude": 112.7946,
  "opening_hours": "08:00-22:00", "description": "...", "rating": 4.5,
  "photo_url": "https://...", "distance_m": 450
}
```

---

## 2. Struktur Project

```
backend/
├─ app/
│  ├─ layout.js, page.js, globals.css        # landing + dokumentasi (Tailwind)
│  └─ api/
│     ├─ health/route.js
│     ├─ categories/route.js
│     ├─ places/route.js                     # GET list + POST create
│     └─ places/[id]/
│        ├─ route.js                          # GET detail + PUT update
│        └─ reviews/route.js                  # GET + POST review
├─ lib/
│  ├─ supabase.js      # client Supabase server-side (service_role), cached + pingDb
│  ├─ response.js      # envelope sukses/error + CORS + withErrorHandling + ApiError
│  ├─ apiKey.js        # cek header X-API-Key
│  ├─ validate.js      # validasi koordinat & payload
│  ├─ haversine.js     # jarak (meter) — dipakai untuk distance_m & sort=distance
│  ├─ categories.js    # data access categories
│  ├─ places.js        # data access places (filter/search/sort/distance/pagination)
│  └─ reviews.js       # data access reviews (+ refresh rating rata-rata)
├─ db/
│  ├─ schema.sql       # tabel categories, places, reviews (jalankan di SQL Editor)
│  └─ seed.sql         # 24 tempat contoh + kategori + review (jalankan di SQL Editor)
├─ postman/AndroidMapDirectory.postman_collection.json
├─ requests.http       # tes cepat (VS Code REST Client)
├─ .env.example
└─ package.json
```

---

## 3. Menjalankan Lokal

**Prasyarat:** Node.js ≥ 18.17, akun Supabase (gratis).

### a. Buat project Supabase
1. Buka [supabase.com](https://supabase.com) → **New project**.
2. **Project Settings → API** → salin **Project URL** dan **service_role** key.
3. **SQL Editor → New query** → jalankan isi `db/schema.sql`, lalu `db/seed.sql`
   (sekali saja; ini membuat tabel + mengisi 24 tempat contoh).

### b. Konfigurasi environment
```bash
cd backend
cp .env.example .env.local       # Windows PowerShell: Copy-Item .env.example .env.local
```
Isi `.env.local`:
```
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service_role secret key dari Settings → API>
API_KEY=<hasil: node -e "console.log(require('crypto').randomBytes(24).toString('hex'))">
CORS_ORIGIN=*
```

### c. Install & jalankan
```bash
npm install
npm run dev          # http://localhost:3000
```

### d. Cek cepat
```bash
curl http://localhost:3000/api/health
curl http://localhost:3000/api/places?category=cafe
```
Atau buka `requests.http` di VS Code (extension **REST Client**), atau import
`postman/AndroidMapDirectory.postman_collection.json` ke Postman.

---

## 4. Variabel Environment

| Variabel | Wajib | Keterangan |
|---|---|---|
| `SUPABASE_URL` | ✓ | Project URL Supabase (`https://<ref>.supabase.co`) |
| `SUPABASE_SERVICE_ROLE_KEY` | ✓ | Service-role secret key; **server-only**, bypass RLS, jangan diberi prefix `NEXT_PUBLIC_` |
| `API_KEY` | ✓ | Kunci untuk endpoint tulis (header `X-API-Key`); rahasia, hanya di server |
| `CORS_ORIGIN` | — | Origin yang diizinkan, default `*` (untuk produksi bisa diisi origin PWA) |

Kredensial **tidak pernah** di-hardcode dan **tidak** dikirim ke klien PWA. Klien hanya
berbicara dengan REST API `/api/*`.

---

## 5. Deployment

### Opsi A — Vercel (utama, dipakai proyek ini)
1. Push repo ke GitHub.
2. [vercel.com](https://vercel.com) → **Add New → Project** → import repo.
3. **Root Directory:** set ke `backend` (karena Next.js ada di subfolder).
   Framework terdeteksi otomatis sebagai **Next.js** (zero-config).
4. **Environment Variables** → tambahkan `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`,
   `API_KEY`, `CORS_ORIGIN` (samakan dengan `.env.local`).
5. **Deploy.** HTTPS aktif otomatis di domain `*.vercel.app`.
6. Pastikan `schema.sql` + `seed.sql` sudah dijalankan di Supabase SQL Editor (sekali saja).

### Opsi B — Render / Railway (cadangan)
Next.js juga bisa di-deploy sebagai Node app:
- **Render:** New → Web Service → Build `npm install && npm run build`, Start `npm start`,
  set env vars yang sama. (Railway serupa: deteksi Next.js + set env vars.)
- Berguna sebagai rencana cadangan bila kuota Vercel berubah.

> **Free tier sering berubah.** Sebelum memilih, cek halaman pricing terbaru:
> Vercel → vercel.com/pricing · Render → render.com/pricing · Railway → railway.app/pricing.
> Supabase free tier mem-pause project yang idle — cek supabase.com/pricing.

### Menguji dari jaringan luar (HP)
Setelah deploy, dari browser HP (data seluler, bukan Wi-Fi lokal) buka:
```
https://<proyek>.vercel.app/api/health
https://<proyek>.vercel.app/api/places
```
Harus mengembalikan JSON valid — membuktikan API publik, bukan localhost.

---

## 6. Checklist Pengujian Endpoint

- [ ] `GET /api/health` → `{ success:true, data:{ status:"ok", db:"ok" } }`
- [ ] `GET /api/categories` → 7 kategori
- [ ] `GET /api/places` → daftar + `meta.total` (≥ 24)
- [ ] `GET /api/places?category=cafe` → hanya kategori kafe
- [ ] `GET /api/places?category=tidakada` → `data: []`, `meta.total: 0`
- [ ] `GET /api/places?q=kopi` → cocok pada name/address
- [ ] `GET /api/places?lat=-7.2756&lng=112.7946&sort=distance` → ada `distance_m`, urut menaik
- [ ] `GET /api/places?limit=5&offset=5` → 5 item, `meta` benar
- [ ] `GET /api/places/1` → 1 objek + array `reviews`
- [ ] `GET /api/places/999999` → 404 `NOT_FOUND`
- [ ] `POST /api/places` tanpa `X-API-Key` → 401 `UNAUTHORIZED`
- [ ] `POST /api/places` dengan key + body valid → 201, objek baru
- [ ] `POST /api/places` koordinat di luar rentang → 400 `BAD_REQUEST`
- [ ] `PUT /api/places/1` dengan key → 200, field terupdate
- [ ] `POST /api/places/1/reviews` rating 1..5 → 201, rating tempat ikut diperbarui
- [ ] JSON body rusak → 400 `BAD_REQUEST`

---

## 7. Keamanan & Kualitas

- **Kredensial hanya di server** (env vars), tidak pernah di response/klien PWA.
- **service_role key server-only** — bypass RLS, dipakai hanya di route handler, tidak
  pernah dikirim ke browser/PWA.
- **Endpoint tulis dilindungi** header `X-API-Key`.
- **Validasi input:** koordinat dalam rentang (lat −90..90, lng −180..180), field wajib,
  `category_id` harus ada. CHECK constraint juga di level database.
- **Akses data via Supabase client** (parameter ter-bind, bukan string SQL mentah) +
  input pencarian disanitasi → aman dari injection.
- **Error handler global** → tidak ada response tak konsisten / stack trace bocor.
- **CORS aktif** + handler `OPTIONS` (preflight) di tiap endpoint.
- **HTTPS** otomatis di Vercel.
```
