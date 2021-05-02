package dev.lazurite.grabz.util;

import dev.lazurite.grabz.core.util.Raycast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class RaycastUtil {
    public static @Nullable Entity getEntityToGrab(PlayerEntity player) {
        Vec3d vec3d = player.getCameraPosVec(1.0f);
        Vec3d vec3d2 = player.getRotationVec(1.0f);
        double d = 5.0;
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        Box box = player.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult result = Raycast.raycast(player, vec3d, vec3d3, box, RaycastUtil::shouldGrab, d);

        if (result != null && result.getEntity() != null) {
            if (result.getEntity().hasVehicle()) {
                return result.getEntity().getVehicle();
            } else {
                return result.getEntity();
            }
        }

        return null;
    }

    public static @Nullable BlockPos getBlockToGrab(PlayerEntity player) {
        HitResult result = player.raycast(4.5, 1.0f, false);

        if (result.getType() != HitResult.Type.MISS) {
            return ((BlockHitResult) result).getBlockPos();
        }

        return null;
    }

    public static boolean shouldGrab(Entity entity) {
        return true;
    }
}
