package com.pm.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.List;
import java.util.UUID;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String , List<UUID>> redisTemplate(RedisConnectionFactory factory){

        RedisTemplate<String,List<UUID>> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();

        return template;

    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter){

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter,new PatternTopic("_keyevent@_:expired"));

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisExpirationListener expirationlistener){

        return new MessageListenerAdapter(expirationlistener);

    }



}
