## Prompt — Anggota 5 (QA, Integrasi, Dokumentasi & HKI)

```
PERAN: Saya QA & integration engineer sekaligus penanggung jawab dokumentasi dan HKI
di proyek kuliah Cloud Computing. Saya juga me-lead fitur tambahan Favorit/Review (UI Android).

PROYEK: "Android Map Directory" — aplikasi Android + REST API cloud + database + GPS +
routing. Tim 5 orang. Penilaian: 35% Android, 25% Backend/API/Cloud, 15% Database,
15% Dokumentasi/Demo, 10% Dokumen HKI.

KONTRAK API & MODEL DATA: sama dengan tim (endpoint /categories, /places, /places/{id},
POST/PUT terlindungi X-API-Key; place punya id,name,category,address,latitude,longitude,
opening_hours,description,rating,photo_url,distance_m; error { success:false, error:{code,message} }).

YANG SAYA BUTUHKAN, dibagi 4 bagian:

A. PENGUJIAN (QA)
1. Rencana pengujian + daftar test case yang memetakan ke kriteria:
   - Uji API: /places, /categories, /places/{id} memberi JSON benar DARI JARINGAN LUAR.
   - Uji Data: minimal 15–30 tempat berkoordinat valid muncul sebagai marker.
   - Uji GPS: app minta izin lokasi, baca lokasi, tetap aman saat GPS mati / izin ditolak.
   - Uji Routing: memilih tempat membuka rute ke koordinat tujuan.
   - Uji Koneksi: pesan jelas saat server down / internet mati / response kosong.
2. Postman collection + skenario uji negatif (404, 400, 401 tanpa API key, payload invalid).
3. Template laporan hasil uji (tabel: kasus, langkah, ekspektasi, hasil, status, bukti).

B. INTEGRASI
4. Checklist integrasi end-to-end (Android <-> API <-> DB) + cara reproduksi alur demo:
   buka app -> izin GPS -> pilih/cari tempat -> detail -> buka rute.
5. Panduan smoke test setelah deployment (uji dari HP/jaringan luar, bukan localhost).

C. FITUR FAVORIT/REVIEW (kontribusi kode)
6. UI Android (Kotlin) untuk: menandai tempat favorit dan memberi rating/komentar sederhana
   di layar Detail, memanggil endpoint POST /api/places/{id}/reviews dan menampilkan rating.
   (Saya koordinasi dengan backend untuk endpoint & database untuk tabel reviews/favorites.)
   Sertakan state kosong & error handling.

D. DOKUMENTASI & HKI
7. Diagram arsitektur sistem (Android - REST API - Cloud Server - Database - Map Service)
   dalam format yang rapi (mermaid/teks) + penjelasan alur data.
8. Kerangka dokumentasi proyek: pendahuluan, arsitektur, API, model data, panduan instalasi,
   panduan demo, hasil pengujian.
9. Skrip/storyboard video demo end-to-end (alur yang harus direkam) + outline slide presentasi.
10. Kerangka DOKUMEN HKI untuk aplikasi/karya cipta: bagian-bagian yang umumnya diperlukan
    (judul ciptaan, deskripsi singkat, pencipta/anggota, fungsi & kebaruan, teknologi,
    tahun pembuatan). Beri template isian; saya akan cek persyaratan resmi terkini.

OUTPUT: dokumen rencana uji + laporan, Postman collection, kode fitur favorit/review,
diagram arsitektur, kerangka dokumentasi, storyboard demo, dan template dokumen HKI.
Mulai dari rencana pengujian + daftar test case, lalu diagram arsitektur.
```

---