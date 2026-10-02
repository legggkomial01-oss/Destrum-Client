package aethereal.render;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record JumpWave(Vec3d pos, long createdAt, float lifetimeMs, float strength, float maxRadius) {
    public float getProgress(long time) {
        return MathHelper.clamp((float) (time - this.createdAt) / this.lifetimeMs, 0.0f, 1.0f);
    }

    public boolean isExpired(long time) {
        return (float) (time - this.createdAt) >= this.lifetimeMs;
    }
}
