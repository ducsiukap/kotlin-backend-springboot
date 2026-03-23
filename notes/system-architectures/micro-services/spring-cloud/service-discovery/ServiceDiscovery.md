# **Service _Discovery_**

## **`1.` Service Discovery & Related Problem**

### **`1.1.` Problems**

Giả sử, trường hợp `Order` gọi `Payment` để thực hiện **thanh toán**:

- Đối với `monolith`:
  - `Order` gọi `paymentService.pay()`
  - Mọi thứ chung trong RAM, địa chỉ bộ nhớ cố định.
- Đối với `microservices`:
  - `Order` và `Payment` được chạy trong các `container` trên các **host** khác nhau
  - Giao tiếp qua giao thức mạng: `HTTP` / `gRPC`

**`Problems`**

- **`Ghost` address**: Trong môi trường **Cloud/Docker**, `IP` của container là `động` (**ephemeral**).
- **`Scale`**: trong một số trường hợp, **Payment** `auto-scale` lên N nodes -> **Load Balancing** cần biết `N-IP` mới này để chia đều traffic.

```kotlin
// Order Service gọi Product Service
// phải hardcode URL
@FeignClient(url = "http://localhost:8082")
interface ProductClient

// Scale Product lên 3 instance?
// localhost:8082, :8083, :8084?
// Service nào down thì sao?
// → không biết, crash
```

> _**Vấn đề**: làm sao xác định được IP và port của service khi nó có thể thay đổi bất kì lúc nào_

### **`1.2.` Service Discovery**

> _**`Service Discovery`** giải quyết một **vấn đề**: service tìm nhau như thế nào khi IP và port có thể thay đổi bất kỳ lúc nào (scale up/down, restart, Docker...)._

Cụ thể, **Service Discovery** xác định:

- `Who` -> service nào?
- `Where` -> host+port
- `Is Up?` -> service is ok.

```kotlin
// Order Service chỉ cần biết tên
@FeignClient(name = "product-service")
interface ProductClient

// Eureka tự giải quyết:
// product-service đang chạy ở đâu?
// → 192.168.1.10:8082 (instance 1)
// → 192.168.1.11:8082 (instance 2)
// Load balance tự động
```

### **`1.3.` Components**

- **Service `Registry`** - **Central Host**:
  - Là một `database` chứa danh sách `map` giữa **Tên Service** (`${application}`) và **`[Danh sách IP:Port]`**.
  - Đại diện tiêu biểu: **`Netflix Eureka`**, HashiCorp Consul, Zookeeper.
- **Service `Provider`** - **Specific Service**:
  - Là service cụ thể trong hệ thống.
  - Khi **start**, cần phải **đăng kí** (báo danh) với **Registry** - **`Registration`**
  - Ex: `Payment`
- **Service `Consumer`** - **Caller Service**:
  - Là service cụ thể, **có nhu cầu gọi tới service khác** (đã đăng kí với Regitry) trong hệ thống.
  - Khi cần gọi tới service khác, **Consumer** cần truy vấn **Registry** để lấy (list) địa chỉ cụ thể của **Provider** - **`Discovery`**
  - Ex: `Order`

### **`1.4.` WorkfLow**

```text
┌─ Eureka Server :8761 ─────────────────────────────┐
│  Registry:                                        │
│    order-service   → 192.168.1.10:8081           │
│    product-service → 192.168.1.11:8082 (x2)      │
│    user-service    → 192.168.1.12:8083           │
│    payment-service → 192.168.1.13:8084           │
└───────────────────────────────────────────────────┘
         ↑ register                    ↑ query
         │ (heartbeat 30s)             │"product-service ở đâu?"
         │                             │
    [services]                  [order-service via Feign]
    (producer)                      (consumer)
```

- `Register`: Khi start, service gửi thông tin (`tên`, `IP`, `port`, `health URL`) lên Eureka (**Registry**).
- `Heartbeat`:
  - Mỗi `30s` service gửi heartbeat.
  - Nếu **mất `90s`** không có heartbeat → Eureka **xóa service** đó khỏi registry.
- `Discover`: `FeignClient` hỏi Eureka "**`product-service` đang ở đâu?**" → nhận list instances → chọn một để gọi.
- `Cache`: **Client `cache registry local`, refresh `30s`**. Eureka down → client vẫn gọi được nhờ cache. (`AP` vs `CP`)

### **`1.5.` `Client-side` vs `Server-side` _Discovery_**

- **`Client-side` Discovery - Spring Cloud Ecosystem / `Eureka`**: **Consumer** thực hiện:
  - Hỏi **Registry** về `target-service`, nhận về **`list N IP`**.
  - Tự áp dụng thuật toán **Round-robin** để chọn IP

  > _Mọi logic về **Load Balancing** nằm ở **Consumer** hoặc **API Gateway**_

- **`Server-side` Discovery - `Kubernetes` / `AWS`**:
  - **Consumer** không cần biết **Registry**, việc của nó là request vào **1 domain chung** (ex: `payment-svc`)
  - **Load Balancer** / **Proxy** (ex: `K8s Service`, ...) đứng giữa, hứng request và tự tìm trong **danh bạ nội bộ** của `K8s` và tự hướng request tới 1 trong các container của service.

---

## **`2.` Implementation**

Details: [Service Discovery - Eureka](./Implementation.md)