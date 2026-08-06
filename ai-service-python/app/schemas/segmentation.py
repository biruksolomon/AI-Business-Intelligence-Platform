from typing import List, Literal

from pydantic import BaseModel, Field


class CustomerSegmentRequest(BaseModel):
    customerId: str
    totalSpent: float = Field(..., ge=0)
    purchaseFrequency: int = Field(..., ge=0, description="Number of purchases in the observed window")
    recencyDays: int = Field(..., ge=0, description="Days since the last purchase")
    averageOrderValue: float = Field(..., ge=0)


class CustomerSegmentResponse(BaseModel):
    customerId: str
    segment: Literal["vip", "loyal", "new", "at_risk", "churned"]
    churnRisk: Literal["low", "medium", "high"]
    explanation: str
    confidence: float


class BatchCustomerSegmentRequest(BaseModel):
    customers: List[CustomerSegmentRequest]


class BatchCustomerSegmentResponse(BaseModel):
    results: List[CustomerSegmentResponse]
