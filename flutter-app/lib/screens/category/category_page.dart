import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';

import '../../common/utils/image_utils.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/goods_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/goods_unavailable_overlay.dart';
import '../../widgets/image_placeholder.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/search_bar.dart';
import '../goods/product_detail_page.dart';
import '../goods/product_list_page.dart';
import 'category_data.dart';

class CategoryPage extends StatefulWidget {
  final String? selectedCatId;
  const CategoryPage({super.key, this.selectedCatId});

  @override
  State<CategoryPage> createState() => _CategoryPageState();
}

class _CategoryPageState extends State<CategoryPage> {
  final _goodsApi = GoodsApi.instance;
  final _sidebarScrollCtrl = ScrollController();
  final _contentScrollCtrl = ScrollController();

  late String _activeCat;

  List<TradeGoods> _goods = [];
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _activeCat = _resolveCat(widget.selectedCatId);
    _loadGoods();
  }

  @override
  void didUpdateWidget(CategoryPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.selectedCatId != null &&
        widget.selectedCatId != oldWidget.selectedCatId) {
      final newCat = _resolveCat(widget.selectedCatId);
      if (newCat != _activeCat) {
        setState(() => _activeCat = newCat);
        _loadGoods();
      }
    }
  }

  String _resolveCat(String? catId) {
    if (catId == null) return 'books';
    if (categoryData.any((c) => c.id == catId)) return catId;
    return 'books';
  }

  Future<void> _loadGoods() async {
    final cat = categoryData.firstWhere((c) => c.id == _activeCat,
        orElse: () => categoryData.first);
    setState(() => _loading = true);
    try {
      final result = await _goodsApi.getGoodsList(
        categoryId: cat.categoryId,
        pageNum: 1,
        pageSize: 10,
        sort: 'hot',
      );
      if (mounted) {
        setState(() {
          _goods = result.rows;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  void dispose() {
    _sidebarScrollCtrl.dispose();
    _contentScrollCtrl.dispose();
    super.dispose();
  }

  void _onCategoryTap(String catId) {
    if (catId == _activeCat) return;
    setState(() => _activeCat = catId);
    _loadGoods();
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Column(
        children: [
          _buildTopBar(),
          Expanded(child: _buildCategoryBody()),
        ],
      ),
    );
  }

  Widget _buildTopBar() {
    return Container(
      color: AppColors.surface,
      padding: AppSpacing.headerPad,
      child: Row(
        children: [
          const Text('分类',
              style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                  color: AppColors.fg,
                  letterSpacing: -0.2)),
          const SizedBox(width: 10),
          Expanded(
            child: AppSearchBar(
              hint: '搜一搜',
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => const ProductListPage(startInSearch: true),
                  ),
                );
              },
              backgroundColor: AppColors.divider,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryBody() {
    final cat = categoryData.firstWhere((c) => c.id == _activeCat,
        orElse: () => categoryData.first);

    return LayoutBuilder(
      builder: (context, constraints) {
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 90,
              height: constraints.maxHeight,
              child: _buildSidebar(),
            ),
            Expanded(child: _buildContent(cat)),
          ],
        );
      },
    );
  }

  Widget _buildSidebar() {
    return Container(
      color: AppColors.divider,
      child: ListView(
        controller: _sidebarScrollCtrl,
        children: categoryData.map((c) {
          final active = _activeCat == c.id;
          return GestureDetector(
            onTap: () => _onCategoryTap(c.id),
            child: Container(
              padding:
                  const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
              decoration: BoxDecoration(
                color: active ? AppColors.surface : Colors.transparent,
                border: active
                    ? const Border(
                        left: BorderSide(color: AppColors.accent, width: 3),
                      )
                    : null,
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Flexible(
                    child: Text(
                      c.name,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight:
                            active ? FontWeight.w600 : FontWeight.w500,
                        color: active ? AppColors.accent : AppColors.muted,
                        height: 1.3,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildContent(CategoryData cat) {
    return SingleChildScrollView(
      controller: _contentScrollCtrl,
      padding: const EdgeInsets.all(12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildSubcatHeader(cat.name),
          const SizedBox(height: 8),
          _buildHotSection(),
        ],
      ),
    );
  }

  Widget _buildSubcatHeader(String name) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(name,
          style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: AppColors.fg,
              letterSpacing: -0.2)),
    );
  }

  Widget _buildHotSection() {
    if (_loading) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 60),
          child: CircularProgressIndicator(color: AppColors.accent),
        ),
      );
    }
    if (_goods.isEmpty) {
      return const Padding(
        padding: EdgeInsets.only(top: 40),
        child: EmptyState(),
      );
    }
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 0.72,
      ),
      itemCount: _goods.length,
      itemBuilder: (context, index) {
        final goods = _goods[index];
        return _buildGoodsCard(goods);
      },
    );
  }

  Widget _buildGoodsCard(TradeGoods goods) {
    final imageUrl = firstImageUrl(goods.imageUrls);
    final unavailable = goods.unavailableForBuyer;
    final radius = BorderRadius.circular(AppColors.radiusSm);
    final card = Container(
      decoration: BoxDecoration(
        color: AppColors.bg,
        borderRadius: radius,
      ),
      child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            AspectRatio(
              aspectRatio: 1,
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(AppColors.radiusSm)),
                child: imageUrl != null
                    ? CorsImage(
                        src: imageUrl,
                        fit: BoxFit.cover,
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) =>
                                  ProductDetailPage(goodsId: goods.goodsId!),
                            ),
                          );
                        },
                      )
                    : const ImagePlaceholder(),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(10, 8, 10, 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(goods.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: AppColors.fg)),
                  const SizedBox(height: 2),
                  RichText(
                    text: TextSpan(
                      style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w700,
                          color: AppColors.price,
                          letterSpacing: -0.3),
                      children: [
                        const TextSpan(
                            text: '¥', style: TextStyle(fontSize: 11)),
                        TextSpan(text: goods.price.toStringAsFixed(0)),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
    );

    return GestureDetector(
      onTap: () {
        if (unavailable) {
          showToast(context, '商品${goods.unavailableLabel}');
          return;
        }
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ProductDetailPage(goodsId: goods.goodsId!),
          ),
        );
      },
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
