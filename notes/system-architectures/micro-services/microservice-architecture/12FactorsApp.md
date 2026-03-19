# `12-factors` App

## **`1.` _Codebase_**

**Mỗi microservice** có đúng **một repo Git riêng biệt**.

- `repo` độc lập: **repo (`folder`) code riêng** + **repo `.git` riêng**
- Deploy lên nhiều môi trường (dev/staging/prod) từ cùng một repo.
- Nếu **nhiều app share codebase** → **mỗi cái** cần **repo riêng**.

Ví dụ:

- Cấu trúc project

  ```text
  my-org/
  user-service/     ← repo riêng, CI/CD riêng
  order-service/    ← deploy độc lập
  payment-service/  ← version độc lập
  shared-lib/       ← shared library giữa các services
  ```

- Cài đặt `shared libs`

  ```xml
  <!-- SAI: copy-paste code giữa service -->

  <!-- ĐÚNG: publish shared lib -->
  <dependency>
  <groupId>com.myorg</groupId>
  <artifactId>shared-events</artifactId>
  <version>1.2.0</version>
  </dependency>
  ```

---

## **`2.` _Dependencies_**

Nguyên tắc: **Không bao giờ được mặc định `host` đã _cài sẵn thư viện gì_**

**Khai báo tường minh** tất cả `dependency` trong `pom.xml` / `build.gradle.kts`. **Không phụ thuộc** vào tool hay library '**_có sẵn_**' trên server. Mọi thứ phải `reproducible` từ một lệnh build sạch.

```xml
<!-- pom.xml -->

<!-- Spring Boot BOM quản lý version tập trung -->
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.0</version>
</parent>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
</dependencies>
```

```dockerfile
# Dockerfile

FROM eclipse-temurin:21-jre-alpine
# Mang cả JRE theo — không phụ thuộc server
WORKDIR /app
COPY target/user-service.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

# Build reproducible:
mvn clean package -DskipTests
docker build -t user-service:1.0.0 .
```

---

## **`3.` _Config_** (`**`)

Tất cả `config` **thay đổi theo môi trường** (`DB URL`, `API key`, `port`, ...) **_phải lưu trong environment variables - `.env`_** — không hardcode trong code, không commit vào Git.

**`Config`** là thứ **DUY NHẤT khác nhau** giữa `dev` và `prod`

```yml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost/mydb}
    username: ${DB_USER:admin}
    password: ${DB_PASS} # bắt buộc, không có default
  jpa:
    hibernate.ddl-auto: ${JPA_DDL:validate}

app:
  jwt-secret: ${JWT_SECRET} # KHÔNG bao giờ hardcode
  payment-url: ${PAYMENT_URL:http://payment-svc}

server:
  port: ${SERVER_PORT:8080}
```

> _Dùng **Spring Cloud Config Server** để quản lý config tập trung. Config lưu trong Git repo riêng, mỗi service pull về theo profile (dev/prod)._

---

## **`4.` _Backing Service_**

`Database`, `Redis`, `Kafka`, `SMTP`, ... đều là '**attached resource**' — **_kết nối qua URL trong config_**. Swap từ local PostgreSQL sang AWS RDS chỉ bằng thay `.env` var, không sửa code.

```env
# .env

# Local dev
DB_URL=jdbc:postgresql://localhost:5432/mydb
REDIS_HOST=localhost
KAFKA_BOOTSTRAP=localhost:9092

# Production — chỉ thay config, không thay code
DB_URL=jdbc:postgresql://prod-db.rds.amazonaws.com/mydb
REDIS_HOST=prod-redis.cache.amazonaws.com
KAFKA_BOOTSTRAP=kafka-prod:9092
```

---

## **`5.` _Build_, _Release_, _Run_**

Ba giai đoạn tách biệt:

- **Build** : `compile` → `jar`
- **Release**: `jar` + `config` = `artifact` bất biến
- **Run** (chạy).

Khi này:

- **Không sửa code** trực tiếp trên `production`.
- Mỗi `release` có `version`, có thể **rollback**.

```yml
# github actions pipline

jobs:
  build: # STAGE 1: Build
    steps:
      - run: mvn clean package -DskipTests
      - run: docker build -t svc:${{ github.sha }} .
      - run: docker push svc:${{ github.sha }}

  deploy: # STAGE 2: Release + Run
    steps:
      - run: |
          helm upgrade --install user-svc ./helm \
            --set image.tag=${{ github.sha }}
```

> _**`Artifact` bất biến**: image Docker tagged bằng `git SHA` không bao giờ thay đổi. Không dùng '`latest`' tag trong production._

---

## **`6.` _Processes_** (`**`)

Service phải `stateless` — **không lưu** bất kỳ `state` nào **trên memory** giữa các request. **Sticky session, in-memory cache**, **local file** đều **vi phạm**.  
State phải nằm ở backing service (Redis, DB, S3)

```java
// SAI: state trên memory (mất khi restart)
@Service
public class CartService {
  private Map<String, Cart> store = new HashMap<>();
  // pod 2 không thấy data của pod 1!
}

// ĐÚNG: lưu ra Redis
@Service
public class CartService {
  @Autowired RedisTemplate<String, Cart> redis;
  public void addItem(String uid, Item item) {
    Cart c = redis.opsForValue().get("cart:"+uid);
    if (c == null) c = new Cart();
    c.addItem(item);
    redis.opsForValue().set("cart:"+uid, c, 30, MINUTES);
  }
}
```

`SPRING SESSION`: share session qua `redis`

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.session</groupId>
  <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

```java
@EnableRedisHttpSession
@Configuration
public class SessionConfig {}
```

```yml
# application.yml
spring.session:
  store-type: redis
  timeout: 30m
# Tất cả pod chia sẻ cùng session store
```

---

## **`7.` Port Binding**

Service tự `expose` HTTP qua **`port` riêng**

- không deploy WAR lên Tomcat ngoài
- không cần web server external.

Spring Boot embed Tomcat/Netty nên factor này đã đạt mặc định

```yml
# application.yml

server:
  port: ${SERVER_PORT:8080}

# Mỗi service chạy trên port riêng:
# java -jar user-service.jar    → :8080
# java -jar order-service.jar   → :8081
# java -jar payment-service.jar → :8082

# Không cần: deploy WAR lên Tomcat standalone
# Không cần: cài JBoss/WebLogic trên server
```

---

## **`8.` _Concurrrency_**

**`Scale`** bằng cách: **chạy thêm `process`** (`horizontal`), **KHÔNG PHẢI** làm **_thread pool lớn hơn_** (`vertical`).

> _Kết hợp Factor 6 (stateless), có thể thêm pod bất kỳ lúc nào không lo mất data._

- `Java 21` - **Virtual Thread**

  ```yml
  # application.yml

  # application.yml — Spring Boot 3.2+
  spring:
  threads:
    virtual:
    enabled: true
  # Dùng Virtual Thread thay OS Thread
  # Handle nhiều concurrent request hơn
  # Không cần tăng thread pool size
  ```

  > _Java 21 Virtual Threads (Project Loom) tăng throughput đáng kể — kết hợp tốt với horizontal scaling._

- **`Kubernetes HPA` - tự động scale**
  ```yml
  apiVersion: autoscaling/v2
    kind: HorizontalPodAutoscaler
    spec:
    scaleTargetRef:
        name: user-service
    minReplicas: 2
    maxReplicas: 10
    metrics:
        - type: Resource
        resource:
            name: cpu
            target:
            averageUtilization: 70  # scale khi CPU > 70%
  ```

---

## **`9.` _Disposability_** (`**`)

Service phải:

- **start nhanh** (`< 30s`)
- **shutdown graceful** — hoàn thành request đang xử lý, đóng DB connection, flush log.

Kubernetes gửi SIGTERM trước khi kill pod, phải xử lý đúng.

- **Graceful shutdown** config:

  ```yml
  # application.yml
  server:
  shutdown: graceful # chờ request xong mới tắt
  spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
  ```

  ```yml
  # kubernetes deployment.yml
  spec:
  template:
    spec:
    terminationGracePeriodSeconds: 60
    containers:
      - lifecycle:
          preStop:
          exec:
            command: ["sh", "-c", "sleep 5"]
            # drain load balancer trước SIGTERM
  ```

- `@PreDestroy`

  ```java
  @Component
  public class GracefulShutdown {
      @Autowired KafkaListenerEndpointRegistry kafka;

      @PreDestroy
      public void onShutdown() {
          // 1. Dừng nhận Kafka message mới
          kafka.stop();

          // 2. Chờ message đang xử lý xong
          kafka.getListenerContainers()
                  .forEach(c -> c.waitWhileRunning(20_000));

          log.info("Graceful shutdown complete");
      }
  }
  ```

---

## **`10.` _`Dev`/`Prod` Parity_** (`**`)

Khoảng cách giữa dev và prod càng nhỏ càng tốt:

- `time gap` (deploy nhanh)
  > _Thay vì **Code xong ngâm ở nhánh `develop` , đợi cuối tháng gom thành 1 cục rồi mới release**, thì **Thu hẹp từ "tháng/tuần" xuống còn "giờ/phút", `git push` -> hệ thống `CI/CD` hoạt động.**_
- `personnel gap` (dev tự deploy)
  > _Thay vì **`dev` code xong và gửi file `.war` cho `Ops` để deloy**, thì **`dev` lo code + deloy, `ops` lo về `kubernetes`, ...**_
- `tools gap` (cùng DB, OS, version)

**Rule**: Giữ môi trường code, staging và production **_giống nhau_** nhất có thể

---

## **`11.` _Logs_**

Service thực hiện:

- **write log ra `stdout`** như một stream của events
- **KHÔNG**:
  - không tự quản lý `log file`
  - không rotate
  - không ghi vào /var/log.

Platform (ELK, Loki, CloudWatch) sẽ thu thập và index.

---

## **`12.` _Admin Processes_**

Tác vụ **quản trị một lần** (`DB migration`, `data fix`, `report`) phải chạy như **process riêng biệt trong cùng môi trường với app** — không nhúng vào startup logic hay scheduled job thường xuyên.

```yml
apiVersion: batch/v1
kind: Job # không phải Deployment!
spec:
  template:
    spec:
      restartPolicy: OnFailure
      containers:
        - name: migration
          image: user-service:1.5.0
          args:
            - "--spring.batch.job.name=migrateUsers"
# Chạy xong tự kết thúc, K8s lưu log
```
