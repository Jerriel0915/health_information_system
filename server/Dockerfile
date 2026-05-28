# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./
COPY ruoyi-admin/pom.xml ruoyi-admin/pom.xml
COPY ruoyi-common/pom.xml ruoyi-common/pom.xml
COPY ruoyi-framework/pom.xml ruoyi-framework/pom.xml
COPY ruoyi-generator/pom.xml ruoyi-generator/pom.xml
COPY ruoyi-quartz/pom.xml ruoyi-quartz/pom.xml
COPY ruoyi-system/pom.xml ruoyi-system/pom.xml

RUN mvn -B -ntp dependency:go-offline

COPY . .

RUN mvn -B -ntp clean package -DskipTests -pl ruoyi-admin -am

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx512m"

RUN mkdir -p /data/upload /app/logs

COPY --from=builder /build/ruoyi-admin/target/ruoyi-admin.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
