-- =====================================================
-- 移动端 APK 版本管理 / 应用内更新 增量脚本（存量库执行一次）
-- 新建 tr_app_version 版本表 + 管理端菜单/权限。
-- 仅支持安卓 APK 侧载更新（不含 iOS）。
--
-- 比对策略：以 version_code（整数，pubspec 的 +N 构建号）单调递增比对，
--           而非 version_name 字符串，杜绝 "1.10 vs 1.9" 误判。
--
-- 菜单挂在「交易管理」目录(2000)下：
--   2112 版本管理(C)   → 组件 trade/appVersion/index，列表权限 trade:version:list
--   2113 版本查询(F)   → trade:version:query
--   2114 版本新增(F)   → trade:version:add
--   2115 版本修改(F)   → trade:version:edit
--   2116 版本删除(F)   → trade:version:remove
--   2117 APK上传(F)    → trade:version:upload
-- 执行后 admin 重新登录即可见；普通管理员需在角色管理勾选该菜单。
-- 幂等：INSERT IGNORE，重复执行无副作用。
-- =====================================================

CREATE TABLE IF NOT EXISTS `tr_app_version` (
  `version_id`    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `version_name`  varchar(32)  NOT NULL                COMMENT '版本名（展示用，如 1.1.0）',
  `version_code`  int(11)      NOT NULL                COMMENT '版本号（比对键，单调递增，对应 pubspec 构建号）',
  `download_url`  varchar(500) NOT NULL                COMMENT 'APK 下载地址（完整 URL）',
  `file_size`     bigint(20)   DEFAULT NULL            COMMENT 'APK 字节大小（用于进度展示）',
  `file_sha256`   char(64)     DEFAULT NULL            COMMENT 'APK 文件 SHA-256，用于下载完整性校验',
  `force_update`  char(1)      DEFAULT '0'             COMMENT '是否强制更新：0否，1是',
  `update_log`    varchar(2000) DEFAULT NULL           COMMENT '更新日志（支持换行）',
  `status`        char(1)      DEFAULT '0'             COMMENT '状态：0启用，1停用（仅启用版本参与移动端比对）',
  `create_by`     varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`      char(1)      DEFAULT '0'             COMMENT '删除标志：0存在，2删除',
  `remark`        varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`version_id`),
  KEY `idx_version_code` (`version_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='移动端APK版本';

-- 菜单与权限
INSERT IGNORE INTO sys_menu VALUES (2112, '版本管理', 2000, 13, 'appVersion', 'trade/appVersion/index', '', '', 1, 0, 'C', '0', '0', 'trade:version:list', 'phone', 'admin', sysdate(), '', NULL, '移动端APK版本与应用内更新');
INSERT IGNORE INTO sys_menu VALUES (2113, '版本查询', 2112, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:version:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2114, '版本新增', 2112, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:version:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2115, '版本修改', 2112, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:version:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2116, '版本删除', 2112, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:version:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES (2117, 'APK上传', 2112, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trade:version:upload', '#', 'admin', sysdate(), '', NULL, '');
