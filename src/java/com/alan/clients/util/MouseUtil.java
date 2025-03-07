package com.alan.clients.util;

import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class MouseUtil {

    /**
     * Checks if mouse coordinates are within the bounds of given coordinates.
     *
     * @param x      The x coordinate of the top-left corner of the area.
     * @param y      The y coordinate of the top-left corner of the area.
     * @param width  The width of the area.
     * @param height The height of the area.
     * @param mouseX The x coordinate of the mouse.
     * @param mouseY The y coordinate of the mouse.
     * @return True if the mouse is within the area, false otherwise.
     */
    public static boolean isHovered(final double x, final double y, final double width, final double height, final int mouseX, final int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    public static void setMouseButtonState(int mouseButton, boolean held) {
        try {
            Field buttonsField = Mouse.class.getDeclaredField("buttons");
            buttonsField.setAccessible(true);

            ByteBuffer buttons = (ByteBuffer) buttonsField.get(null);

            buttons.put(mouseButton, (byte) (held ? 1 : 0));

            buttonsField.set(null, buttons);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
