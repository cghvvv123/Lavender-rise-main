package com.alan.clients.module.impl.render.targetinfo;

import com.alan.clients.module.impl.render.TargetInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;

import static com.alan.clients.util.animations.Easing.*;
public class DwTargetInfo extends Mode<TargetInfo> {

    private final Font productSansLight = FontManager.getProductSansLight(40);
    private final Font productSansMedium = FontManager.getProductSansMedium(40);
    public final NumberValue x = new NumberValue("x", this, 1, -500, 500, 0.5);
    public final NumberValue y = new NumberValue("y", this, 1, -500, 500, 0.5);


    private TargetInfo targetInfoModule;

    private float healthDiffer ;

    private Animation openingAnimation = new Animation(EASE_OUT_ELASTIC, 500);
    private Animation healthAnimation = new Animation(EASE_OUT_SINE, 500);

    public DwTargetInfo(String name, TargetInfo parent) {
        super(name, parent);
    }

    private float lastHealth;


    @Override
    public void onEnable() {
        healthDiffer = -1;
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (this.targetInfoModule == null) {
            this.targetInfoModule = this.getModule(TargetInfo.class);
        }

        Entity en = targetInfoModule.target;
        if(!(en instanceof AbstractClientPlayer)){
            return;
        }

        boolean out = !this.targetInfoModule.inWorld || this.targetInfoModule.stopwatch.finished(1000);

        //完成为-90 显示为 0
        openingAnimation.setDuration(out ? 1000 : 1100);
        openingAnimation.setEasing(out ? EASE_IN_BACK : EASE_OUT_ELASTIC);
        openingAnimation.run(out ? -90 : 0);

        if(openingAnimation.getValue() < -89 && out){
            openingAnimation.setValue(-90);
            openingAnimation.reset();
            return;
        }

        AbstractClientPlayer target = (AbstractClientPlayer) targetInfoModule.target;
        float width = 260f;
        float height = 100f;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GLU.gluPerspective(90f, (float) Display.getWidth()/(float)Display.getHeight(), 1, 1000);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        double hurtTime = (target.hurtTime == 0 ? 0 :
                target.hurtTime - mc.timer.renderPartialTicks) * 0.5;
        GL11.glTranslated(x.getValue().doubleValue(),y.getValue().doubleValue(), (double) Display.getHeight() /-2);
        GL11.glRotated(180, 1, 0, 0);
        GL11.glRotated(openingAnimation.getValue(), 0, 1, 0);


        if(openingAnimation.getValue()>-0.01){
            GL11.glRotated(-hurtTime*1.5f, 0, 0, 1);
        }

        RenderUtil.start();
        RenderUtil.roundedRectangle(-width/2,-height/2,width,height,5f,new Color(0,0,0,100));
        RenderUtil.stop();

        GlStateManager.disableCull();

        RenderUtil.color(ColorUtil.mixColors(Color.RED, Color.WHITE, hurtTime / 9f));
        renderTargetHead(target, -114+hurtTime*0.7f , -36+hurtTime*0.7f,
                60-hurtTime*1.4f);
        RenderUtil.color(Color.WHITE);


        com.alan.clients.fontRender.FontManager.arial40.drawString(target.getDisplayName().getUnformattedText(),-44,-30,Color.WHITE.getRGB());
        productSansMedium.drawString(target.getHealth()+"HP - "+(int)mc.thePlayer.getDistanceToEntity(target)+"M",-44,-2,Color.WHITE.getRGB());
        RenderUtil.roundedRectangle(-114,30,228,12,5f,new Color(100,100,100));


        //扣血
        if(lastHealth > target.getHealth()){
            healthDiffer = lastHealth - target.getHealth();
        }

        healthAnimation.run((target.getHealth()/target.getMaxHealth()));



        if(healthDiffer!= -1){
            //绘制渐变色长血条
            double percent = (healthAnimation.getValue()*target.getMaxHealth()-target.getHealth())/healthDiffer;
            if(percent<= 0.01f){
                healthDiffer = -1;
                percent = 0f;
            }

            if(percent>= 0.99f){
                percent = 1f;
            }

            RenderUtil.roundedRectangle(-114,30,228*healthAnimation.getValue(),12,5f, ColorUtil.mixColors(new Color(17,137,227),Color.CYAN,-4*percent*percent+4*percent));
            //绘制蓝血条目标长度
            RenderUtil.roundedRectangle(-114,30,228*(target.getHealth()/target.getMaxHealth()),12,5f,new Color(17,137,227));
        }else{
            RenderUtil.roundedRectangle(-114,30,228*healthAnimation.getValue(),12,5f,new Color(17,137,227));
        }

        lastHealth = target.getHealth();
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    };

    private void renderTargetHead(final AbstractClientPlayer abstractClientPlayer, final double x, final double y, final double size) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        StencilUtil.initStencil();
        StencilUtil.bindWriteStencilBuffer();
        RenderUtil.roundedRectangle(x, y, size, size, this.getTheme().getRound() * 2, this.getTheme().getBackgroundShade());
        StencilUtil.bindReadStencilBuffer(1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.enableTexture2D();
        final ResourceLocation resourceLocation = targetInfoModule.inWorld && abstractClientPlayer.getHealth() > 0
                ? abstractClientPlayer.getLocationSkin() : RenderSkeleton.getEntityTexture();
        mc.getTextureManager().bindTexture(resourceLocation);
        Gui.drawScaledCustomSizeModalRect(x, y, 4, 4, 4, 4, size, size, 32, 32);
        GlStateManager.disableBlend();
        StencilUtil.uninitStencilBuffer();


    }


}
