-- =====================================================
-- Trade statistics test data
-- Use case:
--   1. Test /trade/statistics/overview
--   2. Test /trade/statistics/category
--   3. Test /trade/statistics/orderTrend
--   4. Test /trade/statistics/paymentTrend
--
-- Before running:
--   1. Run ruoyi-vue/sql/01_ddl.sql and 02_seed.sql first
--   2. Ensure tr_trade_category has the default 6 category rows
--
-- Expected overview after import:
--   goodsTotal = 7
--   goodsPendingAudit = 2
--   goodsOnShelf = 4
--   orderTotal = 5
--   completedOrderCount = 2
--   totalTradeAmount = 101.00
--   studentUserTotal = 5
--   pendingReportCount = 2
--   pendingDisputeCount = 2
--
-- Notes:
--   1. todayNewOrderCount / todayNewUserCount depend on current date when you execute.
--   2. Trend interfaces are designed around the recent 7 days from CURDATE().
--   3. Order status follows current database design:
--      0 pending, 1 confirmed/awaiting payment, 2 paid/awaiting handover, 3 completed, 4 cancelled, 5 dispute
--   4. Dispute handle status follows current database design:
--      0 pending AI analysis, 1 AI analyzing, 2 awaiting manual arbitration, 3 resolved
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

SET @cat_textbook   := (SELECT category_id FROM tr_trade_category WHERE category_name = '教材' AND del_flag = '0' ORDER BY category_id LIMIT 1);
SET @cat_electronic := (SELECT category_id FROM tr_trade_category WHERE category_name = '电子产品' AND del_flag = '0' ORDER BY category_id LIMIT 1);
SET @cat_clothes    := (SELECT category_id FROM tr_trade_category WHERE category_name = '服饰' AND del_flag = '0' ORDER BY category_id LIMIT 1);
SET @cat_daily      := (SELECT category_id FROM tr_trade_category WHERE category_name = '生活用品' AND del_flag = '0' ORDER BY category_id LIMIT 1);
SET @cat_sports     := (SELECT category_id FROM tr_trade_category WHERE category_name = '运动器材' AND del_flag = '0' ORDER BY category_id LIMIT 1);
SET @cat_other      := (SELECT category_id FROM tr_trade_category WHERE category_name = '其他' AND del_flag = '0' ORDER BY category_id LIMIT 1);

DELETE FROM tr_trade_dispute WHERE dispute_id BETWEEN 95001 AND 95010;
DELETE FROM tr_trade_report WHERE report_id BETWEEN 94001 AND 94010;
DELETE FROM tr_trade_order_log WHERE order_id BETWEEN 93001 AND 93010;
DELETE FROM tr_trade_evaluation WHERE order_id BETWEEN 93001 AND 93010;
DELETE FROM tr_trade_message WHERE order_id BETWEEN 93001 AND 93010 OR goods_id BETWEEN 92001 AND 92010;
DELETE FROM tr_trade_favorite WHERE goods_id BETWEEN 92001 AND 92010 OR user_id BETWEEN 90001 AND 90010;
DELETE FROM tr_trade_order WHERE order_id BETWEEN 93001 AND 93010;
DELETE FROM tr_trade_goods_image WHERE goods_id BETWEEN 92001 AND 92010;
DELETE FROM tr_trade_goods WHERE goods_id BETWEEN 92001 AND 92010;
DELETE FROM tr_student_user WHERE user_id BETWEEN 90001 AND 90010;

INSERT INTO tr_student_user
(
    user_id, student_no, phone, password, nickname, avatar, contact_way, credit_score, status, last_login_time,
    create_by, create_time, update_by, update_time, del_flag, remark
)
VALUES
(
    90001, 'TS2026051901', '19990091001', '$2a$10$7sN6wJ0WmM8S0P0J7f2kRO5xJ8GvL8vW6mIuB8L1mB9tR0u8aPp0a',
    'stat_user_a', '', 'wechat:test01', 100, '0', DATE_SUB(NOW(), INTERVAL 1 DAY),
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, '0', 'statistics seed data'
),
(
    90002, 'TS2026051902', '19990091002', '$2a$10$7sN6wJ0WmM8S0P0J7f2kRO5xJ8GvL8vW6mIuB8L1mB9tR0u8aPp0a',
    'stat_user_b', '', 'qq:test02', 98, '0', DATE_SUB(NOW(), INTERVAL 2 DAY),
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, '0', 'statistics seed data'
),
(
    90003, 'TS2026051903', '19990091003', '$2a$10$7sN6wJ0WmM8S0P0J7f2kRO5xJ8GvL8vW6mIuB8L1mB9tR0u8aPp0a',
    'stat_user_c', '', 'mobile:test03', 96, '0', DATE_SUB(NOW(), INTERVAL 3 DAY),
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 11 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 11 HOUR, '0', 'statistics seed data'
),
(
    90004, 'TS2026051904', '19990091004', '$2a$10$7sN6wJ0WmM8S0P0J7f2kRO5xJ8GvL8vW6mIuB8L1mB9tR0u8aPp0a',
    'stat_user_d', '', 'mail:test04', 100, '0', NOW(),
    'statistics_test', CURDATE() + INTERVAL 8 HOUR,
    'statistics_test', CURDATE() + INTERVAL 8 HOUR, '0', 'statistics seed data'
),
(
    90005, 'TS2026051905', '19990091005', '$2a$10$7sN6wJ0WmM8S0P0J7f2kRO5xJ8GvL8vW6mIuB8L1mB9tR0u8aPp0a',
    'stat_user_e', '', 'phone:test05', 97, '0', NOW(),
    'statistics_test', CURDATE() + INTERVAL 9 HOUR,
    'statistics_test', CURDATE() + INTERVAL 9 HOUR, '0', 'statistics seed data'
);

INSERT INTO tr_trade_goods
(
    goods_id, seller_id, category_id, title, price, original_price, quality, description, trade_place, contact_way,
    goods_status, audit_user_id, audit_time, audit_remark, view_count, favorite_count,
    create_by, create_time, update_by, update_time, del_flag, remark
)
VALUES
(
    92001, 90001, @cat_textbook, 'stat-textbook', 35.00, 68.00, '8new', 'statistics textbook item',
    'library_gate', 'wechat:test01', '1', 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, 'approved', 12, 2,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, '0', 'statistics seed data'
),
(
    92002, 90002, @cat_electronic, 'stat-earphone', 128.00, 299.00, '9new', 'statistics electronic item',
    'dormitory_downstairs', 'qq:test02', '1', 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 13 HOUR, 'approved', 20, 5,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 10 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 13 HOUR, '0', 'statistics seed data'
),
(
    92003, 90003, @cat_clothes, 'stat-hoodie', 45.00, 139.00, '7new', 'statistics clothes item',
    'canteen_gate', 'mobile:test03', '0', NULL, NULL, NULL, 5, 0,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 11 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 11 HOUR, '0', 'statistics seed data'
),
(
    92004, 90004, @cat_daily, 'stat-lamp', 25.00, 59.00, '8new', 'statistics daily item',
    'teaching_building_1f', 'mail:test04', '0', NULL, NULL, NULL, 3, 0,
    'statistics_test', CURDATE() + INTERVAL 8 HOUR,
    'statistics_test', CURDATE() + INTERVAL 8 HOUR, '0', 'statistics seed data'
),
(
    92005, 90004, @cat_sports, 'stat-racket', 88.00, 169.00, '9new', 'statistics sports item',
    'playground_stand', 'mail:test04', '1', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR, 'approved', 8, 1,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 9 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR, '0', 'statistics seed data'
),
(
    92006, 90003, @cat_other, 'stat-chair', 66.00, 120.00, '8new', 'statistics sold item',
    'dormitory_1f', 'mobile:test03', '4', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, 'sold', 15, 3,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 20 HOUR, '0', 'statistics seed data'
),
(
    92007, 90005, @cat_other, 'stat-box', 52.00, 99.00, '9new', 'statistics dispute item',
    'parcel_station_side', 'phone:test05', '1', 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, 'approved', 9, 1,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, '0', 'statistics seed data'
);

INSERT INTO tr_trade_order
(
    order_id, order_no, goods_id, buyer_id, seller_id, trade_price, trade_place, appointment_time,
    order_status, payment_status, buyer_remark, seller_remark, cancel_reason,
    confirm_time, pay_time, alipay_trade_no, payment_amount, payment_time, complete_time, cancel_time,
    create_by, create_time, update_by, update_time, del_flag, remark
)
VALUES
(
    93001, 'STAT20260519001', 92001, 90002, 90001, 35.00, 'library_gate',
    DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 18 HOUR,
    '3', '2', 'fast_trade', 'ok', NULL,
    DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 9 HOUR,
    'ALI_STAT_93001',
    35.00,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 9 HOUR,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 20 HOUR,
    NULL,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 10 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 20 HOUR, '0', 'statistics seed data'
),
(
    93002, 'STAT20260519002', 92002, 90003, 90002, 128.00, 'dormitory_downstairs',
    DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 17 HOUR,
    '4', '4', 'small_discount', 'checked', 'buyer_cancelled',
    DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 15 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 11 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 15 HOUR, '0', 'statistics seed data'
),
(
    93003, 'STAT20260519003', 92006, 90004, 90003, 66.00, 'dormitory_1f',
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 18 HOUR,
    '3', '2', 'night_trade', 'ok', NULL,
    DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,
    'ALI_STAT_93003',
    66.00,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 20 HOUR,
    NULL,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 20 HOUR, '0', 'statistics seed data'
),
(
    93004, 'STAT20260519004', 92005, 90005, 90004, 88.00, 'playground_stand',
    CURDATE() + INTERVAL 19 HOUR,
    '0', '0', 'available_today', NULL, NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    'statistics_test', CURDATE() + INTERVAL 9 HOUR,
    'statistics_test', CURDATE() + INTERVAL 9 HOUR, '0', 'statistics seed data'
),
(
    93005, 'STAT20260519005', 92007, 90001, 90005, 52.00, 'parcel_station_side',
    CURDATE() + INTERVAL 20 HOUR,
    '5', '2', 'paid_but_dispute', 'waiting_handle', NULL,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 13 HOUR,
    CURDATE() + INTERVAL 10 HOUR,
    'ALI_STAT_93005',
    52.00,
    CURDATE() + INTERVAL 10 HOUR,
    NULL,
    NULL,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR,
    'statistics_test', CURDATE() + INTERVAL 10 HOUR, '0', 'statistics seed data'
);

INSERT INTO tr_trade_report
(
    report_id, goods_id, order_id, report_user_id, reported_user_id, report_type, report_content, evidence_images,
    handle_status, handle_user_id, handle_time, handle_result,
    create_by, create_time, update_by, update_time, del_flag, remark
)
VALUES
(
    94001, 92002, 93002, 90003, 90002, 'price_fraud', 'pending_report_1', '',
    '0', NULL, NULL, NULL,
    'statistics_test', CURDATE() + INTERVAL 10 HOUR,
    'statistics_test', CURDATE() + INTERVAL 10 HOUR, '0', 'statistics seed data'
),
(
    94002, 92007, 93005, 90001, 90005, 'trade_dispute', 'pending_report_2', '',
    '0', NULL, NULL, NULL,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 16 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 16 HOUR, '0', 'statistics seed data'
),
(
    94003, 92001, 93001, 90002, 90001, 'other', 'handled_report', '',
    '1', 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 15 HOUR, 'handled',
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 15 HOUR, '0', 'statistics seed data'
);

INSERT INTO tr_trade_dispute
(
    dispute_id, order_id, goods_id, applicant_id, respondent_id, dispute_type, dispute_content, evidence_images,
    ai_analysis, handle_status, handle_user_id, handle_time, handle_result,
    create_by, create_time, update_by, update_time, del_flag, remark
)
VALUES
(
    95001, 93005, 92007, 90001, 90005, 'goods_not_match', 'pending_dispute', '',
    'need_manual_review', '0', NULL, NULL, NULL,
    'statistics_test', CURDATE() + INTERVAL 11 HOUR,
    'statistics_test', CURDATE() + INTERVAL 11 HOUR, '0', 'statistics seed data'
),
(
    95002, 93002, 92002, 90003, 90002, 'not_delivered', 'processing_dispute', '',
    'continue_investigate', '1', 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR, 'processing',
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,
    'statistics_test', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR, '0', 'statistics seed data'
);

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- Optional quick verification SQL
-- SELECT COUNT(1) AS goods_total FROM tr_trade_goods WHERE del_flag = '0';
-- SELECT COUNT(1) AS goods_pending_audit FROM tr_trade_goods WHERE del_flag = '0' AND goods_status = '0';
-- SELECT COUNT(1) AS goods_on_shelf FROM tr_trade_goods WHERE del_flag = '0' AND goods_status = '1';
-- SELECT COUNT(1) AS order_total FROM tr_trade_order WHERE del_flag = '0';
-- SELECT COUNT(1) AS completed_order_count FROM tr_trade_order WHERE del_flag = '0' AND order_status = '3';
-- SELECT COALESCE(SUM(COALESCE(payment_amount, trade_price)), 0) AS total_trade_amount FROM tr_trade_order WHERE del_flag = '0' AND order_status = '3';
-- SELECT COUNT(1) AS pending_report_count FROM tr_trade_report WHERE del_flag = '0' AND handle_status = '0';
-- SELECT COUNT(1) AS pending_dispute_count FROM tr_trade_dispute WHERE del_flag = '0' AND handle_status IN ('0', '1');
