import math
# import random
# import pandas as pd
from collections import defaultdict
from operator import itemgetter
# from read_user import *
import time


class UserCF(object):
    """ User based Collaborative Filtering Algorithm Implementation"""

    def __init__(self, rates, similarity="iif"):
        self._User_Item = dict()
        titles = ['职业', '省份', '感兴趣的领域']
        self._User_Attributes = {title: dict() for title in titles}
        self._similarity = similarity
        self._userSimMatrix = dict()  # 用户相似度矩阵
        self.rates = rates
        self._userJaccardDis = {title: dict() for title in titles}
        self._userCoMatrix = dict()

    def similarity(self, Item_Dict, user_id, DisMatrix):
        # 建立User-Item倒排表
        item_user = dict()
        for user, items in Item_Dict.items():
            for item in items:
                item_user.setdefault(item, set())
                item_user[item].add(user)

        # temp_dict = dict()
        # 建立用户物品交集矩阵W, 其中C[u][v]代表的含义是用户u和用户v之间共同喜欢的物品数
        for item, users in item_user.items():
            for u in users:
                for v in users:
                    if u == v:
                        continue
                    if u == user_id or v == user_id:
                        DisMatrix.setdefault(u, defaultdict(int))
                        if self._similarity == "cosine":
                            DisMatrix[u][v] += 1  # 将用户u和用户v共同喜欢的物品数量加一
                        elif self._similarity == "iif":
                            DisMatrix[u][v] += 1. / math.log(1 + len(users))

        # 建立用户相似度矩阵
        for u, related_user in DisMatrix.items():
            # 相似度公式为 |N[u]∩N[v]|/sqrt(N[u]||N[v])
            if u == user_id or user_id in related_user:
                for v, cuv in related_user.items():
                    nu = len(Item_Dict[u])
                    nv = len(Item_Dict[v])
                    DisMatrix[u][v] = cuv / math.sqrt(nu * nv)

    def merge_candidates(self):
        total_user = set(self._User_Item.keys()) | set(self._User_Attributes['职业'].keys() | set(self._User_Attributes['省份'].keys()) | set(self._User_Attributes['感兴趣的领域'].keys()))
        for u in total_user:
            if u in self._userJaccardDis['职业']:
                item1 = self._userJaccardDis['职业'][u]
            else:
                item1 = dict()
            if u in self._userJaccardDis['省份']:
                item2 = self._userJaccardDis['省份'][u]
            else:
                item2 = dict()
            if u in self._userJaccardDis['感兴趣的领域']:
                item3 = self._userJaccardDis['感兴趣的领域'][u]
            else:
                item3 = dict()
            if u in self._userSimMatrix:
                item = self._userSimMatrix[u]
            else:
                item = dict()
            total_item = set(item.keys()) | set(item1.keys()) | set(item2.keys()) | set(item3.keys())
            Matrix_combine = [self._userSimMatrix, self._userJaccardDis['职业'], self._userJaccardDis['省份'],
                              self._userJaccardDis['感兴趣的领域']]
            self._userCoMatrix.setdefault(u, defaultdict(int))
            for v in total_item:
                val = 0
                for ind, mat in enumerate(Matrix_combine):
                    if u in mat and v in mat[u]:
                        val += self.rates[ind] * mat[u][v]
                self._userCoMatrix[u][v] = val

    def recommend(self, user, N, K):
        """
        用户u对物品i的感兴趣程度：
            p(u,i) = ∑WuvRvi
            其中Wuv代表的是u和v之间的相似度， Rvi代表的是用户v对物品i的感兴趣程度，因为采用单一行为的隐反馈数据，所以Rvi=1。
            所以这个表达式的含义是，要计算用户u对物品i的感兴趣程度，则要找到与用户u最相似的K个用户，对于这k个用户喜欢的物品且用户u
            没有反馈的物品，都累加用户u与用户v之间的相似度。
        :param user: 被推荐的用户user
        :param N: 推荐的商品个数
        :param K: 查找的最相似的用户个数
        :return: 按照user对推荐物品的感兴趣程度排序的N个商品
        """
        recommends = dict()
        # 先获取user具有正反馈的item数组
        if user not in self._userCoMatrix:
            return []
        if user not in self._User_Item:
            related_items = []
        else:
            related_items = self._User_Item[user]
        # 将其他用户与user按照相似度逆序排序之后取前K个
        for v, sim in sorted(self._userCoMatrix[user].items(), key=itemgetter(1), reverse=True)[:K]:
            # 从与user相似的用户的喜爱列表中寻找可能的物品进行推荐
            for item in self._User_Item[v]:
                # 如果与user相似的用户喜爱的物品与user喜欢的物品重复了，直接跳过
                if item in related_items:
                    continue
                recommends.setdefault(item, 0.)
                recommends[item] += sim
        # 根据被推荐物品的相似度逆序排列，然后推荐前N个物品给到用户
        return list(sorted(recommends.keys(), key=itemgetter(1), reverse=True)[:N])

    def add_user(self, user_id, user_attributes: dict):
        """
        user_attributes:Dict{Attribute:List[]}
        """
        for title in user_attributes.keySet():
            if title == '浏览记录':
                self._User_Item[user_id] = set(user_attributes[title])
            else:
                self._User_Attributes[title][user_id] = set(user_attributes[title])
        self.similarity(self._User_Item, user_id, self._userSimMatrix)
        for title, Dict in self._userJaccardDis.items():
            self.similarity(self._User_Attributes[title], user_id, self._userJaccardDis[title])

    def chage_user_attrubute(self, user_id, attribute_name, val: list):
        if attribute_name == '浏览记录':
            self._User_Item[user_id] = set(val)
        else:
            self._User_Attributes[attribute_name][user_id] = set(val)
        self.similarity(self._User_Item, user_id, self._userSimMatrix)
        for title, Dict in self._userJaccardDis.items():
            self.similarity(self._User_Attributes[title], user_id, self._userJaccardDis[title])

    def del_user(self, user_id):
        if user_id in self._User_Item:
            del self._User_Item[user_id]
            for title in self._User_Attributes.keys():
                del self._User_Attributes[title][user_id]

            for u, related_user in self._User_Item.items():
                if u == user_id:
                    del self._userSimMatrix[u]
                elif user_id in related_user:
                    del self._userSimMatrix[u][user_id]
            for title in self._userJaccardDis.keys():
                for u, related_user in self._userJaccardDis[title]:
                    if u == user_id:
                        del self._userJaccardDis[title][u]
                    elif user_id in related_user:
                        del self._userJaccardDis[title][user_id]
        else:
            print('No Such User')

    def train(self):
        self.merge_candidates()


# if __name__ == "__main__":
#     # file_root = r'./data/user_info.txt'
#     # user_attributes_dict, user_item_dict = read_user_data(file_root)
#     rates = [0.25, 0.25, 0.25, 0.25]  # 浏览记录 职业 省份 感兴趣的领域
#     a = time.time()
#     userCF = UserCF(rates)
#     userCF.train()
#     b = time.time()
#     # 分别对测试集中的前4个用户进行电影推荐
#     a1 = time.time()
#     recommend = userCF.recommend(2, 30, 160)
#     b1 = time.time()
