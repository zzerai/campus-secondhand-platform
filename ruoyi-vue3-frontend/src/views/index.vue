<template>
  <div class="app-container dash">
    <!-- 欢迎横幅 -->
    <section class="hero">
      <div class="hero-text">
        <p class="hero-date">{{ today }}</p>
        <h2 class="hero-title">{{ greeting }}，{{ nickName }}</h2>
        <p class="hero-sub">
          欢迎回到 校园闲置交易平台 · 管理后台。今日有
          <b>{{ overview.goodsPendingAudit }}</b> 件商品待审核，
          <b>{{ overview.pendingDisputeCount }}</b> 起争议待处理。
        </p>
        <div class="hero-actions">
          <el-button type="primary" round @click="goStatistics">查看数据报表</el-button>
          <el-button round class="hero-ghost" @click="goPending">去处理待审核</el-button>
        </div>
      </div>
      <div class="hero-art">
        <span class="ring ring-1"></span>
        <span class="ring ring-2"></span>
        <span class="ring ring-3"></span>
      </div>
    </section>

    <!-- 关键指标 -->
    <div class="kpi-grid" v-loading="loading">
      <div class="kpi-card" v-for="k in kpis" :key="k.key">
        <div class="kpi-icon" :style="{ background: k.bg, color: k.color }">
          <el-icon><component :is="k.icon" /></el-icon>
        </div>
        <div class="kpi-body">
          <div class="kpi-label">{{ k.title }}</div>
          <div class="kpi-value">{{ k.value }}</div>
          <div class="kpi-extra">{{ k.extra }}</div>
        </div>
      </div>
    </div>

    <div class="dash-cols">
      <!-- 快捷入口 -->
      <section class="panel quick">
        <header class="panel-head">
          <h3>快捷入口</h3>
          <span class="panel-tip">基于你的菜单权限</span>
        </header>
        <div class="quick-grid">
          <button v-for="q in quickLinks" :key="q.path" class="quick-item" @click="go(q.path)">
            <span class="quick-ico"><svg-icon :icon-class="q.icon || 'tree-table'" /></span>
            <span class="quick-name">{{ q.title }}</span>
          </button>
          <div v-if="!quickLinks.length" class="quick-empty">暂无可用菜单</div>
        </div>
      </section>

      <!-- 关于平台 -->
      <section class="panel about">
        <header class="panel-head">
          <h3>关于平台</h3>
        </header>
        <p class="about-desc">
          面向高校学生的二手闲置交易平台管理端。支持商品发布审核、订单与支付状态跟踪、争议仲裁、用户与举报管理，并集成 AI 商品审核与争议分析能力。
        </p>
        <div class="tag-row">
          <span class="soft-tag t-green">学生认证独立</span>
          <span class="soft-tag t-blue">支付宝沙盒</span>
          <span class="soft-tag t-amber">AI 审核</span>
          <span class="soft-tag t-violet">争议仲裁</span>
        </div>
        <div class="stack">
          <div class="stack-col">
            <span class="stack-h">后端</span>
            <span>Spring Boot 3.5 · JDK 17</span>
            <span>Spring Security · JWT</span>
            <span>MyBatis · Druid · Redis</span>
            <span>LangChain4j（DashScope）</span>
          </div>
          <div class="stack-col">
            <span class="stack-h">前端</span>
            <span>Vue 3.5 · Vite 6</span>
            <span>Pinia 3 · Vue Router 4</span>
            <span>Element Plus 2</span>
            <span>Axios · ECharts</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup name="Index">
import { Goods, ShoppingCart, Money, User, Tickets, Warning } from '@element-plus/icons-vue'
import { getOverview } from '@/api/trade/statistics'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const loading = ref(false)
const nickName = computed(() => userStore.nickName || '管理员')

const overview = reactive({
  goodsTotal: 0, orderTotal: 0, totalTradeAmount: 0,
  studentUserTotal: 0, goodsPendingAudit: 0, pendingDisputeCount: 0
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const today = computed(() => {
  const d = new Date()
  const w = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · ${w}`
})

const kpis = computed(() => [
  { key: 'goods', title: '商品总数', value: overview.goodsTotal, extra: '在售 / 待审核 / 已售出', icon: markRaw(Goods), color: '#127C68', bg: '#E8F4F0' },
  { key: 'order', title: '订单总数', value: overview.orderTotal, extra: '预约与交易订单', icon: markRaw(ShoppingCart), color: '#5B83E8', bg: '#EAF0FD' },
  { key: 'amount', title: '交易金额', value: `￥${Number(overview.totalTradeAmount || 0).toFixed(2)}`, extra: '支付宝沙盒累计成交', icon: markRaw(Money), color: '#E0A33B', bg: '#FBF2E2' },
  { key: 'user', title: '学生用户', value: overview.studentUserTotal, extra: '移动端注册用户', icon: markRaw(User), color: '#8A6FE0', bg: '#F1ECFB' },
  { key: 'audit', title: '待审核商品', value: overview.goodsPendingAudit, extra: '需要管理员处理', icon: markRaw(Tickets), color: '#2BA471', bg: '#E6F5EE' },
  { key: 'dispute', title: '争议处理中', value: overview.pendingDisputeCount, extra: '等待 AI / 人工仲裁', icon: markRaw(Warning), color: '#E5685F', bg: '#FCEBEA' }
])

/** 将后台动态路由展平为可导航的叶子节点 */
function joinPath(base, path) {
  if (!path) return base
  if (/^https?:\/\//.test(path)) return path
  if (path.startsWith('/')) return path
  const b = base.endsWith('/') ? base.slice(0, -1) : base
  return `${b}/${path}`
}
function flatten(routes, base = '') {
  const out = []
  for (const r of routes || []) {
    if (r.hidden) continue
    const full = joinPath(base, r.path)
    const kids = (r.children || []).filter(c => !c.hidden)
    if (kids.length) {
      out.push(...flatten(r.children, full))
    } else if (r.meta && r.meta.title && !/^https?:\/\//.test(full)) {
      out.push({ title: r.meta.title, icon: r.meta.icon, path: full })
    }
  }
  return out
}

const quickLinks = computed(() => {
  const all = flatten(permissionStore.sidebarRouters)
  const trade = all.filter(l => l.path.startsWith('/trade'))
  return (trade.length ? trade : all).slice(0, 8)
})

function go(path) { router.push(path).catch(() => {}) }
/** 按路径末段在侧边栏中定位菜单叶子（兼容不同父级层级，且不会误匹配标题文字） */
function findLeaf(seg) {
  return flatten(permissionStore.sidebarRouters).find(l => l.path === seg || l.path.endsWith('/' + seg))
}
function goStatistics() {
  const s = findLeaf('statistics')
  router.push(s ? s.path : '/index').catch(() => {})
}
function goPending() {
  const g = findLeaf('goods')
  router.push(g ? g.path : '/index').catch(() => {})
}

function loadOverview() {
  loading.value = true
  getOverview()
    .then(res => Object.assign(overview, res.data || {}))
    .catch(() => {})
    .finally(() => { loading.value = false })
}

loadOverview()
</script>

<style scoped lang="scss">
.dash {
  padding: 18px 20px 24px;
}

/* ===== Hero ===== */
.hero {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30px 34px;
  border-radius: var(--ct-radius-xl, 18px);
  color: #fff;
  background:
    radial-gradient(120% 140% at 90% 0%, rgba(43, 164, 113, .55) 0%, rgba(43, 164, 113, 0) 50%),
    linear-gradient(135deg, #134e44 0%, #0f6e5c 55%, #0a4f43 100%);
  box-shadow: 0 12px 30px rgba(15, 110, 92, .22);

  .hero-text { position: relative; z-index: 2; }
  .hero-date { margin: 0 0 6px; font-size: 13px; letter-spacing: .5px; color: rgba(255, 255, 255, .78); }
  .hero-title { margin: 0 0 10px; font-size: 28px; font-weight: 800; letter-spacing: .5px; }
  .hero-sub {
    margin: 0 0 20px; font-size: 14px; line-height: 1.7;
    color: rgba(255, 255, 255, .88); max-width: 640px;
    b { color: #aef0d8; font-weight: 700; padding: 0 2px; }
  }
  .hero-actions { display: flex; gap: 12px; }
  .hero-ghost {
    background: rgba(255, 255, 255, .14);
    border-color: rgba(255, 255, 255, .28);
    color: #fff;
    &:hover { background: rgba(255, 255, 255, .24); border-color: rgba(255, 255, 255, .4); color: #fff; }
  }

  .hero-art {
    position: absolute;
    right: 40px; top: 50%;
    transform: translateY(-50%);
    width: 220px; height: 220px;
    .ring {
      position: absolute; border-radius: 50%;
      border: 1.5px solid rgba(255, 255, 255, .18);
      inset: 0; margin: auto;
    }
    .ring-1 { width: 220px; height: 220px; }
    .ring-2 { width: 150px; height: 150px; border-color: rgba(255, 255, 255, .28); }
    .ring-3 {
      width: 80px; height: 80px;
      background: rgba(174, 240, 216, .25);
      border: none;
      backdrop-filter: blur(4px);
    }
  }
}

/* ===== KPI ===== */
.kpi-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}
.kpi-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  background: var(--ct-surface, #fff);
  border: 1px solid var(--ct-border-soft, #edf0f4);
  border-radius: var(--ct-radius-lg, 14px);
  box-shadow: var(--ct-shadow-xs);
  transition: box-shadow .25s ease, transform .25s ease;
  &:hover { box-shadow: var(--ct-shadow-sm); transform: translateY(-2px); }

  .kpi-icon {
    flex-shrink: 0;
    width: 46px; height: 46px;
    display: grid; place-items: center;
    border-radius: 12px;
    font-size: 24px;
  }
  .kpi-label { font-size: 13px; color: var(--ct-text-2, #6b7686); }
  .kpi-value {
    margin-top: 3px; font-size: 22px; font-weight: 700;
    color: var(--ct-ink, #1a2430); font-variant-numeric: tabular-nums;
    line-height: 1.2;
  }
  .kpi-extra { margin-top: 3px; font-size: 12px; color: var(--ct-text-muted, #97a1ae); }
}

/* ===== 双栏 ===== */
.dash-cols {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}
.panel {
  background: var(--ct-surface, #fff);
  border: 1px solid var(--ct-border-soft, #edf0f4);
  border-radius: var(--ct-radius-lg, 14px);
  box-shadow: var(--ct-shadow-xs);
  padding: 20px 22px;
}
.panel-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 16px;
  h3 { margin: 0; font-size: 16px; font-weight: 700; color: var(--ct-ink, #1a2430); }
  .panel-tip { font-size: 12px; color: var(--ct-text-muted, #97a1ae); }
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 18px 10px;
  border: 1px solid var(--ct-border-soft, #edf0f4);
  border-radius: var(--ct-radius, 10px);
  background: var(--ct-surface-2, #fafbfc);
  cursor: pointer;
  transition: all .2s ease;
  &:hover {
    border-color: var(--ct-primary-100, #d2eae3);
    background: var(--ct-primary-050, #e8f4f0);
    transform: translateY(-2px);
    .quick-ico { color: var(--ct-primary, #127c68); }
  }
  .quick-ico { font-size: 22px; color: var(--ct-text-2, #6b7686); transition: color .2s; }
  .quick-name { font-size: 13px; color: var(--ct-text, #46515f); font-weight: 500; }
}
.quick-empty {
  grid-column: 1 / -1; text-align: center; padding: 24px;
  color: var(--ct-text-muted, #97a1ae); font-size: 13px;
}

/* 关于平台 */
.about-desc { margin: 0 0 14px; font-size: 13.5px; line-height: 1.8; color: var(--ct-text, #46515f); }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
.soft-tag {
  font-size: 12px; font-weight: 500; padding: 4px 10px; border-radius: 6px;
  &.t-green { background: #E6F5EE; color: #2BA471; }
  &.t-blue { background: #EAF0FD; color: #5B83E8; }
  &.t-amber { background: #FBF2E2; color: #C98A22; }
  &.t-violet { background: #F1ECFB; color: #8A6FE0; }
}
.stack {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
  padding-top: 16px; border-top: 1px dashed var(--ct-border, #e4e9ee);
}
.stack-col {
  display: flex; flex-direction: column; gap: 7px;
  font-size: 13px; color: var(--ct-text-2, #6b7686);
  .stack-h {
    font-size: 12px; font-weight: 700; color: var(--ct-ink, #1a2430);
    text-transform: uppercase; letter-spacing: .5px; margin-bottom: 2px;
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1280px) {
  .kpi-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 992px) {
  .dash-cols { grid-template-columns: 1fr; }
  .hero-art { display: none; }
}
@media (max-width: 768px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
