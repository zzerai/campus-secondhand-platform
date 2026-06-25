import 'package:flutter/material.dart';

class CorsImage extends StatelessWidget {
  final String src;
  final double? width;
  final double? height;
  final BoxFit fit;
  final Widget Function(BuildContext, Object, StackTrace?)? errorBuilder;
  final BorderRadius? borderRadius;
  final VoidCallback? onTap;

  const CorsImage({
    super.key,
    required this.src,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.errorBuilder,
    this.borderRadius,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final image = Image.network(
      src,
      width: width,
      height: height,
      fit: fit,
      errorBuilder:
          errorBuilder ?? (_, __, ___) => const SizedBox.shrink(),
    );

    if (onTap != null) {
      return GestureDetector(onTap: onTap, child: image);
    }
    return image;
  }
}
