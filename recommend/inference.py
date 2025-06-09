import sys
sys.path.append('./')

import torch
import pymysql
from flask_cors import CORS
from flask import Flask, request, jsonify
from model.LightGCN import LightGCN
from utils.parse_args import args
from utils.data_loader import EdgeListData
from utils.graph_build import build_user_seed_graph

app = Flask(__name__)
CORS(app)

args.device = 'cuda:7'
args.data_path = './user_seed_graph.txt'
args.pre_model_path = './model/LightGCN_pretrained.pt'

# 数据库连接配置
DB_CONFIG = {
    'host': '10.126.59.25',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'pt_database_test',
    'charset': 'utf8mb4'
}

TOPK = 2  # 默认推荐数量

def user_cold_start(topk=TOPK):
    """
    冷启动：直接返回热度最高的topk个种子详细信息
    """
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # 查询热度最高的topk个种子
    cursor.execute(
        f"SELECT seed_id, owner_user_id, tags, title, size, popularity FROM Seed ORDER BY popularity DESC LIMIT %s",
        (topk,)
    )
    seed_rows = cursor.fetchall()
    seed_ids = [row[0] for row in seed_rows]
    seed_map = {row[0]: row for row in seed_rows}

    # 查询用户信息
    owner_ids = list(set(row[1] for row in seed_rows))
    if owner_ids:
        format_strings_user = ','.join(['%s'] * len(owner_ids))
        cursor.execute(
            f"SELECT user_id, username FROM User WHERE user_id IN ({format_strings_user})",
            tuple(owner_ids)
        )
        user_rows = cursor.fetchall()
        user_map = {row[0]: row[1] for row in user_rows}
    else:
        user_map = {}

    # 查询促销信息
    if seed_ids:
        format_strings = ','.join(['%s'] * len(seed_ids))
        cursor.execute(
            f"SELECT seed_id, discount FROM SeedPromotion WHERE seed_id IN ({format_strings})",
            tuple(seed_ids)
        )
        promo_rows = cursor.fetchall()
        promo_map = {row[0]: row[1] for row in promo_rows}
    else:
        promo_map = {}

    cursor.close()
    conn.close()

    seed_list = []
    for seed_id in seed_ids:
        row = seed_map.get(seed_id)
        if not row:
            continue
        owner_user_id = row[1]
        seed_list.append({
            'seed_id': seed_id,
            'tags': row[2],
            'title': row[3],
            'size': row[4],
            'username': user_map.get(owner_user_id, ""),
            'popularity': row[5],
            'discount': promo_map.get(seed_id, 1)
        })
    return seed_list

def run_inference(user_id, topk=TOPK):
    """
    输入: user_id, topk
    输出: 推荐的topk个种子ID列表（原始种子ID字符串）
    """
    user2idx, seed2idx = build_user_seed_graph(return_mapping=True)
    idx2seed = {v: k for k, v in seed2idx.items()}

    if user_id not in user2idx:
        # 冷启动
        return user_cold_start(topk)

    user_idx = user2idx[user_id]

    dataset = EdgeListData(args.data_path, args.data_path)
    pretrained_dict = torch.load(args.pre_model_path, map_location=args.device, weights_only=True)
    pretrained_dict['user_embedding'] = pretrained_dict['user_embedding'][:dataset.num_users]
    pretrained_dict['item_embedding'] = pretrained_dict['item_embedding'][:dataset.num_items]

    model = LightGCN(dataset, phase='vanilla').to(args.device)
    model.load_state_dict(pretrained_dict, strict=False)
    model.eval()

    with torch.no_grad():
        user_emb, item_emb = model.generate()
        user_vec = user_emb[user_idx].unsqueeze(0)
        scores = model.rating(user_vec, item_emb).squeeze(0)
        topk_indices = torch.topk(scores, topk).indices.cpu().numpy()
        topk_seed_ids = [idx2seed[idx] for idx in topk_indices]

    return topk_seed_ids

def seed_info(topk_seed_ids):
    """
    输入: topk_seed_ids（种子ID字符串列表）
    输出: 推荐种子的详细信息列表，每个元素为dict
    """
    if not topk_seed_ids:
        return []

    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # 查询种子基本信息
    format_strings = ','.join(['%s'] * len(topk_seed_ids))
    cursor.execute(
        f"SELECT seed_id, owner_user_id, tags, title, size, popularity FROM Seed WHERE seed_id IN ({format_strings})",
        tuple(topk_seed_ids)
    )
    seed_rows = cursor.fetchall()
    seed_map = {row[0]: row for row in seed_rows}

    # 查询用户信息
    owner_ids = list(set(row[1] for row in seed_rows))
    if owner_ids:
        format_strings_user = ','.join(['%s'] * len(owner_ids))
        cursor.execute(
            f"SELECT user_id, username FROM User WHERE user_id IN ({format_strings_user})",
            tuple(owner_ids)
        )
        user_rows = cursor.fetchall()
        user_map = {row[0]: row[1] for row in user_rows}
    else:
        user_map = {}

    # 查询促销信息
    cursor.execute(
        f"SELECT seed_id, discount FROM SeedPromotion WHERE seed_id IN ({format_strings})",
        tuple(topk_seed_ids)
    )
    promo_rows = cursor.fetchall()
    promo_map = {row[0]: row[1] for row in promo_rows}

    cursor.close()
    conn.close()

    seed_list = []
    for seed_id in topk_seed_ids:
        row = seed_map.get(seed_id)
        if not row:
            continue
        owner_user_id = row[1]
        seed_list.append({
            'seed_id': seed_id,
            'tags': row[2],
            'title': row[3],
            'size': row[4],
            'username': user_map.get(owner_user_id, ""),
            'popularity': row[5],
            'discount': promo_map.get(seed_id, 1)
        })
    return seed_list

@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.get_json()
    user_id = data.get('user_id')
    try:
        result = run_inference(user_id)
        # 如果是冷启动直接返回详细信息，否则查详情
        if isinstance(result, list) and result and isinstance(result[0], dict):
            seed_list = result
        else:
            seed_list = seed_info(result)
        return jsonify({'recommend': seed_list})
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
