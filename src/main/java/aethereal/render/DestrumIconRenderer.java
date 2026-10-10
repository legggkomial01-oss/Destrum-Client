package aethereal.render;

import aethereal.core.Interface;
import aethereal.render.ColorUtil;
import aethereal.render.Draw2DProcessor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * High-end icon renderer for Destrum-V2 dock and cards.
 * Uses pristine anti-aliased 256x256 vector textures rendered through hardware bilinear filtering.
 */
public class DestrumIconRenderer {

    private static final Identifier ICON_CUBE = Identifier.of("delta", "pictures/destrum_cube.png");
    private static final Identifier ICON_GLOBE = Identifier.of("delta", "pictures/destrum_globe.png");
    private static final Identifier ICON_USER = Identifier.of("delta", "pictures/destrum_user.png");
    private static final Identifier ICON_GEAR = Identifier.of("delta", "pictures/destrum_gear.png");
    private static final Identifier ICON_CROSS = Identifier.of("delta", "pictures/destrum_cross.png");
    private static final Identifier ICON_PLUS = Identifier.of("delta", "pictures/destrum_plus.png");

    public static void render3DCube(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float btnSize, boolean hover, float anim, int accentColor) {
        float iconSize = hover ? 16.5f : 15.0f;
        float yOffset = hover ? -0.8f : 0.0f;
        int tint = hover ? ColorUtil.a(255, 230, 170, 255) : -1;

        draw.a(matrices, ICON_CUBE, cx - iconSize * 0.5f, cy - iconSize * 0.5f + yOffset, iconSize, iconSize, 0.0f, tint);
    }

    public static void render3DGlobe(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float btnSize, boolean hover, float anim, int accentColor) {
        float iconSize = hover ? 16.5f : 15.0f;
        float yOffset = hover ? -0.8f : 0.0f;
        int tint = hover ? ColorUtil.a(255, 230, 170, 255) : -1;

        draw.a(matrices, ICON_GLOBE, cx - iconSize * 0.5f, cy - iconSize * 0.5f + yOffset, iconSize, iconSize, 0.0f, tint);
    }

    public static void render3DUser(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float btnSize, boolean hover, float anim, int accentColor) {
        float iconSize = hover ? 16.0f : 14.5f;
        float yOffset = hover ? -0.8f : 0.0f;
        int tint = hover ? ColorUtil.a(255, 230, 170, 255) : -1;

        draw.a(matrices, ICON_USER, cx - iconSize * 0.5f, cy - iconSize * 0.5f + yOffset, iconSize, iconSize, 0.0f, tint);
    }

    public static void render3DGear(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float btnSize, boolean hover, float anim, int accentColor) {
        float iconSize = hover ? 16.5f : 15.0f;
        float yOffset = hover ? -0.8f : 0.0f;
        int tint = hover ? ColorUtil.a(255, 230, 170, 255) : -1;

        matrices.push();
        matrices.translate(cx, cy + yOffset, 0.0f);
        if (hover) {
            float rot = (float) ((System.currentTimeMillis() % 4000L) / 4000.0 * 360.0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rot));
        }
        draw.a(matrices, ICON_GEAR, -iconSize * 0.5f, -iconSize * 0.5f, iconSize, iconSize, 0.0f, tint);
        matrices.pop();
    }

    public static void render3DCrossExit(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float btnSize, boolean hover, float anim, int accentColor) {
        float iconSize = hover ? 15.5f : 14.0f;
        float yOffset = hover ? -0.8f : 0.0f;
        int tint = hover ? ColorUtil.a(255, 170, 110, 255) : -1;

        draw.a(matrices, ICON_CROSS, cx - iconSize * 0.5f, cy - iconSize * 0.5f + yOffset, iconSize, iconSize, 0.0f, tint);
    }

    public static void render3DPlusBadge(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, boolean hover, float anim, int accentColor) {
        float bSize = 28.0f;
        float bRad = 8.0f;
        float yOffset = hover ? -2.0f : 0.0f;

        // Frosted badge background
        draw.a(matrices, cx - bSize * 0.5f, cy - bSize * 0.5f + yOffset, bSize, bSize, bRad,
            ColorUtil.a(255, 255, 255, hover ? 45 : 22));
        int borderCol = hover ? accentColor : ColorUtil.a(255, 255, 255, 55);
        draw.a(matrices, cx - bSize * 0.5f, cy - bSize * 0.5f + yOffset, bSize, bSize, bRad, hover ? 1.25f : 1.0f, borderCol);

        // Pristine vector plus icon inside
        float plusSize = 16.0f;
        int tint = hover ? ColorUtil.a(255, 240, 200, 255) : -1;
        draw.a(matrices, ICON_PLUS, cx - plusSize * 0.5f, cy - plusSize * 0.5f + yOffset, plusSize, plusSize, 0.0f, tint);
    }
}
