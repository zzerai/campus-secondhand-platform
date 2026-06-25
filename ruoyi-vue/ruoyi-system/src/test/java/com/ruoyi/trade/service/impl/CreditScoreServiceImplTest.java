package com.ruoyi.trade.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.trade.domain.TrCreditLog;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.vo.CreditApplyResult;
import com.ruoyi.trade.mapper.TrCreditLogMapper;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.service.IOnlineUserKicker;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 信用分引擎单测：加减分 / 下穿封禁升级 / 幂等 / 钳制 / 自动解禁。
 */
@ExtendWith(MockitoExtension.class)
class CreditScoreServiceImplTest
{
    @Mock
    private TrCreditLogMapper trCreditLogMapper;

    @Mock
    private TrStudentUserMapper trStudentUserMapper;

    @Mock
    private ISysConfigService sysConfigService;

    @Mock
    private IOnlineUserKicker onlineUserKicker;

    @InjectMocks
    private CreditScoreServiceImpl creditScoreService;

    private TrStudentUser student(int score, String status)
    {
        TrStudentUser u = new TrStudentUser();
        u.setUserId(7L);
        u.setCreditScore((long) score);
        u.setStatus(status);
        return u;
    }

    /** 普通扣分、未跌破阈值：只更新分值，不封禁。 */
    @Test
    void deductWithoutCrossingJustUpdatesScore()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(90, "0"));

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -10, CreditConstants.BIZ_ADMIN, null, "测试");

        Assertions.assertTrue(r.isApplied());
        Assertions.assertEquals(80, r.getScoreAfter());
        Assertions.assertEquals(CreditApplyResult.BAN_NONE, r.getBanType());
        verify(trStudentUserMapper).updateCreditScore(7L, 80);
        verify(trStudentUserMapper, never()).updateStatus(anyLong(), anyString());
        verify(onlineUserKicker, never()).kickByUserId(anyLong());
    }

    /** 首次下穿60 → 临时封禁7天、status=1、踢下线。 */
    @Test
    void firstCrossingTriggersSevenDayBan()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(65, "0"));
        when(trCreditLogMapper.countAutoBanByUser(7L)).thenReturn(0);

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -10, CreditConstants.BIZ_ADMIN, null, "扣分");

        Assertions.assertEquals(CreditApplyResult.BAN_TEMP, r.getBanType());
        Assertions.assertNotNull(r.getBanUntil());
        verify(trStudentUserMapper).updateStatus(7L, CreditConstants.STATUS_TEMP_BAN);
        verify(onlineUserKicker).kickByUserId(7L);

        ArgumentCaptor<TrCreditLog> captor = ArgumentCaptor.forClass(TrCreditLog.class);
        verify(trCreditLogMapper, times(2)).insertTrCreditLog(captor.capture());
        TrCreditLog banLog = captor.getAllValues().get(1);
        Assertions.assertEquals(CreditConstants.TYPE_AUTO_BAN, banLog.getChangeType());
        Assertions.assertNotNull(banLog.getBanUntil());
    }

    /** 第二次下穿 → 30天临时封禁。 */
    @Test
    void secondCrossingTriggersThirtyDayBan()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(62, "0"));
        when(trCreditLogMapper.countAutoBanByUser(7L)).thenReturn(1);

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -5, CreditConstants.BIZ_ADMIN, null, "再扣");

        Assertions.assertEquals(CreditApplyResult.BAN_TEMP, r.getBanType());
        Assertions.assertNotNull(r.getBanUntil());
        verify(trStudentUserMapper).updateStatus(7L, CreditConstants.STATUS_TEMP_BAN);
    }

    /** 第三次下穿 → 永久封禁、status=2、banUntil 为 null。 */
    @Test
    void thirdCrossingTriggersPermanentBan()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(61, "0"));
        when(trCreditLogMapper.countAutoBanByUser(7L)).thenReturn(2);

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -5, CreditConstants.BIZ_ADMIN, null, "三次");

        Assertions.assertEquals(CreditApplyResult.BAN_PERMANENT, r.getBanType());
        Assertions.assertNull(r.getBanUntil());
        verify(trStudentUserMapper).updateStatus(7L, CreditConstants.STATUS_PERMANENT_BAN);
        verify(onlineUserKicker).kickByUserId(7L);

        ArgumentCaptor<TrCreditLog> captor = ArgumentCaptor.forClass(TrCreditLog.class);
        verify(trCreditLogMapper, times(2)).insertTrCreditLog(captor.capture());
        Assertions.assertNull(captor.getAllValues().get(1).getBanUntil());
    }

    /** 已在阈值下继续扣分：不构成下穿，不重复封禁。 */
    @Test
    void alreadyBelowThresholdDoesNotRetriggerBan()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(50, "1"));

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -10, CreditConstants.BIZ_ADMIN, null, "继续");

        Assertions.assertEquals(CreditApplyResult.BAN_NONE, r.getBanType());
        verify(trStudentUserMapper, never()).updateStatus(anyLong(), anyString());
        verify(onlineUserKicker, never()).kickByUserId(anyLong());
    }

    /** 分值钳制到 0，不为负。 */
    @Test
    void scoreFlooredAtZero()
    {
        when(sysConfigService.selectConfigByKey(anyString())).thenReturn(null);
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(5, "1"));

        creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -20, CreditConstants.BIZ_ADMIN, null, "清零");

        verify(trStudentUserMapper).updateCreditScore(7L, 0);
    }

    /** 业务事件已处理过 → 幂等跳过，不动用户行。 */
    @Test
    void duplicateBizEventIsSkipped()
    {
        when(trCreditLogMapper.countByEvent(7L, CreditConstants.BIZ_ORDER, 123L, CreditConstants.TYPE_ORDER_COMPLETE))
                .thenReturn(1);

        CreditApplyResult r = creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ORDER_COMPLETE, 1, CreditConstants.BIZ_ORDER, 123L, "完成");

        Assertions.assertFalse(r.isApplied());
        verify(trStudentUserMapper, never()).selectStudentForUpdate(anyLong());
        verify(trStudentUserMapper, never()).updateCreditScore(anyLong(), anyInt());
        verify(trCreditLogMapper, never()).insertTrCreditLog(any());
    }

    /** 用户不存在 → 抛异常。 */
    @Test
    void missingUserThrows()
    {
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(null);

        Assertions.assertThrows(ServiceException.class, () -> creditScoreService.applyChange(
                7L, CreditConstants.TYPE_ADMIN_ADJUST, -10, CreditConstants.BIZ_ADMIN, null, "x"));
    }

    /** 到期解禁：状态置正常，分值重置到 60，写 ban_release 流水。 */
    @Test
    void releaseExpiredResetsScoreAndStatus()
    {
        when(trCreditLogMapper.selectUserIdsToRelease(any(Date.class)))
                .thenReturn(Collections.singletonList(7L));
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(20, "1"));

        int released = creditScoreService.releaseExpiredTempBans();

        Assertions.assertEquals(1, released);
        verify(trStudentUserMapper).updateCreditScore(7L, CreditConstants.RESET_SCORE);
        verify(trStudentUserMapper).updateStatus(7L, CreditConstants.STATUS_NORMAL);

        ArgumentCaptor<TrCreditLog> captor = ArgumentCaptor.forClass(TrCreditLog.class);
        verify(trCreditLogMapper).insertTrCreditLog(captor.capture());
        Assertions.assertEquals(CreditConstants.TYPE_BAN_RELEASE, captor.getValue().getChangeType());
    }

    /** 解禁二次确认：扫描命中但用户已非临时封禁（并发改状态）→ 跳过不处理。 */
    @Test
    void releaseSkipsWhenNoLongerTempBanned()
    {
        when(trCreditLogMapper.selectUserIdsToRelease(any(Date.class)))
                .thenReturn(Collections.singletonList(7L));
        when(trStudentUserMapper.selectStudentForUpdate(7L)).thenReturn(student(20, "2"));

        int released = creditScoreService.releaseExpiredTempBans();

        Assertions.assertEquals(0, released);
        verify(trStudentUserMapper, never()).updateStatus(anyLong(), anyString());
    }
}
