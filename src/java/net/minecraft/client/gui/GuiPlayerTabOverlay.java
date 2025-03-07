package net.minecraft.client.gui;

import com.alan.clients.Client;
import com.alan.clients.Verify.GuiLogin;
import com.alan.clients.module.impl.other.AntiCheat;
import com.alan.clients.util.AnimationUtils;
import com.alan.clients.util.ColorUtils;
import com.alan.clients.util.ESP2D;
import com.alan.clients.util.font.impl.minecraft.FontRenderer;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GuiPlayerTabOverlay extends Gui {
    public static final Ordering<NetworkPlayerInfo> playerList = Ordering.from(new PlayerComparator());
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    @Setter
    private IChatComponent footer;
    @Setter
    private IChatComponent header;
    public float animation = 0;
    public boolean animationDone = false;
    public float hue = 0;

    /**
     * The last time the playerlist was opened (went from not being renderd, to being rendered)
     */
    private long lastTimeOpened;

    /**
     * Weither or not the playerlist is currently being rendered
     */
    private boolean isBeingRendered;

    public GuiPlayerTabOverlay(final Minecraft mcIn, final GuiIngame guiIngameIn) {
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
    }

    /**
     * Returns the name that should be rendered for the player supplied
     */
    public String getPlayerName(final NetworkPlayerInfo networkPlayerInfoIn) {
        String originalName = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());

        AntiCheat antiCheat = Client.INSTANCE.getModuleManager().get(AntiCheat.class);

        boolean isCheater = antiCheat != null && antiCheat.isCheater(originalName);
        if (isCheater) {
            originalName = EnumChatFormatting.RED + "[Hacker] " + EnumChatFormatting.RESET + originalName;
        }
        if (Objects.equals(originalName, mc.thePlayer.getCommandSenderName())) {
            originalName = EnumChatFormatting.RED + "[" + GuiLogin.rank + "] " + EnumChatFormatting.GREEN + "[" + Client.location + "人] " +  EnumChatFormatting.RESET + originalName;
        }
        return originalName;
    }

    /**
     * Called by GuiIngame to update the information stored in the playerlist, does not actually render the list,
     * however.
     *
     * @param willBeRendered True if the playerlist is intended to be renderd subsequently.
     */
    public void updatePlayerList(final boolean willBeRendered) {
        if (willBeRendered && !this.isBeingRendered) {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }

    /**
     * Renders the playerlist, its background, headers and footers.
     */
    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn) {
        final NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = playerList.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        int i = 0;
        int j = 0;

        for (NetworkPlayerInfo networkplayerinfo : list) {
            String playername = networkplayerinfo.getGameProfile().getName();

            int k = this.mc.fontRendererObj.width(playername);
            int k1 = mc.fontRendererObj.width(this.getPlayerName(networkplayerinfo));

            k = Math.max(k, k1);

            String clientTag = "";

            k = k + this.mc.fontRendererObj.width(clientTag); //取最大长度
            i = Math.max(i, k);
            if (scoreObjectiveIn != null
                    && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                k = this.mc.fontRendererObj.width(" " + scoreboardIn
                        .getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn)
                        .getScorePoints());
                j = Math.max(j, k);
            }
        }
        list = list.subList(0, Math.min(list.size(), 80));
        int l3 = list.size();
        int i4 = l3;
        int j4;

        for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4) {
            ++j4;
        }

        boolean flag = this.mc.isIntegratedServerRunning()
                || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l;

        if (scoreObjectiveIn != null) {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                l = 90;
            } else {
                l = j;
            }
        } else {
            l = 0;
        }

        int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List<String> list1 = null;
        List<String> list2 = null;

        if (this.header != null) {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

            for (String s : list1) {
                l1 = Math.max(l1, this.mc.fontRendererObj.width(s));
            }
        }

        if (this.footer != null) {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

            for (String s2 : list2) {
                l1 = Math.max(l1, this.mc.fontRendererObj.width(s2));
            }
        }

        float target = isBeingRendered ? 10 : -(((list.size() / j4) * 10) + (list1 != null ? list1.size() * 10 : 0) + (list2 != null ? list2.size() * 10 : 0));
        this.animation = AnimationUtils.getAnimationState(this.animation, target, Math.max(10, (Math.abs(this.animation - target)) * 35) * 0.3f);

        if (this.hue > 255.0F) {
            this.hue = 0.0F;
        }

        float h = this.hue;
        float h2 = this.hue + 85.0F;
        float h3 = this.hue + 170.0F;

        if (h2 > 255.0F) {
            h2 -= 255.0F;
        }

        if (h3 > 255.0F) {
            h3 -= 255.0F;
        }

        Color a = Color.getHSBColor(h / 255.0F, 0.4F, 1.0F);
        Color b = Color.getHSBColor(h2 / 255.0F, 0.4F, 1.0F);
        Color c = Color.getHSBColor(h3 / 255.0F, 0.4F, 1.0F);
        int color1 = a.getRGB();
        int color2 = b.getRGB();
        int color3 = c.getRGB();
        this.hue = this.hue + 0.05f;

        float bottom = ((list.size() / j4 + (list.size() % j4)) * FontRenderer.FONT_HEIGHT + 1) + (list1 != null ? list1.size() * FontRenderer.FONT_HEIGHT + 1 : 0) + (list2 != null ? list2.size() * FontRenderer.FONT_HEIGHT + 4 : 0) + 1;
        ESP2D.INSTANCE.rectangle(width / 2 - l1 / 2 - 5.5f, animation - 3.5f, width / 2 + l1 / 2 + 5.5f, animation + bottom - 0.5f, 0xff232529);
        ESP2D.INSTANCE.rectangleBordered(width / 2 - l1 / 2 - 5.5f, animation - 3.5f, width / 2 + l1 / 2 + 5.5f, animation + bottom - 0.5f, 0.5d, 0x00ffffff, ColorUtils.getColor(10));
        ESP2D.INSTANCE.rectangleBordered(width / 2 - l1 / 2 - 5f, animation - 3f, width / 2 + l1 / 2 + 5f, animation + bottom - 1f, 0.5d, 0x00ffffff, ColorUtils.getColor(100));
        ESP2D.INSTANCE.rectangleBordered(width / 2 - l1 / 2 - 4.5f, animation - 2.5f, width / 2 + l1 / 2 + 4.5f, animation + bottom - 1.5f, 1d, ColorUtils.getColor(0, 0), ColorUtils.getColor(60));
        ESP2D.INSTANCE.rectangleBordered(width / 2 - l1 / 2 - 3.5f, animation - 1.5f, width / 2 + l1 / 2 + 3.5f, animation + bottom - 2.5f, 0.5d, ColorUtils.getColor(0, 0), ColorUtils.getColor(100));

        drawGradientSideways(width / 2 - l1 / 2 - 3f, animation + bottom - 3.5f, width / 2 + 3f, animation + bottom - 3f, color1, color2);
        drawGradientSideways(width / 2 - 3f, animation + bottom - 3.5f, width / 2 + l1 / 2 + 3f, animation + bottom - 3f, color2, color3);

        animationDone = target == animation && target < 0;

        float k1 = this.animation;

        if (list1 != null) {

            for (String s3 : list1) {
                int i2 = this.mc.fontRendererObj.width(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float) (width / 2 - i2 / 2), k1 + 0.5f, -1);
                k1 += FontRenderer.FONT_HEIGHT;
            }

            ++k1;
        }

        for (int k4 = 0; k4 < l3; ++k4) {
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            float k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 9, 0xff40444b);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (k4 < list.size()) {
                NetworkPlayerInfo networkplayerinfo1 = list.get(k4);
                String s1 = this.getPlayerName(networkplayerinfo1);

                s1 = s1.replaceAll("\u00a7k", "");
                GameProfile gameprofile = networkplayerinfo1.getGameProfile();

                if (flag) {
                    EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                    boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE)
                            && (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
                    this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                    int l2 = 8 + (flag1 ? 8 : 0);
                    int i3 = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float) l2, 8, i3, 9, 9, 64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
                        int j3 = 8 + (flag1 ? 8 : 0);
                        int k3 = 8 * (flag1 ? -1 : 1);
                        Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float) j3, 8, k3, 9, 9, 64.0F, 64.0F);
                    }

                    j2 += 9;
                }

                if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR) {
                    s1 = EnumChatFormatting.ITALIC + s1;
                    this.mc.fontRendererObj.drawStringWithShadow(s1, (float) j2 + 1, k2 + 0.5f, -1862270977);
                } else {
                    this.mc.fontRendererObj.drawStringWithShadow(s1, (float) j2 + 1, k2 + 0.5f, -1);
                }

                if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR) {
                    int k5 = j2 + i + 1;
                    int l5 = k5 + l;

                    if (l5 - k5 > 5) {
                        this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5,
                                networkplayerinfo1);
                    }
                }

                this.drawPing(i1, j2 - (flag ? 9 : 0), (int) k2, networkplayerinfo1);
            }
        }

        if (list2 != null) {
            k1 = k1 + i4 * 9 + 1;

            for (String s4 : list2) {
                int j5 = this.mc.fontRendererObj.width(s4);
                this.mc.fontRendererObj.drawStringWithShadow(s4, (float) (width / 2 - j5 / 2), k1, -1);
                k1 += FontRenderer.FONT_HEIGHT;
            }

        }
    }

    protected void drawPing(final int p_175245_1_, final int p_175245_2_, final int p_175245_3_, final NetworkPlayerInfo networkPlayerInfoIn) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        final int i = 0;
        int j;

        if (networkPlayerInfoIn.getResponseTime() < 0) {
            j = 5;
        } else if (networkPlayerInfoIn.getResponseTime() < 150) {
            j = 0;
        } else if (networkPlayerInfoIn.getResponseTime() < 300) {
            j = 1;
        } else if (networkPlayerInfoIn.getResponseTime() < 600) {
            j = 2;
        } else if (networkPlayerInfoIn.getResponseTime() < 1000) {
            j = 3;
        } else {
            j = 4;
        }

        zLevel += 100.0F;
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0, 176 + j * 8, 10, 8);
        zLevel -= 100.0F;
    }

    private void drawScoreboardValues(final ScoreObjective p_175247_1_, final int p_175247_2_, final String p_175247_3_, final int p_175247_4_, final int p_175247_5_, final NetworkPlayerInfo p_175247_6_) {
        final int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();

        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
            this.mc.getTextureManager().bindTexture(icons);

            if (this.lastTimeOpened == p_175247_6_.func_178855_p()) {
                if (i < p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b(this.guiIngame.getUpdateCounter() + 20);
                } else if (i > p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b(this.guiIngame.getUpdateCounter() + 10);
                }
            }

            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L || this.lastTimeOpened != p_175247_6_.func_178855_p()) {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }

            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            final int j = MathHelper.ceiling_float_int((float) Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            final int k = Math.max(MathHelper.ceiling_float_int((float) (i / 2)), Math.max(MathHelper.ceiling_float_int((float) (p_175247_6_.func_178860_m() / 2)), 10));
            final boolean flag = p_175247_6_.func_178858_o() > (long) this.guiIngame.getUpdateCounter() && (p_175247_6_.func_178858_o() - (long) this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0) {
                final float f = Math.min((float) (p_175247_5_ - p_175247_4_ - 4) / (float) k, 9.0F);

                if (f > 3.0F) {
                    for (int l = j; l < k; ++l) {
                        drawTexturedModalRect((float) p_175247_4_ + (float) l * f, (float) p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int j1 = 0; j1 < j; ++j1) {
                        drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, flag ? 25 : 16, 0, 9, 9);

                        if (flag) {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m()) {
                                drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, 70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m()) {
                                drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, 79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i) {
                            drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i) {
                            drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, (float) p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                } else {
                    final float f1 = MathHelper.clamp_float((float) i / 20.0F, 0.0F, 1.0F);
                    final int i1 = (int) ((1.0F - f1) * 255.0F) << 16 | (int) (f1 * 255.0F) << 8;
                    String s = "" + (float) i / 2.0F;

                    if (p_175247_5_ - this.mc.fontRendererObj.width(s + "hp") >= p_175247_4_) {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s, (float) ((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.width(s) / 2), (float) p_175247_2_, i1);
                }
            }
        } else {
            final String s1 = EnumChatFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1, (float) (p_175247_5_ - this.mc.fontRendererObj.width(s1)), (float) p_175247_2_, 16777215);
        }
    }

    public void func_181030_a() {
        this.header = null;
        this.footer = null;
    }

    static class PlayerComparator implements Comparator<NetworkPlayerInfo> {
        private PlayerComparator() {
        }

        public int compare(final NetworkPlayerInfo p_compare_1_, final NetworkPlayerInfo p_compare_2_) {
            final ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
            final ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
        }
    }

    public static void drawGradientSideways(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawScoreboardValues(ScoreObjective p_175247_1_, float p_175247_2_, String p_175247_3_, int p_175247_4_,
                                      int p_175247_5_, NetworkPlayerInfo p_175247_6_) {
        int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();

        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
            this.mc.getTextureManager().bindTexture(icons);

            if (this.lastTimeOpened == p_175247_6_.func_178855_p()) {
                if (i < p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b(this.guiIngame.getUpdateCounter() + 20);
                } else if (i > p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b(this.guiIngame.getUpdateCounter() + 10);
                }
            }

            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L
                    || this.lastTimeOpened != p_175247_6_.func_178855_p()) {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }

            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            int j = MathHelper.ceiling_float_int((float) Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            int k = Math.max(MathHelper.ceiling_float_int((float) (i / 2)),
                    Math.max(MathHelper.ceiling_float_int((float) (p_175247_6_.func_178860_m() / 2)), 10));
            boolean flag = p_175247_6_.func_178858_o() > (long) this.guiIngame.getUpdateCounter()
                    && (p_175247_6_.func_178858_o() - (long) this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0) {
                float f = Math.min((float) (p_175247_5_ - p_175247_4_ - 4) / (float) k, 9.0F);

                if (f > 3.0F) {
                    for (int l = j; l < k; ++l) {
                        drawTexturedModalRect((float) p_175247_4_ + (float) l * f, p_175247_2_,
                                flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int j1 = 0; j1 < j; ++j1) {
                        drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, p_175247_2_,
                                flag ? 25 : 16, 0, 9, 9);

                        if (flag) {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m()) {
                                drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, p_175247_2_,
                                        70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m()) {
                                drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, p_175247_2_,
                                        79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i) {
                            drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, p_175247_2_,
                                    j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i) {
                            drawTexturedModalRect((float) p_175247_4_ + (float) j1 * f, p_175247_2_,
                                    j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                } else {
                    float f1 = MathHelper.clamp_float((float) i / 20.0F, 0.0F, 1.0F);
                    int i1 = (int) ((1.0F - f1) * 255.0F) << 16 | (int) (f1 * 255.0F) << 8;
                    String s = "" + (float) i / 2.0F;

                    if (p_175247_5_ - this.mc.fontRendererObj.width(s + "hp") >= p_175247_4_) {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s,
                            (float) ((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.width(s) / 2),
                            p_175247_2_, i1);
                }
            }
        } else {
            String s1 = EnumChatFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1,
                    (float) (p_175247_5_ - this.mc.fontRendererObj.width(s1)), p_175247_2_ + 0.5f, 16777215);
        }
    }
}
