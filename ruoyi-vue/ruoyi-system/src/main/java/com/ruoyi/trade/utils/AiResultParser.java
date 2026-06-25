package com.ruoyi.trade.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI 文本响应解析工具：从大模型返回的文本中截取并解析 JSON。
 *
 * @author thr
 */
public final class AiResultParser
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AiResultParser()
    {
    }

    /**
     * 解析 AI 返回文本中的 JSON 对象。
     * 截取首个 '{' 到末个 '}' 之间的片段，兼容返回夹带 markdown 代码块或额外说明文字的情况。
     *
     * @param aiResult AI 原始返回文本
     * @return 解析后的 JSON 节点
     * @throws JsonProcessingException 截取后的内容不是合法 JSON
     */
    public static JsonNode parse(String aiResult) throws JsonProcessingException
    {
        return OBJECT_MAPPER.readTree(extractJson(aiResult));
    }

    private static String extractJson(String text)
    {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
