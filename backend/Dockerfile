#基础镜像，我们要在Java17的环境中跑，slim是精简版，体积小省空间
FROM openjdk:21-jdk-slim
#设置维护者信息
LABEL maintainer = "admin"
#创建一个工作目录
WORKDIR /app
#把打包好的jar包复制进容器，并且改名为app.jar
#注意：target/*.jar就是maven打包后的路径
COPY target/*.jar app.jar

#暴露端口
EXPOSE 8080
#启动命令
ENTRYPOINT ["java","-jar","app.jar"]

