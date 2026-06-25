import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/order_models.dart';
import '../models/ajax_result.dart';
import 'session.dart';

class OrderApi {
  static final OrderApi instance = OrderApi._();
  OrderApi._();

  final _session = Session.instance;

  Future<AjaxResult> createOrder(OrderCreateRequest request) async {
    debugPrint('[OrderApi] 创建订单: POST /app/order/create');
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/order/create'),
      headers: _session.headers,
      body: jsonEncode(request.toJson()),
    );
    debugPrint('[OrderApi] 创建结果: ${response.statusCode} ${response.body}');
    return AjaxResult.fromJson(jsonDecode(response.body));
  }

  Future<OrderListResult> getMyBuyOrders({int pageNum = 1, int pageSize = 20}) async {
    return _getOrders('/app/order/myBuy', pageNum, pageSize);
  }

  Future<OrderListResult> getMySellOrders({int pageNum = 1, int pageSize = 20}) async {
    return _getOrders('/app/order/mySell', pageNum, pageSize);
  }

  Future<OrderListResult> _getOrders(String path, int pageNum, int pageSize) async {
    final uri = Uri.parse('${Session.baseUrl}$path').replace(
      queryParameters: {'pageNum': pageNum.toString(), 'pageSize': pageSize.toString()},
    );
    debugPrint('[OrderApi] 订单列表: GET $uri');
    final response = await http.get(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return OrderListResult(
          rows: (body['rows'] as List)
              .map((e) => TradeOrder.fromJson(Map<String, dynamic>.from(e)))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return OrderListResult(rows: []);
  }

  Future<TradeOrder?> getOrderDetail(int orderId) async {
    debugPrint('[OrderApi] 订单详情: GET /app/order/detail/$orderId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/order/detail/$orderId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return TradeOrder.fromJson(Map<String, dynamic>.from(body['data']));
      }
    }
    return null;
  }

  Future<bool> cancelOrder(int orderId) async {
    debugPrint('[OrderApi] 取消订单: PUT /app/order/cancel/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/cancel/$orderId'),
      headers: _session.headers,
    );
    debugPrint('[OrderApi] 取消结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<String?> payOrder(int orderId) async {
    debugPrint('[OrderApi] 支付: POST /app/order/pay/$orderId');
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/order/pay/$orderId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        final result = AjaxResult.fromJson(Map<String, dynamic>.from(body));
        if (result.data is String) return result.data;
      }
    }
    return null;
  }

  Future<AppOrderPayResultVo?> getPayResult(int orderId) async {
    debugPrint('[OrderApi] 支付结果: GET /app/order/payResult/$orderId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/order/payResult/$orderId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return AppOrderPayResultVo.fromJson(
            Map<String, dynamic>.from(body['data']));
      }
    }
    return null;
  }

  Future<bool> applyRefund(int orderId, String reason) async {
    debugPrint('[OrderApi] 申请退款: PUT /app/order/refund/apply/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/refund/apply/$orderId'),
      headers: _session.headers,
      body: jsonEncode({'reason': reason}),
    );
    debugPrint('[OrderApi] 申请退款结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<bool> agreeRefund(int orderId) async {
    debugPrint('[OrderApi] 同意退款: PUT /app/order/refund/agree/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/refund/agree/$orderId'),
      headers: _session.headers,
    );
    debugPrint('[OrderApi] 同意退款结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<bool> rejectRefund(int orderId) async {
    debugPrint('[OrderApi] 拒绝退款: PUT /app/order/refund/reject/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/refund/reject/$orderId'),
      headers: _session.headers,
    );
    debugPrint('[OrderApi] 拒绝退款结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<bool> sellerRefund(int orderId, String reason) async {
    debugPrint('[OrderApi] 卖家主动退款: PUT /app/order/refund/seller/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/refund/seller/$orderId'),
      headers: _session.headers,
      body: jsonEncode({'reason': reason}),
    );
    debugPrint('[OrderApi] 卖家主动退款结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<bool> finishOrder(int orderId) async {
    debugPrint('[OrderApi] 完成订单: PUT /app/order/finish/$orderId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/order/finish/$orderId'),
      headers: _session.headers,
    );
    debugPrint('[OrderApi] 完成结果: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }
}
