import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';
import '../common/utils/image_utils.dart';
import '../data/models/trade_goods.dart';
import 'cors_image.dart';
import 'goods_unavailable_overlay.dart';
import 'image_placeholder.dart';
import 'product_tag.dart';

class ProductCard extends StatelessWidget {
  final TradeGoods goods;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry? margin;

  const ProductCard({
    super.key,
    required this.goods,
    this.onTap,
    this.margin,
  });

  @override
  Widget build(BuildContext context) {
    final imageUrl = firstImageUrl(goods.imageUrls);
    final tag = ProductTag.derive(goods);
    // 不可用商品（已下架/已售出）禁用跳转并盖遮罩
    final unavailable = goods.unavailableForBuyer;
    final effectiveTap = unavailable ? null : onTap;
    final radius = BorderRadius.circular(AppColors.radiusMd);

    final card = Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: radius,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppColors.radiusSm),
            child: SizedBox(
              width: 110,
              height: 110,
              child: imageUrl != null
                  ? CorsImage(
                      src: imageUrl,
                      fit: BoxFit.cover,
                      onTap: effectiveTap,
                      errorBuilder: (_, __, ___) => const ImagePlaceholder())
                  : const ImagePlaceholder(),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: GestureDetector(
              onTap: effectiveTap,
              behavior: HitTestBehavior.opaque,
              child: SizedBox(
                height: 110,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(goods.title,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w600,
                            height: 1.4)),
                    const SizedBox(height: 4),
                    Text(goods.description,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                            fontSize: 12, color: AppColors.muted)),
                    if (tag != null) ...[
                      const SizedBox(height: 6),
                      tag!,
                    ],
                    const Spacer(),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        RichText(
                          text: TextSpan(
                            style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w700,
                                color: AppColors.price,
                                letterSpacing: -0.36),
                            children: [
                              const TextSpan(
                                  text: '¥',
                                  style: TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w600)),
                              TextSpan(
                                  text: goods.price.toStringAsFixed(0)),
                            ],
                          ),
                        ),
                        Row(children: [
                          if (goods.sellerNickname != null) ...[
                            const Icon(Icons.check_circle,
                                size: 13, color: AppColors.accent),
                            const SizedBox(width: 2),
                            Text(goods.sellerNickname!,
                                style: const TextStyle(
                                    fontSize: 11,
                                    color: AppColors.accent,
                                    fontWeight: FontWeight.w600)),
                            const SizedBox(width: 6),
                          ],
                          Text(goods.tradePlace,
                              style: const TextStyle(
                                  fontSize: 11, color: AppColors.muted)),
                        ]),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
          ],
        ),
    );

    return Container(
      margin: margin ?? const EdgeInsets.symmetric(horizontal: 16, vertical: 5),
      child: unavailable
          ? GoodsUnavailableOverlay(
              label: goods.unavailableLabel,
              borderRadius: radius,
              child: card,
            )
          : card,
    );
  }
}
