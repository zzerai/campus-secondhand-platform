import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/goods_api.dart';

import '../../widgets/product_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/load_more_indicator.dart';
import '../../widgets/search_bar.dart';
import '../../widgets/section_header.dart';
import '../goods/product_detail_page.dart';
import '../goods/product_list_page.dart';

class HomeTab extends StatefulWidget {
  final VoidCallback? onMessagesTap;
  final void Function(String? catId)? onCategoryNavigate;
  const HomeTab({super.key, this.onMessagesTap, this.onCategoryNavigate});

  @override
  State<HomeTab> createState() => HomeTabState();
}

class HomeTabState extends State<HomeTab> {
  final _goodsApi = GoodsApi.instance;
  final ScrollController _scrollCtrl = ScrollController();

  List<TradeGoods> _goods = [];
  int _total = 0;
  bool _loading = false;
  bool _loadingMore = false;
  int _pageNum = 1;
  static const int _pageSize = 10;

  void refresh() {
    _loadData();
  }

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
      final result = await _goodsApi.getGoodsList(
        pageNum: 1,
        pageSize: _pageSize,
        sort: 'hot',
      );
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
      final result = await _goodsApi.getGoodsList(
        pageNum: _pageNum + 1,
        pageSize: _pageSize,
        sort: 'hot',
      );
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

  void _onSearch() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => const ProductListPage(startInSearch: true),
      ),
    );
  }

  void _onMessages() {
    widget.onMessagesTap?.call();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          color: AppColors.surface,
          child: SafeArea(
            bottom: false,
            child: _buildHeader(),
          ),
        ),
        Expanded(
          child: SafeArea(
            top: false,
            child: RefreshIndicator(
              color: AppColors.accent,
              onRefresh: _loadData,
              child: CustomScrollView(
                controller: _scrollCtrl,
                physics: const AlwaysScrollableScrollPhysics(),
                slivers: [
                  SliverToBoxAdapter(child: _buildFeedSection()),
                  _buildProductList(),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildHeader() {
    return Container(
      color: AppColors.surface,
      padding: AppSpacing.headerPad,
      child: Row(
        children: [
          const Text('SecondU',
              style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: AppColors.accent,
                  letterSpacing: -0.4)),
          const SizedBox(width: 10),
          Expanded(
            child: AppSearchBar(
              hint: '搜索闲置好物',
              onTap: _onSearch,
            ),
          ),
          const SizedBox(width: 10),
          GestureDetector(
            onTap: _onMessages,
            child: SizedBox(
              width: 36,
              height: 36,
              child: Stack(
                children: [
                  const Center(
                    child: Icon(Icons.notifications_outlined,
                        size: 22, color: AppColors.muted),
                  ),
                  Positioned(
                    top: 6,
                    right: 6,
                    child: Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: AppColors.price,
                        shape: BoxShape.circle,
                        border: Border.all(
                            color: AppColors.surface, width: 2),
                      ),
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

  Widget _buildSectionHeader(String title, {String? trailing}) {
    return SectionHeader(
      title: title,
      trailing: trailing,
      onTrailingTap: trailing != null
          ? () => widget.onCategoryNavigate?.call(null)
          : null,
    );
  }

  Widget _buildFeedSection() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionHeader('推荐好物'),
        ],
      ),
    );
  }

  Widget _buildProductList() {
    if (_loading) {
      return const SliverFillRemaining(
        child: Center(child: CircularProgressIndicator(color: AppColors.accent)),
      );
    }
    if (_goods.isEmpty) {
      return const SliverFillRemaining(
        child: EmptyState(),
      );
    }
    return SliverList(
      delegate: SliverChildBuilderDelegate(
        (context, index) {
          if (index == _goods.length) {
            return const LoadMoreIndicator(
              padding: EdgeInsets.only(top: 16, bottom: 100),
            );
          }
          return _buildProductCard(_goods[index]);
        },
        childCount: _goods.length + (_loadingMore ? 1 : 0),
      ),
    );
  }

  Widget _buildProductCard(TradeGoods goods) {
    return ProductCard(
      goods: goods,
      onTap: () async {
        await Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ProductDetailPage(goodsId: goods.goodsId!),
          ),
        );
        refresh();
      },
    );
  }
}
