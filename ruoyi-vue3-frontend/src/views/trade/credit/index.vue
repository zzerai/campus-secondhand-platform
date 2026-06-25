<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="学生用户ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="变动类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="全部类型" clearable style="width: 160px">
          <el-option v-for="(v, k) in CHANGE_TYPE" :key="k" :label="v.label" :value="k" />
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
        <el-button type="success" plain icon="EditPen" @click="openAdjust()" v-hasPermi="['trade:credit:adjust']">手动调整</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 流水列表 -->
    <el-table v-loading="loading" :data="logList">
      <el-table-column label="流水ID" prop="logId" align="center" width="80" />
      <el-table-column label="用户ID" prop="userId" align="center" width="90" />
      <el-table-column label="变动类型" prop="changeType" align="center" width="120">
        <template #default="scope">
          <el-tag :type="changeTypeTag(scope.row.changeType)">{{ changeTypeLabel(scope.row.changeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="增减" prop="changeValue" align="center" width="90">
        <template #default="scope">
          <span :class="deltaClass(scope.row.changeValue)">{{ deltaText(scope.row.changeValue) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="变动前 → 后" align="center" width="130">
        <template #default="scope">
          <span class="text-secondary">{{ scope.row.scoreBefore ?? '-' }}</span>
          <span> → </span>
          <span class="credit-score">{{ scope.row.scoreAfter ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="关联业务" align="center" width="140">
        <template #default="scope">
          <div>{{ bizTypeLabel(scope.row.bizType) }}</div>
          <div class="text-secondary" v-if="scope.row.bizId">#{{ scope.row.bizId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="封禁到期" align="center" width="160">
        <template #default="scope">
          <span v-if="scope.row.changeType === 'auto_ban'">
            {{ scope.row.banUntil ? parseTime(scope.row.banUntil) : '永久' }}
          </span>
          <span v-else class="text-secondary">-</span>
        </template>
      </el-table-column>
      <el-table-column label="原因 / 备注" prop="reason" min-width="200" show-overflow-tooltip />
      <el-table-column label="时间" prop="createTime" align="center" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) || '-' }}</template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 手动调整信用分 -->
    <el-dialog v-model="adjustOpen" title="手动调整信用分" width="520px" append-to-body>
      <el-form ref="adjustRef" :model="adjustForm" :rules="adjustRules" label-width="auto">
        <el-form-item label="学生用户ID" prop="userId">
          <el-input v-model="adjustForm.userId" placeholder="请输入学生用户ID" :disabled="adjustLocked" />
        </el-form-item>
        <el-form-item label="增减分值" prop="changeValue">
          <el-input-number v-model="adjustForm.changeValue" :step="1" :precision="0" controls-position="right" style="width: 180px" />
          <span class="form-tip">可负，不可为 0；下穿阈值会自动触发封禁</span>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请填写调整原因（必填，将记入流水）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdjust">确认调整</el-button>
        <el-button @click="adjustOpen = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TradeCredit">
import { listCreditLog, adjustCredit } from '@/api/trade/credit'

const { proxy } = getCurrentInstance()

// 变动类型枚举（与后端 CreditConstants.change_type 对应）
const CHANGE_TYPE = {
  admin_adjust:   { label: '手动调整', type: 'primary' },
  order_complete: { label: '订单完成', type: 'success' },
  order_cancel:   { label: '订单取消', type: 'warning' },
  report_valid:   { label: '举报成立', type: 'danger' },
  dispute_fault:  { label: '争议判责', type: 'danger' },
  evaluation:     { label: '交易评价', type: 'info' },
  auto_ban:       { label: '自动封禁', type: 'danger' },
  ban_release:    { label: '封禁解除', type: 'success' }
}
const BIZ_TYPE = { admin: '管理员', system: '系统', order: '订单', report: '举报', dispute: '争议', evaluation: '评价' }
function changeTypeLabel(v) { return (CHANGE_TYPE[v] || {}).label || (v ?? '-') }
function changeTypeTag(v) { return (CHANGE_TYPE[v] || {}).type || 'info' }
function bizTypeLabel(v) { return BIZ_TYPE[v] || (v ?? '-') }
function deltaText(v) { return v > 0 ? `+${v}` : `${v ?? 0}` }
function deltaClass(v) { return v > 0 ? 'delta-up' : (v < 0 ? 'delta-down' : '') }

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const logList = ref([])
const adjustOpen = ref(false)
const adjustLocked = ref(false)

const queryParams = reactive({ pageNum: 1, pageSize: 10, userId: null, changeType: null })
const adjustForm = reactive({ userId: null, changeValue: null, reason: '' })
const adjustRules = {
  userId: [{ required: true, message: '请输入学生用户ID', trigger: 'blur' }],
  changeValue: [
    { required: true, message: '请输入增减分值', trigger: 'blur' },
    { validator: (rule, value, cb) => (value === 0 ? cb(new Error('调整分值不能为 0')) : cb()), trigger: 'blur' }
  ],
  reason: [{ required: true, message: '请填写调整原因', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listCreditLog(queryParams).then(response => {
    logList.value = response.rows || []
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

/** 打开手动调整（传 userId 则锁定该用户，用于学生页跳转复用） */
function openAdjust(userId) {
  adjustForm.userId = userId ?? null
  adjustForm.changeValue = null
  adjustForm.reason = ''
  adjustLocked.value = !!userId
  adjustOpen.value = true
}
function submitAdjust() {
  proxy.$refs['adjustRef'].validate(valid => {
    if (!valid) return
    adjustCredit(adjustForm).then(res => {
      adjustOpen.value = false
      const r = res.data || {}
      if (r.banType === 'PERMANENT') {
        proxy.$modal.msgWarning('调整成功，该用户已被永久封禁')
      } else if (r.banType === 'TEMP') {
        proxy.$modal.msgWarning(`调整成功，该用户已被临时封禁至 ${parseTime(r.banUntil) || ''}`)
      } else {
        proxy.$modal.msgSuccess('调整成功')
      }
      getList()
    })
  })
}

defineExpose({ openAdjust })

onMounted(() => {
  getList()
})
</script>

<style scoped>
.text-secondary { color: var(--el-text-color-secondary); font-size: 12px; }
.credit-score { font-weight: 600; color: var(--ct-primary); font-variant-numeric: tabular-nums; }
.delta-up { color: #2BA471; font-weight: 600; }
.delta-down { color: #E5685F; font-weight: 600; }
.form-tip { margin-left: 10px; color: var(--el-text-color-secondary); font-size: 12px; }
</style>
