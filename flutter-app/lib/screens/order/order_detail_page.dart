import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/order_models.dart';
import '../../data/models/user_homepage.dart';
import '../../data/services/evaluation_api.dart';
import '../../data/services/order_api.dart';
import '../../data/services/session.dart';
import '../../data/services/user_api.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../common/utils/user_nav.dart';
import '../../common/utils/date_utils.dart';
import '../../widgets/cors_image.dart';
import '../goods/product_detail_page.dart';
import '../messages/chat_page.dart';
import '../profile/review_publish_page.dart';
import '../profile/review_list_page.dart';
import '../profile/dispute_submit_page.dart';
import 'payment_page.dart';

class OrderDetailPage extends StatefulWidget {
  final int orderId;

  const OrderDetailPage({super.key, required this.orderId});

  @override
  State<OrderDetailPage> createState() => _OrderDetailPageState();
}

class _OrderDetailPageState extends State<OrderDetailPage> {
  final _orderApi = OrderApi.instance;
  final _session = Session.instance;

  TradeOrder? _order;
  bool _loading = true;
  bool? _evaluated;
  UserHomepage? _peerInfo;

  Timer? _countdownTimer;
  int _remainingSeconds = 0;
  bool _countdownExpired = false;
  static const int _payTimeoutMinutes = 5; // matches backend trade.order.pay-timeout-minutes

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final order = await _orderApi.getOrderDetail(widget.orderId);
      if (mounted && order != null) {
        _fetchPeerInfo(order);
        setState(() {
          _order = order;
          _loading = false;
        });
        if (order.orderStatus == '3') {
          _checkEvaluated();
        }
        if (order.orderStatus == '0' && order.createTime != null) {
          _startCountdown(order.createTime!);
        }
      } else if (mounted) {
        setState(() => _loading = false);
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _fetchPeerInfo(TradeOrder order) async {
    final me = _session.currentUser?.userId;
    if (me == null) return;
    final peerId = me == order.buyerId ? order.sellerId : order.buyerId;
    try {
      _peerInfo = await UserApi.instance.getUserHomepage(peerId);
      if (mounted) setState(() {});
    } catch (_) {}
  }

  Future<void> _checkEvaluated() async {
    try {
      final evaluated =
          await EvaluationApi.instance.checkEvaluated(widget.orderId);
      if (mounted) setState(() => _evaluated = evaluated);
    } catch (_) {}
  }

  bool get _isBuyer {
    final me = _session.currentUser?.userId;
    return me != null && me == _order?.buyerId;
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    super.dispose();
  }

  void _startCountdown(String createTimeStr) {
    _countdownTimer?.cancel();
    _countdownExpired = false;
    try {
      final createTime = DateTime.parse(createTimeStr);
      final deadline = createTime.add(const Duration(minutes: _payTimeoutMinutes));
      _remainingSeconds = deadline.difference(DateTime.now()).inSeconds;
      if (_remainingSeconds <= 0) {
        _remainingSeconds = 0;
        _countdownExpired = true;
        if (mounted) setState(() {});
        return;
      }
      if (mounted) setState(() {});

      _countdownTimer = Timer.periodic(const Duration(seconds: 1), (_) {
        if (!mounted) return;
        setState(() {
          if (_remainingSeconds > 0) {
            _remainingSeconds--;
          }
          if (_remainingSeconds <= 0) {
            _countdownExpired = true;
            _countdownTimer?.cancel();
          }
        });
      });
    } catch (_) {}
  }

  String _formatCountdown(int seconds) {
    if (seconds <= 0) return '00:00';
    final m = (seconds ~/ 60).toString().padLeft(2, '0');
    final s = (seconds % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  // ── Actions ──

  Future<void> _cancelOrder() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('取消订单'),
        content: const Text('确定要取消这笔订单吗？'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('再想想')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('确定取消',
                  style: TextStyle(color: AppColors.price))),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.cancelOrder(widget.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '订单已取消');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _payOrder() async {
    showToast(context, '正在获取支付信息...');
    final html = await _orderApi.payOrder(widget.orderId);
    if (html == null || !mounted) {
      showToast(context, '发起支付失败');
      return;
    }
    final paid = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) => PaymentPage(orderId: widget.orderId, payHtml: html),
      ),
    );
    if (paid == true && mounted) {
      _loadData();
    }
  }

  Future<void> _finishOrder() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('确认收货'),
        content: const Text('确认已收到商品，完成交易？'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('再等等')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('确认收货',
                  style: TextStyle(color: AppColors.accent))),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.finishOrder(widget.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '交易已完成');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _applyRefund() async {
    final reasonCtrl = TextEditingController();
    final reason = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('申请退款'),
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
              onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, reasonCtrl.text),
              child:
                  const Text('提交', style: TextStyle(color: AppColors.price))),
        ],
      ),
    );
    reasonCtrl.dispose();
    if (reason == null || !mounted) return;
    final ok = await _orderApi.applyRefund(widget.orderId, reason);
    if (!mounted) return;
    if (ok) {
      showToast(context, '退款申请已提交');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _sellerRefund() async {
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
              onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, reasonCtrl.text),
              child: const Text('确认退款',
                  style: TextStyle(color: AppColors.price))),
        ],
      ),
    );
    reasonCtrl.dispose();
    if (reason == null || !mounted) return;
    final ok = await _orderApi.sellerRefund(widget.orderId, reason);
    if (!mounted) return;
    if (ok) {
      showToast(context, '退款已发起');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _agreeRefund() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('同意退款'),
        content: const Text('确定同意买家退款申请？将调用支付宝退款。'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('确定退款',
                  style: TextStyle(color: AppColors.accent))),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.agreeRefund(widget.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '退款已处理');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  Future<void> _rejectRefund() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('拒绝退款'),
        content: const Text('确定拒绝买家退款申请？订单将退回待收货状态。'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('拒绝退款',
                  style: TextStyle(color: AppColors.price))),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;
    final ok = await _orderApi.rejectRefund(widget.orderId);
    if (!mounted) return;
    if (ok) {
      showToast(context, '已拒绝退款申请');
      _loadData();
    } else {
      showToast(context, '操作失败');
    }
  }

  void _openChat() {
    final order = _order;
    if (order == null) return;
    final peerId = _isBuyer ? order.sellerId : order.buyerId;
    final peerName = _peerInfo?.nickname
        ?? (_isBuyer ? order.sellerNickname : order.buyerNickname)
        ?? '校园用户';
    final peerAvatar = _peerInfo?.avatar
        ?? (_isBuyer ? order.sellerAvatar : order.buyerAvatar);
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => ChatPage(
        peerId: peerId,
        peerName: peerName,
        goodsId: order.goodsId,
        peerItem: order.goodsTitle,
        peerAvatar: peerAvatar,
        goodsCoverImage: firstImageUrl(order.goodsImages ?? ''),
        goodsStatus: '4', // 已售出 — 已有订单关联必然已售出
        orderId: order.orderId,
      ),
    ));
  }

  void _goReview() {
    final order = _order;
    if (order == null) return;
    final targetName = _peerInfo?.nickname
        ?? (_isBuyer ? order.sellerNickname : order.buyerNickname)
        ?? '校园用户';
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => ReviewPublishPage(
        orderId: order.orderId,
        goodsTitle: order.goodsTitle,
        goodsPrice: order.tradePrice,
        targetUserName: targetName,
        targetRoleLabel: _isBuyer ? '卖家' : '买家',
        goodsImages: order.goodsImages,
      ),
    )).then((_) {
      if (mounted) _checkEvaluated();
    });
  }

  void _goDispute() {
    final order = _order;
    if (order == null) return;
    final sellerName = _peerInfo?.nickname
        ?? order.sellerNickname
        ?? '校园用户';
    final sellerAvatar = _peerInfo?.avatar
        ?? order.sellerAvatar;
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => DisputeSubmitPage(
        orderId: order.orderId,
        goodsId: order.goodsId,
        goodsTitle: order.goodsTitle,
        sellerName: sellerName,
        sellerAvatar: sellerAvatar,
      ),
    ));
  }

  // ── Build ──

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('订单详情'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: _loading
          ? const Center(
              child: CircularProgressIndicator(color: AppColors.accent))
          : _order == null
              ? const Center(
                  child: Text('订单不存在',
                      style: TextStyle(color: AppColors.muted)))
              : Column(
                  children: [
                    Expanded(child: _buildBody()),
                    _buildBottomBar(),
                  ],
                ),
    );
  }

  Widget _buildBody() {
    final order = _order!;
    return ListView(
      padding: const EdgeInsets.only(bottom: 16),
      children: [
        _buildStatusBanner(order),
        _buildOrderProgress(order),
        if (_shouldShowTradeInfo(order)) _buildTradeInfoCard(order),
        _buildCounterpartyRow(order),
        _buildSectionGap(),
        _buildProductCard(order),
        _buildSectionGap(),
        _buildPriceBreakdown(order),
        _buildSectionGap(),
        _buildOrderInfo(order),
      ],
    );
  }

  bool _shouldShowTradeInfo(TradeOrder order) {
    return ['2', '3'].contains(order.orderStatus) &&
        (order.tradePlace != null && order.tradePlace!.isNotEmpty);
  }

  // ── Status Banner ──

  Widget _buildStatusBanner(TradeOrder order) {
    final status = order.orderStatus ?? '0';
    final config = _statusConfig(status, order);

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: config.color,
              shape: BoxShape.circle,
            ),
            child: Icon(config.icon, color: Colors.white, size: 22),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(config.title,
                    style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                        color: config.color)),
                const SizedBox(height: 2),
                Text(config.desc,
                    style: const TextStyle(fontSize: 12, color: AppColors.muted)),
              ],
            ),
          ),
          if (config.countdown != null)
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.access_time, size: 16, color: config.color),
                const SizedBox(width: 4),
                Text(config.countdown!,
                    style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: config.color)),
              ],
            ),
        ],
      ),
    );
  }

  _StatusConfig _statusConfig(String status, TradeOrder order) {
    String? refundLabel = order.refundStatusLabel;
    switch (status) {
      case '0':
        if (_countdownExpired) {
          return _StatusConfig(
            color: AppColors.muted,
            icon: Icons.access_time,
            title: '支付已超时',
            desc: '订单即将被系统自动取消，请等待处理',
            countdown: '已超时',
          );
        }
        return _StatusConfig(
          color: const Color(0xFFFFB800),
          icon: Icons.access_time,
          title: '等待买家付款',
          desc: '请尽快完成支付，超时订单将自动取消',
          countdown: _remainingSeconds > 0 ? _formatCountdown(_remainingSeconds) : null,
        );
      case '2':
        return _StatusConfig(
          color: AppColors.accent,
          icon: Icons.local_shipping_outlined,
          title: '等待买家收货',
          desc: refundLabel != null
              ? '退款${refundLabel}${order.refundReason != null ? '：${order.refundReason}' : ''}'
              : '请确认商品无误后确认收货',
          countdown: null,
        );
      case '3':
        return _StatusConfig(
          color: AppColors.success,
          icon: Icons.check_circle_outline,
          title: '交易已完成',
          desc: '感谢你的交易，期待再次相遇',
          countdown: null,
        );
      case '4':
        return _StatusConfig(
          color: AppColors.muted,
          icon: Icons.cancel_outlined,
          title: '订单已取消',
          desc: order.cancelReason ?? '该订单已关闭，商品已恢复在售',
          countdown: null,
        );
      case '5':
        return _StatusConfig(
          color: const Color(0xFFFF3B30),
          icon: Icons.gavel,
          title: '争议处理中',
          desc: '请耐心等待仲裁结果',
          countdown: null,
        );
      case '6':
        return _StatusConfig(
          color: const Color(0xFFE65100),
          icon: Icons.refresh,
          title: refundLabel ?? '退款处理中',
          desc: order.refundReason ?? '',
          countdown: null,
        );
      case '7':
        return _StatusConfig(
          color: AppColors.muted,
          icon: Icons.check_circle_outline,
          title: '已退款',
          desc: order.refundReason ?? '退款已完成',
          countdown: null,
        );
      default:
        return _StatusConfig(
          color: AppColors.muted,
          icon: Icons.help_outline,
          title: '未知状态',
          desc: '',
          countdown: null,
        );
    }
  }

  // ── Trade Info Card ──

  Widget _buildOrderProgress(TradeOrder order) {
    final steps = _progressSteps(order);
    final currentIndex = _progressIndex(order);
    final activeColor = _statusConfig(order.orderStatus ?? '', order).color;

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.fromLTRB(18, 2, 18, 18),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          for (var i = 0; i < steps.length; i++) ...[
            Expanded(
              child: _progressStep(
                steps[i],
                isActive: i <= currentIndex,
                isCurrent: i == currentIndex,
                activeColor: activeColor,
              ),
            ),
            if (i < steps.length - 1)
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(top: 10),
                  child: _progressLine(
                    isActive: i < currentIndex,
                    activeColor: activeColor,
                  ),
                ),
              ),
          ],
        ],
      ),
    );
  }

  List<_ProgressStepData> _progressSteps(TradeOrder order) {
    final completed = order.orderStatus == '3';
    final evaluated = completed && _evaluated == true;
    final refunded = order.orderStatus == '7';
    final closed = order.orderStatus == '4';
    final disputed = order.orderStatus == '5';
    final refunding = order.orderStatus == '6';

    if (refunded) {
      return const [
        _ProgressStepData('已拍下', Icons.shopping_bag_outlined),
        _ProgressStepData('已付款', Icons.payments_outlined),
        _ProgressStepData('退款中', Icons.refresh),
        _ProgressStepData('已退款', Icons.check_circle_outline),
      ];
    }
    if (refunding) {
      return const [
        _ProgressStepData('已拍下', Icons.shopping_bag_outlined),
        _ProgressStepData('已付款', Icons.payments_outlined),
        _ProgressStepData('退款中', Icons.refresh),
        _ProgressStepData('退款完成', Icons.radio_button_unchecked),
      ];
    }
    if (closed) {
      return const [
        _ProgressStepData('已拍下', Icons.shopping_bag_outlined),
        _ProgressStepData('已取消', Icons.cancel_outlined),
        _ProgressStepData('已关闭', Icons.radio_button_unchecked),
      ];
    }
    if (disputed) {
      return const [
        _ProgressStepData('已拍下', Icons.shopping_bag_outlined),
        _ProgressStepData('已付款', Icons.payments_outlined),
        _ProgressStepData('争议中', Icons.gavel),
        _ProgressStepData('处理完成', Icons.radio_button_unchecked),
      ];
    }

    return [
      const _ProgressStepData('已拍下', Icons.shopping_bag_outlined),
      const _ProgressStepData('已付款', Icons.payments_outlined),
      _ProgressStepData(completed ? '交易成功' : '待收货',
          completed ? Icons.check_circle_outline : Icons.inventory_2_outlined),
      _ProgressStepData(
        evaluated ? '已评价' : '待评价',
        evaluated ? Icons.rate_review : Icons.rate_review_outlined,
      ),
    ];
  }

  int _progressIndex(TradeOrder order) {
    switch (order.orderStatus) {
      case '2':
        return 1;
      case '3':
        return _evaluated == true ? 3 : 2;
      case '4':
        return 1;
      case '5':
      case '6':
        return 2;
      case '7':
        return 3;
      case '0':
      default:
        return 0;
    }
  }

  Widget _progressStep(
    _ProgressStepData step, {
    required bool isActive,
    required bool isCurrent,
    required Color activeColor,
  }) {
    final color = isActive ? activeColor : AppColors.placeholder;
    final fill = isCurrent
        ? activeColor.withValues(alpha: 0.12)
        : isActive
            ? activeColor
            : Colors.transparent;
    final border = isActive ? activeColor : AppColors.placeholder;
    final iconColor = isCurrent
        ? activeColor
        : isActive
            ? Colors.white
            : AppColors.placeholder;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        AnimatedContainer(
          duration: const Duration(milliseconds: 180),
          width: 22,
          height: 22,
          decoration: BoxDecoration(
            color: fill,
            shape: BoxShape.circle,
            border: Border.all(color: border, width: 1.6),
          ),
          child: Icon(
            step.icon,
            size: isCurrent ? 14 : 13,
            color: iconColor,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          step.label,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 12,
            fontWeight: isActive ? FontWeight.w600 : FontWeight.w400,
            color: color,
          ),
        ),
      ],
    );
  }

  Widget _progressLine({
    required bool isActive,
    required Color activeColor,
  }) {
    return Container(
      height: 2,
      decoration: BoxDecoration(
        color: isActive ? activeColor : AppColors.divider,
        borderRadius: BorderRadius.circular(2),
      ),
    );
  }

  Widget _buildTradeInfoCard(TradeOrder order) {
    final parts = <String>[];
    if (order.tradeMethod != null && order.tradeMethod!.isNotEmpty) {
      parts.add('交易方式：${order.tradeMethod}');
    }
    if (order.tradePlace != null && order.tradePlace!.isNotEmpty) {
      parts.add(order.tradePlace!);
    }
    if (order.appointmentTime != null && order.appointmentTime!.isNotEmpty) {
      parts.add('约定时间：${formatDateTime(order.appointmentTime)}');
    }
    if (parts.isEmpty) return const SizedBox.shrink();

    final contactName = _peerInfo?.nickname
        ?? (_isBuyer ? order.sellerNickname : order.buyerNickname)
        ?? '校园用户';

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: AppColors.accent.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.location_on_outlined,
                size: 20, color: AppColors.accent),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(contactName,
                    style: const TextStyle(
                        fontSize: 14, fontWeight: FontWeight.w600)),
                const SizedBox(height: 4),
                Text(parts.join('\n'),
                    style: const TextStyle(
                        fontSize: 12, color: AppColors.muted, height: 1.4)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ── Counterparty Row ──

  Widget _buildCounterpartyRow(TradeOrder order) {
    final isBuyerView = _isBuyer;
    final name = _peerInfo?.nickname
        ?? (isBuyerView ? order.sellerNickname : order.buyerNickname)
        ?? '校园用户';
    final avatar = _peerInfo?.avatar
        ?? (isBuyerView ? order.sellerAvatar : order.buyerAvatar);
    final peerId = isBuyerView ? order.sellerId : order.buyerId;
    final label = isBuyerView ? '联系卖家' : '联系买家';

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => openUserProfile(context, peerId),
            child: _buildAvatar(avatar, peerId),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: GestureDetector(
              onTap: () => openUserProfile(context, peerId),
              behavior: HitTestBehavior.opaque,
              child: Text(name,
                  style: const TextStyle(
                      fontSize: 14, fontWeight: FontWeight.w500)),
            ),
          ),
          _actionBtn(label, AppColors.accent, _openChat, outlined: true),
        ],
      ),
    );
  }

  Widget _buildAvatar(String? avatar, int peerId) {
    // Follow same pattern as ProductDetailPage._buildSellerAvatar
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
              errorBuilder: (_, __, ___) =>
                  const Icon(Icons.person, size: 22, color: Colors.white),
            )
          : const Icon(Icons.person, size: 22, color: Colors.white),
    );
  }

  // ── Product Card ──

  Widget _buildProductCard(TradeOrder order) {
    final imageUrl = firstImageUrl(order.goodsImages ?? '');

    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(
          MaterialPageRoute(
              builder: (_) => ProductDetailPage(goodsId: order.goodsId)),
        );
      },
      child: Container(
        color: AppColors.surface,
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(AppColors.radiusSm),
              child: SizedBox(
                width: 80,
                height: 80,
                child: imageUrl != null
                    ? CorsImage(src: imageUrl, fit: BoxFit.cover)
                    : Container(
                        decoration: const BoxDecoration(
                          gradient: LinearGradient(
                            colors: [Color(0xFFF0E2F5), Color(0xFFE8D5F0)],
                          ),
                        ),
                        child: const Icon(Icons.image_outlined,
                            size: 28, color: AppColors.muted),
                      ),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(order.goodsTitle,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          height: 1.35)),
                  const SizedBox(height: 6),
                  Row(
                    children: [
                      Text.rich(
                        TextSpan(children: [
                          const TextSpan(
                              text: '¥',
                              style: TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.price)),
                          TextSpan(
                              text: order.tradePrice.toStringAsFixed(0),
                              style: const TextStyle(
                                  fontSize: 18,
                                  fontWeight: FontWeight.w700,
                                  color: AppColors.price,
                                  letterSpacing: -0.36)),
                        ]),
                      ),
                      const Spacer(),
                      const Text('×1',
                          style: TextStyle(
                              fontSize: 12, color: AppColors.muted)),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(width: 4),
            const Icon(Icons.chevron_right,
                size: 16, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

  // ── Price Breakdown ──

  Widget _buildPriceBreakdown(TradeOrder order) {
    final price = order.tradePrice;
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: [
          _priceRow('商品金额', '¥${price.toStringAsFixed(2)}'),
          Container(
            padding:
                const EdgeInsets.only(top: 10, bottom: 14),
            decoration:
                const BoxDecoration(border: Border(top: BorderSide(color: AppColors.border, style: BorderStyle.solid))),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('实付款',
                    style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppColors.fg)),
                Text.rich(
                  TextSpan(children: [
                    const TextSpan(
                        text: '¥',
                        style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: AppColors.price)),
                    TextSpan(
                        text: price.toStringAsFixed(2),
                        style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w700,
                            color: AppColors.price,
                            letterSpacing: -0.4)),
                  ]),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _priceRow(String label, String value, {Color? valueColor}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label,
              style: const TextStyle(fontSize: 13, color: AppColors.muted)),
          Text(value,
              style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                  color: valueColor ?? AppColors.fg)),
        ],
      ),
    );
  }

  // ── Order Info ──

  Widget _buildOrderInfo(TradeOrder order) {
    final items = <Widget>[];

    items.add(_infoRow('订单编号', order.orderNo ?? '--',
        trailing: _copyBtn(order.orderNo ?? '')));

    if (order.createTime != null) {
      items.add(_infoRow('创建时间', formatDateTime(order.createTime)));
    }

    if (order.paymentTime != null && order.paymentTime!.isNotEmpty) {
      items.add(_infoRow('付款时间', formatDateTime(order.paymentTime)));
    } else if (order.payTime != null && order.payTime!.isNotEmpty) {
      items.add(_infoRow('付款时间', formatDateTime(order.payTime)));
    }

    if (order.completeTime != null && order.completeTime!.isNotEmpty) {
      items.add(_infoRow('完成时间', formatDateTime(order.completeTime)));
    }

    if (order.confirmTime != null && order.confirmTime!.isNotEmpty) {
      items.add(_infoRow('确认时间', formatDateTime(order.confirmTime)));
    }

    if (order.cancelTime != null && order.cancelTime!.isNotEmpty) {
      items.add(_infoRow('取消时间', formatDateTime(order.cancelTime)));
    }

    if (order.refundApplyTime != null && order.refundApplyTime!.isNotEmpty) {
      items.add(_infoRow('退款申请时间', order.refundApplyTime!));
    }

    if (order.alipayTradeNo != null && order.alipayTradeNo!.isNotEmpty) {
      items.add(_infoRow('支付宝交易号', order.alipayTradeNo!));
    }

    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(children: items),
    );
  }

  Widget _infoRow(String label, String value, {Widget? trailing}) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 10),
      decoration: const BoxDecoration(
        border: Border(
            top: BorderSide(color: AppColors.border, width: 0.5)),
      ),
      child: Row(
        children: [
          Text(label,
              style:
                  const TextStyle(fontSize: 13, color: AppColors.muted)),
          const Spacer(),
          Text(value,
              style: const TextStyle(
                  fontSize: 12, color: AppColors.fg),
              textAlign: TextAlign.right),
          if (trailing != null) ...[
            const SizedBox(width: 6),
            trailing,
          ],
        ],
      ),
    );
  }

  Widget _copyBtn(String text) {
    return GestureDetector(
      onTap: () {
        Clipboard.setData(ClipboardData(text: text));
        showToast(context, '已复制订单编号');
      },
      child: const Icon(Icons.copy, size: 14, color: AppColors.muted),
    );
  }

  // ── Section Gap ──

  Widget _buildSectionGap() => const SizedBox(height: 8);

  // ── Bottom Bar ──

  Widget _buildBottomBar() {
    final order = _order;
    if (order == null) return const SizedBox.shrink();
    final actions = _buildActions(order);
    if (actions.isEmpty) {
      return Container(
        color: AppColors.surface,
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
        child: const Text('没有更多操作',
            style: TextStyle(color: AppColors.muted),
            textAlign: TextAlign.center),
      );
    }

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text('实付款',
                    style: TextStyle(fontSize: 12, color: AppColors.muted)),
                Text.rich(
                  TextSpan(children: [
                    const TextSpan(
                        text: '¥',
                        style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            color: AppColors.price)),
                    TextSpan(
                        text: order.tradePrice.toStringAsFixed(2),
                        style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w700,
                            color: AppColors.price,
                            letterSpacing: -0.36)),
                  ]),
                ),
              ],
            ),
          ),
          ...actions,
        ],
      ),
    );
  }

  List<Widget> _buildActions(TradeOrder order) {
    final status = order.orderStatus ?? '';
    final refundStatus = order.refundStatus;
    final widgets = <Widget>[];
    const spacer = SizedBox(width: 8);

    if (_isBuyer) {
      // Buyer actions
      switch (status) {
        case '0':
          widgets.add(_actionBtn('取消订单', AppColors.muted, _cancelOrder,
              outlined: true, danger: true));
          widgets.add(spacer);
          widgets.add(_actionBtn('去支付', AppColors.accent, _payOrder));
          break;
        case '2':
          if (refundStatus == null || refundStatus == '0') {
            widgets.add(_actionBtn('申请退款', AppColors.muted, _applyRefund,
                outlined: true));
            widgets.add(spacer);
            widgets.add(_actionBtn('确认收货', AppColors.accent, _finishOrder));
          }
          break;
        case '3':
          widgets
              .add(_actionBtn('申请仲裁', AppColors.muted, _goDispute, outlined: true));
          widgets.add(spacer);
          if (_evaluated == true) {
            widgets.add(_actionBtn('查看评价', const Color(0xFFFFB800), () {
              Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) => const ReviewListPage(initialTab: 1)),
              );
            }));
          } else {
            widgets.add(_actionBtn('去评价', AppColors.accent, _goReview));
          }
          break;
      }
    } else {
      // Seller actions
      switch (status) {
        case '2':
          if (refundStatus == null || refundStatus == '0') {
            widgets.add(_actionBtn('主动退款', AppColors.muted, _sellerRefund,
                outlined: true, danger: true));
          }
          break;
        case '6':
          if (refundStatus == '1') {
            widgets.add(_actionBtn('拒绝退款', AppColors.muted, _rejectRefund,
                outlined: true, danger: true));
            widgets.add(spacer);
            widgets.add(_actionBtn('同意退款', AppColors.accent, _agreeRefund));
          }
          break;
        case '3':
          widgets.add(_actionBtn('申请仲裁', AppColors.muted, _goDispute,
              outlined: true));
          widgets.add(spacer);
          if (_evaluated == true) {
            widgets.add(_actionBtn('查看评价', const Color(0xFFFFB800), () {
              Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) => const ReviewListPage(initialTab: 1)),
              );
            }));
          } else {
            widgets.add(_actionBtn('去评价', AppColors.accent, _goReview));
          }
          break;
      }
    }

    return widgets.map((w) {
      if (w is SizedBox) return w;
      return Flexible(child: w);
    }).toList();
  }

  Widget _actionBtn(String label, Color color, VoidCallback onTap,
      {bool outlined = false, bool danger = false}) {
    final fg = danger ? const Color(0xFFFF3B30) : color;
    return SizedBox(
      height: 36,
      child: outlined
          ? OutlinedButton(
              onPressed: onTap,
              style: OutlinedButton.styleFrom(
                foregroundColor: fg,
                side: BorderSide(color: fg, width: 1),
                padding: const EdgeInsets.symmetric(horizontal: 16),
                shape: RoundedRectangleBorder(
                  borderRadius:
                      BorderRadius.circular(AppColors.radiusPill),
                ),
                textStyle: const TextStyle(
                    fontSize: 13, fontWeight: FontWeight.w600),
              ),
              child: Text(label),
            )
          : ElevatedButton(
              onPressed: onTap,
              style: ElevatedButton.styleFrom(
                backgroundColor: color,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                shape: RoundedRectangleBorder(
                  borderRadius:
                      BorderRadius.circular(AppColors.radiusPill),
                ),
                textStyle: const TextStyle(
                    fontSize: 13, fontWeight: FontWeight.w600),
              ),
              child: Text(label),
            ),
    );
  }
}

class _StatusConfig {
  final Color color;
  final IconData icon;
  final String title;
  final String desc;
  final String? countdown;

  _StatusConfig({
    required this.color,
    required this.icon,
    required this.title,
    required this.desc,
    this.countdown,
  });
}

class _ProgressStepData {
  final String label;
  final IconData icon;

  const _ProgressStepData(this.label, this.icon);
}
