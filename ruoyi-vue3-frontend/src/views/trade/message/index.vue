<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联商品ID" prop="goodsId">
        <el-input
          v-model="queryParams.goodsId"
          placeholder="请输入关联商品ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联订单ID" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入关联订单ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发送人ID" prop="senderId">
        <el-input
          v-model="queryParams.senderId"
          placeholder="请输入发送人ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收人ID" prop="receiverId">
        <el-input
          v-model="queryParams.receiverId"
          placeholder="请输入接收人ID"
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
          v-hasPermi="['trade:message:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:message:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:message:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:message:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="消息ID" align="center" prop="messageId" />
      <el-table-column label="关联商品ID" align="center" prop="goodsId" />
      <el-table-column label="关联订单ID" align="center" prop="orderId" />
      <el-table-column label="发送人ID" align="center" prop="senderId" />
      <el-table-column label="接收人ID" align="center" prop="receiverId" />
      <el-table-column label="消息内容" align="center" prop="content" />
      <el-table-column label="阅读状态：0未读，1已读" align="center" prop="readStatus" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:message:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:message:remove']">删除</el-button>
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

    <!-- 添加或修改私信消息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="messageRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="关联商品ID" prop="goodsId">
              <el-input v-model="form.goodsId" placeholder="请输入关联商品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联订单ID" prop="orderId">
              <el-input v-model="form.orderId" placeholder="请输入关联订单ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发送人ID" prop="senderId">
              <el-input v-model="form.senderId" placeholder="请输入发送人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接收人ID" prop="receiverId">
              <el-input v-model="form.receiverId" placeholder="请输入接收人ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="消息内容">
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
  </div>
</template>

<script setup name="Message">
import { listMessage, getMessage, delMessage, addMessage, updateMessage } from "@/api/trade/message"

const { proxy } = getCurrentInstance()

const messageList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    goodsId: undefined,
    orderId: undefined,
    senderId: undefined,
    receiverId: undefined,
    content: undefined,
    readStatus: undefined,
  },
  rules: {
    senderId: [
      { required: true, message: "发送人ID不能为空", trigger: "blur" }
    ],
    receiverId: [
      { required: true, message: "接收人ID不能为空", trigger: "blur" }
    ],
    content: [
      { required: true, message: "消息内容不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询私信消息列表 */
function getList() {
  loading.value = true
  listMessage(queryParams.value).then(response => {
    messageList.value = response.rows
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
    messageId: null,
    goodsId: null,
    orderId: null,
    senderId: null,
    receiverId: null,
    content: null,
    readStatus: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("messageRef")
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
  ids.value = selection.map(item => item.messageId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加私信消息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _messageId = row.messageId || ids.value
  getMessage(_messageId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改私信消息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["messageRef"].validate(valid => {
    if (valid) {
      if (form.value.messageId != null) {
        updateMessage(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMessage(form.value).then(() => {
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
  const _messageIds = row.messageId || ids.value
  proxy.$modal.confirm('是否确认删除私信消息编号为"' + _messageIds + '"的数据项？').then(function() {
    return delMessage(_messageIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/message/export', {
    ...queryParams.value
  }, `message_${new Date().getTime()}.xlsx`)
}

getList()
</script>
