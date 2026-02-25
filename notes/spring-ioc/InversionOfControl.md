# Spring IoC - Inversion of Control

## **1. `IoC` - Inversion of Control**

> _IoC is not a technology/pattern, its `Design Principle` in software engineer_

**Nguyên lí Hollywood**: _"Don't call us, we'll call you"_

- **Thông thường**: khi cần object, sử dụng `constructor` để tạo mới -> tự quản lý object, code flow, lifecycle, ...
- **IoC**: giao quyền kiếm soát bao gồm tạo, quản lý vòng đời, kết nối các object cho trung gian **IoC container** / **ApplicationContext** =>
  khi cần thiết, Framwork sẽ chủ động gọi và truyền object vào nơi nó được gọi.

## **2. `DI` - Dependency Injection** and the ways to implementation IoC

> _IoC là nguyên lí (principle), DI là **công cụ** để Spring tiến hành triển khai IoC_

### **Injector**

`Injector`: là cơ chế tạo và inject dependency (object) vào object khác. Trong Spring, Injector là **IoC Container** và `ApplicationContext` đóng vai trò triển khai injector.

### **`tightly` and `loosely` coupling**

> _**tightly coupling** : phụ thuộc chặt_

```kotlin
// tightly coupled
class Car {
    private val engine: Engine = DieselEngine()
    // Car phụ thuộc chặt vòa DieselEngine
}
// + muốn thay đổi engine -> phải sửa code định nghĩa class Car
// + DieselEngine thay đổi logic khởi tạo -> sửa code ở Car
//      => khó maintain
```

> _**loosely coupling**: phụ thuộc lỏng_

```kotlin
// loosely coupled:
//  + các class không cần biết nhau, chúng giao tiếp qua interface trung gian

// interface -> contract
interface Engine {}

// participants
//  + low-level -> implementation class
@Component // -> Bean
class DieselEngine: Engine {}
//  + high-level
class Car(
    private val engine: Engine // sử dụng interface
) {}

// High-level chỉ cần biết có interface, không quan tâm implementation
// -> dễ scale / test / maintain / change
```

### **DI - Dependency Injection**

> _DI: khi class cần dependency là 1 object của class khác, thay vì tự tạo thông qua `constructor`, class chỉ cần khai báo dependency nó cần dùng._

**Ways to implementation DI**

- **Constructor Injection** : **tiêu chuẩn**

  > _Dependency được khai báo và inject thẳng qua `constructor` của class_

  ```kotlin
  @Service
  class OrderService(
    // khai báo dependencies ngay trong constructor
    private val paymentService: PaymentService
  ) {}

  // Lợi ích
  //    + bất biến, `val` không thể thay đổi sau khi inject -> thread-safe
  //    + Đảm bảo không null -> trừ khi inject thành công. Nếu không, server không thể chạy
  //    + dễ Test
  ```

- **Setter Injection** : ít dùng
  > _Dependency được inject thông qua `set` (setter)_
  ```kotlin
  @Service
  class OrderService(
    private var paymentService: PaymentService? = null
  ) {
    // sử dụng @Autowired để Spring tự động inject
    @Autowired
    fun setPaymentService(paymentService: PaymentService) {
        this.paymentService = paymentService
    }
    // sử dụng khi
    //  + dependency là optional
    //  + hoặc muốn có khả năng thay đổi PaymentService khác ngay khi app đang chạy
  }
  ```
- **Field Injection**
  > _sử dụng `@Autowired` lên field của class_
  ```kotlin
  @Service
  class OrderService {
    @Autowired
    private lateinit var paymentService: PaymentService
  }
  // Không nên dùng vì
  //    + che giấu sự phụ thuộc
  //    + khó viết Unit Test
  //    + dễ dính Circular Dependency (phụ thuộc vòng)
  ```

**Multiple Beans** trong DI:

- Kịch bản:
  - **nhiều low-level** class implement **một interface**
  - high-level yêu cầu interface

=> khi này, khi start server gặp lỗi `NoUniqueBeanDefinitionException`

Giải quyết:

- `@Primary`: được gắn lên low-level class như 1 marker đánh dấu ưu tiên => Spring tự động lấy `@Primary` để inject.
- `@Qualifier`: chỉ định rõ tên lúc inject.
  ```kotlin
  @Service
  class OrderService(
    @Qualifier("vNPayPayment") // tên Bean là tên class viết thường ký tự đầu
    private val paymentGateway: PaymentGateway
  )
  ```
