class GoodsListResult {
  final List<TradeGoods> rows;
  final int total;

  GoodsListResult({required this.rows, this.total = 0});
}

class TradeGoods {
  final int? goodsId;
  final int? sellerId;
  final String title;
  final int? categoryId;
  final String? categoryName;
  final double price;
  final double? originalPrice;
  final String description;
  final String imageUrls;
  final String quality;
  final String tradePlace;
  final String contactWay;
  final String? goodsStatus;
  final int? viewCount;
  final int? favoriteCount;
  final String? sellerNickname;
  final String? sellerAvatar;
  final String? keyword;
  final String? remark;

  TradeGoods({
    this.goodsId,
    this.sellerId,
    required this.title,
    this.categoryId,
    this.categoryName,
    required this.price,
    this.originalPrice,
    required this.description,
    this.imageUrls = '',
    this.quality = '',
    this.tradePlace = '',
    this.contactWay = '',
    this.goodsStatus,
    this.viewCount,
    this.favoriteCount,
    this.sellerNickname,
    this.sellerAvatar,
    this.keyword,
    this.remark,
  });

  /// 对买家（非卖家本人）而言商品是否不可用：后端非上架商品详情一律返回"商品不存在或已下架"，
  /// 故状态非 '1'(已上架) 即视为不可用。goodsStatus 为空时按可用处理，避免误盖。
  bool get unavailableForBuyer => goodsStatus != null && goodsStatus != '1';

  /// 不可用时卡片遮罩上的文案：已售出单独区分，其余统一显示"已下架"。
  String get unavailableLabel => goodsStatus == '4' ? '已售出' : '已下架';

  Map<String, dynamic> toJson() {
    return {
      if (goodsId != null) 'goodsId': goodsId,
      'title': title,
      'price': price,
      'categoryId': categoryId,
      'description': description,
      if (originalPrice != null) 'originalPrice': originalPrice,
      if (quality.isNotEmpty) 'quality': quality,
      if (tradePlace.isNotEmpty) 'tradePlace': tradePlace,
      if (contactWay.isNotEmpty) 'contactWay': contactWay,
      if (imageUrls.isNotEmpty) 'imageUrls': imageUrls,
      if (remark != null && remark!.isNotEmpty) 'remark': remark,
    };
  }

  factory TradeGoods.fromJson(Map<String, dynamic> json) {
    return TradeGoods(
      goodsId: json['goodsId'] is int ? json['goodsId'] : null,
      sellerId: json['sellerId'] is int ? json['sellerId'] : null,
      title: json['title'] ?? '',
      categoryId: json['categoryId'] is int ? json['categoryId'] : null,
      categoryName: json['categoryName'],
      price: (json['price'] ?? 0).toDouble(),
      originalPrice: json['originalPrice']?.toDouble(),
      description: json['description'] ?? '',
      imageUrls: json['imageUrls'] ?? json['images'] ?? '',
      quality: json['quality'] ?? json['conditionLevel'] ?? '',
      tradePlace: json['tradePlace'] ?? '',
      contactWay: json['contactWay'] ?? json['contactInfo'] ?? '',
      goodsStatus: json['goodsStatus'] ?? json['status'],
      viewCount: json['viewCount'] is int ? json['viewCount'] : null,
      favoriteCount: json['favoriteCount'] is int ? json['favoriteCount'] : null,
      sellerNickname: json['sellerNickname'] ?? json['sellerNickName'],
      sellerAvatar: json['sellerAvatar'],
      keyword: json['keyword'],
      remark: json['remark'],
    );
  }
}
