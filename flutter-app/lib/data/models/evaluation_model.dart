class TradeEvaluation {
  final int evaluationId;
  final int orderId;
  final int fromUserId;
  final String? fromUserName;
  final String? fromUserAvatar;
  final int toUserId;
  final String? toUserName;
  final String? toUserAvatar;
  final int score;
  final String? content;
  final String? goodsTitle;
  final String? goodsImages;
  final double? goodsPrice;
  final String? createTime;

  TradeEvaluation({
    required this.evaluationId,
    required this.orderId,
    required this.fromUserId,
    this.fromUserName,
    this.fromUserAvatar,
    required this.toUserId,
    this.toUserName,
    this.toUserAvatar,
    required this.score,
    this.content,
    this.goodsTitle,
    this.goodsImages,
    this.goodsPrice,
    this.createTime,
  });

  factory TradeEvaluation.fromJson(Map<String, dynamic> json) =>
      TradeEvaluation(
        evaluationId: json['evaluationId'] is int ? json['evaluationId'] : 0,
        orderId: json['orderId'] is int ? json['orderId'] : 0,
        fromUserId: json['fromUserId'] is int ? json['fromUserId'] : 0,
        fromUserName: json['fromUserName'],
        fromUserAvatar: json['fromUserAvatar'],
        toUserId: json['toUserId'] is int ? json['toUserId'] : 0,
        toUserName: json['toUserName'],
        toUserAvatar: json['toUserAvatar'],
        score: json['score'] is int ? json['score'] : 0,
        content: json['content'],
        goodsTitle: json['goodsTitle'],
        goodsImages: json['goodsImages'],
        goodsPrice: (json['goodsPrice'] ?? 0).toDouble(),
        createTime: json['createTime'],
      );
}

class EvaluationScoreSummary {
  final double averageScore;
  final int totalReceived;
  final int totalSent;
  final Map<int, int> distribution;

  EvaluationScoreSummary({
    required this.averageScore,
    required this.totalReceived,
    required this.totalSent,
    required this.distribution,
  });

  factory EvaluationScoreSummary.fromJson(Map<String, dynamic> json) {
    final distRaw = json['distribution'] as Map<String, dynamic>? ?? {};
    final dist = <int, int>{};
    distRaw.forEach((k, v) {
      dist[int.parse(k)] = v is int ? v : (v as num).toInt();
    });
    return EvaluationScoreSummary(
      averageScore: (json['averageScore'] ?? 0).toDouble(),
      totalReceived: json['totalReceived'] is int ? json['totalReceived'] : 0,
      totalSent: json['totalSent'] is int ? json['totalSent'] : 0,
      distribution: dist,
    );
  }
}

class SubmitEvaluationRequest {
  final int orderId;
  final int score;
  final String content;

  SubmitEvaluationRequest({
    required this.orderId,
    required this.score,
    required this.content,
  });

  Map<String, dynamic> toJson() => {
        'orderId': orderId,
        'score': score,
        'content': content,
      };
}
