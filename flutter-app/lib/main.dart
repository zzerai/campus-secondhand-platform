import 'package:flutter/material.dart';
import 'screens/auth/splash_page.dart';
import 'common/constants/app_theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const FlutterApp());
}

class FlutterApp extends StatelessWidget {
  const FlutterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SecondU',
      debugShowCheckedModeBanner: false,
      theme: appTheme(),
      home: const SplashPage(),
    );
  }
}
