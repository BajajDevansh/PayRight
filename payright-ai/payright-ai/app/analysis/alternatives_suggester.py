# app/analysis/alternatives_suggester.py
import logging
from typing import List, Dict, Optional
from app.models.schemas import AlternativeDetail

# Configure a logger for this module
logger = logging.getLogger(__name__)

# --- KNOWLEDGE BASE FOR ALTERNATIVES ---
# Hardcoded list of popular subscriptions and their alternatives.
# Keys should be normalized (e.g., lowercase) for easier matching.
ALTERNATIVES_DB: Dict[str, List[Dict[str, Optional[str]]]] = {
    "netflix": [
        {"name": "Hulu", "description": "Offers live TV options and a different original content library.", "category": "Streaming Video", "notes": "Bundle with Disney+ and ESPN+ available."},
        {"name": "Amazon Prime Video", "description": "Included with Amazon Prime, good selection of movies and originals.", "category": "Streaming Video"},
        {"name": "Disney+", "description": "Focuses on Disney, Pixar, Marvel, Star Wars, and National Geographic content.", "category": "Streaming Video"},
        {"name": "HBO Max (Max)", "description": "Premium content from HBO, Warner Bros, DC, and Max Originals.", "category": "Streaming Video"},
        {"name": "Apple TV+", "description": "Growing library of high-quality original series and films.", "category": "Streaming Video"},
        {"name": "Peacock", "description": "Content from NBCUniversal, including a free ad-supported tier.", "category": "Streaming Video", "notes": "Free tier available."},
    ],
    "spotify": [
        {"name": "Apple Music", "description": "Large music library, integrates well with Apple ecosystem.", "category": "Music Streaming"},
        {"name": "YouTube Music", "description": "Vast catalog including official songs and user uploads, part of YouTube Premium.", "category": "Music Streaming"},
        {"name": "Amazon Music Unlimited", "description": "Extensive music library, HD audio options.", "category": "Music Streaming"},
        {"name": "Tidal", "description": "Focus on high-fidelity audio and artist exclusives.", "category": "Music Streaming"},
    ],
    "hulu": [
        {"name": "Netflix", "description": "Vast library of movies, TV shows, and original content.", "category": "Streaming Video"},
        {"name": "Sling TV", "description": "Live TV streaming with customizable channel packages.", "category": "Live TV Streaming"},
        {"name": "YouTube TV", "description": "Live TV from major broadcast and cable networks.", "category": "Live TV Streaming"},
    ],
    "amazon prime video": [
        {"name": "Netflix", "description": "Large library of original and licensed content.", "category": "Streaming Video"},
        {"name": "Hulu", "description": "Offers current TV episodes and original series.", "category": "Streaming Video"},
    ],
    "disney+": [
        {"name": "Netflix", "description": "Broader selection of general entertainment.", "category": "Streaming Video"},
        {"name": "HBO Max (Max)", "description": "Premium HBO content and Warner Bros. library.", "category": "Streaming Video"},
    ],
    "hbo max": [ # Also covers "Max"
        {"name": "Netflix", "description": "Wide variety of content including many originals.", "category": "Streaming Video"},
        {"name": "Disney+", "description": "Family-friendly content from Disney, Pixar, Marvel, etc.", "category": "Streaming Video"},
        {"name": "Showtime", "description": "Original series, movies, and sports.", "category": "Streaming Video"},
    ],
    "apple music": [
        {"name": "Spotify", "description": "Popular music streaming with a vast library and podcasts.", "category": "Music Streaming"},
        {"name": "YouTube Music", "description": "Large catalog including user uploads and music videos.", "category": "Music Streaming"},
    ],
    "youtube premium": [ # Often includes YouTube Music
        {"name": "Spotify", "description": "Ad-free music and podcasts.", "category": "Music Streaming"},
        {"name": "Netflix", "description": "For video content, if that's the primary use.", "category": "Streaming Video"},
        {"name": "Nebula", "description": "Creator-owned streaming service with educational content.", "category": "Streaming Video"},
    ],
    "dropbox": [
        {"name": "Google Drive", "description": "Generous free storage, integrates with Google Workspace.", "category": "Cloud Storage", "notes": "Part of Google One for more storage."},
        {"name": "Microsoft OneDrive", "description": "Integrates with Windows and Microsoft 365.", "category": "Cloud Storage"},
        {"name": "iCloud Drive", "description": "Seamless integration for Apple users.", "category": "Cloud Storage"},
        {"name": "Box", "description": "Focus on business collaboration and security.", "category": "Cloud Storage"},
    ],
    "google drive": [ # Also covers Google One
        {"name": "Dropbox", "description": "Popular for file syncing and sharing, strong cross-platform support.", "category": "Cloud Storage"},
        {"name": "Microsoft OneDrive", "description": "Good for Windows users and Office integration.", "category": "Cloud Storage"},
    ],
    "microsoft onedrive": [
        {"name": "Google Drive", "description": "Cross-platform cloud storage with good free tier.", "category": "Cloud Storage"},
        {"name": "Dropbox", "description": "Reliable file syncing and sharing.", "category": "Cloud Storage"},
    ],
    "icloud": [ # Assuming iCloud storage
        {"name": "Google Drive", "description": "Generous free storage, cross-platform.", "category": "Cloud Storage"},
        {"name": "Dropbox", "description": "Popular for file syncing and sharing.", "category": "Cloud Storage"},
        {"name": "Microsoft OneDrive", "description": "Good for Windows users and Office integration.", "category": "Cloud Storage"},
    ],
    "adobe creative cloud": [
        {"name": "Affinity Suite (Photo, Designer, Publisher)", "description": "One-time purchase software for photo editing, graphic design, and desktop publishing.", "category": "Creative Software"},
        {"name": "DaVinci Resolve", "description": "Professional video editing software with a powerful free version.", "category": "Video Editing"},
        {"name": "Canva", "description": "User-friendly online design tool, good for social media graphics.", "category": "Graphic Design"},
        {"name": "GIMP", "description": "Free and open-source image editor.", "category": "Photo Editing"},
    ],
    "microsoft 365": [ # Also covers Office 365
        {"name": "Google Workspace", "description": "Suite of online productivity tools (Docs, Sheets, Slides).", "category": "Productivity Suite"},
        {"name": "LibreOffice", "description": "Free and open-source office suite.", "category": "Productivity Suite"},
        {"name": "Zoho Workplace", "description": "Affordable suite of business applications.", "category": "Productivity Suite"},
    ],
    "zoom": [
        {"name": "Google Meet", "description": "Video conferencing integrated with Google Workspace.", "category": "Video Conferencing"},
        {"name": "Microsoft Teams", "description": "Collaboration platform with video meetings, chat, and file sharing.", "category": "Video Conferencing"},
        {"name": "Skype", "description": "Free video and voice calls.", "category": "Video Conferencing"},
    ],
    "slack": [
        {"name": "Microsoft Teams", "description": "Offers chat, video meetings, and file storage, often bundled with Microsoft 365.", "category": "Team Communication"},
        {"name": "Discord", "description": "Popular for communities, also used by businesses for team chat.", "category": "Team Communication"},
        {"name": "Google Chat", "description": "Integrated with Google Workspace.", "category": "Team Communication"},
    ],
    "github pro": [ # For individual developer features
        {"name": "GitLab (Free/Premium tiers)", "description": "Offers a comprehensive DevOps platform, free tier is generous.", "category": "Version Control/DevOps"},
        {"name": "Bitbucket (Free/Standard tiers)", "description": "Git repository management, integrates well with Jira.", "category": "Version Control"},
    ],
    "linkedin premium": [
        {"name": "Free LinkedIn Account", "description": "Many core networking features are available for free.", "category": "Professional Networking"},
        {"name": "Company-specific career pages", "description": "Directly check career pages of companies you're interested in.", "category": "Job Search"},
        {"name": "Networking events/groups", "description": "Industry-specific groups and events for networking.", "category": "Professional Networking"},
    ],
    "evernote": [
        {"name": "Notion", "description": "All-in-one workspace for notes, tasks, wikis, and databases.", "category": "Note Taking/Productivity"},
        {"name": "Microsoft OneNote", "description": "Free-form note-taking app, part of Microsoft ecosystem.", "category": "Note Taking"},
        {"name": "Obsidian", "description": "Powerful knowledge base that works on local Markdown files.", "category": "Note Taking/Knowledge Management"},
        {"name": "Google Keep", "description": "Simple note-taking app, good for quick notes and lists.", "category": "Note Taking"},
    ],
    "audible": [
        {"name": "Libby (via local library)", "description": "Borrow audiobooks for free with your library card.", "category": "Audiobooks"},
        {"name": "Scribd", "description": "Subscription service with access to audiobooks, ebooks, magazines, and more.", "category": "Audiobooks/Ebooks"},
        {"name": "Kobo Audiobooks", "description": "Offers individual audiobook purchases and a subscription.", "category": "Audiobooks"},
        {"name": "Google Play Books", "description": "Purchase audiobooks individually.", "category": "Audiobooks"},
    ],
    # Add more services and their alternatives as needed
}

def get_gemini_alternatives(service_name: str) -> List[AlternativeDetail]:
    """
    Looks up a service in the hardcoded knowledge base and returns its alternatives.
    Note: Currently uses a hardcoded DB. "Gemini" in the name suggests future AI integration.
    """
    normalized_service_name = service_name.lower().strip()
    
    logger.info(f"Looking for alternatives for: '{normalized_service_name}' in hardcoded DB.")

    # Simple direct match first
    if normalized_service_name in ALTERNATIVES_DB:
        alternatives = ALTERNATIVES_DB[normalized_service_name]
        logger.info(f"Found {len(alternatives)} direct match alternatives for '{normalized_service_name}'.")
        return [AlternativeDetail(**alt) for alt in alternatives]

    # Try partial matching (e.g., "icloud storage" should match "icloud")
    for key in ALTERNATIVES_DB.keys():
        if key in normalized_service_name:
            alternatives = ALTERNATIVES_DB[key]
            logger.info(f"Found {len(alternatives)} partial match alternatives for '{normalized_service_name}' (matched with key '{key}').")
            return [AlternativeDetail(**alt) for alt in alternatives]
            
    logger.info(f"No alternatives found for '{normalized_service_name}' in hardcoded DB.")
    return []