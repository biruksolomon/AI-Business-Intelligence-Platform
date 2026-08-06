"""
Customer segmentation via K-Means clustering.

We fit K-Means once (on synthetic data shaped like real customer behavior)
at first use, then map each incoming customer's features to the nearest
cluster and label that cluster using simple business rules on the cluster's
centroid. Replace `_synthetic_customer_training_data` with a real feature
extract from the `customers`/`sales` tables once available.
"""

from typing import Literal, Optional

import numpy as np
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler

from app.schemas.segmentation import CustomerSegmentRequest, CustomerSegmentResponse

FEATURES = ["totalSpent", "purchaseFrequency", "recencyDays", "averageOrderValue"]

_N_CLUSTERS = 5


def _synthetic_customer_training_data():
    rng = np.random.default_rng(7)
    n = 500

    # Roughly emulate 5 natural customer archetypes so K-Means has something
    # sensible to converge to.
    segments = []
    segments.append(rng.normal([5000, 25, 5, 200], [800, 5, 3, 30], size=(n // 5, 4)))   # vip
    segments.append(rng.normal([1500, 12, 15, 120], [400, 4, 5, 25], size=(n // 5, 4)))  # loyal
    segments.append(rng.normal([200, 2, 10, 100], [80, 1, 5, 20], size=(n // 5, 4)))     # new
    segments.append(rng.normal([600, 6, 60, 90], [200, 2, 15, 20], size=(n // 5, 4)))    # at_risk
    segments.append(rng.normal([300, 3, 150, 80], [150, 1, 30, 20], size=(n // 5, 4)))   # churned

    data = np.vstack(segments)
    np.clip(data, 0, None, out=data)
    return data


_scaler: Optional[StandardScaler] = None
_kmeans: Optional[KMeans] = None
_cluster_labels: Optional[dict] = None


def _get_model():
    global _scaler, _kmeans, _cluster_labels
    if _kmeans is None:
        X = _synthetic_customer_training_data()
        _scaler = StandardScaler()
        X_scaled = _scaler.fit_transform(X)

        _kmeans = KMeans(n_clusters=_N_CLUSTERS, random_state=42, n_init=10)
        _kmeans.fit(X_scaled)

        # Map each cluster centroid back to a business label using simple,
        # interpretable rules (spend, frequency, recency).
        centroids = _scaler.inverse_transform(_kmeans.cluster_centers_)
        _cluster_labels = {}
        for idx, (spent, freq, recency, aov) in enumerate(centroids):
            if recency > 100:
                label = "churned"
            elif recency > 40:
                label = "at_risk"
            elif spent > 3000 and freq > 15:
                label = "vip"
            elif freq > 8:
                label = "loyal"
            else:
                label = "new"
            _cluster_labels[idx] = label

    return _scaler, _kmeans, _cluster_labels


def _churn_risk(recency_days: int, segment: str) -> Literal["low", "medium", "high"]:
    if segment == "churned" or recency_days > 100:
        return "high"
    if segment == "at_risk" or recency_days > 40:
        return "medium"
    return "low"


def segment_customer(req: CustomerSegmentRequest) -> CustomerSegmentResponse:
    scaler, kmeans, labels = _get_model()

    features = np.array(
        [[req.totalSpent, req.purchaseFrequency, req.recencyDays, req.averageOrderValue]]
    )
    features_scaled = scaler.transform(features)
    cluster = int(kmeans.predict(features_scaled)[0])
    segment = labels[cluster]

    churn_risk = _churn_risk(req.recencyDays, segment)

    distance = float(np.linalg.norm(features_scaled - kmeans.cluster_centers_[cluster]))
    confidence = round(float(np.clip(1 - distance / 4, 0.4, 0.95)), 2)

    explanation_map = {
        "vip": "High total spend and frequent purchases place this customer in the top tier.",
        "loyal": "Regular purchase frequency and healthy spend indicate a loyal, stable customer.",
        "new": "Low purchase history suggests this is a newer or infrequent customer.",
        "at_risk": "Spending and frequency are moderate but recency is elevated — engagement is slipping.",
        "churned": "No recent activity for an extended period suggests this customer has churned.",
    }

    return CustomerSegmentResponse(
        customerId=req.customerId,
        segment=segment,
        churnRisk=churn_risk,
        explanation=explanation_map[segment],
        confidence=confidence,
    )
