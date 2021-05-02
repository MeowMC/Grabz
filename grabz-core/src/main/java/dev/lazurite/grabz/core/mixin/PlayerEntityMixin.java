package dev.lazurite.grabz.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.lazurite.grabz.core.util.Grabber;
import dev.lazurite.grabz.core.entity.player.GrabberStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements GrabberStorage {
    private Grabber grabber;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(World world, BlockPos pos, float yaw, GameProfile profile, CallbackInfo info) {
        this.grabber = new Grabber((PlayerEntity) (Object) this);
    }

    @Override
    public Grabber getGrabber() {
        return this.grabber;
    }
}
