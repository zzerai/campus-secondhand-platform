import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class AppSearchBar extends StatelessWidget {
  final String hint;
  final VoidCallback? onTap;
  final double height;
  final Color? backgroundColor;

  const AppSearchBar({
    super.key,
    this.hint = '搜索',
    this.onTap,
    this.height = 36,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: height,
        padding: const EdgeInsets.symmetric(horizontal: 14),
        decoration: BoxDecoration(
          color: backgroundColor ?? AppColors.divider,
          borderRadius: BorderRadius.circular(AppColors.radiusPill),
        ),
        child: Row(
          children: [
            const Icon(Icons.search, size: 15, color: AppColors.muted),
            const SizedBox(width: 6),
            Flexible(
              child: Text(
                hint,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 13, color: AppColors.muted),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
