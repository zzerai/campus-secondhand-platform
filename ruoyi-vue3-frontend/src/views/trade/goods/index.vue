<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊" clearable @keyup.enter="handleQuery" style="width: 200px"/>
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 180px">
          <el-option v-for="c in categoryOptions" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="商品状态" prop="goodsStatus">
        <el-select v-model="queryParams.goodsStatus" placeholder="全部状态" clearable style="width: 160px">
          <el-option v-for="d in trade_goods_status" :key="d.value" :label="d.label" :value="d.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="卖家学号" prop="sellerStudentNo">
        <el-input v-model="queryParams.sellerStudentNo" placeholder="学号模糊" clearable @keyup.enter="handleQuery" style="width: 180px"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trade:goods:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="!hasPendingSelected" @click="handleBatchAudit('1')" v-hasPermi="['trade:goods:audit']">
          批量通过
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Close" :disabled="!hasPendingSelected" @click="handleBatchAudit('2')" v-hasPermi="['trade:goods:audit']">
          批量拒绝
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="MagicStick" :disabled="!hasPendingSelected" :loading="batchAiAuditing" @click="handleBatchAiAudit" v-hasPermi="['trade:goods:audit']">
          批量AI审核
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trade:goods:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['trade:goods:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['trade:goods:import']">导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="goodsList" @selection-change="handleSelectionChange" stripe>
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" align="center" prop="goodsId" width="70"/>
      <el-table-column label="商品标题" align="left" prop="title" min-width="180" show-overflow-tooltip/>
      <el-table-column label="分类" align="center" prop="categoryName" width="120"/>
      <el-table-column label="卖家" align="center" min-width="140">
        <template #default="{ row }">
          <span>{{ row.sellerNickname || '—' }}</span>
          <div v-if="row.sellerStudentNo" style="color:#909399;font-size:12px;">{{ row.sellerStudentNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="售价" align="right" prop="price" width="100">
        <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="goodsStatus" width="100">
        <template #default="{ row }">
          <dict-tag :options="trade_goods_status" :value="row.goodsStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="浏览/收藏" align="center" width="110">
        <template #default="{ row }">{{ row.viewCount || 0 }} / {{ row.favoriteCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="createTime" width="160">
        <template #default="{ row }">
          <span>{{ parseTime(row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="340">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['trade:goods:query']">详情</el-button>
          <el-button v-if="scope.row.goodsStatus === '0'"
                     link type="success" icon="Check"
                     @click="handleAudit(scope.row)"
                     v-hasPermi="['trade:goods:audit']">审核</el-button>
          <el-button v-if="scope.row.goodsStatus === '0'"
                     link type="warning" icon="MagicStick"
                     :loading="aiAuditing === scope.row.goodsId"
                     @click="handleAiAudit(scope.row)"
                     v-hasPermi="['trade:goods:audit']">AI审核</el-button>
          <el-button v-if="scope.row.goodsStatus === '1'"
                     link type="warning" icon="Bottom"
                     @click="handleOffline(scope.row)"
                     v-hasPermi="['trade:goods:offline']">下架</el-button>
          <el-button v-if="scope.row.goodsStatus === '3'"
                     link type="success" icon="Top"
                     @click="handleOnline(scope.row)"
                     v-hasPermi="['trade:goods:offline']">上架</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:goods:edit']">修改</el-button>
          <el-button link type="danger"  icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:goods:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total"
                v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="goodsRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="卖家ID" prop="sellerId">
              <el-input v-model="form.sellerId" placeholder="学生用户ID" :disabled="!!form.goodsId"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
                <el-option v-for="c in categoryOptions" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="商品标题" maxlength="100" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input v-model="form.price" placeholder="售价" type="number"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input v-model="form.originalPrice" placeholder="原价（可选）" type="number"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新旧程度" prop="quality">
              <el-input v-model="form.quality" placeholder="如 9 成新"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contactWay">
              <el-input v-model="form.contactWay" placeholder="联系方式"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易地点" prop="tradePlace">
              <el-input v-model="form.tradePlace" placeholder="建议交易地点"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog title="商品审核" v-model="auditOpen" width="520px" append-to-body>
      <el-form :model="auditForm" :rules="auditRules" ref="auditRef" label-width="80px">
        <el-form-item label="商品">
          <span>{{ auditForm.title }}</span>
        </el-form-item>
        <el-form-item label="审核结果" prop="goodsStatus">
          <el-radio-group v-model="auditForm.goodsStatus">
            <el-radio value="1">通过</el-radio>
            <el-radio value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="拒绝时必填"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAudit">提 交</el-button>
        <el-button @click="auditOpen=false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- AI审核结果弹窗 -->
    <el-dialog title="AI 审核结果" v-model="aiResultOpen" width="540px" append-to-body class="ai-result-dialog">
      <div v-if="aiResult" class="airx">
        <!-- 结论卡片 -->
        <div class="airx-verdict" :class="'airx-verdict--' + aiRiskTheme(aiResult.riskLevel).cls">
          <div class="airx-verdict__icon">
            <el-icon><component :is="aiRiskTheme(aiResult.riskLevel).icon" /></el-icon>
          </div>
          <div class="airx-verdict__body">
            <div class="airx-verdict__suggestion">{{ aiResult.suggestion || '暂无建议' }}</div>
            <div class="airx-verdict__meta">
              <span class="airx-verdict__risk">{{ aiRiskTheme(aiResult.riskLevel).label }}</span>
              <span class="airx-verdict__sep">·</span>
              <span class="airx-verdict__title">{{ aiResult._title || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- 风险原因 / 审核依据 -->
        <div class="airx-note" :class="'airx-note--' + aiRiskTheme(aiResult.riskLevel).cls">
          <div class="airx-note__title">
            <el-icon><Document /></el-icon>风险原因 / 审核依据
          </div>
          <div class="airx-note__text">{{ aiResult.riskReason || '（AI 未给出具体说明）' }}</div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="aiResultOpen = false">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="商品详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="ID">{{ detail.goodsId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="trade_goods_status" :value="detail.goodsStatus"/>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detail.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="新旧程度">{{ detail.quality || '—' }}</el-descriptions-item>
        <el-descriptions-item label="售价">¥{{ formatMoney(detail.price) }}</el-descriptions-item>
        <el-descriptions-item label="原价">{{ detail.originalPrice ? '¥' + formatMoney(detail.originalPrice) : '—' }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ detail.sellerNickname || '—' }}（{{ detail.sellerStudentNo || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detail.contactWay || '—' }}</el-descriptions-item>
        <el-descriptions-item label="交易地点" :span="2">{{ detail.tradePlace || '—' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          <div style="white-space: pre-wrap">{{ detail.description || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="浏览/收藏">{{ detail.viewCount || 0 }} / {{ detail.favoriteCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditUserName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detail.auditTime ? parseTime(detail.auditTime) : '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.auditRemark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail && detail.images && detail.images.length" style="margin-top: 12px;">
        <div style="margin-bottom:8px;color:#606266;">商品图片：</div>
        <el-image v-for="img in detail.images" :key="img.imageId"
                  :src="resolveImageUrl(img.imageUrl)"
                  :preview-src-list="detail.images.map(i => resolveImageUrl(i.imageUrl))"
                  fit="cover"
                  style="width:96px;height:96px;margin-right:8px;border-radius:4px;border:1px solid #ebeef5;"/>
      </div>

      <!-- AI 审核意见：打开详情时自动加载最近一次记录，可重新分析 -->
      <el-divider content-position="left">AI 审核意见</el-divider>
      <div v-loading="detailAiLoading" style="min-height:48px;">
        <template v-if="detailAi">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="风险等级">
              <el-tag :type="aiRiskTagType(detailAi.riskLevel)">{{ aiRiskLabel(detailAi.riskLevel) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核建议">
              <el-tag :type="aiSuggestionTagType(detailAi.suggestion)">{{ detailAi.suggestion || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="分析时间">{{ parseTime(detailAi.createTime) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="风险原因" :span="3">
              <div style="white-space:pre-wrap">{{ detailAi.riskReason || '—' }}</div>
            </el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:8px;text-align:right;">
            <el-button link type="warning" icon="Refresh" :loading="detailAiAnalyzing"
                       @click="detailReAnalyze" v-hasPermi="['trade:goods:audit']">重新分析</el-button>
          </div>
        </template>
        <el-empty v-else-if="!detailAiLoading" :image-size="50" description="暂无 AI 审核意见">
          <el-button type="warning" plain icon="MagicStick" :loading="detailAiAnalyzing"
                     @click="detailReAnalyze" v-hasPermi="['trade:goods:audit']">发起 AI 审核</el-button>
        </el-empty>
      </div>

      <template #footer>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <div>
            <template v-if="detail && detail.goodsStatus === '0'">
              <el-button type="danger" icon="Close" @click="detailAudit('2')" v-hasPermi="['trade:goods:audit']">拒绝</el-button>
              <el-button type="success" icon="Check" @click="detailAudit('1')" v-hasPermi="['trade:goods:audit']">通过</el-button>
            </template>
          </div>
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入商品对话框 -->
    <el-dialog v-model="importOpen" title="导入商品" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx,.xls"
        :disabled="importLoading"
        :http-request="handleUploadRequest"
        :before-upload="beforeUpload"
        :on-progress="handleProgress"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :auto-upload="true"
      >
        <el-button type="primary" icon="Upload">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            <el-link type="info" style="font-size: 12px" href="#" @click="downloadTemplate">下载导入模板</el-link>
            <div>仅支持.xlsx/.xls格式文件，文件大小不超过5MB</div>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Goods">
import { listGoods, getGoods, delGoods, addGoods, updateGoods,
         auditGoods, offlineGoods, onlineGoods, importGoods, importGoodsTemplate, aiAuditGoods } from "@/api/trade/goods"
import { listCategory } from "@/api/trade/category"
import { listAiAuditRecord } from "@/api/trade/aiAuditRecord"
import { ElMessageBox } from "element-plus"

const { proxy } = getCurrentInstance()
const { trade_goods_status } = useDict('trade_goods_status')

const goodsList = ref([])
const open = ref(false)
const auditOpen = ref(false)
const detailOpen = ref(false)
const importOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const selectionRows = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const detail = ref(null)
const categoryOptions = ref([])
const importLoading = ref(false)
const aiAuditing = ref(null)
const batchAiAuditing = ref(false)
const aiResultOpen = ref(false)
const aiResult = ref(null)
const detailAi = ref(null)
const detailAiLoading = ref(false)
const detailAiAnalyzing = ref(false)

// 获取 token（支持 localStorage 和 cookie）
const getToken = () => {
  // 优先 localStorage
  let token = localStorage.getItem('Access-Token') || localStorage.getItem('token')
  // 如果都没有，尝试从 cookie 获取 Admin-Token
  if (!token) {
    const match = document.cookie.match(/(^|;) ?Admin-Token=([^;]*)(;|$)/)
    if (match) {
      token = decodeURIComponent(match[2])
    }
  }
  return token || ''
}

// 自定义上传请求（确保带 Authorization header）
const handleUploadRequest = async (options) => {
  const { file, onProgress, onSuccess, onError } = options

  importLoading.value = true

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('updateSupport', false) // 默认不启用更新

    const token = getToken()
    const baseUrl = import.meta.env.VITE_APP_BASE_API || '/dev-api'
    const response = await fetch(
      baseUrl + '/trade/goods/import',
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
          // 注意：如果后端需要 application/json，不要手动设置 Content-Type，让浏览器自动设置
        },
        body: formData
      }
    )

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || `HTTP ${response.status}`)
    }

    const result = await response.json()
    onSuccess(result, file)
  } catch (error) {
    onError(error)
  } finally {
    importLoading.value = false
  }
}

function formatMoney(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  return Number(v).toFixed(2)
}
function resolveImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return (import.meta.env.VITE_APP_BASE_API || '') + url
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    categoryId: undefined,
    goodsStatus: undefined,
    sellerStudentNo: undefined
  },
  rules: {
    sellerId:   [{ required: true, message: "卖家ID不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
    title:      [{ required: true, message: "标题不能为空", trigger: "blur" }],
    price:      [{ required: true, message: "售价不能为空", trigger: "blur" }]
  },
  auditForm: { goodsId: null, goodsStatus: '1', auditRemark: '', title: '' },
  auditRules: {
    goodsStatus: [{ required: true, message: "请选择审核结果", trigger: "change" }],
    auditRemark: [
      {
        validator: (rule, value, callback) => {
          if (data.auditForm.goodsStatus === '2' && !value) {
            return callback(new Error('拒绝时必须填写审核意见'))
          }
          callback()
        }, trigger: 'blur'
      }
    ]
  }
})

const { queryParams, form, rules, auditForm, auditRules } = toRefs(data)

const hasPendingSelected = computed(() =>
  selectionRows.value.length > 0 && selectionRows.value.some(r => r.goodsStatus === '0')
)

/** 从选中行中提取待审核商品的 goodsId */
function pendingIds() {
  return selectionRows.value.filter(r => r.goodsStatus === '0').map(r => r.goodsId)
}

function loadCategories() {
  listCategory({ pageNum: 1, pageSize: 200 }).then(res => {
    categoryOptions.value = (res.rows || []).filter(c => c.status === '0' || c.status === undefined || c.status === null)
  })
}

function getList() {
  loading.value = true
  listGoods(queryParams.value).then(response => {
    goodsList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }

function reset() {
  form.value = {
    goodsId: null, sellerId: null, categoryId: null, title: null,
    price: null, originalPrice: null, quality: null, description: null,
    tradePlace: null, contactWay: null, goodsStatus: null,
    remark: null
  }
  proxy.resetForm("goodsRef")
}

function handleQuery()  { queryParams.value.pageNum = 1; getList() }
function resetQuery()   { proxy.resetForm("queryRef"); handleQuery() }

function handleSelectionChange(selection) {
  selectionRows.value = selection
  ids.value = selection.map(i => i.goodsId)
  single.value   = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); open.value = true; title.value = "新增商品" }

function handleUpdate(row) {
  reset()
  const id = row.goodsId || ids.value[0]
  getGoods(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商品"
  })
}

function submitForm() {
  proxy.$refs["goodsRef"].validate(valid => {
    if (!valid) return
    const ok = form.value.goodsId ? updateGoods(form.value) : addGoods(form.value)
    ok.then(() => {
      proxy.$modal.msgSuccess(form.value.goodsId ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const _ids = row.goodsId || ids.value
  proxy.$modal.confirm('是否确认删除商品编号为"' + _ids + '"的数据项？')
    .then(() => delGoods(_ids))
    .then(() => { getList(); proxy.$modal.msgSuccess("删除成功") })
    .catch(() => {})
}

function handleExport() {
  proxy.download('trade/goods/export', { ...queryParams.value }, `goods_${new Date().getTime()}.xlsx`)
}

function handleDetail(row) {
  detailAi.value = null
  getGoods(row.goodsId).then(res => {
    detail.value = res.data
    detailOpen.value = true
  })
  loadDetailAi(row.goodsId)
}

/** 加载该商品最近一次 AI 审核意见（只读历史记录，不触发新的 AI 调用） */
function loadDetailAi(goodsId) {
  detailAiLoading.value = true
  listAiAuditRecord({ businessType: 'goods', businessId: goodsId, pageNum: 1, pageSize: 20 }).then(res => {
    const rows = res.rows || []
    detailAi.value = rows.sort((a, b) => (b.recordId || 0) - (a.recordId || 0))[0] || null
  }).finally(() => {
    detailAiLoading.value = false
  })
}

/** 详情页内重新发起 AI 审核（会新增一条 AI 记录，消耗一次模型调用） */
function detailReAnalyze() {
  if (!detail.value) return
  detailAiAnalyzing.value = true
  aiAuditGoods(detail.value.goodsId).then(() => {
    proxy.$modal.msgSuccess('AI 审核完成')
    loadDetailAi(detail.value.goodsId)
  }).catch(() => {}).finally(() => {
    detailAiAnalyzing.value = false
  })
}

/** 详情页内直接审核（仅待审核商品，结合上方 AI 意见一处决策） */
function detailAudit(status) {
  if (!detail.value) return
  const id = detail.value.goodsId
  if (status === '2') {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: v => (!!v && v.trim().length > 0) || '拒绝必须填写原因'
    }).then(({ value }) => {
      auditGoods({ goodsId: id, goodsStatus: '2', auditRemark: value }).then(() => {
        proxy.$modal.msgSuccess('已拒绝')
        detailOpen.value = false
        getList()
      })
    }).catch(() => {})
  } else {
    proxy.$modal.confirm('确认通过该商品审核？').then(() => {
      return auditGoods({ goodsId: id, goodsStatus: '1', auditRemark: '' })
    }).then(() => {
      proxy.$modal.msgSuccess('已通过')
      detailOpen.value = false
      getList()
    }).catch(() => {})
  }
}

function handleAudit(row) {
  auditForm.value = { goodsId: row.goodsId, goodsStatus: '1', auditRemark: '', title: row.title }
  auditOpen.value = true
  nextTick(() => proxy.$refs['auditRef']?.clearValidate())
}

function submitAudit() {
  proxy.$refs['auditRef'].validate(valid => {
    if (!valid) return
    auditGoods({
      goodsId: auditForm.value.goodsId,
      goodsStatus: auditForm.value.goodsStatus,
      auditRemark: auditForm.value.auditRemark
    }).then(() => {
      proxy.$modal.msgSuccess("审核已提交")
      auditOpen.value = false
      getList()
    })
  })
}

function handleBatchAudit(targetStatus) {
  const ids = pendingIds()
  const skipped = selectionRows.value.length - ids.length
  if (ids.length === 0) {
    proxy.$modal.msgWarning('所选商品中没有待审核商品')
    return
  }
  const tip = targetStatus === '1' ? '通过' : '拒绝'
  let confirmMsg = `确认批量${tip} ${ids.length} 条待审核商品？`
  if (skipped > 0) {
    confirmMsg += `（已跳过 ${skipped} 条非待审核商品）`
  }
  if (targetStatus === '2') {
    ElMessageBox.prompt('请输入拒绝原因（批量）', '批量拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: v => !!v && v.trim().length > 0 || '拒绝必须填写原因'
    }).then(({ value }) => {
      doBatch(ids, targetStatus, value)
    }).catch(() => {})
  } else {
    proxy.$modal.confirm(confirmMsg)
      .then(() => doBatch(ids, targetStatus, ''))
      .catch(() => {})
  }
}
function doBatch(ids, status, remark) {
  auditGoods({ goodsIds: ids, goodsStatus: status, auditRemark: remark }).then((res) => {
    const n = res?.data?.affected ?? ids.length
    proxy.$modal.msgSuccess(`已处理 ${n} 条`)
    getList()
  })
}

function handleOffline(row) {
  proxy.$modal.confirm(`确认强制下架商品「${row.title}」？`).then(() => {
    return offlineGoods(row.goodsId)
  }).then(() => {
    proxy.$modal.msgSuccess("已下架")
    getList()
  }).catch(() => {})
}
function handleOnline(row) {
  proxy.$modal.confirm(`确认恢复上架商品「${row.title}」？`).then(() => {
    return onlineGoods(row.goodsId)
  }).then(() => {
    proxy.$modal.msgSuccess("已上架")
    getList()
  }).catch(() => {})
}

/** 单条AI审核 */
function handleAiAudit(row) {
  aiAuditing.value = row.goodsId
  aiAuditGoods(row.goodsId).then(res => {
    aiResult.value = { ...res, _title: row.title }
    aiResultOpen.value = true
    getList()
  }).catch(() => {}).finally(() => {
    aiAuditing.value = null
  })
}

/** 批量AI审核 */
function handleBatchAiAudit() {
  const ids = pendingIds()
  if (ids.length === 0) {
    proxy.$modal.msgWarning('所选商品中没有待审核商品')
    return
  }
  proxy.$modal.confirm(`确认对 ${ids.length} 条待审核商品进行批量AI审核？`).then(async () => {
    batchAiAuditing.value = true
    let done = 0
    for (const goodsId of ids) {
      try {
        await aiAuditGoods(goodsId)
        done++
      } catch (e) {
        console.error('AI审核失败', goodsId, e)
      }
    }
    batchAiAuditing.value = false
    proxy.$modal.msgSuccess(`批量AI审核完成：${done}/${ids.length}`)
    getList()
  }).catch(() => {})
}

/** AI 风险等级显示文本（兼容后端 low/middle/high 与中文） */
function aiRiskLabel(level) {
  if (!level) return '—'
  return ({ low: '低', middle: '中', high: '高' })[level] || level
}

/** AI 风险等级 tag 类型 */
function aiRiskTagType(level) {
  if (!level) return 'info'
  const l = String(level).toLowerCase()
  if (l === 'high' || level.includes('高') || level.includes('严重')) return 'danger'
  if (l === 'middle' || level.includes('中')) return 'warning'
  if (l === 'low' || level.includes('低')) return 'success'
  return 'info'
}

/** AI 审核建议 tag 类型 */
function aiSuggestionTagType(s) {
  if (!s) return ''
  if (s.includes('拒绝') || s.includes('不通过') || s.includes('违规')) return 'danger'
  if (s.includes('人工')) return 'warning'
  if (s.includes('通过') || s.includes('正常')) return 'success'
  return 'info'
}

/** AI 风险等级视觉主题（图标 + 配色类 + 文案），复用 aiRiskTagType 的归一化 */
function aiRiskTheme(level) {
  return ({
    success: { icon: 'CircleCheckFilled', cls: 'low',  label: '低风险' },
    warning: { icon: 'WarningFilled',     cls: 'mid',  label: '中风险' },
    danger:  { icon: 'CircleCloseFilled', cls: 'high', label: '高风险' }
  })[aiRiskTagType(level)] || { icon: 'QuestionFilled', cls: 'unknown', label: '未知风险' }
}

/** 导入按钮操作 */
const handleImport = () => {
  importOpen.value = true
}

/** 上传前的文件检查 */
const beforeUpload = (file) => {
  const isExcel = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isExcel) {
    proxy.$message.error('只能上传.xlsx或.xls格式的文件!')
    return false
  }
  if (!isLt5M) {
    proxy.$message.error('文件大小不能超过5MB!')
    return false
  }
  return true
}

/** 下载导入模板 */
const downloadTemplate = () => {
  importGoodsTemplate().then(blob => {
    // response是Blob对象（request拦截器对blob响应直接返回Blob）
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `商品导入模板_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败')
  })
}

/** 上传中 */
const handleProgress = () => {
  importLoading.value = true
}

/** 导入成功 */
const handleImportSuccess = (response) => {
  importLoading.value = false
  if (response.code === 200) {
    const data = response.data
    proxy.$alert(
      `导入完成！总数：${data.total}，成功：${data.success}，失败：${data.failure}`,
      "导入结果",
      { type: data.failure > 0 ? "warning" : "success" }
    )
    if (data.errors && data.errors.length > 0) {
      console.table(data.errors)
    }
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(response.msg || "导入失败")
  }
}

/** 导入失败 */
const handleImportError = (error) => {
  importLoading.value = false
  console.error('导入失败详情:', error)
  proxy.$modal.msgError("上传失败: " + (error.message || error))
}

loadCategories()
getList()
</script>

<style scoped>
/* ============ AI 审核结果弹窗 ============ */
.ai-result-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}

/* —— 结论卡片 —— */
.airx-verdict {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: var(--ct-radius-lg, 14px);
  border: 1px solid var(--v-bd, var(--ct-border-soft));
  background: var(--v-bg, var(--ct-surface-2));
  margin-bottom: 16px;
}
.airx-verdict__icon {
  flex: none;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  font-size: 26px;
  color: #fff;
  background: var(--v-accent, var(--ct-text-muted));
  box-shadow: 0 4px 12px var(--v-shadow, rgba(16, 24, 40, .12));
}
.airx-verdict__body { min-width: 0; }
.airx-verdict__suggestion {
  font-size: 19px;
  font-weight: 700;
  color: var(--ct-ink);
  line-height: 1.3;
}
.airx-verdict__meta {
  margin-top: 5px;
  font-size: 12.5px;
  color: var(--ct-text-2);
  display: flex;
  align-items: center;
  gap: 7px;
}
.airx-verdict__risk {
  font-weight: 600;
  color: var(--v-accent, var(--ct-text-2));
  flex: none;
}
.airx-verdict__sep { color: var(--ct-border); flex: none; }
.airx-verdict__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.airx-verdict--low  { --v-accent: var(--el-color-success); --v-bg: rgba(43, 164, 113, .07); --v-bd: rgba(43, 164, 113, .22); --v-shadow: rgba(43, 164, 113, .25); }
.airx-verdict--mid  { --v-accent: var(--ct-amber); --v-bg: rgba(224, 163, 59, .08); --v-bd: rgba(224, 163, 59, .24); --v-shadow: rgba(224, 163, 59, .25); }
.airx-verdict--high { --v-accent: var(--ct-coral); --v-bg: rgba(229, 104, 95, .08); --v-bd: rgba(229, 104, 95, .24); --v-shadow: rgba(229, 104, 95, .28); }
.airx-verdict--unknown { --v-accent: var(--ct-text-muted); }

/* —— 风险原因 callout —— */
.airx-note {
  border-radius: var(--ct-radius, 10px);
  padding: 14px 16px;
  background: var(--n-bg, var(--ct-surface-2));
  border-left: 3px solid var(--n-accent, var(--ct-border));
}
.airx-note__title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--n-accent, var(--ct-text));
  margin-bottom: 7px;
}
.airx-note__text {
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--ct-text);
  white-space: pre-wrap;
  word-break: break-word;
}
.airx-note--low  { --n-accent: var(--el-color-success); --n-bg: rgba(43, 164, 113, .05); }
.airx-note--mid  { --n-accent: var(--ct-amber); --n-bg: rgba(224, 163, 59, .06); }
.airx-note--high { --n-accent: var(--ct-coral); --n-bg: rgba(229, 104, 95, .06); }
.airx-note--unknown { --n-accent: var(--ct-text-2); }
</style>
