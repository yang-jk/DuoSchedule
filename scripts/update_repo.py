import json
import base64
import os
import urllib.request
import sys

VERSION_NAME = os.environ['VERSION_NAME']
VERSION_CODE = int(os.environ['VERSION_CODE'])
RELEASE_NOTES = os.environ.get('RELEASE_NOTES', '')
TAG_NAME = f"v{VERSION_NAME}"

REPO_OWNER = os.environ.get('REPO_OWNER', 'yang-jk')
GITEE_TOKEN = os.environ.get('GITEE_TOKEN', '')
USE_GITEE = os.environ.get('USE_GITEE', 'false').lower() == 'true'

def update_single_platform(api_url, token, platform_name, download_url):
    update_data = {
        "latestVersion": VERSION_NAME,
        "latestVersionCode": VERSION_CODE,
        "minSupportedVersionCode": 1,
        "downloadUrl": download_url,
        "releaseNotes": RELEASE_NOTES,
        "forceUpdate": False
    }

    content_json = json.dumps(update_data, ensure_ascii=False, indent=2)
    print(f"[{platform_name}] Generated update.json:\n{content_json}")

    content_b64 = base64.b64encode(content_json.encode('utf-8')).decode('ascii')

    try:
        req = urllib.request.Request(api_url)
        req.add_header('Authorization', f'token {token}')
        req.add_header('Content-Type', 'application/json')
        with urllib.request.urlopen(req, timeout=10) as resp:
            existing = json.loads(resp.read().decode('utf-8'))
            sha = existing.get('sha', '')
            print(f"[{platform_name}] Current file SHA: {sha}")
    except Exception as e:
        print(f"[{platform_name}] Could not fetch existing file: {e}")
        sha = ""

    payload = {
        "message": f"Update to {VERSION_NAME} ({VERSION_CODE})",
        "content": content_b64
    }
    if sha:
        payload["sha"] = sha

    payload_json = json.dumps(payload)

    req = urllib.request.Request(api_url, data=payload_json.encode('utf-8'), method='PUT')
    req.add_header('Authorization', f'token {token}')
    req.add_header('Content-Type', 'application/json')

    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            result = json.loads(resp.read().decode('utf-8'))
            print(f"[{platform_name}] Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
            return True
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        print(f"[{platform_name}] HTTP Error {e.code}: {body}")
        return False
    except Exception as e:
        print(f"[{platform_name}] Error: {e}")
        return False

if USE_GITEE and GITEE_TOKEN:
    print("=" * 50)
    print("Updating Gitee...")
    gitee_api_url = f"https://gitee.com/api/v5/repos/{REPO_OWNER}/duoschedule-update/contents/update.json"
    gitee_download_url = f"https://gitee.com/{REPO_OWNER}/DuoSchedule/releases/download/{TAG_NAME}/DuoSchedule-{VERSION_NAME}.apk"
    gitee_success = update_single_platform(gitee_api_url, GITEE_TOKEN, "Gitee", gitee_download_url)
    
    if gitee_success:
        print(f"[Gitee] Purging cache...")
        try:
            purge_url = f"https://gitee.com/{REPO_OWNER}/duoschedule-update/raw/main/update.json"
            req = urllib.request.Request(purge_url)
            with urllib.request.urlopen(req, timeout=10) as resp:
                print(f"[Gitee] Cache purged (status: {resp.status})")
        except Exception as e:
            print(f"[Gitee] Purge failed (non-fatal): {e}")
    else:
        print("[Gitee] Failed to update, continuing...")
        sys.exit(1)
else:
    print("USE_GITEE is not set or GITEE_TOKEN is missing, skipping Gitee update")
