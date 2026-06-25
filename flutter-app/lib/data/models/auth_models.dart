class LoginRequest {
  final String username;
  final String? password;
  final String? code;
  final String? uuid;

  LoginRequest({
    required this.username,
    this.password,
    this.code,
    this.uuid,
  });

  Map<String, dynamic> toJson() => {
        'username': username,
        if (password != null) 'password': password,
        if (code != null) 'code': code,
        if (uuid != null) 'uuid': uuid,
      };
}

class RegisterRequest {
  final String studentNo;
  final String phone;
  final String password;
  final String? nickname;
  final String? smsCode;

  RegisterRequest({
    required this.studentNo,
    required this.phone,
    required this.password,
    this.nickname,
    this.smsCode,
  });

  Map<String, dynamic> toJson() => {
        'studentNo': studentNo,
        'phone': phone,
        'password': password,
        if (nickname != null) 'nickname': nickname,
        if (smsCode != null) 'smsCode': smsCode,
      };
}

class StudentUser {
  final int userId;
  final String? studentNo;
  String? nickname;
  String? phone;
  String? avatar;
  String? contactWay;
  final int creditScore;

  StudentUser({
    required this.userId,
    this.studentNo,
    this.nickname,
    this.phone,
    this.avatar,
    this.contactWay,
    this.creditScore = 100,
  });

  factory StudentUser.fromJson(Map<String, dynamic> json) => StudentUser(
        userId: json['userId'] is int
            ? json['userId']
            : int.tryParse(json['userId'].toString()) ?? 0,
        studentNo: json['studentNo'],
        nickname: json['nickname'],
        phone: json['phone'],
        avatar: json['avatar'],
        contactWay: json['contactWay'],
        creditScore: json['creditScore'] is int
            ? json['creditScore']
            : int.tryParse(json['creditScore']?.toString() ?? '') ?? 100,
      );

  Map<String, dynamic> toJson() => {
        'userId': userId,
        'studentNo': studentNo,
        'nickname': nickname,
        'phone': phone,
        'avatar': avatar,
        'contactWay': contactWay,
        'creditScore': creditScore,
      };
}

class LoginResult {
  final String token;
  final StudentUser? user;

  LoginResult({required this.token, this.user});

  factory LoginResult.fromJson(Map<String, dynamic> json) => LoginResult(
        token: json['token'] ?? '',
        user: json['studentUser'] != null
            ? StudentUser.fromJson(json['studentUser'])
            : null,
      );
}
