package org.vicky.forge.forgeplatform.item;

import org.vicky.platform.items.Animation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class ExtendedItemAnimationState<T extends ExtendedDescriptorItem> {
    private final AnimationState<T> delegate;
    private final ExtendedItemAnimationController<T> controller;

    public ExtendedItemAnimationState(ExtendedItemAnimationController<T> controller, AnimationState<T> delegate) {
        this.delegate = delegate;
        this.controller = controller;
    }

    public void setAnimation(Animation animation) {
        controller.play(animation);
    }

    public PlayState setAndContinue(Animation animation) {
        controller.play(animation);

        return PlayState.CONTINUE;
    }

    public AnimationState<T> getDelegate() {
        return delegate;
    }
}
