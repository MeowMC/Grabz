package dev.lazurite.grabz.core.util;

import com.jme3.bullet.collision.shapes.EmptyShape;
import com.jme3.bullet.joints.SixDofSpringJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import dev.lazurite.grabz.core.GrabzCore;
import dev.lazurite.grabz.core.entity.physics.EntityRigidBody;
import dev.lazurite.grabz.core.entity.GrabbedBlockEntity;
import dev.lazurite.grabz.core.entity.player.GrabberStorage;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.util.math.VectorHelper;
import dev.lazurite.rayon.entity.api.EntityPhysicsElement;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class Grabber {
    private final PlayerEntity player;
    private Optional<Grab> grab = Optional.empty();

    public static Grabber get(PlayerEntity player) {
        return ((GrabberStorage) player).getGrabber();
    }

    public Grabber(PlayerEntity player) {
        this.player = player;
    }

    public void step() {
        grab.ifPresent(grab -> {
            grab.rigidBody.activate();
            grab.pointBody.setPhysicsLocation(VectorHelper.vec3dToVector3f(player.getCameraPosVec(1.0f).add(player.getRotationVector().multiply(2f))));

            if (grab.rigidBody instanceof EntityRigidBody) {
                Vector3f location = grab.rigidBody.getPhysicsLocation(new Vector3f());
                grab.getEntity().updatePosition(location.x, location.y - grab.getEntity().getBoundingBox().getYLength() / 2.0, location.z);
            }
        });
    }

    public void grabEntity(Entity entity) {
        if (!(grab.isPresent() && GrabzCore.isGrabbed(entity))) {
            grab = Optional.of(new Grab(entity));

            if (player instanceof ServerPlayerEntity) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeUuid(player.getUuid());
                buf.writeInt(entity.getEntityId());
                PlayerLookup.tracking(entity).forEach(p -> ServerPlayNetworking.send(p, GrabzCore.GRAB, buf));
            }
        }
    }

    public void grabBlock(BlockPos pos) {
        World world = player.getEntityWorld();
        BlockState state = world.getBlockState(pos);

        if (!state.getBlock().canMobSpawnInside() && player.world.getBlockEntity(pos) == null) {
            GrabbedBlockEntity grabbedBlock = new GrabbedBlockEntity(player.world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
            world.removeBlock(pos, false);
            world.spawnEntity(grabbedBlock);
            grabEntity(grabbedBlock);
        }
    }

    public void stopGrab() {
        grab.ifPresent(grab -> {
            MinecraftSpace space = MinecraftSpace.get(player.world);

            space.getThread().execute(() -> {
                if (grab.rigidBody instanceof EntityRigidBody) {
                    space.removeCollisionObject(grab.rigidBody);
                }

                space.removeCollisionObject(grab.pointBody);
                space.removeJoint(grab.joint);
            });

            if (player instanceof ServerPlayerEntity) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeUuid(player.getUuid());
                PlayerLookup.tracking(grab.getEntity()).forEach(p -> ServerPlayNetworking.send(p, GrabzCore.STOP_GRAB, buf));

                if (grab.rigidBody instanceof EntityRigidBody) {
                    Vec3d velocity = VectorHelper.vector3fToVec3d(grab.rigidBody.getLinearVelocity(new Vector3f()).multLocal(0.05f));
                    grab.getEntity().addVelocity(velocity.x, velocity.y, velocity.z);
                }
            }

            this.grab = Optional.empty();
        });
    }

    public boolean isGrabbing() {
        return this.grab.isPresent();
    }

    public boolean isGrabbing(Entity entity) {
        return this.grab.map(value -> value.getEntity().equals(entity)).orElse(false);
    }

    public Grab getGrab() {
        return this.grab.orElse(null);
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public class Grab {
        private final Entity entity;

        private final PhysicsRigidBody rigidBody;
        private final SixDofSpringJoint joint;
        private final PhysicsRigidBody pointBody;

        public Grab(Entity entity) {
            this.entity = entity;

            if (entity instanceof EntityPhysicsElement) {
                rigidBody = ((EntityPhysicsElement) entity).getRigidBody();
            } else {
                rigidBody = new EntityRigidBody(entity);
            }

            Vector3f pos = VectorHelper.vec3dToVector3f(player.getCameraPosVec(1.0f).add(player.getRotationVector().multiply(2f)));
            pointBody = new PhysicsRigidBody(new EmptyShape(false), 0);
            pointBody.setPhysicsLocation(pos);

            joint = new SixDofSpringJoint(rigidBody, pointBody, Vector3f.ZERO, Vector3f.ZERO, Matrix3f.IDENTITY, Matrix3f.IDENTITY, false);
            joint.setLinearLowerLimit(Vector3f.ZERO);
            joint.setLinearUpperLimit(Vector3f.ZERO);
            joint.setAngularLowerLimit(Vector3f.ZERO);
            joint.setAngularUpperLimit(Vector3f.ZERO);

            MinecraftSpace space = MinecraftSpace.get(entity.getEntityWorld());

            space.getThread().execute(() -> {
                if (!rigidBody.isInWorld()) {
                    space.addCollisionObject(rigidBody);
                }

                space.addCollisionObject(pointBody);
                space.addJoint(joint);
            });
        }

        public Entity getEntity() {
            return this.entity;
        }
    }
}
