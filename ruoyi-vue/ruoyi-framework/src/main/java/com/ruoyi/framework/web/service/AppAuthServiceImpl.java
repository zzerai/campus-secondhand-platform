package com.ruoyi.framework.web.service;

import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.AppLoginBody;
import com.ruoyi.common.core.domain.model.AppTokenInfo;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.UserException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.dto.AppChangePhoneDto;
import com.ruoyi.trade.domain.dto.AppChangePwdDto;
import com.ruoyi.trade.domain.dto.AppProfileUpdateDto;
import com.ruoyi.trade.domain.dto.AppRegisterDto;
import com.ruoyi.trade.domain.dto.AppResetPwdDto;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.service.sms.SmsCodeService;
import com.ruoyi.trade.service.sms.SmsScene;

/**
 * 移动端认证Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-11
 */
@Service
public class AppAuthServiceImpl implements AppAuthService
{
    private static final Logger log = LoggerFactory.getLogger(AppAuthServiceImpl.class);

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SmsCodeService smsCodeService;

    /**
     * 学生用户登录。
     *
     * <p>支持两种模式：</p>
     * <ul>
     *   <li>{@code password} 非空：账号（学号/手机号）+ 密码登录，走 BCrypt 校验</li>
     *   <li>{@code password} 为空且 {@code code} 非空：手机号 + 短信验证码登录，
     *       要求 {@code username} 必须是手机号格式且账号已注册</li>
     * </ul>
     */
    @Override
    public AppTokenInfo login(AppLoginBody loginBody)
    {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String code = loginBody.getCode();

        if (StringUtils.isEmpty(username))
        {
            throw new UserException("user.password.not.match", null);
        }

        TrStudentUser studentUser;
        if (StringUtils.isNotEmpty(password))
        {
            studentUser = selectByPhone(username);
            if (studentUser == null)
            {
                throw new UserException("user.not.exists", null);
            }
            if (!"0".equals(studentUser.getStatus()))
            {
                throw new UserException("user.blocked", null);
            }
            if (!passwordEncoder.matches(password, studentUser.getPassword()))
            {
                throw new UserException("user.password.not.match", null);
            }
        }
        else if (StringUtils.isNotEmpty(code))
        {
            // 短信验证码登录只接受手机号，避免学号入参导致阿里云核验对象错位
            if (!PHONE_PATTERN.matcher(username).matches())
            {
                throw new UserException("user.password.not.match", new Object[] { "请使用手机号登录" });
            }
            studentUser = trStudentUserMapper.selectTrStudentUserByPhone(username);
            if (studentUser == null)
            {
                throw new UserException("user.not.exists", null);
            }
            if (!"0".equals(studentUser.getStatus()))
            {
                throw new UserException("user.blocked", null);
            }
            if (!smsCodeService.checkSmsCode(username, code))
            {
                throw new UserException("user.password.not.match", new Object[] { "验证码错误或已过期" });
            }
        }
        else
        {
            throw new UserException("user.password.not.match", null);
        }

        // 回写 last_login_time；失败仅记日志，不阻断登录主路径
        try
        {
            trStudentUserMapper.updateLastLoginTime(studentUser.getUserId(), DateUtils.getNowDate());
        }
        catch (Exception ex)
        {
            log.warn("回写 last_login_time 失败 userId={}", studentUser.getUserId(), ex);
        }

        return createAppLoginUserAndToken(studentUser);
    }

    /**
     * 注册方法。
     *
     * <p>uk_student_no / uk_phone 是全表唯一索引，软删除不释放约束，
     * 直接 insert 会被 MySQL 抛 Duplicate entry。流程：</p>
     * <ol>
     *   <li>按学号全表查（含软删除）：命中活跃行直接拒绝；命中软删除行复活并刷新业务字段。</li>
     *   <li>按手机号全表查（含软删除）：命中活跃行（且不是上一步命中的复活目标）拒绝；
     *       命中软删除行同样复活。</li>
     *   <li>若学号 / 手机号同时命中两条不同的软删除行，优先复活学号那条 ——
     *       手机号那条会因为唯一键冲突在 update 时被 MySQL 阻止；在 service 层提前
     *       检测并提示用户改用其它手机号。</li>
     *   <li>都未命中走 insert。</li>
     * </ol>
     *
     * <p>强制要求 {@code smsCode} 通过阿里云号码认证服务核验；唯一性 / 软删检查通过后再做短信核验，
     * fail-fast 节省阿里云调用量，且确保即将复活或新建的目标手机号确实归属当前操作者。</p>
     */
    @Override
    public String register(AppRegisterDto registerDto)
    {
        if (registerDto == null)
        {
            return "请求体不能为空";
        }
        if (StringUtils.isEmpty(registerDto.getStudentNo()))
        {
            return "学号不能为空";
        }
        if (StringUtils.isEmpty(registerDto.getPhone()) || !PHONE_PATTERN.matcher(registerDto.getPhone()).matches())
        {
            return "手机号格式不正确";
        }
        if (StringUtils.isEmpty(registerDto.getPassword()))
        {
            return "密码不能为空";
        }
        if (StringUtils.isEmpty(registerDto.getSmsCode()))
        {
            return "短信验证码不能为空";
        }

        TrStudentUser sameStudentNo = trStudentUserMapper.selectAnyByStudentNo(registerDto.getStudentNo());
        if (sameStudentNo != null && !"2".equals(sameStudentNo.getDelFlag()))
        {
            return "学号已存在，请更换后重试！";
        }
        TrStudentUser samePhone = trStudentUserMapper.selectAnyByPhone(registerDto.getPhone());
        // 手机号命中活跃行：除非该行就是上面学号命中的复活目标（同人换号回退场景），否则拒绝
        if (samePhone != null && !"2".equals(samePhone.getDelFlag())
                && (sameStudentNo == null || !samePhone.getUserId().equals(sameStudentNo.getUserId())))
        {
            return "手机号已注册，请直接登录！";
        }
        // 如果学号命中软删除、手机号命中另一条软删除，复活学号那条会触发 uk_phone 冲突。
        // 在 service 层提前判出，提示用户换号。
        if (sameStudentNo != null && "2".equals(sameStudentNo.getDelFlag())
                && samePhone != null && !samePhone.getUserId().equals(sameStudentNo.getUserId()))
        {
            return "手机号被历史账户占用，请更换手机号后重试";
        }

        // 唯一性 / 软删校验通过后再走阿里云短信核验，避免无效请求消耗 quota
        if (!smsCodeService.checkSmsCode(registerDto.getPhone(), registerDto.getSmsCode()))
        {
            return "短信验证码错误或已过期";
        }

        String encrypted = passwordEncoder.encode(registerDto.getPassword());

        if (sameStudentNo != null && "2".equals(sameStudentNo.getDelFlag()))
        {
            // 复活路径：把软删除行 del_flag 置 0 并重置业务字段
            int rows = trStudentUserMapper.reviveStudentUser(
                    sameStudentNo.getUserId(),
                    registerDto.getStudentNo(),
                    registerDto.getPhone(),
                    encrypted,
                    registerDto.getNickname(),
                    registerDto.getStudentNo());
            if (rows <= 0)
            {
                return "注册失败，请稍后重试！";
            }
            return null;
        }
        if (samePhone != null && "2".equals(samePhone.getDelFlag()))
        {
            // 学号无冲突，手机号命中软删除：复用该行（学号会被更新成新值）
            int rows = trStudentUserMapper.reviveStudentUser(
                    samePhone.getUserId(),
                    registerDto.getStudentNo(),
                    registerDto.getPhone(),
                    encrypted,
                    registerDto.getNickname(),
                    registerDto.getStudentNo());
            if (rows <= 0)
            {
                return "注册失败，请稍后重试！";
            }
            return null;
        }

        // 全新注册：仅从 DTO 拷贝允许字段，creditScore/status/delFlag 等一律由服务端设定
        TrStudentUser studentUser = new TrStudentUser();
        studentUser.setStudentNo(registerDto.getStudentNo());
        studentUser.setPhone(registerDto.getPhone());
        studentUser.setPassword(encrypted);
        studentUser.setNickname(registerDto.getNickname());

        prepareStudentUser(studentUser);
        // prepareStudentUser 内部会再次 BCrypt encode，因此先把已加密的密码标记为 $ 开头格式即可跳过
        // 由于 BCrypt 哈希总以 $2a$/$2b$/$2y$ 开头，prepareStudentUser 的 startsWith("$") 判断会自动跳过二次加密。
        int insert = trStudentUserMapper.insertTrStudentUser(studentUser);
        if (insert <= 0)
        {
            return "注册失败，请稍后重试！";
        }

        return null;
    }

    /**
     * 发送移动端短信验证码（注册 / 登录两个场景）。
     */
    @Override
    public String sendSmsCode(String phone, String scene)
    {
        if (StringUtils.isEmpty(phone) || !PHONE_PATTERN.matcher(phone).matches())
        {
            return "手机号格式不正确";
        }
        if (StringUtils.isEmpty(scene))
        {
            return "短信场景不能为空";
        }
        SmsScene smsScene;
        try
        {
            smsScene = SmsScene.valueOf(scene.toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            return "不支持的短信场景：" + scene;
        }

        TrStudentUser existing = trStudentUserMapper.selectTrStudentUserByPhone(phone);
        if (smsScene == SmsScene.REGISTER && existing != null)
        {
            return "手机号已注册，请直接登录！";
        }
        if (smsScene == SmsScene.LOGIN && existing == null)
        {
            return "手机号未注册，请先注册！";
        }
        if (smsScene == SmsScene.RESET_PASSWORD && existing == null)
        {
            return "手机号未注册，请先注册！";
        }
        if (smsScene == SmsScene.CHANGE_PHONE && existing != null)
        {
            return "新手机号已被使用";
        }

        try
        {
            smsCodeService.sendSmsCode(phone, smsScene);
            return null;
        }
        catch (RuntimeException ex)
        {
            return ex.getMessage() == null ? "短信发送失败" : ex.getMessage();
        }
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            tokenService.delLoginUser(token);
        }
    }

    /**
     * 根据手机号/学号查询学生用户
     */
    @Override
    public TrStudentUser selectByPhone(String username)
    {
        // 显式判空避免 Pattern.matcher(null) 抛 NPE；登录路径已先判空，此处兜底防止其它调用方传 null
        if (StringUtils.isEmpty(username))
        {
            return null;
        }
        if (PHONE_PATTERN.matcher(username).matches())
        {
            return trStudentUserMapper.selectTrStudentUserByPhone(username);
        }
        return trStudentUserMapper.selectTrStudentUserByStudentNo(username);
    }

    /**
     * 创建移动端登录用户并生成token
     */
    private AppTokenInfo createAppLoginUserAndToken(TrStudentUser studentUser)
    {
        AppTokenInfo.AppStudentUser userInfo = new AppTokenInfo.AppStudentUser();
        BeanUtils.copyProperties(studentUser, userInfo);

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(studentUser.getUserId());
        loginUser.setPermissions(new HashSet<>());
        loginUser.setUser(buildSysUserForMobile(studentUser));
        tokenService.setLoginUser(loginUser);
        String token = tokenService.createToken(loginUser);

        AppTokenInfo appTokenInfo = new AppTokenInfo(token);
        appTokenInfo.setStudentUser(userInfo);
        return appTokenInfo;
    }

    /**
     * 构建系统用户对象（用于复用LoginUser机制）
     * 不写入密码哈希，避免后续 Controller 通过 SecurityUtils 拿到敏感字段。
     */
    private SysUser buildSysUserForMobile(TrStudentUser studentUser)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(studentUser.getUserId());
        sysUser.setUserName(studentUser.getPhone());
        sysUser.setNickName(studentUser.getNickname());
        sysUser.setEmail(studentUser.getContactWay());
        sysUser.setPhonenumber(studentUser.getPhone());
        sysUser.setAvatar(studentUser.getAvatar());
        sysUser.setStatus(studentUser.getStatus());
        return sysUser;
    }

    private void prepareStudentUser(TrStudentUser user)
    {
        Date now = DateUtils.getNowDate();
        if (user.getPassword() != null && !user.getPassword().startsWith("$"))
        {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getStatus() == null)
        {
            user.setStatus("0");
        }
        if (user.getDelFlag() == null)
        {
            user.setDelFlag("0");
        }
        if (user.getCreditScore() == null)
        {
            user.setCreditScore(100L);
        }
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setCreateBy(user.getStudentNo());
        user.setUpdateBy(user.getStudentNo());
    }

    @Override
    public AppTokenInfo.AppStudentUser getUserProfile(jakarta.servlet.http.HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getUser() == null)
        {
            return null;
        }

        Long userId = loginUser.getUserId();
        TrStudentUser studentUser = trStudentUserMapper.selectTrStudentUserByUserId(userId);
        if (studentUser == null)
        {
            return null;
        }

        AppTokenInfo.AppStudentUser userInfo = new AppTokenInfo.AppStudentUser();
        BeanUtils.copyProperties(studentUser, userInfo);
        return userInfo;
    }

    @Override
    public String updateUserProfile(jakarta.servlet.http.HttpServletRequest request, AppProfileUpdateDto profileDto)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getUser() == null)
        {
            return "用户不存在或已过期";
        }

        Long userId = loginUser.getUserId();
        TrStudentUser existingUser = trStudentUserMapper.selectTrStudentUserByUserId(userId);
        if (existingUser == null)
        {
            return "用户不存在";
        }

        if (!"0".equals(existingUser.getStatus()))
        {
            return "账号已被禁用";
        }

        // 仅允许修改 avatar / nickname / contactWay，其余字段忽略
        TrStudentUser updateStudentUser = new TrStudentUser();
        updateStudentUser.setUserId(userId);
        updateStudentUser.setAvatar(profileDto.getAvatar());
        updateStudentUser.setNickname(profileDto.getNickname());
        updateStudentUser.setContactWay(profileDto.getContactWay());
        updateStudentUser.setUpdateTime(DateUtils.getNowDate());
        updateStudentUser.setUpdateBy(existingUser.getStudentNo());

        int update = trStudentUserMapper.updateTrStudentUser(updateStudentUser);
        if (update <= 0)
        {
            return "更新失败";
        }

        // 把变更字段写回 existingUser，用最终一致状态刷新 LoginUser 缓存
        existingUser.setAvatar(profileDto.getAvatar());
        existingUser.setNickname(profileDto.getNickname());
        existingUser.setContactWay(profileDto.getContactWay());

        LoginUser newLoginUser = new LoginUser();
        newLoginUser.setUserId(userId);
        newLoginUser.setPermissions(new HashSet<>());
        newLoginUser.setUser(buildSysUserForMobile(existingUser));
        newLoginUser.setToken(loginUser.getToken());
        tokenService.refreshToken(newLoginUser);

        return null;
    }

    @Override
    public Long getUserIdFromRequest(jakarta.servlet.http.HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getUser() == null)
        {
            return null;
        }
        return loginUser.getUserId();
    }

    @Override
    public String changePassword(jakarta.servlet.http.HttpServletRequest request, AppChangePwdDto changePwdDto)
    {
        if (changePwdDto == null
                || StringUtils.isEmpty(changePwdDto.getOldPassword())
                || StringUtils.isEmpty(changePwdDto.getNewPassword()))
        {
            return "新旧密码不能为空";
        }

        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getUser() == null)
        {
            return "用户未登录或登录已过期";
        }

        Long userId = loginUser.getUserId();
        TrStudentUser existingUser = trStudentUserMapper.selectTrStudentUserByUserId(userId);
        if (existingUser == null)
        {
            return "用户不存在";
        }
        if (!"0".equals(existingUser.getStatus()))
        {
            return "账号已被禁用";
        }

        if (!passwordEncoder.matches(changePwdDto.getOldPassword(), existingUser.getPassword()))
        {
            return "旧密码错误";
        }
        if (passwordEncoder.matches(changePwdDto.getNewPassword(), existingUser.getPassword()))
        {
            return "新密码不能与旧密码相同";
        }

        TrStudentUser updateStudentUser = new TrStudentUser();
        updateStudentUser.setUserId(userId);
        updateStudentUser.setPassword(passwordEncoder.encode(changePwdDto.getNewPassword()));
        updateStudentUser.setUpdateTime(DateUtils.getNowDate());
        updateStudentUser.setUpdateBy(existingUser.getStudentNo());

        int update = trStudentUserMapper.updateTrStudentUser(updateStudentUser);
        if (update <= 0)
        {
            return "修改失败";
        }

        // 改密码成功后清当前 token，强制本设备用新密码重新登录（其他设备的 token 仍按 Redis 过期时间存活）
        tokenService.delLoginUser(loginUser.getToken());
        return null;
    }

    @Override
    public String resetPassword(AppResetPwdDto resetPwdDto)
    {
        if (resetPwdDto == null)
        {
            return "请求体不能为空";
        }
        if (StringUtils.isEmpty(resetPwdDto.getPhone()) || !PHONE_PATTERN.matcher(resetPwdDto.getPhone()).matches())
        {
            return "手机号格式不正确";
        }
        if (StringUtils.isEmpty(resetPwdDto.getSmsCode()))
        {
            return "短信验证码不能为空";
        }
        if (StringUtils.isEmpty(resetPwdDto.getNewPassword()))
        {
            return "新密码不能为空";
        }

        TrStudentUser existingUser = trStudentUserMapper.selectTrStudentUserByPhone(resetPwdDto.getPhone());
        if (existingUser == null)
        {
            return "手机号未注册";
        }
        if (!"0".equals(existingUser.getStatus()))
        {
            return "账号已被禁用";
        }

        if (!smsCodeService.checkSmsCode(resetPwdDto.getPhone(), resetPwdDto.getSmsCode()))
        {
            return "短信验证码错误或已过期";
        }

        TrStudentUser updateStudentUser = new TrStudentUser();
        updateStudentUser.setUserId(existingUser.getUserId());
        updateStudentUser.setPassword(passwordEncoder.encode(resetPwdDto.getNewPassword()));
        updateStudentUser.setUpdateTime(DateUtils.getNowDate());
        updateStudentUser.setUpdateBy(existingUser.getStudentNo());

        int update = trStudentUserMapper.updateTrStudentUser(updateStudentUser);
        if (update <= 0)
        {
            return "重置失败，请稍后重试";
        }
        // 注：此场景无当前 token；其他设备旧 token 仍会按 Redis 过期时间存活。
        // 若后续要"重置后全设备下线"，给 TokenService 加 deleteByUserId 工具方法即可。
        return null;
    }

    @Override
    public String changePhone(jakarta.servlet.http.HttpServletRequest request, AppChangePhoneDto changePhoneDto)
    {
        if (changePhoneDto == null
                || StringUtils.isEmpty(changePhoneDto.getNewPhone())
                || StringUtils.isEmpty(changePhoneDto.getSmsCode()))
        {
            return "手机号和验证码不能为空";
        }
        if (!PHONE_PATTERN.matcher(changePhoneDto.getNewPhone()).matches())
        {
            return "新手机号格式不正确";
        }

        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getUser() == null)
        {
            return "用户未登录或登录已过期";
        }

        Long userId = loginUser.getUserId();
        TrStudentUser existingUser = trStudentUserMapper.selectTrStudentUserByUserId(userId);
        if (existingUser == null)
        {
            return "用户不存在";
        }
        if (!"0".equals(existingUser.getStatus()))
        {
            return "账号已被禁用";
        }

        String newPhone = changePhoneDto.getNewPhone();
        if (newPhone.equals(existingUser.getPhone()))
        {
            return "新手机号不能与旧手机号相同";
        }

        TrStudentUser samePhone = trStudentUserMapper.selectAnyByPhone(newPhone);
        if (samePhone != null && !"2".equals(samePhone.getDelFlag()) && !samePhone.getUserId().equals(userId))
        {
            return "新手机号已被其他用户使用";
        }

        if (!smsCodeService.checkSmsCode(newPhone, changePhoneDto.getSmsCode()))
        {
            return "短信验证码错误或已过期";
        }

        TrStudentUser updateStudentUser = new TrStudentUser();
        updateStudentUser.setUserId(userId);
        updateStudentUser.setPhone(newPhone);
        updateStudentUser.setUpdateTime(DateUtils.getNowDate());
        updateStudentUser.setUpdateBy(existingUser.getStudentNo());

        int update = trStudentUserMapper.updateTrStudentUser(updateStudentUser);
        if (update <= 0)
        {
            return "换绑失败，请稍后重试";
        }

        // 把新 phone 写回 existingUser 再 buildSysUserForMobile，保证 LoginUser 缓存里的 userName/phonenumber 同步更新
        existingUser.setPhone(newPhone);
        LoginUser newLoginUser = new LoginUser();
        newLoginUser.setUserId(userId);
        newLoginUser.setPermissions(new HashSet<>());
        newLoginUser.setUser(buildSysUserForMobile(existingUser));
        newLoginUser.setToken(loginUser.getToken());
        tokenService.refreshToken(newLoginUser);

        return null;
    }
}
