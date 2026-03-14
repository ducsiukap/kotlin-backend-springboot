# **Kafka _Implementation_**

Điều quan trọng nhất cần nhớ là **_Kafka không biết gì về JSON, Java, hay kiểu dữ liệu_**. Kafka chỉ lưu trữ và truyền tải `byte[]` (mảng byte thuần). Vì vậy, toàn bộ câu chuyện **JSON messaging** thực chất là câu chuyện về `serialization` (Java object → bytes khi gửi) và `deserialization` (bytes → object khi nhận)

Phụ thuộc vào việc **`Serializer`**/**`Deserializer`**, có một số cách triển khai:

- Sử dụng **Header** để dánh dấu `event-type`:
  - Nội bộ các service Spring, ta có thể dùng trực tiếp `__TypeId__` được tự sinh ra.
  - Để giao tiếp với các ngôn ngữ khác, cần **quy định tên chung** của header để xác định, có thể **override** `__TypeMapper__` hoặc tạo field mới, giả sử `event-type`

  Chi tiết: [Xem chi tiết](./type-headers/README.md)

- Sử dụng `Avro`/**Schema Registry**

---

**Notes**:
All `Jackson 2` classes now have `Jackson 3` **counterparts with consistent naming and improved type safety**:

- `JsonKafkaHeaderMapper` replaces `DefaultKafkaHeaderMapper`
- `JacksonJsonSerializer`/`JacksonJsonDeserializer` replaces `JsonSerializer`/`JsonDeserializer`
- `JacksonJsonSerde` replaces `JsonSerde`
- `JacksonJsonMessageConverter` family replaces `JsonMessageConverter` family
- `JacksonProjectingMessageConverter` replaces `ProjectingMessageConverter`
- `DefaultJacksonJavaTypeMapper` replaces `DefaultJackson2JavaTypeMapper`
