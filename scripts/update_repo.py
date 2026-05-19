import json
import base64
import os
import urllib.request
import sys

TOKEN = os.environ['UPDATE_REPO_TOKEN']
REPO_OWNER = os.environ.get('REPO_OWNER', 'yang-jk')
REPO_NAME = os.environ.get('REPO_NAME', 'duoschedule-update')
VERSION_NAME = os.environ['VERSION_NAME']
VERSION_CODE = int(os.environ['VERSION_CODE'])
RELEASE_NOTES = os.environ.get('RELEASE_NOTES', '')
TAG_NAME = f"v{VERSION_NAME}"
DOWNLOAD_URL = f"https://github.com/{REPO_OWNER}/DuoSchedule/releases/download/{TAG_NAME}/DuoSchedule-{VERSION_NAME}.apk"
API_BASE = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/contents/update.json"

update_data = {
    "latestVersion": VERSION_NAME,
    "latestVersionCode": VERSION_CODE,
    "minSupportedVersionCode": 1,
    "downloadUrl": DOWNLOAD_URL,
    "releaseNotes": RELEASE_NOTES,
    "forceUpdate": False
}

content_json = json.dumps(update_data, ensure_ascii=False, indent=2)
print(f"Generated update.json:\n{content_json}")

content_b64 = base64.b64encode(content_json.encode('utf-8')).decode('ascii')

req = urllib.request.Request(API_BASE)
req.add_header('Authorization', f'token {TOKEN}')
req.add_header('Content-Type', 'application/json')

try:
    with urllib.request.urlopen(req) as resp:
        existing = json.loads(resp.read().decode('utf-8'))
        sha = existing.get('sha', '')
        print(f"Current file SHA: {sha}")
except Exception as e:
    print(f"Could not fetch existing file: {e}")
    sha = ""

payload = {
    "message": f"Update to {VERSION_NAME} ({VERSION_CODE})",
    "content": content_b64
}
if sha:
    payload["sha"] = sha

payload_json = json.dumps(payload)
print(f"API payload size: {len(payload_json)} bytes")

req = urllib.request.Request(API_BASE, data=payload_json.encode('utf-8'), method='PUT')
req.add_header('Authorization', f'token {TOKEN}')
req.add_header('Content-Type', 'application/json')

try:
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read().decode('utf-8'))
        print(f"Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
except urllib.error.HTTPError as e:
    body = e.read().decode('utf-8')
    print(f"HTTP Error {e.code}: {body}")
    sys.exit(1)
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)

purge_url = f"https://purge.jsdelivr.net/gh/{REPO_OWNER}/{REPO_NAME}@main/update.json"
print(f"Purging jsdelivr cache: {purge_url}")
try:
    req = urllib.request.Request(purge_url)
    with urllib.request.urlopen(req, timeout=30) as resp:
        print(f"Purge response: {resp.status}")
except Exception as e:
    print(f"Purge failed (non-fatal): {e}")
