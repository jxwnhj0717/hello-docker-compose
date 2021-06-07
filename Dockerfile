FROM gradle:7.0.2-jdk8-hotspot AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY settings.gradle /home/gradle/java-code/
COPY build.gradle /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN gradle downloadDependencies -i --stacktrace

FROM gradle:7.0.2-jdk8-hotspot AS builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /app
WORKDIR /app
RUN gradle bootJar -i --stacktrace

FROM adoptopenjdk:8-jdk-hotspot
EXPOSE 8080
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./app.jar

#处理时区
ENV TZ=Asia/Shanghai \
    DEBIAN_FRONTEND=noninteractive
RUN ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone

ENTRYPOINT ["java", "-jar", "app.jar"]