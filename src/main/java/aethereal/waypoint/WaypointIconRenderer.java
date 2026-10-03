package aethereal.waypoint;

import aethereal.core.Delta;
import net.minecraft.client.util.math.MatrixStack;

public final class WaypointIconRenderer {
    private WaypointIconRenderer() {
    }

    public static void draw(MatrixStack matrices, WaypointIcon icon, float x, float y, float size, int color) {
        Delta.h().d().i().a(matrices, icon.texture(), x, y, size, size, 0.0f, color);
    }
}
