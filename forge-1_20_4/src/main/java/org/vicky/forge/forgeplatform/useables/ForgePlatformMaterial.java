package org.vicky.forge.forgeplatform.useables;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.world.PlatformMaterial;

public record ForgePlatformMaterial(ItemLike material) implements PlatformMaterial {

    @Override
    public boolean isSolid() {
        return material.asItem() instanceof BlockItem;
    }

    @Override
    public boolean isAir() {
        Item item = material.asItem();

        // Item.AIR covers the "no item" case
        if (item == Items.AIR) return true;

        // If it's a block, check for air blocks
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
        }

        return false;
    }


    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.from(ForgeRegistries.ITEMS.getKey(material.asItem()).toString());
    }
}
