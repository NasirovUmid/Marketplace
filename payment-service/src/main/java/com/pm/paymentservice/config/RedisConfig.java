package com.pm.paymentservice.config;

import com.pm.commonevents.PaymentEvent;
import com.pm.paymentservice.dto.PaymentIntentDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PaymentIntentDto> redisTemplate(RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String,PaymentIntentDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    RedisMessageListenerContainer container (RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter listenerAdapter){

        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(listenerAdapter,new PatternTopic("_keyevent@_:expired"));

        return redisMessageListenerContainer;

    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisExpirationListener expirationListener){

        return new MessageListenerAdapter(expirationListener);
    }

}
