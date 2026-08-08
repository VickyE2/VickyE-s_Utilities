package org.vicky.forge.forgeplatform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.item.DescriptorItem;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.forge.forgeplatform.item.ForgeItemStack;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.forge.forgeplatform.useables.ForgePlatformMaterial;
import org.vicky.platform.item.ItemData;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.item.data.SerializedItemStack;
import org.vicky.platform.items.*;
import org.vicky.platform.tags.ItemDataFormat;
import org.vicky.platform.utils.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.vicky.forge.VickyUtilitiesForge.CREATIVE_TABS;

/**
 * Forge implementation that registers Items as a list of RegistryObject[Item] to respect namespacing.
 */
public class ForgePlatformItemFactory extends PlatformItemFactory {

	private final Map<String, DeferredRegister<Item>> itemsRegisters;
	private final Map<ResourceLocation, RegistryObject<Item>> registryObjects = new ConcurrentHashMap<>();
	private final Map<ItemDescriptor, ResourceLocation> descriptorIds = new ConcurrentHashMap<>();

	/**
	 * Create a factory. Call {@link #attachToEventBus(IEventBus)} after you finish
	 * registering all descriptors so Forge will actually register them.
	 */
	public ForgePlatformItemFactory() {
		this.itemsRegisters = new HashMap<>();
	}

	public Map<ItemDescriptor, ResourceLocation> getDescriptorIds() {
		return descriptorIds;
	}

	/**
	 * Attach deferred register to mod event bus. Call this in your mod constructor
	 * after descriptors have been registered (or before, as long as descriptors
	 * are registered before registries are frozen).
	 */
	public void attachToEventBus(IEventBus modEventBus) {
		itemsRegisters.forEach((ignored, it) -> it.register(modEventBus));
	}

	@Override
	public PlatformItemStack fromMaterial(org.vicky.platform.world.PlatformMaterial material) {
		if (material instanceof ForgePlatformMaterial fp) {
			return new ForgeItemStack(fp.material().asItem().getDefaultInstance());
		}
		throw new IllegalArgumentException("Expected ForgePlatformMaterial got " + (material == null ? "null" : material.getClass().getSimpleName()));
	}

	/**
	 * Register descriptor -> create RegistryObject[Item] using the descriptor as the supplier seed.
	 * This does NOT call itemsRegister.register(modEventBus) — you must attach that separately.
	 */
	@Override
	public void registerItem(ResourceLocation id, @NotNull ItemDescriptor descriptor) {
		// build a supplier that creates a vanilla Item based on descriptor properties
		Supplier<DescriptorItem> supplier = () -> createVanillaItemFromDescriptor(descriptor);

		// register with DeferredRegister; register() returns a RegistryObject<Item>
		var register = itemsRegisters.computeIfAbsent(id.getNamespace(),
				namespace -> DeferredRegister.create(Registries.ITEM, namespace));
		RegistryObject<Item> ro = register.register(id.getPath(), supplier);

		// store mapping
		registryObjects.put(id, ro);
		descriptorIds.put(descriptor, id);

		if (descriptor.getTab() != null)
			if (descriptor.getTab() instanceof CreativeTabMenu.Custom custom)
				CREATIVE_TABS.pinItemTo(custom.getId(), id);

		// still call parent logic for descriptor caching (if parent keeps descriptors)
		super.registerItem(id, descriptor);
	}

	// build a vanilla Item using descriptor (Item.Properties derived from descriptor.physicalProps)
	private DescriptorItem createVanillaItemFromDescriptor(ItemDescriptor descriptor) {
		ItemPhysicalProperties phys = descriptor.getPhysicalProps();

		Item.Properties props = new Item.Properties();
		props.stacksTo(phys.getStackable() ? phys.getMaxStackSize() : 1);
		if (phys.getFireResistant())
			props.fireResistant();
		props.rarity(ForgeHacks.fromVicky(phys.getRarity()));
		if (phys.getDurability() != null) {
			props.defaultDurability(phys.getDurability());
			props.durability(phys.getDurability());
		}
		if (descriptor.getFoodProps() != null)
			props.food(createVanillaFoodProperties(descriptor.getFoodProps()));

		if (descriptor.getModel() instanceof ItemModel.CustomItemModel)
			return new ExtendedDescriptorItem(descriptor, props);

		return new DescriptorItem(descriptor, props);
	}

	private net.minecraft.world.food.FoodProperties createVanillaFoodProperties(org.vicky.platform.items.FoodProperties foodProps) {
		var props = new net.minecraft.world.food.FoodProperties.Builder()
				.nutrition(foodProps.getNutrition())
				.saturationMod(foodProps.getSaturationModifier());

		if (foodProps.isMeat()) props.meat();
		if (foodProps.getCanAlwaysEat()) props.alwaysEat();
		if (foodProps.getFastFood()) props.fast();

		for (var effect : foodProps.getEffects()) {
			var forgeEffect = ForgeRegistries.MOB_EFFECTS.getHolder(ForgeHacks.fromVicky(effect.getEffect()));
			if (forgeEffect.isEmpty()) {
				getLogger().severe("Skipping invalid effect: {}", effect.getEffect());
				continue;
			}
			props.effect(
					() -> new MobEffectInstance(
							forgeEffect.get().get(),
							effect.getDuration(),
							effect.getAmplifier()
					),
					effect.getProbability()
			);
		}

		return props.build();
	}

	@Override
	protected @NotNull PlatformItemStack buildStackFromDescriptor(
			@NotNull ItemDescriptor descriptor,
			@NotNull Map<String, ?> overrides
	) {
		ResourceLocation id = descriptorIds.get(descriptor);

		if (id == null) {
			throw new IllegalStateException(
					"Descriptor is not registered: " + descriptor
			);
		}

		RegistryObject<Item> registryObject = registryObjects.get(id);

		if (registryObject == null) {
			throw new IllegalStateException(
					"No registry object exists for: " + id
			);
		}

		ItemStack stack = registryObject.get().getDefaultInstance();

		ForgeItemStack platformStack = new ForgeItemStack(stack);

		// TODO: Forge specific overrides apply[Forge]Overrides(platformStack, overrides);

		return platformStack;
	}

	@Override
	public @NotNull PlatformItemStack fromDescriptor(@NotNull ItemDescriptor itemDescriptor) {
		for (Map.Entry<ResourceLocation, RegistryObject<Item>> e : registryObjects.entrySet()) {
			ResourceLocation rl = e.getKey();
			ItemDescriptor regDesc = getDescriptor(rl);
			if (regDesc != null && regDesc.equals(itemDescriptor)) { // equals requires descriptor equality semantics
				ItemStack s = e.getValue().get().getDefaultInstance();
				return new ForgeItemStack(s);
			}
		}
		// fallback to building a transient delegate stack
		return buildStackFromDescriptor(itemDescriptor, Map.of());
	}

	@Override
	public @NotNull PlatformItemStack fromRegisteredDescriptor(@NotNull ResourceLocation resourceLocation) throws org.vicky.platform.items.DescriptorNotRegisteredException {
		RegistryObject<Item> ro = registryObjects.get(resourceLocation);
		if (ro == null) throw new org.vicky.platform.items.DescriptorNotRegisteredException("Descriptor not registered: " + resourceLocation);
		Item item = ro.get(); // safe to call after registration is complete
		ItemStack stack = item.getDefaultInstance();
		return new ForgeItemStack(stack);
	}

	@Override
	public @Nullable org.vicky.platform.world.PlatformMaterial materialOf(@NotNull ResourceLocation resourceLocation) {
		RegistryObject<Item> ro = registryObjects.get(resourceLocation);
		if (ro == null) return null;
		Item item = ro.get();
		// TODO: adapt to your ForgePlatformMaterial wrapper factory
		return new ForgePlatformMaterial(item);
	}

	@Override
	protected @Nullable PlatformItemStack createNativeItem(@NotNull ResourceLocation resourceLocation) {
		var possibleItem = ForgeRegistries.ITEMS.getValue(ForgeHacks.fromVicky(resourceLocation));
		if (possibleItem == null) return null;
		return new ForgeItemStack(possibleItem.getDefaultInstance());
	}

	@Override
	protected void applySerializedData(@NotNull PlatformItemStack platformItemStack, @NotNull ItemData itemData) {
		var serialised = itemData.serialize();
		if (!(platformItemStack instanceof ForgeItemStack itemStack)) return;
		if (serialised.format() == ItemDataFormat.LEGACY_NBT) {
			try {
				itemStack.delegate().deserializeNBT(TagParser.parseTag(serialised.payload()));
			} catch (CommandSyntaxException ignored) {
            }
        }
	}

	@Override
	public SerializedItemStack serialize(PlatformItemStack stack) {
		if (stack instanceof ForgeItemStack forgeItemStack) {
			return new SerializedItemStack(
					forgeItemStack.key().toString(),
					forgeItemStack.delegate().getCount(),
					ItemDataFormat.LEGACY_NBT,
					forgeItemStack.delegate().getOrCreateTag().getAsString(),
					null
			);
		}
		return null;
	}

	@Override
	public PlatformItemStack deserialize(SerializedItemStack serialized) {
		try {
			return new ForgeItemStack(new ItemStack(
					ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.parse(serialized.item())),
					serialized.count(),
					TagParser.parseTag(serialized.payload())
			));
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PlatformItemStack getEmpty() {
		return ForgeItemStack.EMPTY;
	}
}