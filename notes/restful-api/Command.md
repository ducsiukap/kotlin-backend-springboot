# `Command` layer

> _`Command` là lớp nằm giữa `DTO` và `Entity`, giúp **ẩn DTO (web)** với `service` layer trong quá trình **Request** -> **controller** -> (dto -> command) -> **service** -> repository (entity)_

`Command` class hầu hết là giống DTO, khi này:

- `.toEntity()` được chuyển từ dto -> command
- dto bổ sung `.toCommand()`

details: [UserRequestCommand.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/command/UserCommand.kt)
