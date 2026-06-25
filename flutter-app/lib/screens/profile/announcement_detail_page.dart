import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_announcement.dart';
import '../../data/services/announcement_api.dart';
import '../../data/services/session.dart';
import '../../widgets/cors_image.dart';

class AnnouncementDetailPage extends StatefulWidget {
  final int announcementId;

  const AnnouncementDetailPage({super.key, required this.announcementId});

  @override
  State<AnnouncementDetailPage> createState() => _AnnouncementDetailPageState();
}

class _AnnouncementDetailPageState extends State<AnnouncementDetailPage> {
  final _api = AnnouncementApi.instance;
  TradeAnnouncement? _data;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    if (!mounted) return;
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _data = await _api.getNoticeDetail(widget.announcementId);
      if (mounted) setState(() => _loading = false);
    } catch (e) {
      if (mounted) {
        setState(() {
          _loading = false;
          _error = '加载失败，请重试';
        });
      }
    }
  }

  /// 后端内容为富文本/HTML，移动端简单去标签后按纯文本展示
  String _plainText(String? html) {
    if (html == null || html.isEmpty) return '';
    return html
        .replaceAll(RegExp(r'<br\s*/?>', caseSensitive: false), '\n')
        .replaceAll(RegExp(r'</p>', caseSensitive: false), '\n')
        .replaceAll(RegExp(r'<[^>]+>'), '')
        .replaceAll('&nbsp;', ' ')
        .replaceAll('&lt;', '<')
        .replaceAll('&gt;', '>')
        .replaceAll('&amp;', '&')
        .replaceAll(RegExp(r'\n{3,}'), '\n\n')
        .trim();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('公告详情'),
        centerTitle: true,
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
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
    if (_error != null || _data == null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: AppColors.muted),
            const SizedBox(height: 12),
            Text(_error ?? '公告不存在',
                style: const TextStyle(color: AppColors.muted)),
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

    final item = _data!;
    final content = _plainText(item.content);
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text(
          item.title,
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: AppColors.fg,
            height: 1.4,
          ),
        ),
        const SizedBox(height: 10),
        Row(
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: AppColors.accent.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                item.typeLabel,
                style: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                  color: AppColors.accent,
                ),
              ),
            ),
            const SizedBox(width: 8),
            if (item.displayTime.isNotEmpty)
              Text(
                item.displayTime,
                style: const TextStyle(fontSize: 12, color: AppColors.muted),
              ),
          ],
        ),
        if (item.coverImage != null && item.coverImage!.isNotEmpty) ...[
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: CorsImage(
              src: _fullUrl(item.coverImage!),
              width: double.infinity,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const SizedBox.shrink(),
            ),
          ),
        ],
        const SizedBox(height: 16),
        const Divider(height: 1, color: AppColors.border),
        const SizedBox(height: 16),
        Text(
          content.isEmpty ? '暂无内容' : content,
          style: const TextStyle(
            fontSize: 15,
            color: AppColors.fg,
            height: 1.8,
          ),
        ),
      ],
    );
  }

  String _fullUrl(String url) =>
      url.startsWith('http') ? url : '${Session.baseUrl}$url';
}
