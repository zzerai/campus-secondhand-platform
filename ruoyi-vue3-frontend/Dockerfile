# ============================================================
# 校园二手交易平台 · 管理后台前端镜像
# 多阶段构建：Node 编译 dist -> Nginx 托管静态 + 反代后端 API
# 构建上下文为 ruoyi-vue3-frontend/ 目录
# ============================================================

# ---------- Stage 1: 构建 ----------
# 用 glibc 的 slim 而非 alpine：sass-embedded 在 musl 上易构建失败
FROM docker.m.daocloud.io/library/node:20-slim AS build
WORKDIR /build

# 先装依赖（利用层缓存），用国内镜像源加速
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install

# 再拷源码构建生产包（产物在 dist/，API 基址 /prod-api 见 .env.production）
COPY . .
RUN npm run build:prod

# ---------- Stage 2: 运行 ----------
FROM docker.m.daocloud.io/library/nginx:1.27-alpine
# 静态产物
COPY --from=build /build/dist /usr/share/nginx/html
# 路由配置（托管 SPA + 反代 /prod-api、/app、/profile 到后端）
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
