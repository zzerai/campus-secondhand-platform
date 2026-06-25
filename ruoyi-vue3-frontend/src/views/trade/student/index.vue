<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="queryParams.studentNo"
          placeholder="请输入学号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
          <el-option label="正常" value="0" />
          <el-option label="临时封禁" value="1" />
          <el-option label="永久封禁" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入昵称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系方式" prop="contactWay">
        <el-input
          v-model="queryParams.contactWay"
          placeholder="请输入联系方式"
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
          v-hasPermi="['trade:student:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:student:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:student:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:student:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          @click="handleImport"
          v-hasPermi="['trade:student:import']"
        >导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="头像" align="center" width="70">
        <template #default="scope">
          <el-image
            v-if="scope.row.avatar"
            :src="scope.row.avatar"
            :preview-src-list="[scope.row.avatar]"
            preview-teleported
            fit="cover"
            class="avatar-thumb"
          />
          <el-avatar v-else :size="34">{{ (scope.row.nickname || '?').charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="昵称" align="center" prop="nickname" min-width="120" show-overflow-tooltip />
      <el-table-column label="学号" align="center" prop="studentNo" width="140" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="信用分" align="center" prop="creditScore" width="90">
        <template #default="scope">
          <span class="credit-score">{{ scope.row.creditScore ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" align="center" prop="lastLoginTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastLoginTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="warning" icon="EditPen" @click="openAdjust(scope.row)" v-hasPermi="['trade:credit:adjust']">信用调整</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:student:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:student:remove']">删除</el-button>
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

    <!-- 添加或修改学生用户对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像" prop="avatar">
              <ImageUpload v-model="form.avatar" :limit="1" :file-size="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="联系方式" prop="contactWay">
              <el-input v-model="form.contactWay" placeholder="请输入联系方式" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="信用分" prop="creditScore">
              <el-input v-model="form.creditScore" placeholder="请输入信用分" />
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

    <!-- 导入学生用户对话框 -->
    <el-dialog v-model="importOpen" title="导入学生用户" width="400px" append-to-body>
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

    <!-- 信用分手动调整 -->
    <el-dialog v-model="adjustOpen" title="信用分手动调整" width="480px" append-to-body>
      <el-form ref="adjustRef" :model="adjustForm" :rules="adjustRules" label-width="auto">
        <el-form-item label="学生">
          <span>{{ adjustTarget.nickname || '-' }}（ID：{{ adjustTarget.userId }}，当前 {{ adjustTarget.creditScore ?? '-' }} 分）</span>
        </el-form-item>
        <el-form-item label="增减分值" prop="changeValue">
          <el-input-number v-model="adjustForm.changeValue" :step="1" :precision="0" controls-position="right" style="width: 180px" />
          <span class="adjust-tip">可负，不可为 0；下穿阈值会自动封禁</span>
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

    <!-- 学生用户详情抽屉 -->
    <el-drawer v-model="viewOpen" title="学生用户详情" size="460px" append-to-body>
      <div class="stu-detail-head">
        <el-image v-if="viewData.avatar" :src="viewData.avatar" :preview-src-list="[viewData.avatar]" preview-teleported fit="cover" class="stu-avatar" />
        <el-avatar v-else :size="56">{{ (viewData.nickname || '?').charAt(0) }}</el-avatar>
        <div class="stu-meta">
          <div class="stu-name">{{ viewData.nickname || '未设置昵称' }}</div>
          <el-tag :type="statusType(viewData.status)" size="small">{{ statusLabel(viewData.status) }}</el-tag>
        </div>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生用户ID">{{ viewData.userId }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ viewData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ viewData.contactWay || '-' }}</el-descriptions-item>
        <el-descriptions-item label="信用分">{{ viewData.creditScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ parseTime(viewData.lastLoginTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(viewData.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="Student">
import { listStudent, getStudent, delStudent, addStudent, updateStudent, importStudent, importStudentTemplate } from "@/api/trade/student"
import { adjustCredit } from "@/api/trade/credit"

const { proxy } = getCurrentInstance()

const studentList = ref([])
const open = ref(false)
const importOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const importLoading = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const adjustOpen = ref(false)
const adjustTarget = ref({})
const adjustForm = reactive({ userId: null, changeValue: null, reason: '' })
const adjustRules = {
  changeValue: [
    { required: true, message: '请输入增减分值', trigger: 'blur' },
    { validator: (rule, value, cb) => (value === 0 ? cb(new Error('调整分值不能为 0')) : cb()), trigger: 'blur' }
  ],
  reason: [{ required: true, message: '请填写调整原因', trigger: 'blur' }]
}

const STUDENT_STATUS = {
  '0': { label: '正常', type: 'success' },
  '1': { label: '临时封禁', type: 'warning' },
  '2': { label: '永久封禁', type: 'danger' }
}
function statusLabel(v) { return (STUDENT_STATUS[String(v)] || {}).label || (v ?? '-') }
function statusType(v) { return (STUDENT_STATUS[String(v)] || {}).type || 'info' }

/** 查看学生用户详情 */
function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

/** 打开信用分调整 */
function openAdjust(row) {
  adjustTarget.value = row
  adjustForm.userId = row.userId
  adjustForm.changeValue = null
  adjustForm.reason = ''
  adjustOpen.value = true
}

/** 提交信用分调整 */
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
    const response = await fetch(
      import.meta.env.VITE_APP_BASE_API + '/trade/student/import',
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

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    phone: undefined,
    password: undefined,
    nickname: undefined,
    contactWay: undefined,
    status: undefined,
  },
  rules: {
    studentNo: [
      { required: true, message: "学号不能为空", trigger: "blur" }
    ],
    password: [
      { required: true, message: "密码不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询学生用户列表 */
function getList() {
  loading.value = true
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows
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
    userId: null,
    studentNo: null,
    phone: null,
    password: null,
    nickname: null,
    avatar: null,
    contactWay: null,
    creditScore: null,
    status: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("studentRef")
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
  ids.value = selection.map(item => item.userId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加学生用户"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _userId = row.userId || ids.value
  getStudent(_userId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改学生用户"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.userId != null) {
        updateStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStudent(form.value).then(() => {
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
  const _userIds = row.userId || ids.value
  proxy.$modal.confirm('是否确认删除学生用户编号为"' + _userIds + '"的数据项？').then(function() {
    return delStudent(_userIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/student/export', {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
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

// 在 el-upload 上添加 :before-upload="beforeUpload"

/** 下载导入模板 */
const downloadTemplate = () => {
  importStudentTemplate().then(blob => {
    // response是Blob对象（request拦截器对blob响应直接返回Blob）
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学生用户导入模板_${new Date().getTime()}.xlsx`
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
    // 如果有失败明细，打印到控制台
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

getList()
</script>

<style lang="scss" scoped>
.avatar-thumb {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  vertical-align: middle;
}
.credit-score {
  font-weight: 600;
  color: var(--ct-primary);
  font-variant-numeric: tabular-nums;
}
.adjust-tip {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.stu-detail-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 2px 2px 18px;
  .stu-avatar { width: 56px; height: 56px; border-radius: 12px; }
  .stu-meta { display: flex; flex-direction: column; gap: 6px; }
  .stu-name { font-size: 16px; font-weight: 600; color: var(--ct-ink); }
}
</style>
