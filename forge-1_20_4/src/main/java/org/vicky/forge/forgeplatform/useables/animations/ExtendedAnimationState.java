package org.vicky.forge.forgeplatform.useables.animations;

import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.platform.items.Animation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class ExtendedAnimationState<T extends PlatformBasedLivingEntity> {
    private final AnimationState<T> delegate;
    private final ExtendedAnimationController<T> controller;

    public ExtendedAnimationState(ExtendedAnimationController<T> controller, AnimationState<T> delegate) {
        this.delegate = delegate;
        this.controller = controller;
    }

    public void setAnimation(Animation animation) {
        controller.setResolvedAnimation(animation);
    }

    public PlayState setAndContinue(Animation animation) {
        controller.setResolvedAnimation(animation);

        return PlayState.CONTINUE;
    }

    public ExtendedAnimationController<T> getController() {
        return controller;
    }

    public AnimationState<T> getDelegate() {
        return delegate;
    }
}
