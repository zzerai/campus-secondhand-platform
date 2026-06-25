import 'package:flutter/material.dart';

import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/goods_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/product_card.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/load_more_indicator.dart';
import '../../widgets/search_bar.dart';
import 'product_detail_page.dart';

class ProductListPage extends StatefulWidget {
  final String? keyword;
  final int? categoryId;
  final bool startInSearch;

  const ProductListPage({
    super.key,
    this.keyword,
    this.categoryId,
    this.startInSearch = false,
  });

  @override
  State<ProductListPage> createState() => _ProductListPageState();
}

class _ProductListPageState extends State<ProductListPage> {
  final _goodsApi = GoodsApi.instance;
  final ScrollController _scrollCtrl = ScrollController();
  final TextEditingController _searchCtrl = TextEditingController();
  final _searchFocus = FocusNode();

  List<TradeGoods> _goods = [];
  int _total = 0;
  bool _loading = false;
  bool _loadingMore = false;
  int _pageNum = 1;
  static const int _pageSize = 10;

  String _sortKey = 'default'; // default | new | priceAsc | priceDesc
  bool _showSearch = false;
  bool _fromExternal = false; // true when opened via startInSearch from home/category

  final _minPriceCtrl = TextEditingController();
  final _maxPriceCtrl = TextEditingController();

  static final List<String> _searchHistory = [];

  @override
  void initState() {
    super.initState();
    if (widget.keyword != null) {
      _searchCtrl.text = widget.keyword!;
    }
    _showSearch = widget.startInSearch;
    _fromExternal = widget.startInSearch;
    _scrollCtrl.addListener(_onScroll);
    _loadData();
  }

  @override
  void dispose() {
    _searchFocus.dispose();
    _minPriceCtrl.dispose();
    _maxPriceCtrl.dispose();
    _scrollCtrl.dispose();
    _searchCtrl.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollCtrl.position.pixels >= _scrollCtrl.position.maxScrollExtent - 100 &&
        !_loadingMore &&
        _goods.length < _total) {
      _loadMore();
    }
  }

  String? _sortParam() {
    switch (_sortKey) {
      case 'new':
        return null; // default: order by goods_id desc = newest
      case 'priceAsc':
        return 'priceAsc';
      case 'priceDesc':
        return 'priceDesc';
      case 'hot':
        return 'hot';
      default:
        return null;
    }
  }

  num? _parsePrice(String text) {
    final v = num.tryParse(text.trim());
    return (v != null && v >= 0) ? v : null;
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final result = await _goodsApi.getGoodsList(
        keyword: widget.keyword ??
            (_searchCtrl.text.isNotEmpty ? _searchCtrl.text : null),
        categoryId: widget.categoryId,
        pageNum: 1,
        pageSize: _pageSize,
        sort: _sortParam(),
        minPrice: _parsePrice(_minPriceCtrl.text),
        maxPrice: _parsePrice(_maxPriceCtrl.text),
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
      if (mounted) {
        setState(() => _loading = false);
        showToast(context,'加载失败');
      }
    }
  }

  Future<void> _loadMore() async {
    setState(() => _loadingMore = true);
    try {
      final result = await _goodsApi.getGoodsList(
        keyword: widget.keyword ??
            (_searchCtrl.text.isNotEmpty ? _searchCtrl.text : null),
        categoryId: widget.categoryId,
        pageNum: _pageNum + 1,
        pageSize: _pageSize,
        sort: _sortParam(),
        minPrice: _parsePrice(_minPriceCtrl.text),
        maxPrice: _parsePrice(_maxPriceCtrl.text),
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

  void _onSortChanged(String key) {
    setState(() => _sortKey = key);
    _loadData();
  }

  void _onPriceSortTap() {
    setState(() {
      switch (_sortKey) {
        case 'priceDesc':
          _sortKey = 'priceAsc';
          break;
        case 'priceAsc':
          _sortKey = 'default';
          break;
        default:
          _sortKey = 'priceDesc';
      }
    });
    _loadData();
  }

  void _showFilterSheet() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (_) => _buildFilterSheet(),
    );
  }

  Widget _buildFilterSheet() {
    return StatefulBuilder(
      builder: (context, setSheetState) {
        return Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text('价格区间',
                      style: TextStyle(
                          fontSize: 16, fontWeight: FontWeight.w600)),
                  const Spacer(),
                  TextButton(
                    onPressed: () {
                      _minPriceCtrl.clear();
                      _maxPriceCtrl.clear();
                      setSheetState(() {});
                      setState(() {});
                      _loadData();
                      Navigator.of(context).pop();
                    },
                    child: const Text('清除'),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _minPriceCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        hintText: '最低价',
                        isDense: true,
                        contentPadding: EdgeInsets.symmetric(
                            horizontal: 12, vertical: 10),
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                  const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 12),
                    child: Text('—', style: TextStyle(color: AppColors.muted)),
                  ),
                  Expanded(
                    child: TextField(
                      controller: _maxPriceCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        hintText: '最高价',
                        isDense: true,
                        contentPadding: EdgeInsets.symmetric(
                            horizontal: 12, vertical: 10),
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.accent,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8)),
                  ),
                  onPressed: () {
                    setState(() {});
                    _loadData();
                    Navigator.of(context).pop();
                  },
                  child: const Text('确定'),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  void _onSearch() {
    FocusScope.of(context).unfocus();
    _fromExternal = false;
    final keyword = _searchCtrl.text.trim();
    if (keyword.isNotEmpty) {
      _searchHistory.remove(keyword);
      _searchHistory.insert(0, keyword);
      if (_searchHistory.length > 10) {
        _searchHistory.removeLast();
      }
      setState(() => _showSearch = false);
      _loadData();
    } else {
      setState(() => _showSearch = false);
    }
  }

  void _onHistoryTap(String keyword) {
    _searchCtrl.text = keyword;
    _onSearch();
  }

  void _clearHistory() {
    setState(() => _searchHistory.clear());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Container(
            color: AppColors.surface,
            child: SafeArea(
              bottom: false,
              child: _buildHeader(),
            ),
          ),
          if (!_showSearch) ...[
            _buildSortBar(),
            _buildResultCount(),
            Expanded(child: SafeArea(top: false, child: _buildProductList())),
          ],
        ],
      ),
    );
  }

  Widget _buildHeader() {
    if (_showSearch) {
      return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            color: AppColors.surface,
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            child: Row(
              children: [
                IconButton(
                  onPressed: () {
                    if (_fromExternal) {
                      Navigator.of(context).pop();
                    } else {
                      setState(() => _showSearch = false);
                    }
                  },
                  icon: const Icon(Icons.arrow_back, size: 22),
                  padding: EdgeInsets.zero,
                  color: AppColors.fg,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextField(
                    controller: _searchCtrl,
                    focusNode: _searchFocus,
                    autofocus: true,
                    textInputAction: TextInputAction.search,
                    style: const TextStyle(fontSize: 15, color: AppColors.fg),
                    decoration: const InputDecoration(
                      hintText: '搜索商品',
                      isDense: true,
                      contentPadding:
                          EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                    ),
                    onSubmitted: (_) => _onSearch(),
                  ),
                ),
                const SizedBox(width: 8),
                ListenableBuilder(
                  listenable: _searchCtrl,
                  builder: (context, _) {
                    final hasText = _searchCtrl.text.trim().isNotEmpty;
                    return TextButton(
                      onPressed: _onSearch,
                      child: Text(
                        '搜索',
                        style: TextStyle(
                          color: hasText ? AppColors.accent : AppColors.muted,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
          if (_searchHistory.isNotEmpty) _buildSearchHistory(),
        ],
      );
    }

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: [
          IconButton(
            onPressed: () => Navigator.of(context).pop(),
            icon: const Icon(Icons.arrow_back_rounded, size: 22),
            padding: EdgeInsets.zero,
            color: AppColors.fg,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: AppSearchBar(
              hint: _searchCtrl.text.isNotEmpty ? _searchCtrl.text : '搜索商品',
              height: 38,
              onTap: () => setState(() => _showSearch = true),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchHistory() {
    return Container(
      color: AppColors.surface,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Divider(height: 1),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                const Text('搜索历史',
                    style: TextStyle(fontSize: 13, color: AppColors.muted)),
                const Spacer(),
                GestureDetector(
                  onTap: _clearHistory,
                  child: const Icon(Icons.delete_outline,
                      size: 16, color: AppColors.muted),
                ),
              ],
            ),
          ),
          ..._searchHistory.map((keyword) => ListTile(
                dense: true,
                leading:
                    const Icon(Icons.history, size: 18, color: AppColors.muted),
                title: Text(keyword,
                    style: const TextStyle(fontSize: 14, color: AppColors.fg)),
                onTap: () => _onHistoryTap(keyword),
              )),
        ],
      ),
    );
  }

  Widget _buildSortBar() {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 2),
      decoration: const BoxDecoration(
        color: AppColors.bg,
        border: Border(
          bottom: BorderSide(color: AppColors.border),
        ),
      ),
      child: Row(
        children: [
          _buildSortItem('综合', 'default'),
          _buildSortItem('最新', 'new'),
          _buildSortItem('热度', 'hot'),
          _buildPriceSortItem(),
          Expanded(
            child: GestureDetector(
              onTap: _showFilterSheet,
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.tune,
                        size: 16,
                        color: (_minPriceCtrl.text.isNotEmpty ||
                                _maxPriceCtrl.text.isNotEmpty)
                            ? AppColors.accent
                            : AppColors.muted),
                    const SizedBox(width: 4),
                    Text(
                      '筛选',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: (_minPriceCtrl.text.isNotEmpty ||
                                _maxPriceCtrl.text.isNotEmpty)
                            ? AppColors.accent
                            : AppColors.muted,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSortItem(String label, String key) {
    final active = _sortKey == key;
    return Expanded(
      child: GestureDetector(
        onTap: () => _onSortChanged(key),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 13,
              fontWeight: active ? FontWeight.w600 : FontWeight.w500,
              color: active ? AppColors.accent : AppColors.muted,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPriceSortItem() {
    final active = _sortKey == 'priceDesc' || _sortKey == 'priceAsc';
    final showUp = _sortKey == 'priceAsc';
    final showDown = _sortKey == 'priceDesc';
    return Expanded(
      child: GestureDetector(
        onTap: _onPriceSortTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          alignment: Alignment.center,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '价格',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: active ? FontWeight.w600 : FontWeight.w500,
                  color: active ? AppColors.accent : AppColors.muted,
                ),
              ),
              const SizedBox(width: 3),
              Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Transform.translate(
                    offset: const Offset(0, 3),
                    child: Transform.rotate(
                      angle: -1.5708,
                      child: Icon(Icons.play_arrow_rounded,
                          size: 12,
                          color: showUp
                              ? AppColors.accent
                              : AppColors.muted.withValues(alpha: 0.5)),
                    ),
                  ),
                  Transform.translate(
                    offset: const Offset(0, -3),
                    child: Transform.rotate(
                      angle: 1.5708,
                      child: Icon(Icons.play_arrow_rounded,
                          size: 12,
                          color: showDown
                              ? AppColors.accent
                              : AppColors.muted.withValues(alpha: 0.5)),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildResultCount() {
    return Padding(
      padding: const EdgeInsets.only(left: 16, top: 8, bottom: 2),
      child: Align(
        alignment: Alignment.centerLeft,
        child: RichText(
          text: TextSpan(
            style: const TextStyle(fontSize: 12, color: AppColors.muted),
            children: [
              const TextSpan(text: '共 '),
              TextSpan(
                text: '$_total',
                style: const TextStyle(
                    color: AppColors.fg, fontWeight: FontWeight.w600),
              ),
              const TextSpan(text: ' 条结果'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProductList() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      );
    }
    if (_goods.isEmpty) {
      return const EmptyState();
    }
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _loadData,
      child: ListView.builder(
        controller: _scrollCtrl,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        itemCount: _goods.length + (_loadingMore ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == _goods.length) {
            return const LoadMoreIndicator();
          }
          return _buildProductCard(_goods[index]);
        },
      ),
    );
  }

  Widget _buildProductCard(TradeGoods goods) {
    return ProductCard(
      goods: goods,
      margin: const EdgeInsets.only(top: 10),
      onTap: () async {
        await Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ProductDetailPage(goodsId: goods.goodsId!),
          ),
        );
        _loadData();
      },
    );
  }
}
