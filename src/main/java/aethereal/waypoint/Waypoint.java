package aethereal.waypoint;

import java.util.UUID;
import net.minecraft.util.math.Vec3d;

public record Waypoint(UUID id, String name, Vec3d position, WaypointIcon icon, String dimension, long expiresAt) {
    public boolean temporary() {
        return this.expiresAt > 0L;
    }

    public boolean expired(long now) {
        return temporary() && now >= this.expiresAt;
    }
}
