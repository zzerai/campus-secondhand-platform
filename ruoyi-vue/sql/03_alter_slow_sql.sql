-- =====================================================
-- 校园二手闲置交易平台 - 慢SQL治理增量索引脚本
-- 适用项目：RuoYi-Vue + RuoYi-App
-- 文件用途：补建 message / favorite / goods 三表的复合索引，配套慢SQL治理
-- 执行前提：先跑过 01_ddl.sql 建表
-- 幂等性：通过 information_schema.STATISTICS 预检，已存在则跳过，可重复执行
--         （未使用 ADD INDEX IF NOT EXISTS 以兼容 MySQL 5.7 / 8.0 < 8.0.29）
--
-- 配套改动：
--   - 同步 01_ddl.sql 对应 CREATE TABLE 内 KEY 子句
--   - 见 doc/项目进度清单.md 附录九"慢SQL治理"
-- =====================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS pr_add_index_if_absent;

DELIMITER $$

CREATE PROCEDURE pr_add_index_if_absent(
    IN p_table  VARCHAR(64),
    IN p_index  VARCHAR(64),
    IN p_cols   VARCHAR(255)
)
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(1) INTO cnt
      FROM information_schema.STATISTICS
     WHERE table_schema = DATABASE()
       AND table_name   = p_table
       AND index_name   = p_index;
    IF cnt = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` (', p_cols, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- tr_trade_message：
--   idx_recv_unread 服务 countUnread 与 selectConversations 未读子查询
--   idx_conv        服务 selectConversationMessages、markConversationAsRead、selectConversations 内层聚合
CALL pr_add_index_if_absent('tr_trade_message', 'idx_recv_unread', '`receiver_id`, `read_status`, `del_flag`');
CALL pr_add_index_if_absent('tr_trade_message', 'idx_conv',        '`goods_id`, `sender_id`, `receiver_id`, `del_flag`, `create_time`');

-- tr_trade_favorite：服务 selectMyFavoriteGoods 的 where + order by update_time desc 避免 filesort
CALL pr_add_index_if_absent('tr_trade_favorite', 'idx_user_active', '`user_id`, `del_flag`, `update_time`');

-- tr_trade_goods：服务 selectAppGoodsList 移动端公开列表 where goods_status='1' and del_flag='0'
CALL pr_add_index_if_absent('tr_trade_goods', 'idx_status_active', '`goods_status`, `del_flag`, `goods_id`');

DROP PROCEDURE pr_add_index_if_absent;
