// Shared helpers for the tracking dashboard and the recipient page.
// No API keys required: reverse geocoding (showing a readable address for a pin) uses the
// free OpenStreetMap Nominatim service. It's rate-limited — fine for personal/small-team use;
// swap for a paid provider if you outgrow the public service.

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

// Reverse geocoding (lat/lng -> address text) via Nominatim - a nicety for showing a
// readable address under a live pin. Purely cosmetic: failures just fall back to raw
// coordinates, nothing depends on this succeeding.
export async function reverseGeocode(lat, lng) {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;
    const res = await fetch(url, { headers: { Accept: "application/json" } });
    if (!res.ok) return null;
    const data = await res.json();
    return data.display_name || null;
  } catch {
    return null;
  }
}

export function buildTrackingUrl(sessionId) {
  const base = window.location.origin + window.location.pathname.replace(/index\.html$/, "");
  return `${base}track.html?id=${sessionId}`;
}

export function buildGoogleMapsUrl(lat, lng) {
  return `https://www.google.com/maps?q=${lat},${lng}`;
}

export function buildWhatsAppUrl(message, phone) {
  const text = encodeURIComponent(message);
  const digits = (phone || "").replace(/[^\d]/g, "");
  return digits ? `https://wa.me/${digits}?text=${text}` : `https://wa.me/?text=${text}`;
}
