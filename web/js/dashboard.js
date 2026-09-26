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
  reverseGeocode,
  timeAgo,
  buildTrackingUrl,
  buildGoogleMapsUrl,
  buildWhatsAppUrl,
} from "./utils.js";

const STORAGE_KEY = "ynm_tracking_sessions";
const DEFAULT_CENTER = [21.4858, 39.1925]; // Jeddah, KSA

// ---- DOM ----
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
const statUpdated = document.getElementById("stat-updated");
const statAddress = document.getElementById("stat-address");
const sessionActions = document.getElementById("session-actions");
const gmapsLink = document.getElementById("gmaps-link");
const closeSessionBtn = document.getElementById("close-session-btn");
const removeSessionBtn = document.getElementById("remove-session-btn");

// ---- Map ----
const map = L.map("map").setView(DEFAULT_CENTER, 12);
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
  attribution: "&copy; OpenStreetMap contributors",
  maxZoom: 19,
}).addTo(map);

const liveIcon = L.divIcon({
  html: "🟢",
  className: "emoji-icon",
  iconSize: [22, 22],
});

let focusLiveMarker = null;

// ---- Generate link ----
tripLabelInput.addEventListener("input", () => {
  generateBtn.disabled = tripLabelInput.value.trim().length === 0;
});

generateBtn.addEventListener("click", async () => {
  const label = tripLabelInput.value.trim();
  if (!label) return;
  generateBtn.disabled = true;
  generateBtn.textContent = "Generating…";
  try {
    const id = generateSessionId();
    const expiresAt = Timestamp.fromDate(new Date(Date.now() + 24 * 3600 * 1000));

    await setDoc(doc(db, "tracking_sessions", id), {
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

    const message = `Hi! For "${label}", please share your live location with me. Your location is only shared after you agree: ${url}`;
    whatsappBtn.onclick = () => window.open(buildWhatsAppUrl(message, phoneInput.value), "_blank");

    saveSessionLocally({ id, label, createdAt: Date.now() });
    subscribeSession(id);
    renderSessionList();
    selectSession(id);
  } catch (err) {
    alert("Could not generate the tracking link. Check your Firebase setup (web/js/firebase-config.js) and try again.");
    console.error(err);
  } finally {
    generateBtn.disabled = tripLabelInput.value.trim().length === 0;
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
const addressCache = new Map(); // id -> { lat, lng, time, address }
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

    // `data` is `undefined` until this session's Firestore listener delivers its first
    // snapshot (always at least one microtask away, even from cache) - show a neutral
    // placeholder for that brief gap rather than assuming it's already loaded or missing.
    const badge =
      data === undefined
        ? `<span class="badge">…</span>`
        : (() => {
            const status = data === null ? "expired" : data.status;
            return `<span class="badge badge-${status}">${status}</span>`;
          })();

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

  if (data === undefined) {
    setBanner("Loading…", "");
    statRow.hidden = true;
    sessionActions.hidden = true;
    return;
  }

  if (data === null) {
    setBanner("This tracking link has expired or was removed.", "warn");
    statRow.hidden = true;
    sessionActions.hidden = true;
    return;
  }

  sessionActions.hidden = false;
  closeSessionBtn.hidden = !["pending", "active"].includes(data.status);
  statRow.hidden = false;

  const bannerText = {
    pending: "Waiting for them to open the link and agree to share their location.",
    active: "They're sharing their live location.",
    declined: "They declined to share their location.",
    closed: "This link was closed.",
  }[data.status] || "";
  const bannerClass = { pending: "warn", active: "ok", declined: "error", closed: "" }[data.status] || "";
  setBanner(bannerText, bannerClass);

  statStatus.textContent = data.status;
  statUpdated.textContent = data.location?.updatedAt ? timeAgo(data.location.updatedAt.toDate()) : "—";

  if (data.location) {
    const { lat, lng } = data.location;
    focusLiveMarker = L.marker([lat, lng], { icon: liveIcon }).addTo(map).bindPopup(escapeHtml(data.label || "Live location"));
    map.setView([lat, lng], 15);
    gmapsLink.hidden = false;
    gmapsLink.href = buildGoogleMapsUrl(lat, lng);

    const cached = addressCache.get(id);
    const moved = !cached || Math.hypot(cached.lat - lat, cached.lng - lng) > 0.0005;
    const stale = !cached || Date.now() - cached.time > 25000;
    if (moved || stale) {
      addressCache.set(id, { lat, lng, time: Date.now(), address: cached?.address || "…" });
      statAddress.textContent = cached?.address || "…";
      const address = await reverseGeocode(lat, lng);
      if (selectedSessionId !== id) return; // user navigated away while awaiting
      addressCache.set(id, { lat, lng, time: Date.now(), address: address || "—" });
      statAddress.textContent = address || "—";
    } else {
      statAddress.textContent = cached.address;
    }
  } else {
    map.setView(DEFAULT_CENTER, 12);
    statAddress.textContent = "—";
    gmapsLink.hidden = true;
  }
}

function clearFocusLayers() {
  if (focusLiveMarker) map.removeLayer(focusLiveMarker);
  focusLiveMarker = null;
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
  addressCache.delete(id);
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
generateBtn.disabled = tripLabelInput.value.trim().length === 0;
renderSessionList();
