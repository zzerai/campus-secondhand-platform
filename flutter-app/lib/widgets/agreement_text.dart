import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class AgreementText extends StatelessWidget {
  final String prefix;

  const AgreementText({super.key, this.prefix = '登录即表示同意'});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 24),
      child: RichText(
        textAlign: TextAlign.center,
        text: TextSpan(
          style: const TextStyle(fontSize: 12, color: AppColors.muted),
          children: [
            TextSpan(text: '$prefix\n'),
            const TextSpan(
              text: '《用户协议》',
              style: TextStyle(color: AppColors.accent),
            ),
            const TextSpan(text: ' 和 '),
            const TextSpan(
              text: '《隐私政策》',
              style: TextStyle(color: AppColors.accent),
            ),
          ],
        ),
      ),
    );
  }
}
