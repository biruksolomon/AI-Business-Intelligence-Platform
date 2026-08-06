from fastapi import APIRouter

from app.schemas.recommendation import (
    ProductRecommendationRequest,
    ProductRecommendationResponse,
    RestockRecommendationRequest,
    RestockRecommendationResponse,
    SupplierRecommendationRequest,
    SupplierRecommendationResponse,
)
from app.services import recommendation_service

router = APIRouter(prefix="/predict", tags=["recommendation"])


@router.post("/product-recommendation", response_model=ProductRecommendationResponse)
def product_recommendation(payload: ProductRecommendationRequest) -> ProductRecommendationResponse:
    return recommendation_service.recommend_products(payload)


@router.post("/restock-recommendation", response_model=RestockRecommendationResponse)
def restock_recommendation(payload: RestockRecommendationRequest) -> RestockRecommendationResponse:
    return recommendation_service.recommend_restock(payload)


@router.post("/supplier-recommendation", response_model=SupplierRecommendationResponse)
def supplier_recommendation(payload: SupplierRecommendationRequest) -> SupplierRecommendationResponse:
    return recommendation_service.recommend_suppliers(payload)
