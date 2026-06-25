class AjaxResult {
  final bool error;
  final bool warn;
  final bool success;
  final bool empty;
  final dynamic data;

  AjaxResult({
    required this.error,
    required this.warn,
    required this.success,
    required this.empty,
    this.data,
  });

  factory AjaxResult.fromJson(Map<String, dynamic> json) {
    final code = json['code'];
    final isSuccess = code == 200;
    return AjaxResult(
      error: json['error'] ?? !isSuccess,
      warn: json['warn'] ?? false,
      success: json['success'] ?? isSuccess,
      empty: json['empty'] ?? false,
      data: json['data'] ?? json['msg'],
    );
  }
}
