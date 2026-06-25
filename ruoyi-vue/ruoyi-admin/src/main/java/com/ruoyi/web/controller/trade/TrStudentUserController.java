package com.ruoyi.web.controller.trade;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.service.ITrStudentUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ruoyi.trade.domain.vo.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import static com.ruoyi.framework.datasource.DynamicDataSourceContextHolder.log;

/**
 * 学生用户Controller
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@Tag(name = "学生用户管理")
@RestController
@RequestMapping("/trade/student")
public class TrStudentUserController extends BaseController
{
    @Autowired
    private ITrStudentUserService trStudentUserService;

    /**
     * 查询学生用户列表
     */
    @PreAuthorize("@ss.hasPermi('trade:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrStudentUser trStudentUser)
    {
        startPage();
        List<TrStudentUser> list = trStudentUserService.selectTrStudentUserList(trStudentUser);
        return getDataTable(list);
    }

    /**
     * 导出学生用户列表
     */
    @PreAuthorize("@ss.hasPermi('trade:student:export')")
    @Log(title = "学生用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrStudentUser trStudentUser)
    {
        List<TrStudentUser> list = trStudentUserService.selectTrStudentUserList(trStudentUser);
        ExcelUtil<TrStudentUser> util = new ExcelUtil<TrStudentUser>(TrStudentUser.class);
        util.exportExcel(response, list, "学生用户数据");
    }

    /**
     * 获取学生用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:student:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return success(trStudentUserService.selectTrStudentUserByUserId(userId));
    }

    /**
     * 新增学生用户
     */
    @PreAuthorize("@ss.hasPermi('trade:student:add')")
    @Log(title = "学生用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrStudentUser trStudentUser)
    {
        return toAjax(trStudentUserService.insertTrStudentUser(trStudentUser));
    }

    /**
     * 修改学生用户
     */
    @PreAuthorize("@ss.hasPermi('trade:student:edit')")
    @Log(title = "学生用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrStudentUser trStudentUser)
    {
        return toAjax(trStudentUserService.updateTrStudentUser(trStudentUser));
    }

    /**
     * 删除学生用户
     */
    @PreAuthorize("@ss.hasPermi('trade:student:remove')")
    @Log(title = "学生用户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(trStudentUserService.deleteTrStudentUserByUserIds(userIds));
    }

    /**
     * 学生用户状态修改（启用/停用）
     */
    @PreAuthorize("@ss.hasPermi('trade:student:edit')")
    @Log(title = "学生用户", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody TrStudentUser user)
    {
        user.setUpdateBy(getUsername());
        return toAjax(trStudentUserService.updateUserStatus(user));
    }

    /**
     * 重置学生用户密码
     */
    @PreAuthorize("@ss.hasPermi('trade:student:resetPwd')")
    @Log(title = "学生用户", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody TrStudentUser user)
    {
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(trStudentUserService.resetPwd(user));
    }

    /**
     * 下载学生用户导入模板
     */
    @PreAuthorize("@ss.hasPermi('trade:student:import')")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<TrStudentUser> util = new ExcelUtil<>(TrStudentUser.class);
        util.importTemplateExcel(response, "学生用户导入模板");
    }

    /**
     * 导入学生用户（支持新增/更新）
     *
     * @param file Excel文件
     * @param updateSupport 是否支持更新（true=存在则更新，false=存在则跳过）
     * @return 导入结果
     */
    @PreAuthorize("@ss.hasPermi('trade:student:import')")
    @Log(title = "学生用户", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, @RequestParam(value = "updateSupport", required = false, defaultValue = "false") boolean updateSupport)
    {
        if (file == null || file.isEmpty())
        {
            return error("上传文件为空，请选择Excel文件");
        }
        ExcelUtil<TrStudentUser> util = new ExcelUtil<>(TrStudentUser.class);
        try
        {
            List<TrStudentUser> userList = util.importExcel(file.getInputStream());
            com.ruoyi.trade.domain.vo.ImportResult result = trStudentUserService.importStudentUsers(userList, updateSupport);
            return success(result);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message != null && message.contains("OLE2") || message.contains("OOXML"))
            {
                return error("文件格式错误，请上传.xlsx或.xls格式的Excel文件");
            }
            log.error("导入学生用户失败", e);
            return error("导入失败：" + e.getMessage());
        }
    }
}
