"""
Recommendation logic: product recommendations (content-based on category
overlap), restock priority ranking, and supplier ranking.

These use straightforward, explainable scoring rather than black-box models
since business users need to trust and act on the output. Collaborative
filtering can be layered in once there is enough cross-customer purchase
data to build a meaningful item-item similarity matrix.
"""

from collections import Counter
from typing import List

from app.schemas.recommendation import (
    ProductRecommendationRequest,
    ProductRecommendationResponse,
    RankedSupplier,
    RecommendedProduct,
    RestockItem,
    RestockRecommendationRequest,
    RestockRecommendationResponse,
    SupplierRecommendationRequest,
    SupplierRecommendationResponse,
)


def recommend_products(req: ProductRecommendationRequest) -> ProductRecommendationResponse:
    category_counts = Counter(item.category for item in req.purchaseHistory)
    purchased_ids = {item.productId for item in req.purchaseHistory}
    total_purchases = sum(category_counts.values()) or 1

    scored: List[RecommendedProduct] = []
    for candidate in req.candidateProducts:
        if candidate in purchased_ids:
            continue

        # Without a real product catalog here we score purely on whether the
        # candidate ID/category token matches the customer's favorite
        # category strings; Spring Boot should pass category-qualified IDs
        # (e.g. "laptop-accessories:sku123") for a real content-based match.
        best_match_ratio = 0.0
        best_category = None
        for category, count in category_counts.items():
            if category.lower() in candidate.lower():
                ratio = count / total_purchases
                if ratio > best_match_ratio:
                    best_match_ratio = ratio
                    best_category = category

        base_score = 0.3 if best_category is None else 0.3 + 0.7 * best_match_ratio
        reason = (
            f"Frequently buys from '{best_category}'"
            if best_category
            else "Popular candidate with no direct category match yet"
        )
        scored.append(RecommendedProduct(productId=candidate, score=round(base_score, 3), reason=reason))

    scored.sort(key=lambda r: r.score, reverse=True)
    top = scored[: req.topN]

    return ProductRecommendationResponse(customerId=req.customerId, recommendations=top)


def recommend_restock(req: RestockRecommendationRequest) -> RestockRecommendationResponse:
    n = min(len(req.products), len(req.stockLevels), len(req.historicalSales))
    urgency_scores = []

    for i in range(n):
        series = req.historicalSales[i]
        avg_sales = sum(series) / len(series) if series else 0.0
        stock = req.stockLevels[i]
        days_of_cover = (stock / (avg_sales / 7)) if avg_sales > 0 else float("inf")
        urgency_scores.append((req.products[i], days_of_cover, avg_sales))

    # Lower days_of_cover = more urgent = higher priority (rank 1 first).
    urgency_scores.sort(key=lambda t: t[1])

    priorities = []
    for rank, (product_id, days_of_cover, avg_sales) in enumerate(urgency_scores, start=1):
        if days_of_cover == float("inf"):
            reason = "No recent sales velocity; low restock urgency."
        else:
            reason = f"~{round(days_of_cover, 1)} days of stock left at current sales velocity."
        priorities.append(RestockItem(productId=product_id, priority=rank, reason=reason))

    return RestockRecommendationResponse(restockPriorities=priorities)


def recommend_suppliers(req: SupplierRecommendationRequest) -> SupplierRecommendationResponse:
    if not req.suppliers:
        return SupplierRecommendationResponse(productId=req.productId, rankedSuppliers=[], bestChoice="")

    prices = [s.averagePrice for s in req.suppliers]
    min_price, max_price = min(prices), max(prices)
    delivery_days = [s.averageDeliveryDays for s in req.suppliers]
    min_days, max_days = min(delivery_days), max(delivery_days)

    def normalize(value: float, lo: float, hi: float, invert: bool = False) -> float:
        if hi == lo:
            return 1.0
        score = (value - lo) / (hi - lo)
        return 1 - score if invert else score

    ranked = []
    for supplier in req.suppliers:
        price_score = normalize(supplier.averagePrice, min_price, max_price, invert=True)
        delivery_score = normalize(supplier.averageDeliveryDays, min_days, max_days, invert=True)
        reliability_score = supplier.reliabilityScore

        # Weighted composite: reliability matters most, then price, then speed.
        composite = 0.5 * reliability_score + 0.3 * price_score + 0.2 * delivery_score
        ranked.append((supplier.supplierId, composite))

    ranked.sort(key=lambda t: t[1], reverse=True)
    ranked_suppliers = [
        RankedSupplier(supplierId=sid, rank=i + 1, score=round(score, 3))
        for i, (sid, score) in enumerate(ranked)
    ]

    return SupplierRecommendationResponse(
        productId=req.productId,
        rankedSuppliers=ranked_suppliers,
        bestChoice=ranked_suppliers[0].supplierId,
    )
