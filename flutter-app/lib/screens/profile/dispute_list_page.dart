import 'package:flutter/material.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_dispute.dart';
import '../../data/services/dispute_api.dart';
import '../../data/services/session.dart';

class DisputeListPage extends StatefulWidget {
  const DisputeListPage({super.key});

  @override
  State<DisputeListPage> createState() => _DisputeListPageState();
}

class _DisputeListPageState extends State<DisputeListPage> {
  final _disputeApi = DisputeApi.instance;
  List<TradeDispute> _disputes = [];
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
      _disputes = await _disputeApi.getMyDisputes();
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

  String _roleLabel(TradeDispute d) {
    final myId = Session.instance.currentUser?.userId;
    if (myId != null && d.applicantId != null && d.applicantId == myId) {
      return '我发起的';
    }
    return '对方发起的';
  }

  /// 从 handleResult 中解析处理结论，格式为 【结论】说明
  String? _parseDecision(String? handleResult) {
    if (handleResult == null || handleResult.isEmpty) return null;
    final match = RegExp(r'^【(.+?)】').firstMatch(handleResult);
    return match?.group(1);
  }

  /// 从 handleResult 中解析处理说明（【结论】之后的部分）
  String? _parseDetail(String? handleResult) {
    if (handleResult == null || handleResult.isEmpty) return null;
    final match = RegExp(r'^【.+?】\s*(.*)$', dotAll: true).firstMatch(handleResult);
    final detail = match?.group(1)?.trim();
    return (detail != null && detail.isNotEmpty) ? detail : null;
  }

  String _faultPartyLabel(String? faultParty) {
    switch (faultParty) {
      case 'respondent':
        return '被申诉人担责';
      case 'applicant':
        return '发起人担责';
      case 'both':
        return '双方担责';
      default:
        return '未判定';
    }
  }

  String _refundLabel(String? refundStatus, num? refundAmount) {
    if (refundStatus == null || refundStatus == '0') return '未退款';
    if (refundAmount != null && refundAmount > 0) {
      return '已退款 ¥${refundAmount.toStringAsFixed(2)}';
    }
    return '已退款';
  }

  Widget _infoRow(String label, String value, Color valueColor) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 56,
          child: Text(
            label,
            style: const TextStyle(fontSize: 12, color: AppColors.muted),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(fontSize: 12, color: valueColor, height: 1.4),
          ),
        ),
      ],
    );
  }

  Color _statusColor(String? status) {
    switch (status) {
      case '0':
        return const Color(0xFFFFB800);
      case '1':
        return const Color(0xFF2196F3);
      case '2':
        return const Color(0xFFFF9800);
      case '3':
        return const Color(0xFF00A878);
      default:
        return AppColors.muted;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('争议仲裁'),
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
    if (_disputes.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.gavel_outlined, size: 48, color: AppColors.muted.withValues(alpha: 0.4)),
            const SizedBox(height: 12),
            const Text('暂无争议记录', style: TextStyle(color: AppColors.muted)),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _loadData,
      color: AppColors.accent,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        itemCount: _disputes.length,
        itemBuilder: (context, index) => _buildCard(_disputes[index]),
      ),
    );
  }

  Widget _buildCard(TradeDispute d) {
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
                      color: _statusColor(d.handleStatus).withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      d.statusLabel,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: _statusColor(d.handleStatus),
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Text(
                    d.disputeType ?? '',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.fg,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    _roleLabel(d),
                    style: TextStyle(
                      fontSize: 11,
                      color: AppColors.muted.withValues(alpha: 0.7),
                    ),
                  ),
                ],
              ),
              if (d.orderCreateTime != null)
                Text(
                  d.orderCreateTime!,
                  style: const TextStyle(fontSize: 11, color: AppColors.muted),
                ),
            ],
          ),
          if (d.disputeContent != null && d.disputeContent!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              d.disputeContent!,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 13, color: AppColors.muted, height: 1.5),
            ),
          ],
          if (d.handleStatus == '3' && d.handleResult != null && d.handleResult!.isNotEmpty) ...[
            const SizedBox(height: 10),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: const Color(0xFFF0FDF6),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (_parseDecision(d.handleResult) case final decision?)
                    _infoRow('处理结论', decision, const Color(0xFF00A878)),
                  if (_parseDetail(d.handleResult) case final detail?)
                    Padding(
                      padding: const EdgeInsets.only(top: 6),
                      child: _infoRow('处理说明', detail, AppColors.fg),
                    ),
                  Padding(
                    padding: const EdgeInsets.only(top: 6),
                    child: _infoRow('责任判定', _faultPartyLabel(d.faultParty), AppColors.fg),
                  ),
                  Padding(
                    padding: const EdgeInsets.only(top: 6),
                    child: _infoRow('退款处理', _refundLabel(d.refundStatus, d.refundAmount),
                        (d.refundStatus != null && d.refundStatus != '0') ? const Color(0xFF00A878) : AppColors.fg),
                  ),
                ],
              ),
            ),
          ],
          if (d.orderNo != null) ...[
            const SizedBox(height: 8),
            Text(
              '订单编号：${d.orderNo}',
              style: const TextStyle(fontSize: 11, color: AppColors.muted),
            ),
          ],
        ],
      ),
    );
  }
}
