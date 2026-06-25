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
      <el-form-item label="评价人ID" prop="fromUserId">
        <el-input
          v-model="queryParams.fromUserId"
          placeholder="请输入评价人ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="被评价人ID" prop="toUserId">
        <el-input
          v-model="queryParams.toUserId"
          placeholder="请输入被评价人ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="评分：1-5" prop="score">
        <el-input
          v-model="queryParams.score"
          placeholder="请输入评分：1-5"
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
          v-hasPermi="['trade:evaluation:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:evaluation:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:evaluation:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:evaluation:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="evaluationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="90" />
      <el-table-column label="评价人ID" align="center" prop="fromUserId" width="100" />
      <el-table-column label="被评价人ID" align="center" prop="toUserId" width="110" />
      <el-table-column label="评分" align="center" prop="score" width="150">
        <template #default="scope">
          <el-rate :model-value="Number(scope.row.score) || 0" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column label="评价内容" align="center" prop="content" min-width="220" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ stripHtml(scope.row.content) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评价时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:evaluation:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:evaluation:remove']">删除</el-button>
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

    <!-- 添加或修改交易评价对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="evaluationRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单ID" prop="orderId">
              <el-input v-model="form.orderId" placeholder="请输入订单ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评价人ID" prop="fromUserId">
              <el-input v-model="form.fromUserId" placeholder="请输入评价人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="被评价人ID" prop="toUserId">
              <el-input v-model="form.toUserId" placeholder="请输入被评价人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评分：1-5" prop="score">
              <el-input v-model="form.score" placeholder="请输入评分：1-5" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评价内容">
              <editor v-model="form.content" :min-height="192"/>
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

    <!-- 评价详情抽屉 -->
    <el-drawer v-model="viewOpen" title="评价详情" size="460px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="评价ID">{{ viewData.evaluationId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ viewData.orderId }}</el-descriptions-item>
        <el-descriptions-item label="评价人ID">{{ viewData.fromUserId }}</el-descriptions-item>
        <el-descriptions-item label="被评价人ID">{{ viewData.toUserId }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate :model-value="Number(viewData.score) || 0" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容">
          <div class="rich-html" v-html="viewData.content || '-'"></div>
        </el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ parseTime(viewData.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="Evaluation">
import { listEvaluation, getEvaluation, delEvaluation, addEvaluation, updateEvaluation } from "@/api/trade/evaluation"

const { proxy } = getCurrentInstance()

const evaluationList = ref([])
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

function stripHtml(s) { return String(s || '').replace(/<[^>]+>/g, '').trim() }

/** 查看评价详情 */
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
    fromUserId: undefined,
    toUserId: undefined,
    score: undefined,
    content: undefined,
  },
  rules: {
    orderId: [
      { required: true, message: "订单ID不能为空", trigger: "blur" }
    ],
    fromUserId: [
      { required: true, message: "评价人ID不能为空", trigger: "blur" }
    ],
    toUserId: [
      { required: true, message: "被评价人ID不能为空", trigger: "blur" }
    ],
    score: [
      { required: true, message: "评分：1-5不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询交易评价列表 */
function getList() {
  loading.value = true
  listEvaluation(queryParams.value).then(response => {
    evaluationList.value = response.rows
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
    evaluationId: null,
    orderId: null,
    fromUserId: null,
    toUserId: null,
    score: null,
    content: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("evaluationRef")
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
  ids.value = selection.map(item => item.evaluationId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加交易评价"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _evaluationId = row.evaluationId || ids.value
  getEvaluation(_evaluationId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改交易评价"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["evaluationRef"].validate(valid => {
    if (valid) {
      if (form.value.evaluationId != null) {
        updateEvaluation(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addEvaluation(form.value).then(() => {
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
  const _evaluationIds = row.evaluationId || ids.value
  proxy.$modal.confirm('是否确认删除交易评价编号为"' + _evaluationIds + '"的数据项？').then(function() {
    return delEvaluation(_evaluationIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/evaluation/export', {
    ...queryParams.value
  }, `evaluation_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style lang="scss" scoped>
.rich-html {
  max-height: 240px;
  overflow: auto;
  line-height: 1.6;
  :deep(img) { max-width: 100%; }
}
</style>
