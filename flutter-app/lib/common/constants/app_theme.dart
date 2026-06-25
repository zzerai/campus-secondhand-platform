import 'package:flutter/material.dart';

class AppColors {
  static const Color bg = Color(0xFFF5F5F5);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color fg = Color(0xFF1A0E28);
  static const Color muted = Color(0xFF8E7798);
  static const Color border = Color(0xFFF0E2F5);
  static const Color accent = Color(0xFFFF5722);
  static const Color price = Color(0xFFF43370);
  static const Color success = Color(0xFF00C48C);
  static const Color placeholder = Color(0xFFC4B0CC);
  static const Color divider = Color(0xFFEBEBEB);

  static const double radiusSm = 8;
  static const double radiusMd = 12;
  static const double radiusLg = 16;
  static const double radiusPill = 9999;

  static InputDecorationTheme inputTheme = InputDecorationTheme(
    filled: true,
    fillColor: surface,
    contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(radiusMd),
      borderSide: const BorderSide(color: border, width: 1.5),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(radiusMd),
      borderSide: const BorderSide(color: border, width: 1.5),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(radiusMd),
      borderSide: const BorderSide(color: placeholder, width: 1.5),
    ),
    hintStyle: const TextStyle(color: placeholder, fontSize: 15),
    labelStyle: const TextStyle(color: fg, fontSize: 13, fontWeight: FontWeight.w600),
    floatingLabelBehavior: FloatingLabelBehavior.never,
  );
}

ThemeData appTheme() {
  return ThemeData(
    colorSchemeSeed: AppColors.accent,
    useMaterial3: true,
    scaffoldBackgroundColor: AppColors.bg,
    inputDecorationTheme: AppColors.inputTheme,
    // 使用系统默认字体（Roboto/NotoSansSC 本地文件缺失，暂注释）
  );
}
