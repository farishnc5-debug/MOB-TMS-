import { db } from "./firebase-init.js";
import {
  doc,
  setDoc,
  updateDoc,
  onSnapshot,
  serverTimestamp,
  Timestamp,
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";
import {
  generateSessionId,
  geocode,
  reverseGeocode,
  getRoute,
  formatDistance,
  formatDuration,
  formatEta,
  timeAgo,
  haversineMeters,
  buildTrackingUrl,
  buildWhatsAppUrl,
} from "./utils.js";

const STORAGE_KEY = "ynm_tracking_sessions";
const DEFAULT_CENTER = [21.4858, 39.1925]; // Jeddah, KSA

// ---- DOM ----
const destSearch = document.getElementById("dest-search");
const destSuggestions = document.getElementById("dest-suggestions");
const destChosen = document.getElementById("dest-chosen");
const destLabel = document.getElementById("dest-label");
const tripLabelInput = document.getElementById("trip-label");
const phoneInput = document.getElementById("recipient-phone");
const generateBtn = document.getElementById("generate-btn");
const shareCard = document.getElementById("share-card");
const shareLink = document.getElementById("share-link");
const copyBtn = document.getElementById("copy-btn");
const whatsappBtn = document.getElementById("whatsapp-btn");
const sessionListEl = document.getElementById("session-list");
const sessionEmptyEl = document.getElementById("session-empty");
const statusBanner = document.getElementById("status-banner");
const statRow = document.getElementById("stat-row");
const statStatus = document.getElementById("stat-status");
const statDistance = document.getElementById("stat-distance");
const statEta = document.getElementById("stat-eta");
const statUpdated = document.getElementById("stat-updated");
const sessionActions = document.getElementById("session-actions");
const closeSessionBtn = document.getElementById("close-session-btn");
const removeSessionBtn = document.getElementById("remove-session-btn");

// ---- Map ----
const map = L.map("map").setView(DEFAULT_CENTER, 12);
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
  attribution: "&copy; OpenStreetMap contributors",
  maxZoom: 19,
}).addTo(map);

const destIcon = L.divIcon({
  html: "🏁",
  className: "emoji-icon",
  iconSize: [28, 28],
});
const liveIcon = L.divIcon({
  html: "🟢",
  className: "emoji-icon",
  iconSize: [22, 22],
});

let pickerMarker = null; // marker used while choosing a new destination
let chosenDestination = null; // {lat, lng, address}

let focusDestMarker = null; // destination marker for the currently-viewed session
let focusLiveMarker = null; // recipient marker for the currently-viewed session
let focusRouteLine = null;

// ---- Destination picking ----
map.on("click", async (e) => {
  const { lat, lng } = e.latlng;
  setPickerMarker(lat, lng);
  destLabel.textContent = "Looking up address…";
  destChosen.hidden = false;
  const address = (await reverseGeocode(lat, lng)) || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
  chosenDestination = { lat, lng, address };
  destLabel.textContent = address;
  generateBtn.disabled = false;
});

function setPickerMarker(lat, lng) {
  if (pickerMarker) map.removeLayer(pickerMarker);
  pickerMarker = L.marker([lat, lng], { icon: destIcon }).addTo(map);
}

let searchTimer = null;
destSearch.addEventListener("input", () => {
  clearTimeout(searchTimer);
  const q = destSearch.value;
  if (q.trim().length < 3) {
    destSuggestions.hidden = true;
    destSuggestions.innerHTML = "";
    return;
  }
  searchTimer = setTimeout(async () => {
    try {
      const results = await geocode(q);
      renderSuggestions(results);
    } catch {
      destSuggestions.hidden = true;
    }
  }, 400);
});

function renderSuggestions(results) {
  destSuggestions.innerHTML = "";
  if (!results.length) {
    destSuggestions.hidden = true;
    return;
  }
  for (const r of results) {
    const li = document.createElement("li");
    li.textContent = r.label;
    li.addEventListener("click", () => {
      chosenDestination = { lat: r.lat, lng: r.lng, address: r.label };
      setPickerMarker(r.lat, r.lng);
      map.setView([r.lat, r.lng], 15);
      destLabel.textContent = r.label;
      destChosen.hidden = false;
      destSuggestions.hidden = true;
      destSearch.value = "";
      generateBtn.disabled = false;
    });
    destSuggestions.appendChild(li);
  }
  destSuggestions.hidden = false;
}

// ---- Generate link ----
generateBtn.addEventListener("click", async () => {
  if (!chosenDestination) return;
  generateBtn.disabled = true;
  generateBtn.textContent = "Generating…";
  try {
    const id = generateSessionId();
    const expiresAt = Timestamp.fromDate(new Date(Date.now() + 24 * 3600 * 1000));
    const label = tripLabelInput.value.trim() || null;

    await setDoc(doc(db, "tracking_sessions", id), {
      destination: chosenDestination,
      label,
      createdAt: serverTimestamp(),
      expiresAt,
      status: "pending",
      consentAt: null,
      location: null,
    });

    const url = buildTrackingUrl(id);
    shareLink.textContent = url;
    shareCard.hidden = false;

    const message = `Hi! ${label ? `For "${label}", ` : ""}please share your live location so I can track your trip to ${chosenDestination.address} and see your estimated arrival time. Your location is only shared after you agree: ${url}`;
    whatsappBtn.onclick = () => window.open(buildWhatsAppUrl(message, phoneInput.value), "_blank");

    saveSessionLocally({ id, label: label || chosenDestination.address, createdAt: Date.now() });
    subscribeSession(id);
    renderSessionList();
    selectSession(id);
  } catch (err) {
    alert("Could not generate the tracking link. Check your Firebase setup (web/js/firebase-config.js) and try again.");
    console.error(err);
  } finally {
    generateBtn.disabled = false;
    generateBtn.textContent = "Generate tracking link";
  }
});

copyBtn.addEventListener("click", async () => {
  await navigator.clipboard.writeText(shareLink.textContent);
  copyBtn.textContent = "Copied!";
  setTimeout(() => (copyBtn.textContent = "Copy"), 1500);
});

// ---- Local session list (this browser only — there are no user accounts) ----
function loadLocalSessions() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
  } catch {
    return [];
  }
}
function saveSessionLocally(entry) {
  const list = loadLocalSessions();
  list.unshift(entry);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
}
function removeSessionLocally(id) {
  const list = loadLocalSessions().filter((s) => s.id !== id);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
}

const sessionData = new Map(); // id -> latest Firestore data (or null if deleted/missing)
const unsubscribers = new Map(); // id -> unsubscribe fn
const routeCache = new Map(); // id -> { lat, lng, time, result }
let selectedSessionId = null;

function subscribeSession(id) {
  if (unsubscribers.has(id)) return;
  const unsub = onSnapshot(
    doc(db, "tracking_sessions", id),
    (snap) => {
      sessionData.set(id, snap.exists() ? snap.data() : null);
      renderSessionList();
      if (selectedSessionId === id) renderFocusedSession(id);
    },
    (err) => console.error("session listener error", err)
  );
  unsubscribers.set(id, unsub);
}

function renderSessionList() {
  const list = loadLocalSessions();
  sessionListEl.innerHTML = "";
  sessionEmptyEl.hidden = list.length > 0;

  for (const entry of list) {
    subscribeSession(entry.id);
    const data = sessionData.get(entry.id);
    const li = document.createElement("li");
    li.className = "session-item" + (entry.id === selectedSessionId ? " active" : "");

    const status = data === null ? "expired" : data.status;
    const badge = `<span class="badge badge-${status}">${status}</span>`;

    li.innerHTML = `
      <div class="row1">
        <span class="title">${escapeHtml(entry.label || "Tracking link")}</span>
        ${badge}
      </div>
      <div class="meta">${new Date(entry.createdAt).toLocaleString()}</div>
    `;
    li.addEventListener("click", () => selectSession(entry.id));
    sessionListEl.appendChild(li);
  }
}

function escapeHtml(s) {
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}

function selectSession(id) {
  selectedSessionId = id;
  renderSessionList();
  renderFocusedSession(id);
}

async function renderFocusedSession(id) {
  const data = sessionData.get(id);
  clearFocusLayers();

  if (data === null) {
    setBanner("This tracking link has expired or was removed.", "warn");
    statRow.hidden = true;
    sessionActions.hidden = true;
    return;
  }

  sessionActions.hidden = false;
  closeSessionBtn.hidden = !["pending", "active"].includes(data.status);
  statRow.hidden = false;

  const dest = data.destination;
  focusDestMarker = L.marker([dest.lat, dest.lng], { icon: destIcon }).addTo(map).bindPopup(dest.address);

  const bannerText = {
    pending: "Waiting for the recipient to open the link and agree to share their location.",
    active: "Recipient is sharing their live location.",
    declined: "Recipient declined to share their location.",
    closed: "This trip was closed.",
    arrived: "Recipient has arrived at the destination.",
  }[data.status] || "";
  const bannerClass = { pending: "warn", active: "ok", arrived: "ok", declined: "error", closed: "" }[data.status] || "";
  setBanner(bannerText, bannerClass);

  statStatus.textContent = data.status;
  statUpdated.textContent = data.location?.updatedAt ? timeAgo(data.location.updatedAt.toDate()) : "—";

  if (data.location) {
    const { lat, lng } = data.location;
    focusLiveMarker = L.marker([lat, lng], { icon: liveIcon }).addTo(map).bindPopup("Recipient");
    map.fitBounds(L.latLngBounds([[lat, lng], [dest.lat, dest.lng]]), { padding: [40, 40] });

    const cached = routeCache.get(id);
    const moved = !cached || haversineMeters(cached.lat, cached.lng, lat, lng) > 40;
    const stale = !cached || Date.now() - cached.time > 25000;
    if (moved || stale) {
      routeCache.set(id, { lat, lng, time: Date.now(), result: null });
      const route = await getRoute(lat, lng, dest.lat, dest.lng);
      if (selectedSessionId !== id) return; // user navigated away while awaiting
      routeCache.set(id, { lat, lng, time: Date.now(), result: route });
      applyRoute(route);
    } else {
      applyRoute(cached.result);
    }
  } else {
    map.setView([dest.lat, dest.lng], 13);
    statDistance.textContent = "—";
    statEta.textContent = "—";
  }
}

function applyRoute(route) {
  if (!route) return;
  if (focusRouteLine) map.removeLayer(focusRouteLine);
  focusRouteLine = L.polyline(route.coords, { color: "#4f8cff", weight: 4 }).addTo(map);
  statDistance.textContent = formatDistance(route.distanceMeters);
  statEta.textContent = `${formatDuration(route.durationSeconds)} (≈ ${formatEta(route.durationSeconds)})`;
}

function clearFocusLayers() {
  if (focusDestMarker) map.removeLayer(focusDestMarker);
  if (focusLiveMarker) map.removeLayer(focusLiveMarker);
  if (focusRouteLine) map.removeLayer(focusRouteLine);
  focusDestMarker = focusLiveMarker = focusRouteLine = null;
}

function setBanner(text, cls) {
  if (!text) {
    statusBanner.hidden = true;
    return;
  }
  statusBanner.hidden = false;
  statusBanner.textContent = text;
  statusBanner.className = "status-banner" + (cls ? ` ${cls}` : "");
}

closeSessionBtn.addEventListener("click", async () => {
  if (!selectedSessionId) return;
  try {
    await updateDoc(doc(db, "tracking_sessions", selectedSessionId), { status: "closed" });
  } catch (err) {
    console.error(err);
  }
});

removeSessionBtn.addEventListener("click", () => {
  if (!selectedSessionId) return;
  const id = selectedSessionId;
  const unsub = unsubscribers.get(id);
  if (unsub) unsub();
  unsubscribers.delete(id);
  sessionData.delete(id);
  routeCache.delete(id);
  removeSessionLocally(id);
  if (selectedSessionId === id) {
    selectedSessionId = null;
    clearFocusLayers();
    statRow.hidden = true;
    sessionActions.hidden = true;
    setBanner("", "");
  }
  renderSessionList();
});

// ---- Init ----
renderSessionList();
