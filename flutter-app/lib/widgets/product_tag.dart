import 'package:flutter/material.dart';
import '../data/models/trade_goods.dart';

class ProductTag extends StatelessWidget {
  final String label;
  final Color color;

  const ProductTag(this.label, this.color, {super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        label,
        style: TextStyle(
            fontSize: 11, fontWeight: FontWeight.w600, color: color),
      ),
    );
  }

  static ProductTag? derive(TradeGoods goods) {
    if (goods.quality == '全新') {
      return ProductTag('全新', const Color(0xFFE05A1A));
    }
    if (goods.originalPrice != null &&
        goods.originalPrice! > 0 &&
        goods.price / goods.originalPrice! < 0.6) {
      return ProductTag('超值', const Color(0xFFD45070));
    }
    if (goods.goodsStatus == '1' &&
        goods.viewCount != null &&
        goods.viewCount! > 100) {
      return ProductTag('热卖', const Color(0xFFE89000));
    }
    return null;
  }
}
