import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/services/evaluation_api.dart';
import '../../data/models/evaluation_model.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../widgets/cors_image.dart';
import '../order/order_detail_page.dart';
import 'review_list_page.dart';

class ReviewPublishPage extends StatefulWidget {
  final int orderId;
  final String goodsTitle;
  final double goodsPrice;
  final String targetUserName;
  final String? goodsImages;

  /// 被评价方角色称谓：买家评卖家传 '卖家'，卖家评买家传 '买家'。
  final String targetRoleLabel;

  const ReviewPublishPage({
    super.key,
    required this.orderId,
    required this.goodsTitle,
    required this.goodsPrice,
    required this.targetUserName,
    this.goodsImages,
    this.targetRoleLabel = '卖家',
  });

  @override
  State<ReviewPublishPage> createState() => _ReviewPublishPageState();
}

class _ReviewPublishPageState extends State<ReviewPublishPage> {
  final _api = EvaluationApi.instance;
  final _textCtrl = TextEditingController();

  int _rating = 0;
  bool _submitting = false;
  bool _showSuccess = false;

  static const _hints = [
    '点击星星评分',
    '非常不满意',
    '不满意',
    '一般般',
    '满意，推荐！',
    '非常满意，强烈推荐！',
  ];

  @override
  void dispose() {
    _textCtrl.dispose();
    super.dispose();
  }

  bool get _canSubmit => _rating > 0 && _textCtrl.text.trim().isNotEmpty;

  Future<void> _submit() async {
    if (!_canSubmit) {
      if (_rating == 0) {
        showToast(context, '请先点亮星星评分');
      } else {
        showToast(context, '请输入评价内容');
      }
      return;
    }
    setState(() => _submitting = true);
    try {
      final ok = await _api.submit(SubmitEvaluationRequest(
        orderId: widget.orderId,
        score: _rating,
        content: _textCtrl.text.trim(),
      ));
      if (mounted) {
        setState(() => _submitting = false);
        if (ok) {
          setState(() => _showSuccess = true);
        } else {
          showToast(context, '提交失败，请重试');
        }
      }
    } catch (_) {
      if (mounted) {
        setState(() => _submitting = false);
        showToast(context, '提交失败，请检查网络');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('发布评价'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: Stack(
        children: [
          ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _buildOrderContext(),
              const SizedBox(height: 12),
              _buildRatingSection(),
              const SizedBox(height: 12),
              _buildCommentSection(),
              const SizedBox(height: 24),
              _buildSubmitBtn(),
              const SizedBox(height: 24),
            ],
          ),
          if (_showSuccess) _buildSuccessOverlay(),
        ],
      ),
    );
  }

  Widget _buildOrderContext() {
    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => OrderDetailPage(orderId: widget.orderId),
          ),
        );
      },
      child: Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      padding: const EdgeInsets.all(14),
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppColors.radiusSm),
            child: SizedBox(
              width: 64,
              height: 64,
              child: _buildGoodsImage(),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  widget.goodsTitle,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      fontSize: 15, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 4),
                Text(
                  '${widget.targetRoleLabel}: ${widget.targetUserName}',
                  style: AppTextStyles.muted,
                ),
                Text(
                  '¥${widget.goodsPrice.toStringAsFixed(0)}',
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w700,
                    color: AppColors.price,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    ),
    );
  }

  Widget _buildGoodsImage() {
    final imageUrl = firstImageUrl(widget.goodsImages ?? '');
    if (imageUrl == null) {
      return Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFFF0E2F5), Color(0xFFE8D5F0)],
          ),
        ),
        child: const Icon(Icons.image_outlined, size: 32, color: AppColors.muted),
      );
    }
    return CorsImage(src: imageUrl, fit: BoxFit.cover);
  }

  Widget _buildRatingSection() {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 16),
      child: Column(
        children: [
          const Text('交易体验评分',
              style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600)),
          const SizedBox(height: 14),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(5, (i) {
              final star = i + 1;
              final on = star <= _rating;
              return GestureDetector(
                onTap: () => setState(() => _rating = star),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: Icon(
                    on ? Icons.star : Icons.star_border,
                    size: 40,
                    color: on
                        ? const Color(0xFFFFB800)
                        : AppColors.border,
                  ),
                ),
              );
            }),
          ),
          const SizedBox(height: 10),
          Text(
            _hints[_rating],
            style: TextStyle(
              fontSize: 13,
              color: _rating >= 4 ? AppColors.accent : AppColors.muted,
              fontWeight: _rating >= 4 ? FontWeight.w600 : FontWeight.w400,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCommentSection() {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('评价内容',
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
          const SizedBox(height: 10),
          TextField(
            controller: _textCtrl,
            maxLength: 500,
            maxLines: 5,
            minLines: 4,
            onChanged: (_) => setState(() {}),
            decoration: const InputDecoration(
              hintText: '分享你的交易体验，帮助其他同学参考…',
              hintStyle: TextStyle(color: Color(0xFFC0B8CC), fontSize: 14),
              filled: true,
              fillColor: Color(0xFFFDF8FA),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.all(Radius.circular(8)),
                borderSide: BorderSide(color: AppColors.border),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.all(Radius.circular(8)),
                borderSide: BorderSide(color: AppColors.border),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.all(Radius.circular(8)),
                borderSide: BorderSide(color: AppColors.accent),
              ),
              contentPadding:
                  EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              counterStyle: TextStyle(color: AppColors.muted, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubmitBtn() {
    return SizedBox(
      width: double.infinity,
      height: 48,
      child: ElevatedButton(
        onPressed: _submitting ? null : _submit,
        style: ElevatedButton.styleFrom(
          backgroundColor: _canSubmit ? AppColors.accent : AppColors.border,
          foregroundColor: _canSubmit ? Colors.white : AppColors.muted,
          disabledBackgroundColor: AppColors.border,
          disabledForegroundColor: AppColors.muted,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppColors.radiusPill),
          ),
          elevation: _canSubmit ? 4 : 0,
          shadowColor:
              const Color(0xFFFF5722).withValues(alpha: 0.3),
          textStyle: const TextStyle(
              fontSize: 16, fontWeight: FontWeight.w600),
        ),
        child: _submitting
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                    strokeWidth: 2, color: AppColors.muted),
              )
            : const Text('提交评价'),
      ),
    );
  }

  Widget _buildSuccessOverlay() {
    return GestureDetector(
      onTap: () {},
      child: Container(
        color: const Color(0xBF1A0E28),
        alignment: Alignment.center,
        child: Container(
          margin: const EdgeInsets.symmetric(horizontal: 40),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(AppColors.radiusLg),
          ),
          padding: const EdgeInsets.fromLTRB(28, 32, 28, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.success,
                ),
                child: const Icon(Icons.check, size: 32, color: Colors.white),
              ),
              const SizedBox(height: 16),
              const Text('评价成功',
                  style: TextStyle(
                      fontSize: 20, fontWeight: FontWeight.w700)),
              const SizedBox(height: 6),
              const Text('你的评价已发布，感谢你的反馈',
                  style: AppTextStyles.muted),
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () {
                        Navigator.of(context)
                          ..pop()
                          ..pop(true);
                      },
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.muted,
                        side: const BorderSide(color: AppColors.border),
                        shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.circular(AppColors.radiusPill),
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                      child: const Text('返回'),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () {
                        Navigator.of(context)
                          ..pop(true)
                          ..push(MaterialPageRoute(
                            builder: (_) => const ReviewListPage(initialTab: 1),
                          ));
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.accent,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius:
                              BorderRadius.circular(AppColors.radiusPill),
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                      child: const Text('查看评价'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
