package com.alan.clients.noti;


import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.impl.render.Notifications;
import com.alan.clients.noti.utils.AnimTimeUtil;
import com.alan.clients.noti.utils.Animation;
import com.alan.clients.noti.utils.DecelerateAnimation;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;
import static com.alan.clients.util.math.MathUtil.interpolateInt;
@Getter
public class Notification
        implements InstanceAccess {
    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final AnimTimeUtil timerUtil;
    private final Animation animation;
    final String titleString =  Localization.get("Notification") ;
    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000.0f);
        this.timerUtil = new AnimTimeUtil();
        this.notificationType = type;
        this.animation = new DecelerateAnimation(300, 1.0);
    }
    public static float radius = 7f;
    public boolean clientColor = Client.INSTANCE.getModuleManager().get(Notifications.class).clientColor.getValue();
    static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }
    static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public void drawLettuce(float x, float y, float width, float height) {
        Color color = this.applyOpacity(this.interpolateColorC(Color.BLACK, this.getNotificationType().getColor(), 0.65f), 70.0f);
        float percentage = Math.min((float) this.timerUtil.getTime() / this.getTime(), 1.0f);
        RenderUtil.roundedRectangle(x, y,  width, height, radius,getTheme().getBackgroundShade());
        boolean vertical = Client.INSTANCE.getModuleManager().get(Notifications.class).vertical.getValue();
        if (clientColor){
            RenderUtil.drawRoundedGradientRect(x,y,width * percentage,height,radius,getTheme().getFirstColor(),getTheme().getSecondColor(),vertical);
        }
        else
        {
            RenderUtil.roundedRectangle(x, y,  width * percentage, height,radius,color);
        }
        Color textColor = this.applyOpacity(Color.WHITE, 80.0f);
        FontManager.arial20.drawString(this.getDescription(), x + (this.notificationType == NotificationType.INFO ? 1.0f : 2.8f) + 2.0f, y + 6.0f, textColor.getRGB());
    }
    public void blurLettuce(float x, float y, float width, float height,Color color) {
        Color color_anim = this.applyOpacity(this.interpolateColorC(Color.BLACK, this.getNotificationType().getColor(), true ? 0.65f : 0.0f), 70.0f);
        float percentage = Math.min((float)this.timerUtil.getTime() / this.getTime(), 1.0f);
        RenderUtil.roundedRectangle(x, y, width,  height,radius,color);
        boolean vertical = Client.INSTANCE.getModuleManager().get(Notifications.class).vertical.getValue();
        if (clientColor){
            RenderUtil.drawRoundedGradientRect(x,y,width * percentage,height,radius,getTheme().getFirstColor(),getTheme().getSecondColor(),vertical);
        }
        else
        {
            RenderUtil.roundedRectangle(x, y,  width * percentage, height,radius,color_anim);
        }
        RenderUtil.resetColor();
    }
    public void drawExhi(float x, float y, float width, float height) {
        RenderUtil.rectangle(x, y, width, height, new Color(32, 32, 32,255));
        float percentage = Math.min((timerUtil.getTime() / getTime()), 1);
        RenderUtil.rectangle(x + (width * percentage), y + height - 1, width - (width * percentage), 1, getNotificationType().getColor());
        RenderUtil.drawImage2(new ResourceLocation("lavender/icons/exhi/" + getNotificationType().getIcon() + ".png"), x + 3, (y + FontManager.icon40.getMiddleOfBox(height) + 1f), 18,18,1);

        RapeMasterFontManager tahomaFont18 = FontManager.arial18;
        tahomaFont18.drawString(getTitle(), x + 25, y + 4, Color.WHITE.getRGB());
        FontManager.arial14bold.drawString(getDescription(), x + 25, y + 3.1f + tahomaFont18.getFontHeight(), Color.WHITE.getRGB());
    }
    public void drawLavender(float x, float y, float width, float height) {
        float percentage = Math.min((timerUtil.getTime() / getTime()), 1);
        int textColor = new Color(255, 255, 255, 170).getRGB();

        RenderUtil.rectangle(x, y,  width, height,getTheme().getBackgroundShade());
        RenderUtil.rectangle(x, y,  width, height,new Color(0, 0, 0,30));

        FontManager.arial15bold.drawString(this.getDescription(),
                x + (this.notificationType == NotificationType.INFO ? 1.0f : 2.8f) + 2.0f, y + 17.0f, textColor);
        RenderUtil.resetColor();
        GradientUtil.applyGradientHorizontal(x + (this.notificationType == NotificationType.INFO ? 1.0f : 2.8f) + 2.0f, y + 5.0f, com.alan.clients.util.font.FontManager.getNunitoBold(20).width(titleString), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
            RenderUtil.setAlphaLimit(0);
            com.alan.clients.util.font.FontManager.getNunitoBold(20).drawString(titleString,
                    x + (this.notificationType == NotificationType.INFO ? 1.0f : 2.8f) + 2.0f, y + 5.0f, 0);
        });
        RenderUtil.roundedRectangle(x, y+3.5, 2, 9,2,getClientColors()[0]);
        RenderUtil.circle(x + FontManager.arial15bold.getStringWidth(this.getDescription()) + 9,
                y+8, 12, 360-(360 * percentage), false,  Color.white);
      /*  com.alan.clients.util.font.FontManager.getProductSansBold(14).drawString(String.valueOf(percentage),
                x+com.alan.clients.util.font.FontManager.getNunitoBold(16).width(this.getDescription())+12, y+8, textColor);

       */
    }
    public void blurLavender(float x, float y, float width, float height,Color color) {
        RenderUtil.rectangle(x, y,  width, height,color);
    }
    private Color getClientColor () {
        Color theme1 = this.applyOpacity(this.getTheme().getFirstColor(), 100.0f);
        return new Color(theme1.getRGB());
    }
    private Color getAlternateClientColor () {
        Color theme2 = this.applyOpacity(this.getTheme().getSecondColor(), 100.0f);
        return new Color(theme2.getRGB());
    }
    public Color[] getClientColors () {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}

