# Spring core `Container`

> _the `Core Container` consists of the `Core`, `Bean`, `Context` and `Expression Language` modules._

## **1. `Core` and `Bean`**

> _**Core** and **Bean** modules provide the fundamental parts of the framework, including the IoC and Dependency Injection features_

- `Bean`: Spring Bean là 1 object (instance of a class), được _khởi tạo_, _inject_ và _quản lý_ bởi `IoC container`
- `BeanFactory` - factory pattern chuyên _tạo_ và _quản lý_ các Object.

> _Nếu Object không nằm trong Application Context của Spring thì nó không được coi là Bean => không thể DI, ..._

```kotlin
// example print list of beans
val appContext: ApplicationContext = runApplication<KsbDemoApplication>(*args)
println("list of beans: ")
for (bean in appContext.beanDefinitionNames) {
    println(bean)
}
```

### **Thêm object vào Context (tạo Bean)**

- `Auto-detect` -> Component Scan
  > _Khi server chạy, Component Scan quét toàn bộ directory và đưa toàn bộ class với Annotation phù hợp thành Bean_
  - `@Component`: dùng chung cho mọi class, là annotation gốc
  - `@Service`: -> business logic
  - `@Repository`: -> database
  - `@Controller`/`RestController` -> HTTP
- Manual registration -> `@Bean`
  > _Được sử dụng khi muốn biến class thành Bean thủ công_
  - `@Configuration` -> file config
  - `@Bean` -> method
  ```kotlin
  @Configuration
  class SecurityConfig {
    // tự tạo object PasswordEncoder của BCrypt và chuyển thành Bean
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
  }
  ```

### **Bean Scope**

Khác với `event-loop` của Node.js chạy single-thread, **Kotlin/Java** là môi trường `multi-thread` -> mỗi request là 1 Thread mới được sinh ra.

> _By default, all Bean is **Singleton** -> chỉ tồn tại 1 instance của Bean trong ApplicationContext -> **dùng chung**_

**Note**: không khai báo biến toàn cục - global state - lưu trữ dữ liệu thay đổi bên trong Bean

Ngoài Singleton, Spring còn có:

- `Prototype`: tạo mới Bean cho mỗi lần gọi
- `Request` scope

### **Bean lifecycle**

- Instantiation: Spring gọi `constructor` của Bean để tạo object.
- DI: Spring tìm các Bean khác và inject các Bean phụ thuộc vào
- `@PostConstructor` - khởi động sau inject: logic cần chạy ngay sau khi object được inject, ex: load data, ping server, ... -> gắn `@PostConstructor` lên trên method đó trong class của Bean
- Sẵn sàng: Bean được tạo thành công và nằm trong ApplicationContext, chờ đợi được gọi.
- `@PreDestroy` - dọn dẹp: gắn `@PreDestroy` vào hàm dọn dẹp trong class của Bean để Spring gọi khi tắt Server (Bean bị hủy)

## 2. `Context`

> _**Context** module builds on the solid base provided by **Bean** and **Core** modules -> access objects in a framework-style manner_

- `BeanFactory`:
  - chứa những tính năng cơ bản nhất của IoC
  - **Lazy-loading**: khi thực sự cần gọi tới Bean, BeanFactory mới tạo object.
- `ApplicationContext`:
  - inherits from BeanFactory
  - **Eager-loading**: ngay khi chạy server, ApplicationContext đã tạo sẵn toàn bộ các Bean, nạp sẵn vào bộ nhớ và sẵn sàng sử dụng  
    `Lợi ích`: **Fail-fast** -> cấu hình sai, tiêm chéo, ... -> sập ngay khi khởi tạo thay vì sập sau khi server đã chạy ổn định và gặp lỗi.

```kotlin
// ApplicationContext hoạt động như nào?

@SpringBootApplication
class KsbDemoApplication

fun main(args: Array<String>) {
    // runApplication -> khởi chạy project
    runApplication<KsbDemoApplication>(*args)
        // + Spring tạo ApplicationContext
        // + Tính năng ComponentScan bắt đầu scan các class với annotation: @Component, @Service, @RestController, @Repository, ... và đưa vao ApplicationContext
        // + đọc các yêu cầu DI và liên kết các Bean
        // + khởi động Apache Tomcat server
}
```

Ngoài việc chứa và quản lý các Bean, Context còn có thể:

- Enviroment Abstraction - quản lí môi trường và Profile
  > _cho phép cấu hình môi trường chạy của server_
  - `@Profile("dev")`, `@Profile("prod")`, ...
  - khi bật server, ApplicationContext đọc `application.properties` để xác định môi trường và chọn các Bean phù hợp để quản lý
- Event Publishing

  > _dựa trên **Observer Pattern** -> giảm Coupling_

  ```kotlin
  // event
  data class OrderCreatedEvent(
    // chứa thông tin cần truyền
    val orderId: String,
    val totalAmount: Double,
    val userEmail: String
  )

  // publisher
  @Service
  class OrderService(
    // ApplicationEventPublisher
    private val eventPublisher: ApplicationEventPublisher
  ) {
    fun createOrder() {
        // logic ...

        // create event
        val event = OrderCreatedEvent(
            orderId, amount, userEmail
        );

        // publish
        eventPublisher.publish(event)
    }
  }

  // listener
  @Service
  class EmailService {

    // Annotation: @EventListener
    @EventListener
    fun handleOrderCreated(event: OrderCreatedEvent) {
        // ...
    }
  }
  ```

  Khi có sự kiện:
  - Publisher chỉ cần phát sự kiện đó.
  - Các Listener đã đăng ký lắng nghe sự kiện `@EventListener` / `@TransactionalEventListener` (chỉ bắn sự kiện sau khi transaction commit DB thành công) sẽ được tự động gọi và thực thi.

  Mặc định, Spring Event là `synchronous` (đồng bộ) -> khi thực hiện publish event, Publisher phải chờ các Listener thực hiện xong mới có thể chạy tiếp.

  ```kotlin
  // example code thực thi xử lý bất đồng bộ

  // ProjectNameApplication.kt
  // file chạy project
  @SpringBootApplication
  @EnableAsync // thêm @EnableAsync
  class KsbDemoApplication

  fun main(args: Array<String>) {
    runApplication<KsbDemoApplication>(*args)
  }

  // ở Listener, thêm @Async trên ở hàm lắng nghe
  @Async
  @EventListener
  fun handleEvent(event: Event) {
        // ...
  }
  // khi này, task handleEvent() sẽ được chạy trên thread khác
  // => Publisher có thể tiếp tục chạy mà không cần chờ
  ```

- Hỗ trợ đa ngôn ngữ: Context hỗ trợ đọc sẵn các file như `message_vi.properties`, `message_en.properties`, ... để trả vễ thông báo lỗi API theo đúng ngôn ngữ mà client gửi lên qua **HTTP Header**
