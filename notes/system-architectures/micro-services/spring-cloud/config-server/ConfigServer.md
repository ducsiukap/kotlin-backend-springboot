# **_Config_ server**

## **`1.` Ý tưởng**

### **`1.1.` Ý tưởng**:

Thay vì mỗi service tự chứa `application.yml`, **Config Server** lưu tất cả `config` trong một `Git repo`. **Khi service start**, nó `pull config` từ **Config Server** về — thay đổi config không cần redeploy.

### **`1.2.` _Tại sao_ lại _cần_ `config server`**:

Giả sử, hệ thống có `N-services`, mỗi loại chạy trên 3 môi trường: `dev`, `prod` và `staging` => Ta cần quản lý `Nx3 file config`, nằm **rải rác** ở các service khác nhau:

| **Scenario**                         |                        **`KHÔNG CÓ` config server**                        |                                                         **`CÓ` config server**                                                          |
| :----------------------------------- | :------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------: |
| Change `DB_PASSWORD`                 | Phải tìm tới từng project, sửa file config, sau đó `rebuild` + `redeploy`. | Chỉ cần đổi ở 1 chỗ - Config Server - các service `POST /actuator/refresh` để lấy config mới nhất mà **không cần rebuild hay redeploy** |
| Tăng `RateLimier` trong giờ cao điểm |         Sau khi sửa, cần `restart container` -> **user phải chờ**          |                                                    Đơn giản, vẫn chỉ cần refresh lại                                                    |
| ...                                  |                                    ...                                     |                                                                   ...                                                                   |

**Mục đích chính**: cho phép **thay đổi `config`** mà **KHÔNG CẦN `redeploy`**.  
Mọi thứ khác, bao gồm quản lý tập trung config, secret, enviroment, ... đều là hệ quả của vấn đề này.

---

## **`2.` Cơ chế hoạt động**

#### **Components**

- **`Backend Storage` - Lưu trữ tập trung**: File cấu hình được lưu trữ tại **1 hệ thống quản lí phiên bản - `version control system`**, thường là **`Git`**, **`GitHub`**, **`GitLab`**, **`SVN`**, ...
  ```text
  ┌─ 1. Config Repo (Git) ──────────────────────────┐
  │  config-repo/                                   │
  │    application.yml   // shared, mọi service     │
  │    order-service/                               │
  │      order-service.yml  // chứa ${DB_PASSWORD}  │
  │      order-service-docker.yml                   │
  └─────────────────────────────────────────────────┘
  ```
- **`Config Server`**: **Đứng ở giữa**, đóng vai trò kết nối với **Backend Storage** để lấy file cấu hình về.

  ```text
  ┌─ 2. Config Server :8888 ───────────────────────────────┐
  │  @EnableConfigServer                                   │
  │  application.yml → trỏ đến config repo                 │
  │                                                        │
  │  Env vars trên máy này:                                │
  │    DB_PASSWORD=supersecret123    ← resolve placeholder │
  │    JWT_SECRET=coffee-key-2025!!                        │
  └────────────────────────────────────────────────────────┘
  ```

  > _**Config Server** là `middleman` — đọc `placeholder` từ **Git repo**, **resolve bằng `env var`** trên máy của nó, **trả giá trị thật cho service**. Service không bao giờ biết giá trị đến từ đâu._

- **`Config Client` - services**: Khi một service khởi động, việc đầu tiên nó làm không phải là chạy code, mà là gọi lên Config Server để lấy file config của môi trường tương ứng.
  ```text
  ┌─ 3. Order Service :8081 ───────────────────────────────┐
  │  application.yml:                                      │
  │    spring.application.name: order-service              │
  │    spring.config.import: configserver:http://...:8888  │
  │                                                        │
  │  Nhận được:                                            │
  │    spring.datasource.password = "supersecret123"       │
  └────────────────────────────────────────────────────────┘
  ```

#### **Flow**

- **`1.` Service start**: Service đọc `bootstrap.yml` (hoặc `spring.config.import`), biết **Config Server** ở đâu.
- **`2.` Pull config**: Service gọi `http://config-server:8888/{service-name}/{profile}` để lấy config.
- **`3.` Config Server**: Đọc file từ Git repo tương ứng, trả về JSON chứa tất cả properties.
- **`4.` Service dùng config**: Merge config từ server vào environment — override local `application.yml`.
- **`5.` Refresh (optional)**:
  `POST /actuator/refresh` → service reload config không cần restart.

---

## **`3.` Implementation**: [Config Server Implementation](./Implementation.md)
