import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/image_utils.dart';
import '../../data/models/trade_report.dart';
import '../../data/services/report_api.dart';
import '../../widgets/cors_image.dart';
import '../../widgets/image_placeholder.dart';
import '../goods/product_detail_page.dart';

class ReportListPage extends StatefulWidget {
  const ReportListPage({super.key});

  @override
  State<ReportListPage> createState() => _ReportListPageState();
}

class _ReportListPageState extends State<ReportListPage> {
  final _reportApi = ReportApi.instance;
  List<TradeReport> _reports = [];
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
      _reports = await _reportApi.getMyReports();
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

  Color _statusColor(String? status) {
    switch (status) {
      case '0':
        return const Color(0xFFFFB800);
      case '1':
        return const Color(0xFF00A878);
      case '2':
        return AppColors.price;
      default:
        return AppColors.muted;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('举报记录'),
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
    if (_reports.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.flag_outlined, size: 48, color: AppColors.muted.withValues(alpha: 0.4)),
            const SizedBox(height: 12),
            const Text('暂无举报记录', style: TextStyle(color: AppColors.muted)),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _loadData,
      color: AppColors.accent,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        itemCount: _reports.length,
        itemBuilder: (context, index) => _buildReportCard(_reports[index]),
      ),
    );
  }

  Widget _buildGoodsCard(TradeReport report) {
    final hasGoods = report.goodsTitle != null;
    final imageUrl =
        (report.goodsCoverImage != null && report.goodsCoverImage!.isNotEmpty)
            ? ensureAbsoluteUrl(report.goodsCoverImage!)
            : null;
    // 商品仍在架（'1'）才允许跳详情，其余状态详情接口会返回"已下架"
    final canOpen = hasGoods && report.goodsStatus == '1';
    final unavailable = hasGoods && report.goodsStatus != '1';

    final content = Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: AppColors.bg,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: SizedBox(
              width: 56,
              height: 56,
              child: imageUrl != null
                  ? CorsImage(
                      src: imageUrl,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => const ImagePlaceholder())
                  : const ImagePlaceholder(),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  hasGoods ? report.goodsTitle! : '商品已删除',
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w500,
                    color: hasGoods ? AppColors.fg : AppColors.muted,
                    height: 1.4,
                  ),
                ),
                if (hasGoods && report.goodsPrice != null) ...[
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Text(
                        '¥${report.goodsPrice!.toStringAsFixed(0)}',
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w700,
                          color: AppColors.price,
                        ),
                      ),
                      if (unavailable) ...[
                        const SizedBox(width: 8),
                        Text(
                          report.goodsStatus == '4' ? '已售出' : '已下架',
                          style: const TextStyle(
                              fontSize: 11, color: AppColors.muted),
                        ),
                      ],
                    ],
                  ),
                ],
              ],
            ),
          ),
          if (canOpen)
            const Icon(Icons.chevron_right, size: 18, color: AppColors.muted),
        ],
      ),
    );

    if (!canOpen) return content;
    return GestureDetector(
      onTap: () => Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ProductDetailPage(goodsId: report.goodsId!),
        ),
      ),
      child: content,
    );
  }

  Widget _buildReportCard(TradeReport report) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: _statusColor(report.handleStatus).withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      report.statusLabel,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: _statusColor(report.handleStatus),
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Text(
                    report.reportType,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.fg,
                    ),
                  ),
                ],
              ),
              if (report.createTime != null)
                Text(
                  report.createTime!,
                  style: const TextStyle(fontSize: 11, color: AppColors.muted),
                ),
            ],
          ),
          if (report.goodsId != null) ...[
            const SizedBox(height: 10),
            _buildGoodsCard(report),
          ],
          if (report.reportContent != null && report.reportContent!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              report.reportContent!,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 13, color: AppColors.muted, height: 1.5),
            ),
          ],
          if (report.handleResult != null && report.handleResult!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: AppColors.bg,
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                '处理结果：${report.handleResult!}',
                style: const TextStyle(fontSize: 12, color: AppColors.fg, height: 1.4),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
