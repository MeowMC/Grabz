package dev.lazurite.grabz.core;

import dev.lazurite.grabz.core.entity.render.GrabbedBlockEntityRenderer;
import dev.lazurite.grabz.core.util.Grabber;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class GrabzCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(GrabzCore.GRABBED_BLOCK_ENTITY, (manager, context) -> new GrabbedBlockEntityRenderer(manager));

        ClientPlayNetworking.registerGlobalReceiver(GrabzCore.GRAB, (client, handler, buf, sender) -> {
            UUID playerId = buf.readUuid();
            int entityId = buf.readInt();

            client.execute(() -> {
                PlayerEntity player = client.world.getPlayerByUuid(playerId);
                Entity entity = client.world.getEntityById(entityId);

                if (player != null && entity != null) {
                    Grabber.get(player).grabEntity(entity);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GrabzCore.STOP_GRAB, (client, handler, buf, sender) -> {
            UUID playerId = buf.readUuid();

            client.execute(() -> {
                PlayerEntity player = client.world.getPlayerByUuid(playerId);

                if (player != null) {
                    Grabber.get(player).stopGrab();
                }
            });
        });
    }
}
