package com.alan.clients.module.impl.render;

import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.render.interfaces.ModuleComponent;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.DragValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

@ModuleInfo(name = "module.render.keybinds.name", category = Category.RENDER, description = "CNM")
public class KeyBinds extends Module {
    private final BooleanValue showTitle = new BooleanValue("Title", this, false);
    private final BooleanValue onlyState = new BooleanValue("OnlyModuleState", this, false);
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));
    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    private float anmitY = 0.0f;
    public int getModuleY() {
        int y = 0;
        for (Module module : Client.INSTANCE.getModuleManager()) {
            if (module.getKeyCode() == 0) {
                continue;
            }
            if (onlyState.getValue()) {
                if (!module.isEnabled()) {
                    continue;
                }
            }
            y += 12;
        }
        return y;
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        anmitY = (float) RenderUtil.getAnimationState(anmitY,(scale.y + getModuleY()), 90.0);
        Vector2d position = this.position.position;

        final String titleString = showTitle.getValue() ? Localization.get("KeyBinds") : "";
        NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            float width = scale.x + 74;
            float height = scale.y  + getModuleY();
            float y = (float) position.y + 27;
            if(mc.currentScreen instanceof GuiChat){
                com.alan.clients.util.font.FontManager.getProductSansLight(20).drawString("KeyBinds",position.x+2, position.y-12,new Color(255,255,255,200).getRGB());
            }
            RenderUtil.roundedRectangle(position.x, position.y, width, anmitY, getTheme().getRound(), getTheme().getBackgroundShade());

            this.position.setScale(new Vector2d(width, height));
            final double textX = position.x + 5.3F;
            final double textY = position.y + scale.y / 2.5f - FontManager.arial20.getHeight() / 4.0F - 1F;
            RenderUtil.resetColor();
            GradientUtil.applyGradientHorizontal((float) textX, (float) textY, nunitoNormal.width(titleString), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                RenderUtil.setAlphaLimit(0);
                nunitoNormal.drawString(titleString, (float) textX, (float) textY, 0);
            });
            GlStateManager.pushMatrix();
            for (final Module module : Client.INSTANCE.getModuleManager()) {
                ModuleComponent moduleComponent = new ModuleComponent(this);
                moduleComponent.setTranslatedName(Localization.get(module.getDisplayName()));
                if (module.getKeyCode() == 0)
                    continue;
                if (onlyState.getValue()) {
                    if (!module.isEnabled())
                        continue;
                }
                String text = module.isEnabled() ? "[True]" : "[False]";
                int textColor = module.isEnabled() ? new Color(255, 255, 255).getRGB() : new Color(100, 100, 100).getRGB();
                FontManager.arial18.drawStringWithShadow(moduleComponent.getTranslatedName(),textX ,
                        y - FontManager.arial18.getHeight() + 5.5, -1);
                FontManager.arial18.drawStringWithShadow(text,textX + 55F, y - FontManager.arial18.getHeight() + 5.5,textColor);
                y += 12;
            }
            GlStateManager.popMatrix();
        });
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 74, anmitY,
                getTheme().getRound(), Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.roundedRectangle(position.x, position.y, scale.x + 74, anmitY, getTheme().getRound() + 1, getTheme().getDropShadow()));


    };

    private Color getClientColor() {
        Color theme1 = this.getTheme().getFirstColor();
        return new Color(theme1.getRGB());

    }

    private Color getAlternateClientColor() {
        Color theme2 = this.getTheme().getSecondColor();
        return new Color(theme2.getRGB());
    }

    public Color[] getClientColors() {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}
