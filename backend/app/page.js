/**
 * Simple landing / API documentation page (Tailwind).
 * Serves as a human-friendly index for the deployed API on Vercel.
 */
const ENDPOINTS = [
  ['GET', '/api/health', 'Cek server & database hidup'],
  ['GET', '/api/categories', 'Daftar kategori'],
  ['GET', '/api/places', 'Daftar tempat (filter, search, sort, pagination)'],
  ['GET', '/api/places?category=cafe', 'Filter per kategori (slug atau id)'],
  ['GET', '/api/places?q=kopi', 'Cari per kata kunci'],
  ['GET', '/api/places?lat=..&lng=..&sort=distance', 'Urut berdasarkan jarak'],
  ['GET', '/api/places/{id}', 'Detail satu tempat (+ reviews)'],
  ['POST', '/api/places', 'Tambah tempat (admin, X-API-Key)'],
  ['PUT', '/api/places/{id}', 'Ubah tempat (admin, X-API-Key)'],
  ['POST', '/api/places/{id}/reviews', 'Tambah review (opsional)'],
];

const METHOD_COLOR = {
  GET: 'bg-emerald-100 text-emerald-700',
  POST: 'bg-blue-100 text-blue-700',
  PUT: 'bg-amber-100 text-amber-700',
};

export default function HomePage() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-12">
      <h1 className="text-3xl font-bold tracking-tight">
        Android Map Directory — API
      </h1>
      <p className="mt-2 text-slate-600">
        REST API untuk aplikasi direktori tempat di sekitar kampus. Aplikasi
        Android membaca data dari endpoint di bawah (bukan database langsung).
      </p>

      <a
        href="/api/health"
        className="mt-4 inline-block rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
      >
        Cek /api/health →
      </a>

      <h2 className="mt-10 text-lg font-semibold">Endpoint</h2>
      <div className="mt-3 overflow-hidden rounded-xl border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <tbody>
            {ENDPOINTS.map(([method, path, desc]) => (
              <tr key={`${method} ${path}`} className="border-b border-slate-100 last:border-0">
                <td className="px-4 py-2 align-top">
                  <span
                    className={`inline-block rounded px-2 py-0.5 text-xs font-bold ${
                      METHOD_COLOR[method] || 'bg-slate-100 text-slate-700'
                    }`}
                  >
                    {method}
                  </span>
                </td>
                <td className="px-4 py-2 font-mono text-slate-800">{path}</td>
                <td className="px-4 py-2 text-slate-500">{desc}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <p className="mt-8 text-xs text-slate-400">
        Cloud Computing project · Next.js + Supabase · deployed on Vercel
      </p>
    </main>
  );
}
