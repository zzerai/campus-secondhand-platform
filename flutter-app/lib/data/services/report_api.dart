import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/ajax_result.dart';
import '../models/trade_report.dart';
import 'session.dart';

class ReportApi {
  static final ReportApi instance = ReportApi._();
  ReportApi._();

  final _session = Session.instance;

  Future<AjaxResult> submitReport({
    required int goodsId,
    int? orderId,
    required String reportType,
    required String reportContent,
    String? evidenceImages,
  }) async {
    final body = <String, dynamic>{
      'goodsId': goodsId,
      'reportType': reportType,
      'reportContent': reportContent,
    };
    if (orderId != null) body['orderId'] = orderId;
    if (evidenceImages != null && evidenceImages.isNotEmpty) {
      body['evidenceImages'] = evidenceImages;
    }

    debugPrint('[ReportApi] 举报: POST /app/report/submit');
    debugPrint('[ReportApi] body: ${jsonEncode(body)}');

    try {
      final response = await http.post(
        Uri.parse('${Session.baseUrl}/app/report/submit'),
        headers: _session.headers,
        body: jsonEncode(body),
      );
      debugPrint('[ReportApi] 响应: ${response.statusCode} ${response.body}');
      return AjaxResult.fromJson(jsonDecode(response.body));
    } catch (e) {
      debugPrint('[ReportApi] 异常: $e');
      rethrow;
    }
  }

  Future<List<TradeReport>> getMyReports() async {
    debugPrint('[ReportApi] 我的举报列表: GET /app/report/myList');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/report/myList'),
      headers: _session.headers,
    );
    debugPrint('[ReportApi] 响应: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is List) {
        return (body['data'] as List)
            .map((e) => TradeReport.fromJson(Map<String, dynamic>.from(e)))
            .toList();
      }
    }
    throw Exception('获取举报记录失败');
  }
}
