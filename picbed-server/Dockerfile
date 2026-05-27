FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17
WORKDIR /app
COPY target/picbed-server-1.0.0.jar app.jar
RUN mkdir -p /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
