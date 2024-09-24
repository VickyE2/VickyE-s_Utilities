package org.v_utls.listeners;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public abstract class BaseGuiListener implements Listener {

    // Force subclasses to implement this method
    public abstract void onInventoryClick(InventoryClickEvent event);

    // Force subclasses to implement this method
    public abstract void onInventoryClose(InventoryCloseEvent event);

    //Force subclasses to implement this method
    public abstract void setGuiInventory(Inventory inventory);
}
