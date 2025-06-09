# API 设计文档

## 模块功能一览表

|                                                              | Cheat-System | DataManager-System | Recommend-System | VIP-System | RAG-System | Tracker-System |
| ------------------------------------------------------------ | ------------ | ------------------ | ---------------- | ---------- | ---------- | -------------- |
| 用户基本信息维护<br />（用户名，密码，邮箱，性别，学校，头像URL，个人说明）<br />【WHT】 |              | Y                  |                  |            |            |                |
| 用户PT站数据维护<br />魔力值，上传量，下载量，分享率，播种机制默认磁盘位置)<br />【WHT】 |              | Y                  |                  |            |            | Y              |
|                                                              |              |                    |                  |            |            |                |
| 种子信息维护（种子ID，TrackerURL）<br />【WHT】              |              | Y                  |                  |            |            | Y              |
| 种子信息搜索（下载URL，标题，副标题，基本信息[大小，类型]，外部链接，热度表）【WHT】 |              | Y                  |                  |            | Y          |                |
| 种子信息推荐（下载URL，标题，副标题，基本信息[大小，类型]，外部链接，热度表）【WHT】 |              |                    | Y                |            |            |                |
| 种子下载【RHJ】<br />（用户id，账户状态，种子id，文件大小，文件哈希值，下载时间，开始时间） |              |                    |                  |            |            | Y              |
| 专线下载【RHJ】<br />（用户id，账户状态，种子id，专线id，ip，端口，文件大小，文件哈希值，下载时间、开始时间） |              |                    |                  | Y          |            |                |
| 种子传输量信息维护【RHJ】<br />（用户id，种子id，用户上传量，用户下载量，用户上传峰值，用户下载峰值，总上传量，总下载量，活跃peer数） |              |                    |                  |            |            | Y              |
| 种子成长期保护【RHJ】<br />（种子id，做种数，播种任务id，奖励类型，奖励数值，关联用户，完成情况） |              |                    |                  | Y          |            |                |
| 主动播种机制【RHJ】<br />（种子id，做种数，上次检查时间）    |              |                    |                  | Y          |            |                |
|                                                              |              |                    |                  |            |            |                |
| 做假种检测【WKJ】<br />(主动请求检测：客户端ID，上次请求时间，下次请求时间，连续失败次数) | Y            |                    |                  |            |            |                |
| 伪造上传，下载量检测【WKJ】<br />（作弊个体确认：异常事件唯一ID，用户ID，种子ID） | Y            |                    |                  |            |            |                |
| 作弊惩罚申诉【WKJ】<br />（申诉事件唯一ID，用户ID，账号惩罚内容，用户提交文件，审核状态，审核结果，用户账号状态） | Y            |                    |                  |            |            |                |
|                                                              |              |                    |                  |            |            |                |
| 公告发布与保存【WKJ】<br />（公告ID，公告内容，公告状态，公告板块） |              | Y                  |                  |            |            |                |
| 邀请机制【WKJ】<br />(用户ID，用户剩余可邀请数量，用户已邀请ID) |              | Y                  |                  |            |            |                |
| 求种机制与求种审核【WKJ】<br />(求种帖子ID，求种人数，悬赏魔力值，审核状态，审核结果，是否已推出种子) |              | Y                  |                  |            |            | Y              |
|                                                              |              |                    |                  |            |            |                |
| 论坛信息维护（帖子ID，标题，内容，发帖用户，发帖时间，回复数量，查看次数，回复[回复ID，主题ID，回复内容，回复用户，回复时间]）【WHT】 |              | Y                  |                  |            |            |                |
| 论坛信息搜索（标题，内容）【WHT】                            |              | Y                  |                  |            | Y          |                |
| 论坛信息推荐（标题，内容，发帖用户，发帖时间，回复数量，查看次数）【WHT】 |              |                    | Y                |            |            |                |
|                                                              |              |                    |                  |            |            |                |
| 用户收藏夹                                                   |              | Y                  |                  |            |            |                |
| 魔力值兑换                                                   |              | Y                  |                  |            |            |                |
| 用户迁移                                                     |              | Y                  |                  |            |            |                |

## 功能点与系统交互接口定义

|                                                          | DataManager-System                                           |
| -------------------------------------------------------- | ------------------------------------------------------------ |
| 用户基本信息维护<br />（用户名，密码，邮箱，性别，学校） | `int RegisterUser(User useri)->state(0:success,1:conflict)`<br />`int UpdateInformation(User useri)->state(0:success,1:conflict)`<br />`User GetInformation (UserID id)->all infors ` |

|                                                              | DataManager-System                                           | Tracker-System                                               |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 用户PT站数据维护<br />(魔力值，上传量，下载量，播种机制默认磁盘位置) | `UserPT GetInformation(UserID id)`<br />`int UpdateInformation(UserID id)`<br />`int RegisterUser(UserPT upt)` | `bool AddUpload(UserID)`<br />`bool ReduceUpload(UserID)`<br />`bool AddDownload(UserID)`<br />`bool ReduceDownload(UserID)`<br />`bool ReduceMagic(UserID)`<br />`bool AddMagic(UserID)` |

|                                                           | DataManager-System                                           | Tracker-System                                               |
| --------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 种子信息维护（直接读取Torrent文件，修改TrackerURL）<br /> | `Seed GetSeedInformation(SeedID id)`  <br />`bool RegisterSeed(Seed sd)`<br />`bool UpdateSeed(Seed sd)` | `TTorent ReadTorrent(File file)`<br />`Bool SaveTorrent(TTorent seed,SeedID id)` |

|                                     | DataManager-System        | RAG-System                    |
| ----------------------------------- | ------------------------- | ----------------------------- |
| 种子信息搜索（包括深度搜索）【WHT】 | `Seed SearchSeed(String)` | `Seed DeepSearchSeed(String)` |

|                     | Recommend-System                      |
| ------------------- | ------------------------------------- |
| 种子信息推荐【WHT】 | `List[Seed] GetRecommend(User useri)` |

|                 | Tracker-System                               |
| --------------- | -------------------------------------------- |
| 种子下载【RHJ】 | `File GetTorrent(SeedID id,UserID id)`<br /> |

|                 | VIP-System                         |
| --------------- | ---------------------------------- |
| 专线下载【RHJ】 | `File GetTorrent(SeedID id)`<br /> |

|                           | Tracker-System                                               |
| ------------------------- | ------------------------------------------------------------ |
| 种子传输量信息维护【RHJ】 | `bool AddRecord（SeedID id，Time starttime，Time endtime，UserID id，TransportVolume v）`//+v :up,-v:down |

|                       | VIP-System                                                   |
| --------------------- | ------------------------------------------------------------ |
| 种子成长期保护【RHJ】 | `bool AddFarmerNumber(int number)`<br />`bool ReduceFarmerNumber(int number)`<br />`bool AddSeed(TTorent seed,SeedID id)`<br />`bool RemoveSeed(SeedID)`<br />`void CheckSeed()`//检查当前保种列表是否需要更新 |

|                     | VIP-System                         |
| ------------------- | ---------------------------------- |
| 主动播种机制【RHJ】 | `List[SeedID] GetSeedList()`<br /> |

|                   | Cheat-System                          |
| ----------------- | ------------------------------------- |
| 做假种检测【WKJ】 | `List[(UserID,SeedID)] GetFeakSeed()` |

|                             | Cheat-System                                                 |
| --------------------------- | ------------------------------------------------------------ |
| 伪造上传，下载量检测【WKJ】 | `void DetectionSeed(SeedID id)`<br />`void DetectionAll(SeedID id)`<br />`void PunishUser()`<br />`bool GetUserPunish(UserID id)`:每次登录触发该操作<br />`List[UserID] GetAllPunishUser()` |

|                     | Cheat-System                                                 |
| ------------------- | ------------------------------------------------------------ |
| 作弊惩罚申诉【WKJ】 | `bool AddAppeal(Appeal appeali)`<br />`List[Appeal] GetAppeal(UserID id)`<br />`void RevokePunish(UserID id)`<br /> |

|                       | DataManager-System                                           |
| --------------------- | ------------------------------------------------------------ |
| 公告发布与保存【WKJ】 | `bool AddNotice(Notice nti)`<br />`bool UpdateNotice(Notice nti)`<br />`bool DropNotice(Notice nti)` |

|                 | DataManager-System                                           |
| --------------- | ------------------------------------------------------------ |
| 邀请机制【WKJ】 | `int GetUserAvailableTimes(UserID)`<br />`bool InviteUser(UserID Inviter,UserID BeInviter)` |

|                           | DataManager-System                                           |
| ------------------------- | ------------------------------------------------------------ |
| 求种机制与求种审核【WKJ】 | `bool AddBegSeed(BegInfo info)`<br />`bool RevokeBeg(BegID id)`<br />`bool VoteSeed(SeedID id,int vote)`<br />`bool SubmitSeed(SeedID sid,BegID bid)` |

|                     | DataManager-System                                           |
| ------------------- | ------------------------------------------------------------ |
| 论坛信息维护【WHT】 | `bool AddPost(Post pi)`<br />`bool DropPost(PostID id)`<br />`bool AddCommend(PostID id,Comment cmt)`<br />``bool DropCommend(PostID id,CommentID cid)` |

|            | DataManager-System                                           |
| ---------- | ------------------------------------------------------------ |
| 用户收藏夹 | `bool AddCollect(UserID,SeedID)`<br />`bool DropCollect(UserID,SeedID)` |

|            | DataManager-System                             |
| ---------- | ---------------------------------------------- |
| 魔力值兑换 | `bool exchange(UserID id,int costmagic)`<br /> |

|          | DataManager-System                                           |
| -------- | ------------------------------------------------------------ |
| 用户迁移 | `bool UploadProfile(Profile pr)`<br />`ProfileID GetProfile(ProfileID)`<br />`bool examineProfile(bool passornot)`<br />`List[Profile] GetToExamines()`<br /> |



# 环境变量维护值

* TrackerURL
* 预期做种数
* FakeTime：做假种次数
* BegVote:悬赏种子的投票阈值





# 数据表设计

* User表：存储用户的基本信息
  * 用户ID（字符串），邮箱（字符串）：主键，唯一标识，
  * 用户名：字符串
  * 密码：字符串
  * 性别：m或者f，
  * 学校：字符串，
  * 头像URL：字符串
  * 个人说明：字符串
  * 账户状态：是否被ban，布尔值（0正常，1被ban）
  * 用户剩余可邀请数量
* 用户邀请表
  * 用户ID：字符串，主键
  * 邀请人邮箱：字符串，主键
  * 邀请人是否注册：布尔值
* UserPT表：存储用户的PT站信息
  * 用户ID：主键，应该设置为User表的外键（User表中有这里才能插入，User表删除了，这里必须要删除）
  * 魔力值：整数，用于兑换上传量，下载量，VIP次数
  * 上传量：整数，
  * 下载量：整数
  * 分享率：小数
  * 参与播种机制默认磁盘位置：字符串
  * VIP下载次数：整数
* Seed：种子表，存放每个种子的基本信息
  * SeedID：种子ID，每个种子的唯一编号，主键，字符串
  * 保种用户ID：字符串
  * 标记假种次数：如果检测到一次则加一，成功了清0，大于环境变量的FakeTime则标记为Cheat
  * 上次假种检查时间
  * 外部下载URL：字符串
  * 标题：字符串
  * 副标题：字符串
  * 种子大小：字符串
  * 种子标签：字符串
  * 种子热度（下载量）：整数
* SeedDownload：种子文件下载表（每一条记录表示一次传输任务）
  * 传输任务ID：唯一标识每次传输任务
  * 用户id：字符串，应该设置为User表的外键（User表中有这里才能插入，User表删除了，这里必须要删除）
  * 种子id：下载的哪个种子文件，应该设置为Seed表的外键（Seed表中有这里才能插入，Seed表删除了，这里必须要删除）
  * 下载开始时间：
  * 下载完成时间：
  * 是否是专线下载：bool，0表示正常，1表示专线
  * 下载端ip
* VipSeed：缓存种子数据表
  * 种子id：应该设置为Seed表的外键（Seed表中有这里才能插入，Seed表删除了，这里必须要删除）
  * 做种数（小于等于环境变量：预期做种数）
  * 奖励魔力值数量：
  * 是否继续缓存：0表示继续缓存，1表示不在缓存中
* Transport：种子传输信息表
  * 传输任务ID：唯一标识每次传输任务，每次增量的时候通过选择ID然后增加对应字段值，主键
  * 上传用户ID：字符串，主键，同时应该是User表的外键（User表中有这里才能插入，User表删除了，这里必须要删除）
  * 下载用户ID：字符串，主键，同时应该是User表的外键（User表中有这里才能插入，User表删除了，这里必须要删除）
  * 种子id：字符串必须是Seed的外键，（Seed表中有这里才能插入，Seed表删除了，这里必须要删除）
  * 用户上传量：整数
  * 用户下载量：整数
  * 用户上传峰值：整数
  * 用户下载峰值：整数
* BegSeed：求种任务列表
  * 求种帖子ID：唯一标识
  * 求种人数：有多少个人页点了“求种”，整数
  * 悬赏魔力值：（悬赏者付10%的钱）
  * 悬赏截止时间：
  * 是否有合适种子
* SubmitSeed：提交悬赏任务
  * 求种ID：唯一标识需要是BegSeed的外键
  * 种子ID：唯一标识：需要是Seed的外键
  * 投票：大于环境变量设置票数则在BegSeed中更新为合适，int值，悬赏周期结束后，选取投票数最高的成为悬赏结果
* 帖子表
  * 帖子ID：唯一标识
  * 标题，
  * 内容，
  * 发帖用户，
  * 发帖时间，
  * 回复数量，
  * 查看次数
* 帖子回复表
  * 回复ID，
  * 回复对应帖子ID（要是帖子表的外键）
  * 回复内容，
  * 回复用户，
  * 回复时间
* 公告表
  * 公告ID
  * 公告内容，
  * 公告状态：布尔值，表示是否公示
  * 公共标签
* 用户收藏夹表（没有主键）
  * 用户ID（User表外键）
  * 收藏的种子ID（Seed表外键，要设置级联删除）
* 用户迁移表
  * 迁移任务ID：唯一标识
  * 用户ID：外键
  * 迁移申请书的URL
  * 是否评审通过
  * 待发放魔力值
  * 已发放魔力值
  * 待发放上传量
  * 已发放上传量
* 申诉表
  * 申诉id
  * 申诉人
  * 申诉内容
  * 申诉文件URL、
  * 审核状态（int)

* TTorent表

```sql
-- 用户表
CREATE TABLE `User` (
  `user_id` VARCHAR(36) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `gender` ENUM('m','f') NOT NULL,
  `school` VARCHAR(255) DEFAULT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `bio` TEXT,
  `account_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=正常,1=被ban',
  `invite_left` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`,`email`),
  UNIQUE KEY `uniq_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户邀请表
CREATE TABLE `UserInvite` (
  `user_id` VARCHAR(36) NOT NULL,
  `inviter_email` VARCHAR(255) NOT NULL,
  `inviter_registered` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`,`inviter_email`),
  CONSTRAINT `fk_ui_user` FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户 PT 信息表
CREATE TABLE `UserPT` (
  `user_id` VARCHAR(36) NOT NULL,
  `magic` INT NOT NULL DEFAULT 0,
  `uploaded` BIGINT NOT NULL DEFAULT 0,
  `downloaded` BIGINT NOT NULL DEFAULT 0,
  `ratio` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `default_seed_path` VARCHAR(255) DEFAULT NULL,
  `vip_downloads` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_pt_user` FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 种子表
CREATE TABLE `Seed` (
  `seed_id` VARCHAR(64) NOT NULL,
  `owner_user_id` VARCHAR(36) NOT NULL,
  `fake_hits` INT NOT NULL DEFAULT 0,
  `last_fake_check` DATETIME DEFAULT NULL,
  `external_url` VARCHAR(255) DEFAULT NULL,
  `title` VARCHAR(255) NOT NULL,
  `subtitle` VARCHAR(255) DEFAULT NULL,
  `size` VARCHAR(50) NOT NULL,
  `tags` VARCHAR(255) DEFAULT NULL,
  `popularity` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`seed_id`),
  CONSTRAINT `fk_seed_user` FOREIGN KEY (`owner_user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 种子下载表
CREATE TABLE `SeedDownload` (
  `task_id` VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `seed_id` VARCHAR(64) NOT NULL,
  `download_start` DATETIME NOT NULL,
  `download_end` DATETIME DEFAULT NULL,
  `is_dedicated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=普通,1=专线',
  `client_ip` VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  CONSTRAINT `fk_sd_user` FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sd_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed`(`seed_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 缓存种子数据表
CREATE TABLE `VipSeed` (
  `seed_id` VARCHAR(64) NOT NULL,
  `seeder_count` INT NOT NULL DEFAULT 0,
  `reward_magic` INT NOT NULL DEFAULT 0,
  `stop_caching` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=继续缓存,1=不缓存',
  PRIMARY KEY (`seed_id`),
  CONSTRAINT `fk_vip_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed`(`seed_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 传输信息表
CREATE TABLE `Transport` (
  `task_id` VARCHAR(64) NOT NULL,
  `uploader_id` VARCHAR(36) NOT NULL,
  `downloader_id` VARCHAR(36) NOT NULL,
  `seed_id` VARCHAR(64) NOT NULL,
  `uploaded` BIGINT NOT NULL DEFAULT 0,
  `downloaded` BIGINT NOT NULL DEFAULT 0,
  `upload_peak` BIGINT NOT NULL DEFAULT 0,
  `download_peak` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`task_id`, `uploader_id`, `downloader_id`),
  CONSTRAINT `fk_tr_user_up` FOREIGN KEY (`uploader_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tr_user_down` FOREIGN KEY (`downloader_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tr_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed`(`seed_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 求种任务表
CREATE TABLE `BegSeed` (
  `beg_id` VARCHAR(64) NOT NULL,
  `beg_count` INT NOT NULL DEFAULT 0,
  `reward_magic` INT NOT NULL DEFAULT 0,
  `deadline` DATETIME NOT NULL,
  `has_match` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`beg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 提交悬赏任务表
CREATE TABLE `SubmitSeed` (
  `beg_id` VARCHAR(64) NOT NULL,
  `seed_id` VARCHAR(64) NOT NULL,
  `votes` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`beg_id`,`seed_id`),
  CONSTRAINT `fk_ss_beg` FOREIGN KEY (`beg_id`) REFERENCES `BegSeed`(`beg_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ss_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed`(`seed_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 帖子表
CREATE TABLE `Post` (
  `post_id` VARCHAR(64) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `author_id` VARCHAR(36) NOT NULL,
  `created_at` DATETIME NOT NULL,
  `reply_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`post_id`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`author_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 帖子回复表
CREATE TABLE `PostReply` (
  `reply_id` VARCHAR(64) NOT NULL,
  `post_id` VARCHAR(64) NOT NULL,
  `content` TEXT NOT NULL,
  `author_id` VARCHAR(36) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`reply_id`),
  CONSTRAINT `fk_pr_post` FOREIGN KEY (`post_id`) REFERENCES `Post`(`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pr_user` FOREIGN KEY (`author_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 公告表
CREATE TABLE `Announcement` (
  `announce_id` VARCHAR(64) NOT NULL,
  `content` TEXT NOT NULL,
  `is_public` TINYINT(1) NOT NULL DEFAULT 0,
  `tag` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`announce_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户收藏夹表
CREATE TABLE `UserFavorite` (
  `user_id` VARCHAR(36) NOT NULL,
  `seed_id` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`user_id`,`seed_id`),
  CONSTRAINT `fk_uf_user` FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_uf_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed`(`seed_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户迁移表
CREATE TABLE `UserMigration` (
  `migration_id` VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `application_url` VARCHAR(255) NOT NULL,
  `approved` TINYINT(1) NOT NULL DEFAULT 0,
  `pending_magic` INT NOT NULL DEFAULT 0,
  `granted_magic` INT NOT NULL DEFAULT 0,
  `pending_uploaded` BIGINT NOT NULL DEFAULT 0,
  `granted_uploaded` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`migration_id`),
  CONSTRAINT `fk_um_user` FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 申诉表
CREATE TABLE `Appeal` (
  `appeal_id` VARCHAR(64) NOT NULL COMMENT '申诉ID',
  `user_id`   VARCHAR(36) NOT NULL COMMENT '申诉人（User.user_id）',
  `content`   TEXT         NOT NULL COMMENT '申诉内容',
  `file_url`  VARCHAR(255) DEFAULT NULL COMMENT '申诉文件URL',
  `status`    INT          NOT NULL DEFAULT 0 COMMENT '审核状态',
  PRIMARY KEY (`appeal_id`),
  CONSTRAINT `fk_appeal_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `User`(`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COMMENT='申诉表，记录用户的申诉信息';


```

