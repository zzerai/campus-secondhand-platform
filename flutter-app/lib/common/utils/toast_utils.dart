import 'package:flutter/material.dart';
import '../constants/app_theme.dart';

void showToast(BuildContext context, String msg) {
  if (!context.mounted) return;
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text(msg, textAlign: TextAlign.center),
      duration: const Duration(seconds: 2),
      behavior: SnackBarBehavior.floating,
      width: 280,
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppColors.radiusPill)),
    ));
}
