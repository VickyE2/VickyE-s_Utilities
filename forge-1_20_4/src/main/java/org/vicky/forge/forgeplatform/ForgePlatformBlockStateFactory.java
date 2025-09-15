package org.vicky.forge.forgeplatform;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.vicky.VickyUtilitiesForge;
import org.vicky.forge.forgeplatform.useables.ForgePlatformBlockStateAdapter;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformBlockStateFactory;

public class ForgePlatformBlockStateFactory implements PlatformBlockStateFactory {
    private static HolderLookup.Provider lookup;

    public static void setLookupProvider(HolderLookup.Provider provider) {
        VickyUtilitiesForge.LOGGER.info("Setting lookup provider to: {}", provider);
        lookup = provider;
    }

    public static BlockState parseBlockState(String input) throws CommandSyntaxException {
        HolderLookup<Block> provider;
        if (lookup != null) provider = lookup.lookupOrThrow(Registries.BLOCK);
        else provider = BuiltInRegistries.BLOCK.asLookup();
        StringReader reader = new StringReader(input);
        BlockStateParser.BlockResult result = BlockStateParser.parseForBlock(provider, reader, true);
        return result.blockState();
    }

    @Override
    public PlatformBlockState<?> getBlockState(String type) {
        try {
            var state = parseBlockState(type);
            return new ForgePlatformBlockStateAdapter(state);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}
