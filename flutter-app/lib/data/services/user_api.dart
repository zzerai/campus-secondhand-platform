import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/auth_models.dart';
import '../models/user_homepage.dart';
import 'session.dart';
import 'storage_service.dart';

class UserApi {
  static final UserApi instance = UserApi._();
  UserApi._();

  final _session = Session.instance;

  Future<StudentUser> getUserProfile() async {
    debugPrint('[UserApi] 个人信息: GET /app/user/profile');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/user/profile'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        final user = StudentUser.fromJson(Map<String, dynamic>.from(body['data']));
        _session.currentUser = user;
        return user;
      }
    }
    throw Exception('获取个人信息失败');
  }

  /// 查询任意用户的公开主页（脱敏：不含手机号/学号）。
  Future<UserHomepage> getUserHomepage(int userId) async {
    debugPrint('[UserApi] 用户主页: GET /app/user/homepage/$userId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/user/homepage/$userId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return UserHomepage.fromJson(Map<String, dynamic>.from(body['data']));
      }
    }
    throw Exception('获取用户主页失败');
  }

  Future<String?> updateProfile({
    String? nickname,
    String? contactWay,
    String? avatar,
  }) async {
    debugPrint('[UserApi] 修改个人信息: PUT /app/user/update');
    final body = <String, dynamic>{};
    if (nickname != null) body['nickname'] = nickname;
    if (contactWay != null) body['contactWay'] = contactWay;
    if (avatar != null) body['avatar'] = avatar;

    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/user/update'),
      headers: _session.headers,
      body: jsonEncode(body),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      if (data is Map && data['code'] == 200) {
        final user = _session.currentUser;
        if (user != null) {
          if (nickname != null) user.nickname = nickname;
          if (avatar != null) user.avatar = avatar;
          if (contactWay != null) user.contactWay = contactWay;
        }
        return null;
      }
      return data is Map ? data['msg']?.toString() : '修改失败';
    }
    return '修改失败';
  }

  Future<String?> changePhone(String newPhone, String smsCode) async {
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/user/changePhone'),
      headers: _session.headers,
      body: jsonEncode({'newPhone': newPhone, 'smsCode': smsCode}),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      if (data is Map && data['code'] == 200) {
        if (_session.currentUser != null) _session.currentUser!.phone = newPhone;
        return null;
      }
      return data is Map ? data['msg']?.toString() : '修改失败';
    }
    return '修改失败';
  }

  Future<String?> changePassword(String oldPassword, String newPassword) async {
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/user/changePwd'),
      headers: _session.headers,
      body: jsonEncode({'oldPassword': oldPassword, 'newPassword': newPassword}),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      if (data is Map && data['code'] == 200) {
        _session.logout();
        StorageService.clear();
        return null;
      }
      return data is Map ? data['msg']?.toString() : '修改失败';
    }
    return '修改失败';
  }

  Future<int> _getTotal(String endpoint) async {
    final uri = Uri.parse('${Session.baseUrl}$endpoint').replace(
      queryParameters: {'pageNum': '1', 'pageSize': '1'},
    );
    final response = await http.get(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['total'] is int) return body['total'];
    }
    return 0;
  }

  Future<int> getMyGoodsCount() => _getTotal('/app/goods/myList');
  Future<int> getMySellCount() => _getTotal('/app/order/mySell');
  Future<int> getMyBuyCount() => _getTotal('/app/order/myBuy');
  Future<int> getMyFavoriteCount() => _getTotal('/app/favorite/myList');
}
