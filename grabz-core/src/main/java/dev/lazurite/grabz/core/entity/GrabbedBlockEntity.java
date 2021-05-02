package dev.lazurite.grabz.core.entity;

import dev.lazurite.grabz.core.GrabzCore;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody;
import dev.lazurite.rayon.entity.api.EntityPhysicsElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class GrabbedBlockEntity extends Entity implements EntityPhysicsElement {
    private static final TrackedData<Optional<BlockState>> BLOCK_STATE = DataTracker.registerData(GrabbedBlockEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
    private static final TrackedData<BlockPos> BLOCK_POS = DataTracker.registerData(GrabbedBlockEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private final ElementRigidBody rigidBody = new ElementRigidBody(this);

    public GrabbedBlockEntity(EntityType<?> type, World world) {
        super(type, world);
        getRigidBody().setDragCoefficient(0.001f);
        getRigidBody().setMass(1.5f);
    }

    public GrabbedBlockEntity(World world, double x, double y, double z, BlockState blockState) {
        this(GrabzCore.GRABBED_BLOCK_ENTITY, world);
        this.updatePosition(x, y, z);
        this.setBlockState(blockState);
        this.setFallingBlockPos(new BlockPos(x, y, z));
    }

    @Override
    public void step(MinecraftSpace space) {

    }

    @Override
    public ElementRigidBody getRigidBody() {
        return this.rigidBody;
    }

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(BLOCK_STATE, Optional.ofNullable(Blocks.AIR.getDefaultState()));
        this.getDataTracker().startTracking(BLOCK_POS, getBlockPos());
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
        setBlockState(Block.getStateFromRawId(tag.getInt("block_state")));
        setFallingBlockPos(new BlockPos(tag.getInt("block_x"), tag.getInt("block_y"), tag.getInt("block_z")));
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
        if (getBlockState() == null) {
            tag.putInt("block_state", 0);
        } else {
            tag.putInt("block_state", Block.getRawIdFromState(getBlockState()));
        }

        tag.putInt("block_x", getBlockPos().getX());
        tag.putInt("block_y", getBlockPos().getY());
        tag.putInt("block_z", getBlockPos().getZ());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return getSpawnPacket();
    }

    public BlockState getBlockState() {
        return getDataTracker().get(BLOCK_STATE).orElse(null);
    }

    public void setBlockState(BlockState blockState) {
        getDataTracker().set(BLOCK_STATE, Optional.ofNullable(blockState));
    }

    public BlockPos getFallingBlockPos() {
        return getDataTracker().get(BLOCK_POS);
    }

    public void setFallingBlockPos(BlockPos blockPos) {
        getDataTracker().set(BLOCK_POS, blockPos);
    }
}
