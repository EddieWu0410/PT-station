import argparse

def parse_args():
    parser = argparse.ArgumentParser(description='GraphPro')
    parser.add_argument('--phase', type=str, default='pretrain')
    parser.add_argument('--plugin', action='store_true', default=False)
    parser.add_argument('--save_path', type=str, default="saved" ,help='where to save model and logs')
    parser.add_argument('--data_path', type=str, default="dataset/yelp",help='where to load data')
    parser.add_argument('--exp_name', type=str, default='1')
    parser.add_argument('--desc', type=str, default='')
    parser.add_argument('--ab', type=str, default='full')
    parser.add_argument('--log', type=int, default=1)

    parser.add_argument('--device', type=str, default="cuda")
    parser.add_argument('--model', type=str, default='GraphPro')
    parser.add_argument('--pre_model', type=str, default='GraphPro')
    parser.add_argument('--f_model', type=str, default='GraphPro')
    parser.add_argument('--pre_model_path', type=str, default='pretrained_model.pt')

    parser.add_argument('--hour_interval_pre', type=float, default=1)
    parser.add_argument('--hour_interval_f', type=int, default=1)
    parser.add_argument('--emb_dropout', type=float, default=0)

    parser.add_argument('--updt_inter', type=int, default=1)
    parser.add_argument('--samp_decay', type=float, default=0.05)
    
    parser.add_argument('--edge_dropout', type=float, default=0.5)
    parser.add_argument('--emb_size', type=int, default=64)
    parser.add_argument('--batch_size', type=int, default=2048)
    parser.add_argument('--eval_batch_size', type=int, default=512)
    parser.add_argument('--seed', type=int, default=2023)
    parser.add_argument('--num_epochs', type=int, default=300)
    parser.add_argument('--neighbor_sample_num', type=int, default=5)
    parser.add_argument('--lr', type=float, default=0.001)
    parser.add_argument('--weight_decay', type=float, default=1e-4)
    parser.add_argument('--metrics', type=str, default='recall;ndcg')
    parser.add_argument('--metrics_k', type=str, default='20')
    parser.add_argument('--early_stop_patience', type=int, default=10)
    parser.add_argument('--neg_num', type=int, default=1)

    parser.add_argument('--num_layers', type=int, default=3)


    return parser

parser = parse_args()
args = parser.parse_known_args()[0]
if args.pre_model == args.f_model:
    args.model = args.pre_model
elif args.pre_model != 'LightGCN':
    args.model = args.pre_model

args = parser.parse_args()
if args.pre_model == args.f_model:
    args.model = args.pre_model
elif args.pre_model != 'LightGCN':
    args.model = args.pre_model