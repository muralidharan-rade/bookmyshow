const API_HOST = window.location.hostname || 'localhost';
const API_BASE = `${window.location.protocol}//${API_HOST}:8080/api`;
const state = {
  regions: [], theatres: [], screens: [], seats: [], seatTypes: [], movies: [],
  shows: [], showSeatTypes: [], showSeats: [], users: [], bookings: [], payments: [],
  selectedDate: null
};

const $ = selector => document.querySelector(selector);
const paths = {
  region: 'regions',
  theatre: 'theatres',
  screen: 'screens',
  'seat-type': 'seat-types',
  seat: 'seats',
  movie: 'movies',
  show: 'shows',
  'show-seat-type': 'show-seat-types',
  'show-seat': 'show-seats',
  user: 'users',
  booking: 'bookings',
  payment: 'payments'
};

const fields = {
  region: [{ name: 'name', label: 'Region name', placeholder: 'e.g. Mumbai' }],
  theatre: [
    { name: 'name', label: 'Theatre name', placeholder: 'e.g. PVR Phoenix' },
    { name: 'regionId', label: 'Region', source: 'regions', required: true }
  ],
  screen: [
    { name: 'name', label: 'Screen name', placeholder: 'e.g. Screen 1' },
    { name: 'theatreId', label: 'Theatre', source: 'theatres', required: true, optionLabel: item => `${item.name} · ${item.regionName}` },
    { name: 'features', label: 'Features', type: 'multiple', values: ['THREE_D', 'TWO_D', 'DOLBY'] }
  ],
  'seat-type': [{ name: 'name', label: 'Seat type', placeholder: 'e.g. Recliner' }],
  seat: [
    { name: 'number', label: 'Seat number', placeholder: 'e.g. A1' },
    { name: 'row', label: 'Row number', type: 'number', min: 1 },
    { name: 'column', label: 'Column number', type: 'number', min: 1 },
    { name: 'seatTypeId', label: 'Seat type', source: 'seatTypes', required: true, optionLabel: item => item.type },
    { name: 'screenId', label: 'Screen', source: 'screens', required: true, optionLabel: item => `${item.name} · ${item.theatreName}` }
  ],
  movie: [
    { name: 'name', label: 'Movie name', placeholder: 'e.g. Interstellar' },
    { name: 'features', label: 'Features', type: 'multiple', values: ['THREE_D', 'TWO_D', 'DOLBY'] }
  ],
  show: [
    { name: 'movieId', label: 'Movie', source: 'movies', required: true },
    { name: 'screenId', label: 'Screen', source: 'screens', required: true, optionLabel: item => `${item.name} · ${item.theatreName}` },
    { name: 'startTime', label: 'Start time', type: 'datetime-local' },
    { name: 'endTime', label: 'End time', type: 'datetime-local' },
    { name: 'features', label: 'Features', type: 'multiple', values: ['THREE_D', 'TWO_D', 'DOLBY'] }
  ],
  'show-seat-type': [
    { name: 'showId', label: 'Show', source: 'shows', required: true, optionLabel: item => `${item.movie} · ${formatDate(item.startTime)}` },
    { name: 'seatTypeId', label: 'Seat type', source: 'seatTypes', required: true, optionLabel: item => item.type },
    { name: 'price', label: 'Price', type: 'number', min: 0.01, step: 0.01 }
  ],
  'show-seat': [
    { name: 'showId', label: 'Show', source: 'shows', required: true, optionLabel: showOptionLabel }
  ],
  user: [
    { name: 'name', label: 'Full name', placeholder: 'e.g. Alex Morgan' },
    { name: 'email', label: 'Email', type: 'email', placeholder: 'name@example.com' },
    { name: 'password', label: 'Password', type: 'password' }
  ],
  booking: [
    { name: 'userId', label: 'User', source: 'users', required: true },
    { name: 'showId', label: 'Show', source: 'shows', required: true, optionLabel: item => `${item.movie} · ${formatDate(item.startTime)}` },
    { name: 'showSeatIds', label: 'Show seats', source: 'showSeats', type: 'multiple', optionLabel: item => `${item.seat} · ${item.status}` },
    { name: 'status', label: 'Status', values: ['PENDING', 'SUCCESS', 'CANCELLED', 'FAILED'] },
    { name: 'amount', label: 'Amount', type: 'number', min: 0.01, step: 0.01 }
  ],
  payment: [
    { name: 'referenceNumber', label: 'Reference number' },
    { name: 'amount', label: 'Amount', type: 'number', min: 0.01, step: 0.01 },
    { name: 'mode', label: 'Payment mode', values: ['CASH', 'CARD', 'UPI'] },
    { name: 'status', label: 'Status', values: ['SUCCESS', 'CANCELLED', 'IN_PROGRESS'] },
    { name: 'bookingId', label: 'Booking', source: 'bookings', required: true, optionLabel: item => `#${item.id} · ${item.status}` }
  ]
};

async function api(path, options = {}) {
  const headers = { Accept: 'application/json', ...options.headers };
  if (options.body) headers['Content-Type'] = 'application/json';
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });
  if (!response.ok) {
    let message = `Request failed (${response.status}).`;
    try {
      const body = await response.json();
      message = body.detail || body.message || message;
    } catch { /* The response may not contain JSON. */ }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

async function refreshData() {
  const entries = Object.entries(paths);
  const results = await Promise.allSettled(entries.map(([, path]) => api(`/${path}`)));
  const failures = [];
  entries.forEach(([entity], index) => {
    const key = {
      region: 'regions', theatre: 'theatres', screen: 'screens', 'seat-type': 'seatTypes',
      seat: 'seats', movie: 'movies', show: 'shows', 'show-seat-type': 'showSeatTypes',
      'show-seat': 'showSeats', user: 'users', booking: 'bookings', payment: 'payments'
    }[entity];
    if (results[index].status === 'fulfilled') {
      state[key] = results[index].value;
    } else {
      failures.push({ entity, error: results[index].reason });
    }
  });
  state.loadFailures = failures;
  render();
}

function render() {
  $('#region-count').textContent = state.regions.length;
  $('#theatre-count').textContent = state.theatres.length;
  $('#screen-count').textContent = state.screens.length;
  const failures = state.loadFailures || [];
  $('#connection-label').textContent = failures.length
    ? `${failures.length} data source${failures.length === 1 ? '' : 's'} unavailable`
    : 'API connected';
  $('.connection').classList.toggle('connected', failures.length === 0);
  $('.connection').classList.toggle('failed', failures.length > 0);
  renderVenueTree();
  renderSchedule();
  renderFields();
}

function dateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getWeekDates() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() + index);
    return date;
  });
}

function showDateKey(show) {
  const date = new Date(show.startTime);
  return Number.isNaN(date.getTime()) ? '' : dateKey(date);
}

function renderSchedule() {
  const dates = getWeekDates();
  const todayKey = dateKey(dates[0]);
  if (!state.selectedDate || !dates.some(date => dateKey(date) === state.selectedDate)) {
    const firstScheduledDay = dates.find(date =>
      state.shows.some(show => showDateKey(show) === dateKey(date)));
    state.selectedDate = firstScheduledDay ? dateKey(firstScheduledDay) : todayKey;
  }
  $('#schedule-range').textContent =
    `${dates[0].toLocaleDateString(undefined, { month: 'short', day: 'numeric' })} – ${dates[6].toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}`;

  const regionSelect = $('#schedule-region');
  const currentRegion = regionSelect.value;
  regionSelect.innerHTML = '<option value="">All regions</option>' +
    state.regions.map(region => `<option value="${region.id}">${escapeHtml(region.name)}</option>`).join('');
  regionSelect.value = currentRegion;

  const movieSelect = $('#schedule-movie');
  const currentMovie = movieSelect.value;
  movieSelect.innerHTML = '<option value="">All movies</option>' +
    state.movies.map(movie => `<option value="${movie.id}">${escapeHtml(movie.name)}</option>`).join('');
  movieSelect.value = currentMovie;

  const regionId = regionSelect.value ? Number(regionSelect.value) : null;
  const movieId = movieSelect.value ? Number(movieSelect.value) : null;
  const matchingShows = state.shows.filter(show => {
    if (movieId !== null && show.movieId !== movieId) return false;
    if (regionId === null) return true;
    const screen = state.screens.find(item => item.id === show.screenId);
    const theatre = screen && state.theatres.find(item => item.id === screen.theatreId);
    return theatre?.regionId === regionId;
  });
  $('#today-show-count').textContent = matchingShows.filter(show => showDateKey(show) === todayKey).length;
  $('#week-movie-count').textContent = new Set(
    matchingShows.filter(show => dates.some(date => showDateKey(show) === dateKey(date)))
      .map(show => show.movieId)
  ).size;

  $('#week-strip').innerHTML = dates.map(date => {
    const key = dateKey(date);
    const count = matchingShows.filter(show => showDateKey(show) === key).length;
    const active = key === state.selectedDate;
    const dayLabel = key === todayKey ? 'Today' : date.toLocaleDateString(undefined, { weekday: 'short' });
    return `<button class="day-chip${active ? ' active' : ''}" type="button" data-date="${key}" aria-pressed="${active}"><span>${dayLabel}</span><strong>${date.getDate()}</strong><small>${count} ${count === 1 ? 'show' : 'shows'}</small></button>`;
  }).join('');

  const selectedDate = dates.find(date => dateKey(date) === state.selectedDate);
  $('#selected-day-label').textContent = state.selectedDate === todayKey
    ? 'Today'
    : selectedDate.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });

  const dayShows = matchingShows
    .filter(show => showDateKey(show) === state.selectedDate)
    .sort((left, right) => new Date(left.startTime) - new Date(right.startTime));
  $('#selected-day-count').textContent = `${dayShows.length} ${dayShows.length === 1 ? 'show' : 'shows'}`;
  const scheduleDependencies = new Set(['show', 'movie', 'screen', 'theatre', 'region']);
  const scheduleFailures = (state.loadFailures || [])
    .filter(failure => scheduleDependencies.has(failure.entity));
  const scheduleError = $('#schedule-error');
  scheduleError.hidden = scheduleFailures.length === 0;
  $('#schedule-error-message').textContent = scheduleFailures.length
    ? `Couldn't load ${scheduleFailures.map(failure => paths[failure.entity]).join(', ')}: ${scheduleFailures[0].error.message}`
    : '';
  $('#schedule-empty').hidden = dayShows.length > 0;
  $('#schedule-list').hidden = dayShows.length === 0;
  $('#schedule-empty-title').textContent = matchingShows.length === 0
    ? 'No shows match these filters'
    : 'No shows scheduled for this day';
  $('#schedule-empty-copy').textContent = matchingShows.length === 0
    ? 'Try another region or movie, or create a new show.'
    : 'Pick another day above, or add a show to your schedule.';

  $('#schedule-list').innerHTML = dayShows.map(show => {
    const screen = state.screens.find(item => item.id === show.screenId);
    const theatre = state.theatres.find(item => item.id === (show.theatreId || screen?.theatreId));
    const region = state.regions.find(item => item.id === (show.regionId || theatre?.regionId));
    const movie = state.movies.find(item => item.id === show.movieId);
    return `<article class="show-card">
      <div class="show-time"><strong>${formatTime(show.startTime)}</strong><span>${formatTime(show.endTime)}</span></div>
      <div class="show-divider"></div>
      <div class="movie-poster" aria-hidden="true"><span>▶</span></div>
      <div class="show-details">
        <h4>${escapeHtml(show.movie || movie?.name || 'Untitled movie')}</h4>
        <div class="show-venue">${escapeHtml(show.theatre || theatre?.name || 'Theatre')} <span>·</span> ${escapeHtml(show.screen || screen?.name || 'Screen')}</div>
        <div class="show-meta">${show.region || region ? `<span class="region-tag">${escapeHtml(show.region || region.name)}</span>` : ''}${(show.features || []).map(feature => `<span class="feature-tag">${escapeHtml(feature.replace('_', ' '))}</span>`).join('')}</div>
      </div>
      <div class="show-duration">${durationLabel(show.startTime, show.endTime)}</div>
    </article>`;
  }).join('');
}

function formatTime(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? '—'
    : date.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' });
}

function durationLabel(start, end) {
  const durationMinutes = Math.round((new Date(end) - new Date(start)) / 60000);
  if (!Number.isFinite(durationMinutes) || durationMinutes <= 0) return '';
  const hours = Math.floor(durationMinutes / 60);
  const minutes = durationMinutes % 60;
  return hours ? `${hours}h${minutes ? ` ${minutes}m` : ''}` : `${minutes}m`;
}

function renderVenueTree() {
  const list = $('#venue-list');
  const hasVenues = state.regions.length > 0;
  list.hidden = !hasVenues;
  $('#empty-state').hidden = hasVenues;
  list.innerHTML = state.regions.map(region => {
    const theatres = state.theatres.filter(theatre => theatre.regionId === region.id);
    const theatreMarkup = theatres.map(theatre => {
      const screens = state.screens.filter(screen => screen.theatreId === theatre.id);
      return `<div class="theatre-row"><span class="theatre-marker">▤</span><span>${escapeHtml(theatre.name)}</span><span class="count-pill">${screens.length} ${screens.length === 1 ? 'screen' : 'screens'}</span></div>` +
        screens.map(screen => `<div class="screen-row"><span class="screen-marker">▣</span>${escapeHtml(screen.name)}</div>`).join('');
    }).join('');
    return `<section class="venue-region"><div class="region-row"><span class="region-marker">⌖</span><span>${escapeHtml(region.name)}</span><span class="count-pill">${theatres.length} ${theatres.length === 1 ? 'theatre' : 'theatres'}</span></div>${theatreMarkup}</section>`;
  }).join('');
}

function renderFields() {
  const entity = $('#entity-type').value;
  $('#entity-fields').innerHTML = fields[entity].map(renderField).join('');
  const submitButton = $('#entity-form').querySelector('[type="submit"]');
  submitButton.innerHTML = entity === 'show-seat'
    ? 'Create show seats for all screen seats <span>→</span>'
    : `Create <span>→</span>`;
}

function showOptionLabel(show) {
  const screen = state.screens.find(item => item.id === show.screenId);
  const theatre = state.theatres.find(item => item.id === (show.theatreId || screen?.theatreId));
  return `${show.movie || 'Movie'} · ${show.screen || screen?.name || 'Screen'} · ${theatre?.name || 'Theatre'} · ${formatDate(show.startTime)}`;
}

function renderField(field) {
  const required = field.required === false ? '' : ' required';
  if (field.source || field.values) {
    const options = field.source
      ? state[field.source].map(item => ({
          value: item.id,
          label: field.optionLabel ? field.optionLabel(item) : item.name || item.email || item.id
        }))
      : field.values.map(value => ({ value, label: value }));
    const multiple = field.type === 'multiple';
    const multipleAttrs = multiple ? ' multiple size="3"' : '';
    const selectOptions = options.map(option =>
      `<option value="${escapeHtml(option.value)}">${escapeHtml(option.label)}</option>`).join('');
    return `<label for="field-${field.name}">${escapeHtml(field.label)}</label><select id="field-${field.name}" name="${field.name}"${multipleAttrs}${required}>${multiple ? '' : '<option value="">Choose one</option>'}${selectOptions}</select>`;
  }
  const type = field.type || 'text';
  const min = field.min === undefined ? '' : ` min="${field.min}"`;
  const step = field.step === undefined ? '' : ` step="${field.step}"`;
  const maxLength = type === 'text' ? ' maxlength="150"' : '';
  const placeholder = field.placeholder ? ` placeholder="${escapeHtml(field.placeholder)}"` : '';
  return `<label for="field-${field.name}">${escapeHtml(field.label)}</label><input id="field-${field.name}" name="${field.name}" type="${type}"${min}${step}${maxLength}${placeholder}${required}>`;
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : 'unscheduled';
}

function showNotice(message, isError = false) {
  const notice = $('#notice');
  notice.textContent = message;
  notice.classList.toggle('error', isError);
}

$('#entity-type').addEventListener('change', () => {
  renderFields();
  showNotice('');
});

$('#week-strip').addEventListener('click', event => {
  const button = event.target.closest('[data-date]');
  if (!button) return;
  state.selectedDate = button.dataset.date;
  renderSchedule();
});

$('#schedule-region').addEventListener('change', renderSchedule);
$('#schedule-movie').addEventListener('change', renderSchedule);
$('#schedule-retry').addEventListener('click', () => {
  showNotice('');
  refreshData().catch(error => showNotice(error.message, true));
});
document.querySelectorAll('.nav-link').forEach(link => {
  link.addEventListener('click', () => {
    document.querySelectorAll('.nav-link').forEach(item => item.classList.toggle('active', item === link));
  });
});

$('#create-show-button').addEventListener('click', () => {
  $('#entity-type').value = 'show';
  renderFields();
  $('#setup').scrollIntoView({ behavior: 'smooth', block: 'start' });
});

$('#entity-form').addEventListener('submit', async event => {
  event.preventDefault();
  const entity = $('#entity-type').value;
  const form = event.currentTarget;
  const button = form.querySelector('button[type="submit"]');
  const originalText = button.innerHTML;
  const formData = new FormData(form);
  const payload = {};

  for (const field of fields[entity]) {
    let value;
    if (field.type === 'multiple') {
      value = formData.getAll(field.name);
      if (field.source) value = value.map(Number);
      else if (value.length === 0) continue;
    } else {
      value = formData.get(field.name);
      if (value === '' || value === null) continue;
      if (field.type === 'number' || field.source) value = Number(value);
    }
    payload[field.name] = value;
  }
  if (entity === 'seat-type') payload.name = payload.name.trim();

  button.disabled = true;
  button.textContent = 'Saving…';
  showNotice('');
  try {
    const result = entity === 'show-seat'
      ? await api(`/shows/${payload.showId}/show-seats`, { method: 'POST', body: '{}' })
      : await api(`/${paths[entity]}`, { method: 'POST', body: JSON.stringify(payload) });
    form.reset();
    await refreshData();
    const successMessage = entity === 'show-seat'
      ? result.createdSeats > 0
        ? `Created ${result.createdSeats} show seats (${result.totalSeats} screen seats total).`
        : `Show seats already exist for all ${result.totalSeats} screen seats.`
      : `${$('#entity-type').selectedOptions[0].textContent} created successfully.`;
    showNotice(successMessage);
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    button.disabled = false;
    button.innerHTML = originalText;
  }
});

$('#entity-fields').addEventListener('change', event => {
  if (event.target.name === 'showId' && $('#entity-type').value === 'booking') {
    const showId = Number(event.target.value);
    const showSeats = $('#entity-fields').querySelector('[name="showSeatIds"]');
    const selected = new Set(Array.from(showSeats.selectedOptions, option => option.value));
    showSeats.innerHTML = state.showSeats
      .filter(seat => seat.showId === showId && seat.status === 'AVAILABLE')
      .map(seat => `<option value="${seat.id}">${escapeHtml(seat.seat)} · ${escapeHtml(seat.status)}</option>`).join('');
    for (const option of showSeats.options) option.selected = selected.has(option.value);
  }
});

refreshData().catch(error => {
  $('.connection').classList.add('failed');
  $('#connection-label').textContent = 'API unavailable';
  showNotice(error.message, true);
});
