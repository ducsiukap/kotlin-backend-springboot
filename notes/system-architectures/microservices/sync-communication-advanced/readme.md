# **`Synchronous` communication _advanced_**

## **`1.` `Circuit Breaker` design pattern**

**Problem**: `Service A` cố gắng gọi `Service B` một cách đồng bộ - **Synchronous** - liên tục, bất chấp việc các lần trước đó đều xảy ra lỗi.

Khi này, rõ ràng `B` có thể đã bị `down` hoặc đang bị nghẽn mạng, tải nặng, ... Vì vậy, thay vì trả về ngay, nó **treo API** quá lâu để xử lý hoặc **request vượt quá timeout**.

**Hậu quả**:

- **Trải nghiệm người dùng**: Thời gian chờ - `latency` - **tăng lên đáng kể** và có thể trở nên **lãng phí** nếu `A` bị lỗi **xuất phát từ phía `B`**
- **Với `Service A`**: **Thread Pool** bị **cạn** liên tục sau vài phút, **tràn bộ nhớ**, **quá tải CPU**, ... và có thể dẫn tới `crash service`

**Solution**: [**Circuit Breaker**](./circuit-breaker/readme.md)
---

## **`2.` **
