import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class LoadMoreIndicator extends StatelessWidget {
  final EdgeInsetsGeometry padding;

  const LoadMoreIndicator({
    super.key,
    this.padding = const EdgeInsets.symmetric(vertical: 16),
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: padding,
      child: const Center(
        child: SizedBox(
          width: 24,
          height: 24,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            color: AppColors.accent,
          ),
        ),
      ),
    );
  }
}
