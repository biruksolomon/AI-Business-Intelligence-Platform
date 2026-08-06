"""
Forecasting logic for sales, demand, and profit.

For v1 we use lightweight, fast, explainable models (Linear Regression /
Random Forest) trained on-the-fly from the historical series passed in the
request. This keeps the service stateless and dependency-free (no model
files to ship yet). Once real historical data is available across many
products/customers, swap `_fit_linear_trend` / `_fit_random_forest_demand`
for models loaded from `app/models/*.pkl` (see app/models/README.md).
"""

from typing import List, Literal, Optional

import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LinearRegression

from app.schemas.forecasting import (
    DemandPredictionRequest,
    DemandPredictionResponse,
    ProfitPredictionRequest,
    ProfitPredictionResponse,
    SalesForecastRequest,
    SalesForecastResponse,
)


def _fit_linear_trend(series: List[float]):
    """Fit a simple linear regression of value vs. time index."""
    x = np.arange(len(series)).reshape(-1, 1)
    y = np.array(series, dtype=float)
    model = LinearRegression()
    model.fit(x, y)
    r2 = model.score(x, y) if len(series) > 2 else 0.5
    # Clamp R^2 into a usable confidence range so short/flat series don't
    # produce nonsensical negative confidence.
    confidence = float(max(0.3, min(0.97, r2)))
    return model, confidence


def _classify_trend(percentage_change: float) -> Literal["rising", "declining", "stable"]:
    if percentage_change > 3:
        return "rising"
    if percentage_change < -3:
        return "declining"
    return "stable"


def forecast_sales(req: SalesForecastRequest) -> SalesForecastResponse:
    series = req.historicalSales
    model, confidence = _fit_linear_trend(series)

    last_index = len(series) - 1
    # Roughly one forecast point per week of the requested period, capped so
    # the response stays a manageable size.
    steps = max(1, min(12, round(req.periodDays / 7)))
    future_x = np.arange(last_index + 1, last_index + 1 + steps).reshape(-1, 1)
    raw_forecast = model.predict(future_x)
    forecast = [max(0.0, round(float(v), 2)) for v in raw_forecast]

    baseline = np.mean(series[-min(3, len(series)):])
    projected = np.mean(forecast)
    percentage_change = 0.0 if baseline == 0 else round(((projected - baseline) / baseline) * 100, 1)
    trend = _classify_trend(percentage_change)

    direction = "increased" if trend == "rising" else "decreased" if trend == "declining" else "stayed roughly flat"
    explanation = (
        f"Based on the last {len(series)} data points for '{req.productCategory}', "
        f"sales are projected to have {direction} by {abs(percentage_change)}% "
        f"over the next {req.periodDays} days."
    )

    return SalesForecastResponse(
        forecast=forecast,
        trend=trend,
        percentageChange=percentage_change,
        explanation=explanation,
        confidence=round(confidence, 2),
    )


def _synthetic_demand_training_data():
    """
    Small synthetic dataset mapping (avg_sales, sales_volatility, stock_ratio)
    -> demand risk label, used to bootstrap a Random Forest until real
    historical demand data is wired in from the warehouse/inventory tables.
    """
    rng = np.random.default_rng(42)
    n = 300
    avg_sales = rng.uniform(1, 100, n)
    volatility = rng.uniform(0, 1, n)
    stock_ratio = rng.uniform(0, 3, n)  # current_stock / avg_sales

    # Heuristic ground truth: low stock relative to demand + high average
    # sales + high volatility => higher demand pressure next period.
    demand_next = avg_sales * (1 + 0.15 * volatility) * (1.1 - np.minimum(stock_ratio, 1) * 0.1)

    X = np.column_stack([avg_sales, volatility, stock_ratio])
    y = demand_next
    return X, y


_demand_model: Optional[RandomForestRegressor] = None


def _get_demand_model() -> RandomForestRegressor:
    global _demand_model
    if _demand_model is None:
        X, y = _synthetic_demand_training_data()
        _demand_model = RandomForestRegressor(n_estimators=100, max_depth=6, random_state=42)
        _demand_model.fit(X, y)
    return _demand_model


def predict_demand(req: DemandPredictionRequest) -> DemandPredictionResponse:
    series = np.array(req.historicalSales, dtype=float)
    avg_sales = float(np.mean(series))
    volatility = float(np.std(series) / avg_sales) if avg_sales > 0 else 0.0
    volatility = min(volatility, 1.0)
    stock_ratio = (req.currentStock / avg_sales) if avg_sales > 0 else 3.0

    model = _get_demand_model()
    predicted_demand = float(model.predict([[avg_sales, volatility, stock_ratio]])[0])
    predicted_demand = max(0.0, round(predicted_demand, 2))

    days_until_stockout: Optional[float] = None
    if predicted_demand > 0:
        daily_rate = predicted_demand / 7.0  # predicted_demand is a ~weekly figure
        if daily_rate > 0:
            days_until_stockout = round(req.currentStock / daily_rate, 1)

    if days_until_stockout is not None and days_until_stockout <= req.leadTimeDays:
        risk_level: Literal["low", "medium", "high"] = "high"
        recommendation = (
            f"Reorder now — projected stockout in {days_until_stockout} days, "
            f"which is within the {req.leadTimeDays}-day supplier lead time."
        )
    elif days_until_stockout is not None and days_until_stockout <= req.leadTimeDays * 2:
        risk_level = "medium"
        recommendation = (
            f"Plan a reorder soon — projected stockout in {days_until_stockout} days."
        )
    else:
        risk_level = "low"
        recommendation = "Stock levels look sufficient for the projected demand."

    confidence = round(float(np.clip(1 - volatility, 0.4, 0.95)), 2)

    return DemandPredictionResponse(
        productId=req.productId,
        predictedDemandNextPeriod=predicted_demand,
        daysUntilStockout=days_until_stockout,
        riskLevel=risk_level,
        recommendation=recommendation,
        confidence=confidence,
    )


def predict_profit(req: ProfitPredictionRequest) -> ProfitPredictionResponse:
    revenue = np.array(req.historicalRevenue, dtype=float)
    cost = np.array(req.historicalCost, dtype=float)
    n = min(len(revenue), len(cost))
    revenue, cost = revenue[:n], cost[:n]

    margins = np.divide(
        revenue - cost, revenue, out=np.zeros_like(revenue), where=revenue != 0
    )

    model, confidence = _fit_linear_trend(list(margins))
    next_index = np.array([[n]])
    predicted_margin = float(model.predict(next_index)[0])
    predicted_margin = round(float(np.clip(predicted_margin, -1, 1)), 3)

    avg_revenue = float(np.mean(revenue[-min(3, n):])) if n else 0.0
    scale = req.periodDays / 30.0
    predicted_profit = round(avg_revenue * predicted_margin * scale, 2)

    margin_change = (margins[-1] - margins[0]) if n > 1 else 0.0
    if margin_change > 0.02:
        trend: Literal["improving", "worsening", "stable"] = "improving"
    elif margin_change < -0.02:
        trend = "worsening"
    else:
        trend = "stable"

    return ProfitPredictionResponse(
        productId=req.productId,
        predictedProfitMargin=predicted_margin,
        predictedProfit=predicted_profit,
        trend=trend,
        confidence=round(confidence, 2),
    )
