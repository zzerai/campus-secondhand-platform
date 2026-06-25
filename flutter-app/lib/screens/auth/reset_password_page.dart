import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/services/auth_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/sms_code_button.dart';
import '../../widgets/submit_button.dart';
import '../../widgets/brand_header.dart';
import '../../widgets/app_form_field.dart';

class ResetPasswordPage extends StatefulWidget {
  const ResetPasswordPage({super.key});

  @override
  State<ResetPasswordPage> createState() => _ResetPasswordPageState();
}

class _ResetPasswordPageState extends State<ResetPasswordPage> {
  final _authApi = AuthApi.instance;

  final _phoneCtrl = TextEditingController();
  final _codeCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmPwdCtrl = TextEditingController();

  bool _pwdVisible = false;
  bool _confirmPwdVisible = false;
  bool _loading = false;

  @override
  void dispose() {
    _phoneCtrl.dispose();
    _codeCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmPwdCtrl.dispose();
    super.dispose();
  }

  Future<void> _handleReset() async {
    final phone = _phoneCtrl.text.trim();
    final code = _codeCtrl.text.trim();
    final password = _passwordCtrl.text;
    final confirmPwd = _confirmPwdCtrl.text;

    if (phone.isEmpty || code.isEmpty || password.isEmpty || confirmPwd.isEmpty) {
      showToast(context, '请填写完整信息');
      return;
    }
    if (password.length < 6) {
      showToast(context, '密码至少6位');
      return;
    }
    if (password != confirmPwd) {
      showToast(context, '两次密码不一致');
      return;
    }
    if (code.length < 4) {
      showToast(context, '验证码格式错误');
      return;
    }

    setState(() => _loading = true);

    try {
      final result = await _authApi.resetPassword(phone, code, password);
      if (!mounted) return;
      if (result == 'success') {
        showToast(context, '重置成功，请用新密码登录');
        Navigator.of(context).pop();
      } else {
        showToast(context, result);
      }
    } catch (e) {
      if (!mounted) return;
      showToast(context, e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 42),
            const BrandHeader(subtitle: '通过短信验证码重置密码'),
            const SizedBox(height: 28),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Column(
                  children: [
                    AppFormField(
                      controller: _phoneCtrl,
                      label: '已注册手机号',
                      hint: '输入已注册的手机号',
                      maxLength: 11,
                      keyboardType: TextInputType.phone,
                    ),
                    const SizedBox(height: 14),
                    AppFormField(
                      controller: _codeCtrl,
                      label: '短信验证码',
                      hint: '输入验证码',
                      maxLength: 6,
                      suffix: SmsCodeButton(
                        getPhone: () => _phoneCtrl.text,
                        scene: 'reset_password',
                      ),
                    ),
                    const SizedBox(height: 14),
                    AppFormField(
                      controller: _passwordCtrl,
                      label: '新密码',
                      hint: '6-20位字母或数字',
                      obscure: !_pwdVisible,
                      suffix: _pwdToggle(
                        () => setState(() => _pwdVisible = !_pwdVisible),
                        _pwdVisible,
                      ),
                    ),
                    const SizedBox(height: 14),
                    AppFormField(
                      controller: _confirmPwdCtrl,
                      label: '确认新密码',
                      hint: '再次输入新密码',
                      obscure: !_confirmPwdVisible,
                      suffix: _pwdToggle(
                        () => setState(() => _confirmPwdVisible = !_confirmPwdVisible),
                        _confirmPwdVisible,
                      ),
                    ),
                    const SizedBox(height: 20),
                    SubmitButton(
                      onPressed: _handleReset,
                      isLoading: _loading,
                      label: '重置密码',
                    ),
                    _buildSwitchLink(),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _pwdToggle(VoidCallback onTap, bool visible) {
    return GestureDetector(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Image.asset(
          visible ? 'assets/display.png' : 'assets/hidden.png',
          width: 22,
          height: 22,
        ),
      ),
    );
  }

  Widget _buildSwitchLink() {
    return Padding(
      padding: const EdgeInsets.only(top: 20),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            '想起密码了？',
            style: TextStyle(fontSize: 14, color: AppColors.muted),
          ),
          GestureDetector(
            onTap: () => Navigator.of(context).pop(),
            child: const Text(
              '返回登录',
              style: TextStyle(
                fontSize: 14,
                color: AppColors.accent,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
