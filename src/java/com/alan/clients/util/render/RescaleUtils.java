package com.alan.clients.util.render;

import com.alan.clients.util.interfaces.InstanceAccess;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class RescaleUtils implements InstanceAccess {
   public static void rescaleMC() {
      ScaledResolution resolution = new ScaledResolution(mc);
      rescale((double)(mc.displayWidth / resolution.getScaleFactor()), (double)(mc.displayHeight / resolution.getScaleFactor()));
   }

   public static void rescale(double factor) {
      rescale((double)mc.displayWidth / factor, (double)mc.displayHeight / factor);
   }

   public static void rescale(double width, double height) {
      GlStateManager.clear(256);
      GlStateManager.matrixMode(5889);
      GlStateManager.loadIdentity();
      GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
      GlStateManager.matrixMode(5888);
      GlStateManager.loadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
   }
}
