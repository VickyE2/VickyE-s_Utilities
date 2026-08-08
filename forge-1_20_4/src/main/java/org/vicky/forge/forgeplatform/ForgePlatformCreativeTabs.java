package org.vicky.forge.forgeplatform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.VickyUtilitiesForge;
import org.vicky.forge.forgeplatform.item.ForgeItemStack;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.items.CreativeTabDescriptor;
import org.vicky.platform.items.CreativeTabMenu;
import org.vicky.platform.items.PlatformCreativeTabRegistry;
import org.vicky.platform.utils.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = VickyUtilitiesForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgePlatformCreativeTabs extends PlatformCreativeTabRegistry {

    private final Map<String, DeferredRegister<CreativeModeTab>> tabRegisters;
    private final Map<ResourceLocation, CreativeModeTab.Builder> registryObjects = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, List<ResourceLocation>> pinnedItems =
            new ConcurrentHashMap<>();
    private final Map<ResourceKey<CreativeModeTab>, List<ResourceLocation>>
            vanillaItems = new ConcurrentHashMap<>();

    public ForgePlatformCreativeTabs() {
        this.tabRegisters = new ConcurrentHashMap<>();
    }

    public void pinItemTo(
            @NotNull ResourceLocation creativeTab,
            @NotNull ResourceLocation item
    ) {
        if (!registryObjects.containsKey(creativeTab)) {
            throw new IllegalArgumentException(
                    "Creative tab is not registered: " + creativeTab
            );
        }

        pinnedItems
                .computeIfAbsent(
                        creativeTab,
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(item);
    }

    public void pinItemTo(
            @NotNull ResourceLocation itemId,
            @NotNull CreativeTabMenu.Inbuilt tab
    ) {
        ResourceKey<CreativeModeTab> key = ForgeHacks.fromVicky(tab);

        if (key == null) {
            throw new IllegalArgumentException(
                    "Unknown inbuilt creative tab: " + tab
            );
        }

        vanillaItems
                .computeIfAbsent(
                        key,
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(itemId);
    }

    @Override
    public void registerPlatformTab(@NotNull ResourceLocation id, @NotNull CreativeTabDescriptor descriptor) {
        var builder = CreativeModeTab.builder()
                .title(ForgeHacks.fromVicky(descriptor.getTitle()))
                .icon(() -> {
                    PlatformItemStack stack =
                            descriptor.getIcon().get();

                    if (stack instanceof ForgeItemStack forgeStack) {
                        return forgeStack.delegate();
                    }

                    return ItemStack.EMPTY;
                })
                .displayItems((parameters, output) -> {
                    List<ResourceLocation> items =
                            pinnedItems.get(id);

                    if (items == null) {
                        return;
                    }

                    for (ResourceLocation itemId : items) {
                        Item item = ForgeRegistries.ITEMS.getValue(ForgeHacks.fromVicky(itemId));

                        if (item != null) {
                            output.accept(item);
                        }
                    }
                });

        registryObjects.put(id, builder);
    }

    public void attachToEventBus(IEventBus modEventBus) {
        registryObjects.forEach((id, builder) -> {
            var register = tabRegisters.computeIfAbsent(id.getNamespace(),
                    namespace -> DeferredRegister.create(Registries.CREATIVE_MODE_TAB, namespace));
            register.register(id.getPath(), builder::build);
        });
        tabRegisters.forEach((ignored, it) -> it.register(modEventBus));
    }

    @SubscribeEvent
    public void onBuildCreativeTab(
            BuildCreativeModeTabContentsEvent event
    ) {
        List<ResourceLocation> items =
                vanillaItems.get(event.getTabKey());

        if (items == null) {
            return;
        }

        for (ResourceLocation itemId : items) {
            Item item = ForgeRegistries.ITEMS.getValue(ForgeHacks.fromVicky(itemId));

            if (item != null) {
                event.accept(item);
            }
        }
    }
}
