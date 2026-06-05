## Prompt — Anggota 4 (Android Maps, GPS & Routing)

```
PERAN: Saya Android developer (bagian peta, GPS, dan routing) di proyek kuliah Cloud Computing.

PROYEK: "Android Map Directory". Saya menangani: menampilkan peta, marker tempat dari API,
membaca lokasi GPS pengguna, menghitung jarak, dan membuka rute ke tempat tujuan lewat intent.
Rekan lain menangani navigasi/list/detail; saya menyediakan layar Peta dan fungsi rute yang
bisa dipanggil dari layar Detail.

STACK: Kotlin + Google Maps SDK for Android. Lokasi via FusedLocationProviderClient
(Google Play Services Location). Routing dengan membuka aplikasi peta via Intent
(geo: URI atau https://www.google.com/maps/dir/?...). 
(Jika menyarankan osmdroid/OpenStreetMap karena tanpa API key, jelaskan trade-off-nya.)

DATA dari API (objek place): { id, name, category, address, latitude, longitude, ...,
distance_m }. Saya menerima daftar place (dari modul data/rekan) untuk dirender sebagai marker.

YANG SAYA BUTUHKAN, bertahap:
1. Setup Google Maps SDK: dependency, API key di tempat aman (mis. local.properties /
   manifest placeholder), dan SupportMapFragment/Compose Map.
2. Layar PETA: render peta + satu marker per tempat berdasarkan latitude/longitude,
   info window berisi nama + kategori + jarak; klik info window -> aksi "Buka Rute".
3. IZIN LOKASI: minta runtime permission ACCESS_FINE/COARSE_LOCATION dengan alur yang benar
   (rationale, handle ditolak, handle "jangan tanya lagi").
4. AMBIL LOKASI pengguna via FusedLocationProvider; tampilkan tombol "lokasi saya" dan
   pindahkan kamera ke posisi pengguna.
5. HITUNG JARAK pengguna ke tiap tempat (Location.distanceBetween / Haversine) untuk
   ditampilkan; sediakan utilitas yang bisa dipakai modul list juga.
6. ROUTING: fungsi openRoute(destLat, destLng, label) yang melempar Intent ke aplikasi peta
   untuk navigasi ke koordinat tujuan. Sediakan fungsi ini agar bisa dipanggil dari layar
   Detail rekan (sepakati signature-nya).
7. ERROR HANDLING khusus lokasi: GPS mati (cek setting, beri pesan/aksi nyalakan),
   izin ditolak (tetap bisa lihat peta & marker tanpa lokasi/jarak), tidak ada koneksi peta.

OUTPUT:
- Kode per file dengan path, langkah mendapatkan & memasang Google Maps API key,
  dan permission yang perlu ditambahkan di AndroidManifest.
- Penjelasan singkat + checklist uji: marker tampil, lokasi terbaca, jarak benar,
  rute terbuka, aman saat GPS mati / izin ditolak.

Boleh pakai data tempat dummy dulu jika API belum siap. Mulai dari setup Maps SDK + render marker.
```

---