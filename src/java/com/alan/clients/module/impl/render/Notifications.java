package com.alan.clients.module.impl.render;


import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.noti.Notification;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.utils.Animation;
import com.alan.clients.noti.utils.Direction;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.NumberValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ModuleInfo(name = "module.render.notifications.name", category = Category.RENDER, description = "Better visual")
public class Notifications extends Module {
    private final NumberValue time = new NumberValue("Residence Time",this, 2, 1, 10, .5);
    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Default"))
            .add(new SubMode("Exhibition"))
            .add(new SubMode("Lavender"))
            .setDefault("Default");
    public final BooleanValue clientColor = new BooleanValue("ClientColor", this, true);
    public final BooleanValue vertical = new BooleanValue("Vertical", this, false, () -> !clientColor.getValue());

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        //draw not
        NORMAL_POST_RENDER_RUNNABLES.add(this::drawNotifications);
        NORMAL_BLUR_RUNNABLES.add(() -> {
            drawNotificationsEffects(Color.BLACK);
        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> drawNotificationsEffects(getTheme().getDropShadow()));
    };
    public int offsetValue = 0;

    public void drawNotifications() {
        float yOffset = 0;
        int notificationHeight = 0;
        int notificationWidth;
        int actualOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        NotificationManager.setToggleTime(time.getValue().floatValue());
        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }
            float x, y;

            switch (this.mode.getValue().getName()) {
                case "Default":
                    animation.setDuration(200);
                     actualOffset = 3;
                     notificationHeight = 23;
                    notificationWidth = FontManager.arial20.getStringWidth(notification.getDescription()) + 10;
                    x = (float) ((double) sr.getScaledWidth() - (double) (notificationWidth + 5));
                    y = (float) sr.getScaledHeight() - (yOffset + 18.0f + (float) this.offsetValue + (float) notificationHeight + 15.0f);
                    RenderUtil.scaleStart(x + notificationWidth / 2F, y + notificationHeight / 2F, (float) animation.getOutput());

                    notification.drawLettuce(x, y, notificationWidth, notificationHeight);

                    RenderUtil.scaleEnd();
                    break;
                case "Lavender":
                    animation.setDuration(200);
                    actualOffset = 3;
                    notificationHeight = 26;
                    notificationWidth = com.alan.clients.util.font.FontManager.getProductSansBold(16).width(notification.getDescription()) + 25;
                    x = (float) ((float) (double) sr.getScaledWidth() - ((double) (notificationWidth + 5) * (float) animation.getOutput()));
                    y = (float) sr.getScaledHeight() - (yOffset + 18.0f + (float) this.offsetValue + (float) notificationHeight + 15.0f);

                    notification.drawLavender(x, y, notificationWidth, notificationHeight);
                    break;
                case "Exhibition":
                    animation.setDuration(125);
                    actualOffset = 3;
                    notificationHeight = 25;
                    notificationWidth = Math.max(FontManager.arial18.getStringWidth(notification.getTitle()), FontManager.arial15.getStringWidth(notification.getDescription())) + 30;

                    x = sr.getScaledWidth() - ((sr.getScaledWidth() / 2f + notificationWidth / 2f) * (float) animation.getOutput());
                    y = sr.getScaledHeight() / 2f - notificationHeight / 2f + 40 + yOffset;

                    notification.drawExhi(x, y, notificationWidth, notificationHeight);
                    break;
            }
            yOffset = (float) ((double) yOffset + (double) (notificationHeight + actualOffset) * animation.getOutput());
        }
    }

    public void drawNotificationsEffects(Color color) {
        ScaledResolution sr = new ScaledResolution(mc);
        float yOffset = 0.0f;
        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }
            animation.setDuration(200);
            int actualOffset = 3;
            int notificationHeight = 23;
            int notificationWidth = FontManager.arial20.getStringWidth(notification.getDescription()) + 10;
            switch (this.mode.getValue().getName()) {
                case "Default":
                    float x = (float) ((double) sr.getScaledWidth() - (double) (notificationWidth + 5));
                    float y = (float) sr.getScaledHeight() - (yOffset + 18.0f + (float) this.offsetValue + (float) notificationHeight + 15.0f);
                    RenderUtil.scaleStart(x + notificationWidth / 2F, y + notificationHeight / 2F, (float) animation.getOutput());
                    notification.blurLettuce(x, y, notificationWidth, notificationHeight, color);
                    RenderUtil.scaleEnd();
                    break;
                case "Lavender":
                    animation.setDuration(200);
                    notificationHeight = 26;
                    notificationWidth = com.alan.clients.util.font.FontManager.getProductSansBold(16).width(notification.getDescription()) + 25;
                    x = (float) ((float) (double) sr.getScaledWidth() - ((double) (notificationWidth + 5) * (float) animation.getOutput()));
                    y = (float) sr.getScaledHeight() - (yOffset + 18.0f + (float) this.offsetValue + (float) notificationHeight + 15.0f);

                    notification.blurLavender(x, y, notificationWidth, notificationHeight,color);
                    break;
            }
            yOffset = (float) ((double) yOffset + (double) (notificationHeight + actualOffset) * animation.getOutput());
        }
    }


}
