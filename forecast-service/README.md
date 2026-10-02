# MOB-TMS Demand Forecast Service

A small FastAPI service that wraps Google Research's
[TimesFM](https://github.com/google-research/timesfm) time-series foundation
model and exposes it as an HTTP endpoint the Android app (`app/`) calls to
forecast future shipment/operation volume from historical daily counts.

It uses the **TimesFM 2.5 (200M, PyTorch)** checkpoint
(`google/timesfm-2.5-200m-pytorch`), not 3.0, because its pretrained weights
are Apache-2.0. TimesFM 3.0's weights carry a non-commercial license, which
doesn't fit a product used commercially - see the
[license notice](https://github.com/google-research/timesfm#license-notice-for-pretrained-weights)
in the upstream repo.

## Why a separate service?

TimesFM is a PyTorch model - it can't run on-device in the Android app. The
app already stores its data locally in Room (SQLite) with no backend, so this
adds the one small Python service needed to host the model; the app calls it
over HTTP the same way it already calls the Gemini API.

## Setup

```bash
cd forecast-service
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

The first request after startup downloads the ~800MB checkpoint from Hugging
Face and loads it into memory - this takes a while and needs network access.
Subsequent requests reuse the loaded model.

## Run locally

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

- `GET /healthz` -> `{"status": "ok", "model_loaded": bool}`. Use this to
  check whether the checkpoint has finished loading without paying forecast
  latency.
- `POST /v1/forecast` -> runs a forecast.

### Request

```json
{
  "series": [12, 9, 15, 20, 11, 14, 18],
  "horizon": 14
}
```

- `series`: historical daily values, chronological, **oldest first**, at
  least 4 points. Longer than 512 points is truncated to the most recent 512
  (the model's configured max context).
- `horizon`: how many future days to forecast, 1-64.

### Response

```json
{
  "model": "google/timesfm-2.5-200m-pytorch",
  "horizon": 14,
  "point_forecast": [16.2, 15.8, ...],
  "quantiles": {
    "q10": [...], "q20": [...], "q30": [...], "q40": [...], "q50": [...],
    "q60": [...], "q70": [...], "q80": [...], "q90": [...]
  }
}
```

`point_forecast` is the model's point estimate per day; `quantiles` give a
10%-90% uncertainty band (`q50` is the median) for building a confidence
range in the UI.

If the model fails to load (e.g. no network to Hugging Face, or the download
hasn't completed yet), `/v1/forecast` returns `503` with a descriptive
`detail` message instead of a forecast.

## Tests

```bash
pip install -r requirements-dev.txt
pytest
```

The tests monkeypatch the model-loading step so they run without downloading
the checkpoint - they check the API's validation, response shape, and error
handling, not the model's forecast quality.

## Deploying

The `Dockerfile` builds a container that reads `PORT` (defaults to 8080),
matching Cloud Run's convention:

```bash
docker build -t mob-tms-forecast-service .
docker run -p 8080:8080 mob-tms-forecast-service
```

Point the Android app at the deployed URL by setting `FORECAST_SERVICE_URL`
in `app/.env` (see `app/.env.example`).

## Known limitations

- Single-process, in-memory model - there's no request queueing or batching,
  so concurrent forecast requests under load will serialize on the model.
- No authentication. Put this behind your own auth/network boundary (e.g. a
  Cloud Run service with IAM, or a reverse proxy) before exposing it beyond a
  trusted network - it's a demand-forecasting aid, not a public API.
