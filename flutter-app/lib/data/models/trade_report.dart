class TradeReport {
  final int reportId;
  final int? goodsId;
  final int? orderId;
  final int? reportUserId;
  final int? reportedUserId;
  final String reportType;
  final String? reportContent;
  final String? evidenceImages;
  final String? handleStatus;
  final int? handleUserId;
  final String? handleTime;
  final String? handleResult;
  final String? createTime;
  final String? remark;
  final String? goodsTitle;
  final num? goodsPrice;
  final String? goodsStatus;
  final String? goodsCoverImage;

  TradeReport({
    required this.reportId,
    this.goodsId,
    this.orderId,
    this.reportUserId,
    this.reportedUserId,
    required this.reportType,
    this.reportContent,
    this.evidenceImages,
    this.handleStatus,
    this.handleUserId,
    this.handleTime,
    this.handleResult,
    this.createTime,
    this.remark,
    this.goodsTitle,
    this.goodsPrice,
    this.goodsStatus,
    this.goodsCoverImage,
  });

  String get statusLabel {
    switch (handleStatus) {
      case '0':
        return '待处理';
      case '1':
        return '已处理';
      case '2':
        return '已驳回';
      default:
        return '未知';
    }
  }

  factory TradeReport.fromJson(Map<String, dynamic> json) {
    return TradeReport(
      reportId: (json['reportId'] as num).toInt(),
      goodsId: json['goodsId'] != null ? (json['goodsId'] as num).toInt() : null,
      orderId: json['orderId'] != null ? (json['orderId'] as num).toInt() : null,
      reportUserId: json['reportUserId'] != null ? (json['reportUserId'] as num).toInt() : null,
      reportedUserId: json['reportedUserId'] != null ? (json['reportedUserId'] as num).toInt() : null,
      reportType: json['reportType'] ?? '',
      reportContent: json['reportContent'],
      evidenceImages: json['evidenceImages'],
      handleStatus: json['handleStatus'],
      handleUserId: json['handleUserId'] != null ? (json['handleUserId'] as num).toInt() : null,
      handleTime: json['handleTime'],
      handleResult: json['handleResult'],
      createTime: json['createTime'],
      remark: json['remark'],
      goodsTitle: json['goodsTitle'],
      goodsPrice: json['goodsPrice'] as num?,
      goodsStatus: json['goodsStatus'],
      goodsCoverImage: json['goodsCoverImage'],
    );
  }
}
