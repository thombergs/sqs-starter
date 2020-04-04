![CI/CD](https://github.com/thombergs/sqs-starter/workflows/CI/CD/badge.svg?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.reflectoring/sqs-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.reflectoring/sqs-starter)

# sqs-starter
A Spring Boot starter to send and consume messages to / from an AWS SQS queue in a robust manner.

Note that this library *has not been battle-tested in production, yet* (at least not by me).

* [Installation](#installation)
* [Publishing Messages to SQS](#publishing-messages-to-sqs)
* [Consuming Messages from SQS](#consuming-messages-from-sqs)
* [Local Testing](#local-testing)

## Installation

Include [the dependency](https://maven-badges.herokuapp.com/maven-central/io.reflectoring/sqs-starter) in your build file.

If your Spring Boot application uses `@EnableAutoConfiguration` (which is the default) the SQS starter will be loaded into the Spring application context automatically.

## Publishing Messages to SQS

To send messages to SQS, extend `SqsMessagePublisher`:

```java
class TestMessagePublisher extends SqsMessagePublisher<TestMessage> {

  TestMessagePublisher(AmazonSQS sqsClient, ObjectMapper objectMapper) {
  super("http://localhost:4576/queue/testMessages", sqsClient, objectMapper);
  }

}
```

Send messages by calling the `publish()` method:
```java
TestMessagePublisher messagePublisher = new TestMessagePublisher(...);
messagePublisher.publish(new TestMessage("this is a test message"));
```

The publisher will *automatically retry* up to 3 times with a short exponential backoff if SQS returns an error. You can also pass your own [RetryRegistry](https://github.com/resilience4j/resilience4j/blob/master/resilience4j-retry/src/main/java/io/github/resilience4j/retry/RetryRegistry.java) into the constructor to customize the retry behavior.

## Consuming Messages from SQS

For consuming messages, the SQS starter lets you configure:

* a message poller, which polls messages from SQS at a regular interval, and
* a thread pool of message handlers, which process the messages received by the poller.

*The starter assumes that all messages from an SQS queue are handled by the same message handler.*

First, implement the `SqsMessageHandler` interface:

```java
class TestMessageHandler implements SqsMessageHandler<TestMessage> {

  @Override
  public void handle(TestMessage message) {
    // handle the message
  }

  @Override
  public Class<TestMessage> messageType() {
    return TestMessage.class;
  }

}
```

Then, register a bean of type `SqsMessageHandlerRegistration` in the Spring application context:

```java
@Component
class TestMessageHandlerRegistration implements SqsMessageHandlerRegistration<TestMessage> {

  private final AmazonSQS client;
  private final ObjectMapper objectMapper;
  private final TestMessageHandler messageHandler;

  public TestMessageHandlerRegistration(
      AmazonSQS client, 
      ObjectMapper objectMapper, 
      TestMessageHandler messageHandler) {
    this.client = client;
    this.objectMapper = objectMapper;
    this.messageHandler = messageHandler;
  }

  @Override
  public SqsMessageHandler<TestMessage> messageHandler() {
    return this.messageHandler;
  }

  @Override
  public String name() {
    return "testMessageHandler";
  }

  @Override
  public SqsMessageHandlerProperties messageHandlerProperties() {
    return new SqsMessageHandlerProperties();
  }

  @Override
  public SqsMessagePollerProperties messagePollerProperties() {
    return new SqsMessagePollerProperties("http://localhost:4576/queue/testMessages");
  }

  @Override
  public AmazonSQS sqsClient() {
    return this.client;
  }

  @Override
  public ObjectMapper objectMapper() {
    return this.objectMapper;
  }
}
```

The SQS starter will set up a poller for each `SqsMessageHandlerRegistration` bean it finds in the Spring application context.

You can configure the behavior of the poller (like the waiting intervall between polls) in `SqsMessagePollerProperties`.

You can configure the message handler thread pool in `SqsMessageHandlerProperties`. 

## Local Testing

If you're using [JUnit Jupiter](https://github.com/junit-team/junit5) for testing, you can use the `@SQSTest` annotation provided by the [sqs-starter-test](https://maven-badges.herokuapp.com/maven-central/io.reflectoring/sqs-starter-test) module to easily create local tests against an SQS queue:

```java
@SqsTest(queueNames = "testMessages")
@SpringBootTest
class SendAndReceiveIntegrationTest {

  @Autowired
  private AmazonSQS sqsClient;

  @Autowired
  private TestMessageHandler messageHandler;

  @Autowired
  private TestMessagePublisher messagePublisher;

  @Test
  void sendAndReceive() {
    messagePublisher.publish(new TestMessage("message 1"));

    await().atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(messageHandler.getCount()).isEqualTo(1));
  }

}
```

The `@SQSTest` annotation makes use of [localstack](https://github.com/localstack/localstack) to start up a Docker container with a mock SQS server and it will automatically create the specified queues for you.
