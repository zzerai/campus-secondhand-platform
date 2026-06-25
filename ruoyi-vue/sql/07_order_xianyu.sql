-- =====================================================
-- 校园二手闲置交易平台 - 订单流程改造（闲鱼模式）增量脚本
-- 文件用途：已部署环境的增量脚本，为 tr_trade_order 增加退款相关列、
--          更新 order_status / payment_status 注释为新枚举。
--          首次部署直接跑 01_ddl.sql 即含这些列，本脚本供存量库补建。
-- 幂等：列已存在则跳过；可重复执行。
-- 设计说明见 docs/订单流程改造方案-闲鱼模式.md
-- =====================================================

SET NAMES utf8mb4;

-- 用存储过程做"列不存在才新增"，保证可重复执行
DROP PROCEDURE IF EXISTS `tr_add_order_refund_columns`;
DELIMITER $$
CREATE PROCEDURE `tr_add_order_refund_columns`()
BEGIN
    DECLARE v_db VARCHAR(64);
    SET v_db = DATABASE();

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'refund_status') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `refund_status` CHAR(1) DEFAULT '0'
            COMMENT '退款状态：0无退款，1买家申请待卖家处理，2已同意退款中，3已退款，4卖家拒绝' AFTER `payment_status`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'refund_amount') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `refund_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额' AFTER `refund_status`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'refund_reason') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因' AFTER `refund_amount`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'refund_apply_time') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `refund_apply_time` DATETIME DEFAULT NULL COMMENT '退款申请时间' AFTER `refund_reason`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'refund_time') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `refund_time` DATETIME DEFAULT NULL COMMENT '退款成功时间' AFTER `refund_apply_time`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = v_db AND TABLE_NAME = 'tr_trade_order' AND COLUMN_NAME = 'alipay_refund_no') THEN
        ALTER TABLE `tr_trade_order`
            ADD COLUMN `alipay_refund_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝退款流水号' AFTER `refund_time`;
    END IF;
END$$
DELIMITER ;

CALL `tr_add_order_refund_columns`();
DROP PROCEDURE IF EXISTS `tr_add_order_refund_columns`;

-- 更新 order_status / payment_status 列注释为新枚举（重复执行无副作用）
ALTER TABLE `tr_trade_order`
    MODIFY COLUMN `order_status` CHAR(1) DEFAULT '0'
        COMMENT '订单状态：0待支付，2待收货/待交割，3已完成，4已取消，5争议中，6退款中，7已退款',
    MODIFY COLUMN `payment_status` CHAR(1) DEFAULT '0'
        COMMENT '支付状态：0未支付，1支付中，2支付成功，3支付失败，4已取消/退款';
