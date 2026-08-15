package org.vicky.forge.forgeplatform.item.animation;

import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import software.bernie.geckolib.core.object.PlayState;

@FunctionalInterface
public interface ExtendedItemAnimationStateHandler<T extends ExtendedDescriptorItem> {
    PlayState handle(ExtendedItemAnimationState<T> state);
}
