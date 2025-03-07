package com.alan.clients.Verify;


import com.alan.clients.Client;
import com.alan.clients.fontRender.FontManager;
import com.alan.clients.ui.menu.impl.main.MainMenu;
import com.alan.clients.util.font.impl.minecraft.FontRenderer;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.util.render.GLUtil;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.render.StencilUtil;
import com.alan.clients.util.shader.RiseShaders;
import com.alan.clients.util.shader.base.ShaderRenderType;
import com.alan.clients.util.shader.impl.GaussianBlur;
import com.alan.clients.util.verify.HWIDUtils;
import com.alan.clients.util.verify.SystemUtils;
import com.alan.clients.util.web.Browser;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class GuiLogin extends GuiScreen {

    int alpha = 0;
    private boolean i = false;
    public String Password = null;
    public static String rank = null;
    public static GuiPasswordField password;
    public static GuiUserField username;
    public static boolean render = false;
    public static String DevList = null;
    public static String TesterList = null;
    private float currentX;
    private float currentY;
    public boolean drag = false;
    String hwid;
    public static boolean isload = false;
    public static String HWID = null;
    public static int LOVEU = 1;
    public static String process = "[Waiting For Login...]";

    final ScaledResolution scaledresolution = new ScaledResolution(this.mc);


    public GuiLogin() {
        try {
            this.hwid = HWIDUtils.getHWID();
        } catch (IOException | NoSuchAlgorithmException ioexception) {
            ioexception.printStackTrace();
        }

    }

    public void drawScreen(int i, int j, float f) {
        Display.setTitle("Lavender" + " | 请登陆");
        if (this.i && this.alpha < 255) {
            this.alpha += 5;
        }
        int h = scaledresolution.getScaledHeight();
        int w = scaledresolution.getScaledWidth();

        RenderUtil.drawGradientSideways(0.0D, 0.0D, w, h, (new Color(60, 96, 203)).getRGB(), (new Color(51, 201, 217)).getRGB());
        this.drawBackground(0);
        ScaledResolution sr = new ScaledResolution(mc);
        float xDiff = ((float) (i - h / 2) - this.currentX) / (float) sr.getScaleFactor();
        float yDiff = ((float) (j - w / 2) - this.currentY) / (float) sr.getScaleFactor();

        this.currentX += xDiff * 0.3F;
        this.currentY += yDiff * 0.3F;
        this.drag = Mouse.isButtonDown(0);
        RenderUtil.drawImage(new ResourceLocation("lavender/images/bg2.png"), 0,0 ,sr.scaledWidth, sr.scaledHeight,-1);


       /* GaussianBlur.startBlur();
        RenderUtil.rectangle(0,0 ,sr.scaledWidth, sr.scaledHeight, new Color(255,255,255));
        GaussianBlur.endBlur(40, 2);


        RenderUtil.drawGradientSideways((double) this.width / 2 + 30, (double) height / 2 + 47, (double) width / 2 + 155, (double) height / 2 + 70, (new Color(94, 212, 255)).getRGB(), (new Color(253, 222, 90)).getRGB());
        GaussianBlur.startBlur();
        RenderUtil.drawRect((float) (width / 2 - 180), (float) (height / 2 - 115), (float) (width / 2 + 180), (float) (height / 2 + 115), (new Color(0, 0, 0)).getRGB());
        GaussianBlur.endBlur(40, 2);
*/
        RenderUtil.drawRect((float) (width / 2 - 180), (float) (height / 2 - 115), (float) (width / 2 + 180), (float) (height / 2 + 115), (new Color(40, 40, 40, 171)).getRGB());
        RenderUtil.drawGradientSideways((double) width / 2 + 30, (double) height / 2 + 47, (double) width / 2 + 155, (double) height / 2 + 70, (new Color(0, 0, 0, 40)).getRGB(), (new Color(0, 0, 0,40)).getRGB());
       // RenderUtil.drawGradientSideways((float) (width / 2 + 30), (float) (height / 2 - 9), (float) (width / 2 + 155), (float) (height / 2 - 8), (new Color(0, 111, 255, 255)).getRGB(), (new Color(255, 125, 198)).getRGB());
        //RenderUtil.drawGradientSideways((float) (width / 2 + 30), (float) (height / 2 + 30), (float) (width / 2 + 155), (float) (height / 2 + 31), (new Color(0, 0, 0, 40)).getRGB(), (new Color(0, 0, 0,30)).getRGB());

        if (!GuiLogin.username.getText().isEmpty() && Mouse.isButtonDown(0) && this.drag && i > width / 2 + 30 && i < width / 2 + 155 && j > height / 2 + 47 && j < height / 2 + 70) {

            this.verify();
            FontManager.arial32.drawString("Login", (float) (width / 2 + 10), (float) (height / 2 - 75), -1);
        }
        RenderUtil.color(-1);
        FontManager.arial20.drawString("Login", (float) (width / 2 + 80), (float) (height / 2 + 55), (new Color(255, 255, 255)).getRGB());
        FontManager.arial16.drawString("Log in to your account so that we can", (float) (width / 2 + 10), (float) (height / 2 - 61), (new Color(255, 255, 255)).getRGB());
        FontManager.arial16.drawString("check your identity.", (float) (width / 2 + 10), (float) (height / 2 - 53), (new Color(255, 255, 255)).getRGB());
        FontManager.arial32.drawString("Login to client", ((float) width / 2 - 165), ((float) height / 2 - 105), (new Color(255, 255, 255)).getRGB());
        FontManager.arial30.drawString("Welcome", (float) (width / 2 - 162), (float) (height / 2 + 77), (new Color(255, 255, 255, 255)).getRGB());
        GuiLogin.username.drawTextBox2();
        FontManager.arial17.drawString("By" + "Lavender Team", (float) (width / 2 - 162), (float) (height / 2 + 53 + 40), (new Color(255, 255, 255)).getRGB());
        GuiLogin.password.drawTextBox2();
        if (GuiLogin.password.getText().isEmpty() && !GuiLogin.password.isFocused()) {
            FontManager.arial16.drawString("Your Password", (float) width / 2 + 35, (float) height / 2 + 17, new Color(255, 255, 255,210).getRGB());
        }

        if (GuiLogin.username.getText().isEmpty() && !GuiLogin.username.isFocused()) {
            FontManager.arial16.drawString("Your Username", (float) width / 2 + 35, (float) height / 2 - 23, new Color(255, 255, 255,210).getRGB());
        }

        super.drawScreen(i, j, f);
    }

    public void initGui() {
        InstanceAccess.clearRunnables();
        FontRenderer fontrenderer = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        super.initGui();
        GuiLogin.render = true;
        GuiLogin.username = new GuiUserField(fontrenderer, width / 2 + 30, height / 2 - 30, 125, 20);
        GuiLogin.password = new GuiPasswordField(fontrenderer, width / 2 + 30, height / 2 + 10, 125, 20);
    }

    public void keyTyped(char c0, int i) {
        if (c0 == 9) {
            if (!GuiLogin.username.isFocused()) {
                GuiLogin.username.setFocused(true);
            } else {
                GuiLogin.username.setFocused(true);
                GuiLogin.password.setFocused(!GuiLogin.username.isFocused());
            }
        }

        if (c0 == 27) {
            ;
        }

        GuiLogin.username.textboxKeyTyped(c0, i);
        GuiLogin.password.textboxKeyTyped(c0, i);
    }
    private void verify() {
        try {
            GuiLogin.LOVEU *= 10;
            GuiLogin.HWID = HWIDUtils.getHWID();
            if (!GuiLogin.username.getText().isEmpty() && !GuiLogin.HWID.isEmpty()) {
                GuiLogin.LOVEU *= 10;
                Client.name = GuiLogin.username.getText();
                this.Password = GuiLogin.password.getText();

                String throwable = "[" + Client.name + "]" + HWIDUtils.getHWID() + ":" + Password;
                if (Browser.get("https://gitcode.net/m0_74037382/emperor/-/raw/master/README.md").contains(throwable)) {
                    GuiLogin.LOVEU *= 10;
                    GuiLogin.isload = true;
                    rank = "User";
                    if (Browser.get("https://gitcode.net/m0_74037382/emperor/-/raw/master/TesterList").contains(Client.name)) {
                        DevList = Client.name;
                        GuiLogin.LOVEU *= 10;
                        GuiLogin.isload = true;
                        rank = "Tester";
                        SystemUtils.displayTray("欢迎", "欢迎Tester", TrayIcon.MessageType.INFO);
                    }
                    if (Browser.get("https://gitcode.net/m0_74037382/emperor/-/raw/master/DevList").contains(Client.name)) {
                        TesterList = Client.name;
                        GuiLogin.LOVEU *= 10;
                        GuiLogin.isload = true;
                        rank = "Dev";
                        SystemUtils.displayTray("欢迎", "欢迎DEV", TrayIcon.MessageType.INFO);
                    }
                    mc.displayGuiScreen(new MainMenu());
                    Display.setTitle(Client.NAME + " " + Client.VERSION);
                } else {
                    GuiLogin.isload = false;
                    SystemUtils.displayTray("ERROR", "HWID已复制到剪贴板 发给管理人员", TrayIcon.MessageType.ERROR);

                    // 复制HWID到剪贴板
                    StringSelection stringSelection = new StringSelection(GuiLogin.HWID);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
            JOptionPane.showMessageDialog(null, "ERROR: " + throwable1.getMessage());
            mc.displayGuiScreen(new MainMenu());
        }
        if (Client.name == null) {
            rank = "SB";
        }
    }


    public void mouseClicked(int i, int j, int k) {
        try {
            super.mouseClicked(i, j, k);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }

        GuiLogin.username.mouseClicked(i, j, k);
        GuiLogin.password.mouseClicked(i, j, k);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen() {
        GuiLogin.username.updateCursorCounter();
        GuiLogin.password.updateCursorCounter();
    }
}
