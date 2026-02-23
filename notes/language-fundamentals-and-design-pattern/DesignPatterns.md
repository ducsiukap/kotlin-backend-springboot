# Design Patterns

## **1. Singleton Pattern**
> _**Singleton** đảm bảo 1 class chỉ có `duy nhất 1 instance` được tạo ra trong suốt dòng đời ứng dụng và `cung cấp 1 điểm truy cập toàn cục tới nó`_

Mục đích:
- Tiết kiệm bộ nhớ
- Quản lí trạng thái tập trung => tránh xung đột

example
```kotlin
// kotlin 
// => sử dụng `object` cho singleton
object Config {
    fun getValue(): String = "config"
}

// java
// => private constructor + private static instance + public getInstance()
class Config {

    // instance
    private static Config instance;

    // private constructor
    // => không cho phép khởi tạo từ ngoài
    private Config() {}

    // public static getInstance()
    public static getInstance() {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    // method
    public String getValue() {
        return "config"
    }
}
```
> _Mặc định, gần như tất cả các Bean (Controller, Service, Repository) mà Spring tạo ra và quản lý đều là Singleton, chúng **được tạo ra khi app chạy** và **được dùng chung cho mọi request từ user**_

## **2. Factory Pattern**
> _Giao (giấu) việc khởi tạo object cho 1 "**factory**", client chỉ cần yêu cầu đúng loại object và nhận lại kết quả từ factory_

Mục đích: tránh class phải `Tight Coupling` - phụ thuộc chặt - vào các class triển khai cụ thể => tuân thủ Open/Close (O) trong SOLID

```kotlin

// abstract 
interface PaymentMethod {
    fun pay();
}

// low-level 
// -> class implementation
class VNPayPayment : PaymentMethod {
    override fun pay() {
        // ...
    }
}
class CashPayment : PaymentMethod {
    override fun pay() {
        // ...
    }
}

// factory
// -> provide method to create object -> create()/query(), ....
object PaymentFactory {
    fun create(type: String) : PaymentMethod {
        return when(type) {
            "VNPAY" -> VNPayPayment()
            "CASH" -> CashPayment()
            else -> throw IlligalArgumentException("Unsupported payment method: $type") 
        }
    }
}

// high-level / client
val payment = PaymentFactory.create("VNPAY")
//...
```

## **3. Builder Pattern**
> _Builder Pattern dùng để tạo object phức tạp từng bước_

Cụ thể, Builder Pattern tách rời quá trình xây dựng Object ra khỏi class, khi cần xây dựng nhiều bước

```kotlin
// private constructor
class TimeoutConfig private constructor(
    val timeoutMs: Int,
    val maxRetries: Int,
    val enableFallback: Boolean
) {
    // builder
    class Builder {
        // default parameters
        private val timeoutMs: Int = 3000 
        private val maxRetries: Int = 3
        private val enableFallback: Boolean = false

        // build step
        fun timeout(ms: Int) = apply {
            this.timeoutMs = ms 
        }
        fun retries(count: Int) = apply {
            this.retries = count
        }
        fun withFallback(enable: Boolean) = apply {
            this.enableFallback = enable
        }

        // build method
        // => return instance
        fun build() = TimeoutConfig(
            timeoutMs, maxRetries, enableFallback
        )
    }
}

// create object
val config = TimeoutConfig.Builder()
                .timeout(5000)
                .retries(3)
                .withFallback(true)
                .build() 
```
> _Có thể dùng Default / Named Arguments để thay thế Builder Pattern trong nhiều trường hợp_

## **4. Observer Pattern**
> _Khi 1 object (subject) thay đổi trạng thái, tất cả các object đang theo dõi nó (observers) được thông báo tự động => quan hệ 1-N_ 

Mục đích: giảm phụ thuộc chéo, Subject không cần biết những ai lắng nghe nó, chỉ cần ném sự kiện ra khi có thay đổi.

```kotlin
interface Observer {
    fun update(message: String)
}

class Subscriber(private val name: String) : Observer {
    override fun update(message: String) {
        println("$name received: $message")
    }
}

class Channel {
    private val observers = mutableListOf<Observer>()

    fun subscribe(observer: Observer) {
        observers.add(observer)
    }

    fun notifyAllSubscribers(message: String) {
        observers.forEach { it.update(message) }
    }
}
```
> _Trong microservices, Observer Pattern -> kiến trúc `Event-Driven` (hướng sự kiện) => các service không giao tiếp thông qua REST API trực tiếp mà `publish` qua Kafka/RabbitMQ, service nào cần thì `subcribe` vào topic để lắng nghe._