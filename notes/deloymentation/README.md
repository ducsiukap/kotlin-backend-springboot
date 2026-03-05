# **_Deloyment_**

**Kịch bản deloy**: **_Public_** `localserver` (**không sử dụng Cloud/VPS**) thành `static-URL` ra `internet`, sử dụng `cloudflare`.

**Yêu cầu**: có `domain` và cài `Docker` trên local (hoặc không).

**Why?**: chi phí mua **domain** tương đối rẻ so với thuê **VPS**.
Nhược điểm:

- server không chạy 24/24 (chủ động trong việc start/stop server)
- bảo mật, lưu trữ, ...

---

### **Step 1: Mua `domain` và trỏ về cloudflare:**

1. Mua 1 domain, example: `vduczz.io.vn`
2. Đăng nhập [Cloudflare.com](https://cloudflare.com)
3. Click **_Add a site_** -> **_Connect a Domain_**
4. Nhập "vduczz.io.vn" -> **_Continue_**
5. Select **_Free_** plan -> **_Continue_**
   > _Khi này, cloudflare hiện ra 2 cái **NameServer**, ví dụ: `amy.ns.cloudflare.com`_
6. Quay lại trang web bán domain, tìm phần **Cập nhật NameServer**
7. **Ngoài 2 cái đầu tiên**, xóa các **NameServer mặc định** của domain.
8. Dán 2 **NameServer** của `cloudflare` vào 2 **NameServer** còn lại -> **Save**
9. Quay lại web `Cloudflare`, click **Check nameservers** (thường tầm `5`-`30` minutes)

### **Step 2: mở `cloudflare` tunnels:**

1. Ở web Cloudflare, ở phần **Left-side bar**, truy cập **_Networking_** -> **_Tunnels_**
2. Sau đó, click **Create Tunnel** -> nhập **Tunnel name** (ex: vduczz) -> **Create Tunnel**
3. Ở **Setup Enviroment**, chọn **Docker**.
4. Copy `token` ở **Run tunnel with Docker**, sau phần `--token`

### **Step 3: đóng gói toàn bộ project (App, Redis, MySQL,...)**

- Tạo [docker-compose.yml](/codes/mini-project/docker-compose) ở **THƯ MỤC GỐC** của project (cùng cấp với `/src`, `build.gradle.kts`, ...)

  ```yml
  # example docker-compose.yml
  services:
  # MySQL
  ksb-mysql:
    image: mysql:8.0
    container_name: ksb-mysql
    environment:
    MYSQL_DATABASE: ${DB_NAME}
    MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    interval: 10s
    retries: 5

  # Redis
  ksb-redis:
    image: redis:latest
    container_name: ksb-redis
    ports:
      - "6379:6379"
    healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 5s
    retries: 5

  # Application
  mini-project-app:
    build: .
    container_name: mini-project-app
    # Không cần mở port vì cloudflare đã xử lý
    #    ports:
    #      - "8080:8080"
    environment:
      # MySQL
      - SPRING_DATASOURCE_URL=jdbc:mysql://ksb-mysql:3306/${DB_NAME}?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=${DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      # Redis
      - SPRING_DATA_REDIS_HOST=ksb-redis
      - SPRING_DATA_REDIS_PORT=6379
      #JWT
      - APPLICATION_SECURITY_JWT_SECRET_KEY=${JWT_SECRET_KEY}
      # MAIL
      - SPRING_MAIL_USERNAME=${MAIL_USERNAME}
      - SPRING_MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
    ksb-mysql:
      condition: service_healthy
    ksb-redis:
      condition: service_healthy
    # -> chỉ start server khi mysql + redis chạy
  cloudflare:
    image: cloudflare/cloudflared:latest
    container_name: cloudflared
    command: tunnel --no-autoupdate run
    environment:
      - TUNNEL_TOKEN=${CF_TUNNEL_TOKEN}
    depends_on:
      - mini-project-app
    restart: unless-stopped

  # ổ cứng cho mysql
  volumes:
  mysql_data:
  ```

  > _Để ẩn thông tin cần thiết, tạo `.env` cùng cấp với `docker-compose.yml` và định nghĩa các tham số_

  ```env
  # example .env

  # Database
  DB_NAME=your_db_name
  DB_USER=root
  DB_PASSWORD="your_db_password"

  # JWT
  JWT_SECRET_KEY=your_secret_key

  # CloudFlare
  CF_TUNNEL_TOKEN=your_cloudflare_token

  # Mail
  MAIL_USERNAME=your_email
  MAIL_PASSWORD=your_google_app_password
  ```

- Tạo [Dockerfile](/codes/mini-project/Dockerfile) cùng cấp với [docker-compose.yml](/codes/mini-project/docker-compose.yml)

  ```dockerfile
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
  ```

### **Step 4: start server**

- Chạy lần đầu or khi có cập nhật code:

  ```cmd
  docker compose up -d --build
  ```

### **Step 5: Routes**

Sau khi chạy server thành công, quay lại cloudflare web, check **Connection** và click **Continue**

Quay lại **Tunnel**:

1. Chọn Tunnel vừa tạo (`vduczz`)
2. Chọn **Add route** -> **Public application**
3. Nhập **Subdomain**, ex: api
4. Nhập **Service URL**: `http://mini-project-app:8080`, với:
   - `mini-project-app`: tên server được cấu hình trong [docker-compose.yml](/codes/mini-project/docker-compose.yml)
   - `:8080`: port của server được cấu hình trong [docker-compose.yml](/codes/mini-project/docker-compose.yml)

   sau đó, click **Add route**.

---

### **Your _public_ server**:

Khi này, url dạng: `<subdomain>.<domain>` (ex: `api.vduczz.io.vn`) sẽ trỏ tới `http://mini-project-app:8080` (local-server đang chạy ở Docker trên máy) => **Đã có thể truy cập từ ngoài internet**

Truy cập vào `api` tương tự như khi truy cập dùng `localhost`, ex: `api.vduczz.io.vn/api/v1/users`, ...

**Note**:

- Chỉ có thể truy cập được khi server trong **Docker** được chạy.
- Để chạy server, chỉ cần tới project và chạy lệnh:
  - khi có update code:
    ```cmd
    docker compose up -d --build
    ```
  - khi chỉ cần chạy:
    ```cmd
    docker compose up -d
    ```
