<template>
  <div class="app-container">
    <!-- 顶部检索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="版本名" prop="versionName">
        <el-input v-model="queryParams.versionName" placeholder="如 1.1.0" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(v, k) in STATUS" :key="k" :label="v.label" :value="k" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['trade:version:add']">发布新版本</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['trade:version:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="versionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="版本名" align="center" prop="versionName" width="120" />
      <el-table-column label="版本号" align="center" prop="versionCode" width="90" />
      <el-table-column label="APK大小" align="center" width="110">
        <template #default="scope">{{ formatSize(scope.row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="强制更新" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.forceUpdate === '1' ? 'danger' : 'info'">{{ scope.row.forceUpdate === '1' ? '强制' : '可选' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新日志" align="center" prop="updateLog" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:version:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:version:remove']">删除</el-button>
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

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="640px" append-to-body>
      <el-form ref="versionRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="APK文件" prop="downloadUrl">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{}"
            name="file"
            accept=".apk"
            :show-file-list="false"
            :before-upload="beforeApkUpload"
            :on-success="handleApkSuccess"
            :on-error="handleApkError"
          >
            <el-button type="primary" icon="Upload" :loading="uploading">{{ uploading ? '上传中...' : '上传 APK' }}</el-button>
          </el-upload>
          <div v-if="form.downloadUrl" class="apk-info">
            <div class="apk-line"><span class="apk-label">文件：</span>{{ uploadedName || form.downloadUrl }}</div>
            <div class="apk-line"><span class="apk-label">大小：</span>{{ formatSize(form.fileSize) }}</div>
            <div class="apk-line"><span class="apk-label">SHA256：</span><span class="apk-hash">{{ form.fileSha256 }}</span></div>
          </div>
          <div v-else class="apk-tip">请先上传 APK，系统将自动填充下载地址、大小与 SHA-256 校验值</div>
        </el-form-item>
        <el-form-item label="版本名" prop="versionName">
          <el-input v-model="form.versionName" placeholder="展示用，如 1.1.0" style="width: 220px" />
        </el-form-item>
        <el-form-item label="版本号" prop="versionCode">
          <el-input-number v-model="form.versionCode" :min="1" :step="1" controls-position="right" style="width: 220px" />
          <span class="form-tip">整数，须大于旧版本（对应 pubspec 构建号 +N）</span>
        </el-form-item>
        <el-form-item label="强制更新" prop="forceUpdate">
          <el-radio-group v-model="form.forceUpdate">
            <el-radio label="0">可选更新</el-radio>
            <el-radio label="1">强制更新</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用（参与移动端比对）</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="更新日志" prop="updateLog">
          <el-input v-model="form.updateLog" type="textarea" :rows="4" placeholder="本次更新内容，将展示给用户" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="内部备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AppVersion">
import { listAppVersion, getAppVersion, addAppVersion, updateAppVersion, delAppVersion } from "@/api/trade/appVersion"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()

const STATUS = { '0': { label: '启用' }, '1': { label: '停用' } }

const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/trade/appVersion/uploadApk"
const uploadHeaders = { Authorization: "Bearer " + getToken() }

const versionList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const dialogTitle = ref("")
const uploading = ref(false)
const uploadedName = ref("")

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  versionName: undefined,
  status: undefined
})

const initForm = () => ({
  versionId: undefined,
  versionName: undefined,
  versionCode: undefined,
  downloadUrl: undefined,
  fileSize: undefined,
  fileSha256: undefined,
  forceUpdate: "0",
  status: "0",
  updateLog: undefined,
  remark: undefined
})
const form = reactive(initForm())

const rules = {
  versionName: [{ required: true, message: "请填写版本名", trigger: "blur" }],
  versionCode: [{ required: true, message: "请填写版本号", trigger: "blur" }],
  downloadUrl: [{ required: true, message: "请先上传 APK 文件", trigger: "change" }]
}

/** 字节大小格式化 */
function formatSize(bytes) {
  if (!bytes && bytes !== 0) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

/** 查询列表 */
function getList() {
  loading.value = true
  listAppVersion(queryParams).then(response => {
    versionList.value = response.rows
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
  ids.value = selection.map(item => item.versionId)
  multiple.value = !selection.length
}

function resetForm() {
  Object.assign(form, initForm())
  uploadedName.value = ""
  proxy.resetForm("versionRef")
}

/** 新增 */
function handleAdd() {
  resetForm()
  dialogTitle.value = "发布新版本"
  open.value = true
}

/** 修改 */
function handleUpdate(row) {
  resetForm()
  getAppVersion(row.versionId).then(response => {
    Object.assign(form, response.data)
    dialogTitle.value = "修改版本"
    open.value = true
  })
}

/** APK 上传前校验 */
function beforeApkUpload(file) {
  const isApk = file.name.toLowerCase().endsWith('.apk')
  if (!isApk) {
    proxy.$modal.msgError("只能上传 .apk 文件")
    return false
  }
  uploading.value = true
  return true
}

/** APK 上传成功：回填下载地址、大小、SHA-256 */
function handleApkSuccess(res, file) {
  uploading.value = false
  if (res.code === 200) {
    form.downloadUrl = res.url
    form.fileSize = res.fileSize
    form.fileSha256 = res.fileSha256
    uploadedName.value = res.originalFilename || file.name
    proxy.$refs["versionRef"].validateField("downloadUrl")
    proxy.$modal.msgSuccess("APK 上传成功")
  } else {
    proxy.$modal.msgError(res.msg || "APK 上传失败")
  }
}
function handleApkError() {
  uploading.value = false
  proxy.$modal.msgError("APK 上传失败")
}

/** 提交 */
function submitForm() {
  proxy.$refs["versionRef"].validate(valid => {
    if (!valid) return
    const action = form.versionId ? updateAppVersion : addAppVersion
    action({ ...form }).then(() => {
      proxy.$modal.msgSuccess(form.versionId ? "修改成功" : "发布成功")
      open.value = false
      getList()
    })
  })
}

/** 启停切换 */
function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  updateAppVersion({ versionId: row.versionId, status: row.status }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    // 失败回滚开关状态
    row.status = row.status === '0' ? '1' : '0'
  })
}

/** 删除 */
function handleDelete(row) {
  const _ids = row.versionId || ids.value
  proxy.$modal.confirm('是否确认删除版本编号为"' + _ids + '"的数据项？').then(function() {
    return delAppVersion(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.form-tip { margin-left: 10px; color: var(--el-text-color-secondary); font-size: 12px; }
.apk-tip { color: var(--el-text-color-secondary); font-size: 12px; margin-top: 6px; }
.apk-info { margin-top: 8px; padding: 8px 12px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 12px; line-height: 1.8; width: 100%; }
.apk-label { color: var(--el-text-color-secondary); }
.apk-hash { word-break: break-all; color: var(--el-text-color-regular); }
</style>
