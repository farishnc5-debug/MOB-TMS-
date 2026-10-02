"""FastAPI service that exposes TimesFM demand forecasts to MOB-TMS- clients.

Run locally:
    uvicorn app:app --host 0.0.0.0 --port 8000

See README.md for setup, the request/response contract, and deployment notes.
"""

from __future__ import annotations

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from forecaster import CHECKPOINT, MAX_CONTEXT, MAX_HORIZON, forecaster

app = FastAPI(title="MOB-TMS Demand Forecast Service", version="1.0.0")


class ForecastRequest(BaseModel):
    series: list[float] = Field(
        ...,
        min_length=4,
        description="Historical daily values (e.g. shipment counts), chronological, oldest first.",
    )
    horizon: int = Field(..., ge=1, le=MAX_HORIZON, description="Number of future days to forecast.")


class ForecastResponse(BaseModel):
    model: str
    horizon: int
    point_forecast: list[float]
    quantiles: dict[str, list[float]]


@app.get("/healthz")
def healthz() -> dict:
    return {"status": "ok", "model_loaded": forecaster.is_loaded}


@app.post("/v1/forecast", response_model=ForecastResponse)
def forecast(request: ForecastRequest) -> ForecastResponse:
    series = request.series[-MAX_CONTEXT:]
    try:
        result = forecaster.forecast(series, request.horizon)
    except Exception as exc:
        raise HTTPException(
            status_code=503,
            detail=f"Forecasting model unavailable: {exc}",
        ) from exc

    return ForecastResponse(
        model=CHECKPOINT,
        horizon=request.horizon,
        point_forecast=result.point,
        quantiles=result.quantiles,
    )
