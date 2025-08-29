package org.vicky.forgeplatform;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.vicky.forgeplatform.useables.ForgePlatformBlockStateAdapter;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformBlockStateFactory;

import static org.vicky.VickyUtilitiesForge.access;

public class ForgePlatformBlockStateFactory implements PlatformBlockStateFactory {
    public static BlockState parseBlockState(String input) throws CommandSyntaxException {
        HolderLookup<Block> provider = access.lookupOrThrow(Registries.BLOCK);
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
