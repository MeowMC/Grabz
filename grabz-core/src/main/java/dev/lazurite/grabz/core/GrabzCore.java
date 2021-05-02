package dev.lazurite.grabz.core;

import dev.lazurite.grabz.core.entity.GrabbedBlockEntity;
import dev.lazurite.grabz.core.util.Grabber;
import dev.lazurite.rayon.core.api.event.ElementCollisionEvents;
import dev.lazurite.rayon.core.api.event.PhysicsSpaceEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class GrabzCore implements ModInitializer {
	public static final String MODID = "grabz-core";
	public static final Logger LOGGER = LogManager.getLogger("Grabz Core");

	public static final Identifier GRAB = new Identifier(MODID, "grab");
	public static final Identifier STOP_GRAB = new Identifier(MODID, "stop_grab");

	public static EntityType<Entity> GRABBED_BLOCK_ENTITY;

	public static boolean isGrabbed(Entity entity) {
		return getGrabber(entity) != null;
	}

	public static Grabber getGrabber(Entity entity) {
		for (PlayerEntity player : entity.world.getPlayers()) {
			Grabber grabber = Grabber.get(player);

			if (grabber.isGrabbing(entity)) {
				return grabber;
			}
		}

		return null;
	}

	public static List<Grabber.Grab> getGrabs(World world) {
		return world.getPlayers().stream().map(player -> Grabber.get(player).getGrab()).collect(Collectors.toList());
	}

	@Override
	public void onInitialize() {
		GRABBED_BLOCK_ENTITY = Registry.register(Registry.ENTITY_TYPE,
				new Identifier(MODID, "grabbed_block_entity"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, GrabbedBlockEntity::new)
						.dimensions(EntityDimensions.fixed(1.0F, 1.0F))
						.build());

		EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
			if (isGrabbed(entity)) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeUuid(getGrabber(entity).getPlayer().getUuid());
				buf.writeInt(entity.getEntityId());
				ServerPlayNetworking.send(player, STOP_GRAB, buf);
			}
		});

		EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
			if (isGrabbed(entity)) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeUuid(getGrabber(entity).getPlayer().getUuid());
				ServerPlayNetworking.send(player, STOP_GRAB, buf);
			}
		});

		PhysicsSpaceEvents.STEP.register(space -> {
			space.getThread().getWorldSupplier().getWorlds().forEach(world -> {
				world.getPlayers().forEach(player -> {
					Grabber.get(player).step();
				});
			});
		});

		ElementCollisionEvents.BLOCK_COLLISION.register((executor, element, block, impulse) -> {

		});
	}
}
