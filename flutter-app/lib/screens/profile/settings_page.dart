import 'dart:async';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/app_updater.dart';
import '../../data/models/auth_models.dart';
import '../../data/services/auth_api.dart';
import '../../data/services/user_api.dart';
import '../../data/services/upload_api.dart';
import '../../data/services/session.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/logout_button.dart';
import '../../common/utils/toast_utils.dart';
import '../auth/login_page.dart';
import 'phone_change_dialog.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final _userApi = UserApi.instance;
  final _uploadApi = UploadApi.instance;
  final _authApi = AuthApi.instance;
  final ImagePicker _picker = ImagePicker();

  late StudentUser _user;
  bool _saving = false;
  String _appVersion = '';

  @override
  void initState() {
    super.initState();
    _user = Session.instance.currentUser ?? StudentUser(userId: 0);
    PackageInfo.fromPlatform().then((info) {
      if (mounted) setState(() => _appVersion = 'v${info.version}');
    });
  }

  // ── Avatar ──

  Future<void> _changeAvatar() async {
    final picked = await _picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 512,
      maxHeight: 512,
      imageQuality: 85,
    );
    if (picked == null) return;

    setState(() => _saving = true);
    try {
      final url = await _uploadApi.uploadAvatar(picked);
      if (url != null) {
        setState(() => _user.avatar = url);
        showToast(context,'头像已更新');
      } else {
        showToast(context,'头像上传失败');
      }
    } catch (e) {
      showToast(context,e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  // ── Edit field dialog ──

  Future<void> _editField(String label, String? currentValue,
      Future<String?> Function(String) onSave) async {
    final ctrl = TextEditingController(text: currentValue ?? '');
    final result = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('修改$label'),
        content: TextField(
          controller: ctrl,
          autofocus: true,
          decoration: InputDecoration(
            hintText: '请输入$label',
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppColors.radiusSm),
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, ctrl.text.trim()),
            child: const Text('确定',
                style: TextStyle(color: AppColors.accent)),
          ),
        ],
      ),
    );
    ctrl.dispose();
    if (result == null || result.isEmpty) return;

    setState(() => _saving = true);
    try {
      final error = await onSave(result);
      if (!mounted) return;
      if (error != null) {
        showToast(context,error);
      } else {
        showToast(context,'$label已更新');
      }
    } catch (e) {
      showToast(context,e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  // ── Phone change flow ──

  Future<void> _changePhone() async {
    final phoneCtrl = TextEditingController();
    final codeCtrl = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => PhoneChangeDialog(
        phoneCtrl: phoneCtrl,
        codeCtrl: codeCtrl,
      ),
    );
    if (confirmed != true) return;

    setState(() => _saving = true);
    try {
      final error = await _userApi.changePhone(
        phoneCtrl.text.trim(),
        codeCtrl.text.trim(),
      );
      phoneCtrl.dispose();
      codeCtrl.dispose();
      if (!mounted) return;
      if (error != null) {
        showToast(context,error);
      } else {
        setState(() => _user.phone = phoneCtrl.text.trim());
        showToast(context,'手机号已更新');
      }
    } catch (e) {
      showToast(context,e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  // ── Logout ──

  Future<void> _handleLogout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出当前账号吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child:
                const Text('确定', style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    await _authApi.serverLogout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginPage()),
      (_) => false,
    );
  }

  // ── Build ──

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('设置'),
        centerTitle: true,
      ),
      body: Stack(
        children: [
          ListView(
            children: [
              _buildAvatarTile(),
              _buildDivider(),
              _buildInfoSection(),
              _buildDivider(),
              _buildSecuritySection(),
              _buildDivider(),
              _buildOtherSection(),
              _buildDivider(),
              const SizedBox(height: 20),
              LogoutButton(onPressed: _handleLogout),
              const SizedBox(height: 40),
            ],
          ),
          if (_saving)
            Container(
              color: Colors.black12,
              child: const Center(child: CircularProgressIndicator()),
            ),
        ],
      ),
    );
  }

  Widget _buildDivider() =>
      Container(height: 8, color: const Color(0xFFF5F5F5));

  // ── Avatar tile ──

  Widget _buildAvatarTile() {
    final hasAvatar = _user.avatar != null && _user.avatar!.isNotEmpty;
    String? fullUrl;
    if (hasAvatar) {
      fullUrl = _user.avatar!.startsWith('http')
          ? _user.avatar
          : '${Session.baseUrl}${_user.avatar}';
    }
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      child: GestureDetector(
        onTap: _changeAvatar,
        behavior: HitTestBehavior.opaque,
        child: Row(
          children: [
            const Text('头像',
                style: TextStyle(fontSize: 15, fontWeight: FontWeight.w500)),
            const Spacer(),
            Container(
              width: 56,
              height: 56,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: hasAvatar
                    ? null
                    : const LinearGradient(
                        colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
                      ),
                border: Border.all(color: AppColors.border, width: 1),
              ),
              clipBehavior: Clip.antiAlias,
              child: hasAvatar
                  ? CorsImage(
                      src: fullUrl!,
                      fit: BoxFit.cover,
                      borderRadius: BorderRadius.circular(56),
                      errorBuilder: (_, __, ___) =>
                          const Icon(Icons.person, size: 28, color: Colors.white),
                    )
                  : const Icon(Icons.person, size: 28, color: Colors.white),
            ),
            const SizedBox(width: 8),
            const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

  // ── Info section ──

  Widget _buildInfoSection() {
    return Container(
      color: AppColors.surface,
      child: Column(
        children: [
          _buildInfoTile('昵称', _user.nickname ?? '未设置', () => _editField(
                '昵称', _user.nickname,
                (val) async {
                  final err = await _userApi.updateProfile(nickname: val);
                  if (err == null) setState(() => _user.nickname = val);
                  return err;
                },
              )),
          _buildInfoTile('学号', _user.studentNo ?? '—', null),
          _buildInfoTile('联系方式', _user.contactWay ?? '未设置', () => _editField(
                '联系方式', _user.contactWay,
                (val) async {
                  final err = await _userApi.updateProfile(contactWay: val);
                  if (err == null) setState(() => _user.contactWay = val);
                  return err;
                },
              )),
        ],
      ),
    );
  }

  Widget _buildInfoTile(String label, String value, VoidCallback? onTap) {
    return GestureDetector(
      onTap: onTap,
      behavior: onTap != null ? HitTestBehavior.opaque : HitTestBehavior.deferToChild,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        decoration: const BoxDecoration(
          border: Border(bottom: BorderSide(color: AppColors.border)),
        ),
        child: Row(
          children: [
            Text(label,
                style: const TextStyle(
                    fontSize: 15, fontWeight: FontWeight.w500)),
            const Spacer(),
            Text(value,
                style: const TextStyle(fontSize: 14, color: AppColors.muted)),
            if (onTap != null) ...[
              const SizedBox(width: 8),
              const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
            ],
          ],
        ),
      ),
    );
  }

  // ── Security section (修改密码 + 换绑手机号) ──

  Widget _buildSecuritySection() {
    return Container(
      color: AppColors.surface,
      child: Column(
        children: [
          _buildInfoTile('修改密码', '●●●●●●', _changePassword),
          _buildPhoneTile(),
        ],
      ),
    );
  }

  // ── Other section (检查更新) ──

  Widget _buildOtherSection() {
    return Container(
      color: AppColors.surface,
      child: _buildInfoTile(
        '检查更新',
        _appVersion.isEmpty ? '' : '当前 $_appVersion',
        () => AppUpdater.checkAndPrompt(context, silent: false),
      ),
    );
  }

  Widget _buildPhoneTile() {
    final phone = _user.phone ?? '';
    final masked = phone.length == 11
        ? '${phone.substring(0, 3)}****${phone.substring(7)}'
        : (phone.isEmpty ? '未绑定' : phone);
    return GestureDetector(
      onTap: _changePhone,
      behavior: HitTestBehavior.opaque,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: AppColors.border)),
        ),
        child: Row(
          children: [
            const Text('换绑手机号',
                style: TextStyle(fontSize: 15, fontWeight: FontWeight.w500)),
            const Spacer(),
            Text(masked,
                style: const TextStyle(fontSize: 14, color: AppColors.muted)),
            const SizedBox(width: 8),
            const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

  // ── Change password dialog ──

  Future<void> _changePassword() async {
    final oldPwdCtrl = TextEditingController();
    final newPwdCtrl = TextEditingController();
    final confirmCtrl = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('修改密码'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: oldPwdCtrl,
              obscureText: true,
              decoration: InputDecoration(
                labelText: '当前密码',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                ),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: newPwdCtrl,
              obscureText: true,
              decoration: InputDecoration(
                labelText: '新密码',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                ),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: confirmCtrl,
              obscureText: true,
              decoration: InputDecoration(
                labelText: '确认新密码',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                ),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('确定', style: TextStyle(color: AppColors.accent)),
          ),
        ],
      ),
    );

    oldPwdCtrl.dispose();
    newPwdCtrl.dispose();
    confirmCtrl.dispose();

    if (confirmed != true) return;

    final oldPwd = oldPwdCtrl.text.trim();
    final newPwd = newPwdCtrl.text.trim();
    final confirm = confirmCtrl.text.trim();

    if (oldPwd.isEmpty || newPwd.isEmpty) {
      showToast(context,'请填写完整信息');
      return;
    }
    if (newPwd.length < 6) {
      showToast(context,'新密码长度不能少于6位');
      return;
    }
    if (newPwd != confirm) {
      showToast(context,'两次输入的新密码不一致');
      return;
    }

    setState(() => _saving = true);
    try {
      final error = await _userApi.changePassword(oldPwd, newPwd);
      if (!mounted) return;
      if (error != null) {
        showToast(context,error);
      } else {
        showToast(context,'密码已修改，请重新登录');
        Navigator.of(context).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => const LoginPage()),
          (_) => false,
        );
      }
    } catch (e) {
      showToast(context,e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

}
