#基础镜像，我们要在Java17的环境中跑，slim是精简版，体积小省空间
FROM openjdk:21-jdk-slim
#设置维护者信息
LABEL maintainer = "admin"

# 【新增】换源并安装 FFmpeg
# 因为 slim 镜像太精简了，缺 FFmpeg，我们要手动装
# 为了下载快，我们换成阿里的 Debian 源 (可选，如果网速快不换也行，但建议换)
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources \
    && apt-get update \
    && apt-get install -y ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
#创建一个工作目录
WORKDIR /app
#把打包好的jar包复制进容器，并且改名为app.jar
#注意：target/*.jar就是maven打包后的路径
COPY target/*.jar app.jar

#暴露端口
EXPOSE 8080
#启动命令
ENTRYPOINT ["java","-jar","app.jar"]

