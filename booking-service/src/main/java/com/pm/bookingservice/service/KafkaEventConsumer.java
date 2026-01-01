package com.pm.bookingservice.service;

import com.pm.bookingservice.entity.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventConsumer {

    // payment.{enum`s ordinal} 1 - SUCCESS , 2 - FAILED

    @KafkaListener(topics = "payment.1")
    public void successfulPaymentEventListener(PaymentEvent paymentEvent){



    }

    @KafkaListener(topics = "payment.2")
    public void failedPaymentEventListener(){




    }


}
