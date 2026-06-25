import 'package:flutter/material.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/image_utils.dart';
import '../../common/utils/toast_utils.dart';
import '../../data/models/chat_models.dart';
import '../../data/services/chat_api.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/image_placeholder.dart';
import '../../widgets/section_header.dart';
import 'chat_page.dart';
import 'conversation_data.dart';

class MessagesPage extends StatefulWidget {
  final VoidCallback? onUnreadChanged;

  const MessagesPage({super.key, this.onUnreadChanged});

  @override
  MessagesPageState createState() => MessagesPageState();
}

class MessagesPageState extends State<MessagesPage> {
  final _chatApi = ChatApi.instance;
  List<AppConversationVo> _conversations = [];
  bool _loading = true;
  bool _markingAllRead = false;

  @override
  void initState() {
    super.initState();
    _loadConversations();
  }

  void refresh() {
    _loadConversations();
  }

  Future<void> _loadConversations() async {
    try {
      final result = await _chatApi.getConversations();
      if (mounted) {
        setState(() {
          _conversations = result.rows;
          _loading = false;
        });
        widget.onUnreadChanged?.call();
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _markAllRead() async {
    if (_markingAllRead) return;
    setState(() => _markingAllRead = true);
    try {
      await _chatApi.markAllRead();
      if (mounted) {
        await _loadConversations();
        widget.onUnreadChanged?.call();
      }
    } catch (_) {
      if (mounted) showToast(context, '操作失败');
    } finally {
      if (mounted) setState(() => _markingAllRead = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Column(
        children: [
          _buildTopBar(context),
          Expanded(
            child: RefreshIndicator(
              color: AppColors.accent,
              onRefresh: _loadConversations,
              child: ListView(
                padding: EdgeInsets.zero,
                children: [
                  if (_loading)
                    const Padding(
                      padding: EdgeInsets.only(top: 60),
                      child: Center(
                          child: CircularProgressIndicator(
                              color: AppColors.accent)),
                    )
                  else if (_conversations.isNotEmpty) ...[
                    _buildSectionHeader('交易消息',
                        onReadAll: _markAllRead,
                        marking: _markingAllRead),
                    _buildConversationList(context),
                    const SizedBox(height: 20),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTopBar(BuildContext context) {
    return Container(
      color: AppColors.surface,
      padding: AppSpacing.headerPad,
      child: const Row(
        children: [
          Text('消息', style: AppTextStyles.pageTitle),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String label, {VoidCallback? onReadAll, bool marking = false}) {
    return SectionHeader(
      title: label,
      trailing: onReadAll != null ? (marking ? '处理中...' : '全部已读') : null,
      onTrailingTap: marking ? null : onReadAll,
      showChevron: false,
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      titleStyle: const TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.w600,
        color: AppColors.muted,
        letterSpacing: 0.4,
      ),
      trailingStyle: const TextStyle(fontSize: 12, color: AppColors.muted),
    );
  }

  String _displayLastContent(String? content) {
    if (content == null || content.isEmpty) return '';
    if (content.startsWith('/profile/') && content.contains('.')) return '[图片]';
    if ((content.startsWith('http://') || content.startsWith('https://')) &&
        RegExp(r'\.(jpg|jpeg|png|gif|webp|bmp)(\?|$)', caseSensitive: false).hasMatch(content)) {
      return '[图片]';
    }
    return content;
  }

  String _formatConversationTime(String? time) {
    if (time == null || time.isEmpty) return '';
    try {
      final dt = DateTime.parse(time);
      final now = DateTime.now();
      final today = DateTime(now.year, now.month, now.day);
      final msgDay = DateTime(dt.year, dt.month, dt.day);
      if (msgDay == today) {
        return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
      }
      if (msgDay == today.subtract(const Duration(days: 1))) {
        return '昨天';
      }
      if (dt.year == now.year) {
        return '${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
      }
      return '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
    } catch (_) {
      return time;
    }
  }

  // ── API-backed conversation list ──

  Widget _buildConversationList(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusLg),
      ),
      child: Column(
        children: _conversations.asMap().entries.map((entry) {
          final i = entry.key;
          final c = entry.value;
          final isLast = i == _conversations.length - 1;
          return Column(
            children: [
              _buildConvItem(context, c),
              if (!isLast)
                const Divider(height: 1, color: AppColors.border, indent: 76),
            ],
          );
        }).toList(),
      ),
    );
  }

  Widget _buildConvItem(BuildContext context, AppConversationVo c) {
    final gradient = avatarGradients[c.peerId % avatarGradients.length];
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () async {
        await Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ChatPage(
              peerId: c.peerId,
              peerName: c.peerNickname,
              goodsId: c.goodsId,
              peerItem: c.goodsTitle,
              peerAvatar: c.peerAvatar,
              goodsCoverImage: c.goodsCoverImage,
              orderId: c.orderId,
              goodsStatus: c.goodsStatus,
            ),
          ),
        );
        _loadConversations();
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: [
            _buildAvatar(c.peerAvatar, gradient, c.unreadCount,
                c.goodsCoverImage),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(c.peerNickname,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: AppTextStyles.cardTitle),
                  const SizedBox(height: 4),
                  Text(_displayLastContent(c.lastContent),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: AppTextStyles.muted),
                  const SizedBox(height: 4),
                  Text(_formatConversationTime(c.lastTime),
                      style: AppTextStyles.smallMuted),
                ],
              ),
            ),
            if (c.goodsCoverImage != null && c.goodsCoverImage!.isNotEmpty) ...[
              const SizedBox(width: 10),
              _ItemThumb(imageUrl: c.goodsCoverImage!),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildAvatar(String? avatarUrl, LinearGradient gradient, int unread,
      String? goodsImageUrl) {
    return SizedBox(
      width: 48,
      height: 48,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: gradient,
            ),
            clipBehavior: Clip.antiAlias,
            child: avatarUrl != null && avatarUrl.isNotEmpty
                ? CorsImage(
                    src: ensureAbsoluteUrl(avatarUrl),
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) =>
                        const Icon(Icons.person, color: Colors.white70, size: 24),
                  )
                : const Icon(Icons.person, color: Colors.white70, size: 24),
          ),
          if (unread > 0)
            Positioned(
              top: -2,
              right: -4,
              child: unread > 1
                  ? Container(
                      constraints: const BoxConstraints(minWidth: 16),
                      height: 16,
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      decoration: const BoxDecoration(
                        color: AppColors.price,
                        borderRadius: BorderRadius.all(Radius.circular(8)),
                      ),
                      alignment: Alignment.center,
                      child: Text('$unread',
                          style: const TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: Colors.white)),
                    )
                  : Container(
                      width: 8,
                      height: 8,
                      decoration: const BoxDecoration(
                        color: AppColors.price,
                        shape: BoxShape.circle,
                      ),
                    ),
            ),
          if (goodsImageUrl != null && goodsImageUrl.isNotEmpty)
            Positioned(
              bottom: -2,
              right: -2,
              child: Container(
                width: 18,
                height: 18,
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                        color: Colors.black.withValues(alpha: 0.1),
                        blurRadius: 4,
                        offset: const Offset(0, 2))
                  ],
                ),
                child: const Icon(Icons.inventory_2,
                    size: 12, color: AppColors.muted),
              ),
            ),
        ],
      ),
    );
  }
}

class _ItemThumb extends StatelessWidget {
  final String imageUrl;
  const _ItemThumb({required this.imageUrl});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(6),
      child: SizedBox(
        width: 56,
        height: 56,
        child: CorsImage(
          src: ensureAbsoluteUrl(imageUrl),
          fit: BoxFit.cover,
          errorBuilder: (_, __, ___) => const ImagePlaceholder(
              width: 56, height: 56, iconSize: 22),
        ),
      ),
    );
  }
}
