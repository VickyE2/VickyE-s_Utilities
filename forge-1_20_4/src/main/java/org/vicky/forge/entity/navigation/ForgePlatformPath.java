package org.vicky.forge.entity.navigation;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.AbstractPath;
import org.vicky.platform.entity.AbstractPathNode;
import org.vicky.platform.utils.IntVec3;

public class ForgePlatformPath implements AbstractPath {
    public final Path ordinal;

    private ForgePlatformPath(Path nav) {
        this.ordinal = nav;
    }

    public static ForgePlatformPath from(Path nav) { return new ForgePlatformPath(nav); }

    @Override
    public @Nullable AbstractPathNode getCurrent() {
        return ForgePlatformNode.from(ordinal.getNode(ordinal.getNextNodeIndex() - 1));
    }

    @Override
    public @Nullable AbstractPathNode getEnd() {
        if (ordinal.getEndNode() == null) return null;
        return ForgePlatformNode.from(ordinal.getEndNode());
    }

    @Override
    public boolean isFinished() {
        return ordinal.isDone();
    }

    @Override
    public void advance() {
        ordinal.advance();
    }

    @Override
    public int length() {
        return ordinal.getNodeCount();
    }

    @Override
    public @NotNull IntVec3 target() {
        return ForgeHacks.toVicky(ordinal.getTarget());
    }
}
