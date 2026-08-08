/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.player;

import org.vicky.platform.item.PlatformItemStack;

import java.util.List;

public interface PlatformInventory {

	boolean canFit(PlatformItemStack stack);

	void add(PlatformItemStack stack);

	List<PlatformItemStack> contents();

	List<PlatformItemStack> armor();

	void setArmor(int slot, PlatformItemStack item);

	List<PlatformItemStack> offhand();

	void setOffHand(int slot, PlatformItemStack item);

	int selected();

	PlatformItemStack getItem(int selected);

	void setItem(int selected, PlatformItemStack stack);
}