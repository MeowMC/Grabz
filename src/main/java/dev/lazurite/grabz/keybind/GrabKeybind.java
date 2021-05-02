package dev.lazurite.grabz.keybind;

import dev.lazurite.grabz.Grabz;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GrabKeybind {
    public static void register() {
        KeyBinding grab = new KeyBinding(
                "key." + Grabz.MODID + ".grab",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key." + Grabz.MODID + ".category"
        );

        KeyBindingHelper.registerKeyBinding(grab);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && grab.wasPressed()) {
                ClientPlayNetworking.send(Grabz.GRAB, PacketByteBufs.create());
            }
        });
    }
}
