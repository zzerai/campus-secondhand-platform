-- ============================================
-- 清空 trade 模块所有业务数据
-- 警告：此操作会删除以下所有表的数据，不可恢复！
-- 执行前请务必备份数据库！
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. AI 相关表
-- ============================================
TRUNCATE TABLE tr_ai_audit_record;
TRUNCATE TABLE tr_ai_price_record;

-- ============================================
-- 2. 交易相关表（按依赖顺序，先子表后父表）
-- ============================================
-- 订单相关子表
TRUNCATE TABLE tr_trade_order_log;
TRUNCATE TABLE tr_trade_evaluation;

-- 消息和收藏
TRUNCATE TABLE tr_trade_message;
TRUNCATE TABLE tr_trade_favorite;

-- 商品相关子表
TRUNCATE TABLE tr_trade_goods_image;

-- 举报和争议（依赖订单和商品）
TRUNCATE TABLE tr_trade_report;
TRUNCATE TABLE tr_trade_dispute;

-- 订单和商品
TRUNCATE TABLE tr_trade_order;
TRUNCATE TABLE tr_trade_goods;

-- 信用分流水（封禁状态的唯一事实源，必须随学生数据一起清，否则留下孤儿记录、误触封禁逻辑）
TRUNCATE TABLE tr_credit_log;

-- ============================================
-- 3. 基础数据表（最后清理）
-- ============================================
-- 分类表保留种子数据，如需清空取消下面注释
-- TRUNCATE TABLE tr_trade_category;

-- 学生用户表
TRUNCATE TABLE tr_student_user;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 验证结果
-- ============================================
-- 验证所有业务表是否清空
SELECT 'tr_ai_audit_record' as 表名, COUNT(1) as 数量 FROM tr_ai_audit_record
UNION ALL SELECT 'tr_ai_price_record', COUNT(1) FROM tr_ai_price_record
UNION ALL SELECT 'tr_trade_order_log', COUNT(1) FROM tr_trade_order_log
UNION ALL SELECT 'tr_trade_evaluation', COUNT(1) FROM tr_trade_evaluation
UNION ALL SELECT 'tr_trade_message', COUNT(1) FROM tr_trade_message
UNION ALL SELECT 'tr_trade_favorite', COUNT(1) FROM tr_trade_favorite
UNION ALL SELECT 'tr_trade_goods_image', COUNT(1) FROM tr_trade_goods_image
UNION ALL SELECT 'tr_trade_report', COUNT(1) FROM tr_trade_report
UNION ALL SELECT 'tr_trade_dispute', COUNT(1) FROM tr_trade_dispute
UNION ALL SELECT 'tr_trade_order', COUNT(1) FROM tr_trade_order
UNION ALL SELECT 'tr_trade_goods', COUNT(1) FROM tr_trade_goods
UNION ALL SELECT 'tr_student_user', COUNT(1) FROM tr_student_user
UNION ALL SELECT 'tr_credit_log', COUNT(1) FROM tr_credit_log
UNION ALL SELECT 'tr_trade_category', COUNT(1) FROM tr_trade_category;