package org.vicky.forge.forgeplatform.item.animation;

import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.platform.items.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Objects;

public class ExtendedItemAnimationController<T extends ExtendedDescriptorItem> extends AnimationController<T> {
    public ExtendedItemAnimationController(T animatable, String name, int transitionTickTime, ExtendedItemAnimationStateHandler<T> animationHandler) {
        super(animatable, name, transitionTickTime, null);
        this.stateHandler = animationHandler;
    }

    private final ExtendedItemAnimationStateHandler<T> stateHandler;
    private Animation currentAnimation;
    private Animation manualAnimation;

    public void play(Animation animation) {
        if (animation == null)
            return;

        /*
         * If a manual animation is already playing,
         * apply priority/interrupt rules against that.
         */
        if (manualAnimation != null) {
            if (Objects.equals(
                    manualAnimation.getKey(),
                    animation.getKey()
            )) {
                return;
            }

            if (!manualAnimation.getInterruptable()
                    && manualAnimation.getPriority() >= animation.getPriority()) {
                return;
            }
        }

        manualAnimation = animation;
        currentAnimation = animation;

        applyAnimation(animation);

        if (this.animationState == State.STOPPED)
            this.animationState = State.RUNNING;
    }

    public void stop(@Nullable String animation) {
        if (animation == null) {
            stop();
            return;
        }

        if (manualAnimation != null
                && animation.equals(manualAnimation.getKey())) {
            stop();
        }
    }

    @Override
    public void stop() {
        currentAnimation = null;
        manualAnimation = null;

        super.stop();
    }

    public void pauseAnimation() {
        animationState = State.PAUSED;
    }

    public void resumeAnimation() {
        animationState = State.RUNNING;
    }

    @Override
    protected PlayState handleAnimationState(AnimationState<T> state) {
        /*
         * Manual animation has priority over the resolver.
         */
        if (manualAnimation != null) {

            applyAnimation(manualAnimation);

            if (!manualAnimation.getLoop()
                    && hasAnimationFinished()) {

                manualAnimation = null;
                currentAnimation = null;
            }

            return PlayState.CONTINUE;
        }

        /*
         * IMPORTANT:
         *
         * The resolver runs every time GeckoLib evaluates
         * the controller.
         */

        return stateHandler.handle(
                new ExtendedItemAnimationState<>(this, state)
        );
    }

    private void applyAnimation(Animation animation) {
        if (animation == null)
            return;

        transitionLength = animation.getBlendTime();

        RawAnimation raw = RawAnimation.begin();

        if (animation.getLoop()) {
            raw.thenLoop(animation.getKey());
        } else {
            raw.thenPlay(animation.getKey());
        }

        setAnimation(raw);

        currentAnimation = animation;
    }

    Animation getCurrentAnimationObject() {
        return currentAnimation;
    }

    Animation getManualAnimation() {
        return manualAnimation;
    }

    boolean isManualControlled() {
        return manualAnimation != null;
    }

    void setResolvedAnimation(Animation animation) {
        if (manualAnimation != null)
            return;

        if (animation == null) {
            currentAnimation = null;
            stop();
            return;
        }

        if (currentAnimation == null || animation.getPriority() > currentAnimation.getPriority() || this.animationState == State.STOPPED) {
            currentAnimation = animation;
            applyAnimation(animation);

            if (this.animationState == State.STOPPED)
                this.animationState = State.RUNNING;
        }
    }
}
