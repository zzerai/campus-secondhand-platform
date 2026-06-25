class UserHomepage {
  final int userId;
  final String? nickname;
  final String? avatar;
  final int creditScore;
  final String? createTime;
  final int onSaleCount;
  final int soldCount;
  final double averageScore;
  final int goodRate;
  final int totalReceived;

  UserHomepage({
    required this.userId,
    this.nickname,
    this.avatar,
    this.creditScore = 100,
    this.createTime,
    this.onSaleCount = 0,
    this.soldCount = 0,
    this.averageScore = 0,
    this.goodRate = 0,
    this.totalReceived = 0,
  });

  factory UserHomepage.fromJson(Map<String, dynamic> json) {
    int asInt(dynamic v, [int fallback = 0]) =>
        v is int ? v : int.tryParse(v?.toString() ?? '') ?? fallback;
    return UserHomepage(
      userId: asInt(json['userId']),
      nickname: json['nickname'],
      avatar: json['avatar'],
      creditScore: asInt(json['creditScore'], 100),
      createTime: json['createTime']?.toString(),
      onSaleCount: asInt(json['onSaleCount']),
      soldCount: asInt(json['soldCount']),
      averageScore: (json['averageScore'] ?? 0).toDouble(),
      goodRate: asInt(json['goodRate']),
      totalReceived: asInt(json['totalReceived']),
    );
  }
}
