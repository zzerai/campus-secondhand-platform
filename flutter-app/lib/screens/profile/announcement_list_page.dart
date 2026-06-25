import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/app_version.dart';
import '../../data/models/trade_announcement.dart';
import '../../data/services/announcement_api.dart';
import '../../data/services/session.dart';
import '../../data/services/version_api.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/update_dialog.dart';
import 'announcement_detail_page.dart';

class AnnouncementListPage extends StatefulWidget {
  const AnnouncementListPage({super.key});

  @override
  State<AnnouncementListPage> createState() => _AnnouncementListPageState();
}

class _AnnouncementListPageState extends State<AnnouncementListPage> {
  final _api = AnnouncementApi.instance;
  List<TradeAnnouncement> _list = [];
  bool _loading = true;
  String? _error;
  AppVersion? _latestVersion;
  int _currentCode = 0;

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
      final results = await Future.wait([
        _api.getNoticeList(),
        _fetchVersion(),
      ]);
      _list = results[0] as List<TradeAnnouncement>;
    } catch (e) {
      try {
        _list = await _api.getNoticeList();
      } catch (_) {
        if (mounted) {
          setState(() {
            _loading = false;
            _error = '加载失败，请重试';
          });
          return;
        }
      }
    }
    if (mounted) setState(() => _loading = false);
  }

  Future<void> _fetchVersion() async {
    try {
      final info = await PackageInfo.fromPlatform();
      _currentCode = int.tryParse(info.buildNumber) ?? 0;
      final latest = await VersionApi.instance.getLatest();
      if (latest != null && latest.versionCode > _currentCode) {
        _latestVersion = latest;
      }
    } catch (_) {}
  }

  void _showUpdateDialog() {
    if (_latestVersion == null) return;
    showDialog(
      context: context,
      barrierDismissible: !_latestVersion!.forceUpdate,
      builder: (_) => UpdateDialog(version: _latestVersion!),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('系统公告'),
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
    if (_error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: AppColors.muted),
            const SizedBox(height: 12),
            Text(_error!, style: const TextStyle(color: AppColors.muted)),
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
      onRefresh: _loadData,
      color: AppColors.accent,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        itemCount: _list.length + (_latestVersion != null ? 1 : 0),
        itemBuilder: (context, index) {
          if (_latestVersion != null && index == 0) {
            return _buildVersionCard();
          }
          final itemIndex = _latestVersion != null ? index - 1 : index;
          return _buildCard(_list[itemIndex]);
        },
      ),
    );
  }

  Widget _buildVersionCard() {
    final v = _latestVersion!;
    final log = (v.updateLog ?? '').trim();
    return GestureDetector(
      onTap: _showUpdateDialog,
      behavior: HitTestBehavior.opaque,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: v.forceUpdate ? AppColors.price : AppColors.accent,
            width: 1.5,
          ),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: (v.forceUpdate
                        ? AppColors.price
                        : AppColors.accent)
                    .withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                Icons.system_update_rounded,
                size: 24,
                color: v.forceUpdate ? AppColors.price : AppColors.accent,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: (v.forceUpdate
                                  ? AppColors.price
                                  : AppColors.accent)
                              .withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          v.forceUpdate ? '强制更新' : '版本更新',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                            color: v.forceUpdate
                                ? AppColors.price
                                : AppColors.accent,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '新版本 ${v.versionName}',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: AppColors.fg,
                      height: 1.4,
                    ),
                  ),
                  if (log.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Text(
                      log,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontSize: 12, color: AppColors.muted, height: 1.4),
                    ),
                  ],
                ],
              ),
            ),
            const Padding(
              padding: EdgeInsets.only(left: 4, top: 24),
              child: Icon(Icons.download, size: 18, color: AppColors.muted),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCard(TradeAnnouncement item) {
    return GestureDetector(
      onTap: () => Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) =>
              AnnouncementDetailPage(announcementId: item.announcementId),
        ),
      ),
      behavior: HitTestBehavior.opaque,
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (item.coverImage != null && item.coverImage!.isNotEmpty) ...[
              ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: CorsImage(
                  src: _fullUrl(item.coverImage!),
                  width: 72,
                  height: 72,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => _coverPlaceholder(),
                ),
              ),
              const SizedBox(width: 12),
            ],
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      if (item.isTopped) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: const Color(0xFFFFF3E0),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            '置顶',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.w600,
                              color: Color(0xFFE65100),
                            ),
                          ),
                        ),
                        const SizedBox(width: 6),
                      ],
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 2),
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
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    item.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: AppColors.fg,
                      height: 1.4,
                    ),
                  ),
                  if (item.displayTime.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Text(
                      item.displayTime,
                      style:
                          const TextStyle(fontSize: 11, color: AppColors.muted),
                    ),
                  ],
                ],
              ),
            ),
            const Padding(
              padding: EdgeInsets.only(left: 4, top: 24),
              child:
                  Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
            ),
          ],
        ),
      ),
    );
  }

  Widget _coverPlaceholder() {
    return Container(
      width: 72,
      height: 72,
      color: AppColors.bg,
      child: Icon(Icons.campaign_outlined,
          size: 24, color: AppColors.muted.withValues(alpha: 0.4)),
    );
  }

  String _fullUrl(String url) =>
      url.startsWith('http') ? url : '${Session.baseUrl}$url';
}
