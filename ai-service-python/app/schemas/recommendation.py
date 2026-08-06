from typing import List, Optional

from pydantic import BaseModel, Field


class PurchasedItem(BaseModel):
    productId: str
    category: str
    quantity: int = Field(..., ge=1)


class ProductRecommendationRequest(BaseModel):
    customerId: str
    purchaseHistory: List[PurchasedItem] = Field(default_factory=list)
    candidateProducts: List[str] = Field(
        ..., description="Pool of product IDs/categories eligible to be recommended"
    )
    topN: int = Field(5, gt=0, le=50)


class RecommendedProduct(BaseModel):
    productId: str
    score: float
    reason: str


class ProductRecommendationResponse(BaseModel):
    customerId: str
    recommendations: List[RecommendedProduct]


class RestockRecommendationRequest(BaseModel):
    products: List[str]
    stockLevels: List[int]
    historicalSales: List[List[float]] = Field(
        ..., description="One historical sales series per product, aligned by index"
    )


class RestockItem(BaseModel):
    productId: str
    priority: int
    reason: str


class RestockRecommendationResponse(BaseModel):
    restockPriorities: List[RestockItem]


class SupplierProfile(BaseModel):
    supplierId: str
    averagePrice: float = Field(..., ge=0)
    reliabilityScore: float = Field(..., ge=0, le=1, description="0-1, historical on-time delivery rate")
    averageDeliveryDays: float = Field(..., ge=0)


class SupplierRecommendationRequest(BaseModel):
    productId: str
    suppliers: List[SupplierProfile]


class RankedSupplier(BaseModel):
    supplierId: str
    rank: int
    score: float


class SupplierRecommendationResponse(BaseModel):
    productId: str
    rankedSuppliers: List[RankedSupplier]
    bestChoice: str
