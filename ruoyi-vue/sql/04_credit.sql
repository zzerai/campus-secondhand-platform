-- =====================================================
-- 校园二手闲置交易平台 - 学生信用分模块（PR1 信用引擎）
-- 文件用途：已部署环境的增量脚本，仅新建 tr_credit_log + 注入 sys_config 参数，
--          不改动任何现有表结构（status 列复用，新增取值 '2' 无需 ALTER）。
-- 可重复执行（幂等）。首次部署直接跑 01_ddl.sql 即含该表，本脚本供存量库补建。
-- =====================================================

SET NAMES utf8mb4;

-- 1) 信用分变动流水表（不存在才建）
CREATE TABLE IF NOT EXISTS `tr_credit_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '学生用户ID',
    `change_type` VARCHAR(32) NOT NULL COMMENT '变动类型：admin_adjust/order_complete/report_valid/dispute_fault/order_cancel/evaluation/auto_ban/ban_release',
    `change_value` INT NOT NULL DEFAULT 0 COMMENT '本次增减(可负)',
    `score_before` INT NOT NULL COMMENT '变动前分值',
    `score_after` INT NOT NULL COMMENT '变动后分值',
    `biz_type` VARCHAR(32) DEFAULT NULL COMMENT '关联业务：order/report/dispute/evaluation/admin/system',
    `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务主键',
    `ban_until` DATETIME DEFAULT NULL COMMENT '封禁到期(仅auto_ban行有值; 永久封禁为NULL)',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '原因/管理员备注',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`log_id`),
    UNIQUE KEY `uk_event` (`user_id`, `biz_type`, `biz_id`, `change_type`),
    KEY `idx_user` (`user_id`, `change_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='学生信用分变动流水';

-- 2) 封禁参数注入 sys_config（已存在则跳过；缺失时代码侧也有默认值 60/7/30 兜底）
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '信用分-封禁阈值', 'credit.ban.threshold', '60', 'Y', 'system', NOW(), '信用分低于该值触发封禁'
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'credit.ban.threshold');

INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '信用分-首次封禁天数', 'credit.ban.first.days', '7', 'Y', 'system', NOW(), '第1次跌破阈值的临时封禁天数'
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'credit.ban.first.days');

INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '信用分-第二次封禁天数', 'credit.ban.second.days', '30', 'Y', 'system', NOW(), '第2次跌破阈值的临时封禁天数（第3次永久）'
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'credit.ban.second.days');
