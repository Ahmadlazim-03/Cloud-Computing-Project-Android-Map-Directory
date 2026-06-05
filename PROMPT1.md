## Prompt — Anggota 1 (Backend & Cloud Deployment)

```
PERAN: Saya backend & cloud engineer di proyek kuliah Cloud Computing.

PROYEK: "Android Map Directory" — aplikasi Android yang menampilkan tempat di sekitar
kampus pada peta dan membuka rute. Aplikasi mobile TIDAK mengakses database langsung;
semua data lewat REST API yang saya bangun dan deploy ke cloud.

STACK: Node.js + Express, database PostgreSQL (atau MySQL). Response JSON.
(Jika kamu sarankan stack lain yang lebih mudah dideploy, jelaskan alasannya.)

KONTRAK API yang WAJIB diikuti:
- GET  /api/health           -> cek server hidup
- GET  /api/categories       -> { success, data:[{id,name,slug,icon}] }
- GET  /api/places           -> { success, data:[place...], meta:{total,limit,offset} }
       query params: category(slug/id), q(keyword), lat, lng, sort(distance|rating|name),
       limit(default 50), offset(default 0)
- GET  /api/places/{id}      -> satu objek place (boleh sertakan reviews)
- POST /api/places           -> (admin) tambah tempat, butuh header X-API-Key
- PUT  /api/places/{id}      -> (admin) ubah tempat, butuh header X-API-Key
- POST /api/places/{id}/reviews -> (opsional) tambah review

OBJEK place: { id, name, category:{id,name,slug}, address, latitude, longitude,
opening_hours, description, rating, photo_url, distance_m }

FORMAT ERROR konsisten: { success:false, error:{ code, message } } dengan kode HTTP yang sesuai
(400/401/404/500). Sukses pakai { success:true, data, meta? }.

MODEL DATA:
- categories(id, name, slug unik, icon)
- places(id, category_id FK, name, latitude decimal, longitude decimal, address,
  opening_hours, description, rating, photo_url, created_at, updated_at)
- reviews(id, place_id FK, user_id, rating 1..5, comment, created_at)

YANG SAYA BUTUHKAN, bertahap:
1. Struktur project Express yang rapi (folder routes, controllers, models, middleware).
2. Koneksi database + konfigurasi via environment variables (.env), JANGAN hardcode kredensial.
3. Implementasi semua endpoint GET sesuai kontrak, termასuk:
   - filter by category, pencarian by q (LIKE/ILIKE pada name & address),
   - sort by distance bila lat/lng diberikan (hitung jarak Haversine, isi field distance_m),
   - pagination limit/offset + meta.total.
4. Endpoint POST/PUT yang dilindungi middleware cek header X-API-Key (kunci dari env).
5. Validasi input: koordinat dalam rentang valid, field wajib terisi, category_id ada.
6. Middleware error handler global + helper response sukses/gagal yang konsisten.
7. Aktifkan CORS dan siapkan HTTPS-ready.
8. Skrip seed sederhana untuk mengisi beberapa data contoh (saya akan koordinasi data
   final dengan tim Database).
9. Endpoint /api/health.

DEPLOYMENT:
- Beri langkah deploy backend ke layanan hosting/VPS agar bisa diakses dari HP (bukan localhost).
- Jelaskan cara set environment variables di hosting, mengaktifkan HTTPS, dan menguji
  endpoint dari jaringan luar.
- Catatan: ketentuan free tier sering berubah, beri 2 opsi platform + cara cek paket terbaru.

OUTPUT TAMBAHAN:
- Postman collection (atau file .http) untuk semua endpoint.
- README: cara menjalankan lokal, variabel env, dan cara deploy.

Tampilkan kode lengkap per file dengan path-nya, jelaskan singkat tiap bagian, dan beri
checklist pengujian endpoint. Mulai dari struktur project dan koneksi database.
```

---
