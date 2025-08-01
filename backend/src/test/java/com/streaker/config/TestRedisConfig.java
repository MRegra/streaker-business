package com.streaker.config;

import com.streaker.TestContainerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class TestRedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = TestContainerConfig.redisContainer.getHost();
        Integer port = TestContainerConfig.redisContainer.getMappedPort(6379);

        System.out.println("🔧 Redis Factory initialized with host: " + host + ", port: " + port);
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
