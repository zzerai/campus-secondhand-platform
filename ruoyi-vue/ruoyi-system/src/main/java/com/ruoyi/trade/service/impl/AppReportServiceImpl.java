package com.ruoyi.trade.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.dto.AppReportSubmitDto;
import com.ruoyi.trade.domain.vo.AppReportVo;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeReportMapper;
import com.ruoyi.trade.service.IAppReportService;

/**
 * 移动端举报业务实现。
 */
@Service
public class AppReportServiceImpl implements IAppReportService
{
    @Autowired
    private TrTradeReportMapper trTradeReportMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Override
    public Long submitReport(Long userId, AppReportSubmitDto dto)
    {
        if (userId == null)
        {
            throw new ServiceException("请先登录");
        }
        if (dto == null || dto.getGoodsId() == null)
        {
            throw new ServiceException("商品ID不能为空");
        }

        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(dto.getGoodsId());
        if (goods == null)
        {
            throw new ServiceException("商品不存在或已删除");
        }
        if (userId.equals(goods.getSellerId()))
        {
            throw new ServiceException("不能举报自己发布的商品");
        }
        // 待处理（'0'）的举报存在则拒绝；已处理 / 已驳回的允许用户改进证据后重新举报
        if (trTradeReportMapper.countPendingByUserAndGoods(userId, dto.getGoodsId()) > 0)
        {
            throw new ServiceException("该商品已有待处理的举报，请等待管理员处理后再提交");
        }

        TrTradeReport report = new TrTradeReport();
        report.setGoodsId(dto.getGoodsId());
        report.setOrderId(dto.getOrderId());
        report.setReportType(dto.getReportType());
        report.setReportContent(dto.getReportContent());
        report.setEvidenceImages(dto.getEvidenceImages());
        report.setReportedUserId(goods.getSellerId());
        report.setReportUserId(userId);
        report.setHandleStatus("0");
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底

        // 举报人学号作为 create_by，便于管理端审计列追溯
        TrStudentUser reporter = trStudentUserMapper.selectTrStudentUserByUserId(userId);
        if (reporter != null)
        {
            report.setCreateBy(reporter.getStudentNo());
            report.setUpdateBy(reporter.getStudentNo());
        }

        if (trTradeReportMapper.insertTrTradeReport(report) <= 0)
        {
            throw new ServiceException("举报提交失败");
        }
        return report.getReportId();
    }

    @Override
    public List<AppReportVo> getMyReports(Long userId)
    {
        return trTradeReportMapper.selectAppReportVoListByUser(userId);
    }
}
