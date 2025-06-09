#!/usr/bin/env bash
#
# batch_test_torrents.sh — 批量检测 .torrent 文件是否能正确启动下载
#
# 依赖：curl, jq
#
# 用法：./batch_test_torrents.sh /path/to/torrent_dir

set -euo pipefail

QB_URL="http://127.0.0.1:8080"
QB_USER="admin"
QB_PASS="9H6k8VpcM"
COOKIE_JAR="$(mktemp)"
TORRENT_DIR="${1:-.}"
TIMEOUT_SECS=60     # 最长等多久才判超时
POLL_INTERVAL=2     # 每次轮询间隔
FAILED_DIR="${TORRENT_DIR}/failed_torrents"  # 失败种子存放目录


# 登录
_login() {
  curl -s -c "$COOKIE_JAR" \
       -d "username=$QB_USER&password=$QB_PASS" \
       "$QB_URL/api/v2/auth/login" \
    | grep -q "Ok." || {
      echo "❌ 登录失败" >&2
      exit 1
    }
}

# 登出
_logout() {
  curl -s -b "$COOKIE_JAR" "$QB_URL/api/v2/auth/logout" >/dev/null
  rm -f "$COOKIE_JAR"
}

# 添加 torrent，返回 infoHash
# $1 = .torrent 文件路径
_add_torrent() {
  local file="$1"
  # 丢弃 “Ok.”，只留下后续 info-hash
  curl -s -b "$COOKIE_JAR" -X POST \
       -F "torrents=@${file}" \
       "$QB_URL/api/v2/torrents/add" >/dev/null

  # 等待 qBittorrent 收到任务
  sleep 3

  # 取最新添加的那个 torrent（按 added_on 降序，limit=1）
  local info
  info=$(curl -s -b "$COOKIE_JAR" \
           "$QB_URL/api/v2/torrents/info?limit=1&sort=added_on&reverse=true")

  # 检查是否获取到有效的 JSON 响应
  if ! echo "$info" | jq empty 2>/dev/null; then
    echo "ERROR: Invalid JSON response from qBittorrent API" >&2
    echo "Response: $info" >&2
    return 1
  fi

  # 检查是否有 torrent 记录
  local count
  count=$(echo "$info" | jq 'length')
  if [[ "$count" == "0" ]]; then
    echo "ERROR: No torrents found after adding" >&2
    return 1
  fi

  # 只输出 hash
  echo "$info" | jq -r '.[0].hash'
}

# 删除 torrent，同时删除已下载的文件
# $1 = infoHash
_delete_torrent() {
  local hash="$1"
  curl -s -b "$COOKIE_JAR" \
       -G --data-urlencode "hashes=${hash}" \
       "$QB_URL/api/v2/torrents/delete?deleteFiles=true" >/dev/null
}

# 等待并检测状态
# $1 = infoHash
_wait_for_progress() {
  local hash="$1"
  local waited=0

  while (( waited < TIMEOUT_SECS )); do
    # 获取特定种子信息
    local info
    info=$(curl -s -b "$COOKIE_JAR" \
               -G --data-urlencode "hashes=${hash}" \
               "$QB_URL/api/v2/torrents/info")

    # 检查 JSON 响应
    if ! echo "$info" | jq empty 2>/dev/null; then
      echo "⚠️  ${hash}: Invalid API response, retrying..."
      sleep $POLL_INTERVAL
      waited=$(( waited + POLL_INTERVAL ))
      continue
    fi

    # 检查是否返回了数据
    local count
    count=$(echo "$info" | jq 'length')
    if [[ "$count" == "0" ]]; then
      echo "⚠️  ${hash}: Torrent not found, retrying..."
      sleep $POLL_INTERVAL
      waited=$(( waited + POLL_INTERVAL ))
      continue
    fi

    local state progress
    state=$(echo "$info" | jq -r '.[0].state // "unknown"')
    progress=$(echo "$info" | jq -r '.[0].progress // 0')

    # 成功开始下载（progress > 0）
    if awk "BEGIN {exit !($progress > 0)}"; then
      local progress_percent
      progress_percent=$(awk "BEGIN {printf \"%.2f\", $progress * 100}")
      echo "✅ ${hash}: started downloading (progress=${progress_percent}%)"
      return 0
    fi

    # 出错状态
    if [[ "$state" == "error" ]]; then
      echo "❌ ${hash}: entered error state"
      return 1
    fi

    sleep $POLL_INTERVAL
    waited=$(( waited + POLL_INTERVAL ))
  done

  echo "⚠️  ${hash}: no progress after ${TIMEOUT_SECS}s timeout"
  return 2
}

main() {
  if [[ ! -d "$TORRENT_DIR" ]]; then
    echo "Usage: $0 /path/to/torrent_dir" >&2
    exit 1
  fi

  # 创建失败种子目录（如果不存在）
  mkdir -p "$FAILED_DIR"
  
  # 清空失败种子目录
  if [[ -d "$FAILED_DIR" ]]; then
    rm -f "$FAILED_DIR"/*.torrent 2>/dev/null || true
    echo "已清空失败种子目录：$FAILED_DIR"
  fi

  _login
  echo "开始批量测试目录：$TORRENT_DIR"
  echo "失败的种子将被复制到：$FAILED_DIR"

  for file in "$TORRENT_DIR"/*.torrent; do
    [[ -e "$file" ]] || { echo "目录中没有 .torrent 文件"; break; }

    echo "---- 测试 $file ----"
    hash=$(_add_torrent "$file")
    echo "添加成功，infoHash=$hash"

    if _wait_for_progress "$hash"; then
      echo ">>> $file 下载检测通过"
    else
      echo ">>> $file 下载检测失败"
      cp "$file" "$FAILED_DIR/"
      echo "已将失败种子复制到：$FAILED_DIR/$(basename "$file")"
    fi

    _delete_torrent "$hash"
    echo
  done

  _logout
  echo "全部完成。失败的种子文件已保存在：$FAILED_DIR"
}

main "$@"
