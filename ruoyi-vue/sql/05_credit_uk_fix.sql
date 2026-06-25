-- =====================================================
-- 校园二手闲置交易平台 - tr_credit_log 幂等键修正（PR2 前置）
-- 背景：PR1 的 uk_event=(biz_type,biz_id,change_type) 缺少 user_id，导致同一业务事件
--       影响多个用户时（如订单完成买卖双方各 +1）第二条插入撞唯一键。
-- 修正：uk_event 改为 (user_id, biz_type, biz_id, change_type)，按"用户+事件"去重。
-- 幂等：通过 information_schema.STATISTICS 预检索引存在性，可重复执行。
-- 执行前提：已建 tr_credit_log（01_ddl.sql 或 04_credit.sql）。
-- =====================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS pr_fix_credit_uk;

DELIMITER $$

CREATE PROCEDURE pr_fix_credit_uk()
BEGIN
    DECLARE old_cols INT DEFAULT 0;
    DECLARE has_index INT DEFAULT 0;

    -- 旧索引特征：uk_event 不含 user_id 列
    SELECT COUNT(1) INTO has_index
      FROM information_schema.STATISTICS
     WHERE table_schema = DATABASE() AND table_name = 'tr_credit_log' AND index_name = 'uk_event';

    SELECT COUNT(1) INTO old_cols
      FROM information_schema.STATISTICS
     WHERE table_schema = DATABASE() AND table_name = 'tr_credit_log'
       AND index_name = 'uk_event' AND column_name = 'user_id';

    -- 已是新索引（含 user_id）则跳过；否则丢弃旧索引重建
    IF has_index > 0 AND old_cols = 0 THEN
        ALTER TABLE `tr_credit_log` DROP INDEX `uk_event`;
        SET has_index = 0;
    END IF;
    IF has_index = 0 THEN
        ALTER TABLE `tr_credit_log`
            ADD UNIQUE KEY `uk_event` (`user_id`, `biz_type`, `biz_id`, `change_type`);
    END IF;
END$$

DELIMITER ;

CALL pr_fix_credit_uk();

DROP PROCEDURE pr_fix_credit_uk;
