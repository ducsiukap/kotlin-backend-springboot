# **Spring Cloud _Gateway_**

![API Gateway](./api_gateway.png)

## **`1.` `API` _Gateway_**

### **`1.1.` API Gateway**

> _**`API Gateway`** là **single entry point** - điểm vào duy nhất - của **toàn hệ thống**._

Client **không gọi trực tiếp** từng `service` — chỉ biết 1 địa chỉ duy nhất - **Gateway** - và **tất cả `request` phải đi qua `Gateway`**. Gateway xử lý:

- `routing`: gom đường dẫn
- `auth`: xác thực
- `rate limiting`: chống DDos
- logging
- circuit breaker
- ...

trước khi request tới được với services.

### **`1.2.` Request `Flow`**

![Request Flow](./request_lifecycle.png)

---

## **`2.` Implementations: [Setup API Gateway](./SetupApiGateway.md)**

### **`1.3.` _Routing_ - điều hướng `request`**

`Routing` là quá trình điều hướng request đến đúng service
dựa trên path, header, method, query param.

`Route` gồm 3 phần:

- `ID`: định danh
- `Predicate`: điều khiện khớp: **Predicate khớp** → request được **forward đến URI** đó.

  **Pedicate Types**:
  - `Path`: `Path=/api/v1/orders/**`: khớp URL path, hỗ trợ wildcard `**` và `{param}`
  - `Method`: `Method=GET,POST` — chỉ cho phép HTTP method cụ thể
  - `Header`: `Header=X-Version, v2` — khớp header và giá trị (v2 thường là regex)
  - `Host`: `Host=**.myapp.com` — routing theo subdomain
  - `Query`: `Query=version, v2` — khớp query parameter
  - `Weight`: `Weight=group1, 80` — canary deploy: 80% request trỏ về v1 (group1), 20% v2 -> **A/B testing**

- `URI`: điểm đích.
  - `uri: lb://order-service `: ← tìm "order-service" trong Eureka
  - `uri: http://order-service:8082`: ← gọi trực tiếp, không qua Eureka
  - `uri: forward:/fallback/orders`: ← forward nội bộ trong gateway

### **`1.4.` _Filter_ - xử lý `request`/`response`**

Filter chạy trước (`pre`) và sau (`post`) khi **forward request**:

- **_Previous_ filter**: `auth`, `add header`, `rate limit`.
- **_Post_ filter**: add `CORS` header, `log` response time, transform response body.

### **`1.5.` Auth & Security**

Xác thực `JWT` **một lần tại Gateway** — các service bên trong tin tưởng Gateway đã verify rồi, chỉ cần đọc header X-User-Id. Không cần mỗi service tự verify JWT.

### **`1.6.` Rate Limiter**

`Rate Limiter` tại Gateway ngăn một client gửi quá nhiều request, bảo vệ tất cả services phía sau. Dùng Redis để lưu counter — hoạt động đúng khi chạy nhiều Gateway instance.

### **`1.7.` Circuit Breaker**

Khi **`service` downstream down**, thay vì trả 503 về client, Gateway có thể trả về response dự phòng (`fallback`) — trang thông báo lỗi thân thiện, cached data, hoặc default response.

```
shopflow/
├── api-gateway/ ← đang làm
│   ├── src/main/java/com/shopflow/gateway/
│   │   ├── GatewayApplication.java
│   │   ├── config/
│   │   │   ├── GatewayConfig.java ← routes
│   │   │   ├── SecurityConfig.java ← CORS, security
│   │   │   └── RateLimitConfig.java ← key resolvers
│   │   ├── filter/
│   │   │   ├── AuthenticationFilter.java ← JWT validate
│   │   │   └── LoggingFilter.java ← global logger
│   │   ├── security/
│   │   │   └── JwtTokenProvider.java ← JJWT validate
│   │   └── controller/
│   │       └── FallbackController.java ← CB fallback
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── user-service/
├── order-service/
├── product-service/
├── inventory-service/
├── notification-service/
└── docker-compose.yml
```
