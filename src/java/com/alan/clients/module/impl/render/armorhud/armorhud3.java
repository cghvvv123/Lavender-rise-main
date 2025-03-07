package com.alan.clients.module.impl.render.armorhud;

import com.alan.clients.Client;
import com.alan.clients.module.impl.render.ArmorHud;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
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

public class armorhud3 extends Mode<ArmorHud> {
    public armorhud3(String name, ArmorHud parent) {
        super(name, parent);
    }

    private final BooleanValue showTitle = new BooleanValue("Title", this, true);

    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    int heightfix = 0;
    int Widthfix = 0;
    private float anmitY = 0.0f;
    private ArmorHud ArmorHud;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        final ArmorHud ArmorHud =getModule(ArmorHud.class);
        if (this.ArmorHud == null) {
            this.ArmorHud = this.getModule(ArmorHud.class);
        }
        Interface interfaceModule = getModule(Interface.class);
        anmitY = (float) RenderUtil.getAnimationState(anmitY,63, 90.0);
        Vector2d position = ArmorHud.position;
        final String titleString = interfaceModule.Render3cn.getValue() && showTitle.getValue()
                ? Localization.get("装备显示") : showTitle.getValue() ? Localization.get("ui.armorhud.title") : "";
        int Width =((int)scale.x + Widthfix);
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
                if ((double)interfaceModule.radius.getValue() == 0f)
                    RenderUtil.rectangle(position.x, position.y, Width,43, interfaceModule.getRENDER3BG2Color());
                else
                    RenderUtil.roundedRectangle(position.x, position.y, Width,43,
                            (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BG2Color());
                bg(position.x, position.y, Width,41,15, (double)interfaceModule.radius.getValue(),interfaceModule.getRENDER3BgColor());

            if(interfaceModule.Render3cn.getValue())
                com.alan.clients.fontRender.FontManager.arial17bold.drawString(titleString,
                        (float) textX+FontManager.getfluxicon(20).width("r")-2, (float) (textY-2),
                        interfaceModule.getRENDER3fontColor().getRGB());else
                FontManager.getNunitoBold(20).drawString(titleString, textX+FontManager.getfluxicon(20).width("r")-2, textY-1.5,
                        interfaceModule.getRENDER3fontColor().getRGB());

            FontManager.getfluxicon(20).drawString("r", textX-3, textY-1, interfaceModule.getRENDER3fontColor().getRGB());


            for (int index = 3; index >= 0; index--) {
                ItemStack stack = mc.thePlayer.inventory.armorInventory[index];
                if (stack != null) {
                        FontManager.getNunitoBold(13).drawStringWithShadow(Integer.toString(stack.getMaxDamage() - stack.getItemDamage()),
                                renderx+1, textY + 29, new Color(255, 255 ,255,170).getRGB());
                        renderItem.renderItemIntoGUI(stack, renderx - 2, textY + 10);

                    renderx += 17;
                    rendery += 17;
                    i++;
                }
            }
            if(!isPlayerWearingArmor){
                heightfix = 14;Widthfix =29;
                    FontManager.getNunitoBold(16).drawString("Empty..", textX+5, textY + 19,  new Color(255, 255 ,255,180).getRGB());

            }else if(armorCount == 0) {
                heightfix = 1;Widthfix =17;
            }else if(armorCount == 1) {
                heightfix = 13;Widthfix =29;
            }else  if(armorCount == 2) {
                heightfix = 30;Widthfix =29;
            }else  if(armorCount == 3) {
                heightfix = 47;Widthfix =35;
            }else  if(armorCount == 4) {
                heightfix = 64;Widthfix =53;
            }

        });
        NORMAL_BLUR_RUNNABLES.add(() -> {
                if((double)interfaceModule.radius.getValue() == 0f){
                    RenderUtil.rectangle(position.x, position.y, Width,42, Color.BLACK);
                }else {
                    RenderUtil.roundedRectangle(position.x, position.y, Width,42, (double)interfaceModule.radius.getValue(),Color.BLACK);
                }
        });
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            if(!interfaceModule.BLOOM.getValue()) return;
                if((double)interfaceModule.radius.getValue() == 0f){
                    RenderUtil.rectangle(position.x, position.y, Width,42, interfaceModule.getbloomBG2Color());
                }else {
                    RenderUtil.roundedRectangle(position.x, position.y, Width,42,
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
