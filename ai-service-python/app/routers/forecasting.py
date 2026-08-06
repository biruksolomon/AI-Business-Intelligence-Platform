from fastapi import APIRouter

from app.schemas.forecasting import (
    DemandPredictionRequest,
    DemandPredictionResponse,
    ProfitPredictionRequest,
    ProfitPredictionResponse,
    SalesForecastRequest,
    SalesForecastResponse,
)
from app.services import forecasting_service

router = APIRouter(prefix="/predict", tags=["forecasting"])


@router.post("/sales-forecast", response_model=SalesForecastResponse)
def sales_forecast(payload: SalesForecastRequest) -> SalesForecastResponse:
    return forecasting_service.forecast_sales(payload)


@router.post("/demand", response_model=DemandPredictionResponse)
def demand_prediction(payload: DemandPredictionRequest) -> DemandPredictionResponse:
    return forecasting_service.predict_demand(payload)


@router.post("/profit", response_model=ProfitPredictionResponse)
def profit_prediction(payload: ProfitPredictionRequest) -> ProfitPredictionResponse:
    return forecasting_service.predict_profit(payload)
