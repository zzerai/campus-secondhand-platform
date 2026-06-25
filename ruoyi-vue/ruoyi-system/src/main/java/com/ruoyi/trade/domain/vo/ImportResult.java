package com.ruoyi.trade.domain.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入结果。
 *
 * @author lyl
 */
public class ImportResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private int total;

    /** 成功导入数 */
    private int success;

    /** 失败数 */
    private int failure;

    /** 错误详情（行号+原因） */
    private List<ImportError> errors;

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public int getSuccess()
    {
        return success;
    }

    public void setSuccess(int success)
    {
        this.success = success;
    }

    public int getFailure()
    {
        return failure;
    }

    public void setFailure(int failure)
    {
        this.failure = failure;
    }

    public List<ImportError> getErrors()
    {
        return errors;
    }

    public void setErrors(List<ImportError> errors)
    {
        this.errors = errors;
    }
}
