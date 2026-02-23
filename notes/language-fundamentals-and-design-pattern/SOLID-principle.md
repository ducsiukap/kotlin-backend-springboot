# `SOLID` principle
> _**SOLID** principle là `set of design principles` giúp code trở nên **clean** hơn. Cụ thể, trong `OOP`, SOLID là nguyên tắc thiết kế hệ thống có thể dễ dàng `maintain` và `scale`_
## **1. S - `Single Responsibility` Principle - SRP**
> _Mỗi class chỉ nên `có 1 và chỉ 1 lí do để thay đổi` -> tức class chỉ nên chịu trách nhiệm cho **1 loại thay đổi duy nhất**_

Cụ thể, _**một class chỉ chịu trách nhiệm cho 1 chức năng duy nhất => phục vụ `1 stakeholder (mục đích nghiệp vụ / nhiệm vụ)` duy nhất**_, điều này giúp class đơn giản và dễ bảo trì hơn.

> _Tránh viết tất cả logic vào trong 1 class -> `God class/ Spaghetti code`_

example
```kotlin
// User chịu trách nhiệm cho đối tượng User nghiệp vụ
class User {
    // thay đổi khi đối tượng User trong logic nghiệp vụ thay đổi
}

// UserRepository chịu trách nhiệm giao tiếp với DB
class UserRepository {
    // -> UserRepository chỉ thay đổi nếu
    // User trong DB đổi
    fun save(user: User): Boolean
}

// EmailService chịu trách nhiệm gửi mail
class EmailService {
    fun sendEmail(user: User)
}
```

## **2. O - `Open/Closed` Principle - OCP**
> _Open for **extension**, Closed for **modification** => thiết kế hệ thống có thể dễ dàng **mở rộng bằng cách thêm code mới** nhưng **không cần sửa code đã ổn định**_

- thêm chức năng -> tạo class mới
- nhiều if-else -> polymorphism

example
```kotlin
// hệ thống thanh toán đa nền tảng
interface PaymentMethod { fun pay(amount: Double) }

// thêm phương thức thanh toán mới -> tạo class mới
// => polymorphism
class VNPayPayment : PaymentMethod {
    override fun pay(amount: Double) { 
        //...
    }
}
class MomoPayment : PaymentMethod {
    override fun pay(amount: Double) { 
        //...
    }
}
```

## **3. L - `Liskov Substitution` Principle - LSP**
> _LSP : sub-class phải `thay thế được` super-class mà không làm hỏng tính đúng đắn của chương trình (sai hành vi)_ 

Cụ thể, sub-class phải làm được tất cả những gì super-class có thể làm (methods). 

> **_Nếu B extends A -> ` mọi nơi ` dùng A, ta có thể dùng B mà chương trình vẫn chạy đúng logic_**

example
```kotlin
interface Animal {
    //...
}

interface FlyableAnimal {
    fun fly()
}

// Bird có thể bay -> kế thừa FlyableAnimal
class Bird : FlyableAnimal {
    override fun fly() {
        //...
    }
}

// Dog không thể bay -> không nên extends Bird/FlyableAnimal
// -> extends Animal
class Dog : Animal {
    // ...
}
```

## **4. I - `Interface Segregation` Principle**
> _ISP : thay vì dùng God interface (tương tự God class) với quá nhiều method, nên chia nhỏ thành nhiều interface cụ thể_

Mục đích, không ép client (sub-class) implementation (phụ thuộc vào) các method mà nó không dùng

example
```kotlin
// Cách sai
interface Worker {
    fun eat() // không phải tất cả Worker có thể eat
    fun work() 
}
class Robot : Worker {
    // robot không thể eat!
    public void eat() {
        throw new UnsupportedOperationException();
    }
}


// Cách đúng
interface Eatable {
    fun eat()
}
interface Workable {
    fun work()
}
// Human có thể (phụ thuộc) work và cần eat 
class Human : Eatable, Workable {
    // ... 
}
// Robot can work only
class Robot: Workable {
    // ...
}
```

## **5. D - `Dependency Inversion` Principle - DIP**
Rules:
- Module cấp cao không được phụ thuộc (gọi trực tiếp) vào module cấp thấp, **cả 2 phải phụ thuộc vào Abstraction (interface)**
    ```kotlin
    // abstract interface
    interface Database {
        fun save()
    }

    // implementation
    // => module cấp thấp
    class MySQLDatabase : Database {
        override fun save()
    }
    class MongoDatabase : Database {
        override fun save()
    }

    // usage / service 
    // => module cấp cao
    class UserService( 
        private db: Database // ok
    ) {
        // private db = MongoDatabase() // sai
        fun saveSmth() {
            db.save()
        }
    } 
    ```
- Abstraction không phụ thuộc chi tiết (implementations), chi tiết phụ thuộc abstraction. 
    ```text
    // Business chỉ biết abstract interface Database (làm được gì?)
    // không cần biết chính xác ai làm? / làm như nào? (class implementation)

        Service -> Database (abstract interface)

    // Các class implementation
    
        MySQLDatabase : Database
        MongoDatabase : Database
    ```
    => DIP
    ```text
        [High-level] -> Interface <- [Low-level]
    ```
> _DIP là nền tảng của `Dependency Injection - DI` của Spring Framework_