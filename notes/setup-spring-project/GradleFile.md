## 1. build.gradle.kts

### **`plugins` : build tools**

> _khai báo các plugins giúp Gradle đóng gói ứng dụng_

```kotlin
// example
plugins {
    id("org.springframework.boot") version "3.2.x"
    id("io.spring.dependency-management") version "1.1.x"
    kotlin("jvm") version "1.9.x"
    kotlin("plugin.spring") version "1.9.x"
    kotlin("plugin.jpa") version "1.9.x"
}
```

### **Metadata**

> _project metadata_

```kotlin
// example
group = "com.your_name"
version = "0.0.1-SNAPSHOT"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

### **`repositories` - package manager**

> _tương tự `npmjs.com`, repositories block cho biết Gradle nên tải các thư viện từ đâu_

```kotlin
repositories {
    mavenCentral()
}
```

### **`dependencies` block**

> _khai báo thư viện mà project cần dùng, tương tự `dependencies`/`devDependencies`_

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") // spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // stater-data-jpa
    implementation("org.springframework.boot:spring-boot-starter-validation") // starter-validation
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // kotlin JSON

    // implementation
    //      thư viện bắt buộc phải có để chạy project
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // developmentOnly
    //      thư viện chỉ dùng trong dev
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // runtimeOnly
    //      code không gọi trực tiếp như cần phải có để chạy server
    runtimeOnly("com.mysql:mysql-connector-j")

    // testImplementation
    //      dùng trong src/test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

## 2. Gradle command

> _use `gradlew` or `./gradlew` (Gradle Wrapper)_

- run app: `./gradlew bootRun`
- build: `./gradlew build`
- delete build directory: `./gradlew clean`
- test: `./gradlew test`
- view dependency tree: `./gradlew dependencies`
- re-install dependencies: `./gradlew build --refresh-dependencies`
