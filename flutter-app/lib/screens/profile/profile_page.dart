import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/auth_models.dart';
import '../../data/services/auth_api.dart';
import '../../data/services/chat_api.dart';
import '../../data/services/user_api.dart';
import '../../data/services/session.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/logout_button.dart';
import '../../common/utils/toast_utils.dart';
import '../auth/login_page.dart';
import '../favorite/favorite_list_page.dart';
import '../goods/my_goods_page.dart';
import '../messages/chat_page.dart';
import '../order/my_buy_orders_page.dart';
import '../order/my_sell_orders_page.dart';
import 'announcement_list_page.dart';
import 'dispute_list_page.dart';
import 'report_list_page.dart';
import 'review_list_page.dart';
import 'settings_page.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => ProfilePageState();
}

class ProfilePageState extends State<ProfilePage> {
  final _userApi = UserApi.instance;

  bool _loading = true;
  StudentUser? _profile;
  int _goodsCount = 0;
  int _sellCount = 0;
  int _buyCount = 0;
  int _favoriteCount = 0;

  void refresh() {
    _loadData();
  }

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    if (!mounted) return;
    setState(() => _loading = true);

    try {
      _profile = await _userApi.getUserProfile();
    } catch (_) {
      _profile = null;
    }

    final stats = await Future.wait([
      _safeCount(_userApi.getMyGoodsCount()),
      _safeCount(_userApi.getMySellCount()),
      _safeCount(_userApi.getMyBuyCount()),
      _safeCount(_userApi.getMyFavoriteCount()),
    ]);

    if (!mounted) return;
    setState(() {
      _goodsCount = stats[0];
      _sellCount = stats[1];
      _buyCount = stats[2];
      _favoriteCount = stats[3];
      _loading = false;
    });
  }

  Future<int> _safeCount(Future<int> fn) async {
    try {
      return await fn;
    } catch (_) {
      return 0;
    }
  }

  Future<void> _contactAdmin() async {
    try {
      final admin = await ChatApi.instance.getAdminInfo();
      if (!mounted) return;
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ChatPage(
            peerId: admin.userId,
            peerName: admin.nickname,
            goodsId: 0,
            peerItem: '联系管理员',
            peerAvatar: admin.avatar,
          ),
        ),
      );
    } catch (e) {
      if (mounted) showToast(context, '获取管理员信息失败');
    }
  }

  Future<void> _handleLogout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出当前账号吗？'),
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
    AuthApi.instance.logout();
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginPage()),
      (_) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const SafeArea(
        child: Center(child: CircularProgressIndicator()),
      );
    }
    final user = _profile ?? Session.instance.currentUser;
    return SafeArea(
      child: ListView(
        children: [
          _buildProfileCard(user),
          _buildSectionGap(),
          _buildMenuSection([
            _MenuItem(
              icon: Icons.inventory_2_outlined,
              color: AppColors.accent,
              label: '我的发布',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const MyGoodsPage()),
                );
                _loadData();
              },
            ),
            _MenuItem(
              icon: Icons.shopping_cart_outlined,
              color: AppColors.success,
              label: '我买到的',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const MyBuyOrdersPage()),
                );
                _loadData();
              },
            ),
            _MenuItem(
              icon: Icons.storefront_outlined,
              color: const Color(0xFF2196F3),
              label: '我卖出的',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const MySellOrdersPage()),
                );
                _loadData();
              },
            ),
            _MenuItem(
              icon: Icons.bookmark_outline,
              color: const Color(0xFFFFB800),
              label: '我的收藏',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const FavoriteListPage()),
                );
                _loadData();
              },
            ),
          ]),
          _buildSectionGap(),
          _buildMenuSection([
            _MenuItem(
              icon: Icons.gavel_outlined,
              color: AppColors.muted,
              label: '争议仲裁',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const DisputeListPage()),
                );
                _loadData();
              },
            ),
            _MenuItem(
              icon: Icons.flag_outlined,
              color: AppColors.price,
              label: '举报记录',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const ReportListPage()),
                );
                _loadData();
              },
            ),
            _MenuItem(
              icon: Icons.star_outline,
              color: const Color(0xFFFFB800),
              label: '我的评价',
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const ReviewListPage()),
                );
                _loadData();
              },
            ),
          ]),
          _buildSectionGap(),
          _buildMenuSection([
            _MenuItem(
              icon: Icons.campaign_outlined,
              color: AppColors.success,
              label: '系统公告',
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const AnnouncementListPage()),
              ),
            ),
            _MenuItem(
              icon: Icons.headset_mic_outlined,
              color: AppColors.accent,
              label: '联系管理员',
              onTap: _contactAdmin,
            ),
          ]),
          const SizedBox(height: 12),
          LogoutButton(onPressed: _handleLogout),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  // ── Profile Card ──

  Widget _buildProfileCard(StudentUser? user) {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.fromLTRB(20, 28, 20, 20),
      child: Stack(
        children: [
          Column(
            children: [
              _buildAvatar(user?.avatar),
              const SizedBox(height: 10),
              Text(
                user?.nickname ?? '校园用户',
                style: AppTextStyles.pageTitle,
              ),
              const SizedBox(height: 2),
              Text(
                user?.studentNo ?? '**********',
                style: AppTextStyles.muted,
              ),
              if (user != null) ...[
                const SizedBox(height: 4),
                _buildCreditBadge(user.creditScore),
              ],
              const SizedBox(height: 4),
              _buildStatsRow(),
            ],
          ),
          Positioned(
            top: -14,
            right: -4,
            child: IconButton(
              onPressed: () async {
                    await Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const SettingsPage()),
                    );
                    _loadData();
                  },
              icon: const Icon(Icons.settings_outlined, size: 20),
              color: AppColors.muted,
            ),
          ),
        ],
      ),
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

  Widget _buildAvatar(String? avatarUrl) {
    return Container(
      width: 72,
      height: 72,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: avatarUrl != null && avatarUrl.isNotEmpty
            ? null
            : const LinearGradient(
                colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
        border: Border.all(color: AppColors.surface, width: 3),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFFC47A38).withValues(alpha: 0.2),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      clipBehavior: Clip.antiAlias,
      child: _buildAvatarContent(avatarUrl),
    );
  }

  Widget _buildAvatarContent(String? avatarUrl) {
    if (avatarUrl == null || avatarUrl.isEmpty) {
      return const Icon(Icons.person, size: 36, color: Colors.white);
    }
    final fullUrl = avatarUrl.startsWith('http')
        ? avatarUrl
        : '${Session.baseUrl}$avatarUrl';
    return CorsImage(
      src: fullUrl,
      fit: BoxFit.cover,
      borderRadius: BorderRadius.circular(72),
      errorBuilder: (_, __, ___) =>
          const Icon(Icons.person, size: 36, color: Colors.white),
    );
  }

  // ── Stats Row ──

  Widget _buildStatsRow() {
    return Padding(
      padding: const EdgeInsets.only(top: 18),
      child: Container(
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: AppColors.border)),
        ),
        padding: const EdgeInsets.only(top: 14),
        child: Row(
          children: [
            _buildStatItem('$_goodsCount', '发布', onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const MyGoodsPage()),
              );
              _loadData();
            }),
            _buildStatItem('$_sellCount', '已售', onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const MyGoodsPage(initialTab: 1)),
              );
              _loadData();
            }),
            _buildStatItem('$_buyCount', '已买', onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const MyBuyOrdersPage()),
              );
              _loadData();
            }),
            _buildStatItem('$_favoriteCount', '收藏', onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const FavoriteListPage()),
              );
              _loadData();
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildStatItem(String num, String label, {VoidCallback? onTap}) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        behavior: HitTestBehavior.opaque,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              num,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w700,
                letterSpacing: -0.4,
                color: AppColors.fg,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              label,
              style: const TextStyle(fontSize: 12, color: AppColors.muted),
            ),
          ],
        ),
      ),
    );
  }

  // ── Section Gap ──

  Widget _buildSectionGap() {
    return AppSpacing.sectionGap;
  }

  // ── Menu Section ──

  Widget _buildMenuSection(List<_MenuItem> items) {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        children: items.asMap().entries.map((entry) {
          final isLast = entry.key == items.length - 1;
          return _buildMenuItem(entry.value, isLast: isLast);
        }).toList(),
      ),
    );
  }

  Widget _buildMenuItem(_MenuItem item, {bool isLast = false}) {
    return GestureDetector(
      onTap: item.onTap,
      behavior: HitTestBehavior.opaque,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        decoration: BoxDecoration(
          border: isLast
              ? null
              : const Border(bottom: BorderSide(color: AppColors.border)),
        ),
        child: Row(
          children: [
            SizedBox(
              width: 36,
              height: 36,
              child: Icon(item.icon, size: 24, color: item.color),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                item.label,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                  color: AppColors.fg,
                ),
              ),
            ),
            const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

}

class _MenuItem {
  final IconData icon;
  final Color color;
  final String label;
  final VoidCallback onTap;

  _MenuItem({
    required this.icon,
    required this.color,
    required this.label,
    required this.onTap,
  });
}
