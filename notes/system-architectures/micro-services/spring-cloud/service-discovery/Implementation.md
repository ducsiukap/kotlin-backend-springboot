# **Service Discovery _Implementation_ - `Eureka`**

## **`1.` Service Dicovery - `Server`**

Go to [Spring Initializr](https://start.spring.io/) to create a new project.

### **`1.1.` Dependencies**

- **Eureka Server** - `org.springframework.cloud:spring-cloud-starter-netflix-eureka-server`
- **Actuator** - `org.springframework.boot:spring-boot-starter-actuator`
- **Config Client** - `org.springframework.cloud:spring-cloud-starter-config`
- **Spring Retry** - `org.springframework.retry:spring-retry`

### **`1.2.` Config**

#### `1.2.1.` Service's `application.yml`:

- `application.yml`

  ```yml
  spring:
  application:
    name: eureka-server

  config:
    import: configserver:http://localhost:8888

  cloud:
    config:
    fail-fast: true
    retry:
      max-attempts: 6
      initial-interval: 1500
      multiplier: 1.5
  ```

- `application-docker.yml` (optional)
  ```yml
  spring:
  config:
    import: configserver:http://config-server:8888
  ```

#### `1.2.2.` **Config-repo config**

details: [config-repo/eureka-server](/codes/microservices/config-repo/eureka-server/)

#### `1.2.3.` `@EnableEurekaServer`

details: [ServiceDiscoveryApplication.java](/codes/microservices/service-discovery/src/main/java/com/example/service_discovery/ServiceDiscoveryApplication.java)

```java
@SpringBootApplication
@EnableEurekaServer // enable eureka server
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }

}
```

---

## **`2.` Service Discovery - `Client`**

### **`2.1.` Dependencies**

- **Eureka Client**: `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`

### **`2.2.` Config**

```yml
# config-repo/order-service/order-service.yml
# Phần eureka đã khai báo ở đây — không cần trong source code

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
      # (prod) defaultZone: http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
  instance:
    # prefer-ip-address
    #   + true -> IP, ex: 192.168.1.10:8080
    #   + false -> hostname, ex: my-service.local:8080
    prefer-ip-address: true

    # Health check URL — Eureka dùng để verify service còn sống
    health-check-url-path: /actuator/health # -> Actuator

    # Metadata — có thể thêm thông tin tùy ý
    metadata-map:
      version: 1.0.0
      zone: primary
```

### **`2.3` Spring Boot `2.x` cần `@EnableEurekaClient`**

---

## **`3.` Verify**

```ps
# 1. Eureka dashboard — mở trình duyệt
http://localhost:8761
# Thấy "Instances currently registered with Eureka"
# ORDER-SERVICE xuất hiện trong danh sách

# 2. Eureka REST API — xem registry dạng JSON
curl http://localhost:8761/eureka/apps
# ← XML/JSON list tất cả registered services

# 3. Xem một service cụ thể
curl http://localhost:8761/eureka/apps/order-service
# ← thông tin instance: IP, port, status UP/DOWN

# 4. Health check
curl http://localhost:8761/actuator/health
# ← {"status":"UP"}
```
