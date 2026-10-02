"""Loads Google Research's TimesFM 2.5 (200M) model and serves forecasts.

Uses the TimesFM 2.5 checkpoint rather than 3.0 because its pretrained
weights are Apache-2.0 (3.0's weights carry a non-commercial license) -
see https://github.com/google-research/timesfm#license-notice-for-pretrained-weights.
The model is loaded lazily and once per process so the first request after
a cold start pays the (one-time) checkpoint download/load cost.
"""

from __future__ import annotations

import logging
import threading
from dataclasses import dataclass

import numpy as np

logger = logging.getLogger(__name__)

CHECKPOINT = "google/timesfm-2.5-200m-pytorch"
MAX_CONTEXT = 512
MAX_HORIZON = 64

_QUANTILE_COLUMNS = ["mean", "q10", "q20", "q30", "q40", "q50", "q60", "q70", "q80", "q90"]


@dataclass
class ForecastResult:
    point: list[float]
    quantiles: dict[str, list[float]]


class TimesFmForecaster:
    """Thread-safe, lazily-initialized wrapper around the TimesFM 2.5 model."""

    def __init__(self) -> None:
        self._model = None
        self._lock = threading.Lock()

    @property
    def is_loaded(self) -> bool:
        return self._model is not None

    def _load_model(self):
        import timesfm

        logger.info("Loading TimesFM checkpoint %s ...", CHECKPOINT)
        model = timesfm.TimesFM_2p5_200M_torch.from_pretrained(CHECKPOINT, torch_compile=False)
        model.compile(
            timesfm.ForecastConfig(
                max_context=MAX_CONTEXT,
                max_horizon=MAX_HORIZON,
                normalize_inputs=True,
                use_continuous_quantile_head=True,
                fix_quantile_crossing=True,
            )
        )
        return model

    def _ensure_loaded(self):
        if self._model is None:
            with self._lock:
                if self._model is None:
                    self._model = self._load_model()
        return self._model

    def forecast(self, series: list[float], horizon: int) -> ForecastResult:
        model = self._ensure_loaded()
        context = np.asarray(series[-MAX_CONTEXT:], dtype=np.float32)
        point_forecast, quantile_forecast = model.forecast(horizon=horizon, inputs=[context])

        point = point_forecast[0].tolist()
        quantiles_for_series = quantile_forecast[0]  # shape: (horizon, 10)
        quantiles = {
            label: quantiles_for_series[:, i].tolist()
            for i, label in enumerate(_QUANTILE_COLUMNS)
            if label != "mean"
        }
        return ForecastResult(point=point, quantiles=quantiles)


# One forecaster per process - the model is too heavy to reload per request.
forecaster = TimesFmForecaster()
