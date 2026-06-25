import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../data/services/dispute_api.dart';
import '../../widgets/cors_image.dart';
import '../order/order_detail_page.dart';

class DisputeSubmitPage extends StatefulWidget {
  final int? orderId;
  final int goodsId;
  final String? goodsTitle;
  final String? sellerName;
  final String? sellerAvatar;

  const DisputeSubmitPage({
    super.key,
    this.orderId,
    required this.goodsId,
    this.goodsTitle,
    this.sellerName,
    this.sellerAvatar,
  });

  @override
  State<DisputeSubmitPage> createState() => _DisputeSubmitPageState();
}

class _DisputeSubmitPageState extends State<DisputeSubmitPage> {
  final _disputeApi = DisputeApi.instance;
  final _contentCtrl = TextEditingController();
  String _disputeType = '未交货';
  bool _submitting = false;

  static const _types = ['未交货', '商品不符', '付款问题', '其他'];

  @override
  void dispose() {
    _contentCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final orderId = widget.orderId;
    if (orderId == null) {
      showToast(context, '缺少订单信息');
      return;
    }
    if (_contentCtrl.text.trim().isEmpty) {
      showToast(context, '请填写争议描述');
      return;
    }

    setState(() => _submitting = true);
    try {
      final result = await _disputeApi.submitDispute(
        orderId: orderId,
        disputeType: _disputeType,
        disputeContent: _contentCtrl.text.trim(),
      );
      if (!mounted) return;
      if (result.success) {
        showToast(context, '争议提交成功');
        Navigator.of(context).pop(true);
      } else {
        showToast(context, result.data?.toString() ?? '提交失败');
      }
    } catch (e) {
      if (mounted) showToast(context, '提交失败，请重试');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('申请仲裁'),
        centerTitle: true,
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (widget.goodsTitle != null) _buildTargetCard(),
          if (widget.goodsTitle != null) const SizedBox(height: 12),
          _buildTypeSelector(),
          const SizedBox(height: 16),
          _buildContentField(),
          const SizedBox(height: 24),
          _buildSubmitButton(),
          const SizedBox(height: 16),
          const Text(
            '提交后将由AI先行分析，必要时转入人工仲裁。请如实填写。',
            style: TextStyle(fontSize: 12, color: AppColors.muted),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildSellerAvatar() {
    final avatar = widget.sellerAvatar;
    final hasAvatar = avatar != null && avatar.isNotEmpty;
    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: hasAvatar
            ? null
            : const LinearGradient(
                colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
      ),
      clipBehavior: Clip.antiAlias,
      child: hasAvatar
          ? CorsImage(
              src: ensureAbsoluteUrl(avatar),
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) =>
                  const Icon(Icons.person, size: 22, color: Colors.white),
            )
          : const Icon(Icons.person, size: 22, color: Colors.white),
    );
  }

  Widget _buildTargetCard() {
    final orderId = widget.orderId;
    return GestureDetector(
      onTap: orderId != null
          ? () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => OrderDetailPage(orderId: orderId),
                ),
              );
            }
          : null,
      child: Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Row(
        children: [
          _buildSellerAvatar(),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  widget.goodsTitle ?? '',
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppColors.fg,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  '卖家：${widget.sellerName ?? ""}',
                  style: const TextStyle(fontSize: 12, color: AppColors.muted),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, size: 16, color: AppColors.muted),
        ],
      ),
    ),
    );
  }

  Widget _buildTypeSelector() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('争议类型', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: AppColors.fg)),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _types.map((type) {
              final selected = type == _disputeType;
              return GestureDetector(
                onTap: () => setState(() => _disputeType = type),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  decoration: BoxDecoration(
                    color: selected ? AppColors.accent.withValues(alpha: 0.1) : AppColors.bg,
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: selected ? AppColors.accent : AppColors.border,
                    ),
                  ),
                  child: Text(
                    type,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w500,
                      color: selected ? AppColors.accent : AppColors.fg,
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildContentField() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('争议描述', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: AppColors.fg)),
          const SizedBox(height: 8),
          TextField(
            controller: _contentCtrl,
            maxLines: 5,
            maxLength: 500,
            decoration: const InputDecoration(
              hintText: '请详细描述争议原因和诉求...',
              border: OutlineInputBorder(),
              contentPadding: EdgeInsets.all(12),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      height: 46,
      child: ElevatedButton(
        onPressed: _submitting ? null : _submit,
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.price,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(23)),
          textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        ),
        child: _submitting
            ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
            : const Text('提交争议'),
      ),
    );
  }
}
