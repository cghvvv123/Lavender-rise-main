package com.alan.clients.module.impl.render;

import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


@ModuleInfo(name = "Clock", description = "Show time", category = Category.RENDER)
public class Clock extends Module {
    private final com.alan.clients.util.font.Font fontRenderer40 = FontManager.getProductSansRegular(40);
    private final com.alan.clients.util.font.Font fontRenderer20 = FontManager.getProductSansRegular(20);
    private final RapeMasterFontManager fontRenderer15 = com.alan.clients.fontRender.FontManager.arial14;

    private final Font fontRenderer12 = FontManager.getProductSansRegular(12);

    public final NumberValue x = new NumberValue("x", this, 0, -100, 100, 1);
    public final NumberValue y = new NumberValue("y", this, 0, -100, 100, 1);

    private final double sin45 = Math.sqrt(2)/2;
    private final DragValue position = new DragValue("Position", this, new Vector2d(50, 50));


    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        Vector2d position = this.position.position;

        this.position.setScale(new Vector2d(70,65));
        InstanceAccess.NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            RenderUtil.roundedRectangle(position.x, position.y, this.position.scale.x, this.position.scale.y, 4f, getTheme().getBackgroundShade());

            double centerX = position.x + this.position.scale.x/2;
            double centerY = position.y + this.position.scale.y/2;

            for(int i = 0 ; i < 360 ; i+=15){

                if(i>105 && i <165){
                    continue;
                }

                if(i>195 && i <255){
                    continue;
                }

                if(i>285 && i <345){
                    continue;
                }

                GL11.glPushMatrix();
                GL11.glTranslated(centerX, centerY,0);
                GL11.glRotatef(i,0,0,1);
                RenderUtil.rectangle(-0.5,-25,1,1, Color.WHITE);
                GL11.glPopMatrix();
            }

            fontRenderer40.drawCenteredString(getMinute(),centerX+25*sin45,centerY+25*sin45-fontRenderer40.height()/2.4,Color.WHITE.getRGB());
            fontRenderer40.drawCenteredString(getHour(),centerX-25*sin45,centerY-25*sin45-fontRenderer40.height()/2.4,Color.WHITE.getRGB());
            fontRenderer20.drawString(getDayOfWeek(),centerX-26,centerY+10,Color.WHITE.getRGB());
            fontRenderer12.drawString(getDate(),centerX-26,centerY+20,Color.WHITE.getRGB());
            fontRenderer15.drawString(getAMPM(), (float) (centerX+18.6f), (float) (centerY-26),Color.WHITE.getRGB());

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            //时针
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glPushMatrix();
            GL11.glTranslated(centerX, centerY,0);
            GL11.glRotatef(getIntHour()*30+getIntMinute()/60f*30,0,0,1);
            RenderUtil.rectangle(-0.5,-25,1,25, new Color(10,155,120));
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

        });
        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() ->  RenderUtil.roundedRectangle(position.x, position.y, this.position.scale.x, this.position.scale.y, 4f, Color.BLACK));
        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() ->  RenderUtil.roundedRectangle(position.x, position.y, this.position.scale.x, this.position.scale.y, 4f + 1, getTheme().getDropShadow()));

    };


    public String getHour(){
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        if(hour>12){
            hour-=12;
        }
        return String.format("%02d",hour);
    }

    public int getIntHour(){
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        if(hour>12){
            hour-=12;
        }
        return hour;
    }

    public String getMinute(){
        LocalTime currentTime = LocalTime.now();
        return String.format("%02d",currentTime.getMinute());
    }

    public int getIntMinute(){
        LocalTime currentTime = LocalTime.now();
        return currentTime.getMinute();
    }

    public String getDayOfWeek(){
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().name().substring(0,3).toLowerCase();
    }

    public String getDate(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return today.format(formatter);
    }


    public String getAMPM(){
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");
        return currentTime.format(formatter).toLowerCase();
    }

}
