package com.pm.notificationservice.enums;

import java.util.Arrays;
import java.util.List;

public enum NotificationEventStatus {

    USER_CREATED,USER_UPDATED,BOOKING_CONFIRMED,BOOKING_CANCELED,CATALOG_CREATED;

    public static List<NotificationEventStatus> getNotificationEventStatuses(){

        return Arrays.asList(NotificationEventStatus.values());

    }

}
