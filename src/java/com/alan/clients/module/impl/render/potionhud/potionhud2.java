package com.alan.clients.module.impl.render.potionhud;

import com.alan.clients.module.impl.render.PotionHud;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.PotionData;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

public class potionhud2 extends Mode<PotionHud> {
    public potionhud2(String name, PotionHud parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, true);
    private final Map<Potion, PotionData> potionMap = new HashMap<>();
    private final HashMap<Integer, Integer> potionMaxDurations = new HashMap<>();
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    private float anmitY = 0.0f;
    private PotionHud PotionHud;
    public int getTotalHeight() {
        int h;
        if(mc.thePlayer.getActivePotionEffects().size() == 0){
            h =16;
        }else if(mc.thePlayer.getActivePotionEffects().size() == 1){
            h =20;
        }else if(mc.thePlayer.getActivePotionEffects().size() == 2){
            h =22;
        } else if(mc.thePlayer.getActivePotionEffects().size() == 3){
            h =23;
        } else {
            h=25;
        }

        return h * mc.thePlayer.getActivePotionEffects().size();
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        final PotionHud potionHud =getModule(PotionHud.class);
        if (this.PotionHud == null) {
            this.PotionHud = this.getModule(PotionHud.class);
        }
        anmitY = (float) RenderUtil.getAnimationState(anmitY,(scale.y + getTotalHeight()+4), 90.0);
        Vector2d position = potionHud.position;
        final String titleString = showTitle.getValue() ? Localization.get("PotionHud") : "";

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            float width = scale.x + 85;
            float y = (float) position.y + 27;
            final double textX = position.x + 5.3F;
            final double textY = position.y + scale.y / 2.0F -  FontManager.getNunitoBold(20).height() / 4.0F - 3F;
            PotionHud.positionValue.setScale(new Vector2d(width, anmitY));
            if(PotionHud.rendertitle()){
                FontManager.getProductSansLight(20).drawString("PotionHud", textX-4, textY-15,new Color(255,255,255,200).getRGB());
            }
            RenderUtil.rectangle(position.x, position.y, width, anmitY, getTheme().getBackgroundShade());
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY,  nunitoNormal.width(titleString), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
               FontManager.getNunitoBold(20).drawString(titleString, (float) textX, (float) textY, 0);
            });
            if(showTitle.getValue()) RenderUtil.roundedRectangle(position.x, position.y+3, 2, 9.5, 2,getClientColors()[0]);

            for (final PotionEffect potionEffect : mc.thePlayer.getActivePotionEffects()) {
                final Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                final String name = I18n.format(potion.getName());
                final PotionData potionData;
                if(potionMap.containsKey(potion) && potionMap.get(potion).level == potionEffect.getAmplifier()) potionData = potionMap.get(potion);else potionMap.put(potion, (potionData = new PotionData(potion, potionEffect.getAmplifier())));
                boolean flag = true;
                for(final PotionEffect checkEffect : mc.thePlayer.getActivePotionEffects())
                    if (checkEffect.getAmplifier() == potionData.level) {
                        flag = false;
                        break;
                    }
                int potion2 = potionEffect.getPotionID();
                Integer maxDuration = potionMaxDurations.get(potion2);
                if (maxDuration == null || maxDuration < potionEffect.getDuration()) {
                    potionMaxDurations.put(potion2, potionEffect.getDuration());
                }
                if(flag) potionMap.remove(potion);
                int potionTime,potionMaxTime;
                try {
                    potionTime = Integer.parseInt(Potion.getDurationString(potionEffect).split(":")[0]);
                    potionMaxTime = Integer.parseInt(Potion.getDurationString(potionEffect).split(":")[1]);
                } catch(Exception ignored) {
                    potionTime = 100;
                    potionMaxTime = 1000;
                }
                final int lifeTime = (potionTime * 60 + potionMaxTime);
                float potionDurationRatio = (float)potionEffect.getDuration() / (potionMaxDurations.get(potionEffect.getPotionID()) != null ? potionMaxDurations.get(potionEffect.getPotionID()) : 1);
                if (potionData.getMaxTimer() == 0 || lifeTime > (double)potionData.getMaxTimer()) potionData.maxTimer = lifeTime;


                FontManager.getNunitoBold(18).drawString(name + " " + intToRomanByGreedy(potionEffect.getAmplifier() + 1),
                        (float) (textX + 23F), (float) (y -  FontManager.getNunitoBold(20).height() + 9), new Color(255, 255, 255, 210).getRGB());
                FontManager.getNunitoBold(17).drawString(Potion.getDurationString(potionEffect), (float) (textX + 23F),
                        y + 7.0F, new Color(255, 255, 255, 180).getRGB());

                RenderUtil.circle(textX-2, y - 9, 22, 360, true, new Color(0, 0, 0, 40));
                if (potion.hasStatusIcon()) {
                    int statusIconIndex = potion.getStatusIconIndex();
                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                    StencilUtil.initStencilToWrite();
                    RenderUtil.circle(textX-2, y - 9, 22, 360, true, new Color(0, 0, 0, 40));
                    StencilUtil.bindReadStencilBuffer(1);
                    RenderUtil.resetColor();
                    RenderUtil.setAlphaLimit(0);
                    RenderUtil.resetColor();
                    Gui.drawTexturedModalRect((float) textX, y - 7,
                            statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
                    StencilUtil.uninitStencilBuffer();
                }

                RenderUtil.circle(textX-2, y - 9, 22, 360, false, new Color(0, 0, 0, 70));
                RenderUtil.circle(textX-2, y - 9, 22, potionDurationRatio * 360, false,  Color.white);

                y += 26;
            }

        });

        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.rectangle(position.x, position.y, scale.x + 85, anmitY ,
                 Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.rectangle(position.x, position.y, scale.x + 85, anmitY, getTheme().getDropShadow()));
    };
    private void hasStatusIcon(final float x, final float y, final int minU, final int minV, final float size) {
        StencilUtil.initStencilToWrite();
        RenderUtil.circle(x,
                y, size, 360, true, new Color(0, 0, 0, 40));
        StencilUtil.bindReadStencilBuffer(1);
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
        Gui.drawTexturedModalRect((float) x, (float) y,
                minU, minU, 18, 18);
        StencilUtil.uninitStencilBuffer();
    }
    private String intToRomanByGreedy(int num) {
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < values.length && num >= 0; i++)
            while (values[i] <= num){
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }

        return stringBuilder.toString();
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
