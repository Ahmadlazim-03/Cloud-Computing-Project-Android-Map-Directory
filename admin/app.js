/**
 * ============================================================
 *  Android Map Directory — Admin Panel JavaScript
 *
 *  Mengelola CRUD tempat melalui REST API backend.
 *  Endpoint:
 *    GET    /api/places         → daftar tempat
 *    POST   /api/places         → tambah tempat baru
 *    PUT    /api/places/{id}    → edit tempat
 *    GET    /api/categories     → daftar kategori
 *
 *  Header autentikasi: X-API-Key
 * ============================================================
 */

// ── State ──
let API_BASE = localStorage.getItem('admin_api_base') || '';
let API_KEY  = localStorage.getItem('admin_api_key')  || '';
let places   = [];
let categories = [];
let editingId  = null;
let searchQuery = '';
let filterCategory = '';

// ── DOM Ready ──
document.addEventListener('DOMContentLoaded', () => {
  // Restore config
  const baseInput = document.getElementById('config-base-url');
  const keyInput  = document.getElementById('config-api-key');
  baseInput.value = API_BASE;
  keyInput.value  = API_KEY;

  baseInput.addEventListener('change', () => {
    API_BASE = baseInput.value.replace(/\/+$/, '');
    localStorage.setItem('admin_api_base', API_BASE);
    updateConnectionStatus();
    loadData();
  });

  keyInput.addEventListener('change', () => {
    API_KEY = keyInput.value;
    localStorage.setItem('admin_api_key', API_KEY);
    updateConnectionStatus();
  });

  // Search
  document.getElementById('search-input').addEventListener('input', (e) => {
    searchQuery = e.target.value.toLowerCase();
    renderTable();
  });

  // Category filter
  document.getElementById('filter-category').addEventListener('change', (e) => {
    filterCategory = e.target.value;
    renderTable();
  });

  // Modal
  document.getElementById('modal-backdrop').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) closeModal();
  });

  // Keyboard shortcuts
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });

  // Initial load
  updateConnectionStatus();
  if (API_BASE) loadData();
});

// ── API Helpers ──
async function apiFetch(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (API_KEY) headers['X-API-Key'] = API_KEY;

  const res = await fetch(url, { ...options, headers });
  const json = await res.json();

  if (!json.success) {
    const msg = json.error?.message || `HTTP ${res.status}`;
    throw new Error(msg);
  }

  return json;
}

function updateConnectionStatus() {
  const el = document.getElementById('connection-status');
  const ok = API_BASE && API_KEY;
  el.className = `connection-status ${ok ? 'connected' : 'disconnected'}`;
  el.innerHTML = `
    <span class="connection-dot"></span>
    ${ok ? 'Terhubung' : 'Belum dikonfigurasi'}
  `;
}

// ── Data Loading ──
async function loadData() {
  if (!API_BASE) {
    showToast('Atur Base URL terlebih dahulu.', 'error');
    return;
  }

  showTableLoading();

  try {
    const [placesRes, catsRes] = await Promise.all([
      apiFetch('/api/places?limit=100'),
      apiFetch('/api/categories'),
    ]);

    places     = placesRes.data || [];
    categories = catsRes.data  || [];

    populateCategoryFilter();
    updateStats();
    renderTable();
    showToast(`${places.length} tempat berhasil dimuat.`, 'success');
  } catch (err) {
    showToast(`Gagal memuat data: ${err.message}`, 'error');
    renderEmpty('Gagal memuat data. Periksa konfigurasi API.');
  }
}

function populateCategoryFilter() {
  const select = document.getElementById('filter-category');
  select.innerHTML = '<option value="">Semua Kategori</option>';
  categories.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.slug;
    opt.textContent = c.name;
    select.appendChild(opt);
  });
}

function updateStats() {
  document.getElementById('stat-total').textContent = places.length;
  document.getElementById('stat-categories').textContent = categories.length;

  const validCoords = places.filter(p =>
    p.latitude >= -90 && p.latitude <= 90 &&
    p.longitude >= -180 && p.longitude <= 180
  ).length;
  document.getElementById('stat-coords').textContent = `${validCoords}/${places.length}`;

  const avgRating = places.length
    ? (places.reduce((sum, p) => sum + (p.rating || 0), 0) / places.length).toFixed(1)
    : '0.0';
  document.getElementById('stat-rating').textContent = avgRating;
}

// ── Table Rendering ──
function getFilteredPlaces() {
  return places.filter(p => {
    const matchSearch = !searchQuery ||
      p.name.toLowerCase().includes(searchQuery) ||
      (p.address || '').toLowerCase().includes(searchQuery);
    const matchCategory = !filterCategory ||
      (p.category && p.category.slug === filterCategory);
    return matchSearch && matchCategory;
  });
}

function renderTable() {
  const tbody = document.getElementById('places-tbody');
  const filtered = getFilteredPlaces();

  if (filtered.length === 0) {
    renderEmpty(searchQuery || filterCategory
      ? 'Tidak ada tempat yang cocok dengan filter.'
      : 'Belum ada data tempat. Klik "Tambah Tempat" untuk memulai.'
    );
    return;
  }

  tbody.innerHTML = filtered.map((p, i) => `
    <tr style="animation: fadeInRow 0.3s ease ${i * 0.03}s both;">
      <td class="name-cell">${escapeHtml(p.name)}</td>
      <td>
        ${p.category
          ? `<span class="category-badge">${escapeHtml(p.category.name)}</span>`
          : '<span style="color:var(--clr-text-3)">—</span>'}
      </td>
      <td class="coord-cell">${p.latitude?.toFixed(7) || '—'}</td>
      <td class="coord-cell">${p.longitude?.toFixed(7) || '—'}</td>
      <td>${escapeHtml(p.address || '—')}</td>
      <td>${escapeHtml(p.opening_hours || '—')}</td>
      <td>
        <div class="rating-display">
          <span class="rating-star">★</span>
          <span class="rating-value">${p.rating !== null ? p.rating.toFixed(1) : '—'}</span>
        </div>
      </td>
      <td>
        ${p.photo_url
          ? `<img src="${escapeHtml(p.photo_url)}" alt="${escapeHtml(p.name)}" class="photo-thumb" loading="lazy" onerror="this.style.display='none'">`
          : '<span style="color:var(--clr-text-3)">—</span>'}
      </td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-ghost btn-sm" onclick="openEditModal(${p.id})" title="Edit">✏️ Edit</button>
        </div>
      </td>
    </tr>
  `).join('');

  // Add row fade-in animation
  if (!document.getElementById('row-animation-style')) {
    const style = document.createElement('style');
    style.id = 'row-animation-style';
    style.textContent = `
      @keyframes fadeInRow {
        from { opacity: 0; transform: translateY(8px); }
        to   { opacity: 1; transform: translateY(0); }
      }
    `;
    document.head.appendChild(style);
  }
}

function showTableLoading() {
  const tbody = document.getElementById('places-tbody');
  tbody.innerHTML = `
    <tr>
      <td colspan="9">
        <div class="loading-overlay">
          <div class="spinner"></div>
          <span class="loading-text">Memuat data tempat...</span>
        </div>
      </td>
    </tr>
  `;
}

function renderEmpty(message) {
  const tbody = document.getElementById('places-tbody');
  tbody.innerHTML = `
    <tr>
      <td colspan="9">
        <div class="empty-state">
          <div class="empty-icon">📍</div>
          <p>${message}</p>
        </div>
      </td>
    </tr>
  `;
}

// ── Modal ──
function openAddModal() {
  editingId = null;
  document.getElementById('modal-title').textContent = 'Tambah Tempat Baru';
  document.getElementById('form-submit-btn').textContent = '💾 Simpan';
  clearForm();
  populateFormCategories();
  openModal();
}

function openEditModal(id) {
  const place = places.find(p => p.id === id);
  if (!place) return;

  editingId = id;
  document.getElementById('modal-title').textContent = `Edit: ${place.name}`;
  document.getElementById('form-submit-btn').textContent = '💾 Update';

  populateFormCategories();
  fillForm(place);
  openModal();
}

function openModal() {
  document.getElementById('modal-backdrop').classList.add('active');
  document.body.style.overflow = 'hidden';
  // Focus first input
  setTimeout(() => {
    document.getElementById('field-name').focus();
  }, 200);
}

function closeModal() {
  document.getElementById('modal-backdrop').classList.remove('active');
  document.body.style.overflow = '';
  editingId = null;
}

function populateFormCategories() {
  const select = document.getElementById('field-category');
  select.innerHTML = '<option value="">— Pilih Kategori —</option>';
  categories.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id;
    opt.textContent = `${c.name} (${c.slug})`;
    select.appendChild(opt);
  });
}

function fillForm(place) {
  document.getElementById('field-name').value        = place.name || '';
  document.getElementById('field-category').value     = place.category?.id || '';
  document.getElementById('field-address').value      = place.address || '';
  document.getElementById('field-latitude').value     = place.latitude ?? '';
  document.getElementById('field-longitude').value    = place.longitude ?? '';
  document.getElementById('field-opening-hours').value = place.opening_hours || '';
  document.getElementById('field-description').value  = place.description || '';
  document.getElementById('field-photo-url').value    = place.photo_url || '';
}

function clearForm() {
  ['field-name', 'field-category', 'field-address', 'field-latitude',
   'field-longitude', 'field-opening-hours', 'field-description', 'field-photo-url']
    .forEach(id => {
      const el = document.getElementById(id);
      el.value = '';
      el.classList.remove('error');
    });
  // Clear any visible errors
  document.querySelectorAll('.field-error').forEach(el => el.classList.remove('visible'));
}

// ── Form Validation ──
function validateForm() {
  let valid = true;
  const errors = {};

  const name = document.getElementById('field-name').value.trim();
  if (!name) {
    errors.name = 'Nama tempat wajib diisi.';
    valid = false;
  }

  const categoryId = document.getElementById('field-category').value;
  if (!categoryId) {
    errors.category = 'Pilih kategori.';
    valid = false;
  }

  const lat = parseFloat(document.getElementById('field-latitude').value);
  if (isNaN(lat) || lat < -90 || lat > 90) {
    errors.latitude = 'Latitude harus antara -90 dan 90.';
    valid = false;
  }

  const lng = parseFloat(document.getElementById('field-longitude').value);
  if (isNaN(lng) || lng < -180 || lng > 180) {
    errors.longitude = 'Longitude harus antara -180 dan 180.';
    valid = false;
  }

  // Show/hide errors
  ['name', 'category', 'latitude', 'longitude'].forEach(f => {
    const field = document.getElementById(`field-${f}`);
    const errEl = document.getElementById(`error-${f}`);
    if (errors[f]) {
      field.classList.add('error');
      if (errEl) { errEl.textContent = errors[f]; errEl.classList.add('visible'); }
    } else {
      field.classList.remove('error');
      if (errEl) { errEl.classList.remove('visible'); }
    }
  });

  return valid;
}

// ── Form Submission ──
async function handleFormSubmit(e) {
  e.preventDefault();
  if (!validateForm()) return;

  const btn = document.getElementById('form-submit-btn');
  const originalText = btn.textContent;
  btn.disabled = true;
  btn.textContent = '⏳ Menyimpan...';

  const payload = {
    name:          document.getElementById('field-name').value.trim(),
    category_id:   parseInt(document.getElementById('field-category').value, 10),
    address:       document.getElementById('field-address').value.trim() || null,
    latitude:      parseFloat(document.getElementById('field-latitude').value),
    longitude:     parseFloat(document.getElementById('field-longitude').value),
    opening_hours: document.getElementById('field-opening-hours').value.trim() || null,
    description:   document.getElementById('field-description').value.trim() || null,
    photo_url:     document.getElementById('field-photo-url').value.trim() || null,
  };

  try {
    if (editingId) {
      await apiFetch(`/api/places/${editingId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
      showToast(`"${payload.name}" berhasil diperbarui!`, 'success');
    } else {
      await apiFetch('/api/places', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast(`"${payload.name}" berhasil ditambahkan!`, 'success');
    }

    closeModal();
    await loadData();
  } catch (err) {
    showToast(`Gagal menyimpan: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = originalText;
  }
}

// ── Toast Notifications ──
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span><span>${escapeHtml(message)}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('exit');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ── Utility ──
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = String(text);
  return div.innerHTML;
}

// ── Reload Button ──
function reloadData() {
  loadData();
}
