import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

class ImagePlaceholder extends StatelessWidget {
  final double? width;
  final double? height;
  final double iconSize;

  const ImagePlaceholder({
    super.key,
    this.width,
    this.height,
    this.iconSize = 28,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width,
      height: height,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFFF0E8F5), Color(0xFFF0E2F5)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Icon(Icons.image_outlined,
          size: iconSize, color: AppColors.placeholder),
    );
  }
}
