import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/toast_utils.dart';
import '../../data/models/order_models.dart';
import '../../data/services/order_api.dart';
import '../../widgets/datetime_picker.dart';

Future<int?> showCreateOrderSheet(
  BuildContext context, {
  required int goodsId,
  required String goodsTitle,
  required double goodsPrice,
  String? tradePlace,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
    ),
    builder: (ctx) => _CreateOrderSheet(
      goodsId: goodsId,
      goodsTitle: goodsTitle,
      goodsPrice: goodsPrice,
      tradePlace: tradePlace,
    ),
  );
}

class _CreateOrderSheet extends StatefulWidget {
  final int goodsId;
  final String goodsTitle;
  final double goodsPrice;
  final String? tradePlace;

  const _CreateOrderSheet({
    required this.goodsId,
    required this.goodsTitle,
    required this.goodsPrice,
    this.tradePlace,
  });

  @override
  State<_CreateOrderSheet> createState() => _CreateOrderSheetState();
}

class _CreateOrderSheetState extends State<_CreateOrderSheet> {
  final _orderApi = OrderApi.instance;
  final _placeCtrl = TextEditingController();
  final _remarkCtrl = TextEditingController();
  String? _appointmentTime;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _placeCtrl.text = widget.tradePlace ?? '';
  }

  @override
  void dispose() {
    _placeCtrl.dispose();
    _remarkCtrl.dispose();
    super.dispose();
  }

  Future<void> _pickDateTime() async {
    final result = await showDateTimePicker(context, initialTime: _appointmentTime);
    if (result != null && mounted) {
      setState(() {
        _appointmentTime = result;
      });
    }
  }

  Future<void> _submit() async {
    if (_submitting) return;
    setState(() => _submitting = true);

    final result = await _orderApi.createOrder(OrderCreateRequest(
      goodsId: widget.goodsId,
      tradePlace: _placeCtrl.text.isNotEmpty ? _placeCtrl.text : null,
      appointmentTime: _appointmentTime,
      buyerRemark: _remarkCtrl.text.isNotEmpty ? _remarkCtrl.text : null,
    ));

    if (!mounted) return;
    setState(() => _submitting = false);

    if (result.success) {
      final orderId = (result.data is Map<String, dynamic>)
          ? (result.data as Map<String, dynamic>)['orderId']
          : null;
      Navigator.pop(context, orderId);
    } else {
      showToast(context, result.data?.toString() ?? '下单失败');
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return Padding(
      padding: EdgeInsets.only(bottom: bottomInset),
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 36,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.placeholder,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                '确认下单',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                  color: AppColors.fg,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                widget.goodsTitle,
                style: TextStyle(fontSize: 14, color: AppColors.muted),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 4),
              Text(
                '¥${widget.goodsPrice.toStringAsFixed(2)}',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: AppColors.price,
                ),
              ),
              const SizedBox(height: 20),
              TextField(
                controller: _placeCtrl,
                decoration: InputDecoration(
                  labelText: '交易地点',
                  hintText: '如：图书馆一楼',
                  prefixIcon: Icon(Icons.location_on_outlined,
                      color: AppColors.muted, size: 20),
                ),
              ),
              const SizedBox(height: 14),
              InkWell(
                onTap: _pickDateTime,
                borderRadius: BorderRadius.circular(AppColors.radiusSm),
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(AppColors.radiusSm),
                    border: Border.all(color: AppColors.border, width: 1.5),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.schedule_outlined,
                          size: 20, color: AppColors.muted),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _appointmentTime ?? '预约交易时间（可选）',
                          style: TextStyle(
                            fontSize: 15,
                            color: _appointmentTime != null
                                ? AppColors.fg
                                : AppColors.placeholder,
                          ),
                        ),
                      ),
                      Icon(Icons.chevron_right,
                          size: 18, color: AppColors.muted),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),
              TextField(
                controller: _remarkCtrl,
                maxLines: 3,
                decoration: InputDecoration(
                  labelText: '买家备注（可选）',
                  hintText: '对商品或交易的补充说明...',
                  alignLabelWithHint: true,
                  prefixIcon: Icon(Icons.notes_outlined,
                      color: AppColors.muted, size: 20),
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                height: 48,
                child: ElevatedButton(
                  onPressed: _submitting ? null : _submit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.accent,
                    foregroundColor: Colors.white,
                    elevation: 0,
                    shape: RoundedRectangleBorder(
                      borderRadius:
                          BorderRadius.circular(AppColors.radiusPill),
                    ),
                    textStyle: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  child: _submitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : const Text('确认下单'),
                ),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }
}
