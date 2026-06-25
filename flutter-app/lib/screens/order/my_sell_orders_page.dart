import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/order_models.dart';
import '../../data/services/order_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/empty_state.dart';
import '../../data/services/evaluation_api.dart';
import '../../data/services/user_api.dart';
import '../profile/dispute_submit_page.dart';
import '../profile/review_list_page.dart';
import '../profile/review_publish_page.dart';
import 'order_detail_page.dart';

class MySellOrdersPage extends StatefulWidget {
  const MySellOrdersPage({super.key});

  @override
  State<MySellOrdersPage> createState() => _MySellOrdersPageState();
}

class _MySellOrdersPageState extends State<MySellOrdersPage> {
  final _orderApi = OrderApi.instance;

  List<TradeOrder> _allOrders = [];
  bool _loading = false;
  final _reviewedOrderIds = <int>{};

  int _tabIndex = 0;
  static const _tabs = ['全部', '待付款', '待交割', '待评价', '退款中'];

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final result =
          await _orderApi.getMySellOrders(pageNum: 1, pageSize: 200);
      if (mounted) {
        setState(() {
          _allOrders = result.rows;
          _loading = false;
        });
        _syncReviewStatus();
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _syncReviewStatus() async {
    final completedOrders =
        _allOrders.where((o) => o.orderStatus == '3').toList();
    if (completedOrders.isEmpty) return;
    final results = await Future.wait(
      completedOrders.map((o) => EvaluationApi.instance.checkEvaluated(o.orderId)),
    );
    if (!mounted) return;
    setState(() {
      for (int i = 0; i < completedOrders.length; i++) {
        if (results[i]) {
          _reviewedOrderIds.add(completedOrders[i].orderId);
        }
      }
    });
  }

  List<TradeOrder> get _filteredOrders {
    switch (_tabIndex) {
      case 0: // 全部
        return _allOrders
            .where((o) => ['0', '2', '3', '6'].contains(o.orderStatus))
            .toList();
      case 1: // 待付款
        return _allOrders.where((o) => o.orderStatus == '0').toList();
      case 2: // 待交割
        return _allOrders.where((o) => o.orderStatus == '2').toList();
      case 3: // 待评价
        return _allOrders
            .where((o) =>
                o.orderStatus == '3' &&
                !_reviewedOrderIds.contains(o.orderId))
            .toList();
      case 4: // 退款中
        return _allOrders.where((o) => o.orderStatus == '6').toList();
      default:
        return [];
    }
  }

  // ── Actions ──

  Future<void> _sellerRefund(TradeOrder order) async {
    final reasonCtrl = TextEditingController();
    final reason = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('主动退款'),
        content: TextField(
          controller: reasonCtrl,
          maxLines: 3,
          decoration: const InputDecoration(
            hintText: '请输入退款原因...',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, reasonCtrl.text),
            child: const Text('确认退款',
                style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    reasonCtrl.dispose();
    if (reason == null || !mounted) return;
    final ok = await _orderApi.sellerRefund(order.orderId, reason);
    if (!mounted) return;
    if (ok) {
      showToast(context, '退款已发起');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _agreeRefund(TradeOrder order) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('同意退款'),
        content: const Text('确定同意买家退款申请？将调用支付宝退款。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('确定退款',
                style: TextStyle(color: AppColors.accent)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.agreeRefund(order.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '退款已处理');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _rejectRefund(TradeOrder order) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('拒绝退款'),
        content: const Text('确定拒绝买家退款申请？订单将退回待收货状态。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('拒绝退款',
                style: TextStyle(color: AppColors.price)),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.rejectRefund(order.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '已拒绝退款申请');
      _loadData();
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
        title: const Text('我卖出的'),
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

  Widget _buildTabs() {
    return Container(
      color: AppColors.surface,
      height: 44,
      child: Row(
        children: List.generate(_tabs.length, (index) {
          final active = _tabIndex == index;
          return Expanded(
            child: GestureDetector(
              onTap: () => setState(() => _tabIndex = index),
              behavior: HitTestBehavior.opaque,
              child: Container(
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  border: Border(
                    bottom: BorderSide(
                      color: active ? AppColors.accent : Colors.transparent,
                      width: 2,
                    ),
                  ),
                ),
                child: Text(
                  _tabs[index],
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: active ? FontWeight.w600 : FontWeight.w500,
                    color: active ? AppColors.accent : AppColors.muted,
                  ),
                ),
              ),
            ),
          );
        }),
      ),
    );
  }

  Widget _buildContent() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      );
    }
    final orders = _filteredOrders;
    if (orders.isEmpty) {
      return const EmptyState();
    }
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _loadData,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        itemCount: orders.length,
        itemBuilder: (context, index) => _buildOrderCard(orders[index]),
      ),
    );
  }

  Widget _buildOrderCard(TradeOrder order) {
    final imageUrl = firstImageUrl(order.goodsImages ?? '');
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => OrderDetailPage(orderId: order.orderId),
                ),
              );
              _loadData();
            },
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  _buildThumb(imageUrl),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          order.goodsTitle,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w600,
                            color: AppColors.fg,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          '买家：${order.buyerNickname ?? "校园用户"}',
                          style: AppTextStyles.muted,
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Text(
                              '¥${order.tradePrice.toStringAsFixed(0)}',
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w700,
                                color: AppColors.price,
                              ),
                            ),
                            const Spacer(),
                            _buildStatusBadge(order),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          _buildDivider(),
          _buildActions(order),
        ],
      ),
    );
  }

  Widget _buildThumb(String? imageUrl) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppColors.radiusSm),
      child: SizedBox(
        width: 80,
        height: 80,
        child: imageUrl != null
            ? CorsImage(src: imageUrl, fit: BoxFit.cover)
            : Container(
                color: const Color(0xFFF0E8F5),
                child: const Icon(Icons.image_outlined,
                    size: 28, color: AppColors.placeholder),
              ),
      ),
    );
  }

  Widget _buildStatusBadge(TradeOrder order) {
    final label = order.statusLabel;
    final refundLabel = order.refundStatusLabel;
    final displayLabel =
        (refundLabel != null) ? '$label · $refundLabel' : label;
    Color bg;
    Color fg;
    switch (label) {
      case '待支付':
        bg = const Color(0xFFFFF0F0);
        fg = const Color(0xFFD32F2F);
        break;
      case '待收货':
        bg = const Color(0xFFE8F5E9);
        fg = const Color(0xFF2E7D32);
        break;
      case '已完成':
        bg = const Color(0xFFF5F5F5);
        fg = AppColors.muted;
        break;
      case '已取消':
        bg = const Color(0xFFF5F5F5);
        fg = AppColors.muted;
        break;
      case '争议中':
        bg = const Color(0xFFFFF0F0);
        fg = const Color(0xFFD32F2F);
        break;
      case '退款中':
        bg = const Color(0xFFFFF3E0);
        fg = const Color(0xFFE65100);
        break;
      case '已退款':
        bg = const Color(0xFFF5F5F5);
        fg = AppColors.muted;
        break;
      default:
        bg = const Color(0xFFF5F5F5);
        fg = AppColors.muted;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppColors.radiusPill),
      ),
      child: Text(
        displayLabel,
        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: fg),
      ),
    );
  }

  Widget _buildDivider() {
    return const Divider(
        height: 1, color: AppColors.border, indent: 12, endIndent: 12);
  }

  Widget _buildActions(TradeOrder order) {
    final status = order.orderStatus;
    final refundStatus = order.refundStatus;
    final children = <Widget>[];
    const spacer = SizedBox(width: 8);

    if (status == '2') {
      children.add(
          _actionBtn('主动退款', AppColors.muted, () => _sellerRefund(order)));
    }

    if (status == '6' && refundStatus == '1') {
      children
          .add(_actionBtn('拒绝退款', AppColors.muted, () => _rejectRefund(order)));
      children.add(spacer);
      children
          .add(_actionBtn('同意退款', AppColors.accent, () => _agreeRefund(order)));
    }

    if (status == '3') {
      final reviewed = _reviewedOrderIds.contains(order.orderId);
      if (reviewed) {
        children.add(_actionBtn('查看评价', const Color(0xFFFFB800), () {
          Navigator.of(context).push(
            MaterialPageRoute(
                builder: (_) => const ReviewListPage(initialTab: 1)),
          );
        }));
      } else {
        children.add(_actionBtn('评价', const Color(0xFFFFB800), () async {
          final targetName = order.buyerNickname ?? '校园用户';
          final refreshed = await Navigator.of(context).push<bool>(
            MaterialPageRoute(
              builder: (_) => ReviewPublishPage(
                orderId: order.orderId,
                goodsTitle: order.goodsTitle,
                goodsPrice: order.tradePrice,
                targetUserName: targetName,
                targetRoleLabel: '买家',
                goodsImages: order.goodsImages,
              ),
            ),
          );
          if (refreshed == true && mounted) {
            _reviewedOrderIds.add(order.orderId);
            setState(() {});
          }
        }));
      }
      children.add(spacer);
      children.add(_actionBtn('申请仲裁', AppColors.muted, () async {
        String? sellerName = order.sellerNickname;
        String? sellerAvatar = order.sellerAvatar;
        try {
          final peerInfo = await UserApi.instance.getUserHomepage(order.sellerId);
          sellerName = peerInfo.nickname ?? sellerName;
          sellerAvatar = peerInfo.avatar ?? sellerAvatar;
        } catch (_) {}
        if (!mounted) return;
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => DisputeSubmitPage(
              orderId: order.orderId,
              goodsId: order.goodsId,
              goodsTitle: order.goodsTitle,
              sellerName: sellerName ?? '校园用户',
              sellerAvatar: sellerAvatar,
            ),
          ),
        );
      }));
    }

    if (children.isEmpty) {
      return const SizedBox(height: 8);
    }

    return Padding(
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
      child: Align(
        alignment: Alignment.centerRight,
        child: Wrap(
          spacing: 0,
          runSpacing: 8,
          children: children,
        ),
      ),
    );
  }

  Widget _actionBtn(String label, Color color, VoidCallback onTap) {
    return SizedBox(
      height: 32,
      child: OutlinedButton(
        onPressed: onTap,
        style: OutlinedButton.styleFrom(
          foregroundColor: color,
          side: BorderSide(color: color, width: 1),
          padding: const EdgeInsets.symmetric(horizontal: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppColors.radiusPill),
          ),
          textStyle:
              const TextStyle(fontSize: 13, fontWeight: FontWeight.w500),
        ),
        child: Text(label),
      ),
    );
  }
}
