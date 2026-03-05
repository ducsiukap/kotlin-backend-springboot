# ==========================================
# STAGE 1: Mượn thợ xây (Gradle) đúc ra file .jar
# ==========================================
FROM gradle:9-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# ==========================================
# STAGE 2: Chạy App (Chỉ lấy file .jar, vứt code rác đi)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy đúng cái cục .jar từ STAGE 1 ném sang đây
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# Lệnh khởi động Server
ENTRYPOINT ["java", "-jar", "app.jar"]