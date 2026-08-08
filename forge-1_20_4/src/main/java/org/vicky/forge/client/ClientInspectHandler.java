package org.vicky.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.forge.forgeplatform.item.InspectManager;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.InspectStatePacket;

@Mod.EventBusSubscriber(modid = "yourmod", value = Dist.CLIENT)
public class ClientInspectHandler {
    
    private static boolean wasDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean isDown = ModKeybinds.INSPECT_KEY.isDown();

        // State changed (Key just pressed OR just released)
        if (isDown != wasDown) {
            wasDown = isDown;

            ItemStack mainHand = mc.player.getMainHandItem();
            
            // Check if the item is inspectable (e.g., checks a custom tag, item class, or component)
            boolean isInspectable = isItemInspectable(mainHand); 

            if (isInspectable || !isDown) { // Always allow telling the server we *stopped* inspecting
                // TODO: Send packet to server
                PacketHandler.sendToServer(new InspectStatePacket(isDown));
                // Optionally update client-side manager immediately for instant visual feedback
                InspectManager.setInspecting(mc.player, isDown); 
            }
        }
    }

    private static boolean isItemInspectable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // Example: return stack.is(ModTags.Items.INSPECTABLE);
        return stack.getItem() instanceof ExtendedDescriptorItem;
    }
}