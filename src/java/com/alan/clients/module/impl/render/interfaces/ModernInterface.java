package com.alan.clients.module.impl.render.interfaces;

import com.alan.clients.Client;
import com.alan.clients.Type;
import com.alan.clients.Verify.GuiLogin;
import com.alan.clients.component.impl.render.NotificationComponent;
import com.alan.clients.component.impl.render.ParticleComponent;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.impl.render.Interface;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.other.KillEvent;
import com.alan.clients.newevent.impl.other.TickEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.render.GradientUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.render.particle.Particle;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import com.alan.clients.value.impl.SubMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import util.time.StopWatch;

import java.awt.*;

import static com.alan.clients.module.impl.render.Interface.mixColors2;
import static com.alan.clients.util.animations.Easing.EASE_OUT_ELASTIC;
import static com.alan.clients.util.render.ColorUtil.mixColors;
import static net.minecraft.client.gui.GuiChat.openingAnimation;

public class ModernInterface extends Mode<Interface> {

    private final Font productSansMedium36 = FontManager.getProductSansMedium(36);
    private final RapeMasterFontManager productSansRegular = com.alan.clients.fontRender.FontManager.arial18;
    private final Font productSansMedium18 = FontManager.getProductSansMedium(18);
    private final StopWatch stopWatch = new StopWatch();

    private final ModeValue colorMode = new ModeValue("ArrayList Color Mode", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new SubMode("Static"));
        add(new SubMode("Fade"));
        add(new SubMode("Breathe"));
        setDefault("Fade");
    }};
    private final ModeValue logoMode = new ModeValue("Logo Mode",this,() -> Client.CLIENT_TYPE != Type.RISE){{
        add(new SubMode("NeverLose"));
        add(new SubMode("Normal"));
        add(new SubMode("Lavender"));
        add(new SubMode("Lavender2"));
        add(new SubMode("Lavender3"));
        add(new SubMode("Lavender4"));
        add(new SubMode("Lavender5"));
        add(new SubMode("Skeet"));
        add(new SubMode("Moon"));
        setDefault("NeverLose");
    }};

    private final ModeValue shader = new ModeValue("Shader Effect", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new SubMode("Glow"));
        add(new SubMode("Shadow"));
        add(new SubMode("None"));
        setDefault("Shadow");
    }};

    private final BooleanValue dropShadow = new BooleanValue("Drop Shadow", this, true, () -> Client.CLIENT_TYPE != Type.RISE);
    private final ModeValue sidebar = new ModeValue("Sidebar", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new SubMode("Rise"));
        add(new SubMode("Lavender"));
        add(new SubMode("Left"));
        add(new SubMode("Off"));
        setDefault("Rise");
    }};
    private final BooleanValue  particles = new BooleanValue("Particles on Kill", this, true, () -> Client.CLIENT_TYPE != Type.RISE);
    private final ModeValue background = new ModeValue("BackGround", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new SubMode("Off"));
        add(new SubMode("Normal"));
        add(new SubMode("Blur"));
        setDefault("Normal");
    }};

    private boolean glow, shadow;
    private boolean normalBackGround, blurBackGround;
    private String username, coordinates;
    private float  userWidth, xyzWidth;
    private static final Animation animation = new Animation(EASE_OUT_ELASTIC, 500);
    public ModernInterface(String name, Interface parent) {
        super(name, parent);
    }
    private  Color getClientColor() {
        Color theme1 = this.getTheme().getFirstColor();
        return  new Color(theme1.getRGB());

    }
    private Color getAlternateClientColor() {
        Color  theme2 = this.getTheme().getSecondColor();
        return new  Color(theme2.getRGB());
    }
    public  Color[] getClientColors() {
        Color firstColor = mixColors2(getClientColor(), getAlternateClientColor());
        Color secondColor = mixColors2(getAlternateClientColor(), getClientColor());
        return new Color[] { firstColor, secondColor };
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (isNull()) return;
        if (mc == null || mc.gameSettings.showDebugInfo || mc.theWorld == null || mc.thePlayer == null || Client.CLIENT_TYPE != Type.RISE) {
            return;
        }

        this.getParent().setModuleSpacing(this.productSansRegular.getFontHeight());
        this.getParent().setWidthComparator(this.productSansRegular);
        this.getParent().setEdgeOffset(10);

        // modules in the top right corner of the screen
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName()).replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

            String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                    .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");
            boolean out = NotificationComponent.current == null || NotificationComponent.time.finished(NotificationComponent.current.getThird()+210);
            final double destinationY = out ? 0 : 45;
            animation.run(destinationY);
            animation.setDuration(1000);
            animation.setEasing(Easing.EASE_OUT_EXPO);
            final double x = this.sidebar.getValue().getName().equals("Lavender") || this.sidebar.getValue().getName().equals("Off")
                    ? moduleComponent.getPosition().getX()+3 : moduleComponent.getPosition().getX() ;
            final double y = moduleComponent.getPosition().getY()- 5 + animation.getValue();
            final Color finalColor = moduleComponent.getColor();
            final double widthOffset = 2;
            final Interface interfaceModule = Client.INSTANCE.getModuleManager().get(Interface.class);
            if (this.normalBackGround || this.blurBackGround) {
                Runnable backgroundRunnable = () ->
                        RenderUtil.rectangle(x + 3.5 - widthOffset, (y - 2.5),
                                (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                this.getParent().moduleSpacing, interfaceModule.getRENDER3BG2Color());

                if (this.normalBackGround) {
                    NORMAL_RENDER_RUNNABLES.add(backgroundRunnable);
                }

                if (this.blurBackGround) {
                    NORMAL_BLUR_RUNNABLES.add(() ->
                            RenderUtil.rectangle(x + 3.5 - widthOffset, (y - 2.5),
                                    (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                    this.getParent().moduleSpacing, Color.BLACK));
                }

                // Draw the glow/shadow around the module
                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    if (glow || shadow) {

                        RenderUtil.rectangle(x + 3.5 - widthOffset, (y - 2.5),
                                (moduleComponent.nameWidth + moduleComponent.tagWidth) + 2 + widthOffset,
                                this.getParent().moduleSpacing, glow ? ColorUtil.withAlpha(finalColor, 164) : getTheme().getDropShadow());
                    }
                });
            }

            final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();
            final double xfix = (this.sidebar.getValue().getName().equals("Lavender") ? x + 4
                    : x+3);
            Runnable textRunnable = () -> {
                if (dropShadow.getValue()) {
                    productSansRegular.drawStringWithShadow(name, xfix, y, finalColor.getRGB());

                    if (hasTag) {
                        productSansRegular.drawStringWithShadow(tag, xfix + moduleComponent.getNameWidth() + 3, y, 0xFFCCCCCC);
                    }
                } else {
                    productSansRegular.drawString(name, (float) xfix, (float) y, finalColor.getRGB());

                    if (hasTag) {
                        productSansRegular.drawString(tag, (float) (xfix + moduleComponent.getNameWidth() + 3), (float) y, 0xFFCCCCCC);
                    }
                }
            };

            Runnable shadowRunnable = () -> {
                productSansRegular.drawString(name, (float) xfix, (float) y, Color.BLACK.getRGB());

                if (hasTag) {
                    productSansRegular.drawString(tag, (float) (xfix + moduleComponent.getNameWidth() + 3), (float) y, Color.BLACK.getRGB());
                }
            };


            NORMAL_RENDER_RUNNABLES.add(textRunnable);

            if (glow) {
                NORMAL_POST_BLOOM_RUNNABLES.add(textRunnable);
            } else if (shadow) {
                NORMAL_POST_BLOOM_RUNNABLES.add(shadowRunnable);
            }

            if (this.sidebar.getValue().getName().equals("Left")) {
                Runnable runnable3 = () -> Gui.drawRect2(x + 3 + moduleComponent.getNameWidth() + moduleComponent.getTagWidth() + 2, (y - 2f),
                        1, 12.9,  finalColor.getRGB());

                runnable3.run();
                NORMAL_POST_BLOOM_RUNNABLES.add(runnable3);
            }
            if (this.sidebar.getValue().getName().equals("Rise")) {
                Runnable runnable = () -> RenderUtil.roundedRectangle(x + 3 + moduleComponent.getNameWidth() + moduleComponent.getTagWidth() + 2, (y - 1.5f),
                        2, 9, 1, finalColor);

                runnable.run();
                NORMAL_POST_BLOOM_RUNNABLES.add(runnable);
            }
            if (this.sidebar.getValue().getName().equals("Lavender")) {
                Runnable runnable2 = () -> RenderUtil.roundedRectangle(x + 3 - widthOffset - 1.9, (y - 1.5f),
                        2, 9, 1, finalColor);

                runnable2.run();
                NORMAL_POST_BLOOM_RUNNABLES.add(runnable2);
            }
        }

        if (coordinates == null || username == null) return;

        NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
            if (glow || shadow) {
                // information of user in the bottom right corner of the screen
                final float x = event.getScaledResolution().getScaledWidth();
                final float y = event.getScaledResolution().getScaledHeight() - this.productSansRegular.getHeight() - 1;
                this.productSansRegular.drawStringWithShadow("Lavender - "+Client.name + " - " + GuiLogin.rank, x - userWidth - 5, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
//                this.productSansMedium18.drawStringWithShadow(username, x - nameWidth - 5, y, 0xFFCCCCCC);

                // coordinates of user in the bottom left corner of the screen
                final float coordX = 5;
                this.productSansRegular.drawStringWithShadow("XYZ:", coordX, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
                this.productSansRegular.drawStringWithShadow(coordinates, coordX + xyzWidth, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
            }

            if (!stopWatch.finished(2000)) {
                ParticleComponent.render();
            }
        });

        // information of user in the bottom right corner of the screen
        float x = event.getScaledResolution().getScaledWidth();
        float y = event.getScaledResolution().getScaledHeight() - this.productSansRegular.getHeight() - 1;
        this.productSansRegular.drawStringWithShadow("Lavender - "+Client.name+ " - " + GuiLogin.rank, x - userWidth - 5, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
//        this.productSansMedium18.drawStringWithShadow(username, x - nameWidth - 5, y, 0xFFCCCCCC);

        // coordinates of user in the bottom left corner of the screen
        final float coordX = 5;
        this.productSansRegular.drawStringWithShadow("XYZ:", coordX, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
        this.productSansRegular.drawStringWithShadow(coordinates, coordX + xyzWidth, y - (mc.currentScreen instanceof GuiChat ?  openingAnimation.getOutput()*13 : 0), 0xFFCCCCCC);
        switch (logoMode.getValue().getName()) {
            case "Normal":
                // title in the top left corner of the screen
                //this.productSansMedium36.drawString("Lavender", 6, 16, logoColor.getRGB());

                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.resetColor();
                    GradientUtil.applyGradientHorizontal((float) 6, (float) 11, this.productSansMedium36.width("Lavender"), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                        RenderUtil.setAlphaLimit(0);
                        this.productSansMedium36.drawStringWithShadow("Lavender", 6, 11, 0);
                    });
                });
                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.resetColor();
                    GradientUtil.applyGradientHorizontal((float) 6, (float) 11, this.productSansMedium36.width("Lavender"), 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                        RenderUtil.setAlphaLimit(0);
                        this.productSansMedium36.drawStringWithShadow("Lavender", 6, 11, 0);
                    });
                });
                break;
            case "Skeet":
                String text = "§fL§ravender§f" + " - " + Client.name + " - " + (mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData().serverIP) + " - "
                        + Minecraft.getDebugFPS() + "fps";

                x = 4.5f;
                y = 4.5f;

                Color lineColor = new Color(59, 57, 57).darker();
                RenderUtil.rectangle(x, y, FontManager.getTahoma(16).width(text) + 7, 18.5, new Color(59, 57, 57));

                RenderUtil.rectangle(x + 2.5, y + 2.5, FontManager.getTahoma(16).width(text) + 2, 13, new Color(23, 23, 23));

                // Top small bar
                RenderUtil.rectangle(x + 1, y + 1, FontManager.getTahoma(16).width(text) + 5, .5, lineColor);

                // Bottom small bar
                RenderUtil.rectangle(x + 1, y + 17, FontManager.getTahoma(16).width(text) + 5, .5, lineColor);

                // Left bar
                RenderUtil.rectangle(x + 1, y + 1.5, .5, 16, lineColor);

                // Right Bar
                RenderUtil.rectangle((x + 1.5) + FontManager.getTahoma(16).width(text) + 4, y + 1.5, .5, 16, lineColor);

                // Lowly saturated rainbow bar
                GradientUtil.drawGradientLR(x + 2.5f, y + 1.9f, FontManager.getTahoma(16).width(text) + 2, 1, 1, getTheme().getFirstColor(), getTheme().getSecondColor());

                // Bottom of the rainbow bar
                RenderUtil.rectangle(x + 2.5, y + 16, FontManager.getTahoma(16).width(text) + 2, .5, lineColor);
                FontManager.getTahoma(16).drawString(text, x + 4.5f, y + 6.9f, getTheme().getSecondColor().getRGB());
                break;
            case "NeverLose":
                if (mc.thePlayer == null) return;
                Font m22 = FontManager.getNunitoBold(20), t18 = FontManager.getPoppinsRegular(18);
                String str = String.format(" | %s fps | %s | %s",
                        Minecraft.getDebugFPS(), username,
                        mc.isSingleplayer() || mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP);
                String Clientname = Client.NAME;
                float nw = m22.width(Clientname);

                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    RenderUtil.roundedRectangle(4, 4.5F, nw + t18.width(str) + 8f, t18.height() + 6, 4, getTheme().getBackgroundShade());
                    t18.drawString(str, 8F + nw, 12F, -1);
                    m22.drawString(Clientname, 8F, 11.5, new Color(20, 85, 145).getRGB());
                    m22.drawString(Clientname, 8.5, 11F, -1);
                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    RenderUtil.roundedRectangle(4, 4.5F, nw + t18.width(str) + 8f, t18.height() + 6, 4, Color.BLACK);
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.roundedRectangle(4, 4.5F, nw + t18.width(str) + 8f, t18.height() + 6, 4,  getTheme().getDropShadow());
                });
                break;
            case "Moon" :
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                GradientUtil.applyGradientVertical(4, 4.5f, 90.0F, 90.0F, 1.0f, getClientColors()[0], getClientColors()[1], () ->
                        RenderUtil.drawImage(new ResourceLocation("lavender/icons/moon.png"),4, 4.5f, 90, 90,-1 ));
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                break;
            case "Lavender":
                m22 = FontManager.getNunitoBold(20);
                t18 = FontManager.getPoppinsRegular(18);
                String Lavenderstr = String.format(" | %s fps | %s | %s",
                        Minecraft.getDebugFPS(), username,
                        mc.isSingleplayer() || mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP);
                String LavenderClientname = "Lavender";
                float Lavendernw = m22.width(LavenderClientname);

                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    RenderUtil.roundedRectangle(4, 4.5F, Lavendernw + t18.width(Lavenderstr) + 8f, t18.height() + 6, 4, getTheme().getBackgroundShade());
                    t18.drawString(Lavenderstr, 8F + Lavendernw, 12F, -1);
                    RenderUtil.resetColor();
                    GradientUtil.applyGradientHorizontal(8, 11F, Lavendernw, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                        RenderUtil.setAlphaLimit(0);
                        m22.drawString(LavenderClientname, 8F, 11, 0);
                    });
                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    RenderUtil.roundedRectangle(4, 4.5F, Lavendernw + t18.width(Lavenderstr) + 8f, t18.height() + 6, 4, Color.BLACK);
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.roundedRectangle(4, 4.5F, Lavendernw + t18.width(Lavenderstr) + 8f, t18.height() + 6, 4,  getTheme().getDropShadow());
                });
                break;
            case "Lavender2":
                m22 = FontManager.getNunitoBold(20);
                t18 = FontManager.getPoppinsRegular(18);
                String Lavenderstr2 = String.format(" | %s fps | %s | %s", Minecraft.getDebugFPS(), username, mc.isSingleplayer()
                        || mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP);
                String LavenderClientname2 = "Lavender";
                float Lavendernw2 = m22.width(LavenderClientname2);

                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    RenderUtil.rectangle(4, 4.5F, Lavendernw2 + t18.width(Lavenderstr2) + 1f, t18.height() +3, new Color(0, 0, 0,30));

                    FontManager.getNunitoBold(18).drawString(Lavenderstr2, 9F + Lavendernw2, 10F, new Color(255, 255, 255, 210).getRGB());
                    RenderUtil.resetColor();
                    GradientUtil.applyGradientHorizontal(9, 9f, Lavendernw2, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                        RenderUtil.setAlphaLimit(0);
                        FontManager.getNunitoBold(20).drawString(LavenderClientname2, 9F, 9, 0);

                    });
                    RenderUtil.roundedRectangle(4, 8F, 2, t18.height()-3.5,2,getClientColors()[0]);

                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    RenderUtil.rectangle(4, 4.5F, Lavendernw2 + t18.width(Lavenderstr2) + 1f, t18.height() +3, Color.BLACK);
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.rectangle(4, 4.5F, Lavendernw2 + t18.width(Lavenderstr2) + 1f, t18.height() +3,  getTheme().getDropShadow());
                });

                break;
            case "Lavender3":
                m22 = FontManager.getNunitoBold(20);
                t18 = FontManager.getPoppinsRegular(18);
                String Lavenderstr3 = String.format(" | %s fps | %s | %s",
                        Minecraft.getDebugFPS(), username, mc.isSingleplayer() || mc.getCurrentServerData() == null ? "singleplayer"
                                : mc.getCurrentServerData().serverIP);
                String LavenderClientname3 = "Lavender";
                float Lavendernw3 = FontManager.getNunitoBold(20).width(LavenderClientname3);

                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    RenderUtil.rectangle(4, 4.5F, Lavendernw3+66F
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                                    +FontManager.getNunitoBold(18).width("Fps: " + Minecraft.getDebugFPS())
                            , t18.height() +4,
                            getTheme().getBackgroundShade());

                    //////////////////////////////////
                    FontManager.getNunitoBold(18).drawString("Bps: " +  calculateBPS()
                            , 13F + Lavendernw3, 10.5F,
                            new Color(255, 255, 255, 210).getRGB());

                    RenderUtil.circle(19F + Lavendernw3 + FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                            , 10F, 8, 360, false, new Color(0, 0, 0, 70));
                    RenderUtil.circle(19F + Lavendernw3 + FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                            , 10F, 8, (calculateBPS() / 10) * 360, false,   Color.white);
                    ////////////////////////////////////////
                    FontManager.getNunitoBold(18).drawString("Ping: " + getPing(mc.thePlayer)
                            , 33F + Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS()), 10.5F,
                            new Color(255, 255, 255, 210).getRGB());

                    RenderUtil.circle(38F + Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                            , 10F, 8, 360, false, new Color(0, 0, 0, 70));
                    RenderUtil.circle(38 + Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                            , 10F, 8, (getPing(mc.thePlayer)) * 0.5, false,  Color.white);
                    ///////////////////////////////////////////////////////////

                    FontManager.getNunitoBold(18).drawString("Fps: " + Minecraft.getDebugFPS()
                            , 53F + Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer)), 10.5F,
                            new Color(255, 255, 255, 210).getRGB());

                    RenderUtil.circle(58F + Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                                    +FontManager.getNunitoBold(18).width("Fps: " + Minecraft.getDebugFPS())
                            , 10F, 8, 360, false, new Color(0, 0, 0, 70));
                    RenderUtil.circle(58 +  Lavendernw3
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                                    +FontManager.getNunitoBold(18).width("Fps: " + Minecraft.getDebugFPS())
                            , 10F, 8, (Minecraft.getDebugFPS()) * 0.8, false,  Color.white);


                    ////////////////////////////////////////////////////////////
                    RenderUtil.resetColor();
                    GradientUtil.applyGradientHorizontal(9, 9.5f, Lavendernw3, 20, 1, getClientColors()[0], getClientColors()[1], () -> {
                        RenderUtil.setAlphaLimit(0);
                        FontManager.getNunitoBold(20).drawString(LavenderClientname3, 9F, 9.5, 0);

                    });
                    RenderUtil.roundedRectangle(4, 8F, 2, t18.height()-3.5,2,getClientColors()[0]);

                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    RenderUtil.rectangle(4, 4.5F, Lavendernw3+66F
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                                    +FontManager.getNunitoBold(18).width("Fps: " + Minecraft.getDebugFPS())
                            , t18.height() +4,
                            Color.BLACK);
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    // title in the top left corner of the screen
                    RenderUtil.rectangle(4, 4.5F, Lavendernw3+66F
                                    +FontManager.getNunitoBold(18).width("Bps: " +  calculateBPS())
                                    + FontManager.getNunitoBold(18).width("Ping: " +  getPing(mc.thePlayer))
                                    +FontManager.getNunitoBold(18).width("Fps: " + Minecraft.getDebugFPS())
                            , t18.height() +4,
                            getTheme().getDropShadow());
                });
                break;
            case "Lavender4":
                if (isNull()) return;
                m22 = FontManager.getNunitoBold(20);
                t18 = FontManager.getNunitoBold(18);
                String Lavenderstr4 = String.format(" %s fps | %s | %s", Minecraft.getDebugFPS(), username, mc.isSingleplayer()
                        || mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP);
                float width = 8;
                Interface interfacemodule = getModule(Interface.class);
                String LavenderClientname4 = interfacemodule.Render3cn.getValue() ? "薰衣草" : "Lavender";
                float Lavendernw4 = interfacemodule.Render3cn.getValue() ? m22.width(LavenderClientname4)+10 : m22.width(LavenderClientname4);
                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    ///bg
                    if ((double)interfacemodule.radius.getValue() == 0f) RenderUtil.rectangle(4, 4.5F,
                            FontManager.getLogoIcon(20).width("N")+Lavendernw4 + t18.width(Lavenderstr4)+width+10 ,
                            t18.height() +4, interfacemodule.getRENDER3BG2Color());
                    else
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw4 + t18.width(Lavenderstr4)+width+9 ,
                                t18.height() +4, (double)interfacemodule.radius.getValue(), interfacemodule.getRENDER3BG2Color());
                    ///bg2
                    if ((double)interfacemodule.radius.getValue() == 0f)
                        RenderUtil.rectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+ Lavendernw4+4.5+width,t18.height() +4, interfacemodule.getRENDER3BgColor());
                    else
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw4+4.5+width,t18.height() +4, (double)interfacemodule.radius.getValue(),interfacemodule.getRENDER3BgColor());

                    FontManager.getNunitoBold(18).drawString(Lavenderstr4,
                            FontManager.getLogoIcon(20).width("N")+10F+width + Lavendernw4, 11F,
                            new Color(255, 255, 255, 210).getRGB());

                    if(interfacemodule.Render3cn.getValue())
                        com.alan.clients.fontRender.FontManager.arial17bold.drawString(LavenderClientname4,
                                FontManager.getLogoIcon(20).width("N")+width+3F, 10, interfacemodule.getRENDER3fontColor().getRGB());else
                    FontManager.getNunitoBold(20).drawString(LavenderClientname4,
                            FontManager.getLogoIcon(20).width("N")+width+3F, 10,
                            interfacemodule.getRENDER3fontColor().getRGB());


                    FontManager.getLogoIcon(22).drawString("N", 9F, 11, interfacemodule.getRENDER3fontColor().getRGB());

                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    if((double)interfacemodule.radius.getValue() == 0f){
                        RenderUtil.rectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw4 + t18.width(Lavenderstr4)+width+10, t18.height() +4, Color.BLACK);
                    }else {
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw4 + t18.width(Lavenderstr4)+width+10, t18.height() +4,
                                (double)interfacemodule.radius.getValue(), Color.BLACK);
                    }
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    if(!interfacemodule.BLOOM.getValue()) return;
                    if((double)interfacemodule.radius.getValue() == 0f){
                        RenderUtil.rectangle(4, 4.5F, FontManager.getLogoIcon(20).width("N")+Lavendernw4 +
                                        t18.width(Lavenderstr4)+width+10,
                                t18.height() +4, interfacemodule.getbloomBG2Color());
                    }else {
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw4 + t18.width(Lavenderstr4)+width+10, t18.height() +4,
                                (double)interfacemodule.radius.getValue(), interfacemodule.getbloomBG2Color());
                    }
                });
                break;
            case "Lavender5":
                if (isNull()) return;
                m22 = FontManager.getNunitoBold(20);
                t18 = FontManager.getNunitoBold(18);
                String Lavenderstr5 = String.format(" | %s fps | %s | %s", Minecraft.getDebugFPS(), username, mc.isSingleplayer()
                        || mc.getCurrentServerData() == null ? "singleplayer" : mc.getCurrentServerData().serverIP);
                width = 8;
                Interface interfacemodule2 = getModule(Interface.class);
                String LavenderClientname5 = interfacemodule2.Render3cn.getValue() ? "薰衣草" : "Lavender";
                float Lavendernw5 = interfacemodule2.Render3cn.getValue() ? m22.width(LavenderClientname5)+10 : m22.width(LavenderClientname5);
                NORMAL_POST_RENDER_RUNNABLES.add(() -> {
                    ///bg
                    if ((double)interfacemodule2.radius.getValue() == 0f) RenderUtil.rectangle(4, 4.5F,
                            FontManager.getLogoIcon(20).width("N")+Lavendernw5 + t18.width(Lavenderstr5)+width+10 ,
                            t18.height() +4, interfacemodule2.getRENDER3BG2Color());
                    else
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw5 + t18.width(Lavenderstr5)+width+9 ,
                                t18.height() +4, (double)interfacemodule2.radius.getValue(), interfacemodule2.getRENDER3BG2Color());
                    ///bg2
                    if ((double)interfacemodule2.radius.getValue() == 0f)
                        RenderUtil.rectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+2+width,t18.height() +4,
                                interfacemodule2.getRENDER3BgColor());
                    else
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+2+width,t18.height() +4,
                                (double)interfacemodule2.radius.getValue(),interfacemodule2.getRENDER3BgColor());

                    FontManager.getNunitoBold(18).drawString(Lavenderstr5,
                            FontManager.getLogoIcon(20).width("N")+8F+width + Lavendernw5, 11F,
                            new Color(255, 255, 255, 210).getRGB());

                    if(interfacemodule2.Render3cn.getValue())
                        com.alan.clients.fontRender.FontManager.arial17bold.drawString(LavenderClientname5,
                                FontManager.getLogoIcon(20).width("N")+width+9F, 10,
                                new Color(255, 255, 255, 210).getRGB());else
                        FontManager.getNunitoBold(20).drawString(LavenderClientname5,
                                FontManager.getLogoIcon(20).width("N")+width+9F, 10,
                                new Color(255, 255, 255, 210).getRGB());


                    FontManager.getLogoIcon(22).drawString("N", 9F, 11, interfacemodule2.getRENDER3fontColor().getRGB());

                });


                NORMAL_BLUR_RUNNABLES.add(() -> {
                    if((double)interfacemodule2.radius.getValue() == 0f){
                        RenderUtil.rectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw5 + t18.width(Lavenderstr5)+width+10, t18.height() +4, Color.BLACK);
                    }else {
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw5 + t18.width(Lavenderstr5)+width+10, t18.height() +4,
                                (double)interfacemodule2.radius.getValue(), Color.BLACK);
                    }
                });

                NORMAL_POST_BLOOM_RUNNABLES.add(() -> {
                    if(!interfacemodule2.BLOOM.getValue()) return;
                    if((double)interfacemodule2.radius.getValue() == 0f){
                        RenderUtil.rectangle(4, 4.5F, FontManager.getLogoIcon(20).width("N")+Lavendernw5 +
                                        t18.width(Lavenderstr5)+width+10,
                                t18.height() +4, interfacemodule2.getbloomBG2Color());
                    }else {
                        RenderUtil.roundedRectangle(4, 4.5F,
                                FontManager.getLogoIcon(20).width("N")+Lavendernw5 + t18.width(Lavenderstr5)+width+10, t18.height() +4,
                                (double)interfacemodule2.radius.getValue(), interfacemodule2.getbloomBG2Color());
                    }
                });
                break;
        }

        if (mc.thePlayer.ticksExisted % 150 == 0) {
            stopWatch.reset();
        }
    };

    public static int getPing(final EntityPlayer entityPlayer) {
        if(entityPlayer == null)
            return 0;

        final NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID());

        return networkPlayerInfo == null ? 0 : networkPlayerInfo.getResponseTime();
    }
    private double calculateBPS() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }
    @EventLink()
    public final Listener<KillEvent> onKill = event -> {
        if (Client.CLIENT_TYPE != Type.RISE) return;

        if (!stopWatch.finished(2000) && this.particles.getValue()) {
            for (int i = 0; i <= 10; i++) {
                ParticleComponent.add(new Particle(new Vector2f(0, 0),
                        new Vector2f((float) Math.random(), (float) Math.random())));
            }
        }

        stopWatch.reset();
    };

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        if (isNull()) return;
        if (Client.CLIENT_TYPE != Type.RISE || mc.thePlayer == null || !mc.getNetHandler().doneLoadingTerrain) return;

        threadPool.execute(() -> {
            glow = this.shader.getValue().getName().equals("Glow");
            shadow = this.shader.getValue().getName().equals("Shadow");

            username = mc.getSession() == null || mc.getSession().getUsername() == null ? "null" : mc.getSession().getUsername();
            userWidth = this.productSansRegular.getStringWidth("Lavender - "+Client.name+ " - " + GuiLogin.rank) + 2;
            coordinates = (int) mc.thePlayer.posX + ", " + (int) mc.thePlayer.posY + ", " + (int) mc.thePlayer.posZ;
            xyzWidth = this.productSansRegular.getStringWidth("XYZ:") + 2;

            normalBackGround = background.getValue().getName().equals("Normal");
            blurBackGround = normalBackGround || background.getValue().getName().equals("Blur");

            // modules in the top right corner of the screen
            for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                final boolean hasTag = !moduleComponent.getTag().isEmpty() && this.getParent().suffix.getValue();

                String name = (this.getParent().lowercase.getValue() ? moduleComponent.getTranslatedName().toLowerCase() : moduleComponent.getTranslatedName()).replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

                String tag = (this.getParent().lowercase.getValue() ? moduleComponent.getTag().toLowerCase() : moduleComponent.getTag())
                        .replace(getParent().getRemoveSpaces().getValue() ? " " : "", "");

                Color color = this.getTheme().getFirstColor();
                switch (this.colorMode.getValue().getName()) {
                    case "Breathe": {
                        double factor = this.getTheme().getBlendFactor(new Vector2d(0, 0));
                        color = mixColors(this.getTheme().getFirstColor(), this.getTheme().getSecondColor(), factor);
                        break;
                    }
                    case "Fade": {
                        color = this.getTheme().getAccentColor(new Vector2d(0, moduleComponent.getPosition().getY()));
                        break;
                    }
                }

                moduleComponent.setColor(color);

                moduleComponent.setNameWidth(productSansRegular.getStringWidth(name));
                moduleComponent.setTagWidth(hasTag ? (productSansRegular.getStringWidth(tag) + 4) : 0);
            }
        });
    };
}
