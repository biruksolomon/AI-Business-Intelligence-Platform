from fastapi import APIRouter

from app.schemas.segmentation import (
    BatchCustomerSegmentRequest,
    BatchCustomerSegmentResponse,
    CustomerSegmentRequest,
    CustomerSegmentResponse,
)
from app.services import segmentation_service

router = APIRouter(prefix="/predict", tags=["segmentation"])


@router.post("/customer-segment", response_model=CustomerSegmentResponse)
def customer_segment(payload: CustomerSegmentRequest) -> CustomerSegmentResponse:
    return segmentation_service.segment_customer(payload)


@router.post("/customer-segment/batch", response_model=BatchCustomerSegmentResponse)
def customer_segment_batch(payload: BatchCustomerSegmentRequest) -> BatchCustomerSegmentResponse:
    results = [segmentation_service.segment_customer(c) for c in payload.customers]
    return BatchCustomerSegmentResponse(results=results)
