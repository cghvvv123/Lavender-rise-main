/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.alan.clients.hyt.party;

import com.alan.clients.hyt.party.ui.hyt.party.VexViewComponent;

public class VexViewButton
extends VexViewComponent {
    private final String name;

    public VexViewButton(String name, String id) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

