<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品ID" prop="goodsId">
        <el-input
          v-model="queryParams.goodsId"
          placeholder="请输入商品ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="买家学生用户ID" prop="buyerId">
        <el-input
          v-model="queryParams.buyerId"
          placeholder="请输入买家学生用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="卖家学生用户ID" prop="sellerId">
        <el-input
          v-model="queryParams.sellerId"
          placeholder="请输入卖家学生用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易价格" prop="tradePrice">
        <el-input
          v-model="queryParams.tradePrice"
          placeholder="请输入交易价格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="约定交易地点" prop="tradePlace">
        <el-input
          v-model="queryParams.tradePlace"
          placeholder="请输入约定交易地点"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预约交易时间" prop="appointmentTime">
        <el-date-picker clearable
          v-model="queryParams.appointmentTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择预约交易时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="卖家确认时间" prop="confirmTime">
        <el-date-picker clearable
          v-model="queryParams.confirmTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择卖家确认时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="支付时间" prop="payTime">
        <el-date-picker clearable
          v-model="queryParams.payTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择支付时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="买家确认完成时间" prop="completeTime">
        <el-date-picker clearable
          v-model="queryParams.completeTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择买家确认完成时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="取消时间" prop="cancelTime">
        <el-date-picker clearable
          v-model="queryParams.cancelTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择取消时间">
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
          v-hasPermi="['trade:order:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['trade:order:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['trade:order:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['trade:order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="170" show-overflow-tooltip />
      <el-table-column label="商品ID" align="center" prop="goodsId" width="90" />
      <el-table-column label="买家ID" align="center" prop="buyerId" width="90" />
      <el-table-column label="卖家ID" align="center" prop="sellerId" width="90" />
      <el-table-column label="交易价格" align="center" prop="tradePrice" width="110">
        <template #default="scope">
          <span class="cell-price">￥{{ Number(scope.row.tradePrice || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="orderStatus" width="130">
        <template #default="scope">
          <dict-tag :options="trade_order_status" :value="scope.row.orderStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="paymentStatus" width="120">
        <template #default="scope">
          <dict-tag :options="trade_payment_status" :value="scope.row.paymentStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="退款状态" align="center" prop="refundStatus" width="130">
        <template #default="scope">
          <dict-tag v-if="scope.row.refundStatus && scope.row.refundStatus !== '0'" :options="trade_refund_status" :value="scope.row.refundStatus"/>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="预约交易时间" align="center" prop="appointmentTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.appointmentTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['trade:order:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['trade:order:remove']">删除</el-button>
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

    <!-- 添加或修改交易订单对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单编号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="请输入订单编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品ID" prop="goodsId">
              <el-input v-model="form.goodsId" placeholder="请输入商品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="买家学生用户ID" prop="buyerId">
              <el-input v-model="form.buyerId" placeholder="请输入买家学生用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="卖家学生用户ID" prop="sellerId">
              <el-input v-model="form.sellerId" placeholder="请输入卖家学生用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易价格" prop="tradePrice">
              <el-input v-model="form.tradePrice" placeholder="请输入交易价格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="约定交易地点" prop="tradePlace">
              <el-input v-model="form.tradePlace" placeholder="请输入约定交易地点" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预约交易时间" prop="appointmentTime">
              <el-date-picker clearable
                v-model="form.appointmentTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择预约交易时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="买家备注" prop="buyerRemark">
              <el-input v-model="form.buyerRemark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="卖家备注" prop="sellerRemark">
              <el-input v-model="form.sellerRemark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="取消原因" prop="cancelReason">
              <el-input v-model="form.cancelReason" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="卖家确认时间" prop="confirmTime">
              <el-date-picker clearable
                v-model="form.confirmTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择卖家确认时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="支付时间" prop="payTime">
              <el-date-picker clearable
                v-model="form.payTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择支付时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="买家确认完成时间" prop="completeTime">
              <el-date-picker clearable
                v-model="form.completeTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择买家确认完成时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="取消时间" prop="cancelTime">
              <el-date-picker clearable
                v-model="form.cancelTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择取消时间">
              </el-date-picker>
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

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="viewOpen" title="订单详情" size="480px" append-to-body>
      <el-descriptions :column="1" border class="order-detail">
        <el-descriptions-item label="订单编号">{{ viewData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品ID">{{ viewData.goodsId }}</el-descriptions-item>
        <el-descriptions-item label="买家学生用户ID">{{ viewData.buyerId }}</el-descriptions-item>
        <el-descriptions-item label="卖家学生用户ID">{{ viewData.sellerId }}</el-descriptions-item>
        <el-descriptions-item label="交易价格">
          <span class="cell-price">￥{{ Number(viewData.tradePrice || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <dict-tag :options="trade_order_status" :value="viewData.orderStatus"/>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <dict-tag :options="trade_payment_status" :value="viewData.paymentStatus"/>
        </el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <dict-tag v-if="viewData.refundStatus && viewData.refundStatus !== '0'" :options="trade_refund_status" :value="viewData.refundStatus"/>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ viewData.refundAmount != null ? '￥' + viewData.refundAmount : '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ viewData.refundReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款申请时间">{{ parseTime(viewData.refundApplyTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款成功时间">{{ parseTime(viewData.refundTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付宝退款流水号">{{ viewData.alipayRefundNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="约定交易地点">{{ viewData.tradePlace || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约交易时间">{{ parseTime(viewData.appointmentTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖家确认时间">{{ parseTime(viewData.confirmTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(viewData.payTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家确认完成时间">{{ parseTime(viewData.completeTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取消时间">{{ parseTime(viewData.cancelTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家备注">{{ viewData.buyerRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="卖家备注">{{ viewData.sellerRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取消原因">{{ viewData.cancelReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(viewData.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="Order">
import { listOrder, getOrder, delOrder, addOrder, updateOrder } from "@/api/trade/order"

const { proxy } = getCurrentInstance()
const { trade_order_status, trade_payment_status, trade_refund_status } = useDict('trade_order_status', 'trade_payment_status', 'trade_refund_status')

const orderList = ref([])
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


/** 查看订单详情 */
function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    goodsId: undefined,
    buyerId: undefined,
    sellerId: undefined,
    tradePrice: undefined,
    tradePlace: undefined,
    appointmentTime: undefined,
    orderStatus: undefined,
    paymentStatus: undefined,
    buyerRemark: undefined,
    sellerRemark: undefined,
    cancelReason: undefined,
    confirmTime: undefined,
    payTime: undefined,
    completeTime: undefined,
    cancelTime: undefined,
  },
  rules: {
    orderNo: [
      { required: true, message: "订单编号不能为空", trigger: "blur" }
    ],
    goodsId: [
      { required: true, message: "商品ID不能为空", trigger: "blur" }
    ],
    buyerId: [
      { required: true, message: "买家学生用户ID不能为空", trigger: "blur" }
    ],
    sellerId: [
      { required: true, message: "卖家学生用户ID不能为空", trigger: "blur" }
    ],
    tradePrice: [
      { required: true, message: "交易价格不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询交易订单列表 */
function getList() {
  loading.value = true
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows
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
    orderId: null,
    orderNo: null,
    goodsId: null,
    buyerId: null,
    sellerId: null,
    tradePrice: null,
    tradePlace: null,
    appointmentTime: null,
    orderStatus: null,
    paymentStatus: null,
    buyerRemark: null,
    sellerRemark: null,
    cancelReason: null,
    confirmTime: null,
    payTime: null,
    completeTime: null,
    cancelTime: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    delFlag: null,
    remark: null
  }
  proxy.resetForm("orderRef")
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
  ids.value = selection.map(item => item.orderId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加交易订单"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _orderId = row.orderId || ids.value
  getOrder(_orderId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改交易订单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["orderRef"].validate(valid => {
    if (valid) {
      if (form.value.orderId != null) {
        updateOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addOrder(form.value).then(() => {
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
  const _orderIds = row.orderId || ids.value
  proxy.$modal.confirm('是否确认删除交易订单编号为"' + _orderIds + '"的数据项？').then(function() {
    return delOrder(_orderIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('trade/order/export', {
    ...queryParams.value
  }, `order_${new Date().getTime()}.xlsx`)
}

getList()
</script>
