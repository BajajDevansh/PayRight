from pydantic_settings import BaseSettings
import os
from dotenv import load_dotenv

load_dotenv()

class Settings(BaseSettings):
    gemini_api_key: str = os.getenv("GEMINI_API_KEY")
    gemini_model_name: str = "gemini-1.5-flash-latest" # Or gemini-pro or other suitable model

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'

settings = Settings()