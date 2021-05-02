package dev.lazurite.grabz;

import dev.lazurite.grabz.core.util.Grabber;
import dev.lazurite.grabz.keybind.GrabKeybind;
import dev.lazurite.grabz.util.RaycastUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Grabz implements ModInitializer, ClientModInitializer {
	public static final String MODID = "grabz";
	public static final Logger LOGGER = LogManager.getLogger("Grabz");

	public static final Identifier GRAB = new Identifier(MODID, "grab");

	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(GRAB, (server, player, handler, buf, sender) -> {
			Grabber grabber = Grabber.get(player);

			if (grabber.isGrabbing()) {
				grabber.stopGrab();
			} else {
				Entity entity = RaycastUtil.getEntityToGrab(player);

				if (entity == null) {
					BlockPos pos = RaycastUtil.getBlockToGrab(player);

					if (pos != null) {
						grabber.grabBlock(pos);
					}
				} else {
					grabber.grabEntity(entity);
				}
			}
		});
	}

	@Override
	public void onInitializeClient() {
		GrabKeybind.register();
	}
}
