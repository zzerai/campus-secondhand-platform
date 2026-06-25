import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/favorite_api.dart';
import '../../data/services/goods_api.dart';
import '../../widgets/cors_image.dart';
import '../../common/utils/toast_utils.dart';
import '../order/create_order_page.dart';
import '../order/order_detail_page.dart';
import '../order/payment_page.dart';
import '../../data/services/order_api.dart';
import '../../common/utils/image_utils.dart';
import '../../common/utils/user_nav.dart';
import '../../data/services/session.dart';
import '../messages/chat_page.dart';
import 'goods_publish_page.dart';
import 'report_page.dart';

class ProductDetailPage extends StatefulWidget {
  final int goodsId;

  const ProductDetailPage({super.key, required this.goodsId});

  @override
  State<ProductDetailPage> createState() => _ProductDetailPageState();
}

class _ProductDetailPageState extends State<ProductDetailPage> {
  final _goodsApi = GoodsApi.instance;
  final _favoriteApi = FavoriteApi.instance;
  final PageController _imageCtrl = PageController();
  int _imageIndex = 0;
  bool _isFaved = false;
  bool _favLoading = false;

  TradeGoods? _goods;
  bool _loading = true;

  bool get isOwner => _goods != null &&
      _goods!.sellerId == Session.instance.currentUser?.userId;
  String? _error;
  bool _notFound = false;

  List<String> get _imageUrls {
    if (_goods == null || _goods!.imageUrls.isEmpty) return [];
    return _goods!.imageUrls
        .split(',')
        .map((s) => ensureAbsoluteUrl(s.trim()))
        .where((s) => s.isNotEmpty)
        .toList();
  }

  @override
  void initState() {
    super.initState();
    _loadDetail();
  }

  Future<void> _loadDetail() async {
    try {
      final goods = await _goodsApi.getGoodsDetail(widget.goodsId);
      _favoriteApi.checkFavorite(widget.goodsId).then((isFav) {
        if (mounted) setState(() => _isFaved = isFav);
      });
      if (mounted) {
        setState(() {
          _goods = goods;
          _loading = false;
        });
      }
    } on GoodsNotFoundException catch (e) {
      if (mounted) {
        setState(() {
          _error = e.message;
          _loading = false;
          _notFound = true;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = '加载失败，请重试';
          _loading = false;
        });
      }
    }
  }

  @override
  void dispose() {
    _imageCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return Scaffold(
        backgroundColor: AppColors.bg,
        body: Stack(
          children: [
            const Center(
              child: CircularProgressIndicator(color: AppColors.accent),
            ),
            _buildBackButton(),
          ],
        ),
      );
    }
    if (_error != null) {
      return Scaffold(
        backgroundColor: AppColors.bg,
        body: Stack(
          children: [
            Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    _notFound ? Icons.remove_shopping_cart_outlined : Icons.error_outline,
                    size: 48,
                    color: AppColors.muted,
                  ),
                  const SizedBox(height: 12),
                  Text(_error!, style: const TextStyle(color: AppColors.muted)),
                  if (!_notFound) ...[
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () {
                        setState(() {
                          _loading = true;
                          _error = null;
                          _notFound = false;
                        });
                        _loadDetail();
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.accent,
                        foregroundColor: Colors.white,
                      ),
                      child: const Text('重试'),
                    ),
                  ],
                ],
              ),
            ),
            _buildBackButton(),
          ],
        ),
      );
    }
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: Stack(
        children: [
          ListView(
            children: [
              _buildGallery(),
              _buildInfoCard(),
              _buildSellerCard(),
              _buildDescCard(),
              _buildParamsCard(),
              _buildReportCard(),
            ],
          ),
          _buildBackButton(),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: _buildBottomBar(),
          ),
        ],
      ),
    );
  }

  Widget _buildBackButton() {
    return Positioned(
      top: 0,
      left: 0,
      right: 0,
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Row(
            children: [
              _buildNavButton(
                Icons.arrow_back_rounded,
                () => Navigator.of(context).pop(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ── Image Gallery ──

  Widget _buildGallery() {
    final images = _imageUrls;
    return SizedBox(
      width: double.infinity,
      child: AspectRatio(
        aspectRatio: 1,
        child: Stack(
          children: [
            if (images.isEmpty)
              _buildPlaceholder()
            else
              PageView.builder(
                controller: _imageCtrl,
                onPageChanged: (i) => setState(() => _imageIndex = i),
                itemCount: images.length,
                itemBuilder: (context, index) {
                  return CorsImage(
                    src: images[index],
                    fit: BoxFit.cover,
                    onTap: () => _openFullscreen(index),
                    errorBuilder: (_, __, ___) => _buildPlaceholder(),
                  );
                },
              ),
            if (images.length > 1)
              Positioned(
                bottom: 12,
                left: 0,
                right: 0,
                child: _buildDots(images.length),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlaceholder() {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFFF0E8F5), Color(0xFFF0E2F5)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: const Center(
        child: Icon(Icons.image_outlined, size: 64, color: AppColors.placeholder),
      ),
    );
  }

  Widget _buildDots(int count) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(count, (i) {
        final active = i == _imageIndex;
        return AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          margin: const EdgeInsets.symmetric(horizontal: 3),
          width: active ? 18 : 6,
          height: 6,
          decoration: BoxDecoration(
            color: active ? Colors.white : Colors.white.withValues(alpha: 0.5),
            borderRadius: BorderRadius.circular(3),
          ),
        );
      }),
    );
  }

  Widget _buildNavButton(IconData icon, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          color: Colors.black.withValues(alpha: 0.35),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, size: 20, color: Colors.white),
      ),
    );
  }

  void _openFullscreen(int initialIndex) {
    final images = _imageUrls;
    if (images.isEmpty) return;
    Navigator.of(context).push(
      PageRouteBuilder(
        opaque: false,
        barrierColor: Colors.black,
        pageBuilder: (_, __, ___) => _FullscreenGallery(
          images: images,
          initialIndex: initialIndex,
        ),
      ),
    );
  }

  // ── Info Card ──

  Widget _buildInfoCard() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildPriceRow(),
          const SizedBox(height: 8),
          Text(
            _goods!.title,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              height: 1.4,
              letterSpacing: -0.18,
              color: AppColors.fg,
            ),
          ),
          const SizedBox(height: 10),
          _buildTags(),
          const SizedBox(height: 10),
          _buildMetaRow(),
        ],
      ),
    );
  }

  Widget _buildPriceRow() {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.baseline,
      textBaseline: TextBaseline.alphabetic,
      children: [
        RichText(
          text: TextSpan(
            children: [
              const TextSpan(
                text: '¥',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: AppColors.price,
                ),
              ),
              TextSpan(
                text: '${_goods!.price.toInt()}',
                style: const TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                  color: AppColors.price,
                  letterSpacing: -0.56,
                ),
              ),
            ],
          ),
        ),
        if (_goods!.originalPrice != null && _goods!.originalPrice! > 0) ...[
          const SizedBox(width: 6),
          Text(
            '¥${_goods!.originalPrice!.toInt()}',
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.muted,
              decoration: TextDecoration.lineThrough,
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildTags() {
    return Wrap(
      spacing: 8,
      runSpacing: 6,
      children: [
        if (_goods!.quality.isNotEmpty)
          _buildTag(_goods!.quality, const Color(0xFFE05A1A), const Color(0xFFF0E2F0)),
        if (_goods!.tradePlace.isNotEmpty)
          _buildTag('校内面交', const Color(0xFF00A878), const Color(0xFFD4F5E8)),
        _buildTag('已认证', const Color(0xFFE89000), const Color(0xFFFFE8E0)),
      ],
    );
  }

  Widget _buildTag(String label, Color fg, Color bg) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        label,
        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: fg),
      ),
    );
  }

  Widget _buildMetaRow() {
    return Row(
      children: [
        _buildMetaItem(Icons.location_on_outlined,
            _goods!.tradePlace.isNotEmpty ? _goods!.tradePlace : '校内'),
        const SizedBox(width: 24),
        _buildMetaItem(Icons.visibility_outlined,
            '${_goods!.viewCount ?? 0} 浏览'),
      ],
    );
  }

  Widget _buildMetaItem(IconData icon, String text) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: AppColors.muted),
        const SizedBox(width: 4),
        Text(text, style: AppTextStyles.muted),
      ],
    );
  }

  // ── Seller Card ──

  Widget _buildSellerCard() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      margin: const EdgeInsets.only(bottom: 8),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => openUserProfile(context, _goods!.sellerId),
            child: _buildSellerAvatar(),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: GestureDetector(
              onTap: () => openUserProfile(context, _goods!.sellerId),
              behavior: HitTestBehavior.opaque,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Row(
                    children: [
                      Text(
                        _goods!.sellerNickname ?? '校园用户',
                        style: AppTextStyles.cardTitle,
                      ),
                      const SizedBox(width: 6),
                      const Icon(Icons.star, size: 12, color: AppColors.accent),
                      const Text(
                        ' 已认证',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                          color: AppColors.accent,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          if (!isOwner)
            GestureDetector(
              onTap: () async {
                final goods = _goods!;
                await Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => ChatPage(
                      peerId: goods.sellerId ?? 0,
                      peerName: goods.sellerNickname ?? '校园用户',
                      goodsId: goods.goodsId ?? 0,
                      peerItem: goods.title,
                      peerAvatar: goods.sellerAvatar,
                      goodsCoverImage: firstImageUrl(goods.imageUrls),
                      goodsStatus: goods.goodsStatus,
                    ),
                  ),
                );
                _loadDetail();
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: AppColors.accent,
                  borderRadius: BorderRadius.circular(AppColors.radiusPill),
                ),
                child: const Text(
                  '私聊',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSellerAvatar() {
    final avatar = _goods?.sellerAvatar;
    final hasAvatar = avatar != null && avatar.isNotEmpty;
    return Container(
      width: 44,
      height: 44,
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
              errorBuilder: (_, __, ___) => const Icon(Icons.person,
                  size: 22, color: Colors.white),
            )
          : const Icon(Icons.person, size: 22, color: Colors.white),
    );
  }

  // ── Description Card ──

  Widget _buildDescCard() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('商品描述'),
          const SizedBox(height: 8),
          Text(
            _goods!.description.isNotEmpty ? _goods!.description : '暂无描述',
            style: const TextStyle(
              fontSize: 14,
              height: 1.7,
              color: AppColors.fg,
            ),
          ),
        ],
      ),
    );
  }

  // ── Params Card ──

  Widget _buildParamsCard() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('商品参数'),
          const SizedBox(height: 12),
          _buildParamsGrid(),
        ],
      ),
    );
  }

  Widget _buildParamsGrid() {
    final params = <_ParamItem>[
      _ParamItem('成色', _goods!.quality.isNotEmpty ? _goods!.quality : '未填写'),
      _ParamItem('交易方式',
          _goods!.tradePlace.isNotEmpty ? _goods!.tradePlace : '未填写'),
      _ParamItem('联系方式',
          _goods!.contactWay.isNotEmpty ? _goods!.contactWay : '未填写'),
      if (_goods!.categoryName != null)
        _ParamItem('分类', _goods!.categoryName!),
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 4,
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
      ),
      itemCount: params.length,
      itemBuilder: (context, index) {
        final p = params[index];
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              p.label,
              style: const TextStyle(fontSize: 12, color: AppColors.muted),
            ),
            const SizedBox(height: 2),
            Text(
              p.value,
              style: AppTextStyles.inputLabel,
            ),
          ],
        );
      },
    );
  }

  // ── Report Card ──

  Widget _buildReportCard() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      margin: const EdgeInsets.only(bottom: 80),
      child: GestureDetector(
        onTap: () async {
          await Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => ReportPage(
                goodsId: _goods!.goodsId!,
                goodsTitle: _goods!.title,
                sellerName: _goods!.sellerNickname ?? '校园用户',
                sellerAvatar: _goods!.sellerAvatar,
              ),
            ),
          );
          _loadDetail();
        },
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.flag_outlined, size: 14, color: AppColors.muted),
            const SizedBox(width: 6),
            const Text(
              '举报此商品',
              style: TextStyle(fontSize: 13, color: AppColors.muted),
            ),
            const SizedBox(width: 4),
            const Icon(Icons.chevron_right, size: 14, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

  // ── Section Title ──

  Widget _buildSectionTitle(String title) {
    return Row(
      children: [
        Container(
          width: 3,
          height: 14,
          decoration: BoxDecoration(
            color: AppColors.accent,
            borderRadius: BorderRadius.circular(2),
          ),
        ),
        const SizedBox(width: 6),
        Text(
          title,
          style: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            letterSpacing: -0.15,
            color: AppColors.fg,
          ),
        ),
      ],
    );
  }

  // ── Bottom Bar ──

  Widget _buildBottomBar() {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: SafeArea(
        top: false,
        child: Row(
          children: [
            _buildActionIcon(
              icon: _favLoading
                  ? Icons.favorite_outline
                  : (_isFaved ? Icons.favorite : Icons.favorite_outline),
              color: _favLoading
                  ? AppColors.muted
                  : (_isFaved ? AppColors.price : AppColors.muted),
              label: _favLoading ? '...' : (_isFaved ? '已收藏' : '收藏'),
              onTap: () async {
                if (_favLoading) return;
                setState(() => _favLoading = true);
                final err = _isFaved
                    ? await _favoriteApi.removeFavorite(widget.goodsId)
                    : await _favoriteApi.addFavorite(widget.goodsId);
                if (!mounted) return;
                setState(() {
                  if (err == null) _isFaved = !_isFaved;
                  _favLoading = false;
                });
                if (err != null) {
                  showToast(context, err);
                } else {
                  showToast(context, _isFaved ? '已收藏' : '已取消收藏');
                }
              },
            ),
            const SizedBox(width: 10),
            if (isOwner) ...[
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: OutlinedButton(
                    onPressed: () async {
                      if (_goods == null) return;
                      await Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => GoodsPublishPage(editGoods: _goods),
                        ),
                      );
                      _loadDetail();
                    },
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.accent,
                      side: const BorderSide(color: AppColors.accent, width: 1.5),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppColors.radiusPill),
                      ),
                      textStyle: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    child: const Text('编辑商品'),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: ElevatedButton(
                    onPressed: () async {
                      final ok = await _goodsApi.offlineGoods(widget.goodsId);
                      if (!mounted) return;
                      showToast(context, ok ? '已下架' : '操作失败');
                      if (ok) _loadDetail();
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFE02424),
                      foregroundColor: Colors.white,
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppColors.radiusPill),
                      ),
                      textStyle: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    child: const Text('下架'),
                  ),
                ),
              ),
            ] else ...[
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: OutlinedButton(
                    onPressed: () async {
                      final goods = _goods!;
                      await Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => ChatPage(
                            peerId: goods.sellerId ?? 0,
                            peerName: goods.sellerNickname ?? '校园用户',
                            goodsId: goods.goodsId ?? 0,
                            peerItem: goods.title,
                            peerAvatar: goods.sellerAvatar,
                            goodsCoverImage: firstImageUrl(goods.imageUrls),
                            goodsStatus: goods.goodsStatus,
                          ),
                        ),
                      );
                      _loadDetail();
                    },
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.accent,
                      side: const BorderSide(color: AppColors.accent, width: 1.5),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppColors.radiusPill),
                      ),
                      textStyle: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    child: const Text('聊一聊'),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: SizedBox(
                  height: 44,
                  child: ElevatedButton(
                    onPressed: () async {
                      final goods = _goods!;
                      final orderId = await showCreateOrderSheet(
                        context,
                        goodsId: goods.goodsId ?? 0,
                        goodsTitle: goods.title,
                        goodsPrice: goods.price,
                        tradePlace: goods.tradePlace,
                      );
                      if (orderId == null || !mounted) return;

                      if (!mounted) return;
                      final choice = await showDialog<String>(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Center(child: Text('已下单')),
                          content: const Text('订单已创建，请选择支付方式', textAlign: TextAlign.center),
                          actions: [
                            Row(
                              children: [
                                Expanded(
                                  child: TextButton(
                                    onPressed: () => Navigator.pop(ctx, 'later'),
                                    style: TextButton.styleFrom(
                                      foregroundColor: AppColors.muted,
                                    ),
                                    child: const Text('稍后支付'),
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: ElevatedButton(
                                    onPressed: () => Navigator.pop(ctx, 'pay'),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: AppColors.accent,
                                      foregroundColor: Colors.white,
                                    ),
                                    child: const Text('立即支付'),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                      if (choice == null || !mounted) return;

                      if (choice == 'later') {
                        showToast(context, '已下单，请尽快支付');
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) =>
                                OrderDetailPage(orderId: orderId),
                          ),
                        );
                        return;
                      }

                      showToast(context, '正在获取支付信息...');
                      final html = await OrderApi.instance.payOrder(orderId);
                      if (html == null || !mounted) {
                        showToast(context, '发起支付失败');
                        return;
                      }
                      final paid = await Navigator.of(context).push<bool>(
                        MaterialPageRoute(
                          builder: (_) =>
                              PaymentPage(orderId: orderId, payHtml: html),
                        ),
                      );
                      if (paid == true && mounted) {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) =>
                                OrderDetailPage(orderId: orderId),
                          ),
                        );
                      }
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.accent,
                      foregroundColor: Colors.white,
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppColors.radiusPill),
                      ),
                      textStyle: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    child: const Text('我要买'),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildActionIcon({
    required IconData icon,
    required Color color,
    required String label,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: const Color(0xFFF2EDF7),
          shape: BoxShape.circle,
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 20, color: color),
          ],
        ),
      ),
    );
  }
}

class _ParamItem {
  final String label;
  final String value;
  _ParamItem(this.label, this.value);
}

class _FullscreenGallery extends StatefulWidget {
  final List<String> images;
  final int initialIndex;

  const _FullscreenGallery({
    required this.images,
    required this.initialIndex,
  });

  @override
  State<_FullscreenGallery> createState() => _FullscreenGalleryState();
}

class _FullscreenGalleryState extends State<_FullscreenGallery> {
  late final PageController _controller;
  late int _index;

  @override
  void initState() {
    super.initState();
    _index = widget.initialIndex;
    _controller = PageController(initialPage: _index);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          GestureDetector(
            onTap: () => Navigator.of(context).pop(),
            child: PageView.builder(
              controller: _controller,
              onPageChanged: (i) => setState(() => _index = i),
              itemCount: widget.images.length,
              itemBuilder: (_, i) {
                return InteractiveViewer(
                  child: Center(
                    child: CorsImage(
                      src: widget.images[i],
                      fit: BoxFit.contain,
                    ),
                  ),
                );
              },
            ),
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  GestureDetector(
                    onTap: () => Navigator.of(context).pop(),
                    child: Container(
                      width: 36,
                      height: 36,
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.2),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.close, size: 20, color: Colors.white),
                    ),
                  ),
                  if (widget.images.length > 1)
                    Text(
                      '${_index + 1} / ${widget.images.length}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}


