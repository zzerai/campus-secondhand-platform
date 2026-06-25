<template>
  <div class="login">
    <!-- 左侧品牌区 -->
    <section class="login-brand">
      <div class="brand-inner">
        <div class="brand-logo">
          <span class="brand-mark">易</span>
          <span class="brand-name">{{ title }}</span>
        </div>
        <h1 class="brand-headline">让每一件闲置<br />都能遇见下一个主人</h1>
        <p class="brand-sub">管理后台 · 商品审核 · 订单与支付 · 争议仲裁 · AI 辅助</p>
        <ul class="brand-points">
          <li><i class="dot"></i>商品发布审核与 AI 智能初审</li>
          <li><i class="dot"></i>订单 / 支付宝沙盒全流程跟踪</li>
          <li><i class="dot"></i>举报处理与 AI 争议仲裁</li>
        </ul>
      </div>
      <span class="brand-glow"></span>
      <span class="brand-glow brand-glow--2"></span>
    </section>

    <!-- 右侧登录区 -->
    <section class="login-panel">
      <!-- 环境氛围层：点阵 + 漂浮光晕 + 上升粒子（纯装饰） -->
      <div class="panel-fx" aria-hidden="true">
        <span class="glow g1"></span>
        <span class="glow g2"></span>
        <span class="p p1"></span>
        <span class="p p2"></span>
        <span class="p p3"></span>
        <span class="p p4"></span>
        <span class="p p5"></span>
        <span class="p p6"></span>
        <span class="p p7"></span>
      </div>
      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
        <div class="form-head">
          <h3 class="title">欢迎回来</h3>
          <p class="subtitle">登录到 {{ title }}</p>
        </div>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="账号"
          >
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="密码"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            size="large"
            auto-complete="off"
            placeholder="验证码"
            class="code-input"
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
          <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>
        </div>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login-btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-foot">{{ footerContent }}</div>
    </section>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
$brand: #0f6e5c;
$brand-deep: #0a4f43;

.login {
  display: flex;
  height: 100%;
  width: 100%;
  background: #fff;
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.login-brand {
  position: relative;
  flex: 0 0 46%;
  max-width: 620px;
  display: flex;
  align-items: center;
  padding: 64px;
  color: #fff;
  background:
    radial-gradient(120% 90% at 85% 10%, rgba(43, 164, 113, .55) 0%, rgba(43, 164, 113, 0) 55%),
    linear-gradient(155deg, #134e44 0%, #0f6e5c 48%, #0a4f43 100%);
  overflow: hidden;

  .brand-inner {
    position: relative;
    z-index: 2;
    max-width: 440px;
  }

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 56px;
    animation: ctRise .6s .05s both;

    .brand-mark {
      display: grid;
      place-items: center;
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: rgba(255, 255, 255, .16);
      backdrop-filter: blur(6px);
      font-size: 20px;
      font-weight: 800;
    }
    .brand-name {
      font-size: 17px;
      font-weight: 600;
      letter-spacing: .5px;
    }
  }

  .brand-headline {
    margin: 0 0 18px;
    font-size: 40px;
    line-height: 1.25;
    font-weight: 800;
    letter-spacing: .5px;
    animation: ctRise .7s .15s both;
  }
  .brand-sub {
    margin: 0 0 40px;
    font-size: 15px;
    line-height: 1.7;
    color: rgba(255, 255, 255, .8);
    animation: ctRise .7s .28s both;
  }

  .brand-points {
    list-style: none;
    margin: 0;
    padding: 0;
    li {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      animation: ctRiseSm .6s both;
      &:nth-child(1) { animation-delay: .42s; }
      &:nth-child(2) { animation-delay: .52s; }
      &:nth-child(3) { animation-delay: .62s; }
      font-size: 14.5px;
      color: rgba(255, 255, 255, .92);
      .dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: #7fe3c4;
        box-shadow: 0 0 0 4px rgba(127, 227, 196, .18);
        flex-shrink: 0;
      }
    }
  }

  .brand-glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    z-index: 1;
    will-change: transform;
    &:not(.brand-glow--2) {
      width: 320px; height: 320px;
      right: -80px; bottom: -90px;
      background: rgba(127, 227, 196, .35);
      animation: ctFloat 9s ease-in-out infinite;
    }
    &.brand-glow--2 {
      width: 240px; height: 240px;
      left: -60px; top: 120px;
      background: rgba(91, 131, 232, .22);
      animation: ctFloat2 12s ease-in-out infinite;
    }
  }
}

/* ===== 右侧登录区 ===== */
.login-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(60% 60% at 50% 0%, rgba(18, 124, 104, .04) 0%, rgba(18, 124, 104, 0) 70%),
    #ffffff;
}

/* ===== 右侧氛围层 ===== */
.panel-fx {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  /* 极淡点阵纹理，向边缘渐隐 */
  background-image: radial-gradient(rgba(18, 124, 104, .07) 1px, transparent 1.5px);
  background-size: 26px 26px;
  -webkit-mask-image: radial-gradient(125% 95% at 50% 42%, #000 0%, rgba(0, 0, 0, .35) 58%, transparent 84%);
          mask-image: radial-gradient(125% 95% at 50% 42%, #000 0%, rgba(0, 0, 0, .35) 58%, transparent 84%);

  .glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(70px);
    will-change: transform;
  }
  .g1 {
    width: 300px; height: 300px;
    top: -70px; right: -60px;
    background: rgba(18, 124, 104, .12);
    animation: ctFloat 13s ease-in-out infinite;
  }
  .g2 {
    width: 260px; height: 260px;
    bottom: -80px; left: -70px;
    background: rgba(91, 131, 232, .10);
    animation: ctFloat2 16s ease-in-out infinite;
  }

  .p {
    position: absolute;
    border-radius: 50%;
    opacity: 0;
    filter: blur(.5px);
    will-change: transform, opacity;
    animation: ctParticle linear infinite;
  }
  .p1 { width: 9px;  height: 9px;  left: 14%; bottom: -14px; background: rgba(18, 124, 104, .55); animation-duration: 15s; animation-delay: 0s; }
  .p2 { width: 6px;  height: 6px;  left: 30%; bottom: -10px; background: rgba(43, 164, 113, .5);  animation-duration: 19s; animation-delay: 3s; }
  .p3 { width: 12px; height: 12px; left: 48%; bottom: -16px; background: rgba(91, 131, 232, .35); animation-duration: 22s; animation-delay: 6s; }
  .p4 { width: 5px;  height: 5px;  left: 63%; bottom: -8px;  background: rgba(18, 124, 104, .6);  animation-duration: 17s; animation-delay: 1.5s; }
  .p5 { width: 8px;  height: 8px;  left: 78%; bottom: -12px; background: rgba(127, 227, 196, .6); animation-duration: 20s; animation-delay: 4.5s; }
  .p6 { width: 7px;  height: 7px;  left: 88%; bottom: -10px; background: rgba(138, 111, 224, .3); animation-duration: 24s; animation-delay: 8s; }
  .p7 { width: 4px;  height: 4px;  left: 22%; bottom: -8px;  background: rgba(18, 124, 104, .5);  animation-duration: 18s; animation-delay: 10s; }
}

.login-form {
  width: 100%;
  max-width: 376px;
  position: relative;
  z-index: 1;

  .form-head {
    margin-bottom: 28px;
    animation: ctRiseSm .6s .1s both;
    .title {
      margin: 0 0 6px;
      font-size: 26px;
      font-weight: 700;
      color: var(--ct-ink, #1a2430);
      letter-spacing: .3px;
    }
    .subtitle {
      margin: 0;
      font-size: 14px;
      color: var(--ct-text-2, #6b7686);
    }
  }

  .el-input {
    height: 46px;
    input { height: 46px; }
  }
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 1px 14px;
  }
  .input-icon {
    height: 44px;
    width: 15px;
    margin-left: 0;
    color: var(--ct-text-muted, #97a1ae);
  }

  .code-input {
    width: calc(100% - 124px);
  }

  :deep(.el-form-item) {
    animation: ctRiseSm .55s both;
  }
  :deep(.el-form-item):nth-of-type(1) { animation-delay: .20s; }
  :deep(.el-form-item):nth-of-type(2) { animation-delay: .30s; }
  :deep(.el-form-item):nth-of-type(3) { animation-delay: .40s; }
  :deep(.el-form-item):nth-of-type(4) { animation-delay: .48s; }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 22px;
  animation: ctRiseSm .55s .46s both;
  .link-type {
    font-size: 13.5px;
    color: var(--ct-primary, #127c68);
    font-weight: 500;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 10px;
  background: linear-gradient(120deg, #127c68 0%, #0f6e5c 55%, #16997f 100%);
  background-size: 180% 100%;
  border: none;
  transition: background-position .55s ease, transform .2s ease, box-shadow .2s ease;
  &:hover { background-position: 100% 0; }
}

.login-code {
  width: 112px;
  height: 46px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.login-code-img {
  height: 46px;
  width: 112px;
  border-radius: 10px;
  border: 1px solid var(--ct-border-soft, #edf0f4);
}

.login-foot {
  position: absolute;
  bottom: 26px;
  z-index: 1;
  font-size: 12px;
  letter-spacing: .5px;
  color: var(--ct-text-muted, #97a1ae);
  animation: ctRiseSm .6s .6s both;
}

/* ===== 响应式：窄屏隐藏品牌区 ===== */
@media (max-width: 992px) {
  .login-brand { display: none; }
}

/* ===== 暗色模式 ===== */
html.dark .login {
  background: var(--el-bg-color);
  .login-panel {
    background: var(--el-bg-color);
  }
  .login-form .form-head .title { color: #fff; }
}

/* ===== 动效关键帧 ===== */
@keyframes ctRise {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes ctRiseSm {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes ctFloat {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50%      { transform: translate3d(0, -26px, 0) scale(1.06); }
}
@keyframes ctFloat2 {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50%      { transform: translate3d(20px, 22px, 0) scale(1.08); }
}
@keyframes ctParticle {
  0%   { transform: translateY(0) translateX(0) scale(.9);  opacity: 0; }
  12%  { opacity: .7; }
  85%  { opacity: .5; }
  100% { transform: translateY(-72vh) translateX(26px) scale(1.2); opacity: 0; }
}

/* 尊重「减少动态效果」系统偏好 */
@media (prefers-reduced-motion: reduce) {
  .login *,
  .login *::before,
  .login *::after {
    animation: none !important;
  }
}
</style>
