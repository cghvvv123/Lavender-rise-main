package com.alan.clients.module.impl.render;

import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.value.impl.NumberValue;

@ModuleInfo(name = "module.render.nofov.name", description = "module.render.nofov.description", category = Category.RENDER)
public final class NoFov extends Module {

    public final NumberValue fov = new NumberValue("Fov", this, 1.0, 0.1, 1.4, 0.1);

}
