package com.alan.clients.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class GLUtil {

    public static void enableDepth() {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }

    public static void disableDepth() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }
    public static int getMouseX() {
        return Mouse.getX() * getScreenWidth() / Minecraft.getMinecraft().displayWidth;
    }

    public static int getMouseY() {
        return getScreenHeight() - Mouse.getY() * getScreenHeight() / Minecraft.getMinecraft().displayWidth - 1;
    }
    public static int getScreenWidth() {
        return Minecraft.getMinecraft().displayWidth / getScaleFactor();
    }
    public static int getScaleFactor() {
        int scaleFactor = 1;
        final boolean isUnicode = Minecraft.getMinecraft().isUnicode();
        int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;
        if (guiScale == 0) {
            guiScale = 1000;
        }
        while (scaleFactor < guiScale && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        if (isUnicode && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor;
        }
        return scaleFactor;
    }
    public static int getScreenHeight() {
        return Minecraft.getMinecraft().displayHeight / getScaleFactor();
    }

    public static int[] enabledCaps = new int[32];

    public static void enableCaps(int... caps) {
        for (int cap : caps) glEnable(cap);
        enabledCaps = caps;
    }

    public static void disableCaps() {
        for (int cap : enabledCaps) glDisable(cap);
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void setup2DRendering(boolean blend) {
        if (blend) {
            startBlend();
        }
        GlStateManager.disableTexture2D();
    }

    public static void setup2DRendering() {
        setup2DRendering(true);
    }

    public static void end2DRendering() {
        GlStateManager.enableTexture2D();
        endBlend();
    }

    public static void startRotate(float x, float y, float rotate) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate(rotate, 0, 0, -1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void endRotate(){
        GlStateManager.popMatrix();
    }


}
