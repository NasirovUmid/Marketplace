package com.pm.bookingservice.config;

import com.pm.bookingservice.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class RedisExpirationListener {

    private final BookingService bookingService;

    public void handleMessage(String key){

        if (key.startsWith("booking:")){

            String bookingId = key.replace("booking:","");

            bookingService.cancellingBooking(UUID.fromString(bookingId));

        }



    }

}
