package com.pm.paymentservice.service;

import com.pm.commonevents.PaymentEvent;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisService {

    private final RedisTemplate<String, PaymentEvent> redisTemplate;

    public void savePayment(PaymentEvent paymentEvent){

        redisTemplate.opsForValue().set("payment:"+paymentEvent.bookingId(),paymentEvent,10, TimeUnit.MINUTES);

    }

    public void deletePayment(UUID bookingId){

        redisTemplate.delete("payment:"+bookingId);

    }

    public boolean doesExists(UUID bookingId){

        return redisTemplate.hasKey("payment:"+bookingId);

    }

    public PaymentEvent getPayment(UUID bookingId){

        return redisTemplate.opsForValue().getAndDelete("payment:"+bookingId);

    }
}
