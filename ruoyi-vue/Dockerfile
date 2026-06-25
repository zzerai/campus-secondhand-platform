# ============================================================
# 校园二手交易平台 · 后端（ruoyi-admin）镜像
# 多阶段构建：Maven 编译 -> JRE 运行
# 构建上下文必须是 ruoyi-vue/ 目录（含父 pom 与全部子模块）
# ============================================================

# ---------- Stage 1: 构建 ----------
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 先拷贝全部 pom 与源码再构建。
# 使用 BuildKit 缓存挂载缓存 ~/.m2，避免每次重新下载依赖。
COPY . .
# -s maven-settings.xml 使用阿里云镜像源（国内加速）。
# 注意：settings.xml 必须从 /build 读取，不能放 /root/.m2——
# 那里被 cache 挂载覆盖，放进去会被屏蔽。
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -s maven-settings.xml -DskipTests clean package

# ---------- Stage 2: 运行 ----------
FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app

# 时区（订单/支付时间需正确）
ENV TZ=Asia/Shanghai
# 日志输出到 /app/logs（覆盖 logback 默认的 /home/ruoyi/logs）
ENV LOG_PATH=/app/logs
# JVM 参数，可在 compose 用环境变量覆盖
ENV JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 入口模块产物：ruoyi-admin.jar（pom finalName=${project.artifactId}）
COPY --from=build /build/ruoyi-admin/target/ruoyi-admin.jar /app/app.jar

# 上传目录与日志目录（compose 会挂数据卷持久化）
RUN mkdir -p /app/uploadPath /app/logs

EXPOSE 8080

# -Druoyi.profile 覆盖 application.yml 里的 Windows 路径 D:/ruoyi/uploadPath
# .env 由应用自身从工作目录 ./ 读取（compose 挂载到 /app/.env）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Druoyi.profile=/app/uploadPath -jar /app/app.jar"]
