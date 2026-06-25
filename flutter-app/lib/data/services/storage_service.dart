import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/auth_models.dart';

class StorageService {
  static const _tokenKey = 'auth_token';
  static const _userKey = 'user_json';

  static Future<SharedPreferences> get _prefs =>
      SharedPreferences.getInstance();

  /// 保存登录凭证到本地
  static Future<void> saveAuth(String token, StudentUser user) async {
    final prefs = await _prefs;
    await prefs.setString(_tokenKey, token);
    await prefs.setString(_userKey, jsonEncode(user.toJson()));
  }

  /// 获取本地存储的 token，不存在则返回 null
  static Future<String?> getToken() async {
    final prefs = await _prefs;
    return prefs.getString(_tokenKey);
  }

  /// 获取本地存储的用户信息，不存在则返回 null
  static Future<StudentUser?> getUser() async {
    final prefs = await _prefs;
    final json = prefs.getString(_userKey);
    if (json == null) return null;
    try {
      return StudentUser.fromJson(
        Map<String, dynamic>.from(jsonDecode(json)),
      );
    } catch (_) {
      return null;
    }
  }

  /// 清除所有本地登录凭证
  static Future<void> clear() async {
    final prefs = await _prefs;
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
  }

  /// 是否存在本地登录凭证
  static Future<bool> hasAuth() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
}
