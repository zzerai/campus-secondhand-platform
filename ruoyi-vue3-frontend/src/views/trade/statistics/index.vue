<template>
  <div class="app-container statistics-page">
    <el-form :model="queryParams" :inline="true" label-width="80px" class="mb8" v-loading="loading">
      <el-form-item label="统计范围">
        <el-radio-group v-model="queryParams.days" @change="refreshCharts">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="15">近15天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="日期区间">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="refreshCharts" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Refresh" @click="refreshCharts">刷新</el-button>
        <el-button icon="Download" @click="handleExport">导出图表数据</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="overview-row">
      <el-col v-for="item in overviewCards" :key="item.key" :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-content">
            <div class="overview-icon" :style="{ background: item.bg, color: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-title">{{ item.title }}</div>
              <div class="overview-value">{{ item.value }}</div>
              <div class="overview-extra">{{ item.extra }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>订单趋势</span><el-tag type="info" effect="plain">新增 / 完成 / 取消</el-tag></div></template>
          <div ref="orderTrendRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>商品分类分布</span><el-tag type="success" effect="plain">商品数量占比</el-tag></div></template>
          <div ref="categoryRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>支付金额趋势</span><el-tag type="warning" effect="plain">支付宝沙盒成交金额</el-tag></div></template>
          <div ref="paymentTrendRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>支付概览</span><el-tag type="danger" effect="plain">支付笔数 / 金额</el-tag></div></template>
          <el-table :data="paymentSummary" size="small" border>
            <el-table-column label="日期" prop="statDate" align="center" />
            <el-table-column label="支付笔数" prop="paymentCount" align="center" />
            <el-table-column label="支付金额" prop="paymentAmount" align="center"><template #default="scope">￥{{ scope.row.paymentAmount.toFixed(2) }}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="TradeStatistics">
import * as echarts from 'echarts'
import { Goods, ShoppingCart, Money, User, Warning, Tickets } from '@element-plus/icons-vue'
import { getOverview, getCategoryStatistics, getOrderTrend, getPaymentTrend } from '@/api/trade/statistics'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const queryParams = reactive({ days: 30 })
const dateRange = ref([])
const orderTrendRef = ref(null)
const categoryRef = ref(null)
const paymentTrendRef = ref(null)
let orderChart
let categoryChart
let paymentChart

const overview = reactive({ goodsTotal: 0, orderTotal: 0, totalTradeAmount: 0, studentUserTotal: 0, goodsPendingAudit: 0, pendingDisputeCount: 0 })
const categoryData = ref([])
const orderTrend = ref([])
const paymentTrend = ref([])

const overviewCards = computed(() => [
  { key: 'goods', title: '商品总数', value: overview.goodsTotal, extra: '在售 / 待审核 / 已售出', icon: markRaw(Goods), color: '#409EFF', bg: '#ecf5ff' },
  { key: 'order', title: '订单总数', value: overview.orderTotal, extra: '预约与交易订单', icon: markRaw(ShoppingCart), color: '#67C23A', bg: '#f0f9eb' },
  { key: 'amount', title: '交易金额', value: `￥${overview.totalTradeAmount.toFixed(2)}`, extra: '支付宝沙盒累计成交', icon: markRaw(Money), color: '#E6A23C', bg: '#fdf6ec' },
  { key: 'user', title: '学生用户', value: overview.studentUserTotal, extra: '移动端注册用户', icon: markRaw(User), color: '#909399', bg: '#f4f4f5' },
  { key: 'audit', title: '待审核商品', value: overview.goodsPendingAudit, extra: '需要管理员处理', icon: markRaw(Tickets), color: '#F56C6C', bg: '#fef0f0' },
  { key: 'dispute', title: '争议处理中', value: overview.pendingDisputeCount, extra: '等待 AI / 人工仲裁', icon: markRaw(Warning), color: '#9C27B0', bg: '#f6efff' }
])
const paymentSummary = computed(() => paymentTrend.value.slice(-6).reverse())
function buildTrendQuery() {
  const query = { days: queryParams.days }
  if (dateRange.value && dateRange.value.length === 2) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  }
  return query
}

function initCharts() {
  orderChart = echarts.init(orderTrendRef.value, 'macarons')
  categoryChart = echarts.init(categoryRef.value, 'macarons')
  paymentChart = echarts.init(paymentTrendRef.value, 'macarons')
  window.addEventListener('resize', resizeCharts)
}

function resizeCharts() {
  orderChart && orderChart.resize()
  categoryChart && categoryChart.resize()
  paymentChart && paymentChart.resize()
}

function renderOrderTrend() {
  orderChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增订单', '完成订单', '取消订单'] },
    grid: { left: 35, right: 25, bottom: 30, top: 45 },
    xAxis: { type: 'category', boundaryGap: false, data: orderTrend.value.map(item => item.statDate) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '新增订单', type: 'line', smooth: true, data: orderTrend.value.map(item => item.newOrderCount) },
      { name: '完成订单', type: 'line', smooth: true, data: orderTrend.value.map(item => item.completedOrderCount) },
      { name: '取消订单', type: 'line', smooth: true, data: orderTrend.value.map(item => item.cancelledOrderCount) }
    ]
  })
}

function renderCategory() {
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      name: '商品数量',
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '43%'],
      data: categoryData.value.map(item => ({ name: item.categoryName, value: item.goodsCount }))
    }]
  })
}

function renderPaymentTrend() {
  paymentChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['支付金额', '支付笔数'] },
    grid: { left: 45, right: 45, bottom: 30, top: 45 },
    xAxis: { type: 'category', data: paymentTrend.value.map(item => item.statDate) },
    yAxis: [
      { type: 'value', name: '金额', axisLabel: { formatter: '￥{value}' } },
      { type: 'value', name: '笔数', minInterval: 1 }
    ],
    series: [
      { name: '支付金额', type: 'bar', barMaxWidth: 22, data: paymentTrend.value.map(item => item.paymentAmount) },
      { name: '支付笔数', type: 'line', yAxisIndex: 1, smooth: true, data: paymentTrend.value.map(item => item.paymentCount) }
    ]
  })
}

async function refreshCharts() {
  loading.value = true
  try {
    const trendQuery = buildTrendQuery()
    const [overviewRes, categoryRes, orderTrendRes, paymentTrendRes] = await Promise.all([
      getOverview(),
      getCategoryStatistics(),
      getOrderTrend(trendQuery),
      getPaymentTrend(trendQuery)
    ])
    Object.assign(overview, overviewRes.data || {})
    overview.totalTradeAmount = Number(overview.totalTradeAmount || 0)
    categoryData.value = categoryRes.data || []
    orderTrend.value = orderTrendRes.data || []
    paymentTrend.value = (paymentTrendRes.data || []).map(item => ({ ...item, paymentAmount: Number(item.paymentAmount || 0) }))
    nextTick(() => {
      renderOrderTrend()
      renderCategory()
      renderPaymentTrend()
    })
  } finally {
    loading.value = false
  }
}

function handleExport() {
  proxy.$modal.msgSuccess('当前统计图表已使用后端实时数据')
}

onMounted(() => {
  nextTick(() => {
    initCharts()
    refreshCharts()
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  orderChart && orderChart.dispose()
  categoryChart && categoryChart.dispose()
  paymentChart && paymentChart.dispose()
})
</script>

<style scoped lang="scss">
.statistics-page {
  .mb16 { margin-bottom: 16px; }
  .overview-row { margin-bottom: 16px; }
  .overview-card { margin-bottom: 16px; }
  .overview-content { display: flex; align-items: center; gap: 12px; }
  .overview-icon { width: 46px; height: 46px; display: flex; align-items: center; justify-content: center; border-radius: 10px; font-size: 24px; }
  .overview-title { color: var(--el-text-color-secondary); font-size: 13px; }
  .overview-value { margin-top: 5px; color: var(--el-text-color-primary); font-size: 22px; font-weight: 600; }
  .overview-extra { margin-top: 4px; color: var(--el-text-color-placeholder); font-size: 12px; }
  .chart-row { margin-bottom: 16px; }
  .card-header { display: flex; align-items: center; justify-content: space-between; }
  .chart-box { height: 360px; width: 100%; }
}
</style>
