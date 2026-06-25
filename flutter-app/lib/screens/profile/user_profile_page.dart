import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/image_utils.dart';
import '../../data/models/trade_goods.dart';
import '../../data/models/user_homepage.dart';
import '../../data/services/goods_api.dart';
import '../../data/services/user_api.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/empty_state.dart';
import '../../widgets/load_more_indicator.dart';
import '../../widgets/product_card.dart';
import '../goods/product_detail_page.dart';

/// 用户公开主页（只读）：展示某用户的脱敏资料与其在售商品。
/// 通过 [openUserProfile] 进入；点击自己头像时不会到这里（会跳"我的"）。
class UserProfilePage extends StatefulWidget {
  final int userId;

  const UserProfilePage({super.key, required this.userId});

  @override
  State<UserProfilePage> createState() => _UserProfilePageState();
}

class _UserProfilePageState extends State<UserProfilePage> {
  final _userApi = UserApi.instance;
  final _goodsApi = GoodsApi.instance;
  final ScrollController _scrollCtrl = ScrollController();

  UserHomepage? _homepage;
  bool _loading = true;
  String? _error;

  final List<TradeGoods> _goods = [];
  int _total = 0;
  bool _loadingMore = false;
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
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final homepage = await _userApi.getUserHomepage(widget.userId);
      final result = await _goodsApi.getGoodsList(
        sellerId: widget.userId,
        pageNum: 1,
        pageSize: _pageSize,
      );
      if (!mounted) return;
      setState(() {
        _homepage = homepage;
        _goods
          ..clear()
          ..addAll(result.rows);
        _total = result.total;
        _pageNum = 1;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _error = '加载失败，请重试';
        _loading = false;
      });
    }
  }

  Future<void> _loadMore() async {
    setState(() => _loadingMore = true);
    try {
      final result = await _goodsApi.getGoodsList(
        sellerId: widget.userId,
        pageNum: _pageNum + 1,
        pageSize: _pageSize,
      );
      if (!mounted) return;
      setState(() {
        _goods.addAll(result.rows);
        _total = result.total;
        _pageNum++;
        _loadingMore = false;
      });
    } catch (_) {
      if (mounted) setState(() => _loadingMore = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('个人主页'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.accent),
      );
    }
    if (_error != null || _homepage == null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: AppColors.muted),
            const SizedBox(height: 12),
            Text(_error ?? '用户不存在', style: AppTextStyles.muted),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadData,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.accent,
                foregroundColor: Colors.white,
              ),
              child: const Text('重试'),
            ),
          ],
        ),
      );
    }
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _loadData,
      child: ListView.builder(
        controller: _scrollCtrl,
        padding: const EdgeInsets.only(bottom: 16),
        itemCount: 2 +
            (_goods.isEmpty ? 1 : _goods.length) +
            (_loadingMore ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == 0) return _buildHeaderCard();
          if (index == 1) return _buildGoodsSectionTitle();
          if (_goods.isEmpty) return _buildGoodsEmpty();
          final goodsIndex = index - 2;
          if (goodsIndex == _goods.length) return const LoadMoreIndicator();
          return _buildGoodsCard(_goods[goodsIndex]);
        },
      ),
    );
  }

  // ── Header ──

  Widget _buildHeaderCard() {
    final h = _homepage!;
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 18),
      margin: const EdgeInsets.only(bottom: 8),
      child: Column(
        children: [
          Row(
            children: [
              _buildAvatar(h.avatar),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      h.nickname ?? '校园用户',
                      style: AppTextStyles.pageTitle,
                    ),
                    const SizedBox(height: 6),
                    _buildCreditBadge(h.creditScore),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 18),
          _buildStatsRow(h),
        ],
      ),
    );
  }

  Widget _buildAvatar(String? avatarUrl) {
    final hasAvatar = avatarUrl != null && avatarUrl.isNotEmpty;
    return Container(
      width: 64,
      height: 64,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: hasAvatar ? null : AppDecorations.avatarGradient.gradient,
      ),
      clipBehavior: Clip.antiAlias,
      child: hasAvatar
          ? CorsImage(
              src: ensureAbsoluteUrl(avatarUrl),
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) =>
                  const Icon(Icons.person, size: 32, color: Colors.white),
            )
          : const Icon(Icons.person, size: 32, color: Colors.white),
    );
  }

  Widget _buildCreditBadge(int score) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF3E0),
        borderRadius: BorderRadius.circular(AppColors.radiusPill),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.stars_rounded, size: 14, color: Color(0xFFFF9800)),
          const SizedBox(width: 4),
          Text(
            '信用分 $score',
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Color(0xFFE65100),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsRow(UserHomepage h) {
    return Container(
      decoration: const BoxDecoration(
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      padding: const EdgeInsets.only(top: 14),
      child: Row(
        children: [
          _buildStatItem('${h.onSaleCount}', '在售'),
          _buildStatItem('${h.soldCount}', '已售'),
          _buildStatItem(
              h.totalReceived > 0 ? '${h.goodRate}%' : '—', '好评率'),
          _buildStatItem(
              h.totalReceived > 0 ? h.averageScore.toStringAsFixed(1) : '—',
              '综合评分'),
        ],
      ),
    );
  }

  Widget _buildStatItem(String value, String label) {
    return Expanded(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            value,
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w700,
              letterSpacing: -0.4,
              color: AppColors.fg,
            ),
          ),
          const SizedBox(height: 2),
          Text(label,
              style: const TextStyle(fontSize: 12, color: AppColors.muted)),
        ],
      ),
    );
  }

  // ── Goods Section ──

  Widget _buildGoodsSectionTitle() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Row(
        children: [
          Container(
            width: 3,
            height: 15,
            decoration: BoxDecoration(
              color: AppColors.accent,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 8),
          const Text('TA的在售商品', style: AppTextStyles.sectionTitle),
          const SizedBox(width: 8),
          if (_total > 0)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 1),
              decoration: BoxDecoration(
                color: AppColors.border,
                borderRadius: BorderRadius.circular(AppColors.radiusPill),
              ),
              child: Text('$_total',
                  style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.muted)),
            ),
        ],
      ),
    );
  }

  Widget _buildGoodsEmpty() {
    return const Padding(
      padding: EdgeInsets.only(top: 40),
      child: EmptyState(message: '暂无在售商品'),
    );
  }

  Widget _buildGoodsCard(TradeGoods goods) {
    return ProductCard(
      goods: goods,
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
