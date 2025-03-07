package com.alan.clients.ui.menu.component.button.impl;

import com.alan.clients.ui.menu.component.button.MenuButton;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.RenderUtil;

import java.awt.*;

public class MainMenuButton  extends MenuButton {
    private static final Font FONT_RENDERER = FontManager.getProductSansBold(24);

    public String name;

    public MainMenuButton(double x, double y, double width, double height, Runnable runnable, String name) {
        super(x, y, width, height, runnable);
        this.name = name;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // Runs the animation update - keep this
        super.draw(mouseX, mouseY, partialTicks);

        final double value = getY();
        final double progress = value / this.getY();
        final Color basic = ColorUtil.withAlpha(Color.WHITE, (int) (progress * ( 2*this.getHoverAnimation().getValue())));


        RenderUtil.roundedRectangle(this.getX(), value, this.getWidth(), this.getHeight(), this.getHeight()/2,new Color(255,255,255,51));
        RenderUtil.roundedOutlineRectangle(this.getX(), value, this.getWidth(), this.getHeight(), this.getHeight()/2,
                1, basic);
        FONT_RENDERER.drawCenteredString(this.name, (float) (this.getX() + this.getWidth() / 2.0F),
                (float) (value + this.getHeight() / 2.0F - 4), basic.getRGB());


    }
}
