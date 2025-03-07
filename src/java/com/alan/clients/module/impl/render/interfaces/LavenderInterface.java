package com.alan.clients.module.impl.render.interfaces;

import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.value.Mode;

import java.awt.*;

public class LavenderInterface extends Mode<Interface> {

    private RapeMasterFontManager font;

    public LavenderInterface(String name, Interface parent) {
        super(name, parent);
        font = FontManager.arial16;
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {

        if (InstanceAccess.mc == null || InstanceAccess.mc.gameSettings.showDebugInfo || InstanceAccess.mc.theWorld == null || InstanceAccess.mc.thePlayer == null) {
            return;
        }

        this.getParent().setModuleSpacing(this.font.getFontHeight() + 1);
        this.getParent().setWidthComparator(this.font);
        this.getParent().setEdgeOffset(4);

        // modules in the top right corner of the screen
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            final double x = moduleComponent.getPosition().getX();
            final double y = moduleComponent.getPosition().getY();
            final Color finalColor = Color.WHITE;

            font.drawStringWithShadow(moduleComponent.getTranslatedName(), x, y, finalColor.getRGB());
        }

        RenderUtil.rectangle(0, 10, 185, 12, ColorUtil.withAlpha(Color.WHITE, 100));

        font.drawString("LavenderClient-Beta ", 30, 13, getTheme().getFirstColor().getRGB());
    };

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        InstanceAccess.threadPool.execute(() -> {
            // modules in the top right corner of the screen
            for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                String name = moduleComponent.getTranslatedName();

                moduleComponent.setNameWidth(font.getStringWidth(name));
                moduleComponent.setTagWidth(0);
                moduleComponent.setTag("");
            }
        });
    };
}
