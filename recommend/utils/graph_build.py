import pymysql
import datetime
from collections import defaultdict

SqlURL = "10.126.59.25"
SqlPort = 3306
Database = "pt_database_test"
SqlUsername = "root"
SqlPassword = "123456"


def fetch_user_seed_data():
    conn = pymysql.connect(
        host=SqlURL,
        port=SqlPort,
        user=SqlUsername,
        password=SqlPassword,
        database=Database,
        charset="utf8mb4"
    )
    cursor = conn.cursor()
    cursor.execute("SELECT user_id, seed_id, download_start FROM SeedDownload")
    download_rows = cursor.fetchall()
    cursor.execute("SELECT user_id, seed_id, created_at FROM UserFavorite")
    favorite_rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return download_rows, favorite_rows


def process_records(download_rows, favorite_rows):
    records = []
    user_set = set()
    seed_set = set()
    for row in download_rows:
        user_id, seed_id, created_at = row
        user_set.add(user_id)
        seed_set.add(seed_id)
        if isinstance(created_at, datetime.datetime):
            ts = int(created_at.timestamp())
        else:
            ts = 0
        records.append((user_id, seed_id, ts))
    for row in favorite_rows:
        user_id, seed_id, created_at = row
        user_set.add(user_id)
        seed_set.add(seed_id)
        if isinstance(created_at, datetime.datetime):
            ts = int(created_at.timestamp())
        else:
            ts = 0
        records.append((user_id, seed_id, ts))
    return records, user_set, seed_set


def build_id_maps(user_set, seed_set):
    user2idx = {uid: idx for idx, uid in enumerate(sorted(user_set))}
    seed2idx = {sid: idx for idx, sid in enumerate(sorted(seed_set))}
    return user2idx, seed2idx


def group_and_write(records, user2idx, seed2idx, output_path="./user_seed_graph.txt"):
    user_items = defaultdict(list)
    user_times = defaultdict(list)
    for user_id, seed_id, ts in records:
        uid = user2idx[user_id]
        sid = seed2idx[seed_id]
        user_items[uid].append(sid)
        user_times[uid].append(ts)
    with open(output_path, "w", encoding="utf-8") as f:
        for uid in sorted(user_items.keys()):
            items = " ".join(str(item) for item in user_items[uid])
            times = " ".join(str(t) for t in user_times[uid])
            f.write(f"{uid}\t{items}\t{times}\n")


def build_user_seed_graph(return_mapping=False):
    download_rows, favorite_rows = fetch_user_seed_data()
    records, user_set, seed_set = process_records(download_rows, favorite_rows)
    user2idx, seed2idx = build_id_maps(user_set, seed_set)
    group_and_write(records, user2idx, seed2idx)
    if return_mapping:
        return user2idx, seed2idx