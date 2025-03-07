package com.alan.clients.util.font;

import com.alan.clients.util.font.impl.rise.FontRenderer;
import com.alan.clients.util.font.impl.rise.FontUtil;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

public class FontManager {

    private static final HashMap<Integer, FontRenderer> INTERNATIONAL = new HashMap<>();

    private static final HashMap<Integer, FontRenderer> NUNITO = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> NUNITO_BOLD = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> MUSEO_SANS = new HashMap<>();

    private static final HashMap<Integer, FontRenderer> NUNITO_LIGHT_MAP = new HashMap<>();

    private static final HashMap<Integer, FontRenderer> POPPINS_REGULAR = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> POPPINS_LIGHT = new HashMap<>();

    private static final HashMap<Integer, FontRenderer> TAHOMA_BOLD = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> TAHOMA = new HashMap<>();

    private static final HashMap<Integer, FontRenderer> ICONS_1 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> ICONS_2 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> ICONS_3 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> ICONS_4 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> ICONS_5 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> ICONS_6 = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> PRODUCT_SANS_BOLD = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> PRODUCT_SANS_REGULAR = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> PRODUCT_SANS_MEDIUM = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> PRODUCT_SANS_LIGHT = new HashMap<>();
    private static final HashMap<Integer, FontRenderer> FLUX_ICON = new HashMap<>();

    // COPY THIS METHOD FOR EACH METHOD AND REPLACE FONTNAME WITH THE USED FONT FILE NAME

    public static Font getInternational(int size) {
        return get(INTERNATIONAL, size, "NotoSans-Regular", true, true, false, true);
    }


    public static Font getPoppinsRegular(final int size) {
        return get(POPPINS_REGULAR, size, "Poppins-Regular", true, true);
    }

    public static Font getPoppinsLight(final int size) {
        return get(POPPINS_LIGHT, size, "Poppins-Light", true, true);
    }

    public static Font getNunito(final int size) {
        return get(PRODUCT_SANS_REGULAR, size, "product_sans_regular", true, true);
    }
    public static Font getfluxicon(final int size) {
        return get(FLUX_ICON, size, "fluxicon", true, true);
    }

    public static Font getNunitoBold(final int size) {
        return get(PRODUCT_SANS_BOLD, size, "product_sans_bold", true, true);
    }

    public static Font getNunitoLight(final int size) {
        return get(PRODUCT_SANS_LIGHT, size, "product_sans_light", true, true);
    }

    public static Font getTahomaBold(final int size) {
        return get(TAHOMA_BOLD, size, "TahomaBold", true, true);
    }

    public static Font getTahoma(final int size) {
        return get(TAHOMA, size, "Tahoma", true, true);
    }

    public static Font getIconsOne(final int size) {
        return get(ICONS_1, size, "Icon-1", true, true);
    }

    public static Font getIconsThree(final int size) {
        return get(ICONS_3, size, "Icon-3", true, true);
    }
    public static Font getmicon(final int size) {
        return get(ICONS_3, size, "micon", true, true);
    }
    public static Font geticon(final int size) {
        return get(ICONS_3, size, "tenicon", true, true);
    }
    public static Font geticon2(final int size) {
        return get(ICONS_5, size, "ClientIconOne", true, true);
    }
    public static Font getLogoIcon(final int size) {
        return get(ICONS_4, size, "Lavender", true, true);
    }
    public static Font geticn(final int size) {
        return get(ICONS_6, size, "icn", true, true);
    }
    public static Font getIconsTwo(final int size) {
        return get(ICONS_2, size, "Icon-2", true, true);
    }
    public static Font getProductSansBold(final int size) {
        return get(PRODUCT_SANS_BOLD, size, "product_sans_bold", true, true);
    }

    public static Font getProductSansRegular(final int size) {
        return get(PRODUCT_SANS_REGULAR, size, "product_sans_regular", true, true);
    }

    public static Font getProductSansMedium(final int size) {
        return get(PRODUCT_SANS_MEDIUM, size, "product_sans_medium", true, true);
    }

    public static Font getProductSansLight(final int size) {
        return get(PRODUCT_SANS_LIGHT, size, "product_sans_light", true, true);
    }
    public static Font getMinecraft() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    private static Font get(HashMap<Integer, FontRenderer> map, int size, String name, boolean fractionalMetrics, boolean AA) {
        return get(map, size, name, fractionalMetrics, AA, false, false);
    }

    private static Font get(HashMap<Integer, FontRenderer> map, int size, String name, boolean fractionalMetrics, boolean AA, boolean otf, boolean international) {
        if (!map.containsKey(size)) {
            final java.awt.Font font = FontUtil.getResource("lavender/font/" + name + (otf ? ".otf" : ".ttf"), size);

            if (font != null) {
                map.put(size, new FontRenderer(font, fractionalMetrics, AA, international));
            }
        }

        return map.get(size);
    }
}