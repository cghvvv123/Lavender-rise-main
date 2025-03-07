package com.alan.clients.ui.cloudmusic.utils;


import com.alan.clients.Client;
import com.alan.clients.hyt.animation.AnimationUtils;
import com.alan.clients.newevent.Listener;
import com.alan.clients.newevent.annotations.EventLink;
import com.alan.clients.newevent.impl.render.Render2DEvent;
import net.minecraft.client.Minecraft;

public class SpectrumUtil {

    public SpectrumUtil(){
        Client.INSTANCE.getEventBus().register(this);
    }
    float[] spectrum;

    public void updateSpectrum(float[] spectrum) {
        if (this.spectrum != null){
            for (int i = 0; i < spectrum.length; i++) {
                // 如果少于给定值就更新
                if (this.spectrum[i] < spectrum[i])
                this.spectrum[i] = AnimationUtils.moveUD(this.spectrum[i],spectrum[i],30f / Minecraft.getDebugFPS(),28f / Minecraft.getDebugFPS());
            }
        }else {
            this.spectrum = spectrum;
        }
    }

    // 获取频谱
    public float[] getSpectrum() {
        if (spectrum != null) {
            return spectrum;
        } else {
            return new float[]{0f};
        }
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (spectrum != null) {
            for (int i = 0; i < spectrum.length; i++) {
                // 慢慢的把频谱降到0
                spectrum[i] = AnimationUtils.moveUD(spectrum[i], 0, 2f / Minecraft.getDebugFPS(), 1f / Minecraft.getDebugFPS());
            }
        }
    };
}
