package com.alan.clients.ui.click.other;

import com.alan.clients.module.Module;
import com.alan.clients.util.MouseUtil;
import com.alan.clients.util.animations.Animation;
import com.alan.clients.util.animations.Easing;
import com.alan.clients.util.font.Font;
import com.alan.clients.util.font.FontManager;
import com.alan.clients.util.localization.Localization;
import com.alan.clients.util.render.RenderUtil;
import com.alan.clients.util.vector.Vector2f;
import com.alan.clients.value.Mode;
import com.alan.clients.value.Value;
import com.alan.clients.value.impl.BooleanValue;
import com.alan.clients.value.impl.ModeValue;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class HanabiClickGUI extends GuiScreen {



    public Vector2f position = new Vector2f(-1, -1);
    public Vector2f scale = new Vector2f(450,380);

    public ArrayList<Module> modules = new ArrayList<>();

    private int currentCategory = 0;

    private Module currentModule;

    private boolean drag,sizeDrag;
    private float dragX, dragY , sizeDragX,sizeDragY;


    private float leftWidth = 90f;
    private final com.alan.clients.util.font.Font fontRenderer = FontManager.getProductSansRegular(18);
    private final Font fontRenderer2 = FontManager.getProductSansBold(18);

    private float yWheel = 0;

    public Animation openAnim = new Animation(Easing.EASE_IN_EXPO,100);

    public Animation sliderAnim = new Animation(Easing.LINEAR,100);

    public Animation wheelAnim = new Animation(Easing.LINEAR,100);

    public Animation listAnim = new Animation(Easing.EASE_IN_EXPO,100);



    public HanabiClickGUI(){
        reset();
        sliderAnim.setValue(0);
        sliderAnim.reset();
        wheelAnim.setValue(0);
        wheelAnim.reset();
        listAnim.setValue(0.7);
        listAnim.reset();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        mc.thePlayer.playSound("random.click", 1, 1);

    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);


        if (!Mouse.isButtonDown(0)) {
            drag = false;
            sizeDrag = false;
        }



        if (drag && !sizeDrag) {
            position.x = mouseX - dragX;
            position.y = mouseY - dragY;
        }


        float w = mouseX + sizeDragX - position.x;
        float h = mouseY + sizeDragY - position.y;

        if (sizeDrag && (w > 400) && (w < 700)) {
            scale.x = w;
        }

        if (sizeDrag && (h > 310) && (h < 400)) {
            scale.y = h;
        }

        openAnim.run(1f);
        sizeAnimate(position.x,position.y,scale.x,scale.y, (float) openAnim.getValue());
        RenderUtil.roundedRectangle(position.x - 1, position.y - 1, scale.x + 2,  scale.y + 2, 4, new Color(80,80,80));
        RenderUtil.roundedRectangle(position.x, position.y,  scale.x,  scale.y, 4, new Color(243, 243, 243));
        RenderUtil.image(new ResourceLocation("lavender/hanabi/logo.png"), position.x + 5, position.y + 25, 174 / 2f, 43 / 2f);
        RenderUtil.image(new ResourceLocation("lavender/hanabi/drag.png"), position.x + scale.x - 20, position.y + scale.y - 20, 16, 16,new Color(0, 0, 0));

        sliderAnim.run(currentCategory*30);
        float categoryY = position.y + 80;
        int index = 0;
        for(Category category : Category.values()){
            if(index == currentCategory){
                RenderUtil.roundedRectangle(position.x, position.y + 80 + sliderAnim.getValue() - 8, leftWidth, 26, 2, new Color(0,149,255));
                RenderUtil.image(new ResourceLocation("lavender/hanabi/" + category.name().toLowerCase(Locale.ROOT) + ".png"), position.x + 8, categoryY - 2, 12, 12,new Color(255,255,255));
            }else{
                RenderUtil.image(new ResourceLocation("lavender/hanabi/" + category.name().toLowerCase(Locale.ROOT) + ".png"), position.x + 8, categoryY - 2, 12, 12,new Color(50,50,50));
            }
            fontRenderer.drawString(category.name(), position.x + 28, categoryY + 1, index == currentCategory ? new Color(255,255,255).getRGB() : new Color(50,50,50).getRGB());
            categoryY += 30;
            index++;
        }
        RenderUtil.rectangle(position.x + leftWidth, position.y, 0.5,  scale.y, new Color(178, 178, 178, 70));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.scissor(position.x,position.y+10,scale.x,scale.y-40);

        listAnim.run(1f);

        sizeAnimate(position.x + leftWidth + 200, position.y, scale.x - leftWidth, scale.y, (float) listAnim.getValue());
        if(currentModule != null){
            float y = (float) (position.y + 40 + wheelAnim.getValue());
            fontRenderer.drawString(Localization.get(currentModule.getModuleInfo().name()), position.x + leftWidth + 25, position.y + 14, new Color(0, 0, 0).getRGB());
            RenderUtil.rectangle(position.x + leftWidth + 10, position.y + 26, scale.x - leftWidth - 10, 1,new Color(178, 178, 178, 70));

            for(Value value : currentModule.getValues()){
                if(value.getHideIf() == null || !value.hideIf.getAsBoolean()){
                    if(value instanceof BooleanValue) {
                        fontRenderer.drawString(value.getName(), position.x + leftWidth + 26, y, new Color(20, 20, 20).getRGB());
                        RenderUtil.image(new ResourceLocation("lavender/hanabi/disabled.png"), position.x + leftWidth + 16, y, 8, 8, new Color(255, 255, 255, ((int) (255 - ((BooleanValue) value).getAnim().getValue()))));
                        RenderUtil.image(new ResourceLocation("lavender/hanabi/enabled.png"), position.x + leftWidth + 16, y, 8, 8, new Color(255, 255, 255, ((int) ((BooleanValue) value).getAnim().getValue())));
                        ((BooleanValue) value).getAnim().run(((BooleanValue) value).getValue() ? 255 : 0);
                        y += 20;
                        RenderUtil.rectangle(position.x + leftWidth + 10, y - 6, scale.x - leftWidth - 20, 0.5, new Color(178, 178, 178, 70));
                    }

                    if(value instanceof ModeValue){
                        fontRenderer.drawString(value.getName(), position.x + leftWidth + 26, y, new Color(20, 20, 20).getRGB());
                        RenderUtil.roundedRectangle(position.x + scale.x - 110, y + 1, 80, 14 + ((ModeValue) value).getExpendAnim().getValue(), 2,new Color(232, 232, 232));
                        fontRenderer2.drawString(((ModeValue) value).getValue().getName(), position.x + scale.x - 100, y + 5, new Color(50, 50, 50).getRGB(), false);

                        if(((ModeValue) value).isExpand()){
                            ((ModeValue) value).getExpendAnim().run(((ModeValue) value).getModes().size()*15-15);
                            for(Mode sub : ((ModeValue) value).getModes()){
                                if(sub.equals(value.getValue())){
                                    continue;
                                }
                                fontRenderer2.drawString(sub.getName(),position.x + scale.x - 100, y + 15 + 5 / 2f, new Color(180, 180, 180).getRGB());
                                y += 15;

                            }
                        }else{
                            ((ModeValue) value).getExpendAnim().run(0);
                        }

                        y += 25 ;
                        RenderUtil.rectangle(position.x + leftWidth + 10, y - 6, scale.x - leftWidth - 20, 0.5, new Color(178, 178, 178, 70));

                    }

                }
            }


        }else{
            if(modules.isEmpty()){
                modules = getModules();
            }

            float y = (float) (position.y + 10 + wheelAnim.getValue());

            for(Module module : modules){

                if (y < (position.y + scale.y - 20)){

                    if(!module.isEnabled()){
                        module.getMoveAnim().run(0);
                    }else{
                        module.getMoveAnim().run(10);
                    }
                    RenderUtil.roundedRectangle(position.x + leftWidth + 8, y, scale.x - leftWidth - 16,  30, 3, Color.WHITE);
                    fontRenderer.drawString(" " + Localization.get(module.getModuleInfo().name()), position.x + leftWidth + 12, y + 12, module.isEnabled() ? new Color(0, 0, 0).getRGB() : new Color(127, 127, 127).getRGB());
                    RenderUtil.roundedRectangle(position.x + scale.x - 50, y + 10, 20, 10, 2, new Color(232, 232, 232));
                    RenderUtil.circle(position.x + scale.x - 48.5 + module.getMoveAnim().getValue(), y + 11.5, 7, module.isEnabled()? new Color(52, 141, 255) : new Color(181, 181, 181));
                    y += 35;

                }
            }

            float mouseDWheel = Mouse.getDWheel();
            if(mouseDWheel >0  && yWheel < 0){
                yWheel += 16;
            } else if (mouseDWheel < 0 && yWheel/-35f + (scale.y - 28)/35f < modules.size()) {
                yWheel -= 16;
            }


            wheelAnim.run(yWheel);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

    }


    public void reset(){
        openAnim.setValue(0.5);
        openAnim.reset();
    }


    @Override
    public void initGui() {
        super.initGui();

        //初始化
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if (this.position.x < 0 || this.position.y < 0 ||
                this.position.x + this.scale.x > scaledResolution.getScaledWidth() ||
                this.position.y + this.scale.y > scaledResolution.getScaledHeight()) {
            this.position.x = scaledResolution.getScaledWidth() / 2f - this.scale.x / 2f;
            this.position.y = scaledResolution.getScaledHeight() / 2f - this.scale.y / 2f;
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(listAnim.getValue() < 0.99){
            return;
        }

        if (mouseButton == 0 && MouseUtil.isHovered(position.x, position.y,leftWidth ,  34, mouseX, mouseY)) {
            drag = true;
            dragX = mouseX - position.x;
            dragY = mouseY - position.y;
        }

        if (mouseButton == 0 && MouseUtil.isHovered(position.x + scale.x - 20, position.y + scale.y - 20, 16,16, mouseX, mouseY)) {
            sizeDrag = true;
            sizeDragX = (position.x + scale.x) - mouseX;
            sizeDragY = (position.y + scale.y) - mouseY;
        }

        if (mouseButton == 0 && MouseUtil.isHovered(position.x , position.y +80 , leftWidth,Category.values().length * 30, mouseX, mouseY)) {
            currentCategory = (int) ((mouseY - position.y - 80)/30);
            modules.clear();
            modules = getModules();
            currentModule = null;
            wheelAnim.setValue(0);
            wheelAnim.reset();
            yWheel = 0;
            listAnim.setValue(0.7);
            listAnim.reset();
        }

        if(mouseButton == 1 && currentModule != null){
            currentModule = null;
            wheelAnim.setValue(0);
            wheelAnim.reset();
            yWheel = 0;
            listAnim.setValue(0.7);
            listAnim.reset();
            return;
        }

        if(currentModule == null){
            if(MouseUtil.isHovered(position.x + leftWidth + 3,position.y + 10 ,scale.x - leftWidth - 6,scale.y-40,mouseX,mouseY)){
                int index = (int) (mouseY - wheelAnim.getValue() - position.y)/35;
                if(modules.size() > index){
                    double offset = (mouseY - wheelAnim.getValue() - position.y)%35;
                    if(offset>=12){
                        Module m = modules.get(index);
                        if(mouseButton == 0){
                            mc.thePlayer.playSound("random.bow",1,1);
                            m.toggle();
                        }else{
                            currentModule = m;
                            listAnim.setValue(0.7);
                            listAnim.reset();
                        }
                    }
                }
            }
        }else{
            float y = (float) (position.y + 40 + wheelAnim.getValue());
            for(Value value : currentModule.getValues()){
                if(value.getHideIf() == null || !value.hideIf.getAsBoolean()){
                    if(value instanceof BooleanValue){
                        if(MouseUtil.isHovered(position.x + leftWidth + 16, y, 8, 8,mouseX,mouseY)){
                            value.setValue(!((BooleanValue)value).getValue());
                        }
                        y += 20;
                    }

                    if(value instanceof ModeValue){

                        if(MouseUtil.isHovered(position.x + scale.x - 110, y + 1, 80, 14 + ((ModeValue) value).getExpendAnim().getValue(), mouseX,mouseY)){
                            if(((ModeValue) value).isExpand()){
                                int index = (int)(mouseY - y - 15)/15;
                                for(int i = 0 ; i <= index ; i ++){
                                    if(value.getValue().equals(((ModeValue) value).getModes().get(i))){
                                        index++;
                                        break;
                                    }
                                }

                                value.setValue(((ModeValue) value).getModes().get(index));
                            }
                            ((ModeValue) value).setExpand(!((ModeValue) value).isExpand());

                            if(((ModeValue) value).isExpand()){
                                y += ((ModeValue) value).getModes().size()*15 - 15;
                            }

                            return;
                        }

                        if(((ModeValue) value).isExpand()){
                            y += ((ModeValue) value).getModes().size()*15 - 15;
                        }

                        y+=25;

                    }

//                    if(value instanceof ListValue){
//
//                        if(MouseUtil.isHovered(position.x + scale.x - 110, y + 1, 80, 14 + ((ModeValue) value).getExpendAnim().getValue(), mouseX,mouseY)){
//                            ((ListValue) value).setExpand(!((ListValue) value).isExpand());
//                        }
//
//                        if(((ListValue) value).isExpand()){
//                            y += ((ListValue) value).getModes().size()*15 - 15;
//                        }
//
//                        y+=25;
//                    }




                }
            }
        }



    }

    private ArrayList<Module> getModules(){
        ArrayList<Module> list = new ArrayList();
        switch (currentCategory){
            case 0:
            case 5:
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.COMBAT));
                break;
            case 1:
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.MOVEMENT));
                break;
            case 2:
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.RENDER));
                break;
            case 3:
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.OTHER));
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.PLAYER));
                break;
            case 4:
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.EXPLOIT));
                list.addAll(instance.getModuleManager().get(com.alan.clients.module.api.Category.GHOST));
                break;

        }
        return list;
    }


    private void sizeAnimate(float x, float y, float width, float height, float progress) {
        GL11.glScaled(progress, progress, 1);
        float xpos = (x + width / 2 / progress) * (1 - progress);
        float ypos = (y + height / 2 / progress) * (1 - progress);
        GL11.glTranslated(xpos, ypos, 0);
    }




}

enum Category{
    Combat,
    Movement,
    Render,
    World,
    Misc,
    PVP;

}

