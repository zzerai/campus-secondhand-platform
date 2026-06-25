import 'dart:io';
import 'package:crypto/crypto.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:open_filex/open_filex.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../data/models/app_version.dart';
import '../../data/services/version_api.dart';
import '../../widgets/update_dialog.dart';
import 'toast_utils.dart';

/// 应用内更新（仅安卓 APK 侧载）。
///
/// 比对策略：以本地构建号(versionCode) 与后端启用版本的 [AppVersion.versionCode] 比较，
/// 大于本地才提示更新。下载落盘 → SHA-256 校验 → 调起系统安装器。
class AppUpdater {
  AppUpdater._();

  static const _skippedVersionKey = 'skipped_version_code';

  /// 防重入：避免启动检测与手动检测同时弹两个对话框。
  static bool _checking = false;

  /// 记录用户跳过的版本号，该版本及更低版本下次不再弹窗。
  static Future<void> skipVersion(int versionCode) async {
    final prefs = await SharedPreferences.getInstance();
    final old = prefs.getInt(_skippedVersionKey) ?? 0;
    if (versionCode > old) {
      await prefs.setInt(_skippedVersionKey, versionCode);
    }
  }

  /// 检测更新并按需弹窗。
  ///
  /// [silent] = true（启动自动检测）：仅在有新版时弹窗，无更新或出错都静默。
  /// [silent] = false（手动检测）：无更新提示「已是最新」，出错给出提示。
  static Future<void> checkAndPrompt(BuildContext context,
      {bool silent = true}) async {
    if (_checking) return;
    _checking = true;
    try {
      final info = await PackageInfo.fromPlatform();
      final currentCode = int.tryParse(info.buildNumber) ?? 0;
      final latest = await VersionApi.instance.getLatest();
      if (!context.mounted) return;
      if (latest == null || latest.versionCode <= currentCode) {
        if (!silent) showToast(context, '已是最新版本');
        return;
      }
      // 跳过记录仅对非强制更新生效：强制更新必须弹窗
      if (!latest.forceUpdate) {
        final prefs = await SharedPreferences.getInstance();
        final skipped = prefs.getInt(_skippedVersionKey) ?? 0;
        if (skipped >= latest.versionCode) {
          if (!silent) showToast(context, '已是最新版本');
          return;
        }
      }
      await showDialog(
        context: context,
        barrierDismissible: !latest.forceUpdate,
        builder: (_) => UpdateDialog(version: latest),
      );
    } catch (_) {
      if (!silent && context.mounted) {
        showToast(context, '检测更新失败，请稍后重试');
      }
    } finally {
      _checking = false;
    }
  }

  /// 下载 APK 到应用专属外部目录，带进度回调；若后端提供 SHA-256 则校验完整性。
  /// 返回本地文件路径，失败抛异常。
  static Future<String> downloadApk(
    AppVersion v, {
    required void Function(double progress) onProgress,
  }) async {
    final dir = await getExternalStorageDirectory() ??
        await getApplicationDocumentsDirectory();
    final apkDir = Directory('${dir.path}/apk');
    if (!apkDir.existsSync()) {
      apkDir.createSync(recursive: true);
    }
    final file = File('${apkDir.path}/app-${v.versionCode}.apk');
    if (file.existsSync()) {
      file.deleteSync();
    }

    final client = http.Client();
    try {
      final resp = await client.send(http.Request('GET', Uri.parse(v.downloadUrl)));
      if (resp.statusCode != 200) {
        throw Exception('下载失败(${resp.statusCode})');
      }
      final total = resp.contentLength ?? v.fileSize ?? 0;
      final sink = file.openWrite();
      int received = 0;
      await for (final chunk in resp.stream) {
        sink.add(chunk);
        received += chunk.length;
        if (total > 0) onProgress(received / total);
      }
      await sink.flush();
      await sink.close();
    } finally {
      client.close();
    }

    // SHA-256 完整性校验（与后端上传时计算的值比对）
    final expected = v.fileSha256;
    if (expected != null && expected.isNotEmpty) {
      final actual = sha256.convert(await file.readAsBytes()).toString();
      if (actual.toLowerCase() != expected.toLowerCase()) {
        file.deleteSync();
        throw Exception('文件校验失败，请重新下载');
      }
    }
    return file.path;
  }

  /// 调起系统安装器安装下载好的 APK。
  static Future<void> install(String filePath) async {
    final result = await OpenFilex.open(filePath);
    if (result.type != ResultType.done) {
      throw Exception(result.message.isNotEmpty ? result.message : '无法打开安装程序');
    }
  }
}
