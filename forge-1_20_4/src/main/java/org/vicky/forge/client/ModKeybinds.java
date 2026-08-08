package org.vicky.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeybinds {
    public static final KeyMapping INSPECT_KEY = new KeyMapping(
            "key.vutls.inspect", // Translation key
            KeyConflictContext.IN_GAME, // Only works while playing (not in menus)
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I, // Default key: I
            "key.categories.vutls" // Category name
    );

    @SubscribeEvent
    public static void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(INSPECT_KEY);
    }
}