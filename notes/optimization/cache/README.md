# **_Caching_** & `Redis` caching

### _**Caching**_

**Vấn đề**: (giả sử)

- server có **API** `/api/v1/users/me` để lấy profile người dùng
- mỗi lần chạy App/Web cần gọi **API** này để lấy profile.

> _+ Lấy data từ DB tốn thời gian hơn so với lấy data từ RAM/memory_
>
> _+ Giả sử có 1.000.000 users vào cùng lúc -> MySQL phải thực thi 1.000.000 lệnh `SELECT * FROM users ...` => **Out-of-memory**_

**Tư duy _`Caching`_**:

- **Lần đầu tiên** - `Cache Miss`: lấy data từ DB, sau đó:
  - lưu vào cache
  - trả về cho client
- **Lần thứ 2 tới thứ N** - `Cache Hit`: data có sẵn ở cache -> bỏ qua DB, lấy từ cache và trả về cho client.
- **Khi có update tới data**:
  - lưu vào DB.
  - lưu data mới vào cache.

### _**Implement caching using `Redis`**_

**`Step 1`**: build `Redis` server,
có thể sử dụng:

- Redis Cloud: [cloud.redis.io](https://cloud.redis.io/)
- using Docker

  > requires to install Docker

  open `cmd` and run this command:

  ```cmd
  docker run -d --name ksb-redis -p 6379:6379 redis:latest
  ```

  trong đó:
  - `-d`: chạy background
  - `--name ksb-redis`: container's name
  - `-p 6379:6379`: map port (localhost:6379 ) vào Redis container
  - `redis:latest`: latest Redis
    > => download Redis và cho chạy ngầm ở localhost:6379

  to check, run:

  ```cmd
  docker ps
  ```

  exaple result:

  ```cmd
  CONTAINER ID   IMAGE          COMMAND                  CREATED          STATUS          PORTS                                         NAMES
  10e867b3cc56   redis:latest   "docker-entrypoint.s…"   32 seconds ago   Up 31 seconds   0.0.0.0:6379->6379/tcp, [::]:6379->6379/tcp   ksb-redis
  ```

**`Step 2`**: Dependencies & Configuration

- dependencies:

  ```kotlin
  // dependencis

  // Spring Cache Abstraction
  implementation("org.springframework.boot:spring-boot-starter-cache")
  // Spring Data Redis
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  ```

- config:

  ```properties
  # configuration

  # Redis & Cache
  spring.cache.type=redis
  spring.data.redis.host=localhost
  spring.data.redis.port=6379
  # Cache TTL (ms) -> 1h = 3600000ms
  spring.cache.redis.time-to-live=3600000
  ```

**`Step 3`**: Redis config

details: [/config/RedisConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/RedisConfig.kt)

**`Step 4`**: Annotations

- `@Cacheable`:

  ```kotlin
  @Cacheable(value=["users"], key="#id")
  //    + value: tên cache (#id, #user.id -> tham số / id bên trong tham số của hàm)
  //    + key: key trong Redis
  //    + condition: điều kiện để cache -> true thì cache (filter)
  //    + unless : không cache nếu true (loại trừ)
  fun fetchDataFromDB(id: UUID) {
      //...
  }
  // + `cache-miss` -> chạy method + lưu Redis
  // + `cache-hit` -> trả từ Redis
  ```

- `@CachePut`: update cache
  ```kotlin
  @CachePut(value=["users"], key="#user.id")
  fun updateUser(user: User): User {
      // ...
  }
  ```
- `@CacheEvict`: xóa cache

  ```kotlin
  @CacheEvict(value = ["users"], key = "#id") // xóa 1 theo key

  @CacheEvict(value = ["users"], allEntites = true) // xóa toàn bộ cache
  ```

- `@Caching`: kết hợp

  ```kotlin
  @Caching(
    evict = [
        CacheEvict(value = ["users"], key = "#user.id")
    ],
    // ...
  )
  ```

  details: [/service/impl/v1/UserServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/UserServiceImpl.kt)

  **Notes**: Redis cache là `global cache`, không phải cache riêng cho từng request => cần chọn `key` hợp lí
