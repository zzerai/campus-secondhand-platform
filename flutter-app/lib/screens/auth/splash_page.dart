import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/services/auth_api.dart';
import '../../data/services/user_api.dart';
import '../../data/services/session.dart';
import '../../data/services/storage_service.dart';
import '../home/home_page.dart';
import 'login_page.dart';

class SplashPage extends StatefulWidget {
  const SplashPage({super.key});

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  @override
  void initState() {
    super.initState();
    _checkAuth();
  }

  Future<void> _checkAuth() async {
    final token = await StorageService.getToken();
    if (token == null || token.isEmpty) {
      _goTo(LoginPage);
      return;
    }

    final user = await StorageService.getUser();
    final session = Session.instance;
    session.token = token;
    if (user != null) {
      session.currentUser = user;
    }

    try {
      await UserApi.instance.getUserProfile();
      _goTo(HomePage);
    } catch (_) {
      await StorageService.clear();
      AuthApi.instance.logout();
      _goTo(LoginPage);
    }
  }

  void _goTo(Type page) {
    if (!mounted) return;
    final Widget target;
    if (page == HomePage) {
      target = const HomePage();
    } else {
      target = const LoginPage();
    }
    Navigator.of(context).pushReplacement(
      PageRouteBuilder(
        pageBuilder: (_, __, ___) => target,
        transitionDuration: const Duration(milliseconds: 300),
        transitionsBuilder: (_, animation, __, child) =>
            FadeTransition(opacity: animation, child: child),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: AppColors.accent,
                borderRadius: BorderRadius.circular(18),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFC47A38).withValues(alpha: 0.25),
                    blurRadius: 24,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: const Icon(Icons.shopping_bag_rounded,
                  size: 40, color: Colors.white),
            ),
            const SizedBox(height: 20),
            const Text(
              'SecondU',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.w700,
                letterSpacing: -0.48,
                color: AppColors.fg,
              ),
            ),
            const SizedBox(height: 32),
            const SizedBox(
              width: 22,
              height: 22,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ],
        ),
      ),
    );
  }
}
