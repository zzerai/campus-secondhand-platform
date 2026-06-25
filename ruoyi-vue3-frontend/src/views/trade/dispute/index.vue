<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="订单ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="争议类型" prop="disputeType">
        <el-select v-model="queryParams.disputeType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="t in DISPUTE_TYPES" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="handleStatus">
        <el-select v-model="queryParams.handleStatus" placeholder="全部状态" clearable style="width: 160px">
          <el-option v-for="(v, k) in HANDLE_STATUS" :key="k" :label="v.label" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 状态概览（当前页） -->
    <el-row :gutter="16" class="status-row">
      <el-col v-for="item in statusCards" :key="item.label" :xs="12" :sm="6">
        <el-card shadow="hover" class="status-card">
          <div class="status-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="status-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Cpu" :disabled="single" @click="handleArbitrate()" v-hasPermi="['trade:dispute:arbitrate']">AI仲裁</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="openManual()" v-hasPermi="['trade:dispute:handle']">人工处理</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="disputeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="争议ID" prop="disputeId" align="center" width="80" />
      <el-table-column label="订单 / 商品" align="center" width="140">
        <template #default="scope">
          <div>订单：{{ scope.row.orderId }}</div>
          <div class="text-secondary">商品：{{ scope.row.goodsId || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="争议双方" align="center" width="150">
        <template #default="scope">
          <div>发起人：{{ scope.row.applicantId }}</div>
          <div class="text-secondary">被申诉：{{ scope.row.respondentId || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="争议类型" prop="disputeType" align="center" width="110">
        <template #default="scope">
          <el-tag :type="disputeTypeTag(scope.row.disputeType)">{{ scope.row.disputeType || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="争议描述" prop="disputeContent" min-width="220" show-overflow-tooltip />
      <el-table-column label="AI仲裁" align="center" width="130">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openAiDrawer(scope.row)">查看建议</el-button>
        </template>
      </el-table-column>
      <el-table-column label="处理状态" prop="handleStatus" align="center" width="120">
        <template #default="scope">
          <el-tag :type="handleStatusTag(scope.row.handleStatus)">{{ handleStatusLabel(scope.row.handleStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" align="center" width="160">
        <template #default="scope">{{ parseTime(scope.row.updateTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="220">
        <template #default="scope">
          <el-button link type="primary" icon="Tickets" @click="openDetail(scope.row)">详情</el-button>
          <el-button link type="warning" icon="Cpu" @click="handleArbitrate(scope.row)" v-hasPermi="['trade:dispute:arbitrate']">AI仲裁</el-button>
          <el-button link type="success" icon="Edit" @click="openManual(scope.row)" v-hasPermi="['trade:dispute:handle']">处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- AI 仲裁建议抽屉 -->
    <el-drawer v-model="aiDrawerOpen" title="AI 争议仲裁建议" size="46%" append-to-body>
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="争议ID">{{ currentRow.disputeId }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentRow.orderId }}</el-descriptions-item>
          <el-descriptions-item label="争议类型">
            <el-tag :type="disputeTypeTag(currentRow.disputeType)">{{ currentRow.disputeType || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="handleStatusTag(currentRow.handleStatus)">{{ handleStatusLabel(currentRow.handleStatus) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="latestArbitration">
          <el-divider content-position="left">最近一次 AI 仲裁结果</el-divider>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="仲裁倾向">{{ latestArbitration.arbitrateLevel || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理建议">{{ latestArbitration.suggestion || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分析理由">{{ latestArbitration.reason || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <el-divider content-position="left">AI 分析记录</el-divider>
        <div v-if="currentRow.aiAnalysis" class="ai-reason">{{ currentRow.aiAnalysis }}</div>
        <el-empty v-else description="暂无 AI 分析，请点击「AI仲裁」生成" :image-size="80" />
      </template>
    </el-drawer>

    <!-- 争议详情 -->
    <el-dialog v-model="detailOpen" title="争议详情" width="760px" append-to-body>
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="争议ID">{{ currentRow.disputeId }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="handleStatusTag(currentRow.handleStatus)">{{ handleStatusLabel(currentRow.handleStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentRow.orderId }}</el-descriptions-item>
          <el-descriptions-item label="商品ID">{{ currentRow.goodsId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发起人ID">{{ currentRow.applicantId }}</el-descriptions-item>
          <el-descriptions-item label="被申诉人ID">{{ currentRow.respondentId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="争议类型">
            <el-tag :type="disputeTypeTag(currentRow.disputeType)">{{ currentRow.disputeType || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ parseTime(currentRow.createTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="争议描述" :span="2">{{ currentRow.disputeContent || '-' }}</el-descriptions-item>
          <el-descriptions-item label="证据图片" :span="2">
            <div v-if="evidenceList(currentRow.evidenceImages).length" class="evidence-grid">
              <el-image
                v-for="(img, i) in evidenceList(currentRow.evidenceImages)"
                :key="i"
                :src="img"
                :preview-src-list="evidenceList(currentRow.evidenceImages)"
                :initial-index="i"
                preview-teleported
                fit="cover"
                class="evidence-img"
              />
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理结果" :span="2">{{ currentRow.handleResult || '暂未处理' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 人工仲裁处理 -->
    <el-dialog v-model="manualOpen" title="人工仲裁处理" width="640px" append-to-body>
      <el-form ref="manualRef" :model="manualForm" :rules="manualRules" label-width="auto">
        <el-form-item label="处理结论" prop="decision">
          <el-radio-group v-model="manualForm.decision">
            <el-radio label="支持买家">支持买家</el-radio>
            <el-radio label="支持卖家">支持卖家</el-radio>
            <el-radio label="双方协商">双方协商</el-radio>
            <el-radio label="驳回争议">驳回争议</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="detail">
          <el-input v-model="manualForm.detail" type="textarea" :rows="5" placeholder="请结合 AI 建议、订单信息、争议凭证填写最终处理意见" />
        </el-form-item>
        <el-form-item label="责任判定" prop="faultParty">
          <el-radio-group v-model="manualForm.faultParty">
            <el-radio v-for="f in FAULT_PARTIES" :key="f.value" :label="f.value">{{ f.label }}</el-radio>
          </el-radio-group>
          <div class="fault-tip">判定责任方将扣除其 10 信用分（双方则各扣）；选「不判责」不影响信用分。</div>
        </el-form-item>
        <el-form-item label="退款处理">
          <el-checkbox v-model="manualForm.refundToBuyer">退款给买家</el-checkbox>
          <div class="fault-tip">勾选将调用支付宝退款给买家，订单转为「已退款」；退款失败则整个仲裁回滚。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitManual">确认处理</el-button>
        <el-button @click="manualOpen = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TradeDispute">
import { listDispute, getDispute, handleDispute, reArbitrateDispute } from '@/api/trade/dispute'

const { proxy } = getCurrentInstance()
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 枚举（以 DDL 为准）
const DISPUTE_TYPES = ['未交货', '商品不符', '付款问题', '其他']
const DISPUTE_TYPE_TAG = { '未交货': 'danger', '商品不符': 'warning', '付款问题': 'primary', '其他': 'info' }
const HANDLE_STATUS = {
  '0': { label: '待AI分析', type: 'info' },
  '1': { label: 'AI分析中', type: 'warning' },
  '2': { label: '等待人工仲裁', type: 'danger' },
  '3': { label: '已处理', type: 'success' }
}
// 责任方（对应后端 CreditConstants.FAULT_*）：判责方扣 10 信用分
const FAULT_PARTIES = [
  { value: 'none', label: '不判责' },
  { value: 'respondent', label: '被申诉人担责' },
  { value: 'applicant', label: '发起人担责' },
  { value: 'both', label: '双方各担' }
]
function disputeTypeTag(v) { return DISPUTE_TYPE_TAG[v] || 'info' }
function handleStatusLabel(v) { return (HANDLE_STATUS[String(v)] || {}).label || (v ?? '-') }
function handleStatusTag(v) { return (HANDLE_STATUS[String(v)] || {}).type || 'info' }

function evidenceList(str) {
  if (!str) return []
  return str.split(',').map(s => s.trim()).filter(Boolean).map(u => /^https?:\/\//.test(u) ? u : baseUrl + u)
}

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const single = ref(true)
const disputeList = ref([])
const aiDrawerOpen = ref(false)
const detailOpen = ref(false)
const manualOpen = ref(false)
const currentRow = ref(null)
const latestArbitration = ref(null)

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderId: null, disputeType: null, handleStatus: null })
const manualForm = reactive({ disputeId: null, decision: '双方协商', detail: '', faultParty: 'none', refundToBuyer: false })
const manualRules = {
  decision: [{ required: true, message: '请选择处理结论', trigger: 'change' }],
  detail: [{ required: true, message: '请填写处理说明', trigger: 'blur' }]
}

/** 状态概览（基于当前页数据统计） */
const statusCards = computed(() => {
  const list = disputeList.value
  const count = s => list.filter(d => String(d.handleStatus) === s).length
  return [
    { label: '本页争议', value: list.length, color: 'var(--ct-primary)' },
    { label: '待AI分析', value: count('0'), color: '#7C8696' },
    { label: '等待人工', value: count('2'), color: '#E5685F' },
    { label: '已处理', value: count('3'), color: '#2BA471' }
  ]
})

function getList() {
  loading.value = true
  listDispute(queryParams).then(response => {
    disputeList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.disputeId)
  single.value = selection.length !== 1
  currentRow.value = selection[0] || null
}

/** 查看 AI 建议（拉取详情获取 aiAnalysis 全文） */
function openAiDrawer(row) {
  latestArbitration.value = null
  getDispute(row.disputeId).then(response => {
    currentRow.value = response.data || row
    aiDrawerOpen.value = true
  })
}

/** 争议详情 */
function openDetail(row) {
  getDispute(row.disputeId).then(response => {
    currentRow.value = response.data || row
    detailOpen.value = true
  })
}

/** AI 仲裁 */
function handleArbitrate(row) {
  const target = row || currentRow.value
  if (!target) return
  proxy.$modal.loading('AI 正在分析争议内容...')
  reArbitrateDispute(target.disputeId).then(response => {
    const data = response || {}
    latestArbitration.value = {
      arbitrateLevel: data.arbitrateLevel,
      suggestion: data.suggestion,
      reason: data.reason
    }
    proxy.$modal.closeLoading()
    proxy.$modal.msgSuccess('AI 仲裁建议已生成')
    getList()
    // 刷新当前抽屉详情
    getDispute(target.disputeId).then(res => { currentRow.value = res.data || target })
    aiDrawerOpen.value = true
  }).catch(() => {
    proxy.$modal.closeLoading()
  })
}

/** 人工处理 */
function openManual(row) {
  const target = row || currentRow.value
  if (!target) return
  currentRow.value = target
  manualForm.disputeId = target.disputeId
  manualForm.decision = '双方协商'
  manualForm.detail = ''
  manualForm.faultParty = 'none'
  manualForm.refundToBuyer = false
  manualOpen.value = true
}
function submitManual() {
  proxy.$refs['manualRef'].validate(valid => {
    if (!valid) return
    const handleResult = `【${manualForm.decision}】${manualForm.detail}`
    handleDispute({ disputeId: manualForm.disputeId, handleResult, faultParty: manualForm.faultParty, refundToBuyer: manualForm.refundToBuyer }).then(() => {
      manualOpen.value = false
      getList()
      proxy.$modal.msgSuccess('人工仲裁处理已保存')
    })
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.status-row { margin-bottom: 16px; }
.status-card { text-align: center; }
.status-value { font-size: 26px; font-weight: 600; }
.status-label { margin-top: 6px; color: var(--el-text-color-secondary); }
.text-secondary { color: var(--el-text-color-secondary); font-size: 12px; }
.ai-reason { line-height: 1.7; color: var(--el-text-color-regular); white-space: pre-wrap; }
.evidence-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.evidence-img { width: 72px; height: 72px; border-radius: 8px; border: 1px solid var(--el-border-color-lighter); }
.fault-tip { width: 100%; margin-top: 4px; color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.5; }
</style>
