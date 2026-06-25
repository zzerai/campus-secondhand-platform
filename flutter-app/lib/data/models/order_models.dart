class OrderCreateRequest {
  final int goodsId;
  final String? tradeMethod;
  final String? tradePlace;
  final String? appointmentTime;
  final String? buyerRemark;

  OrderCreateRequest({
    required this.goodsId,
    this.tradeMethod,
    this.tradePlace,
    this.appointmentTime,
    this.buyerRemark,
  });

  Map<String, dynamic> toJson() => {
        'goodsId': goodsId,
        if (tradeMethod != null) 'tradeMethod': tradeMethod,
        if (tradePlace != null) 'tradePlace': tradePlace,
        if (appointmentTime != null) 'appointmentTime': appointmentTime,
        if (buyerRemark != null) 'buyerRemark': buyerRemark,
      };
}

class TradeOrder {
  final int orderId;
  final String? orderNo;
  final int goodsId;
  final int buyerId;
  final int sellerId;
  final String goodsTitle;
  final String? goodsImages;
  final String? tradeMethod;
  final double tradePrice;
  final String? tradePlace;
  final String? appointmentTime;
  final String? orderStatus;
  final String? paymentStatus;
  final String? refundStatus;
  final double? refundAmount;
  final String? refundReason;
  final String? refundApplyTime;
  final String? refundTime;
  final String? alipayRefundNo;
  final String? buyerRemark;
  final String? sellerRemark;
  final String? cancelReason;
  final String? confirmTime;
  final String? payTime;
  final String? alipayTradeNo;
  final double? paymentAmount;
  final String? paymentTime;
  final String? completeTime;
  final String? cancelTime;
  final String? createTime;
  final String? sellerNickname;
  final String? buyerNickname;
  final String? sellerAvatar;
  final String? buyerAvatar;

  TradeOrder({
    required this.orderId,
    this.orderNo,
    required this.goodsId,
    required this.buyerId,
    required this.sellerId,
    required this.goodsTitle,
    this.goodsImages,
    this.tradeMethod,
    required this.tradePrice,
    this.tradePlace,
    this.appointmentTime,
    this.orderStatus,
    this.paymentStatus,
    this.refundStatus,
    this.refundAmount,
    this.refundReason,
    this.refundApplyTime,
    this.refundTime,
    this.alipayRefundNo,
    this.buyerRemark,
    this.sellerRemark,
    this.cancelReason,
    this.confirmTime,
    this.payTime,
    this.alipayTradeNo,
    this.paymentAmount,
    this.paymentTime,
    this.completeTime,
    this.cancelTime,
    this.createTime,
    this.sellerNickname,
    this.buyerNickname,
    this.sellerAvatar,
    this.buyerAvatar,
  });

  factory TradeOrder.fromJson(Map<String, dynamic> json) => TradeOrder(
        orderId: json['orderId'] is int ? json['orderId'] : 0,
        orderNo: json['orderNo'],
        goodsId: json['goodsId'] is int ? json['goodsId'] : 0,
        buyerId: json['buyerId'] is int ? json['buyerId'] : 0,
        sellerId: json['sellerId'] is int ? json['sellerId'] : 0,
        goodsTitle: json['goodsTitle'] ?? '',
        goodsImages: json['goodsImages'],
        tradeMethod: json['tradeMethod'],
        tradePrice: (json['tradePrice'] ?? 0).toDouble(),
        tradePlace: json['tradePlace'],
        appointmentTime: json['appointmentTime'],
        orderStatus: json['orderStatus']?.toString(),
        paymentStatus: json['paymentStatus']?.toString(),
        refundStatus: json['refundStatus']?.toString(),
        refundAmount: json['refundAmount']?.toDouble(),
        refundReason: json['refundReason'],
        refundApplyTime: json['refundApplyTime'],
        refundTime: json['refundTime'],
        alipayRefundNo: json['alipayRefundNo'],
        buyerRemark: json['buyerRemark'],
        sellerRemark: json['sellerRemark'],
        cancelReason: json['cancelReason'],
        confirmTime: json['confirmTime'],
        payTime: json['payTime'],
        alipayTradeNo: json['alipayTradeNo'],
        paymentAmount: json['paymentAmount']?.toDouble(),
        paymentTime: json['paymentTime'],
        completeTime: json['completeTime'],
        cancelTime: json['cancelTime'],
        createTime: json['createTime'],
        sellerNickname: json['sellerNickname'],
        buyerNickname: json['buyerNickname'],
        sellerAvatar: json['sellerAvatar'],
        buyerAvatar: json['buyerAvatar'],
      );

  String get statusLabel {
    switch (orderStatus) {
      case '0':
        return '待支付';
      case '2':
        return '待收货';
      case '3':
        return '已完成';
      case '4':
        return '已取消';
      case '5':
        return '争议中';
      case '6':
        return '退款中';
      case '7':
        return '已退款';
      default:
        return '未知';
    }
  }

  String? get refundStatusLabel {
    switch (refundStatus) {
      case '1':
        return '待卖家处理';
      case '2':
        return '退款处理中';
      case '3':
        return '已退款';
      case '4':
        return '卖家已拒绝';
      default:
        return null;
    }
  }
}

class AppOrderPayResultVo {
  final int orderId;
  final String? orderNo;
  final String? orderStatus;
  final String? paymentStatus;
  final String? alipayTradeNo;
  final double? paymentAmount;

  AppOrderPayResultVo({
    required this.orderId,
    this.orderNo,
    this.orderStatus,
    this.paymentStatus,
    this.alipayTradeNo,
    this.paymentAmount,
  });

  factory AppOrderPayResultVo.fromJson(Map<String, dynamic> json) =>
      AppOrderPayResultVo(
        orderId: json['orderId'] is int ? json['orderId'] : 0,
        orderNo: json['orderNo'],
        orderStatus: json['orderStatus']?.toString(),
        paymentStatus: json['paymentStatus']?.toString(),
        alipayTradeNo: json['alipayTradeNo'],
        paymentAmount: json['paymentAmount']?.toDouble(),
      );
}

class OrderListResult {
  final List<TradeOrder> rows;
  final int total;
  OrderListResult({required this.rows, this.total = 0});
}
