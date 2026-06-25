import 'package:flutter/material.dart';
import '../common/constants/app_theme.dart';

Future<String?> showDateTimePicker(
  BuildContext context, {
  String? initialTime,
}) async {
  return showDialog<String>(
    context: context,
    builder: (ctx) => Dialog(
      backgroundColor: Colors.transparent,
      insetPadding: const EdgeInsets.symmetric(horizontal: 24),
      child: _DateTimePickerDialog(initialTime: initialTime),
    ),
  );
}

class _DateTimePickerDialog extends StatefulWidget {
  final String? initialTime;

  const _DateTimePickerDialog({this.initialTime});

  @override
  State<_DateTimePickerDialog> createState() => _DateTimePickerDialogState();
}

class _DateTimePickerDialogState extends State<_DateTimePickerDialog> {
  late DateTime _selectedDate;
  late int _selectedHour;
  late int _selectedMinute;

  FixedExtentScrollController? _dateController;
  FixedExtentScrollController? _hourController;
  FixedExtentScrollController? _minuteController;

  List<DateTime> _availableDates = [];
  final List<int> _hours = List.generate(24, (i) => i);
  final List<int> _minutes = List.generate(60, (i) => i);

  @override
  void initState() {
    super.initState();
    _initAvailableDates();
    _initSelectedDateTime();
    
    // 初始化控制器
    final dateIndex = _availableDates.indexWhere((d) => 
      d.year == _selectedDate.year && d.month == _selectedDate.month && d.day == _selectedDate.day);
    _dateController = FixedExtentScrollController(initialItem: dateIndex >= 0 ? dateIndex : 0);
    _hourController = FixedExtentScrollController(initialItem: _selectedHour);
    _minuteController = FixedExtentScrollController(initialItem: _selectedMinute);
  }

  void _initAvailableDates() {
    final now = DateTime.now();
    // 只能预约今天及以后 7 天内
    _availableDates = List.generate(7, (i) => DateTime(now.year, now.month, now.day).add(Duration(days: i)));
  }

  void _initSelectedDateTime() {
    if (widget.initialTime != null && widget.initialTime!.isNotEmpty) {
      try {
        final dt = DateTime.parse(widget.initialTime!);
        _selectedDate = DateTime(dt.year, dt.month, dt.day);
        _selectedHour = dt.hour;
        _selectedMinute = dt.minute;
      } catch (_) {
        _setDefaultDateTime();
      }
    } else {
      _setDefaultDateTime();
    }
    
    // 校验选择的时间是否合法（不能是过去的时间）
    _ensureFutureTime();
  }

  void _setDefaultDateTime() {
    final now = DateTime.now();
    // 默认明天下午 2 点，避开当前时间的校验
    _selectedDate = DateTime(now.year, now.month, now.day).add(const Duration(days: 1));
    _selectedHour = 14;
    _selectedMinute = 0;
  }

  void _ensureFutureTime() {
    final now = DateTime.now();
    final selectedFull = DateTime(
      _selectedDate.year,
      _selectedDate.month,
      _selectedDate.day,
      _selectedHour,
      _selectedMinute,
    );

    if (selectedFull.isBefore(now)) {
      // 如果选的是今天且时间已过，自动调整到当前小时的下一刻或明天
      if (_selectedDate.day == now.day && _selectedDate.month == now.month && _selectedDate.year == now.year) {
        if (now.hour < 23) {
          _selectedHour = now.hour + 1;
          _selectedMinute = 0;
        } else {
          _selectedDate = _selectedDate.add(const Duration(days: 1));
          _selectedHour = 9;
          _selectedMinute = 0;
        }
      }
    }
  }

  @override
  void dispose() {
    _dateController?.dispose();
    _hourController?.dispose();
    _minuteController?.dispose();
    super.dispose();
  }

  bool _isTimeDisabled(int hour, int minute) {
    final now = DateTime.now();
    final isToday = _selectedDate.year == now.year &&
        _selectedDate.month == now.month &&
        _selectedDate.day == now.day;
    
    if (!isToday) return false;
    
    if (hour < now.hour) return true;
    if (hour == now.hour && minute <= now.minute) return true;
    
    return false;
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final diff = date.difference(today).inDays;
    
    if (diff == 0) return '今天';
    if (diff == 1) return '明天';
    if (diff == 2) return '后天';
    
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return '${date.month}月${date.day}日 ${weekdays[date.weekday % 7]}';
  }

  void _confirm() {
    final dt = DateTime(
      _selectedDate.year,
      _selectedDate.month,
      _selectedDate.day,
      _selectedHour,
      _selectedMinute,
    );

    if (dt.isBefore(DateTime.now())) {
      // 这里的 toast 逻辑通常在外部处理，或者这里直接拦截
      return;
    }
    
    final formatted = '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')} '
        '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}:00';
    
    Navigator.pop(context, formatted);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(maxWidth: 400),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppColors.radiusLg),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildHeader(),
          const Divider(height: 1, color: AppColors.divider),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 20),
            child: _buildPickerArea(),
          ),
          const Divider(height: 1, color: AppColors.divider),
          _buildActionButtons(),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 12, 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text(
            '选择预约时间',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: AppColors.fg,
            ),
          ),
          IconButton(
            onPressed: () => Navigator.pop(context),
            icon: const Icon(Icons.close, size: 22, color: AppColors.muted),
            padding: EdgeInsets.zero,
            constraints: const BoxConstraints(),
          ),
        ],
      ),
    );
  }

  Widget _buildPickerArea() {
    return Container(
      height: 180,
      child: Stack(
        children: [
          // 选中框背景
          Center(
            child: Container(
              height: 44,
              decoration: BoxDecoration(
                color: AppColors.accent.withOpacity(0.05),
                borderRadius: BorderRadius.circular(AppColors.radiusSm),
              ),
            ),
          ),
          Row(
            children: [
              Expanded(flex: 3, child: _buildDatePicker()),
              _buildDivider(),
              Expanded(flex: 2, child: _buildHourPicker()),
              const Text(':', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AppColors.accent)),
              Expanded(flex: 2, child: _buildMinutePicker()),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildDivider() {
    return Container(
      width: 1,
      height: 40,
      margin: const EdgeInsets.symmetric(horizontal: 8),
      color: AppColors.divider,
    );
  }

  Widget _buildDatePicker() {
    return ListWheelScrollView.useDelegate(
      controller: _dateController,
      itemExtent: 44,
      perspective: 0.005,
      physics: const FixedExtentScrollPhysics(),
      childDelegate: ListWheelChildBuilderDelegate(
        builder: (context, index) {
          final date = _availableDates[index];
          final isSelected = date.year == _selectedDate.year &&
              date.month == _selectedDate.month &&
              date.day == _selectedDate.day;
          
          return Center(
            child: Text(
              _formatDate(date),
              style: TextStyle(
                fontSize: isSelected ? 16 : 14,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w400,
                color: isSelected ? AppColors.accent : AppColors.fg,
              ),
            ),
          );
        },
        childCount: _availableDates.length,
      ),
      onSelectedItemChanged: (index) {
        setState(() {
          _selectedDate = _availableDates[index];
          // 日期变化后，可能原本合法的时间变得不合法（比如今天刚过的时间）
          if (_isTimeDisabled(_selectedHour, _selectedMinute)) {
            final now = DateTime.now();
            _selectedHour = now.hour + 1;
            if (_selectedHour > 23) {
              _selectedHour = 23;
              _selectedMinute = 59;
            }
            _hourController?.animateToItem(_selectedHour, duration: const Duration(milliseconds: 200), curve: Curves.easeOut);
          }
        });
      },
    );
  }

  Widget _buildHourPicker() {
    return ListWheelScrollView.useDelegate(
      controller: _hourController,
      itemExtent: 44,
      perspective: 0.005,
      physics: const FixedExtentScrollPhysics(),
      childDelegate: ListWheelChildBuilderDelegate(
        builder: (context, index) {
          final hour = _hours[index];
          final isSelected = hour == _selectedHour;
          final isDisabled = _isTimeDisabled(hour, 0); // 小时维度的初步禁用判断
          
          return Center(
            child: Text(
              hour.toString().padLeft(2, '0'),
              style: TextStyle(
                fontSize: isSelected ? 20 : 16,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w400,
                color: isDisabled 
                  ? AppColors.placeholder 
                  : (isSelected ? AppColors.accent : AppColors.fg),
              ),
            ),
          );
        },
        childCount: _hours.length,
      ),
      onSelectedItemChanged: (index) {
        setState(() {
          _selectedHour = _hours[index];
        });
      },
    );
  }

  Widget _buildMinutePicker() {
    return ListWheelScrollView.useDelegate(
      controller: _minuteController,
      itemExtent: 44,
      perspective: 0.005,
      physics: const FixedExtentScrollPhysics(),
      childDelegate: ListWheelChildBuilderDelegate(
        builder: (context, index) {
          final minute = _minutes[index];
          final isSelected = minute == _selectedMinute;
          final isDisabled = _isTimeDisabled(_selectedHour, minute);
          
          return Center(
            child: Text(
              minute.toString().padLeft(2, '0'),
              style: TextStyle(
                fontSize: isSelected ? 20 : 16,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w400,
                color: isDisabled 
                  ? AppColors.placeholder 
                  : (isSelected ? AppColors.accent : AppColors.fg),
              ),
            ),
          );
        },
        childCount: _minutes.length,
      ),
      onSelectedItemChanged: (index) {
        setState(() {
          _selectedMinute = _minutes[index];
        });
      },
    );
  }

  Widget _buildActionButtons() {
    final now = DateTime.now();
    final selectedFull = DateTime(
      _selectedDate.year,
      _selectedDate.month,
      _selectedDate.day,
      _selectedHour,
      _selectedMinute,
    );
    final isValid = !selectedFull.isBefore(now);

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: TextButton(
              onPressed: () => Navigator.pop(context),
              style: TextButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusPill),
                  side: const BorderSide(color: AppColors.border),
                ),
              ),
              child: const Text(
                '取消',
                style: TextStyle(color: AppColors.muted, fontSize: 16, fontWeight: FontWeight.w600),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: ElevatedButton(
              onPressed: isValid ? _confirm : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.accent,
                foregroundColor: Colors.white,
                elevation: 0,
                padding: const EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(AppColors.radiusPill),
                ),
              ),
              child: const Text(
                '确认',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
