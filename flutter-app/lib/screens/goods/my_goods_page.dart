import 'dart:async';
import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/goods_api.dart';
import '../../data/services/order_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/image_placeholder.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/load_more_indicator.dart';
import 'product_detail_page.dart';
import 'goods_publish_page.dart';
import '../order/order_detail_page.dart';

class MyGoodsPage extends StatefulWidget {
  final int initialTab;
  const MyGoodsPage({super.key, this.initialTab = 0});

  @override
  State<MyGoodsPage> createState() => _MyGoodsPageState();
}

class _MyGoodsPageState extends State<MyGoodsPage> {
  final _goodsApi = GoodsApi.instance;
  final _orderApi = OrderApi.instance;

  late int _tabIndex = widget.initialTab;
  static const _pageSize = 10;

  // Three independent lists
  final List<TradeGoods> _activeGoods = [];
  final List<TradeGoods> _soldGoods = [];
  final List<TradeGoods> _auditGoods = [];

  // Map goodsId → orderId for sold goods
  final Map<int, int> _soldGoodsOrderId = {};

  int _activeTotal = 0, _soldTotal = 0, _auditTotal = 0;
  bool _loading = false;
  final Map<int, bool> _loadingMore = {0: false, 1: false, 2: false};
  final Map<int, int> _pageNum = {0: 1, 1: 1, 2: 1};

  final _scrollCtrls = <int, ScrollController>{};

  List<TradeGoods> get _list => _tabIndex == 0
      ? _activeGoods
      : _tabIndex == 1
          ? _soldGoods
          : _auditGoods;
  int get _total =>
      _tabIndex == 0 ? _activeTotal : _tabIndex == 1 ? _soldTotal : _auditTotal;

  @override
  void initState() {
    super.initState();
    for (int i = 0; i < 3; i++) {
      _scrollCtrls[i] = ScrollController();
      _scrollCtrls[i]!.addListener(() => _onScroll(i));
    }
    _loadAll();
  }

  @override
  void dispose() {
    for (final c in _scrollCtrls.values) {
      c.dispose();
    }
    super.dispose();
  }

  void _onScroll(int tab) {
    final ctrl = _scrollCtrls[tab]!;
    if (ctrl.position.pixels >= ctrl.position.maxScrollExtent - 100 &&
        !(_loadingMore[tab] ?? false) &&
        _listForTab(tab).length < _totalForTab(tab)) {
      _loadMore(tab);
    }
  }

  List<TradeGoods> _listForTab(int tab) => tab == 0
      ? _activeGoods
      : tab == 1
          ? _soldGoods
          : _auditGoods;

  int _totalForTab(int tab) =>
      tab == 0 ? _activeTotal : tab == 1 ? _soldTotal : _auditTotal;

  Future<void> _loadAll() async {
    setState(() => _loading = true);
    final t0 = _loadTab(0, '1');
    final t1 = _loadTab(1, '4');
    final t2 = _loadTab(2, '0,2');
    final results0 = await t0;
    final results1 = await t1;
    final results2 = await t2;
    // Load order map after goods but before showing content, so sold tab nav is ready
    await _loadSoldOrderMap();
    if (mounted) {
      setState(() {
        _activeGoods
          ..clear()
          ..addAll(results0.$1);
        _activeTotal = results0.$2;
        _soldGoods
          ..clear()
          ..addAll(results1.$1);
        _soldTotal = results1.$2;
        _auditGoods
          ..clear()
          ..addAll(results2.$1);
        _auditTotal = results2.$2;
        _loading = false;
      });
    }
  }

  Future<void> _loadSoldOrderMap() async {
    try {
      final result = await _orderApi.getMySellOrders(pageNum: 1, pageSize: 999);
      _soldGoodsOrderId.clear();
      for (final order in result.rows) {
        _soldGoodsOrderId.putIfAbsent(order.goodsId, () => order.orderId);
      }
    } catch (_) {}
  }

  Future<(List<TradeGoods>, int)> _loadTab(int tab, String statusFilter) async {
    _pageNum[tab] = 1;
    // "0,2" means both pending and rejected — merge two API calls
    if (statusFilter == '0,2') {
      // 审核中 tab 不分页，一次拉取全部，避免两个状态合并分页错位
      final r1 = await _goodsApi.getMyGoods(
        pageNum: 1, pageSize: 999, goodsStatus: '0');
      final r2 = await _goodsApi.getMyGoods(
        pageNum: 1, pageSize: 999, goodsStatus: '2');
      final merged = [...r1.rows, ...r2.rows];
      merged.sort((a, b) => (b.goodsId ?? 0).compareTo(a.goodsId ?? 0));
      return (merged, r1.total + r2.total);
    }
    final result = await _goodsApi.getMyGoods(
      pageNum: 1, pageSize: _pageSize, goodsStatus: statusFilter);
    return (result.rows, result.total);
  }

  Future<void> _loadMore(int tab) async {
    // 审核中 tab 已在首次加载全部数据，无需分页
    if (tab == 2) return;
    _loadingMore[tab] = true;
    if (mounted) setState(() {});
    final statusFilter = tab == 0 ? '1' : '4';
    final p = (_pageNum[tab] ?? 1) + 1;
    final result = await _goodsApi.getMyGoods(
      pageNum: p, pageSize: _pageSize, goodsStatus: statusFilter);
    final newRows = result.rows;
    final newTotal = result.total;
    if (!mounted) return;
    _pageNum[tab] = p;
    final list = _listForTab(tab);
    setState(() {
      list.addAll(newRows);
      if (tab == 0) {
        _activeTotal = newTotal;
      } else {
        _soldTotal = newTotal;
      }
      _loadingMore[tab] = false;
    });
  }

  // ── Actions ──

  Future<void> _offlineGoods(int index) async {
    final goods = _activeGoods[index];
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('确认下架'),
        content: const Text('下架后其他同学将无法看到该商品，确定下架吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('确认下架',
                style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _goodsApi.offlineGoods(goods.goodsId!);
    if (!mounted) return;
    if (ok) {
      _activeGoods.removeAt(index);
      _activeTotal--;
      showToast(context, '已下架');
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _deleteGoods(int index) async {
    final goods = _auditGoods[index];
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('撤回审核'),
        content: const Text('确定要撤回该商品吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('确认撤回',
                style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _goodsApi.deleteGoods(goods.goodsId!);
    if (!mounted) return;
    if (ok) {
      _auditGoods.removeAt(index);
      _auditTotal--;
      showToast(context, '已撤回');
    } else {
      showToast(context, '操作失败');
    }
  }

  // ── Build ──

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('我的发布'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: Column(
        children: [
          _buildTabs(),
          Expanded(child: _buildContent()),
        ],
      ),
    );
  }

  void _switchTab(int index) {
    if (index == _tabIndex) return;
    setState(() => _tabIndex = index);
  }

  Widget _buildTabs() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          _buildTabBtn('在售${_activeTotal > 0 ? " $_activeTotal" : ""}', 0),
          _buildTabBtn('已售${_soldTotal > 0 ? " $_soldTotal" : ""}', 1),
          _buildTabBtn('审核中${_auditTotal > 0 ? " $_auditTotal" : ""}', 2),
        ],
      ),
    );
  }

  Widget _buildTabBtn(String label, int index) {
    final active = _tabIndex == index;
    return Expanded(
      child: GestureDetector(
        onTap: () => _switchTab(index),
        behavior: HitTestBehavior.opaque,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(
                color: active ? AppColors.accent : Colors.transparent,
                width: 2,
              ),
            ),
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: active ? FontWeight.w600 : FontWeight.w500,
              color: active ? AppColors.accent : AppColors.muted,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildContent() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      );
    }
    if (_list.isEmpty) {
      return Column(
        children: [
          Expanded(child: _buildEmpty()),
        ],
      );
    }
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _loadAll,
      child: ListView.builder(
        controller: _scrollCtrls[_tabIndex],
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        itemCount: _list.length + (_loadingMore[_tabIndex] == true ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == _list.length) {
            return const LoadMoreIndicator();
          }
          return _buildCard(_list[index], index);
        },
      ),
    );
  }

  Widget _buildEmpty() {
    String title;
    String desc;
    switch (_tabIndex) {
      case 0:
        title = '暂无在售宝贝';
        desc = '去发布你的第一件闲置好物吧';
        break;
      case 1:
        title = '暂无已售记录';
        desc = '上架的商品售出后会显示在这里';
        break;
      default:
        title = '暂无审核中商品';
        desc = '商品审核通过后即可上架';
    }
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: const Color(0xFFF2EDF7),
                borderRadius: BorderRadius.circular(36),
              ),
              child: const Icon(Icons.inventory_2_outlined,
                  size: 32, color: AppColors.placeholder),
            ),
            const SizedBox(height: 16),
            Text(title,
                style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: AppColors.fg)),
            const SizedBox(height: 6),
            Text(desc,
                style: const TextStyle(fontSize: 13, color: AppColors.muted),
                textAlign: TextAlign.center),
            if (_tabIndex == 0) ...[
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => Navigator.of(context).pop(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.accent,
                  foregroundColor: Colors.white,
                  padding:
                      const EdgeInsets.symmetric(horizontal: 28, vertical: 10),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(99),
                  ),
                ),
                child: const Text('发布闲置',
                    style: TextStyle(
                        fontSize: 14, fontWeight: FontWeight.w600)),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildCard(TradeGoods goods, int index) {
    final imageUrl = firstImageUrl(goods.imageUrls);
    final status = goods.goodsStatus ?? '';
    final isActive = status == '1';
    final isAudit = status == '0' || status == '2';
    final isRejected = status == '2';

    final isSold = status == '4';
    return GestureDetector(
      onTap: isActive ? () async {
        await Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ProductDetailPage(goodsId: goods.goodsId!),
          ),
        );
        _loadAll();
      } : isSold ? () async {
        final orderId = _soldGoodsOrderId[goods.goodsId];
        if (orderId != null) {
          await Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => OrderDetailPage(orderId: orderId),
            ),
          );
          _loadAll();
        }
      } : null,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: SizedBox(
                      width: 80,
                      height: 80,
                      child: imageUrl != null
                          ? CorsImage(
                              src: imageUrl,
                              fit: BoxFit.cover,
                              onTap: isSold ? () async {
                                final orderId = _soldGoodsOrderId[goods.goodsId];
                                if (orderId != null) {
                                  await Navigator.of(context).push(
                                    MaterialPageRoute(
                                      builder: (_) => OrderDetailPage(orderId: orderId),
                                    ),
                                  );
                                  _loadAll();
                                }
                              } : () async {
                                await Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (_) => ProductDetailPage(
                                        goodsId: goods.goodsId!),
                                  ),
                                );
                                _loadAll();
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
                        Text(goods.title,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                                color: AppColors.fg,
                                height: 1.4)),
                        const SizedBox(height: 6),
                        Row(
                          children: [
                            Text.rich(
                              TextSpan(children: [
                                const TextSpan(
                                    text: '¥',
                                    style: TextStyle(
                                        fontSize: 12,
                                        fontWeight: FontWeight.w600,
                                        color: Color(0xFFF43370))),
                                TextSpan(
                                    text: goods.price.toStringAsFixed(0),
                                    style: const TextStyle(
                                        fontSize: 17,
                                        fontWeight: FontWeight.w700,
                                        color: Color(0xFFF43370),
                                        letterSpacing: -0.3)),
                              ]),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 4),
                  _buildStatusBadge(isActive
                      ? '在售'
                      : status == '4'
                          ? '已售'
                          : isRejected
                              ? '审核拒绝'
                              : '审核中'),
                ],
              ),
            ),
            if (isActive || isAudit)
              Container(
                decoration: const BoxDecoration(
                  border: Border(top: BorderSide(color: AppColors.border)),
                ),
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                        '${goods.viewCount ?? 0} 浏览 · ${goods.favoriteCount ?? 0} 收藏',
                        style: const TextStyle(
                            fontSize: 11, color: AppColors.muted)),
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: isActive
                          ? [
                              _actionBtn('下架', isDanger: true,
                                  onTap: () => _offlineGoods(index)),
                            ]
                          : [
                              _actionBtn('编辑重新提交', isAccent: false,
                                  onTap: () async {
                                    await Navigator.of(context).push(
                                      MaterialPageRoute(
                                        builder: (_) => GoodsPublishPage(editGoods: goods),
                                      ),
                                    );
                                    _loadAll();
                                  }),
                              const SizedBox(width: 8),
                              _actionBtn('撤回', isDanger: true,
                                  onTap: () => _deleteGoods(index)),
                            ],
                    ),
                  ],
                ),
              ),
            if (status == '4')
              Container(
                decoration: const BoxDecoration(
                  border: Border(top: BorderSide(color: AppColors.border)),
                ),
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                        '${goods.viewCount ?? 0} 浏览 · ${goods.favoriteCount ?? 0} 收藏',
                        style: const TextStyle(
                            fontSize: 11, color: AppColors.muted)),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusBadge(String label) {
    Color bg, fg;
    switch (label) {
      case '在售':
        bg = const Color(0xFFD4F5E8);
        fg = const Color(0xFF00A878);
        break;
      case '审核中':
        bg = const Color(0xFFFFE8E0);
        fg = const Color(0xFFE89000);
        break;
      case '审核拒绝':
        bg = const Color(0xFFFFE0E5);
        fg = const Color(0xFFE02424);
        break;
      default:
        bg = const Color(0xFFF2EDF7);
        fg = AppColors.muted;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(99),
      ),
      child: Text(label,
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: fg)),
    );
  }

  Widget _actionBtn(String label,
      {bool isAccent = false, bool isDanger = false, VoidCallback? onTap}) {
    Color fg;
    Color border;
    if (isDanger) {
      fg = const Color(0xFFFF3B30);
      border = const Color(0xFFFF3B30);
    } else if (isAccent) {
      fg = Colors.white;
      border = AppColors.accent;
    } else {
      fg = AppColors.fg;
      border = AppColors.border;
    }
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
        decoration: BoxDecoration(
          color: isAccent ? AppColors.accent : Colors.transparent,
          border: Border.all(color: border),
          borderRadius: BorderRadius.circular(99),
        ),
        child: Text(label,
            style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: fg)),
      ),
    );
  }
}
