//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft1.12.2 Mappings"!

//Decompiled by Procyon!

package com.alan.clients.util.animation.impl;

import com.alan.clients.noti.utils.Direction;
import com.alan.clients.util.animation.Animation;

public class ContinualAnimation
{
    private float output;
    private float endpoint;
    private Animation animation;
    
    public ContinualAnimation() {
        this.animation = new SmoothStepAnimation(0, 0.0, Direction.BACKWARDS);
    }
    
    public void animate(final float destination, final int ms) {
        this.output = (float)(this.endpoint - this.animation.getOutput());
        this.endpoint = destination;
        if (this.output != this.endpoint - destination) {
            this.animation = new SmoothStepAnimation(ms, this.endpoint - this.output, Direction.BACKWARDS);
        }
    }
    
    public boolean isDone() {
        return this.output == this.endpoint || this.animation.isDone();
    }
    
    public float getOutput() {
        return this.output = (float)(this.endpoint - this.animation.getOutput());
    }
    
    public Animation getAnimation() {
        return this.animation;
    }
}
