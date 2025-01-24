package org.skills.utils;

import org.kingdoms.server.inventory.BukkitInventoryView;
import org.kingdoms.server.inventory.NewInventoryView;
import org.kingdoms.server.inventory.OldInventoryView;

public final class XInventoryView {
    private XInventoryView() {}

    public static BukkitInventoryView of(Object any) {
        if (any.getClass().isInterface()) return new NewInventoryView(any);
        else return new OldInventoryView(any);
    }
}
