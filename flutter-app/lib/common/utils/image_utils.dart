import '../../data/services/session.dart';

String ensureAbsoluteUrl(String url) {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  return '${Session.baseUrl}$url';
}

String? firstImageUrl(String images) {
  if (images.isEmpty) return null;
  final part = images.split(',').first.trim();
  if (part.isEmpty) return null;
  return ensureAbsoluteUrl(part);
}
