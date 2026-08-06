from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import forecasting, recommendation, segmentation

app = FastAPI(
    title="AI Business Intelligence Platform - AI Service",
    description=(
        "Internal prediction microservice consumed by the Spring Boot "
        "backend. Provides sales forecasting, demand prediction, profit "
        "prediction, customer segmentation, and product/supplier "
        "recommendations."
    ),
    version="0.1.0",
)

# The Spring Boot backend calls this service server-to-server, but CORS is
# kept permissive here for local development against the FastAPI docs UI
# and any browser-based tooling. Lock this down to known origins in
# production via an environment variable.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(forecasting.router)
app.include_router(segmentation.router)
app.include_router(recommendation.router)


@app.get("/", tags=["health"])
def root():
    return {"service": "ai-service-python", "status": "ok"}


@app.get("/health", tags=["health"])
def health():
    return {"status": "healthy"}