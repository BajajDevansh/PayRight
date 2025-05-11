import google.generativeai as genai
from app.config import settings
from app.models.schemas import TransactionInput
from typing import List, Dict, Any, Optional
import json
import logging

logger = logging.getLogger(__name__)  # Ensure this is at the module level

class GeminiService:
    def __init__(self, model_name: str = settings.gemini_model_name):
        try:
            self.model = genai.GenerativeModel(
                model_name,
                safety_settings=safety_settings,
                generation_config=generation_config
            )
            logger.info(f"Gemini model '{model_name}' initialized successfully.")
        except Exception as e:
            logger.error(f"Failed to initialize Gemini model '{model_name}': {e}")
            raise

    def analyze_transactions_for_subscriptions(self, transactions: List[TransactionInput]) -> List[Dict[str, Any]]:
        if not transactions:
            return []

        logger.info(f"Attempting to analyze {len(transactions)} transactions for user: {transactions[0].userId if transactions else 'N/A'}")
        
        transaction_data_with_ids_str = "Here is a list of transactions with their IDs:\n"
        for tx in transactions:
            transaction_data_with_ids_str += f"- ID: {tx.id}, Date: {tx.transaction_date}, Description: \"{tx.description}\", Amount: {tx.amount} {tx.currency}\n"

        prompt_with_ids = f"""
            You are an expert financial analyst specializing in identifying recurring subscriptions from transaction lists.
            Analyze the following transactions for user '{transactions[0].userId if transactions else 'unknown'}'
            and identify any recurring subscriptions.

            For each identified subscription, provide the following information in JSON format:
            - "name": The common name of the subscription service (e.g., "Netflix", "Spotify", "Amazon Prime").
            - "transaction_ids": A list of the *original transaction IDs* (provided in the input) that belong to this subscription.
            - "average_amount": The average monthly or periodic amount of the subscription as a number.
            - "currency": The currency of the subscription (e.g., "USD").
            - "detected_frequency": The detected recurrence frequency (e.g., "monthly", "yearly", "weekly", "bi-weekly").
            - "first_transaction_date": The date of the earliest transaction in this group (YYYY-MM-DD).
            - "last_transaction_date": The date of the latest transaction in this group (YYYY-MM-DD).
            - "confidence_score": Your confidence in this identification (a number from 0.0 to 1.0).
            - "potential_next_billing_date": Your best estimate for the next billing date (YYYY-MM-DD), if calculable.

            Transaction Data:
            {transaction_data_with_ids_str}

            Based on the data, identify all subscriptions.
            Return the output strictly as a JSON list of subscription objects.
            For example:
            [
              {{
                "name": "Netflix",
                "transaction_ids": ["txn_id_1", "txn_id_abc"],
                "average_amount": 15.99,
                "currency": "USD",
                "detected_frequency": "monthly",
                "first_transaction_date": "2023-01-15",
                "last_transaction_date": "2023-02-15",
                "confidence_score": 0.9,
                "potential_next_billing_date": "2023-03-15"
              }}
            ]
            If no subscriptions are found, return an empty JSON list: [].
            Ensure all monetary amounts are numbers, not strings with currency symbols.
            Ensure all dates are in YYYY-MM-DD format.
        """
        
        logger.debug(f"--- PROMPT SENT TO GEMINI ---:\n{prompt_with_ids}\n--- END OF PROMPT ---")

        try:
            response = self.model.generate_content(prompt_with_ids)
            response_text = response.text.strip()
            
            logger.debug(f"--- RAW GEMINI RESPONSE ---:\n{response_text}\n--- END OF RAW RESPONSE ---")

            if response_text.startswith("```json"):
                response_text = response_text[7:]
            if response_text.endswith("```"):
                response_text = response_text[:-3]
            response_text = response_text.strip()

            logger.debug(f"--- CLEANED GEMINI RESPONSE ---:\n{response_text}\n--- END OF CLEANED RESPONSE ---")

            parsed_response = json.loads(response_text)
            if not isinstance(parsed_response, list):
                logger.error(f"Gemini response was not a list after parsing: {parsed_response}")
                return []
            logger.info(f"Successfully parsed Gemini response. Found {len(parsed_response)} potential subscriptions.")
            return parsed_response

        except json.JSONDecodeError as e:
            logger.error(f"JSONDecodeError: Failed to decode JSON from Gemini response. Error: {e}. Response text was: '{response_text}'")
            return []
        except Exception as e:
            logger.error(f"Exception during Gemini API call or processing: {e}", exc_info=True)
            return []

    async def analyze_email_content(self, email_body: str, email_subject: Optional[str] = None) -> Optional[Dict[str, Any]]:
        """
        Analyzes email content to extract potential subscription/transaction details.
        This is a conceptual example; the prompt needs careful crafting.
        """
        prompt = f"""
            You are an intelligent assistant that extracts transaction information from emails.
            Analyze the following email content.
            Email Subject (if available): "{email_subject if email_subject else 'N/A'}"
            Email Body:
            ---
            {email_body[:4000]}
            ---
            If this email appears to be a receipt, invoice, or subscription confirmation,
            extract the following information in JSON format:
            - "merchant_name": The name of the merchant or service provider.
            - "transaction_amount": The total amount of the transaction (as a number).
            - "currency": The currency code (e.g., "USD", "EUR").
            - "transaction_date": The date of the transaction or billing (YYYY-MM-DD).
            - "is_recurring": Boolean, true if the email suggests this is a recurring charge/subscription, otherwise false.
            - "recurrence_details": A string describing recurrence if mentioned (e.g., "monthly", "billed annually").
            - "items": A list of items or services purchased, if detailed. Each item as a dict with "name" and "price".

            If the email does not seem to be a transaction receipt or subscription confirmation, return null.

            Example of desired JSON output:
            {{
                "merchant_name": "Spotify AB",
                "transaction_amount": 9.99,
                "currency": "USD",
                "transaction_date": "2023-03-01",
                "is_recurring": true,
                "recurrence_details": "Your monthly subscription",
                "items": [{{ "name": "Spotify Premium Individual", "price": 9.99 }}]
            }}
            Output only the JSON object or null.
        """
        try:
            response = self.model.generate_content(prompt)
            response_text = response.text.strip()

            if response_text.lower() == "null":
                return None

            if response_text.startswith("```json"):
                response_text = response_text[7:]
            if response_text.endswith("```"):
                response_text = response_text[:-3]
            response_text = response_text.strip()

            extracted_data = json.loads(response_text)
            return extracted_data
        except Exception as e:
            logger.error(f"Error analyzing email with Gemini: {e}")
            return None
genai.configure(api_key=settings.gemini_api_key)

# --- Safety Settings (Adjust as needed) ---
# See https://ai.google.dev/docs/safety_setting_gemini
# For financial data, you might want to be cautious.
# These are examples, review Google's documentation.
safety_settings = [
    {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE"},
    {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE"},
    {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "BLOCK_NONE"},
    {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "BLOCK_NONE"},
]

generation_config = genai.types.GenerationConfig(
    # candidate_count=1, # Default
    # stop_sequences=None, # Default
    # max_output_tokens=8192, # For Gemini 1.5 Flash
    temperature=0.3, # Lower for more deterministic, factual output
    # top_p=1.0, # Default
    # top_k=None, # Default
)