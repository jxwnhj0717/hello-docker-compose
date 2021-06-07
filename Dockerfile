# 缓存gradle依赖，只要settings.gradle和build.gradle文件不更新，就不需要重新下载项目依赖
FROM gradle:7.0.2-jdk8-hotspot AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY settings.gradle /home/gradle/java-code/
COPY agent/build.gradle /home/gradle/java-code/agent/
COPY app/build.gradle /home/gradle/java-code/app/
WORKDIR /home/gradle/java-code
RUN gradle copyDependencies \
&& echo $(ls build/dependencies)

# 在docker环境中编译和构建
FROM gradle:7.0.2-jdk8-hotspot AS builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /app
WORKDIR /app
RUN gradle bootJar -i --stacktrace

FROM adoptopenjdk:8-jdk-hotspot
EXPOSE 8080
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar ./app.jar

#处理时区
ENV TZ=Asia/Shanghai \
    DEBIAN_FRONTEND=noninteractive
RUN ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone

ENTRYPOINT ["java", "-jar", "app.jar"]