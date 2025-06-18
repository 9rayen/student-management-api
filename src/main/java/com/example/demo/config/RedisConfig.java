package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for JWT Token Management
 * Configures Redis connection and RedisTemplate for centralized JWT storage
 */
@Configuration
public class RedisConfig {
    
    /**
     * Configure Redis Connection Factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Use default localhost:6379 for development
        // In production, this should be configured via properties
        return new LettuceConnectionFactory("localhost", 6379);
    }
    
    /**
     * Configure RedisTemplate for String operations
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializers for both key and value
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        // Enable transaction support
        template.setEnableTransactionSupport(true);
        
        return template;
    }
}
