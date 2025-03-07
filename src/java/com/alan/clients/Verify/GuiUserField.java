package com.alan.clients.Verify;


import com.alan.clients.util.font.impl.minecraft.FontRenderer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;

import java.awt.*;

public final class GuiUserField extends Gui {

    private final int xPosition;
    private final int yPosition;
    private final FontRenderer fontRendererInstance;
    @Setter
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private int cursorCounter;
    @Setter
    private boolean enableBackgroundDrawing = true;
    @Getter
    private String text = "";
    @Getter
    private int maxStringLength = 32;
    private final int width;
    private final int height;
    @Setter
    private boolean visible = true;
    private int disabledColor = (new Color(255, 255, 255)).getRGB();
    private int enabledColor = (new Color(255, 255, 255)).getRGB();
    @Getter
    private int selectionEnd;
    @Getter
    private int cursorPosition;
    private int lineScrollOffset;
    private boolean isEnabled = true;

    public GuiUserField(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_) {
        this.fontRendererInstance = p_i1032_1_;
        this.xPosition = p_i1032_2_;
        this.yPosition = p_i1032_3_;
        this.width = p_i1032_4_;
        this.height = p_i1032_5_;
    }

    public void drawTextBox() {
        if (this.getVisible()) {
            if (this.getEnableBackgroundDrawing()) {
                Gui.drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, -6250336);
                Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
            }

            int i = this.isEnabled ? this.enabledColor : this.disabledColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int i1 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            int j1 = l;

            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;

                j1 = this.fontRendererInstance.drawString(s1.replaceAll("\\.", "*"), l, i1, i);
            }

            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;

            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                this.fontRendererInstance.drawString(s.substring(j).replaceAll("\\.", "*"), j1, i1, i);
            }

            if (flag1) {
                if (flag2) {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + FontRenderer.FONT_HEIGHT, -3092272);
                } else {
                    this.fontRendererInstance.drawString("_", k1, i1, i);
                }
            }

            if (k != j) {
                int l1 = l + this.fontRendererInstance.width(s.substring(0, k).replaceAll("\\.", "*"));

                this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + FontRenderer.FONT_HEIGHT);
            }
        }

    }
    public void drawTextBox2() {
        if (this.getVisible()) {
            // Background color with alpha transparency
            int color = (new Color(0, 0, 0, 60)).getRGB();

            // Draw background if enabled
            if (this.getEnableBackgroundDrawing()) {
                Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, color);
            }

            // Text color, depends on whether the text box is enabled
            int textColor = this.isEnabled ? this.enabledColor : this.disabledColor;

            // The relative cursor and selection end positions within the visible area
            int visibleCursor = this.cursorPosition - this.lineScrollOffset;
            int visibleSelectionEnd = this.selectionEnd - this.lineScrollOffset;

            // Extract visible part of the text based on the line scroll offset and box width
            String visibleText = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());

            // Determine if the cursor is within the visible range
            boolean isCursorInVisibleRange = visibleCursor >= 0 && visibleCursor <= visibleText.length();

            // Cursor blink: only when focused and in visible range
            boolean isCursorVisible = this.isFocused && this.cursorCounter / 6 % 2 == 0 && isCursorInVisibleRange;

            // Set initial drawing position
            int textStartX = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int textStartY = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;

            // Draw the visible text
            int textEndX = textStartX;

            if (!visibleText.isEmpty()) {
                String textToDraw = isCursorInVisibleRange ? visibleText.substring(0, visibleCursor) : visibleText;

                // Draw the text with the appropriate color
                textEndX = this.fontRendererInstance.drawString(
                        textToDraw,
                        (float) textStartX,
                        (float) textStartY,
                        textColor
                );
            }

            // Calculate where the cursor should be drawn
            int cursorX = isCursorInVisibleRange ? textEndX : textStartX;

            if (!isCursorInVisibleRange) {
                cursorX = visibleCursor > 0 ? textStartX + this.width : textStartX;
            } else if (this.cursorPosition < this.text.length()) {
                // Adjust cursor position if it's within the text bounds
                cursorX--;
            }

            // Draw the rest of the text after the cursor
            if (!visibleText.isEmpty() && isCursorInVisibleRange && visibleCursor < visibleText.length()) {
                this.fontRendererInstance.drawString(
                        visibleText.substring(visibleCursor),
                        (float) (textEndX + 2),
                        (float) textStartY,
                        textColor
                );
            }

            // Draw cursor or underscore if blinking and visible
            if (isCursorVisible) {
                if (this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength()) {
                    // Draw rectangle for the cursor
                    Gui.drawRect(cursorX, textStartY, cursorX + 1, textStartY + FontRenderer.FONT_HEIGHT, -3092272);
                } else {
                    // Draw blinking underscore for visual feedback
                    this.fontRendererInstance.drawString("_", (float) (cursorX + 1), (float) textStartY, textColor);
                }
            }

            // Handle text selection rendering (if there's a selection)
            if (visibleSelectionEnd != visibleCursor) {
                int selectionEndX = textStartX + this.fontRendererInstance.width(visibleText.substring(0, visibleSelectionEnd).replaceAll("\\.", "*"));
                this.drawCursorVertical(cursorX, textStartY - 1, selectionEndX, textStartY + FontRenderer.FONT_HEIGHT);
            }
        }
    }

    public void deleteFromCursor(int p_146175_1_) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = p_146175_1_ < 0;
                int i = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
                String s = "";

                if (i >= 0) {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                this.text = s;
                if (flag) {
                    this.moveCursorBy(p_146175_1_);
                }
            }
        }

    }

    public boolean getVisible() {
        return this.visible;
    }

    public void deleteWords(int p_146177_1_) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }

    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void moveCursorBy(int p_146182_1_) {
        this.setCursorPosition(this.selectionEnd + p_146182_1_);
    }

    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, this.getCursorPosition(), true);
    }

    public void setEnabled(boolean p_146184_1_) {
        this.isEnabled = p_146184_1_;
    }

    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
        int tessellator;

        if (p_146188_1_ < p_146188_3_) {
            tessellator = p_146188_1_;
            p_146188_1_ = p_146188_3_;
            p_146188_3_ = tessellator;
        }

        if (p_146188_2_ < p_146188_4_) {
            tessellator = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = tessellator;
        }

        if (p_146188_3_ > this.xPosition + this.width) {
            p_146188_3_ = this.xPosition + this.width;
        }

        if (p_146188_1_ > this.xPosition + this.width) {
            p_146188_1_ = this.xPosition + this.width;
        }

        Tessellator tessellator1 = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator1.getWorldRenderer();

        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_146188_1_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_2_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_1_, p_146188_2_, 0.0D).endVertex();
        tessellator1.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        int i = this.text.length();

        if (this.cursorPosition < 0) {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > i) {
            this.cursorPosition = i;
        }

        this.setSelectionPos(this.cursorPosition);
    }

    public void writeText(String p_146191_1_) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - this.selectionEnd);

        if (!this.text.isEmpty()) {
            s = s + this.text.substring(0, i);
        }

        int l;

        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        this.text = s;
        this.moveCursorBy(i - this.selectionEnd + l);
    }

    public void setTextColor(int p_146193_1_) {
        this.enabledColor = p_146193_1_;
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int i = p_146197_2_;
        boolean flag = p_146197_1_ < 0;
        int j = Math.abs(p_146197_1_);

        for (int k = 0; k < j; ++k) {
            if (flag) {
                do {
                    do {
                        do {
                            --i;
                        } while (!p_146197_3_);
                    } while (i <= 0);
                } while (this.text.charAt(i - 1) == 32);

                do {
                    --i;
                } while (i > 0 && this.text.charAt(i - 1) != 32);
            } else {
                int l = this.text.length();

                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (p_146197_3_ && i < l && this.text.charAt(i) == 32) {
                        ++i;
                    }
                }
            }
        }

        return i;
    }

    public void setSelectionPos(int p_146199_1_) {
        int i = this.text.length();

        if (p_146199_1_ > i) {
            p_146199_1_ = i;
        }

        if (p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        this.selectionEnd = p_146199_1_;
        if (this.fontRendererInstance != null) {
            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (p_146199_1_ == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.text, j, true).length();
            }

            if (p_146199_1_ > k) {
                this.lineScrollOffset += p_146199_1_ - k;
            } else if (p_146199_1_ <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
            }

            if (this.lineScrollOffset < 0) {
                this.lineScrollOffset = 0;
            }

            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }
        }

    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;
        if (this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }

    }

    public void setDisabledTextColour(int p_146204_1_) {
        this.disabledColor = p_146204_1_;
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;

        return this.text.substring(i, j);
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
        boolean flag = p_146192_1_ >= this.xPosition && p_146192_1_ < this.xPosition + this.width && p_146192_2_ >= this.yPosition && p_146192_2_ < this.yPosition + this.height;

        if (this.canLoseFocus) {
            this.setFocused(flag);
        }

        if (this.isFocused && p_146192_3_ == 0) {
            int i = p_146192_1_ - this.xPosition;

            if (this.enableBackgroundDrawing) {
                i -= 4;
            }

            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());

            this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }

    }

    public void setFocused(boolean p_146195_1_) {
        if (p_146195_1_ && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = p_146195_1_;
    }

    public void setText(String p_146180_1_) {
        if (p_146180_1_.length() > this.maxStringLength) {
            this.text = p_146180_1_.substring(0, this.maxStringLength);
        } else {
            this.text = p_146180_1_;
        }

        this.setCursorPositionEnd();
    }

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if (!this.isFocused) {
            return false;
        } else {
            switch (p_146201_1_) {
            case '\u0001':
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
                return true;

            case '\u0003':
                GuiScreen.setClipboardString(this.getSelectedText());
                return true;

            case '\u0016':
                if (this.isEnabled) {
                    this.writeText(GuiScreen.getClipboardString());
                }

                return true;

            case '\u0018':
                GuiScreen.setClipboardString(this.getSelectedText());
                if (this.isEnabled) {
                    this.writeText("");
                }

                return true;

            default:
                switch (p_146201_2_) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;

                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;

                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;

                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;

                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;

                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(p_146201_1_));
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }
}
