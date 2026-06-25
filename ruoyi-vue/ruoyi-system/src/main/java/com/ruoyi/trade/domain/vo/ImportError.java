package com.ruoyi.trade.domain.vo;

import java.io.Serializable;

/**
 * 导入单行错误信息。
 *
 * @author lyl
 */
public class ImportError implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Excel行号（从1开始，含表头） */
    private int rowIndex;

    /** 错误原因 */
    private String reason;

    public ImportError() {}

    public ImportError(int rowIndex, String reason)
    {
        this.rowIndex = rowIndex;
        this.reason = reason;
    }

    public int getRowIndex()
    {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
