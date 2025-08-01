package com.streaker;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class TestContainerConfig {

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
            .withDatabaseName("streakertest")
            .withUsername("postgrestest")
            .withPassword("postgrestest");

    @Container
    public static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    static {
        redisContainer.start();
        postgresContainer.start();

        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", String.valueOf(redisContainer.getMappedPort(6379)));

        System.out.println("➡ Redis host = " + System.getProperty("spring.redis.host"));
        System.out.println("➡ Redis port = " + System.getProperty("spring.redis.port"));
    }

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

}
