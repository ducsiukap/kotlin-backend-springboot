# `Lombok` 

_`Lombok` là công cụ hỗ trợ giảm **`boileplate`** - code lặp - khi viết backend bằng **Spring Boot** & **Java**_
### **Annotation**
- `@Getter`, `@Setter`: tự sinh `get`/`set` cho tất cả các field.
- **Constructor**: 
    - `@NoArgConstructor`
    - `@AllArgConstructor`
    - `@RequeireArgsConstructor`: chỉ tạo constructor cho các biến được đánh dấu `final` hoặc annotaion `@NonNull`
- `@Data`: `@Getter` + `@Setter` + `@RequireArgConstructor` + `@ToString` + `@EqualsAndHashCode` + 
- `@Builder`: áp dụng **builder pattern**
  ```java
  User.builder().name="vduczz"
    .age(21).build();
  ```
- `@Value`: gần giống `@Data` nhưng để tạo **Immutable Object
