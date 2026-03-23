# **_`Config-Server`_ implementations**

## **`1.` Config `Repo`**

Details: [microservices/config-repo](/codes/microservices/config-repo/)

**Notes**: thứ tự ưu tiên:

- `application.yml`: thấp nhất (`'application'`)
- `order-service.yml`: base (`${application}`)
- `order-service-prod.yml`: cao nhất, khi chỉ định profile

  ```text
  config-repo/
    |__ application/                    // <- shared config
    |       |__ application.yml         // <- 'applicaiton' ('' nếu nằm ở root, không sử dụng application/)
    |__ user-service/                   // <- '${application}'
    |       |__ user-service.yml        // <- base
    |       |__ user-service-prod.yml   // <- with profile
    |__ ...
  ```

  Cách chỉ định profile:
  - Trong `application.yml` của service:

    ```yml
    spring:
    application:
      name: order-service
    profiles:
      active: default    # chỉ định profile (mặc định là default)
    config:
      import: "configserver:http://localhost:8888"X
    ```

  - `env var` (Docker Compose)
    ```yml
    # docker-compose.yml
    services:
      order-service:
        environment:
          SPRING_PROFILES_ACTIVE: docker
          # Config Server nhận GET /order-service/docker
    ```
  - command line:

    ```powershell
    ./gradlew bootRun --args='--spring.profiles.active=docker'
    ```

Thứ tự `ưu tiên cao hơn` sẽ `ghi đè` (nếu có chỗ trùng) thứ tự `ưu tiên thấp`.

---

## **`2.` Config `Server`**

Go to [Spring Initializr](https://start.spring.io/index.html) to create a new project.

Dependencies:

- `Config Server`
- `Actuator`

### **`2.1.` [build.gradle.kts](/codes/microservices/config-server/build.gradle.kts)**

### **`2.2.` Config**: [application.yaml](/codes/microservices/config-server/src/main/resources/application.yaml)

More details: [https://docs.spring.io/spring-cloud-config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_environment_repository)

### **`2.3.` `@EnableConfigServer`**: [config_server/ConfigServerApplication.kt](/codes/microservices/config-server/src/main/kotlin/com/vduczz/config_server/ConfigServerApplication.kt)

### **Test**:

Send `GET` request to config server enpoints `http://localhost:8888/...`:

```text
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

example:

```powershell
curl localhost:8888/foo/development
curl localhost:8888/foo/development/master
curl localhost:8888/foo/development,db/master
curl localhost:8888/foo-development.yml
curl localhost:8888/foo-db.properties
curl localhost:8888/master/foo-db.properties
```

---

## **`3.` Config `Client`**

More details: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_spring_cloud_config_client

#### **dependencies**:

- **Config Client**: `org.springframework.cloud:spring-cloud-starter-config`
- **Retry**: `org.springframeword.retry:spring-retry`

#### **`application.yaml`**

```yml
# application.yml của order-service

spring:
  application:
    name: order-service # ← QUAN TRỌNG: phải khớp với tên file trong config-repo

    # ngoài ra, có thể thay đổi thông tin về client
    # spring.cloud.config.name=product-service
    # spring.cloud.config.profile=dev
    # spring.cloud.config.label=main

  config:
    # optional:configserver:... -> cho phép app vẫn chạy khi connect failed (nuốt exception)
    # configserver:... -> connect fail = throw exception lúc start
    import: "configserver:http://localhost:8888"

  cloud:
    config:
      fail-fast: true # nếu không kết nối được → fail ngay, không start
      retry: # retry with backoff
        max-attempts: 6 # retry 6 lần trước khi fail
        initial-interval: 1500
        multiplier: 1.5
```

**Nguyên tắc**: Mọi config của service để ở config-repo
```text
Config-repo (thay đổi theo môi trường):    Service application.yml (cố định):
────────────────────────────────────────   ──────────────────────────────────
DB URL, username, password                 spring.application.name
Kafka bootstrap servers                    spring.config.import (URL Config Server)
JWT secret                                 spring.cloud.config.fail-fast
External API URLs                          spring.cloud.config.retry
Feature flags
Timeout, retry values
Log levels
Eureka URL
Server port
```

#### `@RefreshScope` config:

> _`@RefreshScope`: cho phép **reload lại `bean` khi config thay đổi** mà **`không` cần `restart` app**_

```kotlin
@ConfigurationProperties(prefix = "app")
@RefreshScope          // reload khi /actuator/refresh được gọi
data class AppConfig(
    val jwtSecret: String = "",
    val maxOrderItems: Int = 50
)

// Hoặc dùng @Value với @RefreshScope
@Component
@RefreshScope
class FeatureFlags(
    @Value("\${feature.newPricing:false}")
    val newPricingEnabled: Boolean
)

// Chỉ bean có @RefreshScope mới reload.
// Bean không có annotation này
// giữ nguyên giá trị cũ cho đến khi restart.
```

#### **Trigger `reload`**

`POST http://service_host:service_port/actuator/refresh`
