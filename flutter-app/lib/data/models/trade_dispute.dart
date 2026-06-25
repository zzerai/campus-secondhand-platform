class TradeDispute {
  final int disputeId;
  final int? applicantId;
  final int? orderId;
  final int? goodsId;
  final String? disputeType;
  final String? disputeContent;
  final String? evidenceImages;
  final String? aiAnalysis;
  final String? handleStatus;
  final String? orderNo;
  final String? orderCreateTime;
  final String? handleResult;
  final String? faultParty;
  final String? refundStatus;
  final num? refundAmount;
  final String? handleTime;
  final String? createTime;

  TradeDispute({
    required this.disputeId,
    this.applicantId,
    this.orderId,
    this.goodsId,
    this.disputeType,
    this.disputeContent,
    this.evidenceImages,
    this.aiAnalysis,
    this.orderNo,
    this.orderCreateTime,
    this.handleStatus,
    this.handleResult,
    this.faultParty,
    this.refundStatus,
    this.refundAmount,
    this.handleTime,
    this.createTime,
  });

  String get statusLabel {
    switch (handleStatus) {
      case '0':
        return '待AI分析';
      case '1':
        return 'AI分析中';
      case '2':
        return '等待人工仲裁';
      case '3':
        return '已处理';
      default:
        return '未知';
    }
  }

  factory TradeDispute.fromJson(Map<String, dynamic> json) {
    return TradeDispute(
      disputeId: (json['disputeId'] as num).toInt(),
      applicantId: json['applicantId'] != null ? (json['applicantId'] as num).toInt() : null,
      orderId: json['orderId'] != null ? (json['orderId'] as num).toInt() : null,
      goodsId: json['goodsId'] != null ? (json['goodsId'] as num).toInt() : null,
      disputeType: json['disputeType'],
      disputeContent: json['disputeContent'],
      evidenceImages: json['evidenceImages'],
      aiAnalysis: json['aiAnalysis'],
      orderNo: json['orderNo'],
      orderCreateTime: json['orderCreateTime'],
      handleStatus: json['handleStatus'],
      handleResult: json['handleResult'],
      faultParty: json['faultParty'],
      refundStatus: json['refundStatus'],
      refundAmount: json['refundAmount'],
      handleTime: json['handleTime'],
      createTime: json['createTime'],
    );
  }
}
