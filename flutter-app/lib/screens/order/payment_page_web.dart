import 'dart:async';
import 'dart:html' as html;
import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/services/order_api.dart';
import 'order_detail_page.dart';

class PaymentPage extends StatefulWidget {
  final int orderId;
  final String payHtml;

  const PaymentPage({
    super.key,
    required this.orderId,
    required this.payHtml,
  });

  @override
  State<PaymentPage> createState() => _PaymentPageState();
}

class _PaymentPageState extends State<PaymentPage> {
  final _orderApi = OrderApi.instance;
  Timer? _pollTimer;
  bool _paid = false;
  bool _leaving = false;
  bool _confirming = false;

  @override
  void initState() {
    super.initState();
    _openPaymentWindow();
    _startPolling();
  }

  void _openPaymentWindow() {
    final blob = html.Blob([widget.payHtml], 'text/html');
    final url = html.Url.createObjectUrlFromBlob(blob);
    html.window.open(url, '_blank');
    html.Url.revokeObjectUrl(url);
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(const Duration(seconds: 2), (_) async {
      if (_paid) return;
      final result = await _orderApi.getPayResult(widget.orderId);
      if (!mounted) return;
      if (result != null && result.paymentStatus == '2') {
        _paid = true;
        _pollTimer?.cancel();
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (!mounted) return;
          try {
            _goToOrderDetail();
          } catch (_) {}
        });
      }
    });
  }

  void _goToOrderDetail() {
    _leaving = true;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => OrderDetailPage(orderId: widget.orderId),
      ),
    );
  }

  void _onBack() {
    if (_paid) {
      _goToOrderDetail();
      return;
    }
    setState(() => _confirming = true);
  }

  void _cancelBack() {
    setState(() => _confirming = false);
  }

  void _confirmBack() {
    _leaving = true;
    _confirming = false;
    setState(() {});
    Navigator.of(context).pop(false);
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: _paid || _leaving,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop) _onBack();
      },
      child: Scaffold(
        appBar: AppBar(
          leading: _paid
              ? null
              : IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: _onBack,
                ),
          title: const Text('支付宝支付'),
          centerTitle: true,
          backgroundColor: AppColors.surface,
          foregroundColor: AppColors.fg,
          elevation: 0,
        ),
        body: Stack(
          children: [
            Center(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 32),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (!_paid && !_confirming) ...[
                      const SizedBox(
                        width: 48,
                        height: 48,
                        child: CircularProgressIndicator(strokeWidth: 3),
                      ),
                      const SizedBox(height: 24),
                      Text(
                        '支付窗口已打开',
                        style: TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.w600,
                          color: AppColors.fg,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '请在浏览器弹窗中完成支付，支付完成后将自动返回',
                        textAlign: TextAlign.center,
                        style: TextStyle(fontSize: 14, color: AppColors.muted),
                      ),
                    ],
                    if (_paid) ...[
                      Icon(Icons.check_circle, size: 48, color: AppColors.success),
                      const SizedBox(height: 24),
                      Text(
                        '支付成功',
                        style: TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.w600,
                          color: AppColors.fg,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '即将返回订单列表...',
                        style: TextStyle(fontSize: 14, color: AppColors.muted),
                      ),
                    ],
                  ],
                ),
              ),
            ),
            if (_confirming)
              GestureDetector(
                onTap: _cancelBack,
                child: Container(
                  color: Colors.black26,
                  alignment: Alignment.bottomCenter,
                  child: Container(
                    width: double.infinity,
                    decoration: const BoxDecoration(
                      color: AppColors.surface,
                      borderRadius:
                          BorderRadius.vertical(top: Radius.circular(16)),
                    ),
                    padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
                    child: SafeArea(
                      top: false,
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            width: 36,
                            height: 4,
                            decoration: BoxDecoration(
                              color: AppColors.placeholder,
                              borderRadius: BorderRadius.circular(2),
                            ),
                          ),
                          const SizedBox(height: 20),
                          Text(
                            '放弃支付？',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w700,
                              color: AppColors.fg,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            '支付尚未完成，确定要返回吗？',
                            style:
                                TextStyle(fontSize: 14, color: AppColors.muted),
                          ),
                          const SizedBox(height: 24),
                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: ElevatedButton(
                              onPressed: _cancelBack,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppColors.accent,
                                foregroundColor: Colors.white,
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(
                                      AppColors.radiusPill),
                                ),
                              ),
                              child: const Text('继续支付',
                                  style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w600)),
                            ),
                          ),
                          const SizedBox(height: 12),
                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: OutlinedButton(
                              onPressed: _confirmBack,
                              style: OutlinedButton.styleFrom(
                                foregroundColor: AppColors.muted,
                                side: BorderSide(color: AppColors.border),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(
                                      AppColors.radiusPill),
                                ),
                              ),
                              child: const Text('确定返回',
                                  style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w500)),
                            ),
                          ),
                          const SizedBox(height: 8),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
