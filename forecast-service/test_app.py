"""Tests for the forecast API's request handling, independent of TimesFM itself.

The real model (google/timesfm-2.5-200m-pytorch) is a ~800MB checkpoint that
isn't available in CI/offline environments, so these tests monkeypatch the
forecaster's model-loading step and assert on the API contract: validation,
response shape, and error surfacing when the model can't load.
"""

from __future__ import annotations

import forecaster as forecaster_module
from fastapi.testclient import TestClient

import app as app_module


class _FakeModel:
    def forecast(self, horizon: int, inputs: list):
        import numpy as np

        batch = len(inputs)
        point = np.tile(np.arange(1, horizon + 1, dtype=np.float32), (batch, 1))
        quantiles = np.tile(point[:, :, None], (1, 1, 10))
        return point, quantiles


def _install_fake_model(monkeypatch):
    monkeypatch.setattr(app_module.forecaster, "_model", _FakeModel())


def test_healthz_reports_model_not_loaded_initially():
    client = TestClient(app_module.app)
    app_module.forecaster._model = None
    resp = client.get("/healthz")
    assert resp.status_code == 200
    assert resp.json() == {"status": "ok", "model_loaded": False}


def test_forecast_returns_point_and_quantiles(monkeypatch):
    _install_fake_model(monkeypatch)
    client = TestClient(app_module.app)

    resp = client.post("/v1/forecast", json={"series": [1, 2, 3, 4, 5, 6, 7], "horizon": 3})

    assert resp.status_code == 200
    body = resp.json()
    assert body["model"] == forecaster_module.CHECKPOINT
    assert body["horizon"] == 3
    assert body["point_forecast"] == [1.0, 2.0, 3.0]
    assert set(body["quantiles"].keys()) == {"q10", "q20", "q30", "q40", "q50", "q60", "q70", "q80", "q90"}
    assert body["quantiles"]["q50"] == [1.0, 2.0, 3.0]


def test_forecast_rejects_too_short_series(monkeypatch):
    _install_fake_model(monkeypatch)
    client = TestClient(app_module.app)

    resp = client.post("/v1/forecast", json={"series": [1, 2], "horizon": 3})

    assert resp.status_code == 422


def test_forecast_rejects_horizon_above_max(monkeypatch):
    _install_fake_model(monkeypatch)
    client = TestClient(app_module.app)

    resp = client.post(
        "/v1/forecast",
        json={"series": [1, 2, 3, 4, 5], "horizon": forecaster_module.MAX_HORIZON + 1},
    )

    assert resp.status_code == 422


def test_forecast_surfaces_model_load_failure_as_503(monkeypatch):
    def _boom(self):
        raise RuntimeError("checkpoint not downloaded")

    monkeypatch.setattr(app_module.forecaster, "_model", None)
    monkeypatch.setattr(forecaster_module.TimesFmForecaster, "_load_model", _boom)

    client = TestClient(app_module.app)
    resp = client.post("/v1/forecast", json={"series": [1, 2, 3, 4, 5], "horizon": 3})

    assert resp.status_code == 503
    assert "unavailable" in resp.json()["detail"]
