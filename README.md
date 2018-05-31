# REST 기반의 간단한 분산 트랜잭션 구현
* [REST 기반의 간단한 분산 트랜잭션 구현 – 1편](https://www.popit.kr/rest-%EA%B8%B0%EB%B0%98%EC%9D%98-%EA%B0%84%EB%8B%A8%ED%95%9C-%EB%B6%84%EC%82%B0-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EA%B5%AC%ED%98%84-1%ED%8E%B8/)


## System Requirements
* Java 8
* Apache Maven 3.2+
* Apache Kafka 1.1

## Getting Start

### Apache Kafka 
* 설치
```
https://kafka.apache.org/downloads 에서 1.1.0 을 다운로드 맟 입축 해제
```

* 실행
```
${kafka-home}/bin/zookeeper-server-start.sh ../config/zookeeper.properties
${kafka-home}/bin/kafka-server-start.sh ../config/server.properties 
```

* Topic 생성
```
${kafka-home}/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic stock-adjustment
${kafka-home}/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic payment-order

```

### Spring 마이크로 서비스
* Stock Service 실행
```
{PRODUCT_HOME}/stock-service > mvn clean spring-boot:run
```

* Payment Service 실행
```
{PRODUCT_HOME}/payment-service > mvn clean spring-boot:run
```

### Test
```
{PRODUCT_HOME}/order-service > mvn clean test
```