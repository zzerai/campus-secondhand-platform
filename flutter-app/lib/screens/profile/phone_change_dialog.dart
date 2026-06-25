import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/sms_code_button.dart';

class PhoneChangeDialog extends StatefulWidget {
  final TextEditingController phoneCtrl;
  final TextEditingController codeCtrl;

  const PhoneChangeDialog({
    super.key,
    required this.phoneCtrl,
    required this.codeCtrl,
  });

  @override
  State<PhoneChangeDialog> createState() => _PhoneChangeDialogState();
}

class _PhoneChangeDialogState extends State<PhoneChangeDialog> {
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('换绑手机号'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: widget.phoneCtrl,
            keyboardType: TextInputType.phone,
            maxLength: 11,
            decoration: InputDecoration(
              labelText: '新手机号',
              hintText: '请输入新手机号',
              counterText: '',
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppColors.radiusSm),
              ),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: widget.codeCtrl,
            keyboardType: TextInputType.number,
            maxLength: 6,
            decoration: InputDecoration(
              labelText: '验证码',
              hintText: '请输入验证码',
              counterText: '',
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(AppColors.radiusSm),
              ),
              suffixIcon: SmsCodeButton(
                getPhone: () => widget.phoneCtrl.text,
                scene: 'change_phone',
              ),
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: const Text('取消'),
        ),
        TextButton(
          onPressed: () => Navigator.pop(context, true),
          child:
              const Text('确定', style: TextStyle(color: AppColors.accent)),
        ),
      ],
    );
  }
}
