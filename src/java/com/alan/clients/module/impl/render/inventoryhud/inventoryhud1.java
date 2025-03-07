package com.alan.clients.module.impl.render.inventoryhud;

import com.alan.clients.module.impl.render.InventoryHud;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
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

public class inventoryhud1 extends Mode<InventoryHud> {
    public inventoryhud1(String name, InventoryHud parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, true);
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);

    private InventoryHud InventoryHud;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final InventoryHud InventoryHud =getModule(InventoryHud.class);
        if (this.InventoryHud == null) {
            this.InventoryHud = this.getModule(InventoryHud.class);
        }
        Vector2d position = InventoryHud.position;

        final String titleString = showTitle.getValue() ? Localization.get("InventoryHud") : "";
        final float titleWidth = nunitoNormal.width(titleString);
        final double Y = position.y;
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            final double textX = position.x + 5.3F;
            final double textY = Y + scale.y / 2.0F - nunitoNormal.height()/ 4.0F - 1F;
            boolean hasStacks = false;
            InventoryHud.positionValue.setScale(new Vector2d(scale.x + 147, scale.y + 52));
            if(InventoryHud.rendertitle()){
                FontManager.getProductSansLight(20).drawString("InventoryHud", textX-4, textY-17,new Color(255,255,255,200).getRGB());
            }
            RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52, getTheme().getRound(), getTheme().getBackgroundShade());
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY, titleWidth, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                nunitoNormal.drawString(titleString, textX, textY, 0);
            });
            GL11.glPushMatrix();
            if (!(mc.currentScreen instanceof GuiInventory)) {
                for (int i1 = 9; i1 < mc.thePlayer.inventoryContainer.inventorySlots.size() - 9; ++i1) {
                    Slot slot = mc.thePlayer.inventoryContainer.inventorySlots.get(i1);
                    if (slot.getHasStack()) hasStacks = true;
                    int i = slot.xDisplayPosition;
                    int j = slot.yDisplayPosition;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), position.x + i - 4, Y + j - 68);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, slot.getStack(), (int) position.x + i - 4, (int) position.y + j - 68, null);
                }
            }
            if (mc.currentScreen instanceof GuiInventory) {
                FontManager.getTahoma(18).drawString("Already in inventory", (int) position.x + (double) 167 / 2 - (double) FontManager.getTahoma(18).width("Already in inventory") / 2, (int) Y + (double) 72 / 2, -1);
            } else if (!hasStacks) {
                FontManager.getTahoma(18).drawString("Empty...", (int) position.x + (double) 167 / 2 - (double) FontManager.getTahoma(18).width("Empty...") / 2, (int) Y + (double) 72 / 2, -1);
            }
            GL11.glPopMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
        });

        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52, getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, Y, scale.x + 147, scale.y + 52, getTheme().getRound() + 1, getTheme().getDropShadow()));
    };
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
