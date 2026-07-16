package com.lld.practice.tutor_sessions.hotel_reservation.model;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationTypeStatus;

import java.time.LocalDateTime;

public class Notification {

    private String id;
    private String guestId;
    private NotificationEvent notificationEvent;
    private String message;
    private NotificationTypeStatus notificationTypeStatus;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public Notification(String id, String guestId, NotificationEvent notificationEvent, String message, NotificationTypeStatus notificationTypeStatus, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.guestId = guestId;
        this.notificationEvent = notificationEvent;
        this.message = message;
        this.notificationTypeStatus = notificationTypeStatus;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public NotificationEvent getNotificationEvent() {
        return notificationEvent;
    }

    public void setNotificationEvent(NotificationEvent notificationEvent) {
        this.notificationEvent = notificationEvent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationTypeStatus getNotificationTypeStatus() {
        return notificationTypeStatus;
    }

    public void setNotificationTypeStatus(NotificationTypeStatus notificationTypeStatus) {
        this.notificationTypeStatus = notificationTypeStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", guestId='" + guestId + '\'' +
                ", notificationEvent=" + notificationEvent +
                ", message='" + message + '\'' +
                ", notificationTypeStatus=" + notificationTypeStatus +
                ", createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}
