import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class AppFormField extends StatelessWidget {
  final String label;
  final TextEditingController controller;
  final String hint;
  final bool obscure;
  final Widget? suffix;
  final int? maxLength;
  final int maxLines;
  final TextInputType keyboardType;
  final String? Function(String?)? validator;

  const AppFormField({
    super.key,
    required this.label,
    required this.controller,
    required this.hint,
    this.obscure = false,
    this.suffix,
    this.maxLength,
    this.maxLines = 1,
    this.keyboardType = TextInputType.text,
    this.validator,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 2, bottom: 6),
          child: Text(label,
              style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: AppColors.fg)),
        ),
        if (validator != null)
          TextFormField(
            controller: controller,
            obscureText: obscure,
            maxLength: maxLength,
            maxLines: maxLines,
            keyboardType: keyboardType,
            validator: validator,
            style: const TextStyle(fontSize: 15, color: AppColors.fg),
            decoration: InputDecoration(
              hintText: hint,
              counterText: '',
              suffixIcon: suffix,
            ),
          )
        else
          TextField(
            controller: controller,
            obscureText: obscure,
            maxLength: maxLength,
            maxLines: maxLines,
            keyboardType: keyboardType,
            style: const TextStyle(fontSize: 15, color: AppColors.fg),
            decoration: InputDecoration(
              hintText: hint,
              counterText: '',
              suffixIcon: suffix,
            ),
          ),
      ],
    );
  }
}
