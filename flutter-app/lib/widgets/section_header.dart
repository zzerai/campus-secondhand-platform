import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class SectionHeader extends StatelessWidget {
  final String title;
  final String? trailing;
  final VoidCallback? onTrailingTap;
  final bool showChevron;
  final EdgeInsetsGeometry padding;
  final TextStyle? titleStyle;
  final TextStyle? trailingStyle;

  const SectionHeader({
    super.key,
    required this.title,
    this.trailing,
    this.onTrailingTap,
    this.showChevron = true,
    this.padding = const EdgeInsets.only(right: 16),
    this.titleStyle,
    this.trailingStyle,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: padding,
      child: Row(
        children: [
          Text(
            title,
            style: titleStyle ??
                const TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w700,
                  color: AppColors.fg,
                  letterSpacing: -0.2,
                ),
          ),
          const Spacer(),
          if (trailing != null)
            GestureDetector(
              onTap: onTrailingTap,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    trailing!,
                    style: trailingStyle ??
                        const TextStyle(fontSize: 13, color: AppColors.muted),
                  ),
                  if (showChevron)
                    const Icon(Icons.chevron_right, size: 16, color: AppColors.muted),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
