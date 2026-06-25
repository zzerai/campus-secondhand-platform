import 'dart:async';
import 'dart:html' as html;
import 'dart:ui_web' show platformViewRegistry;
import 'package:flutter/material.dart';

class CorsImage extends StatefulWidget {
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
  State<CorsImage> createState() => _CorsImageState();
}

class _CorsImageState extends State<CorsImage> {
  static int _counter = 0;
  late final String _viewType;
  html.ImageElement? _img;
  StreamSubscription<html.MouseEvent>? _clickSub;

  @override
  void initState() {
    super.initState();
    _viewType = 'cors_img_${_counter++}';
    platformViewRegistry.registerViewFactory(_viewType, (int viewId) {
      _img = _createImageElement();
      return _img!;
    });
  }

  @override
  void dispose() {
    _clickSub?.cancel();
    super.dispose();
  }

  html.ImageElement _createImageElement() {
    final img = html.ImageElement()
      ..src = widget.src
      ..style.width = '100%'
      ..style.height = '100%'
      ..style.objectFit = _cssFit(widget.fit)
      ..style.border = 'none';

    if (widget.borderRadius != null) {
      final r = widget.borderRadius!;
      final tl = r.topLeft.x;
      final tr = r.topRight.x;
      final br = r.bottomRight.x;
      final bl = r.bottomLeft.x;
      if (tl == tr && tr == br && br == bl) {
        img.style.borderRadius = '${tl}px';
      } else {
        img.style.borderRadius = '${tl}px ${tr}px ${br}px ${bl}px';
      }
    }

    _bindClick(img);
    return img;
  }

  void _bindClick(html.ImageElement img) {
    _clickSub?.cancel();
    if (widget.onTap != null) {
      _clickSub = img.onClick.listen((_) => widget.onTap!());
      img.style.cursor = 'pointer';
    }
  }

  @override
  void didUpdateWidget(CorsImage old) {
    super.didUpdateWidget(old);
    if (old.src != widget.src && _img != null) {
      _img!.src = widget.src;
    }
    if (old.borderRadius != widget.borderRadius && _img != null) {
      if (widget.borderRadius != null) {
        _img!.style.borderRadius =
            '${widget.borderRadius!.topLeft.x}px';
      } else {
        _img!.style.borderRadius = '0';
      }
    }
    if (old.onTap != widget.onTap && _img != null) {
      _bindClick(_img!);
    }
  }

  String _cssFit(BoxFit fit) {
    switch (fit) {
      case BoxFit.cover:
        return 'cover';
      case BoxFit.contain:
        return 'contain';
      case BoxFit.fill:
        return 'fill';
      case BoxFit.none:
        return 'none';
      case BoxFit.scaleDown:
        return 'scale-down';
      case BoxFit.fitWidth:
      case BoxFit.fitHeight:
        return 'cover';
    }
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: widget.width,
      height: widget.height,
      child: HtmlElementView(viewType: _viewType),
    );
  }
}
