<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="业务ID" prop="businessId">
        <el-input v-model="queryParams.businessId" placeholder="业务ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="业务类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="(v, k) in BUSINESS_TYPE" :key="k" :label="v.label" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="全部等级" clearable style="width: 140px">
          <el-option v-for="(v, k) in RISK_LEVEL" :key="k" :label="v.label" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核建议" prop="suggestion">
        <el-input v-model="queryParams.suggestion" placeholder="审核建议" clearable @keyup.enter="handleQuery" style="width: 150px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trade:aiAuditRecord:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trade:aiAuditRecord:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="记录ID" align="center" prop="recordId" width="80" />
      <el-table-column label="业务类型" align="center" prop="businessType" width="110">
        <template #default="scope">
          <el-tag :type="businessTypeTag(scope.row.businessType)">{{ businessTypeLabel(scope.row.businessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务ID" align="center" prop="businessId" width="90" />
      <el-table-column label="风险等级" align="center" prop="riskLevel" width="100">
        <template #default="scope">
          <span class="risk-dot" :class="'risk-dot--' + riskTheme(scope.row.riskLevel).cls">
            <i class="risk-dot__pip"></i>{{ riskLevelLabel(scope.row.riskLevel) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="审核建议" align="center" prop="suggestion" width="120">
        <template #default="scope">
          <el-tag :type="suggestionTag(scope.row.suggestion)">{{ scope.row.suggestion || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险原因" align="center" prop="riskReason" min-width="200" show-overflow-tooltip />
      <el-table-column label="AI输入" align="center" prop="inputContent" min-width="160" show-overflow-tooltip />
      <el-table-column label="记录时间" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:aiAuditRecord:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情抽屉 -->
    <el-drawer v-model="viewOpen" title="AI 审核记录详情" size="600px" append-to-body class="ai-drawer">
      <!-- 结论卡片 -->
      <div class="ai-verdict" :class="'ai-verdict--' + riskTheme(viewData.riskLevel).cls">
        <div class="ai-verdict__icon">
          <el-icon><component :is="riskTheme(viewData.riskLevel).icon" /></el-icon>
        </div>
        <div class="ai-verdict__body">
          <div class="ai-verdict__suggestion">{{ viewData.suggestion || '暂无建议' }}</div>
          <div class="ai-verdict__meta">
            <span class="ai-verdict__risk">{{ riskTheme(viewData.riskLevel).label }}</span>
            <span class="ai-verdict__sep">·</span>
            <span>{{ businessTypeLabel(viewData.businessType) }}</span>
            <span class="ai-verdict__sep">·</span>
            <span>业务 ID {{ viewData.businessId ?? '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 元信息条 -->
      <div class="ai-grid">
        <div class="ai-grid__cell">
          <span class="ai-grid__k">记录 ID</span>
          <span class="ai-grid__v">#{{ viewData.recordId }}</span>
        </div>
        <div class="ai-grid__cell">
          <span class="ai-grid__k">业务类型</span>
          <span class="ai-grid__v">
            <el-tag size="small" :type="businessTypeTag(viewData.businessType)">{{ businessTypeLabel(viewData.businessType) }}</el-tag>
          </span>
        </div>
        <div class="ai-grid__cell">
          <span class="ai-grid__k">记录时间</span>
          <span class="ai-grid__v">{{ parseTime(viewData.createTime) || '-' }}</span>
        </div>
      </div>

      <!-- 风险原因 / 审核依据 -->
      <div class="ai-note" :class="'ai-note--' + riskTheme(viewData.riskLevel).cls">
        <div class="ai-note__title">
          <el-icon><Document /></el-icon>风险原因 / 审核依据
        </div>
        <div class="ai-note__text">{{ viewData.riskReason || '（AI 未给出具体说明）' }}</div>
      </div>

      <!-- AI 输入内容 -->
      <div class="ai-section">
        <div class="ai-section__hd">
          <el-icon><Upload /></el-icon><span>AI 输入内容</span>
        </div>
        <pre class="ai-block">{{ prettyJson(viewData.inputContent) }}</pre>
      </div>

      <!-- AI 返回结果 -->
      <div class="ai-section">
        <div class="ai-section__hd">
          <el-icon><MagicStick /></el-icon><span>AI 返回结果</span>
        </div>
        <pre class="ai-block ai-block--accent">{{ prettyJson(viewData.aiResult) }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="AiAuditRecord">
import { listAiAuditRecord, delAiAuditRecord } from "@/api/trade/aiAuditRecord"

const { proxy } = getCurrentInstance()

// 枚举（以 DDL 为准）
const BUSINESS_TYPE = {
  goods: { label: '商品审核', type: 'primary' },
  dispute: { label: '争议仲裁', type: 'warning' },
  report: { label: '举报核查', type: 'danger' }
}
const RISK_LEVEL = {
  low: { label: '低', type: 'success' },
  middle: { label: '中', type: 'warning' },
  high: { label: '高', type: 'danger' }
}
const SUGGESTION_TAG = { '通过': 'success', '拒绝': 'danger', '人工复核': 'warning' }
// 风险等级视觉主题：图标 + 配色类（用于结论卡片 / 列表圆点）
const RISK_THEME = {
  low:    { label: '低风险', icon: 'CircleCheckFilled', cls: 'low' },
  middle: { label: '中风险', icon: 'WarningFilled',     cls: 'mid' },
  high:   { label: '高风险', icon: 'CircleCloseFilled', cls: 'high' }
}
function businessTypeLabel(v) { return (BUSINESS_TYPE[v] || {}).label || (v ?? '-') }
function businessTypeTag(v) { return (BUSINESS_TYPE[v] || {}).type || 'info' }
function riskLevelLabel(v) { return (RISK_LEVEL[v] || {}).label || (v ?? '-') }
function suggestionTag(v) { return SUGGESTION_TAG[v] || 'info' }
function riskTheme(v) { return RISK_THEME[v] || { label: v ?? '未知', icon: 'QuestionFilled', cls: 'unknown' } }

/** 尝试格式化 JSON，失败则原样返回 */
function prettyJson(str) {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

const recordList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const viewOpen = ref(false)
const viewData = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  businessId: undefined,
  businessType: undefined,
  riskLevel: undefined,
  suggestion: undefined
})

function getList() {
  loading.value = true
  listAiAuditRecord(queryParams).then(response => {
    recordList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recordId)
  multiple.value = !selection.length
}

function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

function handleDelete(row) {
  const _recordIds = row.recordId || ids.value
  proxy.$modal.confirm('是否确认删除AI审核记录编号为"' + _recordIds + '"的数据项？').then(() => {
    return delAiAuditRecord(_recordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('trade/aiAuditRecord/export', { ...queryParams }, `aiAuditRecord_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ---- 列表：风险等级圆点 ---- */
.risk-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  font-size: 13px;
}
.risk-dot__pip {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: none;
  box-shadow: 0 0 0 3px var(--pip-halo, transparent);
}
.risk-dot--low  { color: var(--el-color-success); --pip-halo: rgba(43, 164, 113, .14); }
.risk-dot--low  .risk-dot__pip { background: var(--el-color-success); }
.risk-dot--mid  { color: var(--ct-amber); --pip-halo: rgba(224, 163, 59, .16); }
.risk-dot--mid  .risk-dot__pip { background: var(--ct-amber); }
.risk-dot--high { color: var(--ct-coral); --pip-halo: rgba(229, 104, 95, .16); }
.risk-dot--high .risk-dot__pip { background: var(--ct-coral); }
.risk-dot--unknown { color: var(--ct-text-muted); }
.risk-dot--unknown .risk-dot__pip { background: var(--ct-text-muted); }

/* ---- 抽屉容器 ---- */
.ai-drawer :deep(.el-drawer__body) {
  padding: 0 22px 22px;
}

/* ---- 结论卡片 ---- */
.ai-verdict {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: var(--ct-radius-lg);
  border: 1px solid var(--verdict-bd, var(--ct-border-soft));
  background: var(--verdict-bg, var(--ct-surface-2));
  margin-bottom: 16px;
}
.ai-verdict__icon {
  flex: none;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  font-size: 26px;
  color: #fff;
  background: var(--verdict-accent, var(--ct-text-muted));
  box-shadow: 0 4px 12px var(--verdict-shadow, rgba(16, 24, 40, .12));
}
.ai-verdict__suggestion {
  font-size: 19px;
  font-weight: 700;
  color: var(--ct-ink);
  line-height: 1.3;
}
.ai-verdict__meta {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--ct-text-2);
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
}
.ai-verdict__risk {
  font-weight: 600;
  color: var(--verdict-accent, var(--ct-text-2));
}
.ai-verdict__sep { color: var(--ct-border); }

.ai-verdict--low  { --verdict-accent: var(--el-color-success); --verdict-bg: rgba(43, 164, 113, .07); --verdict-bd: rgba(43, 164, 113, .22); --verdict-shadow: rgba(43, 164, 113, .25); }
.ai-verdict--mid  { --verdict-accent: var(--ct-amber); --verdict-bg: rgba(224, 163, 59, .08); --verdict-bd: rgba(224, 163, 59, .24); --verdict-shadow: rgba(224, 163, 59, .25); }
.ai-verdict--high { --verdict-accent: var(--ct-coral); --verdict-bg: rgba(229, 104, 95, .08); --verdict-bd: rgba(229, 104, 95, .24); --verdict-shadow: rgba(229, 104, 95, .28); }
.ai-verdict--unknown { --verdict-accent: var(--ct-text-muted); }

/* ---- 元信息条 ---- */
.ai-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  background: var(--ct-border-soft);
  border: 1px solid var(--ct-border-soft);
  border-radius: var(--ct-radius);
  overflow: hidden;
  margin-bottom: 16px;
}
.ai-grid__cell {
  background: var(--ct-surface);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.ai-grid__k {
  font-size: 12px;
  color: var(--ct-text-muted);
}
.ai-grid__v {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--ct-ink);
  font-variant-numeric: tabular-nums;
}

/* ---- 风险原因 callout ---- */
.ai-note {
  border-radius: var(--ct-radius);
  padding: 14px 16px;
  margin-bottom: 18px;
  background: var(--note-bg, var(--ct-surface-2));
  border-left: 3px solid var(--note-accent, var(--ct-border));
}
.ai-note__title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--note-accent, var(--ct-text));
  margin-bottom: 7px;
}
.ai-note__text {
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--ct-text);
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-note--low  { --note-accent: var(--el-color-success); --note-bg: rgba(43, 164, 113, .05); }
.ai-note--mid  { --note-accent: var(--ct-amber); --note-bg: rgba(224, 163, 59, .06); }
.ai-note--high { --note-accent: var(--ct-coral); --note-bg: rgba(229, 104, 95, .06); }
.ai-note--unknown { --note-accent: var(--ct-text-2); }

/* ---- 输入 / 返回 区块 ---- */
.ai-section { margin-bottom: 16px; }
.ai-section__hd {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ct-ink);
  margin-bottom: 8px;
}
.ai-section__hd .el-icon { color: var(--ct-primary); }

.ai-block {
  margin: 0;
  padding: 12px 14px;
  background: var(--ct-surface-2);
  border: 1px solid var(--ct-border-soft);
  border-radius: var(--ct-radius-sm, 8px);
  font-family: "JetBrains Mono", "Cascadia Code", Consolas, Menlo, monospace;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--ct-text);
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 320px;
  overflow: auto;
}
.ai-block--accent {
  background: var(--ct-primary-050);
  border-color: var(--ct-primary-100);
}
</style>
