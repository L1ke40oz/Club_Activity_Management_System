-- ============================================================
-- 追加社团和活动数据
-- ============================================================
SET NAMES utf8mb4;
USE `campus_club_db`;

-- 追加社团
INSERT INTO `club_info` (`name`, `category`, `established_date`, `advisor`, `creator_id`, `description`, `max_members`, `status`, `is_deleted`) VALUES
('羽毛球协会', '体育运动', '2022-09-10', '林丹辉', '2024003', '每周训练加校内联赛欢迎各水平选手加入', 200, 1, 0),
('心理健康协会', '公益实践', '2021-05-25', '张心怡', '2024009', '关注大学生心理健康组织心理讲座和减压活动', 200, 1, 0),
('话剧表演社', '文化艺术', '2022-04-18', '陈导远', '2024011', '排练原创话剧和经典改编每学期一场公演', 200, 1, 0),
('网络安全社', '学术科技', '2023-03-01', '黄安全', '2024007', '研究网络安全技术参加CTF竞赛举办攻防演练', 200, 1, 0),
('乒乓球社', '体育运动', '2021-10-15', '马龙辉', '2024014', '日常训练加积分赛制定期邀请校队选手指导', 200, 1, 0),
('读书会', '人文社科', '2023-11-01', '苏文远', '2024015', '每月共读一本书线下讨论分享读书心得', 200, 1, 0),
('无人机社', '学术科技', '2023-07-15', '李航飞', '2024005', '无人机组装调试与飞行训练参加全国航模竞赛', 200, 1, 0),
('美食探店社', '生活休闲', '2024-03-01', '吴厨艺', '2024006', '每周组团探索校园周边美食撰写美食测评', 200, 1, 0),
('日语学习社', '人文社科', '2022-09-20', '田中老师', '2024012', '零基础日语入门动漫日语角备考经验分享', 200, 1, 0),
('电子音乐社', '文化艺术', '2023-12-10', '陈DJ', '2024008', '电子音乐制作教学校园电音派对', 200, 1, 0);

-- 追加活动（直接用club_id数值，请根据你的实际数据调整）
-- 先查一下新加的社团ID
SET @cid_ymq = (SELECT club_id FROM club_info WHERE name = '羽毛球协会' LIMIT 1);
SET @cid_xl = (SELECT club_id FROM club_info WHERE name = '心理健康协会' LIMIT 1);
SET @cid_hj = (SELECT club_id FROM club_info WHERE name = '话剧表演社' LIMIT 1);
SET @cid_aq = (SELECT club_id FROM club_info WHERE name = '网络安全社' LIMIT 1);
SET @cid_ppq = (SELECT club_id FROM club_info WHERE name = '乒乓球社' LIMIT 1);
SET @cid_ds = (SELECT club_id FROM club_info WHERE name = '读书会' LIMIT 1);
SET @cid_wrj = (SELECT club_id FROM club_info WHERE name = '无人机社' LIMIT 1);
SET @cid_ms = (SELECT club_id FROM club_info WHERE name = '美食探店社' LIMIT 1);
SET @cid_ry = (SELECT club_id FROM club_info WHERE name = '日语学习社' LIMIT 1);
SET @cid_dz = (SELECT club_id FROM club_info WHERE name = '电子音乐社' LIMIT 1);

-- 报名中的活动
INSERT INTO `activity_info` (`club_id`, `applicant_id`, `title`, `details`, `event_time`, `location`, `max_participants`, `status`, `is_deleted`) VALUES
(@cid_ymq, '2024003', '羽毛球新手训练营', '面向零基础同学教学基本握拍和发球技术', DATE_ADD(NOW(), INTERVAL 5 DAY), '体育馆羽毛球场', 20, 1, 0),
(@cid_xl, '2024009', '期末减压工作坊', '正念冥想加手工DIY帮你缓解考试焦虑', DATE_ADD(NOW(), INTERVAL 8 DAY), '心理咨询中心', 30, 1, 0),
(@cid_hj, '2024011', '话剧公演暗恋桃花源', '经典话剧改编演出免费观看', DATE_ADD(NOW(), INTERVAL 12 DAY), '活动中心剧场', 150, 1, 0),
(@cid_aq, '2024007', 'CTF新手入门赛', '校内CTF竞赛设Web和Crypto方向有奖品', DATE_ADD(NOW(), INTERVAL 6 DAY), '计算机学院实验楼', 40, 1, 0),
(@cid_ds, '2024015', '人类简史读书分享会', '共读人类简史第三部分每人准备分享', DATE_ADD(NOW(), INTERVAL 4 DAY), '图书馆讨论室', 15, 1, 0),
(@cid_wrj, '2024005', '无人机航拍体验日', '提供设备教学基本操控和航拍构图', DATE_ADD(NOW(), INTERVAL 9 DAY), '操场空旷区域', 12, 1, 0),
(@cid_ms, '2024006', '校园周边火锅测评', '集体探店三家火锅店投票评选最佳', DATE_ADD(NOW(), INTERVAL 3 DAY), '北门集合出发', 10, 1, 0),
(@cid_dz, '2024008', 'FL Studio制作入门', '从零开始学电子音乐制作带电脑参加', DATE_ADD(NOW(), INTERVAL 7 DAY), '音乐教室A201', 25, 1, 0),
(@cid_ry, '2024012', '动漫日语角第3期', '用经典动漫台词学日语语法', DATE_ADD(NOW(), INTERVAL 5 DAY), '外语楼302', 20, 1, 0),
(@cid_ppq, '2024014', '乒乓球积分挑战赛', '循环赛制积分排名前三有奖', DATE_ADD(NOW(), INTERVAL 10 DAY), '体育馆乒乓球室', 16, 1, 0);

-- 待审批活动
INSERT INTO `activity_info` (`club_id`, `applicant_id`, `title`, `details`, `event_time`, `location`, `max_participants`, `status`, `is_deleted`) VALUES
(@cid_ymq, '2024003', '校际羽毛球邀请赛', '邀请邻校羽毛球队友谊赛需选拔参赛', DATE_ADD(NOW(), INTERVAL 20 DAY), '体育馆', 30, 0, 0),
(@cid_xl, '2024009', '心理电影观影会', '观看心灵捕手映后讨论', DATE_ADD(NOW(), INTERVAL 15 DAY), '多媒体教室', 50, 0, 0);

-- 已结束活动
INSERT INTO `activity_info` (`club_id`, `applicant_id`, `title`, `details`, `event_time`, `location`, `max_participants`, `status`, `is_deleted`) VALUES
(@cid_aq, '2024007', '密码学基础讲座', '对称加密非对称加密和哈希函数原理', DATE_SUB(NOW(), INTERVAL 8 DAY), '计算机学院报告厅', 60, 2, 0),
(@cid_ds, '2024015', '百年孤独分享会', '魔幻现实主义经典作品深度解读', DATE_SUB(NOW(), INTERVAL 12 DAY), '图书馆讨论室', 15, 2, 0),
(@cid_ppq, '2024014', '新生乒乓球体验课', '教学基本站位和正手攻球零基础友好', DATE_SUB(NOW(), INTERVAL 6 DAY), '体育馆乒乓球室', 20, 2, 0);
