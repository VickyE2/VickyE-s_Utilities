package org.vicky.forge.forgeplatform.useables;

import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.platform.items.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Objects;

public class ExtendedAnimationController<T extends PlatformBasedLivingEntity> extends AnimationController<T> {
    public ExtendedAnimationController(T animatable, String name, int transitionTickTime, ExtendedAnimationStateHandler<T> animationHandler) {
        super(animatable, name, transitionTickTime, null);
        this.stateHandler = animationHandler;
    }

    private final ExtendedAnimationStateHandler<T> stateHandler;
    public Animation outgoingAnimation;
    public Animation incomingAnimation;

    public void play(Animation animation) {
        if (animation == null) return;

        if (this.outgoingAnimation != null) {
            // 1. Don't restart the exact same animation
            if (Objects.equals(this.outgoingAnimation.getKey(), animation.getKey())) {
                return;
            }

            // 2. Check if the current animation blocks the new one
            // (Consider using >= so equal priority doesn't interrupt uninterruptable ones)
            if (!this.outgoingAnimation.getInterruptable() &&
                    this.outgoingAnimation.getPriority() >= animation.getPriority()) {
                return;
            }
        }

        this.incomingAnimation = animation;
        this.transitionLength = animation.getBlendTime();
        var animationBuilder = RawAnimation.begin();

        if (incomingAnimation.getLoop())
            animationBuilder.thenLoop(incomingAnimation.getKey());
        else animationBuilder.thenPlay(incomingAnimation.getKey());

        this.setAnimation(animationBuilder);

        this.outgoingAnimation = incomingAnimation;
        this.incomingAnimation = null;
    }

    public void stop(@Nullable String animation) {
        if (animation == null) stop();
        else if (this.outgoingAnimation.getKey().equals(animation) ||
                this.incomingAnimation.getKey().equals(animation)) {
            stop();
        }
    }

    public void pause() {
        this.animationState = State.PAUSED;
    }

    public void resume() {
        this.animationState = State.RUNNING;
    }

    @Override
    protected PlayState handleAnimationState(AnimationState<T> state) {
        if (this.triggeredAnimation != null) {
            if (this.currentRawAnimation != this.triggeredAnimation)
                this.currentAnimation = null;

            setAnimation(this.triggeredAnimation);

            if (!hasAnimationFinished() && (!this.handlingTriggeredAnimations ||
                    this.stateHandler.handle(
                            new ExtendedAnimationState<>(this, state)) == PlayState.CONTINUE))
                return PlayState.CONTINUE;

            this.triggeredAnimation = null;
            this.needsAnimationReload = true;
        }

        return this.stateHandler.handle(new ExtendedAnimationState<>(this, state));
    }
}
