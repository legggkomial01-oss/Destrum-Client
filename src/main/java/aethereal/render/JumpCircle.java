package aethereal.render;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record JumpCircle(Vec3d pos, long createdAt, float lifetimeMs) {
    public float getProgress(long time) {
        return MathHelper.clamp((float) (time - this.createdAt) / this.lifetimeMs, 0.0f, 1.0f);
    }

    public boolean isExpired(long time) {
        return (float) (time - this.createdAt) >= this.lifetimeMs;
    }
}
