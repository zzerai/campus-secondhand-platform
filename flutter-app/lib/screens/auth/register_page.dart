import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/auth_models.dart';
import '../../data/services/auth_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/sms_code_button.dart';
import '../../widgets/submit_button.dart';
import '../../widgets/brand_header.dart';
import '../../widgets/agreement_text.dart';
import '../../widgets/app_form_field.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _authApi = AuthApi.instance;

  final _studentIdCtrl = TextEditingController();
  final _nicknameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmPwdCtrl = TextEditingController();
  final _codeCtrl = TextEditingController();

  bool _pwdVisible = false;
  bool _confirmPwdVisible = false;
  bool _loading = false;

  @override
  void dispose() {
    _studentIdCtrl.dispose();
    _nicknameCtrl.dispose();
    _phoneCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmPwdCtrl.dispose();
    _codeCtrl.dispose();
    super.dispose();
  }

  Future<void> _handleRegister() async {
    final studentId = _studentIdCtrl.text.trim();
    final nickname = _nicknameCtrl.text.trim();
    final phone = _phoneCtrl.text.trim();
    final password = _passwordCtrl.text;
    final confirmPwd = _confirmPwdCtrl.text;
    final code = _codeCtrl.text.trim();

    if (studentId.isEmpty ||
        nickname.isEmpty ||
        phone.isEmpty ||
        password.isEmpty ||
        confirmPwd.isEmpty ||
        code.isEmpty) {
      showToast(context,'请填写完整信息');
      return;
    }
    if (password.length < 6) {
      showToast(context,'密码至少6位');
      return;
    }
    if (password != confirmPwd) {
      showToast(context,'两次密码不一致');
      return;
    }
    if (code.length < 4) {
      showToast(context,'验证码格式错误');
      return;
    }

    setState(() => _loading = true);

    try {
      final request = RegisterRequest(
        studentNo: studentId,
        phone: phone,
        password: password,
        nickname: nickname,
        smsCode: code,
      );
      final result = await _authApi.register(request);
      if (!mounted) return;
      if (result == 'success') {
        showToast(context,'注册成功');
        Navigator.of(context).pop(true);
      } else {
        showToast(context,result);
      }
    } catch (e) {
      if (!mounted) return;
      showToast(context,e.toString().replaceFirst('Exception: ', ''));
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
            const BrandHeader(subtitle: '加入校园闲置交易社区'),
            const SizedBox(height: 28),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Column(
                  children: [
                    AppFormField(controller: _studentIdCtrl, label: '学号', hint: '输入学号', maxLength: 20),
                    const SizedBox(height: 14),
                    AppFormField(controller: _nicknameCtrl, label: '昵称', hint: '给自己起个名字吧',
                        maxLength: 20),
                    const SizedBox(height: 14),
                    AppFormField(controller: _phoneCtrl, label: '手机号', hint: '输入手机号',
                        maxLength: 11, keyboardType: TextInputType.phone),
                    const SizedBox(height: 14),
                    AppFormField(controller: _passwordCtrl, label: '密码', hint: '6-20位字母或数字',
                        obscure: !_pwdVisible,
                        suffix: _pwdToggle(
                            () => setState(() => _pwdVisible = !_pwdVisible),
                            _pwdVisible)),
                    const SizedBox(height: 14),
                    AppFormField(controller: _confirmPwdCtrl, label: '确认密码', hint: '再次输入密码',
                        obscure: !_confirmPwdVisible,
                        suffix: _pwdToggle(
                            () => setState(
                                () => _confirmPwdVisible = !_confirmPwdVisible),
                            _confirmPwdVisible)),
                    const SizedBox(height: 14),
                    AppFormField(controller: _codeCtrl, label: '验证码', hint: '输入验证码',
                        maxLength: 6,
                        suffix: SmsCodeButton(
                          getPhone: () => _phoneCtrl.text,
                          scene: 'register',
                        )),
                    const SizedBox(height: 16),
                    SubmitButton(
                      onPressed: _handleRegister,
                      isLoading: _loading,
                      label: '注 册',
                    ),
                    _buildSwitchLink(),
                    const AgreementText(prefix: '注册即表示同意'),
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
            '已有账号？',
            style: TextStyle(fontSize: 14, color: AppColors.muted),
          ),
          GestureDetector(
            onTap: () => Navigator.of(context).pop(),
            child: const Text(
              '返回登录',
              style: TextStyle(
                  fontSize: 14,
                  color: AppColors.accent,
                  fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }

}
