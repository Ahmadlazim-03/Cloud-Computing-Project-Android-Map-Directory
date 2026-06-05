## Prompt — Anggota 3 (Android Core: Navigasi, List, Detail)

```
PERAN: Saya Android developer (bagian core UI) di proyek kuliah Cloud Computing.

PROYEK: "Android Map Directory". App mengambil data tempat dari REST API cloud dan
menampilkannya. Saya menangani navigasi aplikasi, layar Home & Kategori, List Tempat,
dan Detail Tempat, termasuk lapisan networking dan error handling. (Peta & GPS ditangani
rekan lain, jadi siapkan struktur yang mudah diintegrasikan dengan layar peta.)

STACK: Kotlin + Jetpack (ViewModel, Navigation Component/Compose Navigation),
Retrofit + OkHttp untuk networking, Coil/Glide untuk gambar, Material 3.
(Boleh sarankan Jetpack Compose atau XML — pilih satu dan konsisten.)

KONTRAK API (base URL https://<domain>/api):
- GET /api/categories  -> { success, data:[{id,name,slug,icon}] }
- GET /api/places       -> { success, data:[place...], meta:{total,limit,offset} }
  query: category, q, lat, lng, sort, limit, offset
- GET /api/places/{id}  -> satu objek place
place = { id, name, category:{id,name,slug}, address, latitude, longitude,
opening_hours, description, rating, photo_url, distance_m }
error = { success:false, error:{code,message} }

YANG SAYA BUTUHKAN, bertahap:
1. Struktur project Android yang rapi (pola MVVM): data model (DTO), Retrofit service,
   repository, ViewModel, UI.
2. Setup Retrofit + OkHttp (base URL via BuildConfig/konstanta), parsing JSON ke data class
   sesuai kontrak (termasuk objek nested category dan meta).
3. Layar HOME & KATEGORI: daftar kategori (chip/grid dari /categories) + daftar tempat
   ringkas (kartu: nama, kategori, jarak distance_m, rating) + kolom pencarian.
4. Layar LIST: ambil /places, dukung filter kategori dan pencarian (q) via query param.
5. Layar DETAIL: ambil /places/{id}, tampilkan foto (photo_url), nama, kategori, alamat,
   jam buka, rating, deskripsi, jarak, dan tombol "Buka Rute" (saya cukup sediakan
   tombol + callback berisi latitude/longitude; logika intent rute dikerjakan rekan Maps/GPS,
   sepakati signature fungsinya).
6. Navigasi antar layar (Home -> Detail; Home <-> Peta lewat tab/menu).
7. ERROR HANDLING & STATE: loading state, empty state (data kosong), dan error state
   (internet mati / server gagal / response kosong) dengan tombol "coba lagi".
   Tampilkan pesan ramah, jangan crash.
8. UI pencarian & filter (fitur tambahan) yang memanggil query param q & category.

OUTPUT:
- Kode per file dengan path (mis. app/src/main/java/.../). 
- Penjelasan singkat arsitektur + cara mengganti base URL.
- Catatan titik integrasi dengan modul Peta/GPS (data tempat & callback rute).

Asumsikan endpoint sudah ada (bisa pakai data dummy/mock dulu bila API belum online).
Mulai dari struktur project + setup Retrofit + data model.
```

---
