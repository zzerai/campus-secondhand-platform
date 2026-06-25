<template>
  <div class="app-container category-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input
          v-model="queryParams.categoryName"
          placeholder="请输入分类名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="父级分类ID" prop="parentId">
        <el-input
          v-model="queryParams.parentId"
          placeholder="请输入父级分类ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input
          v-model="queryParams.sort"
          placeholder="请输入排序"
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
          v-hasPermi="['trade:category:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:category:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:category:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:category:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">{{ expandAll ? '折叠全部' : '展开全部' }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="catTableRef" v-loading="loading" :data="categoryList" row-key="categoryId"
              :key="expandKey"
              :tree-props="{ children: 'children' }"
              :default-expand-all="expandAll"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="分类名称" align="left" prop="categoryName" min-width="240">
        <template #default="scope">
          <span
            class="cat-name"
            :class="{ 'is-top': scope.row._level === 0, 'is-child': isChild(scope.row), 'is-expandable': isParent(scope.row) }"
            @click="isParent(scope.row) && toggleExpand(scope.row)"
          >
            <el-icon class="cat-ico">
              <component :is="scope.row._level === 0 ? FolderOpened : PriceTag" />
            </el-icon>
            <span class="cat-text">{{ scope.row.categoryName }}</span>
            <span v-if="isParent(scope.row)" class="cat-count">{{ scope.row.children.length }} 项</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="分类ID" align="center" prop="categoryId" width="90" />
      <el-table-column label="父级分类" align="center" prop="_parentName" width="120" />
      <el-table-column label="排序" align="center" prop="sort" width="70" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:category:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:category:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加或修改商品分类对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="父级分类ID" prop="parentId">
              <el-input v-model="form.parentId" placeholder="请输入父级分类ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序" />
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

<script setup name="Category">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from "@/api/trade/category.js"
import { FolderOpened, PriceTag } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const categoryList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const expandAll = ref(true)
const expandKey = ref(0)
const catTableRef = ref(null)

/** 是否为含子级的父分类 */
function isParent(row) {
  return Array.isArray(row.children) && row.children.length > 0
}
/** 是否为被嵌套的子分类（_level 由 buildTree 写入） */
function isChild(row) {
  return row._level > 0
}
/** 点击父分类名称即可展开/折叠 */
function toggleExpand(row) {
  catTableRef.value && catTableRef.value.toggleRowExpansion(row)
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    categoryName: undefined,
    parentId: undefined,
    sort: undefined,
    status: undefined,
  },
  rules: {
    categoryName: [
      { required: true, message: "分类名称不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 构建分类树：支持任意层级，每级按 sort/categoryName 排序，子级归类到父级下 */
function buildTree(flatList) {
  const list = flatList || []
  // 在册节点集合，用于识别父级缺失的孤儿
  const ids = new Set(list.map(item => Number(item.categoryId)))
  const childrenMap = {}
  const roots = []
  list.forEach(item => {
    const parentId = item.parentId != null ? Number(item.parentId) : 0
    // 顶级，或父级不在列表中（孤儿）→ 视为根，避免丢失
    if (parentId === 0 || !ids.has(parentId)) {
      roots.push(item)
    } else {
      if (!childrenMap[parentId]) childrenMap[parentId] = []
      childrenMap[parentId].push(item)
    }
  })
  const sortNodes = nodes => nodes.sort((a, b) =>
    (a.sort || 0) - (b.sort || 0) || (a.categoryName || '').localeCompare(b.categoryName || '', 'zh'))
  const assemble = (node, level, parentName) => {
    node._level = level
    node._parentName = parentName
    const children = sortNodes(childrenMap[Number(node.categoryId)] || [])
    node.children = children.length > 0
      ? children.map(child => assemble(child, level + 1, node.categoryName))
      : undefined
    return node
  }
  return sortNodes(roots).map(root => assemble(root, 0, '顶级分类'))
}

/** 查询商品分类列表（全量加载，构建树结构） */
function getList() {
  loading.value = true
  listCategory({ ...queryParams.value, pageNum: 1, pageSize: 999 }).then(response => {
    categoryList.value = buildTree(response.rows || [])
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
    categoryId: null,
    categoryName: null,
    parentId: null,
    sort: null,
    status: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("categoryRef")
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
  ids.value = selection.map(item => item.categoryId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加商品分类"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _categoryId = row.categoryId || ids.value
  getCategory(_categoryId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商品分类"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["categoryRef"].validate(valid => {
    if (valid) {
      if (form.value.categoryId != null) {
        updateCategory(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCategory(form.value).then(() => {
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
  const _categoryIds = row.categoryId || ids.value
  proxy.$modal.confirm('是否确认删除商品分类编号为"' + _categoryIds + '"的数据项？').then(function() {
    return delCategory(_categoryIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/category/export', {
    ...queryParams.value
  }, `category_${new Date().getTime()}.xlsx`)
}

function getStatusLabel(status) {
  return String(status) === '0' ? '正常' : '停用'
}

function getStatusTagType(status) {
  return String(status) === '0' ? 'success' : 'danger'
}

/** 展开/折叠全部（通过 key 变化强制表格重建） */
function toggleExpandAll() {
  expandAll.value = !expandAll.value
  expandKey.value++
}

getList()
</script>

<style lang="scss" scoped>
.category-page {
  /* 展开控件：默认小三角 → 圆角 chevron 按钮，悬浮主色高亮，旋转更平滑 */
  :deep(.el-table__expand-icon) {
    width: 22px;
    height: 22px;
    line-height: 22px;
    border-radius: 6px;
    color: var(--ct-text-2);
    transition: background-color .2s ease, color .2s ease, transform .28s ease;

    .el-icon { vertical-align: middle; }

    &:hover {
      background-color: var(--ct-primary-050);
      color: var(--ct-primary);
    }
  }

  /* 名称单元格：父=文件夹+加粗+数量徽标；子=标签图标+缩进引导 */
  .cat-name {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    line-height: 1.4;

    .cat-ico {
      font-size: 16px;
      color: var(--ct-text-muted);
      flex-shrink: 0;
    }
    .cat-text { color: var(--ct-text); }

    /* 顶级分类：文件夹图标(主色) + 加粗，所有顶级一致 */
    &.is-top {
      .cat-ico { color: var(--ct-primary); }
      .cat-text { font-weight: 600; color: var(--ct-ink); }
    }
    /* 含子级、可展开：整名称可点 + 悬浮主色 */
    &.is-expandable {
      cursor: pointer;
      &:hover .cat-text { color: var(--ct-primary); }
    }

    /* 子分类前的连接线，强化层级 */
    &.is-child::before {
      content: "";
      width: 14px;
      height: 14px;
      margin: -6px 2px 0 -2px;
      border-left: 1px solid var(--ct-border);
      border-bottom: 1px solid var(--ct-border);
      border-radius: 0 0 0 4px;
      flex-shrink: 0;
    }

    .cat-count {
      margin-left: 4px;
      padding: 1px 9px;
      font-size: 12px;
      font-weight: 500;
      color: var(--ct-primary);
      background: var(--ct-primary-050);
      border-radius: 999px;
    }
  }
}
</style>
