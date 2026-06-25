package com.ruoyi.framework.interceptor;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.BaseEntity;

@ExtendWith(MockitoExtension.class)
class AuditTimeFillInterceptorTest
{
    @Mock
    private MappedStatement mappedStatement;

    @Mock
    private Executor executor;

    private final AuditTimeFillInterceptor interceptor = new AuditTimeFillInterceptor();

    @Test
    void shouldFillCreateAndUpdateTimeOnInsert() throws Throwable
    {
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        DummyEntity entity = new DummyEntity();
        invoke(entity);

        Assertions.assertNotNull(entity.getCreateTime());
        Assertions.assertNotNull(entity.getUpdateTime());
    }

    @Test
    void shouldFillOnlyUpdateTimeOnUpdate() throws Throwable
    {
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
        DummyEntity entity = new DummyEntity();
        invoke(entity);

        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNotNull(entity.getUpdateTime());
    }

    @Test
    void shouldOverrideExistingTimesOnInsert() throws Throwable
    {
        // INSERT 场景：即使 Service 预设了 createTime，也强制覆盖为当前时间，
        // 并同步把 updateTime 设为同一时刻，保证新记录两个时间戳一致
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        Date past = new Date(0L);
        DummyEntity entity = new DummyEntity();
        entity.setCreateTime(past);
        entity.setUpdateTime(past);
        invoke(entity);

        Assertions.assertNotSame(past, entity.getCreateTime());
        Assertions.assertNotSame(past, entity.getUpdateTime());
        Assertions.assertEquals(entity.getCreateTime(), entity.getUpdateTime());
    }

    @Test
    void shouldPreserveExplicitUpdateTimeOnUpdate() throws Throwable
    {
        // UPDATE 场景：已显式 set 的 updateTime 不应被覆盖
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
        Date past = new Date(0L);
        DummyEntity entity = new DummyEntity();
        entity.setUpdateTime(past);
        invoke(entity);

        Assertions.assertSame(past, entity.getUpdateTime());
    }

    @Test
    void shouldSkipNonBaseEntityParameter() throws Throwable
    {
        // delete by id 这种参数（Long / String）应被静默跳过
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
        Object result = invoke(123L);
        Assertions.assertEquals(1, result);
    }

    @Test
    void shouldSkipSelectAndDelete() throws Throwable
    {
        // SELECT / DELETE 不触发时间填充
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.SELECT);
        DummyEntity entity = new DummyEntity();
        invoke(entity);
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());
    }

    @Test
    void shouldHandleParamMapWithBaseEntityValue() throws Throwable
    {
        // mapper 接口用 @Param 多参数时 MyBatis 包装为 Map，应能识别其中的 BaseEntity
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        DummyEntity entity = new DummyEntity();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("entity", entity);
        paramMap.put("extra", "ignored-string");
        invoke(paramMap);

        Assertions.assertNotNull(entity.getCreateTime());
        Assertions.assertNotNull(entity.getUpdateTime());
    }

    @Test
    void shouldHandleCollectionForBatchInsert() throws Throwable
    {
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        DummyEntity e1 = new DummyEntity();
        DummyEntity e2 = new DummyEntity();
        invoke(Arrays.asList(e1, e2, "non-entity"));

        Assertions.assertNotNull(e1.getCreateTime());
        Assertions.assertNotNull(e2.getCreateTime());
    }

    private Object invoke(Object parameter) throws Throwable
    {
        // Invocation 在构造时校验 method 必须属于 @Signature 中声明的 Executor.class，
        // 因此走 Executor 的 update 方法签名（与拦截器声明保持一致）。
        java.lang.reflect.Method updateMethod = Executor.class.getMethod(
                "update", MappedStatement.class, Object.class);
        when(executor.update(any(MappedStatement.class), any())).thenReturn(1);
        Invocation invocation = new Invocation(executor, updateMethod,
                new Object[] { mappedStatement, parameter });
        return interceptor.intercept(invocation);
    }

    /** 测试用 BaseEntity 子类（包内可见）。 */
    static class DummyEntity extends BaseEntity { }
}
