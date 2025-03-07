package com.alan.clients.noti;

import com.alan.clients.Client;
import com.alan.clients.module.impl.render.Notifications;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    @Getter
    @Setter
    private static float toggleTime = 2.0f;
    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList();

    public static void post(NotificationType type, String title, String description) {
        NotificationManager.post(new Notification(type, title, description));
    }
    public static void post(NotificationType type, String title, String description, float time) {
        NotificationManager.post(new Notification(type, title, description, time));
    }

    public static void post(Notification notification) {
        if (Client.INSTANCE.getModuleManager().get(Notifications.class).isEnabled()) {
            notifications.add(notification);
        }
    }
}

