import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../../common/constants/app_styles.dart';
import '../../common/constants/app_theme.dart';
import '../../data/models/trade_goods.dart';
import '../../data/services/goods_api.dart';
import '../../data/services/upload_api.dart';
import '../../widgets/cors_image.dart';
import '../../common/utils/toast_utils.dart';
import '../../widgets/image_placeholder.dart';
import '../../widgets/submit_button.dart';
import '../../widgets/app_form_field.dart';

const _categories = [
  {'id': 1, 'name': '教材'},
  {'id': 2, 'name': '电子产品'},
  {'id': 3, 'name': '服饰'},
  {'id': 4, 'name': '生活用品'},
  {'id': 5, 'name': '运动器材'},
  {'id': 6, 'name': '其他'},
];

const _qualityLevels = ['全新', '9成新', '8成新', '7成新', '6成新及以下'];

class GoodsPublishPage extends StatefulWidget {
  final TradeGoods? editGoods;
  const GoodsPublishPage({super.key, this.editGoods});

  @override
  State<GoodsPublishPage> createState() => _GoodsPublishPageState();
}

class _GoodsPublishPageState extends State<GoodsPublishPage> {
  final _goodsApi = GoodsApi.instance;
  final _uploadApi = UploadApi.instance;
  final ImagePicker _imagePicker = ImagePicker();
  final _formKey = GlobalKey<FormState>();

  final _titleCtrl = TextEditingController();
  final _priceCtrl = TextEditingController();
  final _originalPriceCtrl = TextEditingController();
  final _descriptionCtrl = TextEditingController();
  final _tradePlaceCtrl = TextEditingController();
  final _contactWayCtrl = TextEditingController();
  final _remarkCtrl = TextEditingController();

  int? _categoryId;
  String _quality = '9成新';

  final List<XFile> _selectedImages = [];
  final List<String> _uploadedUrls = [];
  bool _isUploading = false;
  bool _isSubmitting = false;

  bool get _isEditing => widget.editGoods != null;

  @override
  void initState() {
    super.initState();
    if (_isEditing) _initEditData();
  }

  void _initEditData() {
    final g = widget.editGoods!;
    _titleCtrl.text = g.title;
    _priceCtrl.text = g.price.toStringAsFixed(0);
    if (g.originalPrice != null) {
      _originalPriceCtrl.text = g.originalPrice!.toStringAsFixed(0);
    }
    _descriptionCtrl.text = g.description;
    _tradePlaceCtrl.text = g.tradePlace;
    _contactWayCtrl.text = g.contactWay;
    if (g.remark != null && g.remark!.isNotEmpty) {
      _remarkCtrl.text = g.remark!;
    }
    _categoryId = g.categoryId;
    if (g.quality.isNotEmpty) _quality = g.quality;
    if (g.imageUrls.isNotEmpty) {
      _uploadedUrls.addAll(g.imageUrls.split(',').where((u) => u.trim().isNotEmpty));
    }
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _priceCtrl.dispose();
    _originalPriceCtrl.dispose();
    _descriptionCtrl.dispose();
    _tradePlaceCtrl.dispose();
    _contactWayCtrl.dispose();
    _remarkCtrl.dispose();
    super.dispose();
  }

  // ─── Image handling ───

  Future<void> _pickImages() async {
    final images = await _imagePicker.pickMultiImage();
    if (images.isEmpty) return;
    setState(() => _selectedImages.addAll(images));
    await _uploadImages();
  }

  void _removeServerImage(int index) {
    setState(() => _uploadedUrls.removeAt(index));
  }

  void _removeLocalImage(int index) {
    setState(() => _selectedImages.removeAt(index));
  }

  Future<void> _uploadImages() async {
    if (_selectedImages.isEmpty) return;

    debugPrint('[上传图片] 开始上传 ${_selectedImages.length} 张图片');
    setState(() => _isUploading = true);

    try {
      final urls = await _uploadApi.uploadImages(_selectedImages);
      debugPrint('[上传图片] 返回结果: ${urls.length} 个URL');

      if (urls.isNotEmpty) {
        setState(() {
          _uploadedUrls.addAll(urls);
          _selectedImages.clear();
        });
        showToast(context,'成功上传 ${urls.length} 张图片');
      } else {
        showToast(context,'图片上传失败，请重试');
      }
    } catch (e) {
      debugPrint('[上传图片] 异常: $e');
      showToast(context,'图片上传异常: $e');
    } finally {
      setState(() => _isUploading = false);
    }
  }

  // ─── Submit ───

  Future<void> _submitGoods() async {
    if (!_formKey.currentState!.validate()) return;

    if (_uploadedUrls.isEmpty) {
      showToast(context,'请先上传商品图片');
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final goods = TradeGoods(
        goodsId: widget.editGoods?.goodsId,
        title: _titleCtrl.text.trim(),
        categoryId: _categoryId,
        price: double.parse(_priceCtrl.text.trim()),
        originalPrice: _originalPriceCtrl.text.isNotEmpty
            ? double.parse(_originalPriceCtrl.text.trim())
            : null,
        description: _descriptionCtrl.text.trim(),
        imageUrls: _uploadedUrls.join(','),
        quality: _quality,
        tradePlace: _tradePlaceCtrl.text.trim(),
        contactWay: _contactWayCtrl.text.trim(),
        remark: _remarkCtrl.text.trim(),
      );

      debugPrint('[发布商品] 请求体: ${goods.toJson()}');

      final result = _isEditing
          ? await _goodsApi.updateGoods(goods)
          : await _goodsApi.publishGoods(goods);

      if (!mounted) return;

      if (result.success) {
        showToast(context, _isEditing ? '修改成功，等待重新审核' : '商品发布成功，等待审核');
        Navigator.of(context).pop(true);
      } else {
        showToast(context, '${_isEditing ? '修改' : '发布'}失败: ${result.data ?? "未知错误"}');
      }
    } catch (e) {
      debugPrint('[发布商品] 异常: $e');
      if (mounted) showToast(context,'发布异常: $e');
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  // ─── Build ───

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: Text(_isEditing ? '编辑商品' : '发布商品'),
        backgroundColor: AppColors.surface,
        foregroundColor: AppColors.fg,
        elevation: 0,
        scrolledUnderElevation: 0.5,
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(24, 20, 24, 40),
          children: [
            _buildImageSection(),
            const SizedBox(height: 24),
            AppFormField(
              label: '商品标题',
              controller: _titleCtrl,
              hint: '请输入商品标题',
              validator: (v) =>
                  v == null || v.trim().isEmpty ? '请输入商品标题' : null,
            ),
            const SizedBox(height: 20),
            _buildCategoryDropdown(),
            const SizedBox(height: 20),
            _buildPriceRow(),
            const SizedBox(height: 20),
            _buildQualityDropdown(),
            const SizedBox(height: 20),
            AppFormField(
              label: '交易地点',
              controller: _tradePlaceCtrl,
              hint: '请输入交易地点',
            ),
            const SizedBox(height: 20),
            AppFormField(
              label: '联系方式',
              controller: _contactWayCtrl,
              hint: '微信 / QQ / 手机号',
            ),
            const SizedBox(height: 20),
            AppFormField(
              label: '商品描述',
              controller: _descriptionCtrl,
              hint: '请详细描述商品情况',
              maxLines: 5,
              validator: (v) =>
                  v == null || v.trim().isEmpty ? '请输入商品描述' : null,
            ),
            const SizedBox(height: 20),
            AppFormField(
              label: '备注（选填）',
              controller: _remarkCtrl,
              hint: '补充说明',
              maxLines: 2,
            ),
            const SizedBox(height: 32),
            SubmitButton(
              onPressed: _submitGoods,
              isLoading: _isSubmitting,
              label: _isEditing ? '保存修改' : '发布商品',
            ),
          ],
        ),
      ),
    );
  }

  // ─── Image section ───

  Widget _buildImageSection() {
    final totalImages = _uploadedUrls.length + _selectedImages.length;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('商品图片',
            style: AppTextStyles.inputLabel),
        const SizedBox(height: 10),
        SizedBox(
          height: 100,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: _uploadedUrls.length + _selectedImages.length + 1,
            itemBuilder: (context, index) {
              if (index == _uploadedUrls.length + _selectedImages.length) {
                return _buildAddImageButton();
              }
              if (index < _uploadedUrls.length) {
                return _buildServerImagePreview(index);
              }
              return _buildLocalImagePreview(index - _uploadedUrls.length);
            },
          ),
        ),
        if (_isUploading) ...[
          const SizedBox(height: 8),
          const Row(
            children: [
              SizedBox(
                width: 14,
                height: 14,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
              SizedBox(width: 8),
              Text('图片上传中...',
                  style: TextStyle(fontSize: 13, color: AppColors.muted)),
            ],
          ),
        ],
        if (totalImages > 0) ...[
          const SizedBox(height: 8),
          Text('已上传 $totalImages 张图片',
              style: const TextStyle(fontSize: 13, color: AppColors.success)),
        ],
      ],
    );
  }

  Widget _buildAddImageButton() {
    return GestureDetector(
      onTap: _pickImages,
      child: Container(
        width: 100,
        height: 100,
        margin: const EdgeInsets.only(right: 8),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(AppColors.radiusSm),
          border: Border.all(color: AppColors.border, width: 1.5),
        ),
        child: const Icon(Icons.add_photo_alternate_outlined,
            size: 28, color: AppColors.placeholder),
      ),
    );
  }

  Widget _buildServerImagePreview(int index) {
    final url = _uploadedUrls[index];
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: Stack(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppColors.radiusSm),
            child: CorsImage(
              src: url,
              width: 100,
              height: 100,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const ImagePlaceholder(width: 100, height: 100),
            ),
          ),
          Positioned(
            top: 0,
            right: 0,
            child: GestureDetector(
              onTap: () => _removeServerImage(index),
              behavior: HitTestBehavior.opaque,
              child: Container(
                width: 28,
                height: 28,
                decoration: const BoxDecoration(
                  color: Colors.black54,
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.close, size: 16, color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLocalImagePreview(int index) {
    final path = _selectedImages[index].path;
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: Stack(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(AppColors.radiusSm),
            child: kIsWeb
                ? CorsImage(
                    src: path,
                    width: 100,
                    height: 100,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const ImagePlaceholder(width: 100, height: 100))
                : Image.file(File(path),
                    width: 100,
                    height: 100,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const ImagePlaceholder(width: 100, height: 100)),
          ),
          Positioned(
            top: 0,
            right: 0,
            child: GestureDetector(
              onTap: () => _removeLocalImage(index),
              behavior: HitTestBehavior.opaque,
              child: Container(
                width: 28,
                height: 28,
                decoration: const BoxDecoration(
                  color: Colors.black54,
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.close, size: 16, color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryDropdown() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 2, bottom: 6),
          child: const Text('商品分类',
              style: AppTextStyles.inputLabel),
        ),
        DropdownButtonFormField<int>(
          initialValue: _categoryId,
          decoration: const InputDecoration(hintText: '请选择商品分类'),
          items: _categories.map((c) => DropdownMenuItem<int>(
            value: c['id'] as int,
            child: Text(c['name'] as String),
          )).toList(),
          onChanged: (v) => setState(() => _categoryId = v),
          validator: (v) => v == null ? '请选择商品分类' : null,
        ),
      ],
    );
  }

  Widget _buildPriceRow() {
    return Row(
      children: [
        Expanded(
          child: AppFormField(
            label: '售价',
            controller: _priceCtrl,
            hint: '请输入售价',
            keyboardType: TextInputType.number,
            validator: (v) {
              if (v == null || v.trim().isEmpty) return '请输入售价';
              final price = double.tryParse(v.trim());
              if (price == null || price < 0) return '价格格式错误';
              return null;
            },
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: AppFormField(
            label: '原价（选填）',
            controller: _originalPriceCtrl,
            hint: '请输入原价',
            keyboardType: TextInputType.number,
          ),
        ),
      ],
    );
  }

  Widget _buildQualityDropdown() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 2, bottom: 6),
          child: const Text('商品成色',
              style: AppTextStyles.inputLabel),
        ),
        DropdownButtonFormField<String>(
          initialValue: _quality,
          decoration: const InputDecoration(hintText: '请选择商品成色'),
          items: _qualityLevels
              .map((q) => DropdownMenuItem<String>(
                    value: q,
                    child: Text(q),
                  ))
              .toList(),
          onChanged: (v) {
            if (v != null) setState(() => _quality = v);
          },
        ),
      ],
    );
  }

}
