package com.alan.clients.module.impl.render;

import com.alan.clients.Client;
import com.alan.clients.Type;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.fontRender.RapeMasterFontManager;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.module.impl.combat.KillAura;
import com.alan.clients.module.impl.other.MusicPlayer;
import com.alan.clients.module.impl.player.Manager;
import com.alan.clients.module.impl.player.Stealer;
import com.alan.clients.module.impl.render.interfaces.LavenderInterface;
import com.alan.clients.module.impl.render.interfaces.ModernInterface;
import com.alan.clients.module.impl.render.interfaces.ModuleComponent;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.motion.PreUpdateEvent;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import com.alan.clients.noti.NotificationManager;
import com.alan.clients.noti.NotificationType;
import com.alan.clients.ui.cloudmusic.MusicManager;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.math.MathUtil;
import com.alan.clients.util.render.ColorUtil;
import com.alan.clients.util.vector.Vector2d;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Value;
import com.alan.clients.value.impl.*;
import javafx.scene.media.MediaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import util.time.StopWatch;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ModuleInfo(name = "module.render.interface.name", description = "module.render.interface.description", category = Category.RENDER, autoEnabled = true)
public final class Interface extends Module {

    private final ModeValue mode = new ModeValue("Mode", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new ModernInterface("Modern", (Interface) this.getParent()));
        add(new LavenderInterface("LavenderInterface", (Interface) this.getParent()));
        setDefault("Modern");
    }};

    private final ModeValue modulesToShow = new ModeValue("Modules to Show", this, () -> Client.CLIENT_TYPE != Type.RISE) {{
        add(new SubMode("All"));
        add(new SubMode("Exclude render"));
        add(new SubMode("Only bound"));
        setDefault("Exclude render");
    }};
    public final BooleanValue limitChatWidth = new BooleanValue("Limit Chat Width", this, false);
    public final BooleanValue smoothHotBar = new BooleanValue("Smooth Hot Bar", this, true);
    public final BooleanValue togglenoti = new BooleanValue("ToggleNoti",this,true);
    public final BooleanValue togglesounds = new BooleanValue("ToggleSounds",this,true);
    public BooleanValue suffix = new BooleanValue("Suffix", this, true, () -> Client.CLIENT_TYPE != Type.RISE);
    public BooleanValue lowercase = new BooleanValue("Lowercase", this, false, () -> Client.CLIENT_TYPE != Type.RISE);
    public BooleanValue removeSpaces = new BooleanValue("Remove Spaces", this, false, () -> Client.CLIENT_TYPE != Type.RISE);


    public  BooleanValue shaders = new BooleanValue("Shaders", this, true);
    private final BooleanValue chatblur = new BooleanValue("Chat Shaders", this, true, () -> !(shaders.getValue()));
    public  BooleanValue BLOOM = new BooleanValue("BLOOM", this, false, () -> !(shaders.getValue()));
    public ModeValue Render3blurBgColor = new ModeValue("Render3 Blur Bg Color", this, () -> !(shaders.getValue())) {{
        add(new SubMode("White"));
        add(new SubMode("Dark Gray"));
        setDefault("White");
    }};
    public ModeValue BloomBgColor = new ModeValue("Render3 Bloom Bg Color", this, () -> !(shaders.getValue())) {{
        add(new SubMode("White"));
        add(new SubMode("Dark Gray"));
        setDefault("White");
    }};
    public  NumberValue radius = new NumberValue("Radius", this, 5.0, 0, 10, 1);
    public ModeValue Render3BgColor = new ModeValue("Render3 Bg Color", this) {{
        add(new SubMode("White"));
        add(new SubMode("Dark Gray"));
        add(new SubMode("Deep Blue Purple"));
        add(new SubMode("Custom"));
        setDefault("White");
    }};
    public final ColorValue colorValue = new ColorValue("Color",this, new Color(255, 255, 255), () -> !(Render3BgColor.getValue().getName().equals("Custom")));
    public  BooleanValue Render3cn = new BooleanValue("Render3 CN Font", this, false);
    private ArrayList<ModuleComponent> allModuleComponents = new ArrayList<>(),
            activeModuleComponents = new ArrayList<>();
    private SubMode lastFrameModulesToShow = (SubMode) modulesToShow.getValue();

    private final StopWatch stopwatch = new StopWatch();
    private final StopWatch updateTags = new StopWatch();

    public RapeMasterFontManager widthComparator = FontManager.arial20;
    public float moduleSpacing = 12, edgeOffset;
    private Color bg,font,blurbg,bloombg;
    public Interface() {
        createArrayList();
    }
    public static Color mixColors2(Color color1, Color color2) {
        return ColorUtil.interpolateColorsBackAndForth(15, 1, color1, color2, false);
    }
    public void createArrayList() {
        allModuleComponents.clear();
        Client.INSTANCE.getModuleManager().getAll().stream()
                .sorted(Comparator.comparingDouble(module -> -widthComparator.getStringWidth(Localization.get(module.getDisplayName()))))
                .forEach(module -> allModuleComponents.add(new ModuleComponent(module)));
    }

    public void sortArrayList() {
//        ArrayList<ModuleComponent> components = new ArrayList<>();
//        Client.INSTANCE.getModuleManager().getAll().forEach(module -> components.add(new ModuleComponent(module)));
//
        activeModuleComponents = allModuleComponents.stream()
                .filter(moduleComponent -> moduleComponent.getModule().shouldDisplay(this))
                .sorted(Comparator.comparingDouble(module -> -module.getNameWidth() - module.getTagWidth()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public Color getRENDER3BgColor() {
        if(Render3BgColor.getValue().getName().equals("White")){
            return bg = new Color(255, 255, 255, 185);
        }else if(Render3BgColor.getValue().getName().equals("Dark Gray")){
            return bg = new Color(41, 41, 51, 185);
        }else if(Render3BgColor.getValue().getName().equals("Deep Blue Purple")) {
            return bg = new Color(21, 20, 51, 185);
        }else if(Render3BgColor.getValue().getName().equals("Custom")) {
            return bg = new Color(this.colorValue.getValue().getRGB());
        }
        return bg;
    }
    public Color getRENDER3fontColor() {
        if(Render3BgColor.getValue().getName().equals("White")){
            return font = new Color(0, 0, 0, 210);
        } else if(Render3BgColor.getValue().getName().equals("Dark Gray")){
            return font = new Color(255, 255, 255, 210);
        }else if(Render3BgColor.getValue().getName().equals("Deep Blue Purple")) {
            return font = new Color(255, 255, 255, 210);
        }
        return font;
    }
    public Color getRENDER3BG2Color() {
        if(Render3blurBgColor.getValue().getName().equals("White")){
            return blurbg = new Color(210, 210, 210, 30);
        } else if(Render3blurBgColor.getValue().getName().equals("Dark Gray")){
            return blurbg = new Color(37, 37, 37, 70);
        }
        return blurbg;
    }
    public Color getbloomBG2Color() {
        if(BloomBgColor.getValue().getName().equals("White")){
            return bloombg = new Color(187, 187, 187, 150);
        } else if(BloomBgColor.getValue().getName().equals("Dark Gray")){
            return bloombg = new Color(0, 0, 0, 160);
        }
        return bloombg;
    }
    StopWatch lastUpdate = new StopWatch();

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (lastUpdate.finished(1000)) {
            InstanceAccess.threadPool.execute(() -> {
                for (final ModuleComponent moduleComponent : allModuleComponents) {
                    moduleComponent.setTranslatedName(Localization.get(moduleComponent.getModule().getDisplayName()));
                }
            });
        }
    };
    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        ScaledResolution sr = new ScaledResolution(InstanceAccess.mc);
        NORMAL_POST_RENDER_RUNNABLES.add(() -> mc.ingameGUI.getChatGUI().renderChatBox( getTheme().getBackgroundShade(),true));
        if(chatblur.getValue()) {//聊天栏blur
            NORMAL_BLUR_RUNNABLES.add(() -> mc.ingameGUI.getChatGUI().renderChatBox(Color.BLACK,false));
            NORMAL_POST_BLOOM_RUNNABLES.add(() -> mc.ingameGUI.getChatGUI().renderChatBox(getTheme().getDropShadow(),false));
        }
        if (MusicManager.INSTANCE.lyric && MusicManager.INSTANCE.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
            RapeMasterFontManager lyricFont = FontManager.arial22;
            int addonYlyr = Client.INSTANCE.getModuleManager().get(MusicPlayer.class).musicPosYlyr.getValue().intValue();
            //Lyric
            int borderCol = new Color(238, 171, 227).getRGB();
            int col = new Color(0xffE8DEFF).getRGB();

            lyricFont.drawCenterOutlinedString(MusicManager.INSTANCE.lrcCur.contains("_EMPTY_") ? "": MusicManager.INSTANCE.lrcCur, sr.getScaledWidth() / 2f - 0.5f, sr.getScaledHeight() - 140 - 80 + addonYlyr, borderCol, col);

            lyricFont.drawCenterOutlinedString(MusicManager.INSTANCE.tlrcCur.contains("_EMPTY_") ? "": MusicManager.INSTANCE.tlrcCur, sr.getScaledWidth() / 2f, sr.getScaledHeight() - 120 + 0.5f - 80 + addonYlyr, new Color(0x595959).getRGB(), col);

        }
        if ((MusicManager.showMsg)) {
            NotificationManager.post(NotificationType.INFO,"正在播放",MusicManager.INSTANCE.getCurrentTrack().name + " - " + MusicManager.INSTANCE.getCurrentTrack().artists,15);
            MusicManager.showMsg = false;
        }
        //歌曲封面图片
       /* if (MusicManager.INSTANCE.currentTrack != null) {
            if (MusicManager.INSTANCE.getArt(MusicManager.INSTANCE.currentTrack.id) != null) {
                GL11.glPushMatrix();
                RenderUtil.drawImage2(MusicManager.INSTANCE.getArt(MusicManager.INSTANCE.currentTrack.id), sr.getScaledWidth() / 2f - 0.5f, sr.getScaledHeight() - 140 - 80, 100, 100, 1f);
                GL11.glPopMatrix();
            }
        } */
        for (final ModuleComponent moduleComponent : allModuleComponents) {
            if (moduleComponent.getModule().isEnabled()) {
                moduleComponent.animationTime = Math.min(moduleComponent.animationTime + stopwatch.getElapsedTime() / 100.0F, 10);
            } else {
                moduleComponent.animationTime = Math.max(moduleComponent.animationTime - stopwatch.getElapsedTime() / 100.0F, 0);
            }
        }

        InstanceAccess.threadPool.execute(() -> {
            if (updateTags.finished(50)) {
                updateTags.reset();

                for (final ModuleComponent moduleComponent : activeModuleComponents) {
                    if (moduleComponent.animationTime == 0) {
                        continue;
                    }

                    for (final Value<?> value : moduleComponent.getModule().getValues()) {
                        if (value instanceof ModeValue) {
                            final ModeValue modeValue = (ModeValue) value;

                            moduleComponent.setTag(modeValue.getValue().getName());
                            break;
                        }

                        moduleComponent.setTag("");
                    }
                }

                this.sortArrayList();
            }

            final float screenWidth = event.getScaledResolution().getScaledWidth();
            final Vector2f position = new Vector2f(0, 0);
            for (final ModuleComponent moduleComponent : activeModuleComponents) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                moduleComponent.targetPosition = new Vector2d(screenWidth - moduleComponent.getNameWidth() - moduleComponent.getTagWidth(), position.getY());

                if (!moduleComponent.getModule().isEnabled() && moduleComponent.animationTime < 10) {
                    moduleComponent.targetPosition = new Vector2d(screenWidth + moduleComponent.getNameWidth() + moduleComponent.getTagWidth(), position.getY());
                } else {
                    position.setY(position.getY() + moduleSpacing);
                }

                float offsetX = edgeOffset;
                float offsetY = edgeOffset;

                moduleComponent.targetPosition.x -= offsetX;
                moduleComponent.targetPosition.y += offsetY;

                if (Math.abs(moduleComponent.getPosition().getX() - moduleComponent.targetPosition.x) > 0.5 || Math.abs(moduleComponent.getPosition().getY() - moduleComponent.targetPosition.y) > 0.5 || (moduleComponent.animationTime != 0 && moduleComponent.animationTime != 10)) {
                    for (int i = 0; i < stopwatch.getElapsedTime(); ++i) {
                        moduleComponent.position.x = MathUtil.lerp(moduleComponent.position.x, moduleComponent.targetPosition.x, 1.5E-2F);
                        moduleComponent.position.y = MathUtil.lerp(moduleComponent.position.y, moduleComponent.targetPosition.y, 1.5E-2F);
                    }
                } else {
                    moduleComponent.position = moduleComponent.targetPosition;
                }
            }

            stopwatch.reset();
        });
    };
    public static void addButtons(List<GuiButton> buttonList) {
        for (ModuleButton mb : ModuleButton.values()) {
                buttonList.add(mb.getButton());
        }
    }

    public static void updateButtonStatus() {
        for (ModuleButton mb : ModuleButton.values()) {
            mb.getButton().enabled = Client.INSTANCE.getModuleManager().get(mb.getModule()).isEnabled();
        }
    }

    public static void handleActionPerformed(GuiButton button) {
        for (ModuleButton mb : ModuleButton.values()) {
            if (mb.getButton() == button) {
                Module m = Client.INSTANCE.getModuleManager().get(mb.getModule());
                if (m.isEnabled()) {
                    m.toggle();
                }
                break;
            }
        }
    }
    @Getter
    @AllArgsConstructor
    public enum ModuleButton {
        AURA(KillAura.class, new GuiButton(2461, 3, 4, 120, 20, "Disable KillAura")),
        INVMANAGER(Manager.class,new GuiButton(2462, 3, 26, 120, 20, "Disable InvManager")),
        CHESTSTEALER(Stealer.class, new GuiButton(2463, 3, 48, 120, 20, "Disable ChestStealer"));

        private final Class<? extends Module> module;
        private final GuiButton button;
    }
}
