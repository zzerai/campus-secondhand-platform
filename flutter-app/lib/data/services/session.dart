import '../models/auth_models.dart';

class Session {
  Session._();

  static final Session _instance = Session._();
  static Session get instance => _instance;

  static const String baseUrl = 'http://112.126.27.217';

  String? token;
  StudentUser? currentUser;

  Map<String, String> get headers => {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  bool get isLoggedIn => token != null && token!.isNotEmpty;

  void logout() {
    token = null;
    currentUser = null;
  }
}
