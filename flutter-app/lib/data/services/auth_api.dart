import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/auth_models.dart';
import 'session.dart';
import 'storage_service.dart';

class AuthApi {
  static final AuthApi instance = AuthApi._();
  AuthApi._();

  final _session = Session.instance;

  Future<LoginResult> login(LoginRequest request) async {
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );
    final body = jsonDecode(response.body);
    if (body is Map && body['token'] != null) {
      final result = LoginResult.fromJson(Map<String, dynamic>.from(body));
      _session.token = result.token;
      return result;
    }
    throw Exception(body is Map ? body['msg'] ?? '登录失败' : '登录失败');
  }

  Future<void> sendSmsCode(String phone, String scene) async {
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/sms/send'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'phone': phone, 'scene': scene}),
    );
    final body = jsonDecode(response.body);
    if (body is Map && body['code'] == 200) return;
    throw Exception(body is Map ? body['msg'] ?? '发送失败' : '发送失败');
  }

  Future<String> register(RegisterRequest request) async {
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );
    final body = jsonDecode(response.body);
    if (body is Map) {
      if (body['code'] == 200 || body['success'] == true) return 'success';
      return body['msg'] ?? '注册失败';
    }
    throw Exception('注册失败');
  }

  Future<String> resetPassword(String phone, String smsCode, String newPassword) async {
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/user/resetPwd'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'phone': phone, 'smsCode': smsCode, 'newPassword': newPassword}),
    );
    final body = jsonDecode(response.body);
    if (body is Map && (body['code'] == 200 || body['success'] == true)) {
      return 'success';
    }
    return body is Map ? (body['msg'] ?? '重置失败') : '重置失败';
  }

  void logout() {
    _session.logout();
    StorageService.clear();
  }

  Future<void> serverLogout() async {
    try {
      await http.post(
        Uri.parse('${Session.baseUrl}/app/logout'),
        headers: _session.headers,
      );
    } catch (_) {}
    logout();
  }
}
