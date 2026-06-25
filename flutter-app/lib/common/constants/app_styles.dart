import 'package:flutter/material.dart';
import 'app_theme.dart';

class AppTextStyles {
  static const pageTitle = TextStyle(
    fontSize: 20, fontWeight: FontWeight.w700, color: AppColors.fg,
  );
  static const sectionTitle = TextStyle(
    fontSize: 17, fontWeight: FontWeight.w700, color: AppColors.fg,
    letterSpacing: -0.2,
  );
  static const cardTitle = TextStyle(
    fontSize: 15, fontWeight: FontWeight.w600, color: AppColors.fg,
  );
  static const bodyText = TextStyle(
    fontSize: 14, color: AppColors.fg,
  );
  static const muted = TextStyle(
    fontSize: 13, color: AppColors.muted,
  );
  static const smallMuted = TextStyle(
    fontSize: 11, color: AppColors.muted,
  );
  static const buttonLabel = TextStyle(
    fontSize: 16, fontWeight: FontWeight.w600,
  );
  static const tabLabel = TextStyle(
    fontSize: 14, fontWeight: FontWeight.w500,
  );
  static const priceLarge = TextStyle(
    fontSize: 18, fontWeight: FontWeight.w700, color: AppColors.price,
    letterSpacing: -0.36,
  );
  static const priceSmall = TextStyle(
    fontSize: 13, fontWeight: FontWeight.w600,
  );
  static const inputLabel = TextStyle(
    fontSize: 13, fontWeight: FontWeight.w600, color: AppColors.fg,
  );
}

class AppSpacing {
  static const hPad = EdgeInsets.symmetric(horizontal: 16);
  static const pageH = EdgeInsets.symmetric(horizontal: 24);
  static const cardPad = EdgeInsets.all(12);
  static const tilePad = EdgeInsets.symmetric(horizontal: 16, vertical: 14);
  static const listPad = EdgeInsets.symmetric(horizontal: 16, vertical: 4);
  static const sectionTop = EdgeInsets.fromLTRB(16, 20, 16, 4);
  static const headerPad = EdgeInsets.symmetric(horizontal: 16, vertical: 10);

  static const gap4 = SizedBox(height: 4);
  static const gap6 = SizedBox(height: 6);
  static const gap8 = SizedBox(height: 8);
  static const gap10 = SizedBox(height: 10);
  static const gap12 = SizedBox(height: 12);
  static const gap16 = SizedBox(height: 16);
  static const gap20 = SizedBox(height: 20);
  static const gap24 = SizedBox(height: 24);

  static const gapH4 = SizedBox(width: 4);
  static const gapH6 = SizedBox(width: 6);
  static const gapH8 = SizedBox(width: 8);
  static const gapH10 = SizedBox(width: 10);
  static const gapH12 = SizedBox(width: 12);
  static const gapH16 = SizedBox(width: 16);
  static const gapH24 = SizedBox(width: 24);

  static const sectionGap = SizedBox(height: 8);
}

class AppShadows {
  static BoxShadow buttonShadow = BoxShadow(
    color: const Color(0xFFC47A38).withValues(alpha: 0.35),
    blurRadius: 12, offset: const Offset(0, 4),
  );
  static BoxShadow cardShadow = BoxShadow(
    color: const Color(0xFFC47A38).withValues(alpha: 0.25),
    blurRadius: 24, offset: const Offset(0, 8),
  );
  static BoxShadow avatarShadow = BoxShadow(
    color: const Color(0xFFC47A38).withValues(alpha: 0.2),
    blurRadius: 16, offset: const Offset(0, 4),
  );
}

class AppDecorations {
  static BoxDecoration sectionGap = const BoxDecoration(
    border: Border(top: BorderSide(color: AppColors.border)),
  );

  static BoxDecoration get card => BoxDecoration(
    color: AppColors.surface,
    borderRadius: BorderRadius.circular(AppColors.radiusMd),
  );

  static BoxDecoration get imagePlaceholder => const BoxDecoration(
    gradient: LinearGradient(
      colors: [Color(0xFFF0E8F5), Color(0xFFF0E2F5)],
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
    ),
  );

  static BoxDecoration get avatarGradient => const BoxDecoration(
    gradient: LinearGradient(
      colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
    ),
  );

  static BoxDecoration get sideBarBg => const BoxDecoration(
    color: Color(0xFFF0E8F5),
  );
}
