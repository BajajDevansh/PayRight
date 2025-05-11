from fastapi import FastAPI, HTTPException, Body
from typing import List, Optional
import logging
import decimal  # Import decimal
from datetime import date  # Import date

from app.models.schemas import (
    TransactionInput,
    AnalysisResult,
    IdentifiedSubscription,
    SubscriptionAlternativeRequest,
    SubscriptionAlternativeResponse
)
from app.services.gemini_service import GeminiService
from app.config import settings  # Import settings for API key check
from app.analysis.alternatives_suggester import get_gemini_alternatives

# Configure basic logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="PayRight AI Service (Gemini Enhanced)",
    description="Analyzes transaction data and email content using Google Gemini to detect recurring subscriptions.",
    version="0.2.0"
)

# Initialize Gemini Service (singleton-like for the app instance)
gemini_service: Optional[GeminiService] = None

@app.on_event("startup")
async def startup_event():
    global gemini_service
    logger.info("PayRight AI Service (Gemini Enhanced) starting up...")
    if not settings.gemini_api_key or settings.gemini_api_key == "YOUR_GEMINI_API_KEY_HERE":
        logger.error("GEMINI_API_KEY is not configured. Service will not function correctly.")
        gemini_service = None  # Explicitly set to None
    else:
        try:
            gemini_service = GeminiService()
            logger.info("Gemini Service initialized.")
        except Exception as e:
            logger.error(f"Failed to initialize Gemini Service during startup: {e}")
            gemini_service = None  # Ensure it's None if init fails

@app.get("/", tags=["General"])
async def root():
    """Root endpoint providing service status."""
    status = "running"
    if gemini_service is None and (not settings.gemini_api_key or settings.gemini_api_key == "YOUR_GEMINI_API_KEY_HERE"):
        status = "degraded - GEMINI_API_KEY not configured"
    elif gemini_service is None:
        status = "degraded - Gemini Service initialization failed"

    return {"message": f"PayRight AI Service (Gemini Enhanced) is {status}."}

@app.post("/analyze-transactions-gemini",
          response_model=AnalysisResult,
          tags=["Subscription Analysis"],
          summary="Analyze a list of transactions using Gemini to detect subscriptions")
async def analyze_transactions_gemini_endpoint(
    transactions: List[TransactionInput] = Body(
        ...,
        example=[
            # Example transactions
            {
                "id": "txn_001",
                "userId": "user123",
                "transaction_date": "2023-01-15",
                "description": "NETFLIX.COM",
                "amount": 15.99,
                "currency": "USD",
                "source": "Bank A"
            }
        ]
    )
):
    if gemini_service is None:
        logger.error("Gemini service not available for /analyze-transactions-gemini")
        raise HTTPException(status_code=503, detail="Gemini service is not available. Check API key or initialization.")

    if not transactions:
        logger.warning("Received empty transaction list for Gemini analysis.")
        raise HTTPException(status_code=400, detail="No transactions provided for analysis.")

    logger.info(f"Received {len(transactions)} transactions for Gemini analysis. UserID: {transactions[0].userId if transactions else 'N/A'}")

    try:
        gemini_identified_subs_raw = gemini_service.analyze_transactions_for_subscriptions(transactions)

        validated_subscriptions = []
        for sub_data in gemini_identified_subs_raw:
            try:
                if 'average_amount' in sub_data and isinstance(sub_data['average_amount'], (float, int)):
                    sub_data['average_amount'] = decimal.Decimal(str(sub_data['average_amount']))

                for date_field in ['first_transaction_date', 'last_transaction_date', 'potential_next_billing_date']:
                    if date_field in sub_data and isinstance(sub_data[date_field], str):
                        sub_data[date_field] = date.fromisoformat(sub_data[date_field])
                    elif date_field in sub_data and sub_data[date_field] is None:
                        pass

                validated_subscriptions.append(IdentifiedSubscription(**sub_data))
            except Exception as pydantic_exc:
                logger.error(f"Failed to validate subscription data from Gemini: {sub_data}. Error: {pydantic_exc}")

        logger.info(f"Gemini identified and validated {len(validated_subscriptions)} potential subscriptions.")

        user_id_from_data = transactions[0].userId if transactions else None

        return AnalysisResult(
            user_id=user_id_from_data,
            processed_transaction_ids=[t.id for t in transactions],
            identified_subscriptions=validated_subscriptions
        )
    except Exception as e:
        logger.error(f"Error during Gemini transaction analysis endpoint: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Internal server error during Gemini analysis: {str(e)}")

@app.post("/suggest-alternatives-gemini", response_model=SubscriptionAlternativeResponse)
async def suggest_alternatives_gemini_endpoint(request: SubscriptionAlternativeRequest):
    """
    Suggests alternatives for a given subscription service name using Gemini AI.
    """
    service_name = request.service_name
    if not service_name:
        raise HTTPException(status_code=400, detail="Service name must be provided.")

    try:
        alternatives = get_gemini_alternatives(service_name)
        message = f"Found {len(alternatives)} alternatives for '{service_name}'."
        return SubscriptionAlternativeResponse(
            requested_service=service_name,
            alternatives=alternatives,
            message=message
        )
    except Exception as e:
        logger.error(f"Error suggesting alternatives for {service_name}: {e}")
        raise HTTPException(status_code=500, detail=f"Internal server error while suggesting alternatives: {str(e)}")