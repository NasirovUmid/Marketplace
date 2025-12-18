package com.pm.authservice.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String ,String > redisTemplate;


    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void save(String id,String refreshToken,long ttl){

        redisTemplate.opsForValue().set("refresh:"+id,refreshToken,ttl, TimeUnit.DAYS);

    }

    public boolean exists(String id){

       return Boolean.TRUE.equals(redisTemplate.hasKey("refresh:"+id));

    }

    public void delete(String id){

        redisTemplate.delete("refresh:"+id);

    }

    public String getRefreshToken(String id){

        return redisTemplate.opsForValue().get("refresh:"+id);

    }




}
