import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/evaluation_model.dart';
import '../../data/services/evaluation_api.dart';
import '../../common/utils/image_utils.dart';
import '../../common/utils/user_nav.dart';
import '../../common/utils/date_utils.dart';
import '../../widgets/cors_image.dart';
import '../order/order_detail_page.dart';

class ReviewListPage extends StatefulWidget {
  final int initialTab;
  const ReviewListPage({super.key, this.initialTab = 0});

  @override
  State<ReviewListPage> createState() => _ReviewListPageState();
}

class _ReviewListPageState extends State<ReviewListPage> {
  final _api = EvaluationApi.instance;
  final _scrollCtrl = ScrollController();

  late int _tabIndex = widget.initialTab;

  EvaluationScoreSummary? _summary;
  List<TradeEvaluation> _reviews = [];
  int _total = 0;
  bool _loading = true;
  bool _loadingMore = false;
  int _pageNum = 1;
  static const int _pageSize = 15;

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
        _reviews.length < _total) {
      _loadMore();
    }
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final score = await _api.getMyScore();
      final result = _tabIndex == 0
          ? await _api.getReceived(pageNum: 1, pageSize: _pageSize)
          : await _api.getSent(pageNum: 1, pageSize: _pageSize);
      if (mounted) {
        setState(() {
          _summary = score;
          _reviews = result.rows;
          _total = result.total;
          _pageNum = 1;
          _loading = false;
        });
      }
    } catch (_) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _loadMore() async {
    setState(() => _loadingMore = true);
    try {
      final result = _tabIndex == 0
          ? await _api.getReceived(pageNum: _pageNum + 1, pageSize: _pageSize)
          : await _api.getSent(pageNum: _pageNum + 1, pageSize: _pageSize);
      if (mounted) {
        setState(() {
          _reviews.addAll(result.rows);
          _total = result.total;
          _pageNum++;
          _loadingMore = false;
        });
      }
    } catch (_) {
      if (mounted) setState(() => _loadingMore = false);
    }
  }

  void _switchTab(int index) {
    if (index == _tabIndex) return;
    _tabIndex = index;
    setState(() {
      _reviews = [];
      _total = 0;
      _pageNum = 1;
      _loading = true;
    });
    _loadData();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('我的评价'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: Column(
        children: [
          if (_summary != null) _buildScoreSummary(),
          _buildTabs(),
          Expanded(child: _buildList()),
        ],
      ),
    );
  }

  Widget _buildScoreSummary() {
    final s = _summary!;
    final maxCount = s.distribution.values.fold<int>(1, (a, b) => a > b ? a : b);
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.fromLTRB(24, 20, 24, 20),
      child: Row(
        children: [
          Column(
            children: [
              Text(
                s.averageScore.toStringAsFixed(1),
                style: const TextStyle(
                  fontSize: 48,
                  fontWeight: FontWeight.w700,
                  color: Color(0xFFFFB800),
                  height: 1,
                ),
              ),
              const SizedBox(height: 4),
              const Text('综合评分', style: AppTextStyles.smallMuted),
            ],
          ),
          const SizedBox(width: 20),
          Expanded(
            child: Column(
              children: List.generate(5, (i) {
                final star = 5 - i;
                final cnt = s.distribution[star] ?? 0;
                final ratio = maxCount > 0 ? cnt / maxCount : 0.0;
                return Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Row(
                    children: [
                      SizedBox(
                        width: 24,
                        child: Text(
                          '${star}星',
                          style: AppTextStyles.smallMuted,
                          textAlign: TextAlign.right,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Expanded(
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(2),
                          child: LinearProgressIndicator(
                            value: ratio,
                            minHeight: 4,
                            backgroundColor: AppColors.border,
                            valueColor: const AlwaysStoppedAnimation(
                                Color(0xFFFFB800)),
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTabs() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          _buildTab('收到的评价', _summary?.totalReceived ?? 0, 0),
          _buildTab('发出的评价', _summary?.totalSent ?? 0, 1),
        ],
      ),
    );
  }

  Widget _buildTab(String label, int count, int index) {
    final active = _tabIndex == index;
    return Expanded(
      child: GestureDetector(
        onTap: () => _switchTab(index),
        behavior: HitTestBehavior.opaque,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(
                color: active ? AppColors.accent : Colors.transparent,
                width: 2,
              ),
            ),
          ),
          child: Text.rich(
            TextSpan(
              text: label,
              children: [
                TextSpan(
                  text: ' $count',
                  style: const TextStyle(fontSize: 11, color: AppColors.muted),
                ),
              ],
            ),
            textAlign: TextAlign.center,
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

  Widget _buildList() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      );
    }
    if (_reviews.isEmpty) {
      return ListView(
        children: [
          const SizedBox(height: 120),
          Center(
            child: Column(
              children: [
                Icon(Icons.star_outline, size: 48, color: AppColors.muted.withValues(alpha: 0.4)),
                const SizedBox(height: 12),
                const Text('暂无评价', style: AppTextStyles.muted),
              ],
            ),
          ),
        ],
      );
    }
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _loadData,
      child: ListView.builder(
        controller: _scrollCtrl,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        itemCount: _reviews.length + (_loadingMore ? 1 : 0),
        itemBuilder: (_, i) {
          if (i == _reviews.length) {
            return const Padding(
              padding: EdgeInsets.all(16),
              child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
            );
          }
          return _buildCard(_reviews[i]);
        },
      ),
    );
  }

  Widget _buildCard(TradeEvaluation e) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      padding: const EdgeInsets.all(14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildHeader(e),
          const SizedBox(height: 8),
          _buildStars(e.score),
          const SizedBox(height: 8),
          if (e.content != null && e.content!.isNotEmpty)
            Text(e.content!, style: AppTextStyles.bodyText),
          const SizedBox(height: 10),
          _buildGoodsLink(e),
        ],
      ),
    );
  }

  Widget _buildHeader(TradeEvaluation e) {
    final isReceived = _tabIndex == 0;
    final name = isReceived
        ? (e.fromUserName ?? '校园用户')
        : (e.toUserName ?? '校园用户');
    final avatar = isReceived ? e.fromUserAvatar : e.toUserAvatar;
    final userId = isReceived ? e.fromUserId : e.toUserId;
    final time = formatDateTime(e.createTime);
    return Row(
      children: [
        GestureDetector(
          onTap: () => openUserProfile(context, userId),
          child: _buildAvatar(avatar),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: GestureDetector(
            onTap: () => openUserProfile(context, userId),
            behavior: HitTestBehavior.opaque,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name,
                    style: const TextStyle(
                        fontSize: 14, fontWeight: FontWeight.w600)),
                Text(time,
                    style: const TextStyle(fontSize: 12, color: AppColors.muted)),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAvatar(String? avatarUrl) {
    final imageUrl = firstImageUrl(avatarUrl ?? '');
    return Container(
      width: 40,
      height: 40,
      decoration: const BoxDecoration(
        shape: BoxShape.circle,
        gradient: LinearGradient(
          colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      clipBehavior: Clip.antiAlias,
      child: imageUrl != null
          ? CorsImage(src: imageUrl, fit: BoxFit.cover)
          : const Icon(Icons.person, size: 20, color: Colors.white),
    );
  }

  Widget _buildStars(int score) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (i) {
        return Icon(
          i < score ? Icons.star : Icons.star_border,
          size: 16,
          color: i < score ? const Color(0xFFFFB800) : AppColors.border,
        );
      }),
    );
  }

  Widget _buildGoodsLink(TradeEvaluation e) {
    final imageUrl = firstImageUrl(e.goodsImages ?? '');
    return GestureDetector(
      onTap: () {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => OrderDetailPage(orderId: e.orderId),
          ),
        );
      },
      child: Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: const Color(0xFFF8F1FA),
            borderRadius: BorderRadius.circular(AppColors.radiusSm),
          ),
          child: Row(
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: SizedBox(
                  width: 40,
                  height: 40,
                  child: imageUrl != null
                      ? CorsImage(src: imageUrl, fit: BoxFit.cover)
                      : Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                              colors: [Color(0xFFF0E2F5), Color(0xFFE8D5F0)],
                            ),
                          ),
                          child: const Icon(Icons.image_outlined,
                              size: 20, color: AppColors.muted),
                        ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      e.goodsTitle ?? '',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500),
                    ),
                    Text(
                      '¥${(e.goodsPrice ?? 0).toStringAsFixed(0)}',
                      style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: AppColors.price,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
    );
  }
}
