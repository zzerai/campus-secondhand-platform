import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../../common/constants/app_theme.dart';
import '../../common/utils/toast_utils.dart';
import '../../common/utils/image_utils.dart';
import '../../data/services/report_api.dart';
import '../../data/services/upload_api.dart';
import '../../widgets/cors_image.dart';

class ReportPage extends StatefulWidget {
  final int goodsId;
  final String goodsTitle;
  final String sellerName;
  final String? sellerAvatar;

  const ReportPage({
    super.key,
    required this.goodsId,
    required this.goodsTitle,
    required this.sellerName,
    this.sellerAvatar,
  });

  @override
  State<ReportPage> createState() => _ReportPageState();
}

class _ReportPageState extends State<ReportPage> {
  final _reportApi = ReportApi.instance;
  final _uploadApi = UploadApi.instance;
  final _imagePicker = ImagePicker();
  final _descCtrl = TextEditingController();
  final _contactCtrl = TextEditingController();
  final _reasons = const [
    '虚假商品',
    '欺诈行为',
    '违禁商品',
    '垃圾广告',
    '侵权冒用',
    '其他',
  ];

  String? _selectedReason;
  final List<File> _evidenceFiles = [];
  bool _submitting = false;
  bool _submitted = false;

  static const _maxChars = 500;
  static const _maxImages = 3;

  @override
  void dispose() {
    _descCtrl.dispose();
    _contactCtrl.dispose();
    super.dispose();
  }

  bool get _canSubmit =>
      _selectedReason != null && _descCtrl.text.trim().isNotEmpty;

  Future<void> _pickImage() async {
    if (_evidenceFiles.length >= _maxImages) {
      showToast(context, '最多上传$_maxImages张图片');
      return;
    }
    try {
      final picked = await _imagePicker.pickImage(
        source: ImageSource.gallery,
        imageQuality: 80,
      );
      if (picked != null && mounted) {
        setState(() => _evidenceFiles.add(File(picked.path)));
      }
    } catch (_) {}
  }

  void _removeImage(int index) {
    setState(() => _evidenceFiles.removeAt(index));
  }

  Future<void> _submit() async {
    if (!_canSubmit || _submitting) return;
    setState(() => _submitting = true);

    try {
      String? evidenceUrls;
      if (_evidenceFiles.isNotEmpty) {
        final urls = await _uploadApi.uploadImages(_evidenceFiles);
        if (urls.isNotEmpty) {
          evidenceUrls = urls.join(',');
        }
      }

      String content = _descCtrl.text.trim();
      if (_contactCtrl.text.trim().isNotEmpty) {
        content = '$content\n\n联系方式：${_contactCtrl.text.trim()}';
      }

      await _reportApi.submitReport(
        goodsId: widget.goodsId,
        reportType: _selectedReason!,
        reportContent: content,
        evidenceImages: evidenceUrls,
      );
      if (mounted) setState(() => _submitted = true);
    } catch (e) {
      if (mounted) {
        showToast(context, '提交失败，请重试');
        setState(() => _submitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_submitted) return _buildSuccess();
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(),
            Expanded(
              child: ListView(
                children: [
                  _buildTargetCard(),
                  _buildReasonSection(),
                  _buildDescSection(),
                  _buildEvidenceSection(),
                  _buildContactSection(),
                  _buildSubmitArea(),
                  _buildDisclaimer(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ── Header ──

  Widget _buildHeader() {
    return Container(
      color: AppColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      child: Row(
        children: [
          SizedBox(
            width: 36,
            height: 36,
            child: IconButton(
              onPressed: () => Navigator.of(context).pop(),
              icon: const Icon(Icons.arrow_back_rounded, size: 22),
              padding: EdgeInsets.zero,
              color: AppColors.fg,
            ),
          ),
          const SizedBox(width: 4),
          const Text(
            '举报',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w700,
              letterSpacing: -0.2,
              color: AppColors.fg,
            ),
          ),
        ],
      ),
    );
  }

  // ── Target Card ──

  Widget _buildTargetCard() {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Row(
        children: [
          _buildSellerAvatar(),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  widget.goodsTitle,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppColors.fg,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  '举报对象 · 卖家：${widget.sellerName}',
                  style: const TextStyle(fontSize: 12, color: AppColors.muted),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, size: 16, color: AppColors.muted),
        ],
      ),
    );
  }

  Widget _buildSellerAvatar() {
    final avatar = widget.sellerAvatar;
    final hasAvatar = avatar != null && avatar.isNotEmpty;
    return Container(
      width: 48,
      height: 48,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: hasAvatar
            ? null
            : const LinearGradient(
                colors: [Color(0xFFFF5722), Color(0xFFFF7A45)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
      ),
      clipBehavior: Clip.antiAlias,
      child: hasAvatar
          ? CorsImage(
              src: ensureAbsoluteUrl(avatar),
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const Icon(Icons.person,
                  size: 22, color: Colors.white),
            )
          : const Icon(Icons.person, size: 22, color: Colors.white),
    );
  }

  // ── Reason ──

  Widget _buildReasonSection() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 6),
            child: Text(
              '举报类型',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: AppColors.fg,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                mainAxisSpacing: 8,
                crossAxisSpacing: 8,
                childAspectRatio: 4.5,
              ),
              itemCount: _reasons.length,
              itemBuilder: (_, i) {
                final reason = _reasons[i];
                final selected = _selectedReason == reason;
                return GestureDetector(
                  onTap: () => setState(() => _selectedReason = reason),
                  child: Container(
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(AppColors.radiusSm),
                      border: Border.all(
                        color: selected ? AppColors.accent : AppColors.border,
                        width: 1.5,
                      ),
                      color: selected
                          ? const Color(0xFFF8F1FA)
                          : Colors.transparent,
                    ),
                    child: Text(
                      reason,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
                        color: selected ? AppColors.accent : AppColors.fg,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  // ── Description ──

  Widget _buildDescSection() {
    final charCount = _descCtrl.text.length;
    final over = charCount > _maxChars;
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 6),
            child: Text(
              '详细说明',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: AppColors.fg,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 4),
            child: TextField(
              controller: _descCtrl,
              maxLength: _maxChars,
              maxLines: 4,
              minLines: 4,
              buildCounter: (_, {required currentLength, required maxLength, required isFocused}) => null,
              onChanged: (_) => setState(() {}),
              style: const TextStyle(fontSize: 14, color: AppColors.fg, height: 1.5),
              decoration: InputDecoration(
                hintText: '请描述具体问题，帮助我们快速核实处理\n例如：商品描述与实物不符、卖家要求私下转账等',
                hintStyle: const TextStyle(color: AppColors.placeholder),
                filled: true,
                fillColor: AppColors.surface,
                contentPadding: const EdgeInsets.all(12),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.border, width: 1.5),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.border, width: 1.5),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.accent, width: 1.5),
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
            child: Align(
              alignment: Alignment.centerRight,
              child: Text(
                '$charCount/$_maxChars',
                style: TextStyle(
                  fontSize: 11,
                  color: over ? const Color(0xFFFF3B30) : AppColors.muted,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Evidence Upload ──

  Widget _buildEvidenceSection() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 8),
            child: Text(
              '举证截图（选填，最多3张）',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: AppColors.fg,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ...List.generate(_evidenceFiles.length, (i) {
                  return Stack(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(AppColors.radiusSm),
                        child: SizedBox(
                          width: 72,
                          height: 72,
                          child: Image.file(
                            _evidenceFiles[i],
                            fit: BoxFit.cover,
                          ),
                        ),
                      ),
                      Positioned(
                        top: -6,
                        right: -6,
                        child: GestureDetector(
                          onTap: () => _removeImage(i),
                          child: Container(
                            width: 20,
                            height: 20,
                            decoration: const BoxDecoration(
                              color: AppColors.fg,
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.close,
                                size: 12, color: Colors.white),
                          ),
                        ),
                      ),
                    ],
                  );
                }),
                if (_evidenceFiles.length < _maxImages)
                  GestureDetector(
                    onTap: _pickImage,
                    child: Container(
                      width: 72,
                      height: 72,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(AppColors.radiusSm),
                        border: Border.all(
                          color: AppColors.border,
                          width: 1.5,
                          strokeAlign: BorderSide.strokeAlignInside,
                        ),
                      ),
                      child: const Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.add, size: 22, color: AppColors.muted),
                          SizedBox(height: 2),
                          Text(
                            '上传',
                            style: TextStyle(
                                fontSize: 10, color: AppColors.muted),
                          ),
                        ],
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ── Contact ──

  Widget _buildContactSection() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusMd),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 6),
            child: Text(
              '联系方式（选填）',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: AppColors.fg,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
            child: TextField(
              controller: _contactCtrl,
              style: const TextStyle(fontSize: 14, color: AppColors.fg),
              decoration: InputDecoration(
                hintText: 'QQ/微信/手机号，方便管理员联系核实',
                hintStyle: const TextStyle(color: AppColors.placeholder),
                filled: true,
                fillColor: AppColors.surface,
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.border, width: 1.5),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.border, width: 1.5),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusSm),
                  borderSide: const BorderSide(color: AppColors.accent, width: 1.5),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ── Submit ──

  Widget _buildSubmitArea() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: SizedBox(
        width: double.infinity,
        height: 46,
        child: ElevatedButton(
          onPressed: _canSubmit && !_submitting ? _submit : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFFFF3B30),
            foregroundColor: Colors.white,
            disabledBackgroundColor: AppColors.border,
            disabledForegroundColor: const Color(0xFFA892A8),
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppColors.radiusPill),
            ),
            textStyle: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w600,
            ),
          ),
          child: _submitting
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    color: Colors.white,
                    strokeWidth: 2,
                  ),
                )
              : const Text('提交举报'),
        ),
      ),
    );
  }

  // ── Disclaimer ──

  Widget _buildDisclaimer() {
    return const Padding(
      padding: EdgeInsets.fromLTRB(16, 0, 16, 24),
      child: Text(
        '感谢你的监督，我们会尽快核实处理。\n恶意举报将影响你的信用记录。',
        style: TextStyle(fontSize: 11, color: AppColors.muted, height: 1.5),
        textAlign: TextAlign.center,
      ),
    );
  }

  // ── Success ──

  Widget _buildSuccess() {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(40),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 72,
                  height: 72,
                  decoration: const BoxDecoration(
                    color: Color(0xFFD4F5E8),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.check, size: 36, color: AppColors.success),
                ),
                const SizedBox(height: 16),
                const Text(
                  '举报已提交',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w700,
                    letterSpacing: -0.2,
                    color: AppColors.fg,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  '管理员将在24小时内审核处理，\n处理结果将通过消息通知你。',
                  style: TextStyle(fontSize: 14, color: AppColors.muted, height: 1.5),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                ElevatedButton(
                  onPressed: () => Navigator.of(context).pop(),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.accent,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 10),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppColors.radiusPill),
                    ),
                    textStyle: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  child: const Text('返回'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
