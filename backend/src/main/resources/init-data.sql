-- ============================================================
-- 校园社团活动管理系统 - 数据重置脚本
-- 
-- 使用方法：
-- 1. 停止后端应用
-- 2. 在MySQL中执行此脚本（清空所有数据）
-- 3. 重启后端应用（DataInitializer会自动填充丰富的测试数据）
--
-- 管理员账号: admin / admin123
-- 普通用户: 2024001~2024015 / 123456
-- ============================================================
USE `campus_club_db`;

-- 清空数据（按外键依赖顺序）
DELETE FROM `activity_registration`;
DELETE FROM `activity_info`;
DELETE FROM `club_member`;
DELETE FROM `club_info`;
DELETE FROM `ai_report`;
DELETE FROM `sys_user`;

-- 重置自增ID
ALTER TABLE `club_info` AUTO_INCREMENT = 1;
ALTER TABLE `activity_info` AUTO_INCREMENT = 1;
ALTER TABLE `club_member` AUTO_INCREMENT = 1;
ALTER TABLE `activity_registration` AUTO_INCREMENT = 1;
ALTER TABLE `ai_report` AUTO_INCREMENT = 1;

-- 执行完毕后重启后端，数据将自动填充
