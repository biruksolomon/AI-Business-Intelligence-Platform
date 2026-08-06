# app/models/

This folder is where trained model artifacts (`.pkl` / `.joblib`) will live
once real historical data is available.

Right now, `services/forecasting_service.py` and
`services/segmentation_service.py` fit lightweight models **in-memory on
synthetic data** the first time they're used (module-level lazy singletons),
so the API is fully functional without any training pipeline yet.

## How to upgrade to real trained models

1. Export historical data from the Spring Boot / PostgreSQL side (sales,
   customers, inventory) into CSVs or query it directly with `pandas` +
   `sqlalchemy` in a training script under `notebooks/` or a new
   `app/training/` folder.
2. Train the model with scikit-learn as usual, then persist it:
   ```python
   import joblib
   joblib.dump(model, "app/models/demand_model.pkl")
   ```
3. In the relevant service module, replace the `_get_*_model()` lazy-fit
   function with a `joblib.load("app/models/demand_model.pkl")` call.
4. Keep the request/response Pydantic schemas the same so the Spring Boot
   client and Flutter app are unaffected by the swap.
