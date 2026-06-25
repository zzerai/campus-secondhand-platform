import 'dart:async';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/image_utils.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/user_nav.dart';
import '../../data/models/chat_models.dart';
import '../../data/services/chat_api.dart';
import '../../data/services/session.dart';
import '../../data/services/upload_api.dart';
import '../../widgets/cors_image.dart';
import '../goods/product_detail_page.dart';
import '../goods/report_page.dart';
import '../profile/dispute_submit_page.dart';

class ChatPage extends StatefulWidget {
  final int peerId;
  final String peerName;
  final int goodsId;
  final String peerItem;
  final String? peerAvatar;
  final String? goodsCoverImage;
  final int? orderId;
  final String? goodsStatus;

  const ChatPage({
    super.key,
    required this.peerId,
    required this.peerName,
    required this.goodsId,
    required this.peerItem,
    this.peerAvatar,
    this.goodsCoverImage,
    this.orderId,
    this.goodsStatus,
  });

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _chatApi = ChatApi.instance;
  final _uploadApi = UploadApi.instance;
  final ImagePicker _imagePicker = ImagePicker();
  final ScrollController _scrollCtrl = ScrollController();
  final TextEditingController _textCtrl = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  bool _sendActive = false;

  List<AppMessageVo> _messages = [];
  bool? _peerOnline;
  bool _loading = true;
  Timer? _pollTimer;

  static const List<String> _quickReplies = [
    '还在吗？',
    '能便宜点吗',
    '在哪里交易',
    '今天可以看实物吗',
    '好的',
  ];

  @override
  void initState() {
    super.initState();
    _textCtrl.addListener(_onTextChanged);
    _loadMessages();
    _markRead();
    _pollTimer = Timer.periodic(const Duration(seconds: 5), (_) => _pollMessages());
  }

  void _markRead() async {
    try {
      await _chatApi.markRead(peerId: widget.peerId, goodsId: widget.goodsId);
    } catch (_) {}
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    _textCtrl.removeListener(_onTextChanged);
    _textCtrl.dispose();
    _focusNode.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  void _showMoreActions() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (ctx) => Container(
        decoration: const BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
        ),
        child: SafeArea(
          top: false,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 36,
                height: 4,
                margin: const EdgeInsets.only(top: 10, bottom: 6),
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              ListTile(
                leading: const Icon(Icons.gavel_outlined, color: AppColors.fg),
                title: const Text('申请仲裁'),
                subtitle: const Text('对该订单发起争议仲裁', style: TextStyle(fontSize: 12, color: AppColors.muted)),
                onTap: () {
                  Navigator.of(ctx).pop();
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => DisputeSubmitPage(
                        orderId: widget.orderId,
                        goodsId: widget.goodsId,
                        goodsTitle: widget.peerItem,
                        sellerName: widget.peerName,
                        sellerAvatar: widget.peerAvatar,
                      ),
                    ),
                  );
                },
              ),
              const Divider(height: 1),
              ListTile(
                leading: const Icon(Icons.flag_outlined, color: AppColors.price),
                title: const Text('举报'),
                subtitle: const Text('举报此用户或商品', style: TextStyle(fontSize: 12, color: AppColors.muted)),
                onTap: () {
                  Navigator.of(ctx).pop();
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => ReportPage(
                        goodsId: widget.goodsId,
                        goodsTitle: widget.peerItem,
                        sellerName: widget.peerName,
                        sellerAvatar: widget.peerAvatar,
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }

  void _onTextChanged() {
    final active = _textCtrl.text.trim().isNotEmpty;
    if (active != _sendActive) setState(() => _sendActive = active);
  }

  void _scrollToBottom() {
    Future.delayed(const Duration(milliseconds: 100), () {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(_scrollCtrl.position.maxScrollExtent,
            duration: const Duration(milliseconds: 200),
            curve: Curves.easeOut);
      }
    });
  }

  Future<void> _loadMessages() async {
    try {
      final result = await _chatApi.getMessages(
        peerId: widget.peerId,
        goodsId: widget.goodsId,
      );
      if (mounted) {
        setState(() {
          _messages = result.rows.reversed.toList();
          _peerOnline = _extractPeerOnline(result.rows);
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// 会话内每条消息的 peerOnline 相同，取任意一条即可；无消息时返回 null（状态未知）。
  bool? _extractPeerOnline(List<AppMessageVo> rows) {
    return rows.isEmpty ? _peerOnline : rows.first.peerOnline;
  }

  Future<void> _pollMessages() async {
    try {
      final result = await _chatApi.getMessages(
        peerId: widget.peerId,
        goodsId: widget.goodsId,
      );
      if (!mounted) return;
      final newMessages = result.rows.reversed.toList();
      final newPeerOnline = _extractPeerOnline(result.rows);
      if (newMessages.length != _messages.length || newPeerOnline != _peerOnline) {
        setState(() {
          _messages = newMessages;
          _peerOnline = newPeerOnline;
        });
      }
    } catch (_) {}
  }

  Future<void> _sendMessage(String text) async {
    final content = text.trim();
    if (content.isEmpty) return;
    _textCtrl.clear();

    try {
      await _chatApi.sendMessage(
        receiverId: widget.peerId,
        goodsId: widget.goodsId,
        orderId: widget.orderId,
        content: content,
      );
      _loadMessages();
      _scrollToBottom();
    } catch (e) {
      if (mounted) showToast(context, '发送失败，请重试');
    }
  }

  Future<void> _pickAndSendImage() async {
    try {
      final picked = await _imagePicker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 85,
        maxWidth: 1920,
      );
      if (picked == null) return;
      if (!mounted) return;

      showToast(context, '上传中...');
      final result = await _uploadApi.uploadChatImage(picked);
      if (!mounted) return;

      if (result == null || result['fileName'] == null) {
        showToast(context, '上传失败，请重试');
        return;
      }
      await _chatApi.sendMessage(
        receiverId: widget.peerId,
        goodsId: widget.goodsId,
        content: result['fileName']!,
      );
      _loadMessages();
      _scrollToBottom();
    } catch (e) {
      if (mounted) showToast(context, '发送图片失败，请重试');
    }
  }

  Future<void> _deleteMessage(AppMessageVo msg) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('删除消息'),
        content: const Text('确定删除这条消息吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('删除', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
    if (confirm != true || !mounted) return;

    try {
      final deleted = await _chatApi.deleteMessages(messageIds: [msg.messageId]);
      if (deleted > 0) {
        _loadMessages();
      } else {
        showToast(context, '删除失败');
      }
    } catch (e) {
      if (mounted) showToast(context, '删除失败，请重试');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: Column(
          children: [
            _buildTopBar(),
            Expanded(
              child: GestureDetector(
                onTap: () => _focusNode.unfocus(),
                child: _loading
                    ? const Center(
                        child: CircularProgressIndicator(color: AppColors.accent))
                    : ListView(
                        controller: _scrollCtrl,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        children: [
                          if (!_isAdminChat) _buildProductCard(),
                          _buildTimestamp(),
                          ..._messages.map(_buildBubble),
                        ],
                      ),
              ),
            ),
            _buildInputBar(),
          ],
        ),
      ),
    );
  }

  // ── Top Bar ──

  Widget _buildTopBar() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      child: Row(
        children: [
          _buildIconBtn(
            Icons.arrow_back_rounded,
            () => Navigator.of(context).pop(),
          ),
          const SizedBox(width: 10),
          GestureDetector(
            onTap: () => openUserProfile(context, widget.peerId),
            child: _buildAvatar(36, const [Color(0xFFF43370), Color(0xFFFF6B99)],
                avatarUrl: widget.peerAvatar),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: GestureDetector(
              onTap: () => openUserProfile(context, widget.peerId),
              behavior: HitTestBehavior.opaque,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(_isAdminChat ? '联系管理员' : widget.peerName,
                      style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                          color: AppColors.fg)),
                  if (!_isAdminChat) _buildPresenceRow(),
                ],
              ),
            ),
          ),
          if (!_isAdminChat)
            _buildIconBtn(Icons.more_horiz, () => _showMoreActions()),
        ],
      ),
    );
  }

  Widget _buildPresenceRow() {
    final online = _peerOnline == true;
    final color = online ? AppColors.success : AppColors.muted;
    return Row(
      children: [
        _OnlineDot(color: color),
        const SizedBox(width: 4),
        Text(online ? '在线' : '离线',
            style: TextStyle(fontSize: 11, color: color)),
      ],
    );
  }

  Widget _buildAvatar(double size, List<Color> gradient,
      {String? avatarUrl}) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: avatarUrl != null && avatarUrl.isNotEmpty
            ? null
            : LinearGradient(
                colors: gradient,
                begin: Alignment.topLeft,
                end: Alignment.bottomRight),
      ),
      clipBehavior: Clip.antiAlias,
      child: avatarUrl != null && avatarUrl.isNotEmpty
          ? CorsImage(
              src: ensureAbsoluteUrl(avatarUrl),
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Icon(Icons.person,
                  size: size * 0.5,
                  color: Colors.white.withValues(alpha: 0.9)),
            )
          : Icon(Icons.person,
              size: size * 0.5,
              color: Colors.white.withValues(alpha: 0.9)),
    );
  }

  Widget _buildIconBtn(IconData icon, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 36,
        height: 36,
        alignment: Alignment.center,
        child: Icon(icon, size: 20, color: AppColors.fg),
      ),
    );
  }

  // ── Product Card ──

  bool get _isAdminChat => widget.goodsId == 0;

  bool get _goodsUnavailable =>
      widget.goodsStatus != null && widget.goodsStatus != '1';

  String get _goodsStatusLabel {
    switch (widget.goodsStatus) {
      case '4':
        return '已售出';
      case '3':
        return '已下架';
      case '2':
        return '审核拒绝';
      case '0':
        return '审核中';
      default:
        return '在售中';
    }
  }

  Widget _buildProductCard() {
    final unavailable = _goodsUnavailable;
    return GestureDetector(
      onTap: () {
        if (unavailable) {
          showToast(context, '商品$_goodsStatusLabel');
          return;
        }
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => ProductDetailPage(goodsId: widget.goodsId),
          ),
        );
      },
      child: Container(
        margin: const EdgeInsets.fromLTRB(16, 0, 16, 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(AppColors.radiusMd),
          boxShadow: [
            BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 8,
                offset: const Offset(0, 2)),
          ],
        ),
        child: Row(
          children: [
            ClipRRect(
              borderRadius:
                  const BorderRadius.all(Radius.circular(AppColors.radiusSm)),
              child: SizedBox(
                width: 64,
                height: 64,
                child: widget.goodsCoverImage != null &&
                        widget.goodsCoverImage!.isNotEmpty
                    ? CorsImage(
                        src: ensureAbsoluteUrl(widget.goodsCoverImage!),
                        fit: BoxFit.cover,
                        onTap: () => Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) =>
                                ProductDetailPage(goodsId: widget.goodsId),
                          ),
                        ),
                        errorBuilder: (_, __, ___) => Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                                colors: [
                                  Color(0xFFF0E2F5),
                                  Color(0xFFE8D5F0)
                                ],
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight),
                          ),
                          child: const Icon(Icons.desktop_mac_outlined,
                              size: 28, color: AppColors.muted),
                        ),
                      )
                    : Container(
                        decoration: const BoxDecoration(
                          gradient: LinearGradient(
                              colors: [Color(0xFFF0E2F5), Color(0xFFE8D5F0)],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight),
                        ),
                        child: const Icon(Icons.desktop_mac_outlined,
                            size: 28, color: AppColors.muted),
                      ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(widget.peerItem,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: AppColors.fg)),
                  const SizedBox(height: 8),
                  _StatusBadge(label: _goodsStatusLabel),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Timestamp ──

  Widget _buildTimestamp() {
    return Center(
      child: Container(
        margin: const EdgeInsets.only(top: 4, bottom: 12),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 3),
        decoration: BoxDecoration(
          color: const Color(0xFFF6F0F8),
          borderRadius: BorderRadius.circular(AppColors.radiusPill),
        ),
        child: const Text('聊天记录',
            style: TextStyle(fontSize: 11, color: AppColors.muted)),
      ),
    );
  }

  // ── Chat Bubble ──

  bool _isImageContent(String content) {
    if (content.startsWith('/profile/') && content.contains('.')) return true;
    if ((content.startsWith('http://') || content.startsWith('https://')) &&
        RegExp(r'\.(jpg|jpeg|png|gif|webp|bmp)(\?|$)', caseSensitive: false).hasMatch(content)) {
      return true;
    }
    return false;
  }

  String _displayImageUrl(String content) {
    if (content.startsWith('/profile/')) {
      return '${Session.baseUrl}$content';
    }
    return content;
  }

  Widget _buildBubble(AppMessageVo msg) {
    final isMe = msg.mine;
    final isImage = _isImageContent(msg.content);
    final bubble = Container(
      constraints: BoxConstraints(maxWidth: isImage ? 200 : 280),
      padding: isImage
          ? const EdgeInsets.all(4)
          : const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: isMe ? AppColors.accent : AppColors.surface,
        borderRadius: BorderRadius.only(
          topLeft: const Radius.circular(16),
          topRight: const Radius.circular(16),
          bottomLeft: Radius.circular(isMe ? 16 : 4),
          bottomRight: Radius.circular(isMe ? 4 : 16),
        ),
        boxShadow: isMe
            ? null
            : [
                BoxShadow(
                    color: Colors.black.withValues(alpha: 0.04),
                    blurRadius: 3,
                    offset: const Offset(0, 1))
              ],
      ),
      child: isImage
          ? ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: CorsImage(
                src: ensureAbsoluteUrl(_displayImageUrl(msg.content)),
                fit: BoxFit.cover,
                width: 192,
                height: 192,
              ),
            )
          : Text(msg.content,
              style: TextStyle(
                  fontSize: 14,
                  height: 1.55,
                  color: isMe ? Colors.white : AppColors.fg)),
    );

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        mainAxisAlignment: isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isMe) ...[
            GestureDetector(
              onTap: () => openUserProfile(context, widget.peerId),
              child: _buildAvatar(30, const [Color(0xFFF43370), Color(0xFFFF6B99)],
                  avatarUrl: widget.peerAvatar),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Column(
              crossAxisAlignment:
                  isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
              children: [
                isMe
                    ? GestureDetector(
                        onLongPress: () => _deleteMessage(msg),
                        child: bubble,
                      )
                    : bubble,
                const SizedBox(height: 4),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(_formatTime(msg.createTime),
                        style: TextStyle(
                            fontSize: 11,
                            color: isMe ? const Color(0xFFD08090) : AppColors.muted)),
                    if (isMe && msg.readStatus != null) ...[
                      const SizedBox(width: 4),
                      Text(
                        msg.readStatus == '1' ? '已读' : '未读',
                        style: TextStyle(
                          fontSize: 11,
                          color: msg.readStatus == '1'
                              ? AppColors.muted
                              : const Color(0xFFD08090),
                        ),
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),
          if (isMe) ...[
            const SizedBox(width: 8),
            _buildAvatar(30, const [Color(0xFFFF5722), Color(0xFFFF7A45)],
                avatarUrl: Session.instance.currentUser?.avatar),
          ],
        ],
      ),
    );
  }

  String _formatTime(String? time) {
    if (time == null || time.isEmpty) return '';
    try {
      final dt = DateTime.parse(time);
      final now = DateTime.now();
      final today = DateTime(now.year, now.month, now.day);
      final msgDay = DateTime(dt.year, dt.month, dt.day);
      final hm =
          '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
      if (msgDay == today) return hm;
      if (msgDay == today.subtract(const Duration(days: 1))) return '昨天 $hm';
      if (dt.year == now.year) {
        return '${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')} $hm';
      }
      return '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')} $hm';
    } catch (_) {
      return time;
    }
  }

  // ── Input Bar ──

  Widget _buildInputBar() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (!_isAdminChat) ...[
            SizedBox(
              height: 32,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: _quickReplies.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, index) {
                  return GestureDetector(
                    onTap: () => _sendMessage(_quickReplies[index]),
                    child: Container(
                      padding:
                          const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                      decoration: BoxDecoration(
                        border: Border.all(color: AppColors.border),
                        borderRadius:
                            BorderRadius.circular(AppColors.radiusPill),
                      ),
                      child: Text(_quickReplies[index],
                          style:
                              const TextStyle(fontSize: 12, color: AppColors.fg)),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 8),
          ],
          Row(
            children: [
            Expanded(
                child: Container(
                  height: 38,
                  margin: const EdgeInsets.symmetric(horizontal: 8),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFAF6FC),
                    borderRadius:
                        BorderRadius.circular(AppColors.radiusPill),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: TextField(
                    controller: _textCtrl,
                    focusNode: _focusNode,
                    onSubmitted: (text) {
                      if (text.trim().isNotEmpty) _sendMessage(text.trim());
                    },
                    style: const TextStyle(
                        fontSize: 14, color: AppColors.fg),
                    decoration: const InputDecoration(
                      contentPadding:
                          EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      border: InputBorder.none,
                      hintText: '输入消息...',
                      hintStyle:
                          TextStyle(fontSize: 14, color: AppColors.muted),
                    ),
                  ),
                ),
              ),
              _buildInputBtn(
                  Icons.image_outlined, () => _pickAndSendImage()),
              GestureDetector(
                onTap: () {
                  if (_textCtrl.text.trim().isNotEmpty) {
                    _sendMessage(_textCtrl.text.trim());
                  }
                },
                child: Container(
                  width: 38,
                  height: 38,
                  decoration: BoxDecoration(
                    color: _sendActive ? AppColors.accent : AppColors.bg,
                    shape: BoxShape.circle,
                    boxShadow: _sendActive
                        ? [
                            BoxShadow(
                                color:
                                    const Color(0xFFFF5722).withValues(alpha: 0.3),
                                blurRadius: 8,
                                offset: const Offset(0, 2))
                          ]
                        : null,
                  ),
                  child: Icon(Icons.send_rounded,
                      size: 20,
                      color: _sendActive ? Colors.white : AppColors.muted),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInputBtn(IconData icon, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 36,
        height: 36,
        child: Icon(icon, size: 20, color: AppColors.muted),
      ),
    );
  }
}

// ── Small Widgets ──

class _OnlineDot extends StatelessWidget {
  final Color color;
  const _OnlineDot({required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 6,
      height: 6,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final String label;
  const _StatusBadge({required this.label});

  Color get _fg {
    switch (label) {
      case '在售中':
        return AppColors.success;
      case '审核中':
        return const Color(0xFFE89000);
      case '审核拒绝':
        return const Color(0xFFE02424);
      default:
        return AppColors.muted;
    }
  }

  Color get _bgStart {
    switch (label) {
      case '在售中':
        return const Color(0xFFE6F9F3);
      case '审核中':
        return const Color(0xFFFFE8E0);
      case '审核拒绝':
        return const Color(0xFFFFE0E5);
      default:
        return const Color(0xFFF2EDF7);
    }
  }

  Color get _bgEnd {
    switch (label) {
      case '在售中':
        return const Color(0xFFE6F9F1);
      case '审核中':
        return const Color(0xFFFFE0D0);
      case '审核拒绝':
        return const Color(0xFFFFE0E0);
      default:
        return const Color(0xFFE8E2F0);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(
        gradient: LinearGradient(
            colors: [_bgStart, _bgEnd],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight),
        borderRadius:
            const BorderRadius.all(Radius.circular(AppColors.radiusPill)),
      ),
      child: Text(label,
          style: TextStyle(
              fontSize: 12, fontWeight: FontWeight.w500, color: _fg)),
    );
  }
}
