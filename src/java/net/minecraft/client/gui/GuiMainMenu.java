package net.minecraft.client.gui;

import com.alan.clients.util.account.microsoft.MicrosoftLogin;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.CustomPanorama;
import net.optifine.CustomPanoramaProperties;
import net.optifine.reflect.Reflector;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    private static final AtomicInteger field_175373_f = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private final float updateCounter;
    private String splashText;
    private GuiButton buttonResetDemo;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private final boolean field_175375_v;
    private final Object threadLock;
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private ResourceLocation backgroundTexture;
    private GuiButton realmsButton;
    private boolean field_183502_L;
    private GuiScreen field_183503_M;
    private GuiButton modButton;
    private GuiScreen modUpdateNotification;

    public GuiMainMenu() {
        block18: {
            this.field_175375_v = true;
            this.threadLock = new Object();
            this.openGLWarning2 = field_96138_a;
            this.field_183502_L = false;
            this.splashText = "missingno";
            BufferedReader bufferedreader = null;
            try {
                try {
                    String s;
                    ArrayList list = Lists.newArrayList();
                    bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));
                    while ((s = bufferedreader.readLine()) != null) {
                        if ((s = s.trim()).isEmpty()) continue;
                        list.add(s);
                    }
                    if (!list.isEmpty()) {
                        do {
                            this.splashText = (String)list.get(RANDOM.nextInt(list.size()));
                        } while (this.splashText.hashCode() == 125780783);
                    }
                }
                catch (IOException iOException) {
                    if (bufferedreader != null) {
                        try {
                            bufferedreader.close();
                        }
                        catch (IOException iOException2) {}
                    }
                    break block18;
                }
            }
            catch (Throwable throwable) {
                if (bufferedreader != null) {
                    try {
                        bufferedreader.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                throw throwable;
            }
            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        this.updateCounter = RANDOM.nextFloat();
        this.openGLWarning1 = "";
        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            this.openGLWarning1 = I18n.format("title.oldgl1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldgl2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    private boolean func_183501_a() {
        return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && this.field_183503_M != null;
    }

    public void updateScreen() {
        ++this.panoramaTimer;
        if (this.func_183501_a()) {
            this.field_183503_M.updateScreen();
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) == 24) {
            this.splashText = "Merry X-mas!";
        } else if (calendar.get(Calendar.MONTH) + 1 == 1 && calendar.get(Calendar.DATE) == 1) {
            this.splashText = "Happy new year!";
        } else if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31) {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }
        int i = 24;
        int j = this.height / 4 + 48;
        if (this.mc.isDemo()) {
            this.addDemoButtons(j, 24);
        } else {
            this.addSingleplayerMultiplayerButtons(j, 24);
        }
        this.buttonList.add(new GuiButton(0, 215, this.height - 25, 98, 20, I18n.format("menu.options")));
        this.buttonList.add(new GuiButton(4, this.width - 104, this.height - 25, 98, 20, I18n.format("menu.quit")));
        Object object = this.threadLock;
        synchronized (object) {
            this.field_92023_s = this.fontRendererObj.width(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.width(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = this.buttonList.get(0).yPosition - 24;
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }
        Realms.setConnectedToRealms(false);
        if (Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && !this.field_183502_L) {
            RealmsBridge realmsbridge = new RealmsBridge();
            this.field_183503_M = realmsbridge.getNotificationScreen(this);
            this.field_183502_L = true;
        }
        if (this.func_183501_a()) {
            this.field_183503_M.initGui();
        }
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
        this.buttonList.add(new GuiButton(1, 5, this.height - 25, 98, 20, I18n.format("menu.singleplayer")));
        this.buttonList.add(new GuiButton(2, 110, this.height - 25, 98, 20, I18n.format("menu.multiplayer")));
        if (Reflector.GuiModList_Constructor.exists()) {
            this.modButton = new GuiButton(6, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("fml.menu.mods"));
            this.buttonList.add(this.modButton);
        }
    }

    private void addDemoButtons(int p_73972_1_, int p_73972_2_) {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, p_73972_1_, I18n.format("menu.playdemo")));
        this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, p_73972_1_ + p_73972_2_, I18n.format("menu.resetdemo"));
        this.buttonList.add(this.buttonResetDemo);
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");
        if (worldinfo == null) {
            this.buttonResetDemo.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 14) {
            final String clipboardContent = getClipboardString();

            mc.session = new Session(clipboardContent, "", "", "mojang");

            MicrosoftLogin.getRefreshToken(refreshToken -> {
                if (refreshToken != null) {
                    final MicrosoftLogin.LoginData login = MicrosoftLogin.login(refreshToken);
                    mc.session = new Session(login.username, login.uuid, login.mcToken, "microsoft");
                }
            });
        }

        if (button.id == 4) {
            this.mc.shutdown();
        }

        if (button.id == 6 && Reflector.GuiModList_Constructor.exists()) {
            this.mc.displayGuiScreen((GuiScreen) Reflector.newInstance(Reflector.GuiModList_Constructor, new Object[]{this}));
        }

        if (button.id == 11) {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }

        if (button.id == 12) {
            final ISaveFormat isaveformat = this.mc.getSaveLoader();
            final WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

            if (worldinfo != null) {
                final GuiYesNo guiyesno = GuiSelectWorld.func_152129_a(this, worldinfo.getWorldName(), 12);
                this.mc.displayGuiScreen(guiyesno);
            }
        }
    }

    private void switchToRealms() {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    public void confirmClicked(boolean result, int id) {
        if (result && id == 12) {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        } else if (id == 13) {
            if (result) {
                try {
                    Class oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
                    oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new URI(this.openGLWarningLink));
                }
                catch (Throwable throwable) {
                    logger.error("Couldn't open link", throwable);
                }
            }
            this.mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0f, 1.0f, 0.05f, 10.0f);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;
        int j = 64;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();
        if (custompanoramaproperties != null) {
            j = custompanoramaproperties.getBlur1();
        }
        int k = 0;
        while (k < j) {
            GlStateManager.pushMatrix();
            float f = ((float)(k % i) / (float)i - 0.5f) / 64.0f;
            float f1 = ((float)(k / i) / (float)i - 0.5f) / 64.0f;
            float f2 = 0.0f;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float)this.panoramaTimer + p_73970_3_) / 400.0f) * 25.0f + 20.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(-((float)this.panoramaTimer + p_73970_3_) * 0.1f, 0.0f, 1.0f, 0.0f);
            int l = 0;
            while (l < 6) {
                GlStateManager.pushMatrix();
                if (l == 1) {
                    GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
                }
                if (l == 2) {
                    GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                }
                if (l == 3) {
                    GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
                }
                if (l == 4) {
                    GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                }
                if (l == 5) {
                    GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
                }
                ResourceLocation[] aresourcelocation = titlePanoramaPaths;
                if (custompanoramaproperties != null) {
                    aresourcelocation = custompanoramaproperties.getPanoramaLocations();
                }
                this.mc.getTextureManager().bindTexture(aresourcelocation[l]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                int i1 = 255 / (k + 1);
                float f3 = 0.0f;
                worldrenderer.pos(-1.0, -1.0, 1.0).tex(0.0, 0.0).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0, -1.0, 1.0).tex(1.0, 0.0).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0, 1.0, 1.0).tex(1.0, 1.0).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(-1.0, 1.0, 1.0).tex(0.0, 1.0).color(255, 255, 255, i1).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
                ++l;
            }
            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
            ++k;
        }
        worldrenderer.setTranslation(0.0, 0.0, 0.0);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox(float p_73968_1_) {
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;
        int j = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();
        if (custompanoramaproperties != null) {
            j = custompanoramaproperties.getBlur2();
        }
        int k = 0;
        while (k < j) {
            float f = 1.0f / (float)(k + 1);
            int l = this.width;
            int i1 = this.height;
            float f1 = (float)(k - i / 2) / 256.0f;
            worldrenderer.pos(l, i1, zLevel).tex(0.0f + f1, 1.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(l, 0.0, zLevel).tex(1.0f + f1, 1.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(0.0, 0.0, zLevel).tex(1.0f + f1, 0.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(0.0, i1, zLevel).tex(0.0f + f1, 0.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            ++k;
        }
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        int i = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();
        if (custompanoramaproperties != null) {
            i = custompanoramaproperties.getBlur3();
        }
        int j = 0;
        while (j < i) {
            this.rotateAndBlurSkybox(p_73971_3_);
            this.rotateAndBlurSkybox(p_73971_3_);
            ++j;
        }
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        float f2 = this.width > this.height ? 120.0f / (float)this.width : 120.0f / (float)this.height;
        float f = (float)this.height * f2 / 256.0f;
        float f1 = (float)this.width * f2 / 256.0f;
        int k = this.width;
        int l = this.height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(0.0, l, zLevel).tex(0.5f - f, 0.5f + f1).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(k, l, zLevel).tex(0.5f - f, 0.5f - f1).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(k, 0.0, zLevel).tex(0.5f + f, 0.5f - f1).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(0.0, 0.0, zLevel).tex(0.5f + f, 0.5f + f1).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        tessellator.draw();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr1 = new ScaledResolution(this.mc);
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(new ResourceLocation("lavender/icons/background.png"));
        Gui.drawModalRectWithCustomSizedTexture((sr1.getScaledWidth() - 256) / 2, (sr1.getScaledHeight() - 256) / 2, 0.0f, 0.0f, 256, 256, 255.0f, 255.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        String s = "";
        this.drawString(this.fontRendererObj, " ", 2, this.height - 10, -1);
        this.drawString(this.fontRendererObj, "", 2, this.height - 20, -1);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        String s2 = "";
        this.drawString(this.fontRendererObj, "", this.width - this.fontRendererObj.width("") - 2, this.height - 10, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        Object object = this.threadLock;
        synchronized (object) {
            if (this.openGLWarning1.length() > 0 && mouseX >= this.field_92022_t && mouseX <= this.field_92020_v && mouseY >= this.field_92021_u && mouseY <= this.field_92019_w) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
        if (this.func_183501_a()) {
            this.field_183503_M.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void onGuiClosed() {
        if (this.field_183503_M != null) {
            this.field_183503_M.onGuiClosed();
        }
    }
}
