# `controller` Layer

> _`Controller` là nơi tiếp nhận yêu cầu - **Request** - từ phía `client`_

Tầng `controller` là tập các API - `endpoint` - của server, cho phép bên ngoài (**client** : Frontend, Mobile, 3rd, ... ) được phép giao tiếp với **server**

### **1. Naming for `controller` classes:**

- **prefix**: `Controller`
- **class**: số ít -> ex: `UserController`
- **url**: số nhiều -> ex: `/users`
- Với **API** mang tính **quản lí (Admin / CMS)** / **CRUD** cơ bản -> **đặt tên theo Entity**, ex: `UserController`
- Với các `end-user features` / `complex business-flow` / `multi-entity` -> **đặt tên theo Business**
  > ex: `AuthController`, `CheckoutController`, ...

### **2. Sub-router** configuration:

- custom `api-annotation` (**optional**):

  ```kotlin
  // core.annotation package


  // định nghĩa các API annotation class
  @Target(AnnotationTarget.CLASS) // annotation chỉ gán cho class
  @Retention(AnnotationRetention.RUNTIME) // annotation chỉ chạy lúc runtime
  @RestController // nhúng RestController vào annotation -> class nào có annot này thì auto là rest
  annotation class AppApiV1()

  @Target(AnnotaionTarget.CLASS)
  @Retention(AnnotaionRetention.RUNTIME)
  @RestController
  annotation class AdminApiV1()
  ```

- config `preifx-url`: [config/WebConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/WebConfig.kt)

### **3. Implementation**

details: [UserControler.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)

**3.1. No Boilerplate Code**

> _**No Boilerplate Code**: KHÔNG nên `try-catch` ở `controller`._

Khi 1 method `throw Exception`, nó sẽ :

- từ bất cứ đầu và **bubble up** (nhảy ngược) lên `controller`
- sau đó đẩy lên `DispatcherServlet` có khả năng:
  - **hứng Exception**
  - tự động trả về cho client **500 Internal Server Error**
    ```json
    {
      "timestamp": "2026-02-28T10:30:00.000+00:00",
      "status": 500,
      "error": "Internal Server Error",
      "trace": "java.lang.RuntimeException: User not found for id... (dài 100 dòng)",
      "message": "User not found",
      "path": "/api/v1/users/123"
    }
    ```
    > _có thể thay đổi message trả về nhờ `@RestControllerAdvice`_
- do mỗi `request` được xử lý bởi **1 thread trong Thread Pool** -> không crash toàn bộ server / ảnh hưởng thread khác. _**Đơn giản, khi có exception, nó tạm dừng thread đó vào trả về cho client, sau đó tiếp tục phục vụ client khác**_

**3.2. Annotation**:

- `@RestController` -> for RESTful API, = `@Controller` + `@ResponseBody`
- `@RequestMapping`:

  ```kotlin
  // Cổ điển
  @RequestMapping(value=["urls"], method=[RequestMethod.GET])
  class ...

  // Hiện tại
  @RequestMapping("url")
  class ...

  // ------------------------------------------------------------
  // @RequestMapping đặt trên class -> địa chỉ chung (prefix) cho toàn bộ method trong class

  // HTTP method Mapping -> gán trên hàm
  // (extends @RequestMapping)
        // @GetMapping
        // @PostMapping
        // @PutMapping
        // @PatchMapping
        // @DeleteMapping


  // ------------------------------------------------------------
  // @RequestMapping params:
  // - consumes: input-filter
  @PostMapping(
    value=["/avatars"],
    consumes = ["multipart/form-data"] // chỉ nhận file
  )
  fun uploadAvatar(@RequestParam("file") file: MultipartFile)
  // - produces: ép format đầu ra
  @GetMapping(
    value = ["/exports"],
    produces = ["application/pdf"] // ép output thành pdf
  )
  fun exportUserList(): ByteArray
  // - params
  @GetMapping(value = ["/special"], params = ["vip=true"]) // chỉ url dạng .../special?vip=true thì được nhận
  // - headers
  @GetMapping(value = ["/special"], headers = ["X-Device-Type=iOS"]) // chỉ request với header có chứa "X-Device-Type: iOS" mới được nhận
  ```

- **URL variable**:

  ```kotlin
  // @PathVariable -> định danh cụ thể trên url thông qua
  // "{variable}"
  @GetMapping("/users/{userId}")
  fun getUser(

    // lấy {userId} trong URL
    @PathVariable
    userId: UUID // variable trùng tên với variable trong URL

  )

  // @RequestParam -> phục vụ query/filter/pagination thông qua
  // "?key=value"
  @GetMapping
  fun getUsers(

    // ?role=...
    @RequestParam role: String,  // String -> required

    // ?page=...
    // optional -> nếu không truyền thì lấy default value
    @RequestParam(defaultValue = "1") page: Int,

    // ?search=...
    // nullable
    @RequestParam(required=false) search: String? // String? -> nullable
  )
  ```

- **Request data**:
  - `@RequestHeader`

    ```kotlin
    // @RequestHeader -> lấy Header của Request
    @GetMapping
    fun getProfile(
        // yêu cầu lấy "Authorization" trong Header
        @RequestHeader("Authorization") token: String // String -> required

        // ex: Accept-Language , User-Agent (client's device infor)
    )
    ```

  - `@RequestBody`

    ```kotlin
    // @RequestBody -> for POST / PUT / PATCH
    // -> chỉ request có content-type = JSON mới được nhận
    // dependencies: jackson-module-kotlin
    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserRequest // paste dto
    )
    ```

    **Note:**
    - một `function` chỉ có **DUY NHẤT MỘT** `@RequestParam`
    - `@GetMapping` không có `@RequestBody`
    - `@RequestBody` chỉ có nhiệm vụ **nhét data vào dto**, không có khả năng validation -> sử dụng `@Valid`

    **Hứng `data` từ `@RequestBody`**:

    ```kotlin
    // sử dụng dto class
    // chuẩn nhất, có @Valid
    fun ... (
        @RequestBody userDto: UserDto
    )




    // sử dụng entity -> vi phạm controller biết nghiệp vụ
    // Validation thủ công, or validation trong entity :)
    fun ... (
        @RequestBody user: User
    )

    // sử dụng Map<String, Any> -> PATCH
    fun ... (
        @RequestBody payload: Map<String, Any>
    ) {
        // using payload
        val name = payload["name"] as? String ?: throw Exception("name is required")
        // dễ crash nếu sai
        // ex: {"age": "18"} thay vi {"age": 18} -> crash
        val age = payload["age"] as? Int ?: 0

        // validate

        // map vào entity thủ công
        val user = User(...)
    }

    // raw JSON
    // JsonNode
    fun webhookReceive(
        @RequestBody rawNode: JsonNode
    ) {
        // lấy data:
        val email = rawNode.get("email")?.asText()
    }
    // String
    fun rawReceive(
        @RequestBody rawString: String
    ) {
        // ...
    }
    ```

**3.3. `ResponseEntity<T>`**

Spring có khả năng tự động chuyển thành JSON:

```kotlin
@GetMapping("/{id}")
fun getUser(@PathVariable id: UUID): UserDetailResponse {
    // ....

    return userService.getDetail(id).toResponse()
    // tự paste thành JSON + gắn 200 OK
}
```

nhưng, không chuẩn `RESTful API` => sử dụng `ResponseEntity`:

- **Component of `ResponseEntity`**
  - **Body** - response data => `ResponseDto`
  - **StatusCode**
  - **Headers** - thông tin đi kèm

- methods:

  ```kotlin
  // ------------------------------------------------------------
  // T trong ResponseEntity<T> -> kiểu dữ liệu của body (dto / List<dto>)
  //    ex: ResponseEntity<List<UserDetailResponse>> , ResponseEntity<UserDetailResponse>, ...
  // không có body (DELETE) -> T = Unit / T = Void

  // ------------------------------------------------------------
  // STATUS CODE + BODY

  // .ok()
  // -> 200 OK // GET, PUT, PATCH
  val data = userService.getDetailUser(id).toUserDetailsResponse() // dto
  return ResponseEntity.ok(data)

  // .status() -> statusCode
  ResponseEntity.status(HttpStatus.CREATED) // POST
  // .body()
  // response body
  val data = userService.createUser(request.toCommand()).toResponse()
  return ResponseEntity.status(HttpStatus.CREATED).body(data)
  // -> thường .status().body()

  // nothing to respone
  return ResponseEntity.noContent().build() // 204 NO CONTENT // DELETE only
  // or gọi thẳng status
  return ResponseEntity.badRequest().body() // kèm body
  return ResponseEntity.notFound().build()

  // tuân thủ:
  // Post OK -> 201 CREATED
  // Delete OK -> 204 NO CONTENT
  // còn lại OK -> 200 OK

  // ------------------------------------------------------------
  // HEADERS
  // .header() -> kẹp thêm header
  return ResponseEntity.ok()
    .header("X-Total-Count", totalCount.toString())
    .header("X-Powered-By", "vduczz")
    .body()
  ```
