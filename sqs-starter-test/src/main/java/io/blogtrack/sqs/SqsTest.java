package io.blogtrack.sqs;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use this annotation for tests that need to connect to an SQS queue. It spins up a local SQS environment with localstack
 * and creates queues with the given names fresh for each test.
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = "sqs")
@Tag("SqsTest")
@ActiveProfiles("test")
@ExtendWith(CreateQueueExtension.class)
public @interface SqsTest {

    /**
     * The names of the SQS queues that the tests needs to interact with. These queues will be created in a @BeforeEach
     * method for each test. These queues will then be available to interact with via the URL pattern
     * http://localhost:4576/queue/{QUEUE_NAME}.
     */
    String[] queueNames() default "";

}
