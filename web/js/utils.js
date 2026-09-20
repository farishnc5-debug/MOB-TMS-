// Shared helpers for the tracking dashboard and the recipient page.
// No API keys required: geocoding uses OpenStreetMap Nominatim, routing/ETA uses the
// public OSRM demo server. Both are free but rate-limited — fine for personal/small-team
// use; swap for a paid provider if you outgrow the public demo servers.

const ID_CHARS = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

// Unguessable session id. This id doubles as the "credential" for the tracking link
// (see /firestore.rules) so it must be long and generated with a CSPRNG, never sequential.
export function generateSessionId(length = 22) {
  const bytes = new Uint32Array(length);
  crypto.getRandomValues(bytes);
  let out = "";
  for (let i = 0; i < length; i++) out += ID_CHARS[bytes[i] % ID_CHARS.length];
  return out;
}

export function formatDistance(meters) {
  if (meters == null || Number.isNaN(meters)) return "—";
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

export function formatDuration(seconds) {
  if (seconds == null || Number.isNaN(seconds)) return "—";
  const mins = Math.round(seconds / 60);
  if (mins < 1) return "under 1 min";
  if (mins < 60) return `${mins} min`;
  const hrs = Math.floor(mins / 60);
  const rem = mins % 60;
  return rem === 0 ? `${hrs} hr` : `${hrs} hr ${rem} min`;
}

export function formatEta(seconds) {
  if (seconds == null || Number.isNaN(seconds)) return "—";
  const arrival = new Date(Date.now() + seconds * 1000);
  return arrival.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

export function timeAgo(date) {
  if (!date) return "never";
  const secs = Math.round((Date.now() - date.getTime()) / 1000);
  if (secs < 10) return "just now";
  if (secs < 60) return `${secs}s ago`;
  const mins = Math.round(secs / 60);
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.round(mins / 60);
  return `${hrs}h ago`;
}

// Straight-line distance in meters, used to decide whether movement is significant
// enough to spend a route-recalculation call on.
export function haversineMeters(lat1, lon1, lat2, lon2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

// Forward geocoding (address text -> candidate locations) via Nominatim.
export async function geocode(query) {
  if (!query || query.trim().length < 3) return [];
  const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q=${encodeURIComponent(query)}`;
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error("Geocoding failed");
  const data = await res.json();
  return data.map((d) => ({
    label: d.display_name,
    lat: parseFloat(d.lat),
    lng: parseFloat(d.lon),
  }));
}

// Reverse geocoding (lat/lng -> address text) via Nominatim.
export async function reverseGeocode(lat, lng) {
  const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) return null;
  const data = await res.json();
  return data.display_name || null;
}

// Driving route + ETA between two points via the public OSRM demo server.
// Returns { distanceMeters, durationSeconds, coords: [[lat,lng], ...] } or null if unreachable.
export async function getRoute(fromLat, fromLng, toLat, toLng) {
  const url = `https://router.project-osrm.org/route/v1/driving/${fromLng},${fromLat};${toLng},${toLat}?overview=full&geometries=geojson`;
  const res = await fetch(url);
  if (!res.ok) return null;
  const data = await res.json();
  const route = data.routes && data.routes[0];
  if (!route) return null;
  return {
    distanceMeters: route.distance,
    durationSeconds: route.duration,
    coords: route.geometry.coordinates.map(([lng, lat]) => [lat, lng]),
  };
}

export function buildTrackingUrl(sessionId) {
  const base = window.location.origin + window.location.pathname.replace(/index\.html$/, "");
  return `${base}track.html?id=${sessionId}`;
}

export function buildWhatsAppUrl(message, phone) {
  const text = encodeURIComponent(message);
  const digits = (phone || "").replace(/[^\d]/g, "");
  return digits ? `https://wa.me/${digits}?text=${text}` : `https://wa.me/?text=${text}`;
}
