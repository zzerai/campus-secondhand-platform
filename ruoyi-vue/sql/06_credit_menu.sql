-- =====================================================
-- 信用分管理菜单 / 权限增量脚本（存量库执行一次）
-- 02_seed.sql 已含同样两行；本文件供已初始化的库补菜单用。
-- 幂等：INSERT IGNORE，重复执行无副作用。
--
-- 注意：菜单ID 2100/2101 在部分库已被其它（隐藏的空）菜单占用，
--      会导致 INSERT IGNORE 被静默跳过、菜单不出现，故改用 2110/2111。
--
-- 菜单挂在「交易管理」目录(2000)下：
--   2110 信用分管理(C) → 组件 trade/credit/index，列表权限 trade:credit:list
--   2111 信用手动调整(F) → 按钮权限 trade:credit:adjust
--
-- 超级管理员(admin)拥有全部菜单，执行后【重新登录】即可在侧边栏看到并使用；
-- 普通管理员需在「系统管理 → 角色管理」里勾选该菜单后重新登录生效。
-- =====================================================

INSERT IGNORE INTO sys_menu VALUES (2110, '信用分管理', 2000, 12, 'credit', 'trade/credit/index', '', '', 1, 0, 'C', '0', '0', 'trade:credit:list', 'star', 'admin', sysdate(), '', NULL, '信用流水与手动调整');
INSERT IGNORE INTO sys_menu VALUES (2111, '信用手动调整', 2110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:credit:adjust', '#', 'admin', sysdate(), '', NULL, '');
