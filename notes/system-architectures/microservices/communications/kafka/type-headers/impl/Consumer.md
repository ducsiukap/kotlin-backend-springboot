# **Comsumer _`Type mappings`_ _DDD_ implementation**

## **Mapping Types**:

- Với `StringDeserialize`, ta cần manual mapping tại hàm `@KafkaListener` bằng cách sử dụng `@Header('x-event-type')` + `switch-case` trong hàm consume.
- Với `JsonDeserialize`, ta có thể config TypeMapper để mapping đúng `alias` (giá trị được lưu trong `x-event-type`/`__TypeId__` của header) với `Class` tương ứng, sau đó dùng trực tiếp Class đó để làm tham số đầu vào của hàm consume.

## **Manual Mapping** - `StringDeserialize`

```kotlin
@Component
class UserEventsKafkaListener(
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(
        topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
        groupId = KafkaConsumeConstants.GROUP_ID,
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consume(
        // lấy header chứa event's type
        @Header("__TypeId__") typeId: String?,
        @Header("event_type") eventType: String?,
        @Payload payload: String
    ) {

        val type = eventType ?: typeId
        when (type) { // mapping header đó với class tương ứng và gọi hàm xử lý
            KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1 -> {
                val obj = objectMapper.readValue(payload, WelcomeMailRequest::class.java);
                println("message: $obj")

                // gọi hàm xử lý
            }

            KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V2 -> {
                val obj = objectMapper.readValue(payload, WelcomeMailRequest::class.java);
                println("message: $obj")

            }

            else -> println("Unhandled event type $eventType")
        }
    }
}
```

## **TypeMapping** -> `JsonDeserialize`

### Sử dụng `@KafkaListener` cho method:

```kotlin
@KafkaListener(
    topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
    groupId = KafkaConsumeConstants.GROUP_ID,
)
fun handleUserCreateEvent(
    @Payload
    request: WelcomeMailRequest, // JSON parse chính xác -> match

) {
    // notificationService.sendWelcomeMail(request);
    println("receive event: $request")
}
```

**Tuy nhiên**: khi `Deserialize Error` (Parse Error, invalid JSON type, no-maching class, ...) -> `lỗi` + `retry` (có thể dẫn tới `infinite-loop`)

### Sử dụng `@KafkaLister` cho class và khai báo **DefaultHanlder - `@KafkaHandler(isDefault = true)`** xử lý trường hợp `no-maching class`:

```kotlin
@Component
// Multiple message-type in one topic
@KafkaListener(
    topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
    groupId = KafkaConsumeConstants.GROUP_ID,
)
class UserEventListener(
    private val notificationService: NotificationService
) {

    // ____________________ @Payload Matching Specific Type ____________________ //
    @KafkaHandler // handle 1 loại message type
    fun handleUserCreateEvent(
        @Payload
        request: WelcomeMailRequest, // JSON parse chính xác -> match
    ) {
        // notificationService.sendWelcomeMail(request);
        println("receive event: $request")
    }

    // ____________________ Default Handler ____________________ //
    // khi JSON parse không match request nào
    // bắt buộc hứng để tránh lỗi
    //  => sử dụng cho các message mà nó không quan tâm
    @KafkaHandler(isDefault = true)
    fun handleUnknowMessage(
        @Payload
        payload: Any, // payload only

        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.OFFSET) offset: Long,

        // full-message
        message: ConsumerRecord<String, Any>
    ) {
        println("[Default Handler] receive event: $message ")
    }
}
```

#### **`@KafkaListener`**

- Listen theo 1 topic:
  ```java
  @KafkaListener(
    topics = "orders",
    groupId = "order-processor"
  )
  ```
- Listen nhiều topic cùng lúc:
  ```java
  @KafkaListener(t
      topics = {"orders.created", "orders.updated", "orders.cancelled"}
      groupId = "order-processor"
  )
  ```
- Listen từ topic theo pattern:
  ```java
  @KafkaListener(
      topicPattern = "orders\\..*",
      groupId = "order-processer"
  )
  ```
