package com.pm.paymentservice.service;

import com.pm.paymentservice.dto.PaymentIntentDto;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisService {

    private final RedisTemplate<String, PaymentIntentDto> redisTemplate;

    public void savePayment(PaymentIntentDto paymentIntentDto) {

        redisTemplate.opsForValue().set("payment:" + paymentIntentDto.bookingId(), paymentIntentDto, 10, TimeUnit.MINUTES);

    }

    public void deletePayment(UUID bookingId) {

        redisTemplate.delete("payment:" + bookingId);

    }

    public boolean doesExists(UUID bookingId) {

        return redisTemplate.hasKey("payment:" + bookingId);

    }

    public PaymentIntentDto getPayment(UUID bookingId) {

        return redisTemplate.opsForValue().getAndDelete("payment:" + bookingId);

    }
}
