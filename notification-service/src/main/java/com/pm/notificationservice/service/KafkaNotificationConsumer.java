package com.pm.notificationservice.service;

import com.pm.commonevents.BookingNotificationEvent;
import com.pm.commonevents.CatalogNotificationEvent;
import com.pm.commonevents.UserNotificationEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaNotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.created",groupId = "user-notification")
    public void userCreated(UserNotificationEvent userNotificationEvent){

         notificationService.saveUser(userNotificationEvent);

    }

    @KafkaListener(topics = "booking",groupId = "booking-notification")
    public void bookingNotification(BookingNotificationEvent bookingNotificationEvent){

        notificationService.saveBooking(bookingNotificationEvent);

    }

    @KafkaListener(topics = "catalog",groupId = "catalog-notification")
    public void catalogNotification(CatalogNotificationEvent catalogNotificationEvent){

        notificationService.saveCatalog(catalogNotificationEvent);

    }


}
