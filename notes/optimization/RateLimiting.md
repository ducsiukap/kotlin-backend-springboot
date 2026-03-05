# `Rate` **_Limiting_**

## Rate Limiting & problem

> **Rate Limiting** là kỹ thuật **_kiểm soát số lượng request_** mà **một client được phép gửi đến server trong 1 khoảng thời gian nhất định**

Server nhận ra client thông qua:

- IP
- UserID
- API Key

Nếu client **_vượt qua số lượt request giới hạn_**, thì **server từ chối phục vụ** và phản hồi ngay về client lỗi `429 Too Many Requests`

#### **Advantages of _Rate Limiting_**:

- chống **DDoS** / **Spam Attack**: giúp server tránh rơi vào tình trạng quá tải, treo do thiếu RAM hoặc crash
- chống **Brute-force Attack**
- chống bị cào sạch dữ liệu - **Web Scraping** -> giảm tốc độ cào của attacker.
- chống **Noisy Neighbour**
- chống **Cloud Billing** // chi phí phải trả cho băng
  thông & thời gian CPU khi server chạy trên AWS, Google Cloud, ...

## Implement **_Rate Limit_** using `Redis` + `Lecttuce` + `Bucket4j`

### **1. dependencies** : [build.gradle.kts](/codes/mini-project/build.gradle.kts)

```kotlin
// Thư viện Rate Limiting lõi
implementation("com.bucket4j:bucket4j_jdk17-core:8.16.1")
// Thư viện giúp Bucket4j nói chuyện được với Redis (qua Lettuce)
implementation("com.bucket4j:bucket4j_jdk17-lettuce:8.16.1")
```

### **2. Bucket4j-Redis config**: [/config/Bucket4jRedisConfiguration.jt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/Bucket4jRedisConfiguration.kt)

### **3. Add Bucket filter**: [/core/security/RateLimitFilter.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/RateLimitFilter.kt)
