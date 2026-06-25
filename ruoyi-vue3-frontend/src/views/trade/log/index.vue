<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入订单ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人ID" prop="operatorId">
        <el-input
          v-model="queryParams.operatorId"
          placeholder="请输入操作人ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['trade:log:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:log:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:log:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:log:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="90" />
      <el-table-column label="操作人ID" align="center" prop="operatorId" width="90" />
      <el-table-column label="操作人类型" align="center" prop="operatorType" width="110">
        <template #default="scope">
          <el-tag :type="operatorTypeType(scope.row.operatorType)">{{ operatorTypeLabel(scope.row.operatorType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态流转" align="center" width="220">
        <template #default="scope">
          <span class="status-flow">
            <el-tag :type="orderStatusType(scope.row.beforeStatus)" size="small">{{ orderStatusLabel(scope.row.beforeStatus) }}</el-tag>
            <span class="flow-arrow">→</span>
            <el-tag :type="orderStatusType(scope.row.afterStatus)" size="small">{{ orderStatusLabel(scope.row.afterStatus) }}</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" align="center" prop="operationType" width="100">
        <template #default="scope">
          <el-tag :type="operationTypeType(scope.row.operationType)">{{ operationTypeLabel(scope.row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作说明" align="center" prop="operationContent" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ stripHtml(scope.row.operationContent) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:log:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:log:remove']">删除</el-button>
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

    <!-- 添加或修改订单操作日志对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="logRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单ID" prop="orderId">
              <el-input v-model="form.orderId" placeholder="请输入订单ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作人ID" prop="operatorId">
              <el-input v-model="form.operatorId" placeholder="请输入操作人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作说明">
              <editor v-model="form.operationContent" :min-height="192"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 操作日志详情抽屉 -->
    <el-drawer v-model="viewOpen" title="操作日志详情" size="460px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志ID">{{ viewData.logId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ viewData.orderId }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ viewData.operatorId }}</el-descriptions-item>
        <el-descriptions-item label="操作人类型">
          <el-tag :type="operatorTypeType(viewData.operatorType)">{{ operatorTypeLabel(viewData.operatorType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作前状态">
          <el-tag :type="orderStatusType(viewData.beforeStatus)">{{ orderStatusLabel(viewData.beforeStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作后状态">
          <el-tag :type="orderStatusType(viewData.afterStatus)">{{ orderStatusLabel(viewData.afterStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="operationTypeType(viewData.operationType)">{{ operationTypeLabel(viewData.operationType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作说明">
          <div class="rich-html" v-html="viewData.operationContent || '-'"></div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(viewData.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="Log">
import { listLog, getLog, delLog, addLog, updateLog } from "@/api/trade/log"

const { proxy } = getCurrentInstance()

const logList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const viewOpen = ref(false)
const viewData = ref({})

// 订单状态（以 DDL 为准，用于操作前/后状态）
const ORDER_STATUS = {
  '0': { label: '待确认', type: 'info' },
  '1': { label: '已确认/待支付', type: 'warning' },
  '2': { label: '已支付/待交割', type: 'primary' },
  '3': { label: '已完成', type: 'success' },
  '4': { label: '已取消', type: 'info' },
  '5': { label: '争议中', type: 'danger' }
}
const OPERATOR_TYPE = {
  '1': { label: '买家', type: 'primary' },
  '2': { label: '卖家', type: 'success' },
  '3': { label: '管理员', type: 'warning' }
}
const OPERATION_TYPE = {
  create:   { label: '创建', type: 'info' },
  confirm:  { label: '确认', type: 'primary' },
  cancel:   { label: '取消', type: 'danger' },
  pay:      { label: '支付', type: 'warning' },
  complete: { label: '完成', type: 'success' },
  dispute:  { label: '争议', type: 'danger' },
  handle:   { label: '处理', type: 'warning' }
}
function orderStatusLabel(v) { return (ORDER_STATUS[String(v)] || {}).label || (v ?? '-') }
function orderStatusType(v) { return (ORDER_STATUS[String(v)] || {}).type || 'info' }
function operatorTypeLabel(v) { return (OPERATOR_TYPE[String(v)] || {}).label || (v ?? '-') }
function operatorTypeType(v) { return (OPERATOR_TYPE[String(v)] || {}).type || 'info' }
function operationTypeLabel(v) { return (OPERATION_TYPE[v] || {}).label || (v ?? '-') }
function operationTypeType(v) { return (OPERATION_TYPE[v] || {}).type || 'info' }
function stripHtml(s) { return String(s || '').replace(/<[^>]+>/g, '').trim() }

/** 查看日志详情 */
function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderId: undefined,
    operatorId: undefined,
    operatorType: undefined,
    beforeStatus: undefined,
    afterStatus: undefined,
    operationType: undefined,
    operationContent: undefined,
  },
  rules: {
    orderId: [
      { required: true, message: "订单ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询订单操作日志列表 */
function getList() {
  loading.value = true
  listLog(queryParams.value).then(response => {
    logList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    logId: null,
    orderId: null,
    operatorId: null,
    operatorType: null,
    beforeStatus: null,
    afterStatus: null,
    operationType: null,
    operationContent: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("logRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.logId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加订单操作日志"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _logId = row.logId || ids.value
  getLog(_logId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改订单操作日志"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["logRef"].validate(valid => {
    if (valid) {
      if (form.value.logId != null) {
        updateLog(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addLog(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _logIds = row.logId || ids.value
  proxy.$modal.confirm('是否确认删除订单操作日志编号为"' + _logIds + '"的数据项？').then(function() {
    return delLog(_logIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/log/export', {
    ...queryParams.value
  }, `log_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style lang="scss" scoped>
.status-flow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  .flow-arrow {
    color: var(--ct-text-muted);
    font-weight: 600;
  }
}
.rich-html {
  max-height: 240px;
  overflow: auto;
  line-height: 1.6;
  :deep(img) { max-width: 100%; }
}
</style>
