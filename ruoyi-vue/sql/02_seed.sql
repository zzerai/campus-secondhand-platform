-- =====================================================
-- 校园二手闲置交易平台 - 种子/初始化数据
-- 适用项目：RuoYi-Vue + RuoYi-App
-- 文件用途：业务表分类种子 + 后台菜单 + 状态字典，不含任何"测试数据"
-- 执行前提：先跑过 01_ddl.sql 建表，且 RuoYi 主库 (ry_20260417.sql) 已就绪
-- 幂等性：
--   - sys_menu：INSERT IGNORE，主键冲突自动跳过，可重复执行
--   - sys_dict_type：INSERT IGNORE，dict_type 唯一约束保证不重复
--   - sys_dict_data：先 DELETE 再 INSERT，保证每次跑都是干净的 5 条
--   - tr_trade_category：仅在表为空时插入，避免重复种子
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 初始化商品分类（仅当表中无任何分类时执行）
-- =====================================================
INSERT INTO `tr_trade_category`
(`category_name`, `parent_id`, `sort`, `status`, `create_by`, `create_time`, `del_flag`, `remark`)
SELECT * FROM (
    SELECT '教材'     AS category_name, 0 AS parent_id, 1 AS sort, '0' AS status, 'admin' AS create_by, NOW() AS create_time, '0' AS del_flag, '系统初始化分类' AS remark UNION ALL
    SELECT '电子产品', 0, 2, '0', 'admin', NOW(), '0', '系统初始化分类' UNION ALL
    SELECT '服饰',     0, 3, '0', 'admin', NOW(), '0', '系统初始化分类' UNION ALL
    SELECT '生活用品', 0, 4, '0', 'admin', NOW(), '0', '系统初始化分类' UNION ALL
    SELECT '运动器材', 0, 5, '0', 'admin', NOW(), '0', '系统初始化分类' UNION ALL
    SELECT '其他',     0, 6, '0', 'admin', NOW(), '0', '系统初始化分类'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM `tr_trade_category` WHERE `del_flag` = '0');


-- =====================================================
-- 2. 交易管理菜单与按钮权限
-- 一级菜单 menu_id 段 2000-2079，商品管理用 6000 段；
-- order_num 按业务流程分档（10/20/.../130）；
-- icon 全部使用若依自带 SVG（ruoyi-vue3-frontend/src/assets/icons/svg/）。
-- =====================================================

-- 一级目录
INSERT IGNORE INTO sys_menu VALUES (2000, '交易管理', 0,   5, 'trade', NULL, '', '', 1, 0, 'M', '0', '0', '',                 'build',      'admin', sysdate(), '', NULL, '交易管理目录');

-- 二级菜单（业务模块）
INSERT IGNORE INTO sys_menu VALUES (2007, '学生用户',         2000,  10, 'student',       'trade/student/index',       '', '', 1, 0, 'C', '0', '0', 'trade:student:list',         'user',       'admin', sysdate(), '', NULL, '学生用户菜单');
INSERT IGNORE INTO sys_menu VALUES (2001, '商品分类',         2000,  20, 'category',      'trade/category/index',      '', '', 1, 0, 'C', '0', '0', 'trade:category:list',        'tree-table', 'admin', sysdate(), '', NULL, '商品分类菜单');
INSERT IGNORE INTO sys_menu VALUES (6000, '商品管理',         2000,  30, 'goods',         'trade/goods/index',         '', '', 1, 0, 'C', '0', '0', 'trade:goods:list',           'shopping',   'admin', sysdate(), '', NULL, '闲置商品管理（含审核）');
INSERT IGNORE INTO sys_menu VALUES (2019, '商品图片',         2000,  40, 'image',         'trade/image/index',         '', '', 1, 0, 'C', '0', '0', 'trade:image:list',           'eye-open',   'admin', sysdate(), '', NULL, '商品图片菜单');
INSERT IGNORE INTO sys_menu VALUES (2067, '商品收藏',         2000,  50, 'favorite',      'trade/favorite/index',      '', '', 1, 0, 'C', '0', '0', 'trade:favorite:list',        'star',       'admin', sysdate(), '', NULL, '商品收藏菜单');
INSERT IGNORE INTO sys_menu VALUES (2031, '交易订单',         2000,  60, 'order',         'trade/order/index',         '', '', 1, 0, 'C', '0', '0', 'trade:order:list',           'money',      'admin', sysdate(), '', NULL, '交易订单菜单');
INSERT IGNORE INTO sys_menu VALUES (2037, '订单操作日志',     2000,  70, 'log',           'trade/log/index',           '', '', 1, 0, 'C', '0', '0', 'trade:log:list',             'log',        'admin', sysdate(), '', NULL, '订单操作日志菜单');
INSERT IGNORE INTO sys_menu VALUES (2073, '交易评价',         2000,  80, 'evaluation',    'trade/evaluation/index',    '', '', 1, 0, 'C', '0', '0', 'trade:evaluation:list',      'rate',       'admin', sysdate(), '', NULL, '交易评价菜单');
INSERT IGNORE INTO sys_menu VALUES (2079, '私信消息',         2000,  90, 'message',       'trade/message/index',       '', '', 1, 0, 'C', '0', '0', 'trade:message:list',         'message',    'admin', sysdate(), '', NULL, '私信消息菜单');
INSERT IGNORE INTO sys_menu VALUES (2043, '举报信息',         2000, 100, 'report',        'trade/report/index',        '', '', 1, 0, 'C', '0', '0', 'trade:report:list',          'question',   'admin', sysdate(), '', NULL, '举报信息菜单');
INSERT IGNORE INTO sys_menu VALUES (2049, '交易争议处理',     2000, 110, 'dispute',       'trade/dispute/index',       '', '', 1, 0, 'C', '0', '0', 'trade:dispute:list',         'tool',       'admin', sysdate(), '', NULL, '交易争议处理菜单');
INSERT IGNORE INTO sys_menu VALUES (2087, '统计报表',         2000, 120, 'statistics',    'trade/statistics/index',    '', '', 1, 0, 'C', '0', '0', 'trade:statistics:view',     'chart',      'admin', sysdate(), '', NULL, '统计报表菜单');
INSERT IGNORE INTO sys_menu VALUES (2055, 'AI审核记录',       2000, 130, 'aiAuditRecord', 'trade/aiAuditRecord/index', '', '', 1, 0, 'C', '0', '0', 'trade:aiAuditRecord:list',   'skill',      'admin', sysdate(), '', NULL, 'AI审核记录菜单');
INSERT IGNORE INTO sys_menu VALUES (2061, 'AI估价记录',       2000, 140, 'aiPriceRecord', 'trade/aiPriceRecord/index', '', '', 1, 0, 'C', '0', '0', 'trade:aiPriceRecord:list',   'chart',      'admin', sysdate(), '', NULL, 'AI估价记录菜单');

-- 三级按钮权限：标准 5 件套（查询/新增/修改/删除/导出）
-- 学生用户 2008-2012
INSERT IGNORE INTO sys_menu VALUES (2008, '学生用户查询', 2007, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2009, '学生用户新增', 2007, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2010, '学生用户修改', 2007, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2011, '学生用户删除', 2007, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2012, '学生用户导出', 2007, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2013, '学生用户导入', 2007, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:student:import', '#', 'admin', sysdate(), '', NULL, '');
-- 商品分类 2002-2006
INSERT IGNORE INTO sys_menu VALUES (2002, '商品分类查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:category:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2003, '商品分类新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:category:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2004, '商品分类修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:category:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2005, '商品分类删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:category:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2006, '商品分类导出', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:category:export', '#', 'admin', sysdate(), '', NULL, '');
-- 商品管理 6001-6007（含 audit / offline）
INSERT IGNORE INTO sys_menu VALUES (6001, '商品查询',   6000, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:query',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6002, '商品新增',   6000, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:add',     '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6003, '商品修改',   6000, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:edit',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6004, '商品删除',   6000, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:remove',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6005, '商品导出',   6000, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:export',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6006, '商品审核',   6000, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:audit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6007, '商品上下架', 6000, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:offline', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (6009, '商品导入', 6000, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:goods:import', '#', 'admin', sysdate(), '', NULL, '');
-- 商品图片 2020-2024
INSERT IGNORE INTO sys_menu VALUES (2020, '商品图片查询', 2019, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:image:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2021, '商品图片新增', 2019, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:image:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2022, '商品图片修改', 2019, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:image:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2023, '商品图片删除', 2019, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:image:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2024, '商品图片导出', 2019, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:image:export', '#', 'admin', sysdate(), '', NULL, '');
-- 商品收藏 2068-2072
INSERT IGNORE INTO sys_menu VALUES (2068, '商品收藏查询', 2067, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:favorite:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2069, '商品收藏新增', 2067, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:favorite:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2070, '商品收藏修改', 2067, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:favorite:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2071, '商品收藏删除', 2067, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:favorite:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2072, '商品收藏导出', 2067, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:favorite:export', '#', 'admin', sysdate(), '', NULL, '');
-- 交易订单 2032-2036
INSERT IGNORE INTO sys_menu VALUES (2032, '交易订单查询', 2031, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:order:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2033, '交易订单新增', 2031, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:order:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2034, '交易订单修改', 2031, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:order:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2035, '交易订单删除', 2031, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:order:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2036, '交易订单导出', 2031, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:order:export', '#', 'admin', sysdate(), '', NULL, '');
-- 订单操作日志 2038-2042
INSERT IGNORE INTO sys_menu VALUES (2038, '订单操作日志查询', 2037, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:log:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2039, '订单操作日志新增', 2037, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:log:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2040, '订单操作日志修改', 2037, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:log:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2041, '订单操作日志删除', 2037, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:log:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2042, '订单操作日志导出', 2037, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:log:export', '#', 'admin', sysdate(), '', NULL, '');
-- 交易评价 2074-2078
INSERT IGNORE INTO sys_menu VALUES (2074, '交易评价查询', 2073, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:evaluation:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2075, '交易评价新增', 2073, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:evaluation:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2076, '交易评价修改', 2073, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:evaluation:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2077, '交易评价删除', 2073, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:evaluation:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2078, '交易评价导出', 2073, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:evaluation:export', '#', 'admin', sysdate(), '', NULL, '');
-- 私信消息 2080-2084
INSERT IGNORE INTO sys_menu VALUES (2080, '私信消息查询', 2079, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:message:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2081, '私信消息新增', 2079, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:message:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2082, '私信消息修改', 2079, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:message:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2083, '私信消息删除', 2079, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:message:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2084, '私信消息导出', 2079, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:message:export', '#', 'admin', sysdate(), '', NULL, '');

-- 用户咨询（管理员联系功能）
INSERT IGNORE INTO sys_menu VALUES (2112, '用户咨询', 2000, 91, 'contact', 'trade/adminContact/index', '', '', 1, 0, 'C', '0', '0', 'trade:message:list', 'chat-round', 'admin', sysdate(), '', NULL, '用户咨询管理菜单');
-- 举报信息 2044-2048
INSERT IGNORE INTO sys_menu VALUES (2044, '举报信息查询', 2043, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2045, '举报信息新增', 2043, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2046, '举报信息修改', 2043, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2047, '举报信息删除', 2043, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2048, '举报信息导出', 2043, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:export', '#', 'admin', sysdate(), '', NULL, '');
-- 交易争议处理 2050-2054
INSERT IGNORE INTO sys_menu VALUES (2050, '交易争议处理查询', 2049, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2051, '交易争议处理新增', 2049, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2052, '交易争议处理修改', 2049, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2053, '交易争议处理删除', 2049, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2054, '交易争议处理导出', 2049, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:export', '#', 'admin', sysdate(), '', NULL, '');
-- AI 审核记录 2056-2060
INSERT IGNORE INTO sys_menu VALUES (2056, 'AI审核记录查询', 2055, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiAuditRecord:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2057, 'AI审核记录新增', 2055, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiAuditRecord:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2058, 'AI审核记录修改', 2055, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiAuditRecord:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2059, 'AI审核记录删除', 2055, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiAuditRecord:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2060, 'AI审核记录导出', 2055, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiAuditRecord:export', '#', 'admin', sysdate(), '', NULL, '');
-- AI 估价记录 2062-2066
INSERT IGNORE INTO sys_menu VALUES (2062, 'AI估价记录查询', 2061, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiPriceRecord:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2063, 'AI估价记录新增', 2061, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiPriceRecord:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2064, 'AI估价记录修改', 2061, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiPriceRecord:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2065, 'AI估价记录删除', 2061, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiPriceRecord:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2066, 'AI估价记录导出', 2061, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:aiPriceRecord:export', '#', 'admin', sysdate(), '', NULL, '');
-- 业务动作按钮：举报处理 2085 / 争议人工仲裁 2086
INSERT IGNORE INTO sys_menu VALUES (2085, '举报处理',     2043, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:report:handle',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2086, '争议人工仲裁', 2049, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:handle', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2088, '争议重新仲裁', 2049, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:dispute:reArbitrate', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2089, '统计报表查看', 2087, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:statistics:view', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2090, '统计报表查询', 2087, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:statistics:list', '#', 'admin', sysdate(), '', NULL, '');

-- 公告管理（交易模块）
INSERT IGNORE INTO sys_menu VALUES (2091, '公告管理', 2000, 150, 'announcement', 'trade/announcement/index', '', '', 1, 0, 'C', '0', '0', 'trade:announcement:list', 'bell', 'admin', sysdate(), '', NULL, '公告管理');
INSERT IGNORE INTO sys_menu VALUES (2092, '公告查询', 2091, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2093, '公告新增', 2091, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2094, '公告修改', 2091, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2095, '公告删除', 2091, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2096, '公告导出', 2091, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2097, '公告发布', 2091, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:announcement:publish', '#', 'admin', sysdate(), '', NULL, '');

-- 信用分管理（交易模块）；2100/2101 在部分库已被占用，改用 2110/2111
INSERT IGNORE INTO sys_menu VALUES (2110, '信用分管理', 2000, 12, 'credit', 'trade/credit/index', '', '', 1, 0, 'C', '0', '0', 'trade:credit:list', 'star', 'admin', sysdate(), '', NULL, '信用流水与手动调整');
INSERT IGNORE INTO sys_menu VALUES (2111, '信用手动调整', 2110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:credit:adjust', '#', 'admin', sysdate(), '', NULL, '');

-- =====================================================
-- 3. 字典：商品状态 trade_goods_status
-- 用于商品列表状态标签字典联动。
-- 先删后插，保证每次执行结果一致（5 条）。
-- =====================================================
INSERT IGNORE INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('商品状态', 'trade_goods_status', '0', 'admin', sysdate(), '0待审核/1已上架/2审核拒绝/3已下架/4已售出');

DELETE FROM sys_dict_data WHERE dict_type = 'trade_goods_status';

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待审核',   '0', 'trade_goods_status', '', 'warning', 'N', '0', 'admin', sysdate(), '待审核'),
 (2, '已上架',   '1', 'trade_goods_status', '', 'success', 'N', '0', 'admin', sysdate(), '已上架'),
 (3, '审核拒绝', '2', 'trade_goods_status', '', 'danger',  'N', '0', 'admin', sysdate(), '审核拒绝'),
 (4, '已下架',   '3', 'trade_goods_status', '', 'info',    'N', '0', 'admin', sysdate(), '已下架'),
 (5, '已售出',   '4', 'trade_goods_status', '', '',        'N', '0', 'admin', sysdate(), '已售出');


-- =====================================================
-- 4. 字典：举报处理状态 trade_report_handle_status
-- 用于举报列表处理状态标签字典联动。
-- =====================================================
INSERT IGNORE INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('举报处理状态', 'trade_report_handle_status', '0', 'admin', sysdate(), '0待处理/1已处理/2已驳回');

DELETE FROM sys_dict_data WHERE dict_type = 'trade_report_handle_status';

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待处理', '0', 'trade_report_handle_status', '', 'warning', 'N', '0', 'admin', sysdate(), '待处理'),
 (2, '已处理', '1', 'trade_report_handle_status', '', 'success', 'N', '0', 'admin', sysdate(), '已处理'),
 (3, '已驳回', '2', 'trade_report_handle_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已驳回');


-- =====================================================
-- 5. 字典：争议处理状态 trade_dispute_status
-- 用于交易争议列表处理状态标签字典联动。
-- =====================================================
INSERT IGNORE INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('争议处理状态', 'trade_dispute_status', '0', 'admin', sysdate(), '0待AI分析/1AI分析中/2等待人工仲裁/3已处理');

DELETE FROM sys_dict_data WHERE dict_type = 'trade_dispute_status';

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待AI分析',     '0', 'trade_dispute_status', '', 'info',    'N', '0', 'admin', sysdate(), '待AI分析'),
 (2, 'AI分析中',     '1', 'trade_dispute_status', '', 'primary', 'N', '0', 'admin', sysdate(), 'AI分析中'),
 (3, '等待人工仲裁', '2', 'trade_dispute_status', '', 'warning', 'N', '0', 'admin', sysdate(), '等待人工仲裁'),
 (4, '已处理',       '3', 'trade_dispute_status', '', 'success', 'N', '0', 'admin', sysdate(), '已处理');
