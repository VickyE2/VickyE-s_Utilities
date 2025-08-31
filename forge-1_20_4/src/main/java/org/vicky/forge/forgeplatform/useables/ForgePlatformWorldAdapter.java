package org.vicky.forge.forgeplatform.useables;

import de.pauleff.api.ICompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformBlock;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformWorld;

import java.util.List;
import java.util.stream.Collectors;

public record ForgePlatformWorldAdapter(Level world) implements PlatformWorld<BlockState, Level> {

    @Override
    public String getName() {
        return world.dimension().location().toString();
    }

    @Override
    public Level getNative() {
        return world;
    }

    public int getHighestBlockYAt(double x, double z) {
        // Forge/vanilla equivalent of Bukkit#getHighestBlockYAt
        BlockPos pos = world.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, // blocks movement (like solid or leaves)
                new BlockPos((int) x, 0, (int) z)
        );
        return pos.getY();
    }

    @Override
    public int getMaxWorldHeight() {
        return world.getMaxBuildHeight();
    }

    @Override
    public List<PlatformPlayer> getPlayers() {
        return world.players().stream().map(ServerPlayer.class::cast).map(ForgePlatformPlayer::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public PlatformBlock<BlockState> getBlockAt(double x, double y, double z) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        BlockState state = world.getBlockState(pos);
        return new ForgePlatformBlockAdapter(pos, state, world);
    }

    @Override
    public PlatformBlock<BlockState> getBlockAt(Vec3 pos) {
        return getBlockAt(pos.x, pos.y, pos.z);
    }

    @Override
    public PlatformBlockState<BlockState> getAirBlockState() {
        return new ForgePlatformBlockStateAdapter(Blocks.AIR.defaultBlockState());
    }

    @Override
    public PlatformBlockState<BlockState> getWaterBlockState() {
        return new ForgePlatformBlockStateAdapter(Blocks.WATER.defaultBlockState());
    }

    @Override
    public void setPlatformBlockState(Vec3 position, PlatformBlockState<BlockState> state) {
        BlockPos pos = new BlockPos((int) position.x, (int) position.y, (int) position.z);
        world.setBlock(pos, state.getNative(), 3); // 3 = update clients + block updates
    }

    @Override
    public void setPlatformBlockState(Vec3 position, PlatformBlockState<BlockState> state, ICompoundTag nbt) {
        BlockPos pos = new BlockPos((int) position.x, (int) position.y, (int) position.z);
        world.setBlock(pos, state.getNative(), 3); // 3 = update clients + block updates
    }

    @Override
    public PlatformBlockState<BlockState> createPlatformBlockState(String id, String properties) {
        // id = like "minecraft:oak_log"
        // properties = something like "[axis=y]"
        ResourceLocation rl = new ResourceLocation(id);
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block == null) return null;

        BlockState state = block.defaultBlockState();

        // TODO: parse `properties` and apply (you'll need to split like axis=y, facing=north, etc.)
        // Example:
        // state = state.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);

        return new ForgePlatformBlockStateAdapter(state);
    }


    @Override
    public void loadChunkIfNeeded(int chunkX, int chunkZ) {
        // Force-load chunk if not loaded
        world.getChunkSource().getChunk(chunkX, chunkZ, true);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return world.hasChunk(chunkX, chunkZ);
    }

}
