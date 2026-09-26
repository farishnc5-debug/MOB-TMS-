import { db } from "./firebase-init.js";
import {
  doc,
  getDoc,
  updateDoc,
  serverTimestamp,
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js";

const MIN_UPDATE_INTERVAL_MS = 8000;
const MIN_UPDATE_DISTANCE_METERS = 15;

function haversineMeters(lat1, lon1, lat2, lon2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

const states = ["loading", "error", "consent", "active", "declined", "closed"];
function showState(name) {
  for (const s of states) {
    document.getElementById(`${s}-state`).hidden = s !== name;
  }
}

const params = new URLSearchParams(window.location.search);
const sessionId = params.get("id");
const sessionRef = sessionId ? doc(db, "tracking_sessions", sessionId) : null;

let watchId = null;
let lastSent = { lat: null, lng: null, time: 0 };
let map, liveMarker;

async function init() {
  if (!sessionId) {
    showState("error");
    document.getElementById("error-title").textContent = "No tracking link provided";
    document.getElementById("error-message").textContent = "This page needs a valid tracking link from the sender.";
    return;
  }

  let snap;
  try {
    snap = await getDoc(sessionRef);
  } catch (err) {
    console.error(err);
    showState("error");
    document.getElementById("error-title").textContent = "Couldn't load this link";
    document.getElementById("error-message").textContent =
      "Check your connection, or the sender's Firebase project may not be set up yet.";
    return;
  }

  if (!snap.exists()) {
    showState("error");
    document.getElementById("error-title").textContent = "Link invalid or expired";
    document.getElementById("error-message").textContent = "This tracking link no longer exists.";
    return;
  }

  const data = snap.data();

  if (data.expiresAt && data.expiresAt.toDate() < new Date()) {
    showState("error");
    document.getElementById("error-title").textContent = "Link expired";
    document.getElementById("error-message").textContent = "This tracking link has expired.";
    return;
  }

  document.getElementById("topbar-sub").textContent = data.label || "Live tracking";

  if (data.status === "declined") return showState("declined");
  if (data.status === "closed") return showState("closed");

  if (data.status === "active") {
    startSharing(true);
    return;
  }

  // status === "pending"
  document.getElementById("consent-label").textContent = data.label
    ? `They named this link "${data.label}".`
    : "";
  showState("consent");

  document.getElementById("share-btn").addEventListener("click", () => startSharing(false));
  document.getElementById("decline-btn").addEventListener("click", decline);
}

async function decline() {
  try {
    await updateDoc(sessionRef, { status: "declined" });
  } catch (err) {
    console.error(err);
  }
  showState("declined");
}

function initMap(lat, lng) {
  map = L.map("map").setView([lat, lng], 15);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors",
    maxZoom: 19,
  }).addTo(map);
}

function startSharing(alreadyActive) {
  showState("active");

  if (!navigator.geolocation) {
    document.getElementById("active-banner").textContent =
      "This browser doesn't support location sharing.";
    document.getElementById("active-banner").className = "status-banner error";
    return;
  }

  const onFirstFix = async (pos) => {
    if (!alreadyActive) {
      try {
        await updateDoc(sessionRef, {
          status: "active",
          consentAt: serverTimestamp(),
          location: toLocationPayload(pos),
        });
      } catch (err) {
        console.error(err);
      }
    }
    handlePosition(pos);
    watchId = navigator.geolocation.watchPosition(handlePosition, onWatchError, {
      enableHighAccuracy: true,
      maximumAge: 5000,
      timeout: 20000,
    });
  };

  navigator.geolocation.getCurrentPosition(onFirstFix, onPermissionError, {
    enableHighAccuracy: true,
    timeout: 20000,
  });

  document.getElementById("stop-btn").addEventListener("click", stopSharing);
}

function toLocationPayload(pos) {
  return {
    lat: pos.coords.latitude,
    lng: pos.coords.longitude,
    accuracy: pos.coords.accuracy ?? null,
    heading: pos.coords.heading ?? null,
    speed: pos.coords.speed ?? null,
    updatedAt: serverTimestamp(),
  };
}

async function handlePosition(pos) {
  const { latitude: lat, longitude: lng } = pos.coords;

  if (!map) initMap(lat, lng);
  if (!liveMarker) {
    const icon = L.divIcon({ html: "🟢", className: "emoji-icon", iconSize: [22, 22] });
    liveMarker = L.marker([lat, lng], { icon }).addTo(map);
  } else {
    liveMarker.setLatLng([lat, lng]);
  }
  map.panTo([lat, lng]);

  const now = Date.now();
  const movedEnough =
    lastSent.lat == null || haversineMeters(lastSent.lat, lastSent.lng, lat, lng) > MIN_UPDATE_DISTANCE_METERS;
  const enoughTimePassed = now - lastSent.time > MIN_UPDATE_INTERVAL_MS;

  if (movedEnough && enoughTimePassed) {
    lastSent = { lat, lng, time: now };
    try {
      await updateDoc(sessionRef, { location: toLocationPayload(pos) });
    } catch (err) {
      console.error(err);
    }
  }
}

async function stopSharing() {
  if (watchId != null) navigator.geolocation.clearWatch(watchId);
  watchId = null;
  try {
    await updateDoc(sessionRef, { status: "closed" });
  } catch (err) {
    console.error(err);
  }
  showState("closed");
}

async function onPermissionError(err) {
  console.error(err);
  await decline();
}

function onWatchError(err) {
  console.error("watchPosition error", err);
}

init();
