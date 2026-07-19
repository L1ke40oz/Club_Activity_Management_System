-- 创建数据库
CREATE DATABASE IF NOT EXISTS `campus_club_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `campus_club_db`;

-- 1. 用户表
CREATE TABLE `sys_user` (
  `student_id` varchar(20) NOT NULL COMMENT '学号（主键）',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `password` varchar(255) NOT NULL COMMENT '登录密码 (BCrypt加密)',
  `class_name` varchar(50) DEFAULT NULL COMMENT '所在班级',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户, 1-系统管理员',
  `contact_info` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `no_show_count` int NOT NULL DEFAULT '0' COMMENT '活动爽约（未签到）次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 社团表
CREATE TABLE `club_info` (
  `club_id` int NOT NULL AUTO_INCREMENT COMMENT '社团编号（主键）',
  `name` varchar(100) NOT NULL COMMENT '社团名称',
  `category` varchar(50) DEFAULT NULL COMMENT '社团类别',
  `established_date` date NOT NULL COMMENT '成立日期',
  `advisor` varchar(50) DEFAULT NULL COMMENT '指导老师',
  `creator_id` varchar(20) NOT NULL COMMENT '创建人学号(外键)',
  `description` varchar(500) DEFAULT NULL COMMENT '社团简介',
  `max_members` int NOT NULL DEFAULT '50' COMMENT '最大成员数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-待审批, 1-正常, 2-已驳回',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-正常, 1-已解散',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`club_id`),
  KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团信息表';

-- 3. 社团成员表 (用户-社团 M:N)
CREATE TABLE `club_member` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `club_id` int NOT NULL COMMENT '社团编号(外键)',
  `student_id` varchar(20) NOT NULL COMMENT '学号(外键)',
  `position` varchar(50) NOT NULL DEFAULT '成员' COMMENT '职务：社长/副社长/部长/成员',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待审批, 1-已通过, 2-已退出',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_club_student` (`club_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团成员表';

-- 4. 活动表
CREATE TABLE `activity_info` (
  `activity_id` int NOT NULL AUTO_INCREMENT COMMENT '活动编号（主键）',
  `club_id` int NOT NULL COMMENT '主办社团编号(外键)',
  `applicant_id` varchar(20) NOT NULL COMMENT '申请人学号(外键)',
  `title` varchar(100) NOT NULL COMMENT '活动标题',
  `details` text NOT NULL COMMENT '活动详情',
  `event_time` datetime NOT NULL COMMENT '举办时间',
  `location` varchar(100) NOT NULL COMMENT '举办地点',
  `max_participants` int DEFAULT NULL COMMENT '最大参与人数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待审批, 1-已发布, 2-已结束, 3-已驳回',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`activity_id`),
  KEY `idx_club_id` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动信息表';

-- 5. 活动报名与签到表 (用户-活动 M:N)
CREATE TABLE `activity_registration` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` int NOT NULL COMMENT '活动编号(外键)',
  `student_id` varchar(20) NOT NULL COMMENT '学号(外键)',
  `reg_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `sign_in_status` tinyint NOT NULL DEFAULT '0' COMMENT '签到状态：0-未签到, 1-已签到',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_student` (`activity_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名签到表';

-- 6. 智能分析报告表
CREATE TABLE `ai_report` (
  `report_id` int NOT NULL AUTO_INCREMENT COMMENT '报告编号（主键）',
  `club_id` int DEFAULT NULL COMMENT '关联社团编号(全局分析为空)',
  `time_range` varchar(100) NOT NULL COMMENT '统计周期',
  `evaluation_and_suggestion` text NOT NULL COMMENT '大模型生成的评估与建议',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能分析报告表';

-- 初始管理员账号由 DataInitializer 在应用启动时自动创建（密码: admin123）

-- 7. 社团评价表
CREATE TABLE IF NOT EXISTS `club_comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `club_id` int NOT NULL COMMENT '社团编号(外键)',
  `student_id` varchar(20) NOT NULL COMMENT '评价人学号',
  `content` varchar(500) NOT NULL COMMENT '评价内容',
  `rating` tinyint NOT NULL DEFAULT '5' COMMENT '评分1-5',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_club_id` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团评价表';

-- 8. 站内信表
CREATE TABLE IF NOT EXISTS `sys_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` varchar(20) NOT NULL COMMENT '接收者学号',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` varchar(500) NOT NULL COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';
