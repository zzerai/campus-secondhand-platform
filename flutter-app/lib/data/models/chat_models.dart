class AdminInfo {
  final int userId;
  final String nickname;
  final String? avatar;

  AdminInfo({
    required this.userId,
    required this.nickname,
    this.avatar,
  });

  factory AdminInfo.fromJson(Map<String, dynamic> json) {
    return AdminInfo(
      userId: json['userId'] is int ? json['userId'] : 0,
      nickname: json['nickname'] ?? '管理员',
      avatar: json['avatar'],
    );
  }
}

class ConversationListResult {
  final List<AppConversationVo> rows;
  final int total;
  ConversationListResult({required this.rows, this.total = 0});
}

class AppConversationVo {
  final int peerId;
  final String peerNickname;
  final String? peerAvatar;
  final int goodsId;
  final String goodsTitle;
  final String? goodsCoverImage;
  final String? goodsStatus;
  final String? lastContent;
  final int? lastSenderId;
  final String? lastTime;
  final int unreadCount;

  final int? orderId;

  AppConversationVo({
    required this.peerId,
    required this.peerNickname,
    this.peerAvatar,
    required this.goodsId,
    required this.goodsTitle,
    this.goodsCoverImage,
    this.goodsStatus,
    this.lastContent,
    this.lastSenderId,
    this.lastTime,
    this.unreadCount = 0,
    this.orderId,
  });

  factory AppConversationVo.fromJson(Map<String, dynamic> json) {
    return AppConversationVo(
      peerId: json['peerId'] is int ? json['peerId'] : 0,
      peerNickname: json['peerNickname'] ?? '',
      peerAvatar: json['peerAvatar'],
      goodsId: json['goodsId'] is int ? json['goodsId'] : 0,
      goodsTitle: json['goodsTitle'] ?? '',
      goodsCoverImage: json['goodsCoverImage'],
      goodsStatus: json['goodsStatus'],
      lastContent: json['lastContent'],
      lastSenderId: json['lastSenderId'] is int ? json['lastSenderId'] : null,
      lastTime: json['lastTime'],
      unreadCount: json['unreadCount'] is int ? json['unreadCount'] : 0,
      orderId: json['orderId'] is int ? json['orderId'] : null,
    );
  }
}

class MessageListResult {
  final List<AppMessageVo> rows;
  final int total;
  MessageListResult({required this.rows, this.total = 0});
}

class AppMessageVo {
  final int messageId;
  final int senderId;
  final int receiverId;
  final String content;
  final String? readStatus;
  final String? createTime;
  final bool mine;
  final bool? peerOnline;

  AppMessageVo({
    required this.messageId,
    required this.senderId,
    required this.receiverId,
    required this.content,
    this.readStatus,
    this.createTime,
    this.mine = false,
    this.peerOnline,
  });

  factory AppMessageVo.fromJson(Map<String, dynamic> json) {
    return AppMessageVo(
      messageId: json['messageId'] is int ? json['messageId'] : 0,
      senderId: json['senderId'] is int ? json['senderId'] : 0,
      receiverId: json['receiverId'] is int ? json['receiverId'] : 0,
      content: json['content'] ?? '',
      readStatus: json['readStatus'],
      createTime: json['createTime'],
      mine: json['mine'] ?? false,
      peerOnline: json['peerOnline'] is bool ? json['peerOnline'] : null,
    );
  }
}
