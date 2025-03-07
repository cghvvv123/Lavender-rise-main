package com.alan.clients.module.impl.render.potionhud;

import com.alan.clients.Client;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.module.impl.render.PotionHud;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.*;
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

public class potionhud3 extends Mode<PotionHud> {
    public potionhud3(String name, PotionHud parent) {
        super(name, parent);
    }
    private final BooleanValue showTitle = new BooleanValue("Title", this, true);
    private final Map<Potion, PotionData> potionMap = new HashMap<>();
    private final HashMap<Integer, Integer> potionMaxDurations = new HashMap<>();
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    private float anmitY = 0.0f;
    private PotionHud PotionHud;
    final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
    public static Color color1;
    public static Color color2;
    public int getTotalHeight() {
        int h;
        if(interfaceModule.Render3cn.getValue()) {
            if (mc.thePlayer.getActivePotionEffects().size() == 0) {
                h = 10;
            } else if (mc.thePlayer.getActivePotionEffects().size() == 1) {
                h = 21;
            } else if (mc.thePlayer.getActivePotionEffects().size() == 2) {
                h = 24;
            } else if (mc.thePlayer.getActivePotionEffects().size() == 3) {
                h = 26;
            } else {
                h = 27;
            }
        }else {
            if(mc.thePlayer.getActivePotionEffects().size() == 0){
                h =10;
            }else if(mc.thePlayer.getActivePotionEffects().size() == 1){
                h =20;
            }else if(mc.thePlayer.getActivePotionEffects().size() == 2){
                h =23;
            } else if(mc.thePlayer.getActivePotionEffects().size() == 3){
                h =24;
            } else {
                h=25;
            }
        }

        return h * mc.thePlayer.getActivePotionEffects().size();
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        final PotionHud potionHud =getModule(PotionHud.class);
        if (this.PotionHud == null) {
            this.PotionHud = this.getModule(PotionHud.class);
        }
        anmitY = (float) RenderUtil.getAnimationState(anmitY,(scale.y + getTotalHeight()+4), 90.0);
        Vector2d position = potionHud.position;
        final String titleString = interfaceModule.Render3cn.getValue() && showTitle.getValue()
                ? Localization.get("药水显示") :showTitle.getValue() ? Localization.get("PotionHud") : "";

        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            float width = scale.x + 85;
            float y = (float) position.y + 27;
            final double textX = position.x + 5.3F;
            final double textY = position.y + scale.y / 2.0F -  FontManager.getNunitoBold(20).height() / 4.0F - 3F;
            PotionHud.positionValue.setScale(new Vector2d(width, anmitY));
            if(PotionHud.rendertitle()){
                FontManager.getProductSansLight(20).drawString("PotionHud", textX-4, textY-15,new Color(255,255,255,200).getRGB());
            }
            if ((double)interfaceModule.radius.getValue() == 0f)
                RenderUtil.rectangle(position.x, position.y, width, anmitY, interfaceModule.getRENDER3BG2Color());
            else RenderUtil.roundedRectangle(position.x, position.y, width, anmitY,
                    (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BG2Color());

            bg(position.x, position.y, width, anmitY,15, (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BgColor());

            if(interfaceModule.Render3cn.getValue())
                com.alan.clients.fontRender.FontManager.arial17bold.drawString(titleString,
                        (float) (FontManager.geticn(21).width("F")+textX+1),
                        (float) (textY), interfaceModule.getRENDER3fontColor().getRGB());else
            FontManager.getNunitoBold(20).drawString(titleString, (float)  FontManager.geticn(21).width("F")+textX+1,
                    (float) textY+0.5, interfaceModule.getRENDER3fontColor().getRGB());

            FontManager.geticn(20).drawString("F", (float) textX-1, (float) textY,interfaceModule.getRENDER3fontColor().getRGB());

            for (final PotionEffect potionEffect : mc.thePlayer.getActivePotionEffects()) {
                final Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                String name = I18n.format(potion.getName());
                if(potion.getName() == "potion.nightVision") {
                    name = "夜视效果";
                } else if(potion.getName() == "potion.regeneration"){
                    name = "生命恢复";
                }else if(potion.getName() == "potion.weakness"){
                    name = "迅捷";
                }else if(potion.getName() == "potion.fireResistance"){
                    name = "抗火";
                } else if(potion.getName() == "potion.poison") {
                    name = "中毒";
                }else if(potion.getName() =="potion.weakness") {
                    name = "虚弱";
                }else  if(potion.getName() =="potion.damageBoost") {
                    name = "力量效果";
                }else  if(potion.getName() =="potion.moveSlowdown") {
                    name = "缓慢效果";
                }else  if(potion.getName() =="potion.jump") {
                    name = "跳跃提升";
                }else  if(potion.getName() =="potion.waterBreathing") {
                    name = "水下呼吸";
                }else  if(potion.getName() =="potion.invisibility") {
                    name = "隐身";
                }else  if(potion.getName() =="potion.moveSpeed") {
                    name = "速度";
                }
                //"potion.moveSpeed"
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

               RenderUtil.rectangle(textX, y + 4F, width-13, 1, new Color(40, 40, 40, 205));

                for (float i = (float)textX; i < (float)(textX + potionDurationRatio *(width-13)); i += 1.0f) {
                    Gui.drawRect(i, y +4, i + 1.0f, y + 5, getColor((int)(i * 10.0f)));
                }
                if(interfaceModule.Render3cn.getValue()) {
                    com.alan.clients.fontRender.FontManager.arial18bold.drawString(name + " " +
                                    intToRomanByGreedy(potionEffect.getAmplifier() + 1),
                            (float) (textX), (float) (y - FontManager.getNunitoBold(20).height() + 8), new Color(255, 255, 255, 210).getRGB());
                    com.alan.clients.fontRender.FontManager.arial16bold.drawString(Potion.getDurationStringcn(potionEffect), (float) (textX),
                            y + 8F, new Color(255, 255, 255, 180).getRGB());
                    y += 28;
                } else {
                    FontManager.getNunitoBold(18).drawString(I18n.format(potion.getName()) + " " + intToRomanByGreedy(potionEffect.getAmplifier() + 1),
                            (float) (textX), (float) (y -  FontManager.getNunitoBold(20).height() + 9), new Color(255, 255, 255, 210).getRGB());
                    FontManager.getNunitoBold(17).drawString(Potion.getDurationString(potionEffect), (float) (textX),
                            y + 9F, new Color(255, 255, 255, 180).getRGB());
                    y += 26;
                }

            }

        });
        NORMAL_BLUR_RUNNABLES.add(() -> {
            if((double)interfaceModule.radius.getValue() == 0f){
                RenderUtil.rectangle(position.x, position.y, scale.x + 85, anmitY, Color.BLACK);
            }else {
                RenderUtil.roundedRectangle(position.x, position.y, scale.x + 85, anmitY, (double)interfaceModule.radius.getValue(),Color.BLACK);
            }
        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            if(!interfaceModule.BLOOM.getValue()) return;
            if((double)interfaceModule.radius.getValue() == 0f){
                RenderUtil.rectangle(position.x, position.y, scale.x + 85, anmitY, interfaceModule.getbloomBG2Color());
            }else {
                RenderUtil.roundedRectangle(position.x, position.y, scale.x + 85, anmitY, (double)interfaceModule.radius.getValue(),
                        interfaceModule.getbloomBG2Color());
            }
        });
    };
    public int getColor(int offset) {
        return ColorUtil.getColor2(new Color(255, 255, 255), new Color(140, 140, 140), 2800, offset);
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
