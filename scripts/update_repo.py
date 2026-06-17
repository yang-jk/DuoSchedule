import json
import base64
import os
import sys
import urllib.request
import urllib.error
import urllib.parse

VERSION_NAME = os.environ['VERSION_NAME']
VERSION_CODE = int(os.environ['VERSION_CODE'])
RELEASE_NOTES = os.environ.get('RELEASE_NOTES', '')
APK_SIZE = os.environ.get('APK_SIZE', '')
RELEASE_DATE = os.environ.get('RELEASE_DATE', '')
TAG_NAME = f"v{VERSION_NAME}"

GITHUB_OWNER = os.environ.get('GITHUB_OWNER', 'yang-jk')
GITEE_OWNER = os.environ.get('GITEE_OWNER', 'su-zijie21')
GITHUB_TOKEN = os.environ.get('UPDATE_REPO_TOKEN', '')
GITEE_TOKEN = os.environ.get('GITEE_TOKEN', '')
GITEE_RELEASE_SUCCESS = os.environ.get('GITEE_RELEASE_SUCCESS', 'true').lower() == 'true'

def fetch_file_sha(api_url, headers, max_retries=3):
    """获取文件 SHA，支持重试"""
    for attempt in range(1, max_retries + 1):
        try:
            req = urllib.request.Request(api_url)
            for key, value in headers.items():
                req.add_header(key, value)
            with urllib.request.urlopen(req, timeout=10) as resp:
                existing = json.loads(resp.read().decode('utf-8'))
                sha = existing.get('sha', '')
                return sha
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return ''  # 文件不存在，返回空 SHA
            print(f"  获取 SHA 失败 (尝试 {attempt}/{max_retries}): HTTP {e.code}")
        except Exception as e:
            print(f"  获取 SHA 失败 (尝试 {attempt}/{max_retries}): {e}")
        if attempt < max_retries:
            import time
            time.sleep(2)
    return ''

def update_github(api_url, token, download_url):
    update_data = {
        "latestVersion": VERSION_NAME,
        "latestVersionCode": VERSION_CODE,
        "minSupportedVersionCode": 1,
        "downloadUrl": download_url,
        "releaseNotes": RELEASE_NOTES,
        "forceUpdate": False,
        "size": APK_SIZE,
        "date": RELEASE_DATE
    }

    content_json = json.dumps(update_data, ensure_ascii=False, indent=2)
    print(f"[GitHub] Generated update.json:\n{content_json}")

    content_b64 = base64.b64encode(content_json.encode('utf-8')).decode('ascii')

    sha = fetch_file_sha(api_url, {'Authorization': f'token {token}'})
    if sha:
        print(f"[GitHub] Current file SHA: {sha}")
    else:
        print("[GitHub] No existing file found, will create new file")

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
            print(f"[GitHub] Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
            return True
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        # SHA 不匹配时重试：重新获取 SHA 再 PUT
        if e.code == 409 or e.code == 422:
            print(f"[GitHub] SHA conflict (HTTP {e.code}), retrying with fresh SHA...")
            new_sha = fetch_file_sha(api_url, {'Authorization': f'token {token}'})
            if new_sha:
                payload["sha"] = new_sha
                payload_json = json.dumps(payload)
                req2 = urllib.request.Request(api_url, data=payload_json.encode('utf-8'), method='PUT')
                req2.add_header('Authorization', f'token {token}')
                req2.add_header('Content-Type', 'application/json')
                try:
                    with urllib.request.urlopen(req2, timeout=30) as resp2:
                        result2 = json.loads(resp2.read().decode('utf-8'))
                        print(f"[GitHub] Retry success! Commit: {result2.get('commit', {}).get('sha', 'unknown')}")
                        return True
                except Exception as e2:
                    print(f"[GitHub] Retry failed: {e2}")
                    return False
        print(f"[GitHub] HTTP Error {e.code}: {body}")
        return False
    except Exception as e:
        print(f"[GitHub] Error: {e}")
        return False

def update_gitee(api_url, token, download_url):
    update_data = {
        "latestVersion": VERSION_NAME,
        "latestVersionCode": VERSION_CODE,
        "minSupportedVersionCode": 1,
        "downloadUrl": download_url,
        "releaseNotes": RELEASE_NOTES,
        "forceUpdate": False,
        "size": APK_SIZE,
        "date": RELEASE_DATE
    }

    content_json = json.dumps(update_data, ensure_ascii=False, indent=2)
    print(f"[Gitee] Generated update.json:\n{content_json}")

    content_b64 = base64.b64encode(content_json.encode('utf-8')).decode('ascii')

    sha = fetch_file_sha(f"{api_url}?access_token={token}", {'Content-Type': 'application/json'})
    if sha:
        print(f"[Gitee] Current file SHA: {sha}")
    else:
        print("[Gitee] No existing file found, will create new file")

    payload = {
        "access_token": token,
        "message": f"Update to {VERSION_NAME} ({VERSION_CODE})",
        "content": content_b64
    }
    if sha:
        payload["sha"] = sha

    payload_json = json.dumps(payload)

    req = urllib.request.Request(api_url, data=payload_json.encode('utf-8'), method='PUT')
    req.add_header('Content-Type', 'application/json')

    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            result = json.loads(resp.read().decode('utf-8'))
            print(f"[Gitee] Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
            return True
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        # SHA 缺失或不匹配时重试：重新获取 SHA 再 PUT
        if e.code == 400 or e.code == 409 or e.code == 422:
            print(f"[Gitee] SHA issue (HTTP {e.code}), retrying with fresh SHA...")
            new_sha = fetch_file_sha(f"{api_url}?access_token={token}", {'Content-Type': 'application/json'})
            if new_sha:
                payload["sha"] = new_sha
                payload_json = json.dumps(payload)
                req2 = urllib.request.Request(api_url, data=payload_json.encode('utf-8'), method='PUT')
                req2.add_header('Content-Type', 'application/json')
                try:
                    with urllib.request.urlopen(req2, timeout=30) as resp2:
                        result2 = json.loads(resp2.read().decode('utf-8'))
                        print(f"[Gitee] Retry success! Commit: {result2.get('commit', {}).get('sha', 'unknown')}")
                        return True
                except Exception as e2:
                    print(f"[Gitee] Retry failed: {e2}")
                    return False
        print(f"[Gitee] HTTP Error {e.code}: {body}")
        return False
    except Exception as e:
        print(f"[Gitee] Error: {e}")
        return False

any_success = False

if GITHUB_TOKEN:
    print("=" * 50)
    print("Updating GitHub...")
    github_api_url = f"https://api.github.com/repos/{GITHUB_OWNER}/duoschedule-update/contents/update.json"
    github_download_url = f"https://github.com/{GITHUB_OWNER}/DuoSchedule/releases/download/{TAG_NAME}/DuoSchedule-{VERSION_NAME}.apk"
    github_success = update_github(github_api_url, GITHUB_TOKEN, github_download_url)
    if github_success:
        any_success = True
        print("[GitHub] update.json updated successfully")
        print("[GitHub] Purging jsDelivr CDN cache...")
        try:
            purge_url = f"https://purge.jsdelivr.net/gh/{GITHUB_OWNER}/duoschedule-update@main/update.json"
            req = urllib.request.Request(purge_url)
            with urllib.request.urlopen(req, timeout=30) as resp:
                print(f"[GitHub] jsDelivr cache purged (status: {resp.status})")
        except Exception as e:
            print(f"[GitHub] jsDelivr purge failed (non-fatal): {e}")
    else:
        print("[GitHub] Failed to update, continuing...")
else:
    print("UPDATE_REPO_TOKEN not set, skipping GitHub update")

if GITEE_TOKEN:
    print("=" * 50)
    print("Updating Gitee...")
    gitee_api_url = f"https://gitee.com/api/v5/repos/{GITEE_OWNER}/duoschedule-update/contents/update.json"
    github_download_url = f"https://github.com/{GITHUB_OWNER}/DuoSchedule/releases/download/{TAG_NAME}/DuoSchedule-{VERSION_NAME}.apk"
    gitee_download_url = f"https://gitee.com/{GITEE_OWNER}/duoschedule/releases/download/{TAG_NAME}/DuoSchedule-{VERSION_NAME}.apk"

    if GITEE_RELEASE_SUCCESS:
        effective_download_url = gitee_download_url
        print(f"[Gitee] Gitee Release succeeded, using Gitee download URL")
    else:
        effective_download_url = github_download_url
        print(f"[Gitee] Gitee Release failed, falling back to GitHub download URL")

    gitee_success = update_gitee(gitee_api_url, GITEE_TOKEN, effective_download_url)

    if gitee_success:
        any_success = True
        print("[Gitee] Purging cache...")
        try:
            purge_url = f"https://gitee.com/{GITEE_OWNER}/duoschedule-update/raw/main/update.json"
            req = urllib.request.Request(purge_url)
            with urllib.request.urlopen(req, timeout=10) as resp:
                print(f"[Gitee] Cache purged (status: {resp.status})")
        except Exception as e:
            print(f"[Gitee] Purge failed (non-fatal): {e}")
    else:
        print("[Gitee] Failed to update, continuing...")
else:
    print("GITEE_TOKEN is not set, skipping Gitee update")

if not any_success:
    print("All platforms failed to update!")
    sys.exit(1)

print("=" * 50)
print("Update process completed!")
