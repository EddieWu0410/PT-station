-- 用户表
CREATE TABLE `User` (
    `user_id` VARCHAR(36) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `username` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `gender` ENUM('m', 'f') NOT NULL,
    `school` VARCHAR(255) DEFAULT NULL,
    `avatar_url` VARCHAR(255) DEFAULT NULL,
    `bio` TEXT,
    `account_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=正常,1=被ban',
    `invite_left` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `email`),
    UNIQUE KEY `uniq_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用户邀请表
CREATE TABLE `UserInvite` (
    `user_id` VARCHAR(36) NOT NULL,
    `inviter_email` VARCHAR(255) NOT NULL,
    `inviter_registered` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `inviter_email`),
    CONSTRAINT `fk_ui_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用户 PT 信息表
CREATE TABLE `UserPT` (
    `user_id` VARCHAR(36) NOT NULL,
    `magic` INT NOT NULL DEFAULT 0,
    `uploaded` BIGINT NOT NULL DEFAULT 0,
    `downloaded` BIGINT NOT NULL DEFAULT 0,
    `ratio` DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    `default_seed_path` VARCHAR(255) DEFAULT NULL,
    `vip_downloads` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_pt_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    CONSTRAINT `fk_seed_user` FOREIGN KEY (`owner_user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    CONSTRAINT `fk_sd_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sd_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 缓存种子数据表
CREATE TABLE `VipSeed` (
    `seed_id` VARCHAR(64) NOT NULL,
    `seeder_count` INT NOT NULL DEFAULT 0,
    `reward_magic` INT NOT NULL DEFAULT 0,
    `stop_caching` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=继续缓存,1=不缓存',
    PRIMARY KEY (`seed_id`),
    CONSTRAINT `fk_vip_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    PRIMARY KEY (
        `task_id`,
        `uploader_id`,
        `downloader_id`
    ),
    CONSTRAINT `fk_tr_user_up` FOREIGN KEY (`uploader_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tr_user_down` FOREIGN KEY (`downloader_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tr_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 求种任务表
CREATE TABLE `BegSeed` (
    `beg_id` VARCHAR(64) NOT NULL,
    `beg_count` INT NOT NULL DEFAULT 0,
    `reward_magic` INT NOT NULL DEFAULT 0,
    `deadline` DATETIME NOT NULL,
    `has_match` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`beg_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 提交悬赏任务表
CREATE TABLE `SubmitSeed` (
    `beg_id` VARCHAR(64) NOT NULL,
    `seed_id` VARCHAR(64) NOT NULL,
    `votes` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`beg_id`, `seed_id`),
    CONSTRAINT `fk_ss_beg` FOREIGN KEY (`beg_id`) REFERENCES `BegSeed` (`beg_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ss_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    CONSTRAINT `fk_post_user` FOREIGN KEY (`author_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 帖子回复表
CREATE TABLE `PostReply` (
    `reply_id` VARCHAR(64) NOT NULL,
    `post_id` VARCHAR(64) NOT NULL,
    `content` TEXT NOT NULL,
    `author_id` VARCHAR(36) NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`reply_id`),
    CONSTRAINT `fk_pr_post` FOREIGN KEY (`post_id`) REFERENCES `Post` (`post_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pr_user` FOREIGN KEY (`author_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 公告表
CREATE TABLE `Announcement` (
    `announce_id` VARCHAR(64) NOT NULL,
    `content` TEXT NOT NULL,
    `is_public` TINYINT(1) NOT NULL DEFAULT 0,
    `tag` VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (`announce_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用户收藏夹表
CREATE TABLE `UserFavorite` (
    `user_id` VARCHAR(36) NOT NULL,
    `seed_id` VARCHAR(64) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `seed_id`),
    CONSTRAINT `fk_uf_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_uf_seed` FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

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
    CONSTRAINT `fk_um_user` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 悬赏任务用户投票表
CREATE TABLE `UserVotes` (
    `user_id` VARCHAR(36) NOT NULL,
    `beg_id` VARCHAR(64) NOT NULL,
    `seed_id` VARCHAR(64) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (
        `user_id`,
        `beg_id`,
        `seed_id`
    ),
    FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    FOREIGN KEY (`beg_id`) REFERENCES `BegSeed` (`beg_id`) ON DELETE CASCADE,
    FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
-- 种子促销表
CREATE TABLE `SeedPromotion` (
    `promotion_id` VARCHAR(64) NOT NULL,
    `seed_id` VARCHAR(64) NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `discount` TINYINT NOT NULL DEFAULT 1 COMMENT '折扣率, 1表示无折扣',
    PRIMARY KEY (`promotion_id`),
    FOREIGN KEY (`seed_id`) REFERENCES `Seed` (`seed_id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `BegInfo` (
    `beg_id` varchar(64) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `Info` text NOT NULL,
    PRIMARY KEY (`beg_id`),
    CONSTRAINT `fk_BegInfo_BegSeed` FOREIGN KEY (`beg_id`) REFERENCES `BegSeed` (`beg_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_BegInfo_User` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE

) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci

