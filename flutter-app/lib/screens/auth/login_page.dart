import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/auth_models.dart';
import '../../data/services/auth_api.dart';
import '../../data/services/session.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/sms_code_button.dart';
import '../../widgets/submit_button.dart';
import '../../widgets/brand_header.dart';
import '../../widgets/agreement_text.dart';
import '../../widgets/app_form_field.dart';
import '../../data/services/storage_service.dart';
import 'register_page.dart';
import 'reset_password_page.dart';
import '../home/home_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage>
    with SingleTickerProviderStateMixin {
  final _authApi = AuthApi.instance;

  late final TabController _tabController;

  final _pwdAccountCtrl = TextEditingController();
  final _pwdPasswordCtrl = TextEditingController();
  final _codeAccountCtrl = TextEditingController();
  final _codeCtrl = TextEditingController();

  bool _pwdVisible = false;
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _pwdAccountCtrl.dispose();
    _pwdPasswordCtrl.dispose();
    _codeAccountCtrl.dispose();
    _codeCtrl.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final isPwd = _tabController.index == 0;
    final account =
        isPwd ? _pwdAccountCtrl.text.trim() : _codeAccountCtrl.text.trim();

    if (account.isEmpty) {
      showToast(context,'请输入学号或手机号');
      return;
    }

    if (isPwd) {
      final pwd = _pwdPasswordCtrl.text.trim();
      if (pwd.isEmpty) {
        showToast(context,'请输入密码');
        return;
      }
    } else {
      final code = _codeCtrl.text.trim();
      if (code.isEmpty || code.length < 4) {
        showToast(context,'验证码格式错误');
        return;
      }
    }

    setState(() => _loading = true);

    try {
      final request = LoginRequest(
        username: account,
        password: isPwd ? _pwdPasswordCtrl.text.trim() : null,
        code: isPwd ? null : _codeCtrl.text.trim(),
      );
      final result = await _authApi.login(request);
      if (result.user != null) {
        Session.instance.currentUser = result.user!;
        await StorageService.saveAuth(result.token, result.user!);
      }
      if (!mounted) return;
      showToast(context,'登录成功');
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const HomePage()),
      );
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
            const SizedBox(height: 56),
            const BrandHeader(),
            const SizedBox(height: 36),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Column(
                  children: [
                    _buildTabs(),
                    const SizedBox(height: 28),
                    SizedBox(
                      height: 280,
                      child: TabBarView(
                        controller: _tabController,
                        children: [_buildPwdPanel(), _buildCodePanel()],
                      ),
                    ),
                    _buildSwitchLink(),
                    const AgreementText(),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }


  Widget _buildTabs() {
    return Container(
      decoration: const BoxDecoration(
        border: Border(
          bottom: BorderSide(color: AppColors.border, width: 2),
        ),
      ),
      child: TabBar(
        controller: _tabController,
        indicatorSize: TabBarIndicatorSize.label,
        indicatorColor: AppColors.accent,
        indicatorWeight: 2,
        labelColor: AppColors.accent,
        unselectedLabelColor: AppColors.muted,
        labelStyle:
            const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
        unselectedLabelStyle:
            const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
        tabs: const [
          Tab(text: '密码登录'),
          Tab(text: '验证码登录'),
        ],
      ),
    );
  }

  Widget _buildPwdPanel() {
    return Column(
      children: [
        AppFormField(
          label: '学号 / 手机号',
          controller: _pwdAccountCtrl,
          hint: '输入学号或手机号',
        ),
        const SizedBox(height: 16),
        AppFormField(
          label: '密码',
          controller: _pwdPasswordCtrl,
          hint: '输入密码',
          obscure: !_pwdVisible,
          suffix: GestureDetector(
            onTap: () => setState(() => _pwdVisible = !_pwdVisible),
            child: Padding(
              padding: const EdgeInsets.all(10),
              child: Image.asset(
                _pwdVisible ? 'assets/display.png' : 'assets/hidden.png',
                width: 22,
                height: 22,
              ),
            ),
          ),
        ),
        const SizedBox(height: 4),
        Align(
          alignment: Alignment.centerRight,
          child: GestureDetector(
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ResetPasswordPage()),
              );
            },
            child: const Text(
              '忘记密码？',
              style: AppTextStyles.muted,
            ),
          ),
        ),
        const SizedBox(height: 16),
        SubmitButton(
          onPressed: _handleLogin,
          isLoading: _loading,
          label: '登 录',
        ),
      ],
    );
  }

  Widget _buildCodePanel() {
    return Column(
      children: [
        AppFormField(
          label: '手机号',
          controller: _codeAccountCtrl,
          hint: '输入手机号',
        ),
        const SizedBox(height: 16),
        AppFormField(
          label: '验证码',
          controller: _codeCtrl,
          hint: '输入验证码',
          maxLength: 6,
          suffix: SmsCodeButton(
            getPhone: () => _codeAccountCtrl.text,
            scene: 'login',
          ),
        ),
        const SizedBox(height: 16),
        SubmitButton(
          onPressed: _handleLogin,
          isLoading: _loading,
          label: '登 录',
        ),
      ],
    );
  }

  Widget _buildSwitchLink() {
    return Padding(
      padding: const EdgeInsets.only(top: 24),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text(
            '没有账号？',
            style: TextStyle(fontSize: 14, color: AppColors.muted),
          ),
          GestureDetector(
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const RegisterPage()),
              );
            },
            child: const Text(
              '立即注册',
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
