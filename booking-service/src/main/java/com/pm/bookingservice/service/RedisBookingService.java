package com.pm.bookingservice.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisBookingService {

    private final RedisTemplate<String, List<UUID>> redisTemplate;


    public void saveBooking(UUID bookingId,UUID ticketId,UUID userId){
    redisTemplate.opsForValue().set("booking:"+bookingId,Arrays.asList(ticketId,userId),10, TimeUnit.MINUTES);

    }

    public void deleteBooking(UUID bookingId){

        redisTemplate.delete("booking:"+bookingId);

    }


}
