/// 移动端版本信息，对应后端 /app/version/latest 返回的 tr_app_version。
///
/// 仅安卓 APK 侧载更新使用。是否需要更新由客户端用本地 versionCode 与 [versionCode] 比对决定。
class AppVersion {
  /// 版本号（比对键，整数，对应 pubspec 构建号 +N）
  final int versionCode;

  /// 版本名（展示用，如 1.1.0）
  final String versionName;

  /// APK 下载地址（完整 URL）
  final String downloadUrl;

  /// APK 字节大小（可能为空）
  final int? fileSize;

  /// APK 文件 SHA-256（小写十六进制，下载后完整性校验；可能为空）
  final String? fileSha256;

  /// 是否强制更新
  final bool forceUpdate;

  /// 更新日志
  final String? updateLog;

  AppVersion({
    required this.versionCode,
    required this.versionName,
    required this.downloadUrl,
    this.fileSize,
    this.fileSha256,
    this.forceUpdate = false,
    this.updateLog,
  });

  factory AppVersion.fromJson(Map<String, dynamic> json) {
    return AppVersion(
      versionCode: (json['versionCode'] as num?)?.toInt() ?? 0,
      versionName: json['versionName'] ?? '',
      downloadUrl: json['downloadUrl'] ?? '',
      fileSize: (json['fileSize'] as num?)?.toInt(),
      fileSha256: json['fileSha256'],
      forceUpdate: json['forceUpdate'] == '1',
      updateLog: json['updateLog'],
    );
  }
}
