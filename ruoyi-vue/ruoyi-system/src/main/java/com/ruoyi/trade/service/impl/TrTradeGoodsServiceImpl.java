package com.ruoyi.trade.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeCategory;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeGoodsImage;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeCategoryMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsImageMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.ITrTradeGoodsService;
import com.ruoyi.trade.domain.vo.BatchAuditResult;
import com.ruoyi.trade.domain.vo.ImportResult;
import com.ruoyi.trade.domain.vo.ImportError;

/**
 * 闲置商品Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeGoodsServiceImpl implements ITrTradeGoodsService
{
    /** 商品状态：待审核 */
    public static final String STATUS_PENDING   = "0";
    /** 商品状态：已上架 */
    public static final String STATUS_ON_SHELF  = "1";
    /** 商品状态：审核拒绝 */
    public static final String STATUS_REJECTED  = "2";
    /** 商品状态：已下架 */
    public static final String STATUS_OFF_SHELF = "3";
    /** 商品状态：已售出 */
    public static final String STATUS_SOLD      = "4";

    private static final List<String> ALL_STATUSES =
            Arrays.asList(STATUS_PENDING, STATUS_ON_SHELF, STATUS_REJECTED, STATUS_OFF_SHELF, STATUS_SOLD);

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrTradeGoodsImageMapper trTradeGoodsImageMapper;

    @Autowired
    private TrTradeCategoryMapper trTradeCategoryMapper;

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Override
    public TrTradeGoods selectTrTradeGoodsByGoodsId(Long goodsId)
    {
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (goods != null)
        {
            TrTradeGoodsImage q = new TrTradeGoodsImage();
            q.setGoodsId(goodsId);
            goods.setImages(trTradeGoodsImageMapper.selectTrTradeGoodsImageList(q));
        }
        return goods;
    }

    @Override
    public List<TrTradeGoods> selectTrTradeGoodsList(TrTradeGoods trTradeGoods)
    {
        return trTradeGoodsMapper.selectTrTradeGoodsList(trTradeGoods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTrTradeGoods(TrTradeGoods trTradeGoods)
    {
        validateForeignKeys(trTradeGoods);
        validatePrice(trTradeGoods);
        if (StringUtils.isEmpty(trTradeGoods.getGoodsStatus()))
        {
            // 管理端新增默认直接上架（管理员自助录入），如要走审核流程将此处改为 STATUS_PENDING
            trTradeGoods.setGoodsStatus(STATUS_ON_SHELF);
        }
        else if (!ALL_STATUSES.contains(trTradeGoods.getGoodsStatus()))
        {
            throw new ServiceException("非法的商品状态：" + trTradeGoods.getGoodsStatus());
        }
        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();
        trTradeGoods.setCreateBy(username);
        trTradeGoods.setCreateTime(now);
        trTradeGoods.setUpdateBy(username);
        trTradeGoods.setUpdateTime(now);
        return trTradeGoodsMapper.insertTrTradeGoods(trTradeGoods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTrTradeGoods(TrTradeGoods trTradeGoods)
    {
        if (trTradeGoods.getGoodsId() == null)
        {
            throw new ServiceException("修改商品失败，商品ID不能为空");
        }
        TrTradeGoods exist = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(trTradeGoods.getGoodsId());
        if (exist == null)
        {
            throw new ServiceException("修改商品失败，商品不存在或已删除");
        }
        // 已售出商品不允许再修改业务字段
        if (STATUS_SOLD.equals(exist.getGoodsStatus()))
        {
            throw new ServiceException("已售出的商品不允许修改");
        }
        if (trTradeGoods.getCategoryId() != null || trTradeGoods.getSellerId() != null)
        {
            validateForeignKeys(trTradeGoods);
        }
        validatePrice(trTradeGoods);
        // 通用 edit 不允许直接修改状态/审核字段，必须走 audit/offline/online 接口
        // 防止管理员误操作绕过状态机；如有审核字段调整需求请补独立接口
        trTradeGoods.setGoodsStatus(null);
        trTradeGoods.setAuditUserId(null);
        trTradeGoods.setAuditTime(null);
        trTradeGoods.setAuditRemark(null);
        trTradeGoods.setUpdateBy(SecurityUtils.getUsername());
        trTradeGoods.setUpdateTime(DateUtils.getNowDate());
        return trTradeGoodsMapper.updateTrTradeGoods(trTradeGoods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTrTradeGoodsByGoodsIds(Long[] goodsIds)
    {
        if (goodsIds == null || goodsIds.length == 0)
        {
            throw new ServiceException("删除商品失败，ID 列表不能为空");
        }
        for (Long goodsId : goodsIds)
        {
            ensureDeletable(goodsId);
        }
        return trTradeGoodsMapper.deleteTrTradeGoodsByGoodsIds(goodsIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTrTradeGoodsByGoodsId(Long goodsId)
    {
        ensureDeletable(goodsId);
        return trTradeGoodsMapper.deleteTrTradeGoodsByGoodsId(goodsId);
    }

    /**
     * 删除前置校验：商品存在 + 非已售出 + 无活跃订单。
     * 已售出（'4'）或存在 order_status in 0/1/2/5 的订单时拒绝删除，
     * 避免删除后订单悬空、买家详情查不到商品。
     */
    private void ensureDeletable(Long goodsId)
    {
        if (goodsId == null)
        {
            throw new ServiceException("商品ID不能为空");
        }
        TrTradeGoods exist = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (exist == null)
        {
            throw new ServiceException("商品不存在或已删除（ID=" + goodsId + "）");
        }
        if (STATUS_SOLD.equals(exist.getGoodsStatus()))
        {
            throw new ServiceException("商品[" + exist.getTitle() + "]已售出，存在交易记录，不允许删除");
        }
        int activeOrders = trTradeOrderMapper.countActiveOrdersByGoodsId(goodsId);
        if (activeOrders > 0)
        {
            throw new ServiceException(
                    "商品[" + exist.getTitle() + "]存在 " + activeOrders + " 笔未完成订单，不允许删除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditGoods(Long goodsId, String goodsStatus, String auditRemark)
    {
        if (goodsId == null)
        {
            throw new ServiceException("审核失败，商品ID不能为空");
        }
        if (!STATUS_ON_SHELF.equals(goodsStatus) && !STATUS_REJECTED.equals(goodsStatus))
        {
            throw new ServiceException("审核失败，目标状态只能是 1(通过) 或 2(拒绝)");
        }
        TrTradeGoods exist = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (exist == null)
        {
            throw new ServiceException("审核失败，商品不存在或已删除");
        }
        if (!STATUS_PENDING.equals(exist.getGoodsStatus()))
        {
            throw new ServiceException("审核失败，仅待审核状态可以执行审核操作");
        }
        if (STATUS_REJECTED.equals(goodsStatus) && StringUtils.isEmpty(auditRemark))
        {
            throw new ServiceException("拒绝审核必须填写审核意见");
        }
        Long auditUserId = currentSysUserId();
        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();
        return trTradeGoodsMapper.updateGoodsAudit(
                goodsId, goodsStatus, auditUserId, now, auditRemark, username, now);
    }

    @Override
    public BatchAuditResult batchAuditGoods(Long[] goodsIds, String goodsStatus, String auditRemark)
    {
        if (goodsIds == null || goodsIds.length == 0)
        {
            throw new ServiceException("批量审核失败，ID 列表不能为空");
        }
        // 单条 auditGoods 自带 @Transactional，逐条提交：一条失败不影响其它
        // 因此本方法本身不加 @Transactional，需要按条收集结果
        BatchAuditResult result = new BatchAuditResult();
        for (Long id : goodsIds)
        {
            try
            {
                if (auditGoods(id, goodsStatus, auditRemark) > 0)
                {
                    result.incSuccess();
                }
                else
                {
                    result.addError(id, "数据库未更新（可能并发已被改）");
                }
            }
            catch (ServiceException ex)
            {
                result.addError(id, ex.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int offlineGoods(Long goodsId)
    {
        TrTradeGoods exist = ensureExist(goodsId);
        if (!STATUS_ON_SHELF.equals(exist.getGoodsStatus()))
        {
            throw new ServiceException("仅已上架的商品可以下架");
        }
        return trTradeGoodsMapper.updateGoodsStatus(
                goodsId, STATUS_OFF_SHELF, SecurityUtils.getUsername(), DateUtils.getNowDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int onlineGoods(Long goodsId)
    {
        TrTradeGoods exist = ensureExist(goodsId);
        if (!STATUS_OFF_SHELF.equals(exist.getGoodsStatus()))
        {
            throw new ServiceException("仅已下架的商品可以重新上架");
        }
        return trTradeGoodsMapper.updateGoodsStatus(
                goodsId, STATUS_ON_SHELF, SecurityUtils.getUsername(), DateUtils.getNowDate());
    }

    private TrTradeGoods ensureExist(Long goodsId)
    {
        if (goodsId == null)
        {
            throw new ServiceException("商品ID不能为空");
        }
        TrTradeGoods exist = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (exist == null)
        {
            throw new ServiceException("商品不存在或已删除");
        }
        return exist;
    }

    private void validateForeignKeys(TrTradeGoods goods)
    {
        if (goods.getCategoryId() == null)
        {
            throw new ServiceException("商品分类不能为空");
        }
        TrTradeCategory cat = trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(goods.getCategoryId());
        if (cat == null || "1".equals(cat.getStatus()))
        {
            throw new ServiceException("商品分类不存在或已停用");
        }
        if (goods.getSellerId() == null)
        {
            throw new ServiceException("卖家不能为空");
        }
        TrStudentUser seller = trStudentUserMapper.selectTrStudentUserByUserId(goods.getSellerId());
        if (seller == null)
        {
            throw new ServiceException("卖家不存在");
        }
        if ("1".equals(seller.getStatus()))
        {
            throw new ServiceException("卖家账号已禁用，无法发布/编辑商品");
        }
    }

    private void validatePrice(TrTradeGoods goods)
    {
        if (goods.getPrice() != null && goods.getPrice().signum() < 0)
        {
            throw new ServiceException("出售价格不能为负数");
        }
        if (goods.getOriginalPrice() != null && goods.getOriginalPrice().signum() < 0)
        {
            throw new ServiceException("原价不能为负数");
        }
    }

    /** 取当前登录管理员 userId（找不到则返回 null）。 */
    private Long currentSysUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 批量导入商品。
     *
     * 匹配规则：
     * - 新增：卖家ID+商品标题 作为唯一键（实际业务中，同一卖家可发布同名商品？这里简化处理）
     * - 更新：按（sellerId, title）匹配，存在则更新，否则新增
     *
     * @param goodsList 商品列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importGoods(List<TrTradeGoods> goodsList, boolean updateSupport)
    {
        ImportResult result = new ImportResult();
        result.setTotal(goodsList.size());

        int successCount = 0;
        List<ImportError> errors = new ArrayList<>();

        String currentUser = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        for (int i = 0; i < goodsList.size(); i++)
        {
            TrTradeGoods goods = goodsList.get(i);
            int rowIndex = i + 2; // Excel行号

            try
            {
                // 1. 必填字段校验
                if (StringUtils.isEmpty(goods.getTitle()))
                {
                    errors.add(new ImportError(rowIndex, "商品标题不能为空"));
                    continue;
                }
                if (goods.getPrice() == null)
                {
                    errors.add(new ImportError(rowIndex, "出售价格不能为空"));
                    continue;
                }

                // 2. sellerId 优先使用传入ID，若没有则通过sellerStudentNo查询
                // 注意：ExcelUtils会根据@Excel(name="卖家学生用户ID")自动填充sellerId
                // 如果用户填的是学号而不是ID，这里要做转换（可以预先通过模板约束只允许填ID）

                // 3. categoryId 类似处理
                if (goods.getCategoryId() == null)
                {
                    errors.add(new ImportError(rowIndex, "商品分类ID不能为空"));
                    continue;
                }

                // 4. 验证分类是否存在
                TrTradeCategory category = trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(goods.getCategoryId());
                if (category == null || "1".equals(category.getStatus()))
                {
                    errors.add(new ImportError(rowIndex, "商品分类不存在或已停用（ID=" + goods.getCategoryId() + "）"));
                    continue;
                }

                // 5. 验证卖家是否存在且状态正常
                if (goods.getSellerId() == null)
                {
                    errors.add(new ImportError(rowIndex, "卖家学生用户ID不能为空"));
                    continue;
                }
                TrStudentUser seller = trStudentUserMapper.selectTrStudentUserByUserId(goods.getSellerId());
                if (seller == null)
                {
                    errors.add(new ImportError(rowIndex, "卖家不存在（ID=" + goods.getSellerId() + "）"));
                    continue;
                }
                if ("1".equals(seller.getStatus()))
                {
                    errors.add(new ImportError(rowIndex, "卖家账号已禁用（ID=" + goods.getSellerId() + "）"));
                    continue;
                }

                // 6. 价格校验
                if (goods.getPrice() != null && goods.getPrice().signum() < 0)
                {
                    errors.add(new ImportError(rowIndex, "出售价格不能为负数"));
                    continue;
                }
                if (goods.getOriginalPrice() != null && goods.getOriginalPrice().signum() < 0)
                {
                    errors.add(new ImportError(rowIndex, "原价不能为负数"));
                    continue;
                }

                // 7. 状态处理：导入商品默认待审核
                if (StringUtils.isEmpty(goods.getGoodsStatus()))
                {
                    goods.setGoodsStatus(STATUS_PENDING);
                }
                else if (!ALL_STATUSES.contains(goods.getGoodsStatus()))
                {
                    errors.add(new ImportError(rowIndex, "非法商品状态：" + goods.getGoodsStatus()));
                    continue;
                }

                // 8. 浏览次数/收藏次数默认
                if (goods.getViewCount() == null)
                {
                    goods.setViewCount(0L);
                }
                if (goods.getFavoriteCount() == null)
                {
                    goods.setFavoriteCount(0L);
                }

                // 9. 审核字段：导入时不自动填审核信息（除非用户显式提供）
                // 如果用户提供了审核管理员ID/时间/意见，则保留；否则置null

                // 10. 判断新增还是更新
                // 查询同一卖家是否已发布同名商品（简化去重用）
                TrTradeGoods exist = trTradeGoodsMapper.selectBySellerAndTitle(goods.getSellerId(), goods.getTitle());

                if (exist != null)
                {
                    if (!updateSupport)
                    {
                        errors.add(new ImportError(rowIndex, "卖家已发布同名商品，请检查或启用更新支持"));
                        continue;
                    }
                    // 更新模式
                    goods.setGoodsId(exist.getGoodsId());
                    goods.setUpdateBy(currentUser);
                    goods.setUpdateTime(now);
                    trTradeGoodsMapper.updateTrTradeGoods(goods);
                }
                else
                {
                    // 新增模式
                    goods.setCreateBy(currentUser);
                    goods.setCreateTime(now);
                    goods.setUpdateBy(currentUser);
                    goods.setUpdateTime(now);
                    trTradeGoodsMapper.insertTrTradeGoods(goods);
                }

                successCount++;
            }
            catch (Exception e)
            {
                errors.add(new ImportError(rowIndex, "处理失败：" + e.getMessage()));
            }
        }

        result.setSuccess(successCount);
        result.setFailure(errors.size());
        result.setErrors(errors);

        return result;
    }
}
