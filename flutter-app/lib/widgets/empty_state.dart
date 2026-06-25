import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class EmptyState extends StatelessWidget {
  final IconData icon;
  final String message;

  const EmptyState({
    super.key,
    this.icon = Icons.inventory_2_outlined,
    this.message = '暂无商品',
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 48, color: AppColors.placeholder),
          const SizedBox(height: 12),
          Text(message, style: const TextStyle(color: AppColors.muted)),
        ],
      ),
    );
  }
}
