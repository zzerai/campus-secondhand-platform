package com.ruoyi.framework.interceptor;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MyBatis 审计时间字段自动填充拦截器。
 *
 * <p>所有继承 {@link BaseEntity} 的实体在 INSERT / UPDATE 时自动兜底 {@code createTime} /
 * {@code updateTime}：</p>
 * <ul>
 *   <li>INSERT：无条件把 createTime 与 updateTime 都设为当前时间，确保新记录两个时间戳一致，
 *       且不依赖 Service 是否显式设置（历史上 RuoYi 生成的 Service 通常只 set createTime）</li>
 *   <li>UPDATE：若 updateTime 为 null 则填当前时间（已显式 set 的值保留）</li>
 * </ul>
 *
 * <p>不在此处统一填 {@code createBy} / {@code updateBy}：管理端走 {@code sys_user}、
 * 移动端走学生 JWT，两套认证体系的"操作人"语义不同，强行从 SecurityContext 取会取错。
 * by 字段仍由 service 层显式 set。</p>
 *
 * <p>覆盖参数形态：</p>
 * <ul>
 *   <li>单实体参数：{@code insertXxx(TrXxx entity)}</li>
 *   <li>{@code @Param} 多参数（MyBatis 包装为 Map）：遍历 values 处理其中的 BaseEntity</li>
 *   <li>集合参数（批量 insert）：遍历元素中的 BaseEntity</li>
 * </ul>
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class })
})
@Component
public class AuditTimeFillInterceptor implements Interceptor
{
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType cmd = ms.getSqlCommandType();
        if (cmd == SqlCommandType.INSERT || cmd == SqlCommandType.UPDATE)
        {
            fillAuditTime(invocation.getArgs()[1], cmd == SqlCommandType.INSERT, new Date());
        }
        return invocation.proceed();
    }

    private void fillAuditTime(Object parameter, boolean isInsert, Date now)
    {
        if (parameter == null)
        {
            return;
        }
        if (parameter instanceof BaseEntity)
        {
            fillEntity((BaseEntity) parameter, now, isInsert);
        }
        else if (parameter instanceof Map)
        {
            for (Object value : ((Map<?, ?>) parameter).values())
            {
                if (value instanceof BaseEntity)
                {
                    fillEntity((BaseEntity) value, now, isInsert);
                }
                else if (value instanceof Collection)
                {
                    fillCollection((Collection<?>) value, now, isInsert);
                }
            }
        }
        else if (parameter instanceof Collection)
        {
            fillCollection((Collection<?>) parameter, now, isInsert);
        }
    }

    private void fillCollection(Collection<?> collection, Date now, boolean isInsert)
    {
        for (Object item : collection)
        {
            if (item instanceof BaseEntity)
            {
                fillEntity((BaseEntity) item, now, isInsert);
            }
        }
    }

    private void fillEntity(BaseEntity entity, Date now, boolean isInsert)
    {
        if (isInsert)
        {
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
        }
        else if (entity.getUpdateTime() == null)
        {
            entity.setUpdateTime(now);
        }
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
        // no properties needed
    }
}
