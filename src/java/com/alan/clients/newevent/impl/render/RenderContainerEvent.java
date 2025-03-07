package com.alan.clients.newevent.impl.render;

import com.alan.clients.newevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiContainer;

@Getter
@AllArgsConstructor
public class RenderContainerEvent extends CancellableEvent {
    private final GuiContainer container;


}
