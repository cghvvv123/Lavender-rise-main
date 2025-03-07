package com.alan.clients.module.impl.render;

import com.alan.clients.api.Rise;
import com.alan.clients.module.Module;
import com.alan.clients.module.api.Category;
import com.alan.clients.module.api.ModuleInfo;
import com.alan.clients.value.impl.NumberValue;

@Rise
@ModuleInfo(name = "module.render.fakebody.name", description = "Render Fake body", category = Category.RENDER)
public class FakeBody extends Module {

    public final NumberValue tick = new NumberValue("Fakebody per tick", this, 50, 10, 500, 1);
    public final NumberValue count = new NumberValue("Fakebody body count", this, 3,1,5,1);


    public static FakeBody INSTANCE;
    public FakeBody(){
        INSTANCE = this;
    }
}
