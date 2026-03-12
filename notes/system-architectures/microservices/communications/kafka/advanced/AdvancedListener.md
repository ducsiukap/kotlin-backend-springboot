# **Listener _Advanced_**

## **`1.` About `@Listener`**

### **_`1.1.`_ Các _tham số_ của `@KafkaListener`**:

- `topics`: danh sách các topic mà bean này listen // **"topic" or {"topic-A", "topic-B", ...}**
- `topicPattern`: Subscribe topic theo **regex**.
- `groupId`: consumer group // **consumer cùng groupId → load balancing**
- `containerFactory`: chỉ định `KafkaListenerContainerFactory` / `ConcurrentKafkaListenerContainerFactory` // **Cần phải config trước**  
   sử dụng theo tên bean: _**containerFactory = "kafkaListenerContainerFactory"**_
- `errorHandler`: chỉ định error handler, cần config tương tự `containerFactory`
- `id`, `clientIdPrefix`
- `concurrency`: số consumer thread song song
- `properties`: các tham số nâng cao
- `containerGroup`
- `autoStartup`

### **1.2. `Args đầu vào` hàm xử lý của `@KafkaListener`**:

- `@Payload String payload`: message body (mặc định, nếu hàm chỉ khai báo 1 tham số -> payload)
- `@Header String header`: message header field

  ```java
  // headers
  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
  @Header(KafkaHeaders.OFFSET) long offset,
  @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
  // custom header ...
  @Headers Map<String, Object> allHeaders // all-header
  ```

- `ConsumerRecord<String, String> record`: Toàn bộ record (toàn bộ message, bao gồm header, payload, ...)
- `Acknowledge ack`: Dành cho `ack-mode: MANUAL / MANUAL_IMMEDIATE` hoặc muốn ACK thủ công.

  ```java
  @KafkaListener(topics = "orders", groupId = "order-processor")
  public void consumeManualAck(
          ConsumerRecord<String, OrderEvent> record, // message
          Acknowledgment ack // ACK
  ) {
      try {
          // processOrder(record.value()); // business
          ack.acknowledge(); // Commit offset SAU khi xử lý thành công
      } catch (RetryableException e) {

          // Không acknowledge → message sẽ được retry
          log.warn("Retryable error, will retry: {}", e.getMessage());
              // có thể dẫn tới infinite retry
      } catch (Exception e) {

          // log.error("Fatal error processing order", e);
          ack.acknowledge(); // Vẫn acknowledge để không bị stuck
          // Sau đó xử lý riêng (dead letter queue, alert, v.v.)
      }
  }
  ```

- `List<ConsumerRecord<String, String>> records`: dành cho `ack-mode: BATCH`

  ```java
  // cần config kafka listener container factory riêng với batchListener=true
  @KafkaListener(
      topics = "orders",
      groupId = "batch-processor",
      containerFactory = "batchKafkaListenerContainerFactory"
  )
  public void consumeBatch(
      // xử lý theo batch
      List<ConsumerRecord<String, String>> records,
      // manual ack
      Acknowledgment acknowledgment
  ) {
      log.info("Processing batch of {} messages", records.size());

      for (ConsumerRecord<String, String> record : records) {
          try {
              // processOrder(record.value());
          } catch (Exception e) {
              // Trong batch, cần quyết định: bỏ qua lỗi hay dừng cả batch?
              log.error("Error at offset {}: {}", record.offset(), e.getMessage());
          }
      }

      // Commit sau khi xử lý xong toàn bộ batch
      acknowledgment.acknowledge();
  }
  ```
