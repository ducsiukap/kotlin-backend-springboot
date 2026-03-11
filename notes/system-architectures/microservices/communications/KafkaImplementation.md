# `Kafka` _Implementation_

## **`1.` _Docker container_ setup for _Kafka_ broker**

Tạo [monorepo-microservice-example/**docker-compose.yml**](/codes/monorepo-microservice-example/docker-compose.yml) (root project, nơi chứa cả 2 services).

```yaml
services:
  kafka:
    image: apache/kafka:latest
    ports:
      - "9092:9092"
    environment:
      # KRaft
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller # KRaft: vừa là broker, vừa là controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_LISTENERS: INTERNAL://0.0.0.0:29092,EXTERNAL://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:29092,EXTERNAL://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      # cho phép tự tạo topic khi có người publish mà chưa khai báo trước
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true" # nên đặt là false
    volumes:
      - kafka-data:/var/lib/kafka/data

volumes:
  kafka-data:
```

Để chạy container, thực hiện chạy lệnh:

```cmd
docker compose up -d
```

---

## **`2.` _Producer_** side: [Kafka Producer Implementation](./KafkaProducer.md)

## **`3.` _Consumer_** side: [Kafka Consumer Implementation](./KafkaConsumer.md)
