
# 使用 Eclipse Temurin 的 JRE 21 版本（行业标准，比 openjdk 镜像更稳）
# 'jammy' 代表基于 Ubuntu 22.04 系统，兼容性好
FROM eclipse-temurin:21-jre-jammy
#设置维护者信息
LABEL maintainer="admin"

# 【核心修复】针对 Ubuntu 底层系统的阿里源替换命令
# 1. 备份源文件
# 2. 将 archive.ubuntu.com 替换为 mirrors.aliyun.com (阿里源)
# 3. 将 security.ubuntu.com 替换为 mirrors.aliyun.com (阿里源)
# 4. 安装 FFmpeg 并清理
# 【核心修复】针对 Ubuntu 系统的阿里源替换
# 1. 备份源文件
# 2. 替换 archive.ubuntu.com -> mirrors.aliyun.com
# 3. 替换 security.ubuntu.com -> mirrors.aliyun.com
# 4. 安装 FFmpeg 并清理
RUN cp /etc/apt/sources.list /etc/apt/sources.list.bak \
    && sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list \
    && sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list \
    && apt-get update \
    && apt-get install -y ffmpeg imagemagick libimage-exiftool-perl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN sed -i 's/rights="none" pattern="PDF"/rights="read|write" pattern="PDF"/g' /etc/ImageMagick-6/policy.xml
#创建一个工作目录
WORKDIR /app
#把打包好的jar包复制进容器，并且改名为app.jar
# 复制 jar 包
COPY *.jar app.jar
# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java","-jar","app.jar"]