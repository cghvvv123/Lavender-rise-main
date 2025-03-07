package com.alan.clients.module.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.value.impl.BooleanValue;

/**
 * @author Strikeless
 * @since 15.03.2022
 */
@Rise
@ModuleInfo(name = "module.render.nocameraclip.name", description = "module.render.nocameraclip.description", category = Category.RENDER)
public final class NoCameraClip extends Module {
    public final BooleanValue NoFire = new BooleanValue("NoFire",this,true);
}
