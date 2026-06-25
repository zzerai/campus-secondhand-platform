package com.ruoyi.web.controller.app;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.AppTokenInfo.AppStudentUser;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.storage.StorageType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.framework.web.service.AppAuthService;
import com.ruoyi.trade.domain.dto.AppChangePhoneDto;
import com.ruoyi.trade.domain.dto.AppChangePwdDto;
import com.ruoyi.trade.domain.dto.AppProfileUpdateDto;
import com.ruoyi.trade.domain.dto.AppResetPwdDto;
import com.ruoyi.trade.domain.vo.AppUserHomepageVo;
import com.ruoyi.trade.service.IAppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端用户信息控制器
 * 
 * @author ruoyi
 */
@Tag(name = "移动端用户信息", description = "获取/修改个人信息")
@RestController
@RequestMapping("/app/user")
public class AppUserController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AppUserController.class);

    @Autowired
    private AppAuthService appAuthService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private IAppUserService appUserService;

    /**
     * 获取用户公开主页（脱敏：不含手机号/学号）
     */
    @Anonymous
    @Operation(summary = "用户公开主页", description = "查看任意用户的公开资料：昵称、头像、信用分、加入时间、在售/已售数、评价均分与好评率（不含手机号/学号）")
    @GetMapping("/homepage/{userId}")
    public AjaxResult getUserHomepage(@Parameter(description = "用户ID", required = true) @PathVariable Long userId)
    {
        AppUserHomepageVo vo = appUserService.getUserHomepage(userId);
        return vo == null ? error("用户不存在") : success(vo);
    }

    /**
     * 获取个人信息
     */
    @Operation(summary = "获取个人信息", description = "展示头像、昵称、学号、信用分等")
    @GetMapping("/profile")
    public AjaxResult getUserProfile(HttpServletRequest request)
    {
        AppStudentUser studentUser = appAuthService.getUserProfile(request);
        if (studentUser == null)
        {
            return error("未获取到用户信息");
        }
        return success(studentUser);
    }

    /**
     * 修改个人信息
     */
    @Operation(summary = "修改个人信息", description = "修改头像、昵称、联系方式")
    @PutMapping("/update")
    public AjaxResult updateUserProfile(HttpServletRequest request, @RequestBody AppProfileUpdateDto profileDto)
    {
        String msg = appAuthService.updateUserProfile(request, profileDto);
        if (StringUtils.isEmpty(msg))
        {
            return success("修改成功");
        }
        return error(msg);
    }

    /**
     * 上传头像（上传到OSS并自动更新用户头像字段）
     */
    @Operation(summary = "上传头像", description = "上传头像图片并自动更新当前用户的头像字段，返回完整URL")
    @PostMapping("/avatar")
    public AjaxResult uploadAvatar(
            HttpServletRequest request,
            @Parameter(description = "头像图片文件，表单字段名 file") @RequestParam("file") MultipartFile file)
    {
        try
        {
            String storedUrl = storageService.uploadImage(file, "avatar");
            String avatarUrl = toAbsoluteUrl(storedUrl);

            AppProfileUpdateDto updateDto = new AppProfileUpdateDto();
            updateDto.setAvatar(avatarUrl);
            String msg = appAuthService.updateUserProfile(request, updateDto);
            if (StringUtils.isNotEmpty(msg))
            {
                return error(msg);
            }

            AjaxResult ajax = AjaxResult.success("头像上传成功");
            ajax.put("url", avatarUrl);
            return ajax;
        }
        catch (Exception e)
        {
            log.error("头像上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改密码（登录态）
     */
    @Operation(summary = "修改密码", description = "需登录态；旧密码校验通过后更新为新密码，并清除当前 token，前端需引导用户用新密码重新登录")
    @PutMapping("/changePwd")
    public AjaxResult changePwd(HttpServletRequest request, @RequestBody AppChangePwdDto changePwdDto)
    {
        String msg = appAuthService.changePassword(request, changePwdDto);
        if (StringUtils.isEmpty(msg))
        {
            return success("修改成功，请用新密码重新登录");
        }
        return error(msg);
    }

    /**
     * 重置密码（无需登录态，通过短信验证码）
     */
    @Operation(summary = "重置密码", description = "无需登录态；先通过 /app/sms/send?scene=reset_password 获取验证码，再调用本接口提交手机号+验证码+新密码")
    @PostMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody AppResetPwdDto resetPwdDto)
    {
        String msg = appAuthService.resetPassword(resetPwdDto);
        if (StringUtils.isEmpty(msg))
        {
            return success("重置成功，请用新密码登录");
        }
        return error(msg);
    }

    /**
     * 换绑手机号（登录态）
     */
    @Operation(summary = "换绑手机号", description = "需登录态；先通过 /app/sms/send?scene=change_phone 让新手机号收到验证码，再调用本接口提交")
    @PutMapping("/changePhone")
    public AjaxResult changePhone(HttpServletRequest request, @RequestBody AppChangePhoneDto changePhoneDto)
    {
        String msg = appAuthService.changePhone(request, changePhoneDto);
        if (StringUtils.isEmpty(msg))
        {
            return success("换绑成功");
        }
        return error(msg);
    }

    private String toAbsoluteUrl(String storedUrl)
    {
        if (storageService.getType() == StorageType.LOCAL)
        {
            return serverConfig.getUrl() + storedUrl;
        }
        return storedUrl;
    }
}
