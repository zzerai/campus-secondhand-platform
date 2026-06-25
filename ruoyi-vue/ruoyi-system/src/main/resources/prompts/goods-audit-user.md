请对以下校园二手商品进行审核分析：

商品标题：{{title}}
商品分类：{{category}}
出售价格：{{price}}
原价：{{originalPrice}}
新旧程度：{{quality}}
商品描述：{{description}}
交易地点：{{tradePlace}}

请返回 JSON 格式（不要包含其他文字）：
{
  "riskLevel": "low/middle/high",
  "suggestion": "通过/拒绝/人工复核",
  "riskReason": "具体的风险原因或审核依据"
}
