import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/ajax_result.dart';
import '../models/trade_dispute.dart';
import 'session.dart';

class DisputeApi {
  static final DisputeApi instance = DisputeApi._();
  DisputeApi._();

  final _session = Session.instance;

  Future<AjaxResult> submitDispute({
    required int orderId,
    required String disputeType,
    required String disputeContent,
    String? evidenceImages,
  }) async {
    final body = <String, dynamic>{
      'orderId': orderId,
      'disputeType': disputeType,
      'disputeContent': disputeContent,
    };
    if (evidenceImages != null && evidenceImages.isNotEmpty) {
      body['evidenceImages'] = evidenceImages;
    }

    debugPrint('[DisputeApi] 提交争议: POST /app/dispute/submit');
    debugPrint('[DisputeApi] body: ${jsonEncode(body)}');

    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/dispute/submit'),
      headers: _session.headers,
      body: jsonEncode(body),
    );
    debugPrint('[DisputeApi] 响应: ${response.statusCode} ${response.body}');
    return AjaxResult.fromJson(jsonDecode(response.body));
  }

  Future<List<TradeDispute>> getMyDisputes() async {
    debugPrint('[DisputeApi] 我的争议列表: GET /app/dispute/myList');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/dispute/myList'),
      headers: _session.headers,
    );
    debugPrint('[DisputeApi] 响应: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is List) {
        return (body['data'] as List)
            .map((e) => TradeDispute.fromJson(Map<String, dynamic>.from(e)))
            .toList();
      }
    }
    throw Exception('获取争议记录失败');
  }
}
