-- =====================================================
-- 校园二手闲置交易平台 - DDL 业务表结构
-- 适用项目：RuoYi-Vue + RuoYi-App
-- 文件用途：仅包含 DROP/CREATE TABLE，业务初始数据见 02_seed.sql
-- ⚠️  危险：执行本脚本会 DROP 所有 tr_trade_* 业务表，丢失全部业务数据。
--         仅在首次部署或显式重置环境时执行，生产环境必须先备份。
-- 设计约束：所有业务删除均使用 del_flag 逻辑删除，禁止物理删除
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 学生用户表
-- 说明：移动端学生用户，不与 RuoYi 后台 sys_user 混用
-- =====================================================
DROP TABLE IF EXISTS `tr_student_user`;
CREATE TABLE `tr_student_user` (
                                   `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生用户ID',
                                   `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
                                   `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                                   `password` VARCHAR(100) NOT NULL COMMENT '密码',
                                   `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                                   `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
                                   `contact_way` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
                                   `credit_score` INT DEFAULT 100 COMMENT '信用分',
                                   `status` CHAR(1) DEFAULT '0' COMMENT '账号状态：0正常，1临时封禁，2永久封禁',
                                   `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',

                                   `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                   `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                   `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                   `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                   PRIMARY KEY (`user_id`),
                                   UNIQUE KEY `uk_student_no` (`student_no`),
                                   UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='学生用户表';


-- =====================================================
-- 2. 商品分类表
-- 说明：教材、电子产品、服饰、生活用品、运动器材、其他
-- 你现在代码生成里已有 tr_trade_category，可以保留此表名
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_category`;
CREATE TABLE `tr_trade_category` (
                                     `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                     `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
                                     `parent_id` BIGINT DEFAULT 0 COMMENT '父级分类ID',
                                     `sort` INT DEFAULT 0 COMMENT '排序',
                                     `status` CHAR(1) DEFAULT '0' COMMENT '状态：0正常，1停用',

                                     `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                     `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                     `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                     `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                     PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';


-- =====================================================
-- 3. 闲置商品表
-- 商品状态 goods_status：
-- 0 待审核
-- 1 已上架
-- 2 审核拒绝
-- 3 已下架
-- 4 已售出
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_goods`;
CREATE TABLE `tr_trade_goods` (
                                  `goods_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                                  `seller_id` BIGINT NOT NULL COMMENT '卖家学生用户ID',
                                  `category_id` BIGINT NOT NULL COMMENT '商品分类ID',

                                  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
                                  `price` DECIMAL(10,2) NOT NULL COMMENT '出售价格',
                                  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
                                  `quality` VARCHAR(20) DEFAULT NULL COMMENT '新旧程度',
                                  `description` TEXT COMMENT '商品描述',
                                  `trade_place` VARCHAR(255) DEFAULT NULL COMMENT '建议交易地点',
                                  `contact_way` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',

                                  `goods_status` CHAR(1) DEFAULT '0' COMMENT '商品状态：0待审核，1已上架，2审核拒绝，3已下架，4已售出',
                                  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
                                  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
                                  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',

                                  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
                                  `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',

                                  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                  PRIMARY KEY (`goods_id`),
                                  KEY `idx_seller_id` (`seller_id`),
                                  KEY `idx_category_id` (`category_id`),
                                  KEY `idx_goods_status` (`goods_status`),
                                  KEY `idx_create_time` (`create_time`),
                                  KEY `idx_status_active` (`goods_status`, `del_flag`, `goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='闲置商品表';


-- =====================================================
-- 4. 商品图片表
-- 说明：一个商品最多 9 张图片，业务层校验
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_goods_image`;
CREATE TABLE `tr_trade_goods_image` (
                                        `image_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
                                        `goods_id` BIGINT NOT NULL COMMENT '商品ID',
                                        `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
                                        `sort` INT DEFAULT 0 COMMENT '排序',

                                        `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                        `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                        `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                        `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                        `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                        `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                        PRIMARY KEY (`image_id`),
                                        KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';


-- =====================================================
-- 5. 交易订单表
-- 订单状态 order_status：
-- 0 待确认
-- 1 已确认/待支付
-- 2 已支付/待交割
-- 3 已完成
-- 4 已取消
-- 5 争议中
--
-- 支付状态 payment_status：
-- 0 未支付
-- 1 待支付
-- 2 支付成功
-- 3 支付失败
-- 4 已取消/退款
--
-- 当前流程：交割货物后由买家点击完成
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_order`;
CREATE TABLE `tr_trade_order` (
                                  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                                  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',

                                  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
                                  `buyer_id` BIGINT NOT NULL COMMENT '买家学生用户ID',
                                  `seller_id` BIGINT NOT NULL COMMENT '卖家学生用户ID',

                                  `goods_title` VARCHAR(255) DEFAULT NULL COMMENT '下单时商品标题快照',
                                  `goods_images` VARCHAR(2000) DEFAULT NULL COMMENT '下单时商品图片快照（多图逗号分隔URL）',

                                  `trade_price` DECIMAL(10,2) NOT NULL COMMENT '交易价格',
                                  `trade_method` VARCHAR(20) DEFAULT NULL COMMENT '交易方式：offline线下交易',
                                  `trade_place` VARCHAR(255) DEFAULT NULL COMMENT '约定交易地点',
                                  `appointment_time` DATETIME DEFAULT NULL COMMENT '预约交易时间',

                                  `order_status` CHAR(1) DEFAULT '0' COMMENT '订单状态：0待支付，2待收货/待交割，3已完成，4已取消，5争议中，6退款中，7已退款',
                                  `payment_status` CHAR(1) DEFAULT '0' COMMENT '支付状态：0未支付，1支付中，2支付成功，3支付失败，4已取消/退款',
                                  `refund_status` CHAR(1) DEFAULT '0' COMMENT '退款状态：0无退款，1买家申请待卖家处理，2已同意退款中，3已退款，4卖家拒绝',
                                  `refund_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额',
                                  `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
                                  `refund_apply_time` DATETIME DEFAULT NULL COMMENT '退款申请时间',
                                  `refund_time` DATETIME DEFAULT NULL COMMENT '退款成功时间',
                                  `alipay_refund_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝退款流水号',

                                  `buyer_remark` VARCHAR(500) DEFAULT NULL COMMENT '买家备注',
                                  `seller_remark` VARCHAR(500) DEFAULT NULL COMMENT '卖家备注',
                                  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',

                                  `confirm_time` DATETIME DEFAULT NULL COMMENT '卖家确认时间',
                                  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
                                  `alipay_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易流水号',
                                  `payment_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实际支付金额',
                                  `payment_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
                                  `complete_time` DATETIME DEFAULT NULL COMMENT '买家确认完成时间',
                                  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',

                                  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                  PRIMARY KEY (`order_id`),
                                  UNIQUE KEY `uk_order_no` (`order_no`),
                                  KEY `idx_goods_id` (`goods_id`),
                                  KEY `idx_buyer_id` (`buyer_id`),
                                  KEY `idx_seller_id` (`seller_id`),
                                  KEY `idx_order_status` (`order_status`),
                                  KEY `idx_payment_status` (`payment_status`),
                                  KEY `idx_create_time` (`create_time`),
                                  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';


-- =====================================================
-- 6. 订单操作日志表
-- 说明：记录订单状态流转，便于审计和排查问题
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_order_log`;
CREATE TABLE `tr_trade_order_log` (
                                      `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                      `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                      `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
                                      `operator_type` CHAR(1) DEFAULT NULL COMMENT '操作人类型：1买家，2卖家，3管理员',
                                      `before_status` CHAR(1) DEFAULT NULL COMMENT '操作前订单状态',
                                      `after_status` CHAR(1) DEFAULT NULL COMMENT '操作后订单状态',
                                      `operation_type` VARCHAR(50) DEFAULT NULL COMMENT '操作类型：create/confirm/cancel/pay/complete/dispute/handle',
                                      `operation_content` VARCHAR(1000) DEFAULT NULL COMMENT '操作说明',

                                      `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                      `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                      `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                      `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                      `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                      `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                      PRIMARY KEY (`log_id`),
                                      KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';


-- =====================================================
-- 7. 举报信息表
-- 处理状态 handle_status：
-- 0 待处理
-- 1 已处理
-- 2 已驳回
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_report`;
CREATE TABLE `tr_trade_report` (
                                   `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',

                                   `goods_id` BIGINT DEFAULT NULL COMMENT '被举报商品ID',
                                   `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
                                   `report_user_id` BIGINT NOT NULL COMMENT '举报人ID',
                                   `reported_user_id` BIGINT DEFAULT NULL COMMENT '被举报人ID',

                                   `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：虚假信息/违禁品/价格欺诈/交易纠纷/其他',
                                   `report_content` VARCHAR(1000) DEFAULT NULL COMMENT '举报内容',
                                   `evidence_images` VARCHAR(1000) DEFAULT NULL COMMENT '证据图片，多个用逗号分隔',

                                   `handle_status` CHAR(1) DEFAULT '0' COMMENT '处理状态：0待处理，1已处理，2已驳回',
                                   `handle_user_id` BIGINT DEFAULT NULL COMMENT '处理管理员ID',
                                   `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
                                   `handle_result` VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',

                                   `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                   `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                   `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                   `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                   PRIMARY KEY (`report_id`),
                                   KEY `idx_goods_id` (`goods_id`),
                                   KEY `idx_order_id` (`order_id`),
                                   KEY `idx_report_user_id` (`report_user_id`),
                                   KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='举报信息表';


-- =====================================================
-- 8. 交易争议处理表
-- 说明：买家发起争议，AI 可辅助分析，管理员人工处理
-- 处理状态 handle_status：
-- 0 待AI分析
-- 1 AI分析中
-- 2 等待人工仲裁
-- 3 已处理
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_dispute`;
CREATE TABLE `tr_trade_dispute` (
                                    `dispute_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '争议ID',
                                    `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                    `goods_id` BIGINT DEFAULT NULL COMMENT '商品ID',

                                    `applicant_id` BIGINT NOT NULL COMMENT '发起人ID',
                                    `respondent_id` BIGINT DEFAULT NULL COMMENT '被申诉人ID',
                                    `dispute_type` VARCHAR(50) DEFAULT NULL COMMENT '争议类型：未交货/商品不符/付款问题/其他',
                                    `dispute_content` VARCHAR(1000) DEFAULT NULL COMMENT '争议描述',
                                    `evidence_images` VARCHAR(1000) DEFAULT NULL COMMENT '证据图片，多个用逗号分隔',

                                    `ai_analysis` TEXT COMMENT 'AI仲裁分析结果',
                                    `handle_status` CHAR(1) DEFAULT '0' COMMENT '处理状态：0待AI分析，1AI分析中，2等待人工仲裁，3已处理',
                                    `handle_user_id` BIGINT DEFAULT NULL COMMENT '处理管理员ID',
                                    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
                                    `handle_result` VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',

                                    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                    PRIMARY KEY (`dispute_id`),
                                    KEY `idx_order_id` (`order_id`),
                                    KEY `idx_applicant_id` (`applicant_id`),
                                    KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='交易争议处理表';


-- =====================================================
-- 9. AI审核记录表
-- 说明：商品审核、举报、争议均可记录 AI 调用结果
-- =====================================================
DROP TABLE IF EXISTS `tr_ai_audit_record`;
CREATE TABLE `tr_ai_audit_record` (
                                      `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI审核记录ID',
                                      `business_id` BIGINT DEFAULT NULL COMMENT '业务ID，如商品ID或争议ID',
                                      `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型：goods商品/dispute争议/report举报',

                                      `input_content` TEXT COMMENT 'AI输入内容',
                                      `ai_result` TEXT COMMENT 'AI返回完整结果',
                                      `risk_level` VARCHAR(20) DEFAULT NULL COMMENT '风险等级：low低，middle中，high高',
                                      `suggestion` VARCHAR(100) DEFAULT NULL COMMENT '审核建议：通过/拒绝/人工复核',
                                      `risk_reason` VARCHAR(1000) DEFAULT NULL COMMENT '风险原因',

                                      `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                      `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                      `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                      `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                      `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                      `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                      PRIMARY KEY (`record_id`),
                                      KEY `idx_business_id` (`business_id`),
                                      KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI审核记录表';


-- =====================================================
-- 10. AI估价记录表
-- 说明：根据商品标题、分类、新旧程度、描述生成参考价格
-- =====================================================
DROP TABLE IF EXISTS `tr_ai_price_record`;
CREATE TABLE `tr_ai_price_record` (
                                      `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI估价记录ID',
                                      `goods_id` BIGINT DEFAULT NULL COMMENT '商品ID',
                                      `title` VARCHAR(100) DEFAULT NULL COMMENT '商品标题',
                                      `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称',
                                      `quality` VARCHAR(20) DEFAULT NULL COMMENT '新旧程度',
                                      `description` TEXT COMMENT '商品描述',

                                      `suggest_price` DECIMAL(10,2) DEFAULT NULL COMMENT 'AI建议价格',
                                      `price_reason` TEXT COMMENT '估价理由',
                                      `ai_result` TEXT COMMENT 'AI返回完整结果',

                                      `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                      `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                      `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                      `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                      `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                      `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                      PRIMARY KEY (`record_id`),
                                      KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI估价记录表';


-- =====================================================
-- 11. 商品收藏表
-- 说明：选做功能；如果暂时不做收藏，也可以先保留表
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_favorite`;
CREATE TABLE `tr_trade_favorite` (
                                     `favorite_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                     `user_id` BIGINT NOT NULL COMMENT '学生用户ID',
                                     `goods_id` BIGINT NOT NULL COMMENT '商品ID',

                                     `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                     `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                     `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                     `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                     PRIMARY KEY (`favorite_id`),
                                     UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
                                     KEY `idx_user_id` (`user_id`),
                                     KEY `idx_goods_id` (`goods_id`),
                                     KEY `idx_user_active` (`user_id`, `del_flag`, `update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';


-- =====================================================
-- 12. 交易评价表
-- 说明：选做功能；订单完成后双方互评
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_evaluation`;
CREATE TABLE `tr_trade_evaluation` (
                                       `evaluation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
                                       `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                       `from_user_id` BIGINT NOT NULL COMMENT '评价人ID',
                                       `to_user_id` BIGINT NOT NULL COMMENT '被评价人ID',
                                       `score` INT NOT NULL COMMENT '评分：1-5',
                                       `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',

                                       `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                       `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                       `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                       `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                       `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                       `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                       PRIMARY KEY (`evaluation_id`),
                                       KEY `idx_order_id` (`order_id`),
                                       KEY `idx_from_user_id` (`from_user_id`),
                                       KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='交易评价表';


-- =====================================================
-- 13. 私信消息表
-- 说明：选做功能；买卖双方文字沟通
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_message`;
CREATE TABLE `tr_trade_message` (
                                    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                    `goods_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
                                    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
                                    `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
                                    `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
                                    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
                                    `read_status` CHAR(1) DEFAULT '0' COMMENT '阅读状态：0未读，1已读',

                                    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                    PRIMARY KEY (`message_id`),
                                    KEY `idx_sender_id` (`sender_id`),
                                    KEY `idx_receiver_id` (`receiver_id`),
                                    KEY `idx_goods_id` (`goods_id`),
                                    KEY `idx_order_id` (`order_id`),
                                    KEY `idx_recv_unread` (`receiver_id`, `read_status`, `del_flag`),
                                    KEY `idx_conv` (`goods_id`, `sender_id`, `receiver_id`, `del_flag`, `create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';


-- =====================================================
-- 14. 交易公告表
-- 说明：管理端发布交易相关公告，移动端可能展示（待定）
-- =====================================================
DROP TABLE IF EXISTS `tr_trade_announcement`;
CREATE TABLE `tr_trade_announcement` (
                                        `announcement_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
                                        `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
                                        `content` TEXT COMMENT '公告内容（支持富文本/HTML）',
                                        `type` CHAR(1) DEFAULT '1' COMMENT '公告类型：1活动通知，2规则变更，3维护提醒',
                                        `is_top` CHAR(1) DEFAULT '0' COMMENT '是否置顶：0否，1是',
                                        `publish_status` CHAR(1) DEFAULT '0' COMMENT '发布状态：0草稿，1已发布',
                                        `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
                                        `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图地址',

                                        `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                        `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
                                        `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                        `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
                                        `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志：0存在，2删除',
                                        `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                        PRIMARY KEY (`announcement_id`),
                                        KEY `idx_publish_time` (`publish_time`),
                                        KEY `idx_is_top` (`is_top`),
                                        KEY `idx_publish_status` (`publish_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='交易公告表';


-- =====================================================
-- 14. 学生信用分变动流水表
-- 说明：追加写、只增不改，信用分唯一事实源；tr_student_user.credit_score 为物化当前值。
--      封禁次数由本表 change_type='auto_ban' 行数推导，封禁到期存于 auto_ban 行的 ban_until。
-- =====================================================
DROP TABLE IF EXISTS `tr_credit_log`;
CREATE TABLE `tr_credit_log` (
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


SET FOREIGN_KEY_CHECKS = 1;
