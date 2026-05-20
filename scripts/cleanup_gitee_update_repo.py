import json
import os
import sys
import urllib.request
import urllib.error
import urllib.parse

OWNER = "su-zijie21"
REPO = "duoschedule-update"
BRANCH = "main"
KEEP_FILE = "update.json"
API_BASE = f"https://gitee.com/api/v5/repos/{OWNER}/{REPO}/contents"

TOKEN = os.environ.get("GITEE_TOKEN", "")
if not TOKEN:
    print("Error: GITEE_TOKEN environment variable is not set")
    sys.exit(1)


def api_get(url):
    sep = "&" if "?" in url else "?"
    full_url = f"{url}{sep}access_token={TOKEN}"
    req = urllib.request.Request(full_url)
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))


def api_delete(url, sha, message):
    payload = json.dumps({
        "access_token": TOKEN,
        "message": message,
        "sha": sha,
        "branch": BRANCH,
    })
    req = urllib.request.Request(url, data=payload.encode("utf-8"), method="DELETE")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))


def collect_all_files(path=""):
    url = f"{API_BASE}/{path}" if path else API_BASE
    items = api_get(url)
    files = []
    for item in items:
        if item["type"] == "dir":
            files.extend(collect_all_files(item["path"]))
        else:
            files.append(item)
    return files


def main():
    print(f"Listing files in {OWNER}/{REPO}...")
    all_files = collect_all_files()
    print(f"Found {len(all_files)} file(s)")

    to_delete = [f for f in all_files if f["name"] != KEEP_FILE]
    if not to_delete:
        print(f"Only {KEEP_FILE} exists, nothing to clean up")
        return

    print(f"Will delete {len(to_delete)} file(s) (keeping {KEEP_FILE}):")
    for f in to_delete:
        print(f"  - {f['path']}")

    success = 0
    failed = 0
    for f in to_delete:
        file_path = f["path"]
        sha = f["sha"]
        print(f"Deleting {file_path} (sha: {sha[:8]}...)...", end=" ", flush=True)
        try:
            delete_url = f"{API_BASE}/{urllib.parse.quote(file_path)}"
            api_delete(delete_url, sha, f"Remove {file_path}")
            print("OK")
            success += 1
        except urllib.error.HTTPError as e:
            body = e.read().decode("utf-8")
            print(f"FAILED (HTTP {e.code}: {body})")
            failed += 1
        except Exception as e:
            print(f"FAILED ({e})")
            failed += 1

    print(f"\nDone: {success} deleted, {failed} failed")
    if failed > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
