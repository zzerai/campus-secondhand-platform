package com.ruoyi.trade.service.impl;

import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.mapper.TrStudentUserMapper;

/**
 * 学生用户 Service 单元测试
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrStudentUserServiceImplTest
{
    private static final String FAKE_ADMIN = "admin";

    @Mock
    private TrStudentUserMapper trStudentUserMapper;

    @InjectMocks
    private TrStudentUserServiceImpl trStudentUserService;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName(FAKE_ADMIN);
        LoginUser loginUser = new LoginUser(1L, 1L, sysUser, new HashSet<>(Collections.singletonList("*:*:*")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, "", loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void selectListShouldDelegateToMapper()
    {
        TrStudentUser query = new TrStudentUser();
        when(trStudentUserMapper.selectTrStudentUserList(query)).thenReturn(Collections.emptyList());

        Assertions.assertTrue(trStudentUserService.selectTrStudentUserList(query).isEmpty());
        verify(trStudentUserMapper).selectTrStudentUserList(query);
    }

    @Test
    void insertShouldEncryptPasswordAndStampAuditFields()
    {
        TrStudentUser user = new TrStudentUser();
        user.setStudentNo("2026001");
        user.setPhone("13800000001");
        user.setPassword("plain-pwd");

        when(trStudentUserMapper.selectAnyByStudentNo("2026001")).thenReturn(null);
        when(trStudentUserMapper.selectAnyByPhone("13800000001")).thenReturn(null);
        when(trStudentUserMapper.insertTrStudentUser(user)).thenReturn(1);

        int rows = trStudentUserService.insertTrStudentUser(user);

        Assertions.assertEquals(1, rows);
        Assertions.assertNotEquals("plain-pwd", user.getPassword(), "password should be encrypted");
        Assertions.assertTrue(SecurityUtils.matchesPassword("plain-pwd", user.getPassword()),
                "encrypted password should match original via BCrypt");
        Assertions.assertEquals(FAKE_ADMIN, user.getCreateBy());
        Assertions.assertNotNull(user.getCreateTime());
    }

    @Test
    void insertShouldRejectDuplicateStudentNo()
    {
        TrStudentUser existing = new TrStudentUser();
        existing.setUserId(99L);
        existing.setStudentNo("2026001");
        when(trStudentUserMapper.selectAnyByStudentNo("2026001")).thenReturn(existing);

        TrStudentUser dup = new TrStudentUser();
        dup.setStudentNo("2026001");
        dup.setPassword("x");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trStudentUserService.insertTrStudentUser(dup));
        Assertions.assertTrue(ex.getMessage().contains("学号已存在"));
        verify(trStudentUserMapper, never()).insertTrStudentUser(dup);
    }

    @Test
    void insertShouldRejectDuplicatePhone()
    {
        when(trStudentUserMapper.selectAnyByStudentNo("2026002")).thenReturn(null);
        TrStudentUser existingPhone = new TrStudentUser();
        existingPhone.setUserId(88L);
        existingPhone.setPhone("13800000002");
        when(trStudentUserMapper.selectAnyByPhone("13800000002")).thenReturn(existingPhone);

        TrStudentUser dup = new TrStudentUser();
        dup.setStudentNo("2026002");
        dup.setPhone("13800000002");
        dup.setPassword("x");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trStudentUserService.insertTrStudentUser(dup));
        Assertions.assertTrue(ex.getMessage().contains("手机号已存在"));
        verify(trStudentUserMapper, never()).insertTrStudentUser(dup);
    }

    @Test
    void updateShouldNotChangePasswordAndStampUpdateBy()
    {
        TrStudentUser user = new TrStudentUser();
        user.setUserId(10L);
        user.setStudentNo("2026003");
        user.setPassword("attacker-tries-this");

        when(trStudentUserMapper.selectAnyByStudentNo("2026003")).thenReturn(null);
        when(trStudentUserMapper.updateTrStudentUser(user)).thenReturn(1);

        int rows = trStudentUserService.updateTrStudentUser(user);

        Assertions.assertEquals(1, rows);
        Assertions.assertNull(user.getPassword(), "update must not propagate password — use resetPwd instead");
        Assertions.assertEquals(FAKE_ADMIN, user.getUpdateBy());
        Assertions.assertNotNull(user.getUpdateTime());
    }

    @Test
    void updateUserStatusShouldOnlyTouchStatusAuditFields()
    {
        TrStudentUser input = new TrStudentUser();
        input.setUserId(20L);
        input.setStatus("1");

        ArgumentCaptor<TrStudentUser> captor = ArgumentCaptor.forClass(TrStudentUser.class);
        when(trStudentUserMapper.updateTrStudentUser(captor.capture())).thenReturn(1);

        int rows = trStudentUserService.updateUserStatus(input);

        Assertions.assertEquals(1, rows);
        TrStudentUser sent = captor.getValue();
        Assertions.assertEquals(20L, sent.getUserId());
        Assertions.assertEquals("1", sent.getStatus());
        Assertions.assertEquals(FAKE_ADMIN, sent.getUpdateBy());
        Assertions.assertNotNull(sent.getUpdateTime());
        Assertions.assertNull(sent.getPassword());
        Assertions.assertNull(sent.getStudentNo());
        Assertions.assertNull(sent.getPhone());
    }

    @Test
    void resetPwdShouldOnlyTouchPasswordAuditFields()
    {
        TrStudentUser input = new TrStudentUser();
        input.setUserId(30L);
        input.setPassword(SecurityUtils.encryptPassword("new-secret"));

        ArgumentCaptor<TrStudentUser> captor = ArgumentCaptor.forClass(TrStudentUser.class);
        when(trStudentUserMapper.updateTrStudentUser(captor.capture())).thenReturn(1);

        int rows = trStudentUserService.resetPwd(input);

        Assertions.assertEquals(1, rows);
        TrStudentUser sent = captor.getValue();
        Assertions.assertEquals(30L, sent.getUserId());
        Assertions.assertNotNull(sent.getPassword());
        Assertions.assertTrue(SecurityUtils.matchesPassword("new-secret", sent.getPassword()));
        Assertions.assertEquals(FAKE_ADMIN, sent.getUpdateBy());
        Assertions.assertNotNull(sent.getUpdateTime());
        Assertions.assertNull(sent.getStudentNo());
        Assertions.assertNull(sent.getStatus());
    }

    @Test
    void checkStudentNoUniqueAllowsSameRecordEdit()
    {
        TrStudentUser existing = new TrStudentUser();
        existing.setUserId(7L);
        existing.setStudentNo("2026004");
        when(trStudentUserMapper.selectAnyByStudentNo("2026004")).thenReturn(existing);

        TrStudentUser editing = new TrStudentUser();
        editing.setUserId(7L);
        editing.setStudentNo("2026004");

        Assertions.assertTrue(trStudentUserService.checkStudentNoUnique(editing),
                "editing the same record should be considered unique");

        TrStudentUser other = new TrStudentUser();
        other.setUserId(8L);
        other.setStudentNo("2026004");
        Assertions.assertFalse(trStudentUserService.checkStudentNoUnique(other),
                "different userId with same studentNo should be considered duplicate");
    }
}
