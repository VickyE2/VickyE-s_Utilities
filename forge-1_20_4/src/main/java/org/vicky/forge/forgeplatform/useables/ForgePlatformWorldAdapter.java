/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import de.pauleff.api.ICompoundTag;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformEntity;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.entity.PlatformLivingEntity;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformBlock;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
        BlockPos pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, // blocks movement (like solid or leaves)
                new BlockPos((int) x, 0, (int) z));
        return pos.getY();
    }

    @Override
    public PlatformBlock<BlockState> getHighestBlockAt(double x, double z) {
        int y = getHighestBlockYAt(x, z);
        return getBlockAt(x, y, z);
    }

    @Override
    public int getMaxWorldHeight() {
        return world.getMaxBuildHeight();
    }

    @Override
    public @NotNull List<? extends @NotNull PlatformEntity> getEntities() {
        if (world instanceof ServerLevel serverLevel) {
            var list = new ArrayList<PlatformEntity>();
            serverLevel.getAllEntities().forEach(e -> {
                if (e instanceof LivingEntity le) {
                    list.add(ForgePlatformLivingEntity.from(le));
                }
                else if (e instanceof Player pe) {
                    list.add(ForgePlatformPlayer.from(pe));
                }
                else {
                    list.add(ForgePlatformEntity.from(e));
                }
            });
            return list;
        }
        return List.of();
    }

    @Override
    public @NotNull List<? extends @NotNull PlatformEntity> getEntitiesWithin(double x, double y, double z, float r) {
        if (world instanceof ServerLevel serverLevel) {
            var list = new ArrayList<PlatformEntity>();
            serverLevel.getEntities().get(
                    AABB.ofSize(new net.minecraft.world.phys.Vec3(x, y, z), r, r, r),
            e -> {
                if (e instanceof LivingEntity le) {
                    list.add(ForgePlatformLivingEntity.from(le));
                }
                else if (e instanceof Player pe) {
                    list.add(ForgePlatformPlayer.from(pe));
                }
                else {
                    list.add(ForgePlatformEntity.from(e));
                }
            });
            return list;
        }
        return List.of();
    }

    @Override
    public @NotNull List<? extends @NotNull PlatformLivingEntity> getLivingEntitiesWithin(double x, double y, double z, float r) {
        if (world instanceof ServerLevel serverLevel) {
            var list = new ArrayList<PlatformLivingEntity>();
            serverLevel.getEntities().get(
                    AABB.ofSize(new net.minecraft.world.phys.Vec3(x, y, z), r, r, r),
                    e -> {
                        if (e instanceof LivingEntity le) {
                            list.add(ForgePlatformLivingEntity.from(le));
                        }
                        else if (e instanceof Player pe) {
                            list.add(ForgePlatformPlayer.from(pe));
                        }
                    });
            return list;
        }
        return List.of();
    }

    @Override
    public List<PlatformPlayer> getPlayers() {
        return world.players().stream().map(ServerPlayer.class::cast).map(ForgePlatformPlayer::new)
                .collect(Collectors.toUnmodifiableList());
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
        ResourceLocation rl = ResourceLocation.parse(id);
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block == null) return null;

        BlockState state = block.defaultBlockState();
        if (properties != null && !properties.isEmpty()) {
            // properties = "axis=y,facing=north"
            for (String propPair : properties.replace("[", "").replace("]", "").split(",")) {
                String[] kv = propPair.split("=");
                if (kv.length != 2) continue;
                String key = kv[0].trim();
                String value = kv[1].trim();

                AtomicReference<Property<?>> props = new AtomicReference<>();
                state.getProperties().stream()
                        .filter(p -> p.getName().equals(key))
                        .findFirst()
                        .ifPresent(props::set);

                try {
                    if (props.get() != null) {
                        var p = props.get();
                        Comparable<?> parsed = p.getValueClass()
                                .cast(BlockStateParser.parseForBlock(getNative().holderLookup(Registries.BLOCK), value, true));
                        state = setBlockStateValue(state, p, parsed);
                    }
                } catch (Exception ignored) {}
            }
        }
        return new ForgePlatformBlockStateAdapter(state);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Comparable<T>> BlockState setBlockStateValue(BlockState state, Property<?> property, Comparable<?> value) {
        state = state.setValue((Property) property, (T) value);
        return state;
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

    @Override
    public PlatformBlock<BlockState> raycastBlock(Vec3 eyeLocation, Vec3 lookDirection, Float range) {
        BlockPos start = new BlockPos((int) eyeLocation.x, (int) eyeLocation.y, (int) eyeLocation.z);
        net.minecraft.world.phys.Vec3 endVec = new net.minecraft.world.phys.Vec3(
                eyeLocation.x + lookDirection.x * range,
                eyeLocation.y + lookDirection.y * range,
                eyeLocation.z + lookDirection.z * range
        );
        BlockHitResult result = world.clip(new ClipContext(
                new net.minecraft.world.phys.Vec3(eyeLocation.x, eyeLocation.y, eyeLocation.z),
                endVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        ));
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = result.getBlockPos();
            return getBlockAt(hitPos.getX(), hitPos.getY(), hitPos.getZ());
        }
        return null;
    }

}
