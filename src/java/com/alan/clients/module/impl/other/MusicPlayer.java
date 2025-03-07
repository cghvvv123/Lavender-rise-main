package com.alan.clients.module.impl.other;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.ui.cloudmusic.ui.GuiCloudMusic;
import com.alan.clients.util.interfaces.InstanceAccess;
import com.alan.clients.value.impl.NumberValue;

@ModuleInfo(name = "MusicPlayer", category = Category.OTHER, description = "CNM")
public class MusicPlayer extends Module {
    public final NumberValue musicPosYlyr = new NumberValue("MusicPlayerLyricY",this, 120d, 0d, 200d, 1d);
    @Override
    public void onEnable(){
        InstanceAccess.mc.displayGuiScreen(new GuiCloudMusic());
        this.setEnabled(false);
    }
}
