import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/app_version.dart';
import 'session.dart';

class VersionApi {
  static final VersionApi instance = VersionApi._();
  VersionApi._();

  final _session = Session.instance;

  /// 获取当前启用的最新版本（后端匿名放行）。返回 null 表示后台未配置启用版本。
  Future<AppVersion?> getLatest() async {
    debugPrint('[VersionApi] 检测更新: GET /app/version/latest');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/version/latest'),
      headers: _session.headers,
    );
    debugPrint('[VersionApi] 响应: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return AppVersion.fromJson(Map<String, dynamic>.from(body['data']));
      }
      // data 为 null：后台无启用版本
      return null;
    }
    throw Exception('检测更新失败');
  }
}
