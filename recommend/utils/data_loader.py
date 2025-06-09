from utils.parse_args import args
from os import path
from tqdm import tqdm
import numpy as np
import scipy.sparse as sp
import torch
import networkx as nx
from copy import deepcopy
from collections import defaultdict
import pandas as pd


class EdgeListData:
    def __init__(self, train_file, test_file, phase='pretrain', pre_dataset=None, has_time=True):
        self.phase = phase
        self.has_time = has_time
        self.pre_dataset = pre_dataset

        self.hour_interval = args.hour_interval_pre if phase == 'pretrain' else args.hour_interval_f

        self.edgelist = []
        self.edge_time = []
        self.num_users = 0
        self.num_items = 0
        self.num_edges = 0

        self.train_user_dict = {}
        self.test_user_dict = {}

        self._load_data(train_file, test_file, has_time)

        if phase == 'pretrain':
            self.user_hist_dict = self.train_user_dict
        
        users_has_hist = set(list(self.user_hist_dict.keys()))
        all_users = set(list(range(self.num_users)))
        users_no_hist = all_users - users_has_hist
        for u in users_no_hist:
            self.user_hist_dict[u] = []

    def _read_file(self, train_file, test_file, has_time=True):
        with open(train_file, 'r') as f:
            for line in f:
                line = line.strip().split('\t')
                if not has_time:
                    user, items = line[:2]
                    times = " ".join(["0"] * len(items.split(" ")))
                else:
                    user, items, times = line
                    
                for i in items.split(" "):
                    self.edgelist.append((int(user), int(i)))
                for i in times.split(" "):
                    self.edge_time.append(int(i))
                self.train_user_dict[int(user)] = [int(i) for i in items.split(" ")]

        self.test_edge_num = 0
        with open(test_file, 'r') as f:
            for line in f:
                line = line.strip().split('\t')
                user, items = line[:2]
                self.test_user_dict[int(user)] = [int(i) for i in items.split(" ")]
                self.test_edge_num += len(self.test_user_dict[int(user)])

    def _load_data(self, train_file, test_file, has_time=True):
        self._read_file(train_file, test_file, has_time)

        self.edgelist = np.array(self.edgelist, dtype=np.int32)
        self.edge_time = 1 + self.timestamp_to_time_step(np.array(self.edge_time, dtype=np.int32))
        self.num_edges = len(self.edgelist)
        if self.pre_dataset is not None:
            self.num_users = self.pre_dataset.num_users
            self.num_items = self.pre_dataset.num_items
        else:
            self.num_users = max([np.max(self.edgelist[:, 0]) + 1, np.max(list(self.test_user_dict.keys())) + 1])
            self.num_items = max([np.max(self.edgelist[:, 1]) + 1, np.max([np.max(self.test_user_dict[u]) for u in self.test_user_dict.keys()]) + 1])

        self.graph = sp.coo_matrix((np.ones(self.num_edges), (self.edgelist[:, 0], self.edgelist[:, 1])), shape=(self.num_users, self.num_items))

        if self.has_time:
            self.edge_time_dict = defaultdict(dict)
            for i in range(len(self.edgelist)):
                self.edge_time_dict[self.edgelist[i][0]][self.edgelist[i][1]+self.num_users] = self.edge_time[i]
                self.edge_time_dict[self.edgelist[i][1]+self.num_users][self.edgelist[i][0]] = self.edge_time[i]

    def timestamp_to_time_step(self, timestamp_arr, least_time=None):
        interval_hour = self.hour_interval
        if least_time is None:
            least_time = np.min(timestamp_arr)
        timestamp_arr = timestamp_arr - least_time
        timestamp_arr = timestamp_arr // (interval_hour * 3600)
        return timestamp_arr
