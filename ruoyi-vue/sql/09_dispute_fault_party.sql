-- 争议表增加责任判定字段
-- 用于记录管理员人工仲裁时判定的责任方，供移动端展示
ALTER TABLE `tr_trade_dispute`
    ADD COLUMN `fault_party` VARCHAR(20) DEFAULT NULL COMMENT '责任判定：respondent/applicant/both/none' AFTER `handle_result`;
