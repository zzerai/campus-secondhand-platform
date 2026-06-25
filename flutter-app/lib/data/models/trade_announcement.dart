class TradeAnnouncement {
  final int announcementId;
  final String title;
  final String? content;
  final String? type;
  final String? isTop;
  final String? publishStatus;
  final String? publishTime;
  final String? coverImage;
  final String? createTime;

  TradeAnnouncement({
    required this.announcementId,
    required this.title,
    this.content,
    this.type,
    this.isTop,
    this.publishStatus,
    this.publishTime,
    this.coverImage,
    this.createTime,
  });

  bool get isTopped => isTop == '1';

  /// 类型标签，与管理端 views/trade/announcement 的取值保持一致
  String get typeLabel {
    switch (type) {
      case '1':
        return '系统公告';
      case '2':
        return '活动公告';
      case '3':
        return '通知公告';
      default:
        return '公告';
    }
  }

  /// 展示用时间：优先发布时间，其次创建时间
  String get displayTime => publishTime ?? createTime ?? '';

  factory TradeAnnouncement.fromJson(Map<String, dynamic> json) {
    return TradeAnnouncement(
      announcementId: (json['announcementId'] as num).toInt(),
      title: json['title'] ?? '',
      content: json['content'],
      type: json['type'],
      isTop: json['isTop'],
      publishStatus: json['publishStatus'],
      publishTime: json['publishTime'],
      coverImage: json['coverImage'],
      createTime: json['createTime'],
    );
  }
}
