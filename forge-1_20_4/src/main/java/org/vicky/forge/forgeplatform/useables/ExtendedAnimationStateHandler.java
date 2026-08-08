package org.vicky.forge.forgeplatform.useables;

import org.vicky.forge.entity.PlatformBasedLivingEntity;
import software.bernie.geckolib.core.object.PlayState;

@FunctionalInterface
public interface ExtendedAnimationStateHandler<T extends PlatformBasedLivingEntity> {
    PlayState handle(ExtendedAnimationState<T> state);
}
