package com.alan.clients.noti;

import com.alan.clients.fontRender.RapeMasterFontManager;

import java.awt.*;

public enum NotificationType {
    SUCCESS(new Color(20, 250, 90), RapeMasterFontManager.CHECKMARK),
    DISABLE(new Color(226,87,76), RapeMasterFontManager.XMARK),
    INFO(new Color(127,174,210), RapeMasterFontManager.INFO),
    WARNING(new Color(255,255,94), RapeMasterFontManager.WARNING);

    private final Color color;
    private final String icon;

    NotificationType(Color color, String icon) {
        this.color = color;
        this.icon = icon;
    }

    public Color getColor() {
        return this.color;
    }

    public String getIcon() {
        return this.icon;
    }
}

