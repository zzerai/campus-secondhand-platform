<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="商品ID" prop="goodsId">
        <el-input
            v-model="queryParams.goodsId"
            placeholder="请输入商品ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="商品标题" prop="title">
        <el-input
            v-model="queryParams.title"
            placeholder="请输入商品标题"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="分类名称" prop="categoryName">
        <el-input
            v-model="queryParams.categoryName"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="新旧程度" prop="quality">
        <el-input
            v-model="queryParams.quality"
            placeholder="请输入新旧程度"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="建议价格" prop="suggestPrice">
        <el-input
            v-model="queryParams.suggestPrice"
            placeholder="请输入AI建议价格"
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
            v-hasPermi="['trade:aiPriceRecord:add']"
        >新增</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['trade:aiPriceRecord:edit']"
        >修改</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['trade:aiPriceRecord:remove']"
        >删除</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['trade:aiPriceRecord:export']"
        >导出</el-button>
      </el-col>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" align="center" prop="recordId" width="90" />
      <el-table-column label="商品ID" align="center" prop="goodsId" width="90" />
      <el-table-column label="商品标题" align="center" prop="title" min-width="140" />
      <el-table-column label="分类名称" align="center" prop="categoryName" width="110" />
      <el-table-column label="新旧程度" align="center" prop="quality" width="100" />
      <el-table-column label="商品描述" align="center" prop="description" min-width="180" show-overflow-tooltip />
      <el-table-column label="AI建议价格" align="center" prop="suggestPrice" width="120" />
      <el-table-column label="估价理由" align="center" prop="priceReason" min-width="180" show-overflow-tooltip />
      <el-table-column label="AI返回结果" align="center" prop="aiResult" min-width="180" show-overflow-tooltip />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['trade:aiPriceRecord:edit']"
          >修改</el-button>

          <el-button
              link
              type="primary"
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['trade:aiPriceRecord:remove']"
          >删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="auto">
        <el-form-item label="商品ID" prop="goodsId">
          <el-input v-model="form.goodsId" placeholder="请输入商品ID" />
        </el-form-item>

        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>

        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="新旧程度" prop="quality">
          <el-input v-model="form.quality" placeholder="请输入新旧程度" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入商品描述" />
        </el-form-item>

        <el-form-item label="AI建议价格" prop="suggestPrice">
          <el-input v-model="form.suggestPrice" placeholder="请输入AI建议价格" />
        </el-form-item>

        <el-form-item label="估价理由" prop="priceReason">
          <el-input v-model="form.priceReason" type="textarea" placeholder="请输入估价理由" />
        </el-form-item>

        <el-form-item label="AI返回结果" prop="aiResult">
          <el-input v-model="form.aiResult" type="textarea" placeholder="请输入AI返回完整结果" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
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

<script setup name="AiPriceRecord">
import {
  listAiPriceRecord,
  getAiPriceRecord,
  delAiPriceRecord,
  addAiPriceRecord,
  updateAiPriceRecord
} from "@/api/trade/aiPriceRecord"

const { proxy } = getCurrentInstance()

const recordList = ref([])
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
    title: undefined,
    categoryName: undefined,
    quality: undefined,
    description: undefined,
    suggestPrice: undefined,
    priceReason: undefined,
    aiResult: undefined
  },
  rules: {}
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAiPriceRecord(queryParams.value).then(response => {
    recordList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    recordId: null,
    goodsId: null,
    title: null,
    categoryName: null,
    quality: null,
    description: null,
    suggestPrice: null,
    priceReason: null,
    aiResult: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("recordRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recordId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加AI估价记录"
}

function handleUpdate(row) {
  reset()
  const _recordId = row.recordId || ids.value
  getAiPriceRecord(_recordId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改AI估价记录"
  })
}

function submitForm() {
  proxy.$refs["recordRef"].validate(valid => {
    if (valid) {
      if (form.value.recordId != null) {
        updateAiPriceRecord(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addAiPriceRecord(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _recordIds = row.recordId || ids.value
  proxy.$modal.confirm('是否确认删除AI估价记录编号为"' + _recordIds + '"的数据项？').then(() => {
    return delAiPriceRecord(_recordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('trade/aiPriceRecord/export', {
    ...queryParams.value
  }, `aiPriceRecord_${new Date().getTime()}.xlsx`)
}

getList()
</script>
