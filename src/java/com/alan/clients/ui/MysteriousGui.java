package com.alan.clients.ui;

import com.alan.clients.fontRender.FontManager;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;

public class MysteriousGui extends GuiScreen {
    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        String message = " 帝王鸡巴18CM ";
        float messagewidth = FontManager.arial22bold.getStringWidth(message);
        int startY = this.height / (20 * 10);
        int startX = this.width;
        RenderUtil.drawImage(new ResourceLocation("lavender/icons/datou.png"), 0, 0, sr.scaledWidth, sr.scaledHeight, -1);
        for (int i = 0; i < 55; i++) {
            for (int j = 0; j < 40; j++) {
                int finalJ = j;
                int finalI = i;
                GradientUtil.applyGradientHorizontal((float) (startX - j * FontManager.arial22bold.getStringWidth(message)), (float) (startY + i * 20), messagewidth, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                    RenderUtil.setAlphaLimit(0);
                    FontManager.arial22bold.drawString(message,(float) (startX - finalJ * FontManager.arial22bold.getStringWidth(message)), (float) (startY + finalI * 20), 0);
                });
            }
        }
        RenderUtil.drawImage(new ResourceLocation("lavender/icons/dwzfb.png"), 20, 20, 180, 320, -1);
        RenderUtil.drawImage(new ResourceLocation("lavender/icons/xbzsm.png"), 540, 20, 180, 180, -1);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
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
