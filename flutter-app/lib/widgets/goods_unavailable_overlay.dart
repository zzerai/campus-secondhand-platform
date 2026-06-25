import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

/// 商品不可用（已下架 / 已售出）时盖在商品卡片上的半透明遮罩。
///
/// 通过 [IgnorePointer] 让遮罩本身不拦截手势——是否禁用详情跳转由各列表页在 onTap 处自行判断，
/// 这样既不影响收藏列表的左滑取消手势，也保持点击禁用逻辑集中在调用方。
/// [child] 不应自带 margin，且 [borderRadius] 需与卡片圆角一致，遮罩才能精确贴合卡片可视区域。
class GoodsUnavailableOverlay extends StatelessWidget {
  final Widget child;
  final String label;
  final BorderRadius borderRadius;

  const GoodsUnavailableOverlay({
    super.key,
    required this.child,
    required this.label,
    required this.borderRadius,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        child,
        Positioned.fill(
          child: IgnorePointer(
            child: ClipRRect(
              borderRadius: borderRadius,
              child: Container(
                color: Colors.white.withValues(alpha: 0.62),
                alignment: Alignment.center,
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.black.withValues(alpha: 0.55),
                    borderRadius:
                        BorderRadius.circular(AppColors.radiusPill),
                  ),
                  child: Text(
                    label,
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
