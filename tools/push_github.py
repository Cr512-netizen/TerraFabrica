"""Пушит локальные изменения terrafabrica в GitHub Cr512-netizen/TerraFabrica (main)
через Git Data API, без локального git. Сравнивает blob-SHA с удалённым деревом
и заливает только изменившиеся файлы.

Токен: ~/.dsh/.credentials.yaml -> refs.GITHUB_TOKEN.

    python push_github.py --dry
    python push_github.py -m "сообщение"
"""
import argparse
import base64
import hashlib
import json
import os
import urllib.request
import urllib.error

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OWNER, REPO, BRANCH = "Cr512-netizen", "TerraFabrica", "main"
API = "https://api.github.com"


def load_token():
    cred = os.path.expanduser("~/.dsh/.credentials.yaml")
    with open(cred, encoding="utf-8") as f:
        text = f.read()
    import re
    m = re.search(r"GITHUB_TOKEN:\s*(ghp_[A-Za-z0-9]+)", text)
    if m:
        return m.group(1)
    for line in text.splitlines():
        s = line.strip()
        if s.startswith("GITHUB_TOKEN:"):
            return s.split(":", 1)[1].strip().strip('"').strip("'")
    raise SystemExit("GITHUB_TOKEN не найден в " + cred)


def api(token, method, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(API + path, data=data, method=method)
    req.add_header("Authorization", "Bearer " + token)
    req.add_header("Accept", "application/vnd.github+json")
    req.add_header("X-GitHub-Api-Version", "2022-11-28")
    req.add_header("User-Agent", "terrafabrica-push")
    if data:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read()
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        raise SystemExit(f"GitHub API {e.code} {method} {path}: {e.read().decode('utf-8','replace')[:400]}")


def git_blob_sha(content: bytes) -> str:
    h = hashlib.sha1()
    h.update(b"blob %d\0" % len(content))
    h.update(content)
    return h.hexdigest()


def collect_files():
    skip = {"build", "run", ".gradle", "gradle", "__pycache__", ".git", ".idea"}
    out = []
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in skip]
        for name in filenames:
            if name.endswith((".class", ".log")):
                continue
            full = os.path.join(dirpath, name)
            out.append((os.path.relpath(full, ROOT).replace(os.sep, "/"), full))
    return sorted(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("-m", "--message", default="auto: sync terrafabrica workspace")
    ap.add_argument("--dry", action="store_true")
    args = ap.parse_args()
    token = load_token()

    ref = api(token, "GET", f"/repos/{OWNER}/{REPO}/git/ref/heads/{BRANCH}")
    base_commit = ref["object"]["sha"]
    base_tree = api(token, "GET", f"/repos/{OWNER}/{REPO}/git/commits/{base_commit}")["tree"]["sha"]
    remote = {t["path"]: t["sha"] for t in
              api(token, "GET", f"/repos/{OWNER}/{REPO}/git/trees/{base_tree}?recursive=1")["tree"]
              if t["type"] == "blob"}
    print(f"база: {base_commit[:10]}, файлов на гитхабе: {len(remote)}")

    tree_items, n_changed = [], 0
    for rel, full in collect_files():
        with open(full, "rb") as f:
            content = f.read()
        sha = git_blob_sha(content)
        if remote.get(rel) == sha:
            continue
        n_changed += 1
        blob = api(token, "POST", f"/repos/{OWNER}/{REPO}/git/blobs",
                   {"content": base64.b64encode(content).decode(), "encoding": "base64"})
        assert blob["sha"] == sha
        tree_items.append({"path": rel, "mode": "100644", "type": "blob", "sha": sha})

    # файлы, удалённые локально
    local_paths = {rel for rel, _ in collect_files()}
    deleted = [p for p in remote if p not in local_paths]
    for p in deleted:
        tree_items.append({"path": p, "mode": "100644", "type": "blob", "sha": None})
        n_changed += 1

    print(f"изменено/удалено файлов: {n_changed}")
    if args.dry or not tree_items:
        if not tree_items:
            print("нечего пушить")
        return

    new_tree = api(token, "POST", f"/repos/{OWNER}/{REPO}/git/trees",
                   {"base_tree": base_tree, "tree": tree_items})
    commit = api(token, "POST", f"/repos/{OWNER}/{REPO}/git/commits",
                 {"message": args.message, "tree": new_tree["sha"], "parents": [base_commit]})
    api(token, "PATCH", f"/repos/{OWNER}/{REPO}/git/refs/heads/{BRANCH}",
        {"sha": commit["sha"], "force": False})
    print(f"запушено: {commit['sha'][:10]} — {args.message}")


if __name__ == "__main__":
    main()
