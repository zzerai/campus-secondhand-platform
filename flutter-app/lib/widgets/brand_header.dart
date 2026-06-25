import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class BrandHeader extends StatelessWidget {
  final String subtitle;
  final double iconSize;
  final double fontSize;
  final double borderRadius;

  const BrandHeader({
    super.key,
    this.subtitle = '让闲置在校园里流转',
    this.iconSize = 64,
    this.fontSize = 26,
    this.borderRadius = 18,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: iconSize,
          height: iconSize,
          decoration: BoxDecoration(
            color: AppColors.accent,
            borderRadius: BorderRadius.circular(borderRadius),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFFC47A38).withValues(alpha: 0.25),
                blurRadius: 24,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          child: Icon(Icons.shopping_bag_rounded,
              size: iconSize * 0.6, color: Colors.white),
        ),
        const SizedBox(height: 16),
        Text(
          'SecondU',
          style: TextStyle(
            fontSize: fontSize,
            fontWeight: FontWeight.w700,
            letterSpacing: -fontSize * 0.02,
            color: AppColors.fg,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          subtitle,
          style: const TextStyle(fontSize: 14, color: AppColors.muted),
        ),
      ],
    );
  }
}
