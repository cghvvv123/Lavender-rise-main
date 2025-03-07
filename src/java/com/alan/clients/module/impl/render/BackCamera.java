package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.value.impl.DragValue;
import com.alan.clients.value.impl.NumberValue;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(name = "BackCamera", description = "Show back", category = Category.RENDER)
public class BackCamera extends Module {


    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    public final NumberValue fov = new NumberValue("FOV", this, 70, 30, 110, 1);

    public final NumberValue scaleX = new NumberValue("scaleX", this, 70, 0, 400, 1);
    public final NumberValue scaleY = new NumberValue("scaleY", this, 70, 0, 400, 1);


    private boolean vaild;
    public int mirrorFBO;
    public int mirrorTex;
    public int mirrorDepth;

    public RenderGlobal global;

    public RenderGlobal tempGlobal;

    public static boolean silentRender = false;

    public static BackCamera INSTANCE;

    @Override
    protected void onEnable() {
        vaild = false;
    }

    public BackCamera(){
        INSTANCE = this;

        mirrorFBO = ARBFramebufferObject.glGenFramebuffers();
        mirrorTex = GL11.glGenTextures();
        mirrorDepth = GL11.glGenTextures();

        update();
        global = new RenderGlobal(InstanceAccess.mc);
    }

    private int width,height = 0;

    private void update(){
        if (InstanceAccess.mc.displayWidth != width || InstanceAccess.mc.displayHeight != height) {
            width = InstanceAccess.mc.displayWidth;
            height = InstanceAccess.mc.displayHeight;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGBA, GL11.GL_INT,
                    (java.nio.IntBuffer) null);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT,
                    GL11.GL_INT, (java.nio.IntBuffer) null);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

    }
    int frameCount = 0;

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {


        this.position.setScale(new Vector2d(scaleX.getValue().doubleValue(),scaleY.getValue().doubleValue()));

        if(frameCount ++ %2 == 0 && scaleX.getValue().intValue() != 0 && scaleY.getValue().intValue() != 0){
            update();
            updateBackCamera(event.getPartialTicks());
        }

        RenderUtil.color(Color.WHITE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        drawQuad(position.position.x,position.position.y,position.scale.x,position.scale.y);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,0);


    };

    public static void drawQuad(final double x, final double y, final double width, final double height) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public void updateGlobal(){
        if(tempGlobal == null){
            tempGlobal = InstanceAccess.mc.renderGlobal;
        }else if(tempGlobal.getWorld() != global.getWorld()){
            global.setWorldAndLoadRenderers(tempGlobal.getWorld());
        }

        InstanceAccess.mc.renderGlobal = tempGlobal;
    }

    public void endGlobal(){
        if(tempGlobal != null){
            InstanceAccess.mc.renderGlobal = tempGlobal;
            tempGlobal = null;
        }
    }


    public void updateBackCamera(float tick){
        silentRender = true;

        int width = InstanceAccess.mc.displayWidth;
        int height = InstanceAccess.mc.displayHeight;
        MovingObjectPosition mouseOver;
        float y, py, p, pp,fov;
        y = InstanceAccess.mc.getRenderViewEntity().rotationYaw;
        py = InstanceAccess.mc.getRenderViewEntity().prevRotationYaw;
        p = InstanceAccess.mc.getRenderViewEntity().rotationPitch;
        pp = InstanceAccess.mc.getRenderViewEntity().prevRotationPitch;
        int view;
        view = InstanceAccess.mc.gameSettings.thirdPersonView;
        mouseOver = InstanceAccess.mc.objectMouseOver;
        fov = InstanceAccess.mc.gameSettings.fovSetting;

        InstanceAccess.mc.gameSettings.thirdPersonView = 0;
        InstanceAccess.mc.gameSettings.limitFramerate = 0;
        InstanceAccess.mc.getRenderViewEntity().rotationYaw += 180;
        InstanceAccess.mc.getRenderViewEntity().prevRotationYaw += 180;
        InstanceAccess.mc.getRenderViewEntity().rotationPitch = -p + 18;
        InstanceAccess.mc.getRenderViewEntity().prevRotationPitch = -pp + 18;
        InstanceAccess.mc.gameSettings.fovSetting = this.fov.getValue().floatValue();

        switchToFB();

        updateGlobal();



        GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_ENABLE_BIT |
                GL11.GL_CURRENT_BIT | GL11.GL_POLYGON_BIT |
                GL11.GL_TEXTURE_BIT);



        InstanceAccess.mc.entityRenderer.updateCameraAndRender(tick,System.nanoTime());


        GL11.glPopAttrib();

        endGlobal();

        switchFromFB();

        InstanceAccess.mc.objectMouseOver = mouseOver;
        InstanceAccess.mc.getRenderViewEntity().rotationYaw = y;
        InstanceAccess.mc.getRenderViewEntity().prevRotationYaw = py;
        InstanceAccess.mc.getRenderViewEntity().rotationPitch = p;
        InstanceAccess.mc.getRenderViewEntity().prevRotationPitch = pp;
        InstanceAccess.mc.gameSettings.thirdPersonView = view;
        InstanceAccess.mc.gameSettings.fovSetting = fov;

        InstanceAccess.mc.displayWidth = width;
        InstanceAccess.mc.displayHeight = height;
        silentRender = false;

    }


    //切换缓冲
    private void switchToFB() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, mirrorFBO);
        ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER,
                ARBFramebufferObject.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                mirrorTex, 0);
        ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER,
                ARBFramebufferObject.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                mirrorDepth, 0);
    }


    private void switchFromFB() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
    }
}
