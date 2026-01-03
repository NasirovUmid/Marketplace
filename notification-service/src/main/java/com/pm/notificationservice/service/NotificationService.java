package com.pm.notificationservice.service;

import com.pm.notificationservice.entity.BookingNotificationEvent;
import com.pm.notificationservice.entity.CatalogNotificationEvent;
import com.pm.notificationservice.entity.Notification;
import com.pm.notificationservice.entity.UserNotificationEvent;
import com.pm.notificationservice.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;

    public void saveUser(UserNotificationEvent userNotificationEvent){

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(userNotificationEvent.userId())
                        .userEmail(userNotificationEvent.userEmail())
                        .phoneNumber(userNotificationEvent.phoneNumber())
                        .event(userNotificationEvent.status())
                        .time(userNotificationEvent.time())
                .build());

        emailSenderService.sendEmail(notification.getUserEmail(),"Account Created","your profile was fully created");

    }

    public void saveBooking(BookingNotificationEvent bookingNotificationEvent){

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(bookingNotificationEvent.userId())
                        .catalogId(bookingNotificationEvent.catalogId())
                        .catalogName(bookingNotificationEvent.catalogName())
                        .bookingId(bookingNotificationEvent.bookingId())
                        .userEmail(bookingNotificationEvent.email())
                        .event(bookingNotificationEvent.status())
                        .time(bookingNotificationEvent.time())
                        .build()
        );

        emailSenderService.sendEmail(
                notification.getUserEmail(), "Booking Status",
                "Your booking of course: "+notification.getCatalogName()+" = "+notification.getEvent().name().substring(8)+" at "+notification.getTime());
    }

    public void saveCatalog(CatalogNotificationEvent catalogNotificationEvent){

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(catalogNotificationEvent.creatorId())
                        .catalogId(catalogNotificationEvent.catalogId())
                        .catalogName(catalogNotificationEvent.catalogName())
                        .event(catalogNotificationEvent.catalogStatus())
                        .time(catalogNotificationEvent.createdAt())
                        .build()
        );

        emailSenderService.sendEmail("admin","NEW CATALOG CREATED","you need to inform every user about new catalog");

    }

}
