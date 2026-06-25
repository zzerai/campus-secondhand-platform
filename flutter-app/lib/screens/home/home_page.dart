import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/app_updater.dart';
import '../../data/services/chat_api.dart';
import '../goods/goods_publish_page.dart';
import 'home_tab.dart';
import '../profile/profile_page.dart';
import '../category/category_page.dart';
import '../messages/messages_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _currentTab = 0;
  String? _pendingCatId;
  int _unreadCount = 0;
  final _profileKey = GlobalKey<ProfilePageState>();
  final _homeKey = GlobalKey<HomeTabState>();
  final _messagesKey = GlobalKey<MessagesPageState>();
  final _chatApi = ChatApi.instance;

  @override
  void initState() {
    super.initState();
    _fetchUnreadCount();
    // 启动静默检测更新，已跳过则不再弹窗
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) AppUpdater.checkAndPrompt(context, silent: true);
    });
  }

  Future<void> _fetchUnreadCount() async {
    try {
      final count = await _chatApi.getUnreadCount();
      if (mounted) setState(() => _unreadCount = count);
    } catch (_) {}
  }

  void _onCategoryNavigate(String? catId) {
    _pendingCatId = catId;
    _switchTab(1);
  }

  void _switchTab(int index) {
    if (index == _currentTab) return;
    setState(() => _currentTab = index);
    if (index == 0) {
      _homeKey.currentState?.refresh();
    } else if (index == 2) {
      _messagesKey.currentState?.refresh();
      _fetchUnreadCount();
    } else if (index == 3) {
      _profileKey.currentState?.refresh();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentTab,
        children: [
          HomeTab(
            key: _homeKey,
            onMessagesTap: () => _switchTab(2),
            onCategoryNavigate: _onCategoryNavigate,
          ),
          CategoryPage(selectedCatId: _pendingCatId),
          MessagesPage(key: _messagesKey, onUnreadChanged: _fetchUnreadCount),
          ProfilePage(key: _profileKey),
          const SizedBox.shrink(),
        ],
      ),
      bottomNavigationBar: _buildBottomBar(),
    );
  }

  Widget _buildBottomBar() {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.surface,
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      padding: const EdgeInsets.only(top: 4, bottom: 2),
      child: Row(
        children: [
          _buildTab(Icons.home_outlined, Icons.home, '首页', 0),
          _buildTab(Icons.category_outlined, Icons.category, '分类', 1),
          Expanded(child: _buildPublishButton()),
          _buildTab(Icons.messenger_outline, Icons.messenger, '消息', 2),
          _buildTab(Icons.person_outline, Icons.person, '我的', 3),
        ],
      ),
    );
  }

  Widget _buildTab(
      IconData icon, IconData activeIcon, String label, int index) {
    final active = _currentTab == index;
    final showBadge = index == 2 && _unreadCount > 0;
    return Expanded(
      child: GestureDetector(
        onTap: () => _switchTab(index),
        behavior: HitTestBehavior.opaque,
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 6),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                width: 24,
                height: 24,
                child: Stack(
                  clipBehavior: Clip.none,
                  children: [
                    Icon(active ? activeIcon : icon,
                        size: 22,
                        color: active ? AppColors.accent : AppColors.muted),
                    if (showBadge)
                      Positioned(
                        top: -4,
                        right: -10,
                        child: Container(
                          constraints: const BoxConstraints(minWidth: 14),
                          height: 14,
                          padding: const EdgeInsets.symmetric(horizontal: 3),
                          decoration: const BoxDecoration(
                            color: AppColors.price,
                            borderRadius:
                                BorderRadius.all(Radius.circular(7)),
                          ),
                          alignment: Alignment.center,
                          child: Text(
                            _unreadCount > 99 ? '99+' : '$_unreadCount',
                            style: const TextStyle(
                              fontSize: 9,
                              fontWeight: FontWeight.w700,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 2),
              Text(label,
                  style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w500,
                      color: active ? AppColors.accent : AppColors.muted)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPublishButton() {
    return GestureDetector(
      onTap: () async {
        final result = await Navigator.of(context).push(
          MaterialPageRoute(builder: (_) => const GoodsPublishPage()),
        );
        if (result == true) {
          _homeKey.currentState?.refresh();
          _profileKey.currentState?.refresh();
        }
      },
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 44,
            height: 44,
            margin: const EdgeInsets.only(bottom: 2),
            decoration: BoxDecoration(
              color: AppColors.accent,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFFC47A38).withValues(alpha: 0.35),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: const Icon(Icons.add, color: Colors.white, size: 22),
          ),
          const Text('发布',
              style: TextStyle(fontSize: 10, color: AppColors.muted)),
        ],
      ),
    );
  }
}
