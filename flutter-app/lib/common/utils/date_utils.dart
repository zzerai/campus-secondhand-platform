String formatDateTime(String? timeStr) {
  if (timeStr == null || timeStr.isEmpty) return '';
  try {
    // 尝试解析 ISO 格式或其他标准格式
    DateTime dt = DateTime.parse(timeStr);
    String y = dt.year.toString();
    String m = dt.month.toString().padLeft(2, '0');
    String d = dt.day.toString().padLeft(2, '0');
    String hh = dt.hour.toString().padLeft(2, '0');
    String mm = dt.minute.toString().padLeft(2, '0');
    return '$y-$m-$d $hh:$mm';
  } catch (e) {
    // 解析失败，可能是已经手动处理过的非标准字符串
    // 如果长度足够，尝试通过截断处理
    if (timeStr.length >= 16) {
      // 检查是否包含 T 标识（ISO）
      String res = timeStr.substring(0, 16).replaceAll('T', ' ');
      // 进一步确保格式是 yyyy-MM-dd HH:mm
      if (RegExp(r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$').hasMatch(res)) {
        return res;
      }
    }
    return timeStr;
  }
}
