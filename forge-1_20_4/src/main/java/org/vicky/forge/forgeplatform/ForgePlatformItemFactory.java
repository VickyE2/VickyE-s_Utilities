package org.vicky.forge.forgeplatform;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgePlatformItem;
import org.vicky.forge.forgeplatform.useables.ForgePlatformMaterial;
import org.vicky.platform.items.ItemDescriptor;
import org.vicky.platform.items.ItemPhysicalProperties;
import org.vicky.platform.items.PlatformItemFactory;
import org.vicky.platform.PlatformItemStack;
import org.vicky.platform.utils.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Forge implementation that registers Items as RegistryObject<Item>.
 */
public class ForgePlatformItemFactory extends PlatformItemFactory {

	private final String modId;
	private final DeferredRegister<Item> itemsRegister;
	private final Map<ResourceLocation, RegistryObject<Item>> registryObjects = new ConcurrentHashMap<>();

	/**
	 * Create factory. Call {@link #attachToEventBus(IEventBus)} after you finish
	 * registering all descriptors so Forge will actually register them.
	 */
	public ForgePlatformItemFactory(String modId) {
		this.modId = modId;
		this.itemsRegister = DeferredRegister.create(ForgeRegistries.ITEMS, modId);
	}

	/**
	 * Attach deferred register to mod event bus. Call this in your mod constructor
	 * after descriptors have been registered (or before, as long as descriptors
	 * are registered before registries are frozen).
	 */
	public void attachToEventBus(IEventBus modEventBus) {
		itemsRegister.register(modEventBus);
	}

	@Override
	public PlatformItemStack fromMaterial(org.vicky.platform.world.PlatformMaterial material) {
		if (material instanceof ForgePlatformMaterial fp) {
			return new ForgePlatformItem(fp.material().asItem().getDefaultInstance());
		}
		throw new IllegalArgumentException("Expected ForgePlatformMaterial got " + (material == null ? "null" : material.getClass().getSimpleName()));
	}

	/**
	 * Register descriptor -> create RegistryObject<Item> using the descriptor as the supplier seed.
	 * This does NOT call itemsRegister.register(modEventBus) — you must attach that separately.
	 */
	@Override
	public void registerItem(ResourceLocation id, ItemDescriptor descriptor) {
		// build a supplier that creates a vanilla Item based on descriptor properties
		Supplier<Item> supplier = () -> createVanillaItemFromDescriptor(descriptor);

		// register with DeferredRegister; register() returns a RegistryObject<Item>
		RegistryObject<Item> ro = itemsRegister.register(id.getPath(), supplier);

		// store mapping
		registryObjects.put(id, ro);

		// still call parent logic for descriptor caching (if parent keeps descriptors)
		super.registerItem(id, descriptor);
	}

	// build a vanilla Item using descriptor (Item.Properties derived from descriptor.physicalProps)
	private Item createVanillaItemFromDescriptor(ItemDescriptor descriptor) {
		ItemPhysicalProperties phys = descriptor.getPhysicalProps();

		Item.Properties props = new Item.Properties();
		props.stacksTo(descriptor.getCanStack() ? 64 : 1);
		if (descriptor.getPhysicalProps().isEnchanted()) {
			props.();
		}

		return new Item(props);
	}

	@Override
	protected @NotNull PlatformItemStack buildStackFromDescriptor(@NotNull ItemDescriptor itemDescriptor, @NotNull Map<String, ?> overrides) {
		// If someone asked to build directly from descriptor (not from registry), we can:
		// - Check if we already registered a RegistryObject for this descriptor's id (preferred)
		// - Fallback: create a temporary itemstack from a vanilla item (not recommended for registered items)
		// Here we'll prefer the registered item if available.

		// try to find a registered RegistryObject by descriptor -> parent keeps descriptors map, so you might convert descriptor->id
		// simplest: if descriptor was registered, the parent registerItem stored it; try to locate registryObject by comparing descriptors
		// For simplicity, fallback to creating an ItemStack from a new Item (same logic as createVanillaItemFromDescriptor)
		Item item = createVanillaItemFromDescriptor(itemDescriptor);
		ItemStack stack = item.getDefaultInstance();
		// apply display name/lore/nbt as earlier (use your existing code to set NBT/display)
		// TODO: convert descriptor.displayName and descriptor.lore to NBT display tag (Adventure -> JSON)
		return new ForgePlatformItem(stack);
	}

	@Override
	public @NotNull PlatformItemStack fromDescriptor(@NotNull ItemDescriptor itemDescriptor) {
		// Attempt to find descriptor registration -> create stack from registered item
		// If you store reverse mapping from descriptor->ResourceLocation you can do a direct lookup.
		// As a simple approach, try to find registry entry whose descriptor equals the provided descriptor.
		for (Map.Entry<ResourceLocation, RegistryObject<Item>> e : registryObjects.entrySet()) {
			ResourceLocation rl = e.getKey();
			ItemDescriptor regDesc = getDescriptor(rl);
			if (regDesc != null && regDesc.equals(itemDescriptor)) { // equals requires descriptor equality semantics
				ItemStack s = e.getValue().get().getDefaultInstance();
				return new ForgePlatformItem(s);
			}
		}
		// fallback to building a transient item stack
		return buildStackFromDescriptor(itemDescriptor, Map.of());
	}

	@Override
	public @NotNull PlatformItemStack fromRegisteredDescriptor(@NotNull ResourceLocation resourceLocation) throws org.vicky.platform.items.DescriptorNotRegisteredException {
		RegistryObject<Item> ro = registryObjects.get(resourceLocation);
		if (ro == null) throw new org.vicky.platform.items.DescriptorNotRegisteredException("Descriptor not registered: " + resourceLocation);
		Item item = ro.get(); // safe to call after registration is complete
		ItemStack stack = item.getDefaultInstance();
		return new ForgePlatformItem(stack);
	}

	@Override
	public @Nullable org.vicky.platform.world.PlatformMaterial materialOf(@NotNull ResourceLocation resourceLocation) {
		RegistryObject<Item> ro = registryObjects.get(resourceLocation);
		if (ro == null) return null;
		Item item = ro.get();
		// TODO: adapt to your ForgePlatformMaterial wrapper factory
		return new ForgePlatformMaterial(item);
	}
}