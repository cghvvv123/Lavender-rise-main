//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft1.12.2 Mappings"!

//Decompiled by Procyon!

package com.alan.clients.fontRender;


import com.alan.clients.Client;

import java.awt.*;
import java.io.InputStream;

public class FontManager
{
    public static RapeMasterFontManager arial13;
    public static RapeMasterFontManager arial14;
    public static RapeMasterFontManager arial15;
    public static RapeMasterFontManager arial22bold;
    public static RapeMasterFontManager arial20bold;
    public static RapeMasterFontManager arial18bold;
    public static RapeMasterFontManager arial17bold;
    public static RapeMasterFontManager arial16bold;
    public static RapeMasterFontManager arial15bold;
    public static RapeMasterFontManager arial14bold;
    public static RapeMasterFontManager arial13bold;
    public static RapeMasterFontManager arial16;
    public static RapeMasterFontManager arial17;
    public static RapeMasterFontManager arial18;
    public static RapeMasterFontManager arial19;
    public static RapeMasterFontManager arial20;
    public static RapeMasterFontManager arial22;
    public static RapeMasterFontManager arial32;
    public static RapeMasterFontManager arial30;
    public static RapeMasterFontManager arial40;
    public static RapeMasterFontManager micon15;
    public static RapeMasterFontManager micon30;
    public static RapeMasterFontManager icon40;
    
    public static void init() {
        FontManager.icon40 = new RapeMasterFontManager(getFont("icon.ttf",40.0f));
        FontManager.micon30 = new RapeMasterFontManager(getFont("micon.ttf", 30.0f));
        FontManager.micon15 = new RapeMasterFontManager(getFont("micon.ttf", 15.0f));
        FontManager.arial15 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 15.0f));
        FontManager.arial13 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 13.0f));
        FontManager.arial14 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 14.0f));
        FontManager.arial16 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 16.0f));
        FontManager.arial17 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 17.0f));
        FontManager.arial18 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 18.0f));
        FontManager.arial19 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 19.0f));
        FontManager.arial20 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 20.0f));
        FontManager.arial22 = new RapeMasterFontManager(getFont("product_sans_regular.ttf",22.0f));
        FontManager.arial32 = new RapeMasterFontManager(getFont("product_sans_regular.ttf",32.0f));
        FontManager.arial30 = new RapeMasterFontManager(getFont("product_sans_regular.ttf",30.0f));
        FontManager.arial40 = new RapeMasterFontManager(getFont("product_sans_regular.ttf", 40.0f));
        FontManager.arial22bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 22.0f));
        FontManager.arial20bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 20.0f));
        FontManager.arial18bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 18.0f));
        FontManager.arial17bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 17.0f));
        FontManager.arial16bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 16.0f));
        FontManager.arial15bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 15.0f));
        FontManager.arial14bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 14.0f));
        FontManager.arial13bold = new RapeMasterFontManager(getFont("product_sans_bold.ttf", 13.0f));
    }
    
    private static Font getFont(final String fontName, final float fontSize) {
        Font font = null;
        try {
            final InputStream inputStream = Client.class.getResourceAsStream("/assets/minecraft/lavender/font/" + fontName);
            assert inputStream != null;
            font = Font.createFont(0, inputStream);
            font = font.deriveFont(fontSize);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return font;
    }
}
