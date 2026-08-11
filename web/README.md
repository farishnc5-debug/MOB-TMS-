# Live Tracking (web/)

A small standalone web app, separate from the Yalla Muv Android app in this repo, that lets you:

1. Pick a destination on a map.
2. Generate a one-time tracking link.
3. Send that link to anyone on WhatsApp.
4. Once they open it and tap **Share my location**, watch their live position on your
   dashboard map, with distance remaining and an ETA that updates as they move.

Two pages:

- `index.html` — your dashboard: set the destination, generate/send the link, watch the map.
- `track.html` — what the recipient opens: a consent screen, then (only if they agree) their
  live location is shared until they stop, arrive, or the link expires (24h).

No accounts, no app install for the recipient, no paid API keys. It uses:

- **Firebase Firestore** (free "Spark" tier is enough) to relay location updates in real time.
- **Firebase Hosting** to serve the two pages over HTTPS (required — browsers only allow the
  location permission prompt on secure origins).
- **Leaflet + OpenStreetMap** for the map, and the **public OSRM** routing server for
  directions/ETA. Both are free and keyless, but rate-limited — fine for personal/small-team
  use; if you outgrow them, swap in a paid routing provider in `web/js/utils.js#getRoute`.

## One-time setup

1. **Create a Firebase project**: https://console.firebase.google.com → Add project (free).
2. **Enable Firestore**: in the console, Build → Firestore Database → Create database →
   start in production mode, pick a region close to your users.
3. **Register a Web app**: Project settings (gear icon) → General → "Your apps" → Web (`</>`)
   icon. Copy the `firebaseConfig` object it gives you.
4. Paste those values into `web/js/firebase-config.js` in this repo (replace the
   `YOUR_...` placeholders). This file is not secret — Firebase web config is public by
   design; access is controlled by `firestore.rules` instead.
5. **Install the Firebase CLI** (once): `npm install -g firebase-tools`, then `firebase login`.
6. From the repo root, run `firebase use --add` and pick the project you just created.

## Deploy

From the repo root:

```
firebase deploy --only firestore:rules,hosting
```

Firebase prints a Hosting URL like `https://your-project.web.app`. That's your dashboard —
open `https://your-project.web.app/index.html` (or just the base URL) to start generating
tracking links. Links you generate automatically point recipients at
`https://your-project.web.app/track.html?id=...` on the same domain.

Re-run the same command any time you change files under `web/` or `firestore.rules`.

## How the security model works (read this before sharing widely)

There are no logins. Each tracking link's id is a long random token
(`web/js/utils.js#generateSessionId`) — knowing the id is what authorizes reading/updating
that one trip, the same trust model as "anyone with this link" location-sharing features in
other consumer apps. `firestore.rules` enforces, for every session, regardless of who holds
the link:

- No one can list/enumerate sessions (only direct-by-id reads work), so links can't be
  guessed by scanning the database.
- No one can delete a session or change its destination/label after creation.
- Once a trip is `declined`, `closed`, or `arrived`, it can never be reopened or written to
  again by anyone.
- Writes stop being accepted once `expiresAt` (24h after creation) passes.

If you need stronger guarantees (e.g. only *you* can close a trip, not the recipient), add
Firebase Authentication for the sender side — that's a natural next step but out of scope
for this first version.

## Notes and limits

- **The dashboard's session list lives in that browser's `localStorage`.** It's how the
  dashboard remembers which links you've generated, since there's no login. Generating links
  from a different browser/device gives you a separate list. If you need one shared dashboard
  across devices, add Firebase Auth and a per-user sessions collection.
- **Location only updates while the recipient's tab is open and in the foreground** — this is
  a browser limitation (Background geolocation on the web is intentionally restricted), not
  something this app can work around. For always-on background tracking you'd need a native
  app with location permissions, not a web page.
- **OpenStreetMap Nominatim / public OSRM** are shared, rate-limited public services intended
  for light use. If you see search or ETA calls failing under heavier traffic, switch to a
  paid geocoding/routing provider (e.g. Mapbox, Google Maps Platform, or a self-hosted OSRM).
