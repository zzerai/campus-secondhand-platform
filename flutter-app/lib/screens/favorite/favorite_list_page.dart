import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/favorite_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/image_placeholder.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/goods_unavailable_overlay.dart';
import '../../widgets/load_more_indicator.dart';
import '../goods/product_detail_page.dart';

class FavoriteListPage extends StatefulWidget {
  const FavoriteListPage({super.key});

  @override
  State<FavoriteListPage> createState() => _FavoriteListPageState();
}

class _FavoriteListPageState extends State<FavoriteListPage> {
  final _favoriteApi = FavoriteApi.instance;
  final ScrollController _scrollCtrl = ScrollController();

  List<TradeGoods> _goods = [];
  int _total = 0;
  bool _loading = false;
  bool _loadingMore = false;
  bool _batchRemoving = false;
  int _pageNum = 1;
  static const int _pageSize = 10;

  @override
  void initState() {
    super.initState();
    _scrollCtrl.addListener(_onScroll);
    _loadData();
  }

  @override
  void dispose() {
    _scrollCtrl.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollCtrl.position.pixels >=
            _scrollCtrl.position.maxScrollExtent - 100 &&
        !_loadingMore &&
        _goods.length < _total) {
      _loadMore();
    }
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final result =
          await _favoriteApi.getMyFavorites(pageNum: 1, pageSize: _pageSize);
      if (mounted) {
        setState(() {
          _goods = result.rows;
          _total = result.total;
          _pageNum = 1;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _loadMore() async {
    setState(() => _loadingMore = true);
    try {
      final result = await _favoriteApi.getMyFavorites(
          pageNum: _pageNum + 1, pageSize: _pageSize);
      if (mounted) {
        setState(() {
          _goods.addAll(result.rows);
          _total = result.total;
          _pageNum++;
          _loadingMore = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loadingMore = false);
    }
  }

  int get _unavailableCount =>
      _goods.where((g) => g.unavailableForBuyer).length;

  List<Widget> _buildAppBarActions() {
    if (_unavailableCount == 0) return [];
    return [
      _batchRemoving
          ? const Padding(
              padding: EdgeInsets.only(right: 16),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: AppColors.accent,
                ),
              ),
            )
          : TextButton(
              onPressed: _batchRemoveUnavailable,
              child: Text(
                '批量清理($_unavailableCount)',
                style: const TextStyle(fontSize: 13, color: AppColors.price),
              ),
            ),
    ];
  }

  Future<void> _batchRemoveUnavailable() async {
    final count = _unavailableCount;
    if (count == 0) return;

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('批量清理收藏'),
        content: Text('确定要取消收藏 $count 件已下架/已售出的商品吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('确定', style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;

    setState(() => _batchRemoving = true);
    int removed = 0;
    final ids = _goods
        .where((g) => g.unavailableForBuyer)
        .map((g) => g.goodsId!)
        .toList();

    for (final id in ids) {
      try {
        final err = await _favoriteApi.removeFavorite(id);
        if (err == null) removed++;
      } catch (_) {}
    }

    if (mounted) {
      setState(() => _batchRemoving = false);
      showToast(context, '已清理 $removed 件');
      _loadData();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('我的收藏'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
        actions: _buildAppBarActions(),
      ),
      body: _loading
          ? const Center(
              child: CircularProgressIndicator(color: AppColors.accent))
          : _goods.isEmpty
              ? const EmptyState()
              : RefreshIndicator(
                  color: AppColors.accent,
                  onRefresh: _loadData,
                  child: ListView.builder(
                    controller: _scrollCtrl,
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 40),
                    itemCount: _goods.length + (_loadingMore ? 1 : 0),
                    itemBuilder: (context, index) {
                      if (index == _goods.length) {
                        return const LoadMoreIndicator();
                      }
                      return _buildItem(index);
                    },
                  ),
                ),
    );
  }

  Widget _buildItem(int index) {
    final goods = _goods[index];
    final imageUrl = firstImageUrl(goods.imageUrls);
    final unavailable = goods.unavailableForBuyer;
    final radius = BorderRadius.circular(AppColors.radiusMd);
    return Dismissible(
      key: Key('fav_${goods.goodsId}'),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 24),
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: AppColors.price,
          borderRadius: BorderRadius.circular(AppColors.radiusMd),
        ),
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.delete_outline, color: Colors.white, size: 22),
            SizedBox(height: 2),
            Text('取消收藏',
                style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: Colors.white)),
          ],
        ),
      ),
      confirmDismiss: (direction) async {
        final goods = _goods[index];
        final err = await _favoriteApi.removeFavorite(goods.goodsId!);
        if (!mounted) return false;
        if (err == null) {
          setState(() {
            _goods.removeAt(index);
            _total--;
          });
          showToast(context, '已取消收藏');
          return true;
        } else {
          showToast(context, err);
          return false;
        }
      },
      child: GestureDetector(
        onTap: () async {
          if (unavailable) {
            showToast(context, '商品${goods.unavailableLabel}');
            return;
          }
          await Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => ProductDetailPage(goodsId: goods.goodsId!),
            ),
          );
          _loadData();
        },
        child: Container(
          margin: const EdgeInsets.only(bottom: 12),
          child: _wrapUnavailable(
            unavailable,
            goods.unavailableLabel,
            radius,
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: radius,
              ),
              child: Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(AppColors.radiusSm),
                child: SizedBox(
                  width: 80,
                  height: 80,
                  child: imageUrl != null
                      ? CorsImage(
                          src: imageUrl,
                          fit: BoxFit.cover,
                          onTap: () async {
                            await Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => ProductDetailPage(
                                    goodsId: goods.goodsId!),
                              ),
                            );
                            _loadData();
                          },
                        )
                      : const ImagePlaceholder(),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      goods.title,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                        color: AppColors.fg,
                        height: 1.3,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      goods.sellerNickname ?? '校园用户',
                      style: AppTextStyles.muted,
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Text(
                          '¥${goods.price.toStringAsFixed(0)}',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w700,
                            color: AppColors.price,
                          ),
                        ),
                        if (goods.originalPrice != null &&
                            goods.originalPrice! > 0) ...[
                          const SizedBox(width: 6),
                          Text(
                            '¥${goods.originalPrice!.toStringAsFixed(0)}',
                            style: const TextStyle(
                              fontSize: 12,
                              color: AppColors.muted,
                              decoration: TextDecoration.lineThrough,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
            ],
              ), // Row
            ), // 内层装饰 Container
          ), // _wrapUnavailable
        ), // 外层 margin Container
      ), // GestureDetector
    );
  }

  /// 不可用商品包一层遮罩，可用则原样返回。
  Widget _wrapUnavailable(
      bool unavailable, String label, BorderRadius radius, Widget child) {
    if (!unavailable) return child;
    return GoodsUnavailableOverlay(
      label: label,
      borderRadius: radius,
      child: child,
    );
  }
}
