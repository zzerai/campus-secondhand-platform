<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="被举报商品ID" prop="goodsId">
        <el-input v-model="queryParams.goodsId" placeholder="商品ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="举报人ID" prop="reportUserId">
        <el-input v-model="queryParams.reportUserId" placeholder="举报人ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="举报类型" prop="reportType">
        <el-select v-model="queryParams.reportType" placeholder="全部类型" clearable style="width: 160px">
          <el-option v-for="t in REPORT_TYPES" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="handleStatus">
        <el-select v-model="queryParams.handleStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(v, k) in HANDLE_STATUS" :key="k" :label="v.label" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="multiple" @click="handleBatch('1')" v-hasPermi="['trade:report:edit']">批量通过</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Close" :disabled="multiple" @click="handleBatch('2')" v-hasPermi="['trade:report:edit']">批量驳回</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trade:report:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trade:report:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="举报ID" align="center" prop="reportId" width="80" />
      <el-table-column label="被举报商品" align="center" prop="goodsId" width="100">
        <template #default="scope">{{ scope.row.goodsId || '-' }}</template>
      </el-table-column>
      <el-table-column label="举报人 / 被举报人" align="center" width="150">
        <template #default="scope">
          <div>举报人：{{ scope.row.reportUserId }}</div>
          <div class="text-secondary">被举报：{{ scope.row.reportedUserId || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="举报类型" align="center" prop="reportType" width="110">
        <template #default="scope">
          <el-tag :type="reportTypeTag(scope.row.reportType)">{{ scope.row.reportType || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="举报内容" align="center" prop="reportContent" min-width="200" show-overflow-tooltip />
      <el-table-column label="处理状态" align="center" prop="handleStatus" width="100">
        <template #default="scope">
          <el-tag :type="handleStatusTag(scope.row.handleStatus)">{{ handleStatusLabel(scope.row.handleStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理时间" align="center" prop="handleTime" width="120">
        <template #default="scope">{{ parseTime(scope.row.handleTime, '{y}-{m}-{d}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="success" icon="Finished" :disabled="scope.row.handleStatus !== '0'" @click="openHandle(scope.row)" v-hasPermi="['trade:report:edit']">处理</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:report:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 举报详情抽屉 -->
    <el-drawer v-model="viewOpen" title="举报详情" size="520px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="举报ID">{{ viewData.reportId }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="handleStatusTag(viewData.handleStatus)">{{ handleStatusLabel(viewData.handleStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="举报类型">
          <el-tag :type="reportTypeTag(viewData.reportType)">{{ viewData.reportType || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="被举报商品ID">{{ viewData.goodsId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联订单ID">{{ viewData.orderId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="举报人ID">{{ viewData.reportUserId }}</el-descriptions-item>
        <el-descriptions-item label="被举报人ID">{{ viewData.reportedUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="举报内容">{{ viewData.reportContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证据图片">
          <div v-if="evidenceList(viewData.evidenceImages).length" class="evidence-grid">
            <el-image
              v-for="(img, i) in evidenceList(viewData.evidenceImages)"
              :key="i"
              :src="img"
              :preview-src-list="evidenceList(viewData.evidenceImages)"
              :initial-index="i"
              preview-teleported
              fit="cover"
              class="evidence-img"
            />
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理管理员ID">{{ viewData.handleUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ parseTime(viewData.handleTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ viewData.handleResult || '暂未处理' }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">{{ parseTime(viewData.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- 处理举报对话框 -->
    <el-dialog title="处理举报" v-model="handleOpen" width="560px" append-to-body>
      <el-form ref="handleRef" :model="handleForm" :rules="handleRules" label-width="auto">
        <el-form-item label="处理结论" prop="handleStatus">
          <el-radio-group v-model="handleForm.handleStatus">
            <el-radio label="1">举报成立（通过）</el-radio>
            <el-radio label="2">举报不实（驳回）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="5" placeholder="请填写处理说明，将记录到该举报的处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitHandle">确认处理</el-button>
          <el-button @click="handleOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Report">
import { listReport, delReport, handleReport, batchHandleReport } from "@/api/trade/report"

const { proxy } = getCurrentInstance()
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 枚举（以 DDL 为准）
const REPORT_TYPES = ['虚假信息', '违禁品', '价格欺诈', '交易纠纷', '其他']
const REPORT_TYPE_TAG = { '虚假信息': 'warning', '违禁品': 'danger', '价格欺诈': 'danger', '交易纠纷': 'primary', '其他': 'info' }
const HANDLE_STATUS = {
  '0': { label: '待处理', type: 'warning' },
  '1': { label: '已处理', type: 'success' },
  '2': { label: '已驳回', type: 'info' }
}
function reportTypeTag(v) { return REPORT_TYPE_TAG[v] || 'info' }
function handleStatusLabel(v) { return (HANDLE_STATUS[String(v)] || {}).label || (v ?? '-') }
function handleStatusTag(v) { return (HANDLE_STATUS[String(v)] || {}).type || 'info' }

function evidenceList(str) {
  if (!str) return []
  return str.split(',').map(s => s.trim()).filter(Boolean).map(u => /^https?:\/\//.test(u) ? u : baseUrl + u)
}

const reportList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const viewOpen = ref(false)
const viewData = ref({})
const handleOpen = ref(false)
const handleForm = reactive({ reportId: null, handleStatus: '1', handleResult: '' })
const handleRules = {
  handleStatus: [{ required: true, message: "请选择处理结论", trigger: "change" }],
  handleResult: [{ required: true, message: "请填写处理结果", trigger: "blur" }]
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  goodsId: undefined,
  reportUserId: undefined,
  reportType: undefined,
  handleStatus: undefined
})

/** 查询举报信息列表 */
function getList() {
  loading.value = true
  listReport(queryParams).then(response => {
    reportList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索/重置 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.reportId)
  multiple.value = !selection.length
}

/** 查看详情 */
function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

/** 单条处理 */
function openHandle(row) {
  handleForm.reportId = row.reportId
  handleForm.handleStatus = '1'
  handleForm.handleResult = ''
  handleOpen.value = true
}
function submitHandle() {
  proxy.$refs["handleRef"].validate(valid => {
    if (!valid) return
    handleReport({ ...handleForm }).then(() => {
      proxy.$modal.msgSuccess("处理成功")
      handleOpen.value = false
      getList()
    })
  })
}

/** 批量处理 */
function handleBatch(status) {
  const action = status === '1' ? '通过' : '驳回'
  proxy.$modal.confirm(`是否确认批量${action}选中的 ${ids.value.length} 条举报？`).then(() => {
    return batchHandleReport({ reportIds: ids.value, handleStatus: status, handleResult: `批量${action}` })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(`批量${action}成功`)
  }).catch(() => {})
}

/** 删除 */
function handleDelete(row) {
  const _reportIds = row.reportId || ids.value
  proxy.$modal.confirm('是否确认删除举报信息编号为"' + _reportIds + '"的数据项？').then(function() {
    return delReport(_reportIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出 */
function handleExport() {
  proxy.download('trade/report/export', { ...queryParams }, `report_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.text-secondary { color: var(--el-text-color-secondary); font-size: 12px; }
.evidence-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.evidence-img { width: 72px; height: 72px; border-radius: 8px; border: 1px solid var(--el-border-color-lighter); }
</style>
