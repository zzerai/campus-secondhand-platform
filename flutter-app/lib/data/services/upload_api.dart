import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:cross_file/cross_file.dart';
import 'dart:io';
import 'session.dart';

class UploadApi {
  static final UploadApi instance = UploadApi._();
  UploadApi._();

  final _session = Session.instance;

  Future<Map<String, String>?> uploadChatImage(dynamic imageFile) async {
    final request = http.MultipartRequest(
      'POST',
      Uri.parse('${Session.baseUrl}/app/upload/image'),
    );
    if (_session.token != null) {
      request.headers['Authorization'] = 'Bearer ${_session.token}';
    }
    if (imageFile is XFile) {
      final bytes = await imageFile.readAsBytes();
      request.files.add(http.MultipartFile.fromBytes(
        'file', bytes, filename: imageFile.name,
      ));
    } else if (imageFile is File) {
      request.files.add(
        await http.MultipartFile.fromPath('file', imageFile.path),
      );
    }
    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    if (response.statusCode == 200) {
      final result = jsonDecode(response.body);
      if (result is Map) {
        final url = result['url']?.toString();
        final fileName = result['fileName']?.toString();
        if (fileName != null && fileName.isNotEmpty) {
          return {'url': url ?? '', 'fileName': fileName};
        }
      }
    }
    return null;
  }

  Future<String?> uploadImage(dynamic imageFile) async {
    final request = http.MultipartRequest(
      'POST',
      Uri.parse('${Session.baseUrl}/app/upload/image'),
    );
    if (_session.token != null) {
      request.headers['Authorization'] = 'Bearer ${_session.token}';
    }

    if (imageFile is XFile) {
      final bytes = await imageFile.readAsBytes();
      request.files.add(http.MultipartFile.fromBytes(
        'file', bytes, filename: imageFile.name,
      ));
    } else if (imageFile is File) {
      request.files.add(
        await http.MultipartFile.fromPath('file', imageFile.path),
      );
    }

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode == 200) {
      final result = jsonDecode(response.body);
      if (result is Map && result['url'] != null) {
        return result['url'].toString();
      }
    }
    return null;
  }

  Future<List<String>> uploadImages(List<dynamic> imageFiles) async {
    debugPrint('[UploadApi] 批量上传 ${imageFiles.length} 张图片');

    final request = http.MultipartRequest(
      'POST',
      Uri.parse('${Session.baseUrl}/app/upload/images'),
    );
    if (_session.token != null) {
      request.headers['Authorization'] = 'Bearer ${_session.token}';
    }
    for (final file in imageFiles) {
      if (file is XFile) {
        final bytes = await file.readAsBytes();
        request.files.add(http.MultipartFile.fromBytes(
          'files', bytes, filename: file.name,
        ));
      } else if (file is File) {
        request.files.add(
          await http.MultipartFile.fromPath('files', file.path),
        );
      }
    }

    try {
      final streamedResponse = await request.send();
      final response = await http.Response.fromStream(streamedResponse);
      debugPrint('[UploadApi] 响应: ${response.statusCode}');

      if (response.statusCode == 200) {
        final result = jsonDecode(response.body);
        if (result is Map) {
          if (result['data'] is List) {
            return List<String>.from(result['data']);
          }
          if (result['urls'] is String && (result['urls'] as String).isNotEmpty) {
            return (result['urls'] as String).split(',');
          }
        }
      }
      return [];
    } catch (e) {
      debugPrint('[UploadApi] 异常: $e');
      rethrow;
    }
  }

  Future<String?> uploadAvatar(dynamic imageFile) async {
    final request = http.MultipartRequest(
      'POST',
      Uri.parse('${Session.baseUrl}/app/user/avatar'),
    );
    if (_session.token != null) {
      request.headers['Authorization'] = 'Bearer ${_session.token}';
    }

    if (imageFile is XFile) {
      final bytes = await imageFile.readAsBytes();
      request.files.add(http.MultipartFile.fromBytes(
        'file', bytes, filename: imageFile.name,
      ));
    } else if (imageFile is File) {
      request.files.add(
        await http.MultipartFile.fromPath('file', imageFile.path),
      );
    }

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode == 200) {
      final result = jsonDecode(response.body);
      if (result is Map) {
        final url = result['url']?.toString();
        if (url != null && url.isNotEmpty) {
          if (_session.currentUser != null) _session.currentUser!.avatar = url;
          return url;
        }
      }
    }
    return null;
  }
}
