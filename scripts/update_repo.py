import json
import base64
import os
import sys
import subprocess

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

def curl_request(url, method='GET', headers=None, data=None, max_retries=3, timeout=30):
    """使用 curl 发起请求，支持重试"""
    for attempt in range(1, max_retries + 1):
        try:
            cmd = ['curl', '-sS', '-X', method, '--connect-timeout', '10', '--max-time', str(timeout)]
            if headers:
                for key, value in headers.items():
                    cmd += ['-H', f'{key}: {value}']
            if data:
                cmd += ['-d', data]
            cmd.append(url)

            result = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout + 10)
            if result.returncode == 0:
                return result.stdout
            else:
                print(f"  curl 失败 (尝试 {attempt}/{max_retries}): {result.stderr.strip()}")
        except subprocess.TimeoutExpired:
            print(f"  curl 超时 (尝试 {attempt}/{max_retries})")
        except Exception as e:
            print(f"  curl 异常 (尝试 {attempt}/{max_retries}): {e}")
        if attempt < max_retries:
            import time
            time.sleep(2)
    return None

def fetch_file_sha(api_url, headers, max_retries=3):
    """获取文件 SHA，支持重试"""
    response = curl_request(api_url, method='GET', headers=headers, max_retries=max_retries)
    if response:
        try:
            existing = json.loads(response)
            sha = existing.get('sha', '')
            return sha
        except json.JSONDecodeError:
            # 404 返回的不是 JSON，curl 会返回空或错误页面
            pass
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

    headers = {'Authorization': f'token {token}', 'Content-Type': 'application/json'}
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

    response = curl_request(api_url, method='PUT', headers=headers, data=payload_json)
    if response:
        try:
            result = json.loads(response)
            # 检查是否有 API 错误
            if 'message' in result and 'sha' not in result.get('content', {}):
                error_msg = result.get('message', 'Unknown error')
                # SHA 冲突时重试
                if 'conflict' in error_msg.lower() or '422' in error_msg or '409' in error_msg:
                    print(f"[GitHub] SHA conflict, retrying with fresh SHA...")
                    new_sha = fetch_file_sha(api_url, {'Authorization': f'token {token}'})
                    if new_sha:
                        payload["sha"] = new_sha
                        payload_json = json.dumps(payload)
                        response2 = curl_request(api_url, method='PUT', headers=headers, data=payload_json)
                        if response2:
                            result2 = json.loads(response2)
                            if 'content' in result2 or 'commit' in result2:
                                print(f"[GitHub] Retry success! Commit: {result2.get('commit', {}).get('sha', 'unknown')}")
                                return True
                            print(f"[GitHub] Retry failed: {result2.get('message', 'unknown')}")
                            return False
                print(f"[GitHub] API Error: {error_msg}")
                return False
            print(f"[GitHub] Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
            return True
        except json.JSONDecodeError:
            print(f"[GitHub] Invalid JSON response: {response[:200]}")
            return False
    print("[GitHub] Request failed after retries")
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

    headers = {'Content-Type': 'application/json'}
    response = curl_request(api_url, method='PUT', headers=headers, data=payload_json)
    if response:
        try:
            result = json.loads(response)
            # 检查是否有 API 错误
            if 'message' in result and 'content' not in result:
                error_msg = result.get('message', 'Unknown error')
                # SHA 问题时重试
                if sha and ('sha' in error_msg.lower() or 'conflict' in error_msg.lower() or '400' in error_msg or '409' in error_msg or '422' in error_msg):
                    print(f"[Gitee] SHA issue, retrying with fresh SHA...")
                    new_sha = fetch_file_sha(f"{api_url}?access_token={token}", {'Content-Type': 'application/json'})
                    if new_sha:
                        payload["sha"] = new_sha
                        payload_json = json.dumps(payload)
                        response2 = curl_request(api_url, method='PUT', headers=headers, data=payload_json)
                        if response2:
                            result2 = json.loads(response2)
                            if 'content' in result2 or 'commit' in result2:
                                print(f"[Gitee] Retry success! Commit: {result2.get('commit', {}).get('sha', 'unknown')}")
                                return True
                            print(f"[Gitee] Retry failed: {result2.get('message', 'unknown')}")
                            return False
                print(f"[Gitee] API Error: {error_msg}")
                return False
            print(f"[Gitee] Success! Commit: {result.get('commit', {}).get('sha', 'unknown')}")
            return True
        except json.JSONDecodeError:
            print(f"[Gitee] Invalid JSON response: {response[:200]}")
            return False
    print("[Gitee] Request failed after retries")
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
        response = curl_request(f"https://purge.jsdelivr.net/gh/{GITHUB_OWNER}/duoschedule-update@main/update.json")
        if response:
            print(f"[GitHub] jsDelivr cache purged")
        else:
            print("[GitHub] jsDelivr purge failed (non-fatal)")
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
        response = curl_request(f"https://gitee.com/{GITEE_OWNER}/duoschedule-update/raw/main/update.json", timeout=10)
        if response:
            print(f"[Gitee] Cache purged")
        else:
            print("[Gitee] Purge failed (non-fatal)")
    else:
        print("[Gitee] Failed to update, continuing...")
else:
    print("GITEE_TOKEN is not set, skipping Gitee update")

if not any_success:
    print("All platforms failed to update!")
    sys.exit(1)

print("=" * 50)
print("Update process completed!")
