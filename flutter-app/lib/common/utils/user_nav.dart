import 'package:flutter/material.dart';
import '../../data/services/session.dart';
import '../../screens/profile/profile_page.dart';
import '../../screens/profile/user_profile_page.dart';

/// 统一的"打开某用户主页"入口：所有头像点击都走这里。
///
/// - 点击自己的头像 → 跳回"我的个人中心"（复用 [ProfilePage]，用 Scaffold 包裹补返回键）。
/// - 点击他人头像 → 打开只读公开主页 [UserProfilePage]。
/// - userId 为空或 0（数据缺失）时不响应。
void openUserProfile(BuildContext context, int? userId) {
  if (userId == null || userId == 0) return;
  final me = Session.instance.currentUser?.userId;
  if (me != null && me == userId) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => Scaffold(
          appBar: AppBar(title: const Text('我的')),
          body: const ProfilePage(),
        ),
      ),
    );
  } else {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => UserProfilePage(userId: userId)),
    );
  }
}
