package com.alan.clients.module.impl.render;

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
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@ModuleInfo(name = "Compass", description = "Show compass", category = Category.RENDER)
public class Compass extends Module {
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    private final com.alan.clients.util.font.Font fontRenderer10 = FontManager.getProductSansRegular(10);
    private final com.alan.clients.util.font.Font fontRenderer11 = FontManager.getProductSansRegular(11);
    private final Font fontRenderer12 = FontManager.getProductSansRegular(12);

    private static final List<String> degreeData = new ArrayList<>();

    static {
        degreeData.add("N");
        degreeData.add("NE");
        degreeData.add("E");
        degreeData.add("SE");
        degreeData.add("S");
        degreeData.add("SW");
        degreeData.add("W");
        degreeData.add("NW");
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        Vector2d position = this.position.position;
        float degree = MathHelper.wrapAngleTo180_float(InstanceAccess.mc.thePlayer.rotationYaw) + 180;
        InstanceAccess.NORMAL_POST_RENDER_RUNNABLES.add(() -> {

            //外观
            this.position.setScale(new Vector2d(350,35));
            RenderUtil.roundedRectangle(position.x, position.y, this.position.scale.x, this.position.scale.y, getTheme().getRound(), getTheme().getBackgroundShade());
            double center = position.x + this.position.scale.x/2;


            //内容区
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(position.x, position.y, this.position.scale.x, this.position.scale.y);

            int margin = 12;
            int count = (int)this.position.scale.x/2/margin;
            double start = position.x - (5-(this.position.scale.x/2 - count*margin - degree%5))*margin/5;
            double startDegree = wrapDegree(degree - degree%5 - 5*count);
            for(double i = start; i < this.position.scale.x+position.x+margin ; i+= margin){
                if(startDegree%15 == 0){
                    if(startDegree%45 == 0){
                        RenderUtil.rectangle(i, position.y + 7.9,1,7.2,new Color(111,111,111));
                        fontRenderer11.drawCenteredString(degreeData.get((int) (startDegree/45)),i, position.y+17,Color.WHITE.getRGB());
                    }else{
                        RenderUtil.rectangle(i, position.y + 10.5,1,2,Color.GRAY);
                        fontRenderer10.drawCenteredString(startDegree+"",i, position.y+17,Color.GRAY.getRGB());
                    }
                }else{
                    RenderUtil.rectangle(i, position.y + 10.5,1,2,Color.GRAY);
                }
                startDegree = wrapDegree(startDegree+5);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);



            //顶部
            RenderUtil.color(new Color(250,50,56));
            RenderUtil.start();
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            int size = 4;
            GL11.glVertex2d(center-size,position.y - 1);
            GL11.glVertex2d(center+size,position.y - 1);
            GL11.glVertex2d(center,position.y+size*Math.sqrt(3) - 1);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderUtil.rectangle(center - 0.5, position.y + 8,1,7,new Color(250,50,56));
            fontRenderer12.drawCenteredString(Math.round(degree)+"",center, position.y-6,Color.WHITE.getRGB());
        });

        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(()->{
            double center = position.x + this.position.scale.x/2;
            RenderUtil.color(Color.BLACK);
            RenderUtil.start();
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            int size = 4;
            GL11.glVertex2d(center-size,position.y - 1);
            GL11.glVertex2d(center+size,position.y - 1);
            GL11.glVertex2d(center,position.y+size*Math.sqrt(3) - 1);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            RenderUtil.rectangle(center - 0.5, position.y + 8,1,7,Color.BLACK);

        });
    };


    public static double wrapDegree(double degree){
        while (degree< 0){
            degree+=360;
        }
        while (degree>= 360){
            degree-=360;
        }
        return degree;
    }
}
