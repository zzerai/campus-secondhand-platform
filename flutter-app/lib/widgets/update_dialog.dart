import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';
import '../common/utils/app_updater.dart';
import '../common/utils/toast_utils.dart';
import '../data/models/app_version.dart';

/// 发现新版本对话框：展示更新日志，点击「立即更新」下载并调起安装。
///
/// 强制更新时（[AppVersion.forceUpdate]）不可关闭（屏蔽返回键、隐藏「稍后」）。
class UpdateDialog extends StatefulWidget {
  final AppVersion version;
  const UpdateDialog({super.key, required this.version});

  @override
  State<UpdateDialog> createState() => _UpdateDialogState();
}

class _UpdateDialogState extends State<UpdateDialog> {
  bool _downloading = false;
  double _progress = 0;

  Future<void> _startUpdate() async {
    setState(() {
      _downloading = true;
      _progress = 0;
    });
    try {
      final path = await AppUpdater.downloadApk(
        widget.version,
        onProgress: (p) {
          if (mounted) setState(() => _progress = p);
        },
      );
      await AppUpdater.install(path);
      if (!mounted) return;
      // 安装器已弹出。非强制更新直接关闭对话框；强制更新保留对话框，
      // 并复位下载态使「立即更新」按钮重现——用户若取消系统安装可重试，避免卡死无按钮弹窗。
      if (widget.version.forceUpdate) {
        setState(() => _downloading = false);
      } else {
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _downloading = false);
        showToast(context, e.toString().replaceFirst('Exception: ', ''));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final v = widget.version;
    final log = (v.updateLog ?? '').trim();
    return PopScope(
      canPop: !v.forceUpdate && !_downloading,
      child: AlertDialog(
        title: Text('发现新版本 ${v.versionName}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (log.isNotEmpty)
              Text(log, style: const TextStyle(fontSize: 14, height: 1.5)),
            if (v.forceUpdate) ...[
              const SizedBox(height: 8),
              const Text('本次为重要更新，需更新后继续使用',
                  style: TextStyle(fontSize: 13, color: AppColors.price)),
            ],
            if (_downloading) ...[
              const SizedBox(height: 16),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: _progress > 0 ? _progress : null,
                  minHeight: 6,
                  backgroundColor: AppColors.border,
                  color: AppColors.accent,
                ),
              ),
              const SizedBox(height: 6),
              Text('下载中 ${(_progress * 100).toStringAsFixed(0)}%',
                  style: const TextStyle(fontSize: 12, color: AppColors.muted)),
            ],
          ],
        ),
        actions: _downloading
            ? null
            : [
                if (!v.forceUpdate)
                  TextButton(
                    onPressed: () {
                      AppUpdater.skipVersion(v.versionCode);
                      Navigator.of(context).pop();
                    },
                    child: const Text('稍后',
                        style: TextStyle(color: AppColors.muted)),
                  ),
                TextButton(
                  onPressed: _startUpdate,
                  child: const Text('立即更新',
                      style: TextStyle(color: AppColors.accent)),
                ),
              ],
      ),
    );
  }
}
