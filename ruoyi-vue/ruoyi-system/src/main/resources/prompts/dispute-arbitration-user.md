请对以下校园二手交易争议进行仲裁分析：

=== 争议信息 ===
争议类型：{{disputeType}}
争议描述：{{disputeContent}}

=== 订单信息 ===
订单ID：{{orderId}}
交易价格：{{tradePrice}}
订单状态：{{orderStatus}}

=== 商品信息 ===
{{goodsInfo}}

请返回 JSON 格式（不要包含其他文字）：
{
  "arbitrateLevel": "buyer/buyer_偏买家/seller/seller_偏卖家/双方责任",
  "suggestion": "退款给买家/买家确认收货/双方协商/人工介入",
  "reason": "仲裁理由，结合争议内容、订单信息、商品信息综合分析"
}
