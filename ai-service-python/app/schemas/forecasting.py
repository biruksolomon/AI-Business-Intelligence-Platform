from typing import List, Literal, Optional

from pydantic import BaseModel, Field


class SalesForecastRequest(BaseModel):
    productCategory: str = Field(..., description="Category name, e.g. 'laptop-accessories'")
    historicalSales: List[float] = Field(
        ..., min_length=3, description="Chronological sales figures (oldest -> newest)"
    )
    periodDays: int = Field(30, gt=0, description="Number of days ahead to forecast")


class SalesForecastResponse(BaseModel):
    forecast: List[float]
    trend: Literal["rising", "declining", "stable"]
    percentageChange: float
    explanation: str
    confidence: float


class DemandPredictionRequest(BaseModel):
    productId: str
    currentStock: int = Field(..., ge=0)
    historicalSales: List[float] = Field(..., min_length=3)
    leadTimeDays: int = Field(7, gt=0, description="Supplier lead time in days")


class DemandPredictionResponse(BaseModel):
    productId: str
    predictedDemandNextPeriod: float
    daysUntilStockout: Optional[float]
    riskLevel: Literal["low", "medium", "high"]
    recommendation: str
    confidence: float


class ProfitPredictionRequest(BaseModel):
    productId: str
    historicalRevenue: List[float] = Field(..., min_length=3)
    historicalCost: List[float] = Field(..., min_length=3)
    periodDays: int = Field(30, gt=0)


class ProfitPredictionResponse(BaseModel):
    productId: str
    predictedProfitMargin: float
    predictedProfit: float
    trend: Literal["improving", "worsening", "stable"]
    confidence: float
