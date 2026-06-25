import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/evaluation_model.dart';
import 'session.dart';

class EvaluationApi {
  static final EvaluationApi instance = EvaluationApi._();
  EvaluationApi._();

  final _session = Session.instance;

  Future<EvaluationScoreSummary> getMyScore() async {
    debugPrint('[EvaluationApi] 评价概览: GET /app/evaluation/myScore');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/evaluation/myScore'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return EvaluationScoreSummary.fromJson(
            Map<String, dynamic>.from(body['data']));
      }
    }
    return EvaluationScoreSummary(
        averageScore: 0, totalReceived: 0, totalSent: 0, distribution: {});
  }

  Future<EvaluationListResult> getReceived({int pageNum = 1, int pageSize = 20}) async {
    return _getList('/app/evaluation/received', pageNum, pageSize);
  }

  Future<EvaluationListResult> getSent({int pageNum = 1, int pageSize = 20}) async {
    return _getList('/app/evaluation/sent', pageNum, pageSize);
  }

  Future<EvaluationListResult> _getList(String path, int pageNum, int pageSize) async {
    final uri = Uri.parse('${Session.baseUrl}$path').replace(
      queryParameters: {'pageNum': pageNum.toString(), 'pageSize': pageSize.toString()},
    );
    debugPrint('[EvaluationApi] 评价列表: GET $uri');
    final response = await http.get(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return EvaluationListResult(
          rows: (body['rows'] as List)
              .map((e) => TradeEvaluation.fromJson(Map<String, dynamic>.from(e)))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return EvaluationListResult(rows: []);
  }

  Future<bool> submit(SubmitEvaluationRequest request) async {
    debugPrint('[EvaluationApi] 提交评价: POST /app/evaluation/submit');
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/evaluation/submit'),
      headers: _session.headers,
      body: jsonEncode(request.toJson()),
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      return body is Map && (body['code'] == 200 || body['success'] == true);
    }
    return false;
  }

  Future<bool> checkEvaluated(int orderId) async {
    debugPrint('[EvaluationApi] 检查评价状态: GET /app/evaluation/check/$orderId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/evaluation/check/$orderId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return body['data']['evaluated'] == true;
      }
    }
    return false;
  }
}

class EvaluationListResult {
  final List<TradeEvaluation> rows;
  final int total;
  EvaluationListResult({required this.rows, this.total = 0});
}
