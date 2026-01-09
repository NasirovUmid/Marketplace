package com.pm.notificationservice.service;

import com.pm.commonevents.BookingNotificationEvent;
import com.pm.commonevents.CatalogNotificationEvent;
import com.pm.notificationservice.entity.Notification;
import com.pm.commonevents.UserNotificationEvent;
import com.pm.notificationservice.enums.NotificationEventStatus;
import com.pm.notificationservice.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void saveUser(UserNotificationEvent userNotificationEvent){


        // I get string but we need enums and our enum class include many of them (generalized) so I compare its string type to find necessary enum
        // otherwise i would have to create a lot of enums for each so i just aggregated them
        // I cannot compare with ORDINAL OF ENUMS
        /*
        NotificationEventStatus notificationEventStatus = NotificationEventStatus.getNotificationEventStatuses().
                stream().filter(status -> status.name().equals(userNotificationEvent.status())).findFirst().get();

                THE ONE ABOVE IS MORE SIMPLIFIED and i leave method untouched(getNotificationEventStatuses) in case
*/

        try {
            NotificationEventStatus notificationEventStatus = NotificationEventStatus.valueOf(userNotificationEvent.status());

            Notification notification = notificationRepository.save(
                    Notification.builder()
                            .userId(userNotificationEvent.userId())
                            .catalogId(null)
                            .catalogName(null)
                            .bookingId(null)
                            .userEmail(userNotificationEvent.userEmail())
                            .phoneNumber(userNotificationEvent.phoneNumber())
                            .event(notificationEventStatus)
                            .time(userNotificationEvent.time())
                            .build());

            emailSenderService.sendEmail(notification.getUserEmail(), "Account Created", "your profile was fully created");

        } catch (Exception e){

            logger.error("Fucked up = [ {} ]",userNotificationEvent);

        }
    }

    public void saveBooking(BookingNotificationEvent bookingNotificationEvent){

        /*
        NotificationEventStatus notificationEventStatus = NotificationEventStatus.getNotificationEventStatuses()
                .stream().filter(status -> status.name().equals(bookingNotificationEvent.status())).findFirst().get();
*/

        try {

            NotificationEventStatus notificationEventStatus = NotificationEventStatus.valueOf(bookingNotificationEvent.status());

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(bookingNotificationEvent.userId())
                        .catalogId(bookingNotificationEvent.catalogId())
                        .catalogName(bookingNotificationEvent.catalogName())
                        .bookingId(bookingNotificationEvent.bookingId())
                        .userEmail(bookingNotificationEvent.email())
                        .event(notificationEventStatus)
                        .time(bookingNotificationEvent.time())
                        .build()
        );

        emailSenderService.sendEmail(
                notification.getUserEmail(), "Booking Status",
                "Your booking of course: "+notification.getCatalogName()+" = "+notification.getEvent().name().substring(8)+" at "+notification.getTime());
        }catch (Exception e){

            logger.error("booking is canceled = [ {} ]",bookingNotificationEvent);
            emailSenderService.sendEmail(bookingNotificationEvent.email(),
                    "Booking Status"," Your booking of "+bookingNotificationEvent.catalogName().toUpperCase()+" is cancelled");
        }
    }

    public void saveCatalog(CatalogNotificationEvent catalogNotificationEvent){

        /*

        NotificationEventStatus notificationEventStatus = NotificationEventStatus.getNotificationEventStatuses()
                .stream().filter(status -> status.name().equals(catalogNotificationEvent.catalogStatus())).findFirst().get();
*/
        try {

        NotificationEventStatus notificationEventStatus = NotificationEventStatus.valueOf(catalogNotificationEvent.catalogStatus());

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .userId(catalogNotificationEvent.creatorId())
                        .catalogId(catalogNotificationEvent.catalogId())
                        .catalogName(catalogNotificationEvent.catalogName())
                        .event(notificationEventStatus)
                        .time(catalogNotificationEvent.createdAt())
                        .build()
        );

        emailSenderService.sendEmail("umidbeknosirov832@gmail.com","NEW CATALOG CREATED","you need to inform every user about new catalog");

        }catch (Exception e){

            logger.error("Problem with Saving catalog = [ {} ]",catalogNotificationEvent);
            emailSenderService.sendEmail("umidbeknosirov832@gmail.com","FAILED TO SEND","problem with creation of catalog and sending");

        }
    }

}
