import 'package:flutter/material.dart';

class SubCategory {
  final String iconId;
  final String label;
  const SubCategory(this.iconId, this.label);
}

class CategoryData {
  final String id;
  final int categoryId;
  final Color color;
  final String name;
  final List<SubCategory> subs;

  const CategoryData({
    required this.id,
    required this.categoryId,
    required this.color,
    required this.name,
    required this.subs,
  });
}

// ── Category Dataset ──
// categoryId 对应数据库 tr_trade_category.category_id

const List<CategoryData> categoryData = [
  CategoryData(
    id: 'books',
    categoryId: 1,
    color: Color(0xFFFF5722),
    name: '教材教辅',

    subs: [
      SubCategory('book-open', '考研资料'),
      SubCategory('doc-text', '四六级'),
      SubCategory('monitor', '计算机'),
      SubCategory('ruler', '数学'),
      SubCategory('flask', '理工科'),
      SubCategory('book', '文科'),
    ],
  ),
  CategoryData(
    id: 'elec',
    categoryId: 2,
    color: Color(0xFFF43370),
    name: '数码电子',

    subs: [
      SubCategory('smartphone', '手机平板'),
      SubCategory('laptop', '笔记本'),
      SubCategory('headphones', '耳机音箱'),
      SubCategory('camera', '相机'),
      SubCategory('keyboard', '外设配件'),
      SubCategory('watch', '智能穿戴'),
    ],
  ),
  CategoryData(
    id: 'life',
    categoryId: 4,
    color: Color(0xFFFFB800),
    name: '生活用品',

    subs: [
      SubCategory('lamp', '家具'),
      SubCategory('zap', '小家电'),
      SubCategory('bed', '床品'),
      SubCategory('coffee', '厨具'),
      SubCategory('package', '收纳日用'),
      SubCategory('leaf', '绿植'),
    ],
  ),
  CategoryData(
    id: 'cloth',
    categoryId: 3,
    color: Color(0xFF00C48C),
    name: '服饰鞋包',

    subs: [
      SubCategory('shirt', '男装'),
      SubCategory('hanger', '女装'),
      SubCategory('shoe', '鞋子'),
      SubCategory('bag', '包包'),
      SubCategory('glasses', '配饰'),
      SubCategory('gem', '首饰'),
    ],
  ),
  CategoryData(
    id: 'sport',
    categoryId: 5,
    color: Color(0xFF1A0E28),
    name: '运动户外',

    subs: [
      SubCategory('circle', '球类'),
      SubCategory('dumbbell', '健身器械'),
      SubCategory('bike', '骑行'),
      SubCategory('sun', '瑜伽'),
      SubCategory('tent', '户外装备'),
      SubCategory('run', '运动鞋服'),
    ],
  ),
  CategoryData(
    id: 'other',
    categoryId: 6,
    color: Color(0xFF8E7798),
    name: '其他闲置',

    subs: [
      SubCategory('gift', '礼品'),
      SubCategory('brush', '文具画材'),
      SubCategory('car', '出行交通'),
      SubCategory('paw', '宠物用品'),
      SubCategory('wrench', '其他'),
    ],
  ),
];