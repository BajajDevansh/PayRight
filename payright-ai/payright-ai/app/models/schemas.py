from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import date
import decimal

# Ensure __init__.py files are present in app, app/models, app/analysis folders.

class TransactionInput(BaseModel):
    id: str = Field(..., description="Unique identifier for the transaction")
    userId: str = Field(..., description="Identifier for the user associated with the transaction")
    transaction_date: date = Field(..., description="Date of the transaction")  # Renamed from 'date'
    description: str = Field(..., description="Transaction description or merchant name")
    amount: decimal.Decimal = Field(..., description="Transaction amount")
    currency: str = Field(..., description="Currency code (e.g., USD, EUR)")
    source: Optional[str] = Field(None, description="Source of the transaction data (e.g., 'Bank A', 'Mock')")

    model_config = {
        "json_encoders": {
            decimal.Decimal: lambda v: str(v)
        },
        "from_attributes": True
    }

class IdentifiedSubscription(BaseModel):
    name: str = Field(..., description="Identified name of the subscription (e.g., 'Netflix', 'Spotify')")
    transaction_ids: List[str] = Field(..., description="List of transaction IDs forming this subscription")
    average_amount: decimal.Decimal = Field(..., description="Average amount of the subscription")
    currency: str = Field(..., description="Currency of the subscription")
    detected_frequency: str = Field(..., description="Detected recurrence frequency (e.g., 'monthly', 'yearly', 'bi-weekly', 'unknown')")
    first_transaction_date: date = Field(..., description="Date of the earliest transaction in this group")
    last_transaction_date: date = Field(..., description="Date of the latest transaction in this group")
    confidence_score: float = Field(..., ge=0.0, le=1.0, description="Confidence score of this identification (0.0 to 1.0)")
    potential_next_billing_date: Optional[date] = Field(None, description="Estimated next billing date based on frequency")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Additional metadata like matched keywords")

    model_config = {
        "json_encoders": {
            decimal.Decimal: lambda v: str(v)
        },
        "from_attributes": True
    }

class AnalysisResult(BaseModel):
    user_id: Optional[str] = Field(None, description="User ID for whom the analysis was performed (if available from input)")
    processed_transaction_ids: List[str] = Field(..., description="List of all transaction IDs processed in this batch")
    identified_subscriptions: List[IdentifiedSubscription] = Field(..., description="List of subscriptions identified from the transactions")
    # Alerts might be better handled by the backend based on this data and user settings
    # For now, we'll focus on identification.
    # alerts: List[str] = Field(default_factory=list, description="Contextual alerts based on analysis")

    model_config = {
        "from_attributes": True
    }
class AlternativeDetail(BaseModel):
    name: str
    description: Optional[str] = None
    category: Optional[str] = None # e.g., "Streaming Video", "Music", "Cloud Storage"
    notes: Optional[str] = None # e.g., "Ad-supported free tier available"

class SubscriptionAlternativeRequest(BaseModel):
    service_name: str = Field(..., example="Netflix", description="The name of the subscription service to find alternatives for.")
    # Optionally, you could add current_price, features_used, etc., for more tailored suggestions later.

class SubscriptionAlternativeResponse(BaseModel):
    requested_service: str
    alternatives: List[AlternativeDetail]
    message: Optional[str] = None

# Update AISuggestionOutput if you want to use it generically,
# or keep it separate if preferred. For clarity, let's keep it separate for now.
class AISuggestionOutput(BaseModel):
    message: str
    suggestions: Optional[List[dict]] = None # This can be kept generic for other types of suggestions