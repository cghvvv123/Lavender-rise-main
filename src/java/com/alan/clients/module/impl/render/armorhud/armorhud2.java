package com.alan.clients.module.impl.render.armorhud;

import com.alan.clients.module.impl.render.ArmorHud;
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
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

public class armorhud2 extends Mode<ArmorHud> {
    public armorhud2(String name, ArmorHud parent) {
        super(name, parent);
    }
    private final ModeValue modeValue = new ModeValue("Alignment", this) {{
        add(new SubMode("Horizontal"));
        add(new SubMode("Vertical"));
        setDefault("Vertical");
    }};
    private final BooleanValue selfadaption = new BooleanValue("Self Adaption", this, true);

    private final BooleanValue showTitle = new BooleanValue("Title", this, true);

    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    int heightfix = 0;
    int Widthfix = 0;
    private float anmitY = 0.0f;
    private ArmorHud ArmorHud;

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final ArmorHud ArmorHud =getModule(ArmorHud.class);
        if (this.ArmorHud == null) {
            this.ArmorHud = this.getModule(ArmorHud.class);
        }
        anmitY = (float) RenderUtil.getAnimationState(anmitY,((this.modeValue.getValue().getName().equalsIgnoreCase("Vertical") && selfadaption.getValue() ? (int) (scale.y + heightfix) :63)), 90.0);
        Vector2d position = ArmorHud.position;
        final String titleString = showTitle.getValue() ? Localization.get("ui.armorhud.title") : "";
        final float titleWidth = nunitoNormal.width(titleString);
        int Width =(this.modeValue.getValue().getName().equalsIgnoreCase("Vertical") && selfadaption.getValue() ? (int) scale.x + 22 : (int)scale.x + Widthfix);
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            ArmorHud.positionValue.setScale(new Vector2d(scale.x + 22, scale.y + 25));
            final double textX = position.x + 5.3F;
            final double textY = position.y + scale.y / 2.0F - nunitoNormal.height() / 4.0F - 1F;
            int armorCount = 0;
            double rendery = textY;
            double renderx = textX;
            int i = 0;
            RenderItem renderItem = mc.getRenderItem();
            boolean isPlayerWearingArmor = false;

            for (int index = 3; index >= 0; index--) {
                ItemStack stack = mc.thePlayer.inventory.armorInventory[index];
                if (stack != null) {
                    isPlayerWearingArmor = true;
                    armorCount++;}
            }
            if(ArmorHud.rendertitle()){
                FontManager.getProductSansLight(20).drawString("ArmorHud", textX-4.5, textY-16.5,new Color(255,255,255,200).getRGB());
            }
            if (this.modeValue.getValue().getName().equalsIgnoreCase("Vertical")) {
                RenderUtil.rectangle(position.x, position.y, scale.x + 22,anmitY , getTheme().getBackgroundShade());
            }else {
                RenderUtil.rectangle(position.x, position.y, Width,41 , getTheme().getBackgroundShade());
            }
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY-2, titleWidth, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                FontManager.getNunitoBold(20).drawString(titleString, textX, textY-1, 0);
            });
            if(showTitle.getValue()) RenderUtil.roundedRectangle(position.x, position.y+3.5, 2,9.5 , 2, getClientColors()[0]);

            for (int index = 3; index >= 0; index--) {
                ItemStack stack = mc.thePlayer.inventory.armorInventory[index];
                if (stack != null) {
                    if (this.modeValue.getValue().getName().equalsIgnoreCase("Vertical")) {
                        FontManager.getNunitoBold(14).drawStringWithShadow(Integer.toString(stack.getMaxDamage() - stack.getItemDamage()),
                                textX + 18, rendery + 16, new Color(255, 255 ,255,170).getRGB());
                        renderItem.renderItemIntoGUI(stack, textX - 2, rendery + 10);
                    }else {
                        FontManager.getNunitoBold(13).drawStringWithShadow(Integer.toString(stack.getMaxDamage() - stack.getItemDamage()),
                                renderx+1, textY + 28, new Color(255, 255 ,255,170).getRGB());
                        renderItem.renderItemIntoGUI(stack, renderx - 2, textY + 9);

                    }
                    renderx += 17;
                    rendery += 17;
                    i++;
                }
            }
            if(!isPlayerWearingArmor){
                heightfix = 14;Widthfix =17;
                if (this.modeValue.getValue().getName().equalsIgnoreCase("Vertical")) {
                    FontManager.getNunitoBold(16).drawString("Empty..", textX, textY + 14, new Color(255, 255 ,255,180).getRGB());
                }else{
                    FontManager.getNunitoBold(16).drawString("Empty..", textX, textY + 18,  new Color(255, 255 ,255,180).getRGB());
                }
            }else if(armorCount == 0) {
                heightfix = 1;Widthfix =17;
            }else if(armorCount == 1) {
                heightfix = 13;Widthfix =17;
            }else  if(armorCount == 2) {
                heightfix = 30;Widthfix =19;
            }else  if(armorCount == 3) {
                heightfix = 47;Widthfix =35;
            }else  if(armorCount == 4) {
                heightfix = 64;Widthfix =53;
            }

        });

        NORMAL_BLUR_RUNNABLES.add(() -> {
            if (this.modeValue.getValue().getName().equalsIgnoreCase("Vertical")) {
                RenderUtil.rectangle(position.x, position.y, scale.x + 22,anmitY , Color.BLACK);
            }else {
                RenderUtil.rectangle(position.x, position.y, Width,41 , Color.BLACK);
            }
        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            if (this.modeValue.getValue().getName().equalsIgnoreCase("Vertical")) {
                RenderUtil.rectangle(position.x, position.y, scale.x + 22,anmitY , getTheme().getDropShadow());
            }else {
                RenderUtil.rectangle(position.x, position.y, Width,41 ,  getTheme().getDropShadow());
            }
        });
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
