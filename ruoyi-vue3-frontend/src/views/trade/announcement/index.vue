<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="公告标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入公告标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公告类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部类型" clearable>
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" placeholder="全部状态" clearable>
          <el-option v-for="item in publishStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否置顶" prop="isTop">
        <el-select v-model="queryParams.isTop" placeholder="全部" clearable>
          <el-option label="否" value="0" />
          <el-option label="是" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间" prop="publishTime">
        <el-date-picker clearable
          v-model="queryParams.publishTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择发布时间">
        </el-date-picker>
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
          v-hasPermi="['trade:announcement:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:announcement:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:announcement:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div v-loading="loading" class="announcement-grid">
      <div
        v-for="item in announcementList"
        :key="item.announcementId"
        class="announcement-card"
        :class="{ 'is-draft': item.publishStatus === '0' }"
      >
        <div class="card-check">
          <el-checkbox
            :model-value="ids.includes(item.announcementId)"
            @change="(val) => toggleSelect(item.announcementId, val)"
          />
        </div>
        <div class="card-cover" @click="handleView(item)">
          <el-image
            v-if="item.coverImage"
            :src="resolveImage(item.coverImage)"
            fit="cover"
            class="cover-img"
            @error="onCoverError"
          >
            <template #error>
              <div class="cover-placeholder">
                <el-icon><PictureFilled /></el-icon>
                <span>加载失败</span>
              </div>
            </template>
          </el-image>
          <div v-else class="cover-placeholder">
            <el-icon><Bell /></el-icon>
            <span>暂无封面</span>
          </div>
          <span v-if="item.isTop === '1'" class="top-badge">
            <el-icon><TopRight /></el-icon> 置顶
          </span>
        </div>
        <div class="card-body">
          <h4 class="card-title" @click="handleView(item)">
            <span v-if="item.isTop === '1'" class="top-tag"><el-icon><TopRight /></el-icon></span>
            {{ item.title }}
          </h4>
          <div class="card-tags">
            <el-tag :type="getTagType(item.type)" size="small">{{ getTypeLabel(item.type) }}</el-tag>
            <el-tag :type="item.publishStatus === '1' ? 'success' : 'info'" size="small">
              {{ item.publishStatus === '1' ? '已发布' : '草稿' }}
            </el-tag>
          </div>
          <div class="card-time">{{ getTimeText(item) }}</div>
        </div>
        <div class="card-actions">
          <el-button
            v-if="item.publishStatus === '0'"
            type="primary"
            size="small"
            :icon="'TopRight'"
            @click="handlePublish(item)"
            v-hasPermi="['trade:announcement:publish']"
          >发布</el-button>
          <el-button
            v-if="item.publishStatus === '1'"
            type="warning"
            size="small"
            :icon="'BottomLeft'"
            @click="handleRetract(item)"
            v-hasPermi="['trade:announcement:edit']"
          >撤回</el-button>
          <el-button
            type="primary"
            size="small"
            :icon="'Edit'"
            @click="handleUpdate(item)"
            v-hasPermi="['trade:announcement:edit']"
          >修改</el-button>
          <el-button
            type="info"
            size="small"
            :icon="'View'"
            @click="handleView(item)"
          >查看</el-button>
          <el-button
            type="danger"
            size="small"
            :icon="'Delete'"
            @click="handleDelete(item)"
            v-hasPermi="['trade:announcement:remove']"
          >删除</el-button>
        </div>
      </div>
      <div v-if="!announcementList.length && !loading" class="grid-empty">
        <el-empty description="暂无公告数据" />
      </div>
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 公告详情抽屉 -->
    <el-drawer v-model="viewOpen" title="公告详情" size="640px" append-to-body>
      <div class="content-body">
        <h2 class="view-title">
          <span v-if="viewData.isTop === '1'" class="top-tag"><el-icon><TopRight /></el-icon></span>
          {{ viewData.title }}
        </h2>
        <div class="view-meta">
          <el-tag :type="getTagType(viewData.type)">{{ getTypeLabel(viewData.type) }}</el-tag>
          <el-tag :type="viewData.publishStatus === '1' ? 'success' : 'info'">
            {{ viewData.publishStatus === '1' ? '已发布' : '草稿' }}
          </el-tag>
          <span class="view-time">{{ getTimeText(viewData) }}</span>
        </div>
        <el-image
          v-if="viewData.coverImage"
          :src="resolveImage(viewData.coverImage)"
          :preview-src-list="[resolveImage(viewData.coverImage)]"
          preview-teleported
          fit="cover"
          class="view-cover"
        />
        <el-divider />
        <div class="view-content" v-html="viewData.content || '<p>暂无内容</p>'"></div>
      </div>
    </el-drawer>

    <!-- 添加或修改公告对话框 -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="announcementRef" :model="form" :rules="rules" label-width="auto">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <image-upload v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <editor v-model="form.content" :min-height="192" />
        </el-form-item>
        <el-form-item label="发布状态" prop="publishStatus">
          <el-radio-group v-model="form.publishStatus">
            <el-radio label="0">草稿</el-radio>
            <el-radio label="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶" prop="isTop">
          <el-radio-group v-model="form.isTop">
            <el-radio label="0">否</el-radio>
            <el-radio label="1">是</el-radio>
          </el-radio-group>
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

<script setup name="Announcement">
import { listAnnouncement, getAnnouncement, delAnnouncement, addAnnouncement, updateAnnouncement } from "@/api/trade/announcement"
import { isExternal } from "@/utils/validate"

const { proxy } = getCurrentInstance()
const { sys_notice_type } = useDict("sys_notice_type")

const announcementList = ref([])
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

function resolveImage(url) {
  if (!url) return ''
  if (isExternal(url)) return url
  return (import.meta.env.VITE_APP_BASE_API || '') + url
}

function onCoverError() {
  // el-image #error slot handles the display fallback
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    type: undefined,
    publishStatus: undefined,
    isTop: undefined,
    publishTime: undefined
  },
  rules: {
    title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
    type: [{ required: true, message: "公告类型不能为空", trigger: "change" }],
    content: [{ required: true, message: "公告内容不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

const typeOptions = ref([
  { value: '1', label: '系统公告' },
  { value: '2', label: '活动公告' },
  { value: '3', label: '通知公告' }
])

const publishStatusOptions = ref([
  { value: '0', label: '草稿' },
  { value: '1', label: '已发布' }
])

/** 查询公告列表 */
function getList() {
  loading.value = true
  listAnnouncement(queryParams.value).then(response => {
    announcementList.value = response.rows
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
    announcementId: undefined,
    title: undefined,
    type: undefined,
    content: undefined,
    publishStatus: "0",
    isTop: "0",
    coverImage: undefined
  }
  proxy.resetForm("announcementRef")
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

/** 卡片复选框切换 */
function toggleSelect(id, checked) {
  if (checked) {
    ids.value.push(id)
  } else {
    ids.value = ids.value.filter(i => i !== id)
  }
  single.value = ids.value.length != 1
  multiple.value = !ids.value.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加公告"
}

/**修改按钮操作 */
function handleUpdate(row) {
  reset()
  const announcementId = row.announcementId || ids.value
  getAnnouncement(announcementId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改公告"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["announcementRef"].validate(valid => {
    if (valid) {
      if (form.value.announcementId != undefined) {
        updateAnnouncement(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addAnnouncement(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 查看公告详情（页内抽屉，不做路由跳转） */
function handleView(row) {
  getAnnouncement(row.announcementId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

/** 发布公告 */
function handlePublish(row) {
  proxy.$modal.confirm('确认要发布该公告吗？').then(function() {
    return updateAnnouncement({
      ...row,
      publishStatus: '1'
    })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("发布成功")
  }).catch(() => {})
}

/** 撤回公告 */
function handleRetract(row) {
  proxy.$modal.confirm('确认要撤回该公告吗？').then(function() {
    return updateAnnouncement({
      ...row,
      publishStatus: '0'
    })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("撤回成功")
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row) {
  const announcementIds = row.announcementId || ids.value
  proxy.$modal.confirm('是否确认删除公告编号为"' + announcementIds + '"的数据项？').then(function() {
    return delAnnouncement(announcementIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 获取类型标签样式 */
function getTagType(type) {
  const types = {
    '1': '',
    '2': 'success',
    '3': 'warning'
  }
  return types[type] || ''
}

/** 获取类型标签文本 */
function getTypeLabel(type) {
  const types = {
    '1': '系统公告',
    '2': '活动公告',
    '3': '通知公告'
  }
  return types[type] || '未知类型'
}

/** 获取时间显示文本 */
function getTimeText(item) {
  if (item.publishTime) {
    return item.publishTime
  }
  if (item.createTime) {
    return item.createTime
  }
  return '暂无时间'
}

getList()
</script>

<style scoped>
/* ===== 卡片网格 ===== */
.announcement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(292px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.grid-empty {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

/* ===== 单个卡片 ===== */
.announcement-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

.announcement-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.announcement-card.is-draft {
  opacity: 0.85;
}

/* 复选框 */
.card-check {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
}

/* 封面图区域 */
.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
  background: #f1f5f9;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-img :deep(.el-image__inner) {
  object-fit: cover;
  transition: transform 0.35s ease;
}

.card-cover:hover .cover-img :deep(.el-image__inner) {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 13px;
}

.cover-placeholder .el-icon {
  font-size: 36px;
  color: #cbd5e1;
}

/* 置顶徽章 */
.top-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(230, 162, 60, 0.92);
  color: #fff;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
  backdrop-filter: blur(4px);
}

/* 卡片内容区 */
.card-body {
  padding: 14px 16px 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  margin: 0;
  font-size: 14.5px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.45;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.card-title:hover {
  color: #4F6EF7;
}

.top-tag {
  color: #E6A23C;
  margin-right: 2px;
  vertical-align: -1px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.card-time {
  font-size: 12px;
  color: #94a3b8;
}

/* 操作按钮区 */
.card-actions {
  padding: 0 16px 14px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.card-actions .el-button {
  margin-left: 0;
}

/* ===== 详情抽屉 ===== */
.content-body {
  padding: 0 4px;
}

.view-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
}

.view-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.view-time {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.view-cover {
  width: 100%;
  max-height: 240px;
  border-radius: 8px;
}

.view-content {
  line-height: 1.8;
  word-break: break-word;
}

.view-content :deep(img) {
  max-width: 100%;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .announcement-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>