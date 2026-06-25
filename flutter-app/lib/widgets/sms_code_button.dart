import 'dart:async';
import 'package:flutter/material.dart';
import '../data/services/auth_api.dart';
import '../common/constants/app_theme.dart';
import '../common/utils/toast_utils.dart';

class SmsCodeButton extends StatefulWidget {
  final String Function() getPhone;
  final String scene;

  const SmsCodeButton({
    super.key,
    required this.getPhone,
    required this.scene,
  });

  @override
  State<SmsCodeButton> createState() => _SmsCodeButtonState();
}

class _SmsCodeButtonState extends State<SmsCodeButton> {
  bool _counting = false;
  int _seconds = 60;
  Timer? _timer;

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _sendCode() async {
    if (_counting) return;
    final phone = widget.getPhone().trim();
    if (phone.isEmpty) {
      showToast(context, '请输入手机号');
      return;
    }
    if (!RegExp(r'^1[3-9]\d{9}$').hasMatch(phone)) {
      showToast(context, '手机号格式不正确');
      return;
    }
    try {
      await AuthApi.instance.sendSmsCode(phone, widget.scene);
    } catch (e) {
      if (!mounted) return;
      showToast(context, e.toString().replaceFirst('Exception: ', ''));
      return;
    }
    if (!mounted) return;
    showToast(context, '验证码已发送');
    setState(() {
      _counting = true;
      _seconds = 60;
    });
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!mounted) {
        timer.cancel();
        return;
      }
      setState(() {
        _seconds--;
        if (_seconds <= 0) {
          _counting = false;
          timer.cancel();
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 4),
      child: IntrinsicWidth(
        child: SizedBox(
          height: 34,
          child: TextButton(
          onPressed: _counting ? null : _sendCode,
          style: TextButton.styleFrom(
            backgroundColor: _counting ? AppColors.border : AppColors.accent,
            foregroundColor: _counting ? AppColors.muted : Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppColors.radiusPill),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 10),
            minimumSize: Size.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
          child: Text(
            _counting ? '${_seconds}s后重发' : '获取验证码',
            style:
                const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
          ),
        ),
      ),
    ),
    );
  }
}
