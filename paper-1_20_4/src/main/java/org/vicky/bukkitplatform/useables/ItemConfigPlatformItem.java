/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.vicky.guiparent.GuiCreator;
import org.vicky.platform.PlatformItem;

import java.util.UUID;

public class ItemConfigPlatformItem implements PlatformItem {

    private final GuiCreator.ItemConfig stack;
    private final UUID id = UUID.randomUUID();

    public ItemConfigPlatformItem(GuiCreator.ItemConfig stack) {
        this.stack = stack;
    }

    @Override
    public String getName() {
        return stack.getMaterial().name();
    }

    @Override
    public String getIdentifier() {
        return id.toString();
    }

    public GuiCreator.ItemConfig getStack() {
        return stack;
    }
}
