package com.alan.clients.module.impl.render;

import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.other.AntiCheat;
import com.alan.clients.module.impl.other.HytHelper;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.DragValue;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ModuleInfo(name = "module.render.playerlisthud.name",category = Category.RENDER,description = "module.render.playerlisthud.description")
public class PlayerList extends Module {
    private final BooleanValue showTitle = new BooleanValue("Title", this, false);
    private final DragValue position = new DragValue("Position", this, new Vector2d(200, 200));

    private final Vector2f scale = new Vector2f(RenderUtil.GENERIC_SCALE, RenderUtil.GENERIC_SCALE);
    private float maxTextWidth = 0;
    private float anmitY = 0.0f;
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        InstanceAccess.NORMAL_POST_RENDER_RUNNABLES.add(() -> {
            this.position.setScale(new Vector2d(scale.x + 147, scale.y + 52));
            render(getTheme().getBackgroundShade(),true);
        });
        InstanceAccess.NORMAL_BLUR_RUNNABLES.add(() ->
                render( Color.BLACK,false));
        InstanceAccess.NORMAL_POST_BLOOM_RUNNABLES.add(() ->  render(getTheme().getDropShadow(),false));

    };
    private void render(final Color backgroundColor, final boolean font) {
        Vector2d position = this.position.position;
        final String titleString = showTitle.getValue() ? Localization.get("PlayerList") : "";
        final float titleWidth = InstanceAccess.nunitoNormal.width(titleString);
        final double Y =  position.y;
        final double textX = position.x + 5.3F;
        final double textY = Y + scale.y / 2.0F - InstanceAccess.nunitoNormal.height() / 8.0F - 12F;
        final double renderPlayerY = Y + 4F;
        this.position.setScale(new Vector2d(scale.x + 147, scale.y + 52));
        if (InstanceAccess.mc.thePlayer == null || InstanceAccess.mc.theWorld == null) return;
        List<EntityPlayer> players = InstanceAccess.mc.theWorld.playerEntities.stream().filter(p -> p != null && !p.isDead).collect(Collectors.toList());
        float width = 145;
       anmitY = (float) RenderUtil.getAnimationState(anmitY,(31 + (players.size()-1) * (FontManager.arial18.getHeight()+5)), 90.0);
        int widthfix = (int) (maxTextWidth - 4);
        GlStateManager.color(1, 1, 1, 1);
        RenderUtil.roundedRectangle(textX, Y - 10, widthfix, anmitY, getTheme().getRound(), backgroundColor);
        if(!font) return;

        for (int i = 0; i < players.size(); i++) {
            EntityPlayer player2 = players.get(i);
            renderPlayer(player2, i, (float) textX, (float) renderPlayerY);
        }
        RenderUtil.resetColor();
        GradientUtil.applyGradientHorizontal((float) textX + 5, (float) textY, titleWidth, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
            RenderUtil.setAlphaLimit(0);
            InstanceAccess.nunitoNormal.drawStringWithShadow(titleString, textX + 5, textY, 0);
        });
    }
    private void renderPlayer(EntityPlayer player, int i, float x, float y) {
        float height =FontManager.arial18.getHeight()+5;
        float offset = i * (height);
        float healthPercent = MathHelper.clamp_float((player.getHealth() + player.getAbsorptionAmount()) / (player.getMaxHealth()
                + player.getAbsorptionAmount()), 0, 1);

        Color healthColor = healthPercent > .75 ? new Color(66, 246, 123) : healthPercent > .5 ? new Color(228, 255, 105) : healthPercent > .35 ? new Color(236, 100, 64) : new Color(255, 65, 68);
        String healthText = (int) MathUtil.round(healthPercent * 100, 0) + "%";
        String prefix =  player == InstanceAccess.mc.thePlayer ?"[Me] " :  player.isOnSameTeam(InstanceAccess.mc.thePlayer)  &&  player != InstanceAccess.mc.thePlayer? "[Team] " : "";
        String HytHelperfix = HytHelper.strength && Objects.equals(player.getCommandSenderName(), HytHelper.name)? "[力量药水]"
                : HytHelper.regen && Objects.equals(player.getCommandSenderName(), HytHelper.name) ? "[恢复药水]"
                : HytHelper.godaxe && Objects.equals(player.getCommandSenderName(), HytHelper.name)? "[秒人斧]"
                : HytHelper.kbball && Objects.equals(player.getCommandSenderName(), HytHelper.name)? "[击退球]"
                : HytHelper.gapple && Objects.equals(player.getCommandSenderName(), HytHelper.name)? "[附魔金苹果]" : "";
        AntiCheat antiCheat = Client.INSTANCE.getModuleManager().get(AntiCheat.class);
        String originalName = player.getCommandSenderName();
        boolean isCheater = antiCheat != null && antiCheat.isCheater(originalName);
        if (isCheater) {
            originalName = EnumChatFormatting.RED + "[Hacker] " + EnumChatFormatting.RESET + originalName;
        }
        String text = originalName + " " + healthText;
        int prefixWidth = player.isOnSameTeam(InstanceAccess.mc.thePlayer) ||  player == InstanceAccess.mc.thePlayer?  FontManager.arial18.getStringWidth(prefix) : 0;
        int HytHelperfixWidth = (HytHelper.strength|| HytHelper.regen || HytHelper.godaxe || HytHelper.kbball || HytHelper.gapple) ?  FontManager.arial18.getStringWidth(HytHelperfix) : 0;
        int textWidth = FontManager.arial18.getStringWidth(text) + 32 + prefixWidth + HytHelperfixWidth;
        maxTextWidth = Math.max(maxTextWidth, textWidth);
        if(player.isOnSameTeam(InstanceAccess.mc.thePlayer) || player == InstanceAccess.mc.thePlayer) {
            FontManager.arial18.drawString(prefix, x + 21, y + offset + FontManager.arial20.getMiddleOfBox(height) + 3, Color.RED.getRGB());
        }
        if(HytHelper.strength || HytHelper.regen|| HytHelper.godaxe|| HytHelper.kbball|| HytHelper.gapple) {
            FontManager.arial18.drawString(HytHelperfix, x  +  FontManager.arial18.getStringWidth(text) + prefixWidth + HytHelperfixWidth-10, y + offset + FontManager.arial20.getMiddleOfBox(height) + 3,
                    Color.RED.getRGB());
        }
        FontManager.arial18.drawString(text, x + 21 + prefixWidth, y + offset + FontManager.arial20.getMiddleOfBox(height)+3, healthColor.getRGB());
        float headX = x + 5;
        float headWH = 32;
        float headY = y + offset + height /2f - 6;
        float f = 0.35F;
        RenderUtil.resetColor();
        RenderUtil.scaleStart(headX, headY, f);
        InstanceAccess.mc.getTextureManager().bindTexture(((AbstractClientPlayer) player).getLocationSkin());
        Gui.drawTexturedModalRect(headX, headY, (int) headWH, (int) headWH, (int) headWH, (int) headWH);
        RenderUtil.scaleEnd();
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
        Color firstColor = Interface.mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = Interface.mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[]{firstColor, secondColor};
    }
}
