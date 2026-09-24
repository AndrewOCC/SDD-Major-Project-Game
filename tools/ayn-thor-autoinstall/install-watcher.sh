#!/data/data/com.termux/files/usr/bin/bash
#
# Listens on an ntfy.sh topic for "new build" pings from CI and installs the
# latest MajorProject-debug artifact on this device via `pm install -r`
# (needs root — that's what makes this silent, no "unknown sources" prompt).
#
# One-time setup: see README.md in this directory.
#
# Usage:
#   ./install-watcher.sh          # listen forever, install on each ping
#   ./install-watcher.sh --once   # fetch + install the latest build right now, then exit

set -euo pipefail

# --- Config -----------------------------------------------------------------
REPO="AndrewOCC/SDD-Major-Project-Game"
WORKFLOW_FILE="ci.yml"
BRANCH="cursor/startup-crash-fix-a6be"   # branch to track; change as needed
NTFY_TOPIC_FILE="$HOME/.config/ayn-thor-autoinstall/ntfy-topic"
TOKEN_FILE="$HOME/.config/ayn-thor-autoinstall/github-token"
STATE_DIR="$HOME/.local/state/ayn-thor-autoinstall"
LOCK_FILE="$STATE_DIR/install.lock"
LAST_RUN_FILE="$STATE_DIR/last-installed-run-id"
LOG_FILE="$STATE_DIR/watcher.log"
WORK_DIR="$STATE_DIR/work"
# ------------------------------------------------------------------------------

mkdir -p "$STATE_DIR" "$WORK_DIR"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

require_file() {
  if [ ! -s "$1" ]; then
    log "Missing $1 — see README.md for setup."
    exit 1
  fi
}

require_file "$NTFY_TOPIC_FILE"
require_file "$TOKEN_FILE"
NTFY_TOPIC="$(cat "$NTFY_TOPIC_FILE")"
GITHUB_TOKEN="$(cat "$TOKEN_FILE")"

api() {
  curl -sf -H "Authorization: Bearer $GITHUB_TOKEN" \
       -H "Accept: application/vnd.github+json" \
       "$@"
}

install_latest() {
  # Prevent overlapping installs if two pings land close together.
  exec 9>"$LOCK_FILE"
  if ! flock -n 9; then
    log "Install already in progress, skipping this trigger."
    return
  fi

  log "Checking latest successful '$WORKFLOW_FILE' run on $BRANCH..."
  run_id="$(api "https://api.github.com/repos/$REPO/actions/workflows/$WORKFLOW_FILE/runs?branch=$BRANCH&status=success&per_page=1" \
    | jq -r '.workflow_runs[0].id // empty')"

  if [ -z "$run_id" ]; then
    log "No successful run found yet."
    return
  fi

  if [ -f "$LAST_RUN_FILE" ] && [ "$(cat "$LAST_RUN_FILE")" = "$run_id" ]; then
    log "Run $run_id already installed, skipping."
    return
  fi

  log "Fetching artifact for run $run_id..."
  artifact_url="$(api "https://api.github.com/repos/$REPO/actions/runs/$run_id/artifacts" \
    | jq -r '.artifacts[] | select(.name == "MajorProject-debug") | .archive_download_url' | head -n1)"

  if [ -z "$artifact_url" ]; then
    log "No MajorProject-debug artifact on run $run_id."
    return
  fi

  rm -rf "$WORK_DIR"/*
  api -L -o "$WORK_DIR/apk.zip" "$artifact_url"
  unzip -oq "$WORK_DIR/apk.zip" -d "$WORK_DIR/extracted"
  apk_path="$(find "$WORK_DIR/extracted" -name '*.apk' | head -n1)"

  if [ -z "$apk_path" ]; then
    log "Downloaded artifact but no .apk inside it."
    return
  fi

  log "Installing $apk_path (run $run_id)..."
  if su -c "pm install -r --user 0 '$apk_path'" 2>&1 | tee -a "$LOG_FILE"; then
    echo "$run_id" > "$LAST_RUN_FILE"
    log "Installed run $run_id."
    command -v termux-notification >/dev/null 2>&1 && \
      termux-notification --title "MajorProject updated" \
        --content "Installed build from $BRANCH" || true
  else
    log "pm install failed for run $run_id."
    command -v termux-notification >/dev/null 2>&1 && \
      termux-notification --title "MajorProject update failed" \
        --content "Check $LOG_FILE" || true
  fi
}

if [ "${1:-}" = "--once" ]; then
  install_latest
  exit 0
fi

log "Listening on ntfy.sh/$NTFY_TOPIC for build notifications..."
while true; do
  curl -s -N "https://ntfy.sh/$NTFY_TOPIC/sse" | while IFS= read -r line; do
    case "$line" in
      data:\ *) ;;
      *) continue ;;
    esac
    payload="${line#data: }"
    event="$(echo "$payload" | jq -r '.event // empty' 2>/dev/null || true)"
    if [ "$event" = "message" ]; then
      log "Build ping received: $(echo "$payload" | jq -r '.message // empty')"
      install_latest
    fi
  done
  log "SSE connection dropped, reconnecting in 5s..."
  sleep 5
done
