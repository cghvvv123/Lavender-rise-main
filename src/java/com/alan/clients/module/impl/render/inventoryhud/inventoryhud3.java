package com.alan.clients.module.impl.render.inventoryhud;

import com.alan.clients.Client;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.module.impl.render.InventoryHud;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

public class inventoryhud3 extends Mode<InventoryHud> {
    public inventoryhud3(String name, InventoryHud parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, true);
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
    private InventoryHud InventoryHud;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        final InventoryHud InventoryHud =getModule(InventoryHud.class);
        if (this.InventoryHud == null) {
            this.InventoryHud = this.getModule(InventoryHud.class);
        }
        Vector2d position = InventoryHud.position;

        final String titleString = interfaceModule.Render3cn.getValue() && showTitle.getValue() ? Localization.get("背包显示") : showTitle.getValue() ? Localization.get("InventoryHud") : "";

        final double Y = position.y;
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            final double textX = position.x + 5.3F;
            final double textY = Y + scale.y / 2.0F - nunitoNormal.height()/ 4.0F - 1F;
            boolean hasStacks = false;
            InventoryHud.positionValue.setScale(new Vector2d(scale.x + 147, scale.y + 52));
            if(InventoryHud.rendertitle()){
                FontManager.getProductSansLight(20).drawString("InventoryHud", textX-4, textY-17.5,new Color(255,255,255,200).getRGB());
            }
            if ((double)interfaceModule.radius.getValue() == 0f)
                RenderUtil.rectangle(position.x, Y, scale.x + 147, scale.y + 52, interfaceModule.getRENDER3BG2Color());
            else
                RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52,
                        (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BG2Color());

            bg(position.x, Y, scale.x + 147, scale.y + 52,15, (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BgColor());

            if(interfaceModule.Render3cn.getValue())
                com.alan.clients.fontRender.FontManager.arial17bold.drawString(titleString,
                        (float) (textX+ FontManager.geticn(19).width("D")+1),
                        (float) (textY-2.4), interfaceModule.getRENDER3fontColor().getRGB());else
            FontManager.getNunitoBold(20).drawString(titleString, textX+ FontManager.geticn(19).width("D")+1, textY-2,
                    interfaceModule.getRENDER3fontColor().getRGB());


            FontManager.geticn(19).drawString("D", textX-1, textY-1.5,interfaceModule.getRENDER3fontColor().getRGB());

            if (!(mc.currentScreen instanceof GuiInventory)) {
                for (int i1 = 9; i1 < mc.thePlayer.inventoryContainer.inventorySlots.size() - 9; ++i1) {
                    GL11.glPushMatrix();
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    if (mc.theWorld != null) {
                        RenderHelper.enableGUIStandardItemLighting();
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.disableAlpha();
                    GlStateManager.clear(256);
                    Slot slot = mc.thePlayer.inventoryContainer.inventorySlots.get(i1);
                    if (slot.getHasStack()) hasStacks = true;
                    int i = slot.xDisplayPosition;
                    int j = slot.yDisplayPosition;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), position.x + i - 4, Y + j - 66);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, slot.getStack(), (int) position.x + i - 4, (int) position.y + j - 66, null);

                    GlStateManager.disableBlend();
                    GlStateManager.scale(0.5d, 0.5d, 0.5d);
                    GlStateManager.disableDepth();
                    GlStateManager.disableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.scale(2.0f, 2.0f, 2.0f);
                    GlStateManager.enableAlpha();
                    GlStateManager.popMatrix();
                    GL11.glPopMatrix();
                }
            }

            if (mc.currentScreen instanceof GuiInventory) {
                FontManager.getNunitoBold(18).drawString("Already in inventory", (int) position.x + (double) 167 / 2 - (double) FontManager.getTahoma(18).width("Already in inventory") / 2, (int) Y + (double) 80 / 2, new Color(255, 255, 255, 180).getRGB());
            } else if (!hasStacks) {
                FontManager.getNunitoBold(18).drawString("Empty...", (int) position.x + (double) 167 / 2 - (double) FontManager.getTahoma(18).width("Empty...") / 2, (int) Y + (double) 80 / 2, new Color(255, 255, 255, 180).getRGB());
            }
        });

        NORMAL_BLUR_RUNNABLES.add(() -> {
            if((double)interfaceModule.radius.getValue() == 0f){
                RenderUtil.rectangle(position.x, Y, scale.x + 147, scale.y + 52, Color.BLACK);
            }else {
                RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52, (double)interfaceModule.radius.getValue(),Color.BLACK);
            }
        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            if(!interfaceModule.BLOOM.getValue()) return;
            if((double)interfaceModule.radius.getValue() == 0f){
                RenderUtil.rectangle(position.x, Y, scale.x + 147, scale.y + 52, interfaceModule.getbloomBG2Color());
            }else {
                RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52,
                        (double)interfaceModule.radius.getValue(),interfaceModule.getbloomBG2Color());
            }
        });
    };

    private void bg(double x, double y, double width,  double height,double height2, double radius, Color color) {
        if (isNull()) return;
        StencilUtil.initStencilToWrite();
        RenderUtil.roundedRectangle(x, y, width, height,
                radius,color);
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        RenderUtil.rectangle(x, y, width, height2,color);
        StencilUtil.uninitStencilBuffer();
    }
    private Color getClientColor () {
        Color theme1 = this.getTheme().getFirstColor();
        return new Color(theme1.getRGB());

    }
    private Color getAlternateClientColor () {
        Color theme2 = this.getTheme().getSecondColor();
        return new Color(theme2.getRGB());
    }
    public Color[] getClientColors () {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}
