package aethereal.render;

import aethereal.render.ColorUtil;
import aethereal.render.Draw2DProcessor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector4f;

public class DestrumIconRenderer {

    /**
     * Renders an isometric 3D Minecraft grass/dirt block cube with authentic axonometric lighting.
     */
    public static void render3DCube(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float r = size * 0.46f;
        // Subtle hover float
        float yOffset = hover ? -1.0f : 0.0f;

        // 1. Drop shadow underneath
        draw.a(matrices, cx - r * 1.1f, cy + r * 0.6f + yOffset, r * 2.2f, r * 0.8f, r * 0.4f, ColorUtil.a(0, 0, 0, 75));

        // Colors
        int topFace = hover ? ColorUtil.a(255, 230, 160, 255) : ColorUtil.a(235, 240, 250, 240);
        int leftFace = hover ? ColorUtil.a(230, 155, 65, 255) : ColorUtil.a(160, 175, 195, 230);
        int rightFace = hover ? ColorUtil.a(180, 110, 40, 255) : ColorUtil.a(110, 125, 145, 230);
        int rimLight = hover ? ColorUtil.a(255, 255, 220, 255) : ColorUtil.a(255, 255, 255, 160);

        // 2. Left side face
        draw.a(matrices, cx - r * 0.95f, cy - r * 0.1f + yOffset, r * 0.95f, r * 1.15f, new Vector4f(0.5f, 0.5f, 2.0f, 0.5f), leftFace);

        // 3. Right side face
        draw.a(matrices, cx, cy - r * 0.1f + yOffset, r * 0.95f, r * 1.15f, new Vector4f(0.5f, 0.5f, 0.5f, 2.0f), rightFace);

        // 4. Center vertical seam shadow
        draw.a(matrices, cx - 0.4f, cy - r * 0.1f + yOffset, 0.8f, r * 1.15f, 0.4f, ColorUtil.a(0, 0, 0, 40));

        // 5. Isometric Top Face (rotated 45 deg, squashed vertically)
        matrices.push();
        matrices.translate(cx, cy - r * 0.38f + yOffset, 0.0f);
        matrices.scale(1.0f, 0.55f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0f));
        float diamondSide = r * 0.98f;
        draw.a(matrices, -diamondSide * 0.5f, -diamondSide * 0.5f, diamondSide, diamondSide, 1.2f, topFace);
        // Specular highlight outline on top
        draw.a(matrices, -diamondSide * 0.5f, -diamondSide * 0.5f, diamondSide, diamondSide, 1.2f, 0.75f, rimLight);
        matrices.pop();
    }

    /**
     * Renders a 3D volumetric planet / globe with orbital ring.
     */
    public static void render3DGlobe(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float r = size * 0.40f;
        float yOffset = hover ? -1.0f : 0.0f;

        // 1. Drop shadow underneath
        draw.a(matrices, cx - r * 1.1f, cy + r * 0.65f + yOffset, r * 2.2f, r * 0.7f, r * 0.35f, ColorUtil.a(0, 0, 0, 75));

        // 2. Back half of the orbital ring (drawn behind planet)
        matrices.push();
        matrices.translate(cx, cy + yOffset, 0.0f);
        matrices.scale(1.0f, 0.38f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-24.0f));
        float ringR = r * 1.75f;
        int backRing = hover ? ColorUtil.a(230, 140, 50, 130) : ColorUtil.a(150, 165, 185, 110);
        draw.a(matrices, -ringR * 0.5f, -ringR * 0.5f, ringR, ringR, ringR * 0.5f, 1.1f, backRing);
        matrices.pop();

        // 3. Central 3D sphere body
        int sphereCol = hover ? ColorUtil.a(240, 160, 60, 245) : ColorUtil.a(180, 195, 215, 235);
        draw.a(matrices, cx - r, cy - r + yOffset, r * 2.0f, r * 2.0f, r, sphereCol);

        // Volumetric sphere shading (dark bottom-right)
        draw.a(matrices, cx - r * 0.2f, cy - r * 0.2f + yOffset, r * 1.2f, r * 1.2f, r * 0.6f, ColorUtil.a(0, 0, 0, hover ? 35 : 45));

        // Specular highlight on top-left of sphere
        int specCol = hover ? ColorUtil.a(255, 245, 210, 255) : ColorUtil.a(255, 255, 255, 210);
        draw.a(matrices, cx - r * 0.65f, cy - r * 0.65f + yOffset, r * 0.75f, r * 0.75f, r * 0.375f, specCol);

        // 4. Front half of the orbital ring (drawn in front)
        matrices.push();
        matrices.translate(cx, cy + yOffset, 0.0f);
        matrices.scale(1.0f, 0.38f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-24.0f));
        int frontRing = hover ? ColorUtil.a(255, 215, 140, 245) : ColorUtil.a(230, 240, 255, 215);
        draw.a(matrices, -ringR * 0.5f, 0.0f, ringR, ringR * 0.5f, new Vector4f(0.0f, 0.0f, ringR * 0.5f, ringR * 0.5f), 1.3f, frontRing);

        // Glowing orbital node
        float nodeR = 1.35f;
        draw.a(matrices, ringR * 0.46f - nodeR, -nodeR, nodeR * 2.0f, nodeR * 2.0f, nodeR, hover ? ColorUtil.a(255, 240, 180, 255) : -1);
        matrices.pop();
    }

    /**
     * Renders a 3D Apple-style volumetric user avatar bust.
     */
    public static void render3DUser(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float yOffset = hover ? -1.0f : 0.0f;
        float r = size * 0.40f;

        // 1. Drop shadow
        draw.a(matrices, cx - r * 1.0f, cy + r * 0.7f + yOffset, r * 2.0f, r * 0.7f, r * 0.35f, ColorUtil.a(0, 0, 0, 75));

        int headCol = hover ? ColorUtil.a(255, 225, 150, 255) : ColorUtil.a(235, 240, 250, 245);
        int bodyCol = hover ? ColorUtil.a(240, 165, 70, 245) : ColorUtil.a(175, 190, 210, 230);
        int specCol = hover ? ColorUtil.a(255, 250, 220, 255) : ColorUtil.a(255, 255, 255, 200);

        // 2. 3D Body / Shoulders
        float bodyW = r * 1.9f;
        float bodyH = r * 0.95f;
        draw.a(matrices, cx - bodyW * 0.5f, cy + r * 0.1f + yOffset, bodyW, bodyH, new Vector4f(r * 0.45f, r * 0.45f, 2.0f, 2.0f), bodyCol);
        // Bevel highlight on collar
        draw.a(matrices, cx - bodyW * 0.35f, cy + r * 0.1f + yOffset, bodyW * 0.7f, 1.0f, 0.5f, specCol);

        // 3. 3D Head
        float headR = r * 0.52f;
        float headY = cy - r * 0.6f + yOffset;
        draw.a(matrices, cx - headR, headY - headR, headR * 2.0f, headR * 2.0f, headR, headCol);
        // Head specular highlight
        draw.a(matrices, cx - headR * 0.55f, headY - headR * 0.6f, headR * 0.75f, headR * 0.75f, headR * 0.375f, specCol);
    }

    /**
     * Renders a 3D mechanical cogwheel gear with animated hover rotation.
     */
    public static void render3DGear(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float r = size * 0.40f;
        float yOffset = hover ? -1.0f : 0.0f;

        // 1. Drop shadow
        draw.a(matrices, cx - r * 1.05f, cy - r * 0.95f + yOffset + 1.0f, r * 2.1f, r * 2.1f, r * 1.05f, ColorUtil.a(0, 0, 0, 65));

        matrices.push();
        matrices.translate(cx, cy + yOffset, 0.0f);
        // Gentle rotation on hover
        float angle = hover ? (System.currentTimeMillis() % 3600L) * 0.1f : 0.0f;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));

        int toothCol = hover ? ColorUtil.a(240, 160, 60, 245) : ColorUtil.a(180, 195, 215, 235);
        int hubCol = hover ? ColorUtil.a(255, 215, 140, 255) : ColorUtil.a(220, 230, 245, 245);
        int holeCol = ColorUtil.a(16, 20, 28, 230);
        int rimLight = hover ? ColorUtil.a(255, 245, 210, 255) : ColorUtil.a(255, 255, 255, 180);

        // 2. Teeth (3 rotated cross bars forming 6 teeth)
        float toothL = r * 2.2f;
        float toothW = r * 0.54f;
        for (int i = 0; i < 3; i++) {
            draw.a(matrices, -toothL * 0.5f, -toothW * 0.5f, toothL, toothW, 1.0f, toothCol);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(60.0f));
        }

        // 3. Center gear hub
        float hubR = r * 0.78f;
        draw.a(matrices, -hubR, -hubR, hubR * 2.0f, hubR * 2.0f, hubR, hubCol);
        draw.a(matrices, -hubR, -hubR, hubR * 2.0f, hubR * 2.0f, hubR, 0.8f, rimLight);

        // 4. Center axle hole
        float holeR = r * 0.32f;
        draw.a(matrices, -holeR, -holeR, holeR * 2.0f, holeR * 2.0f, holeR, holeCol);

        matrices.pop();
    }

    /**
     * Renders a tactile 3D rounded 'X' cross with realistic bevel, depth, and specular gloss.
     */
    public static void render3DCrossExit(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float barL = size * 0.44f;
        float barT = size * 0.12f;
        float radius = barT * 0.5f;
        float yOffset = hover ? -1.0f : 0.0f;

        // 1. Drop shadow layer underneath (offset down-right)
        matrices.push();
        matrices.translate(cx + 0.7f, cy + 0.9f + yOffset, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0f));
        draw.a(matrices, -barL * 0.5f, -barT * 0.5f, barL, barT, radius, ColorUtil.a(0, 0, 0, 85));
        draw.a(matrices, -barT * 0.5f, -barL * 0.5f, barT, barL, radius, ColorUtil.a(0, 0, 0, 85));
        matrices.pop();

        // 2. Volumetric side bevel layer (slightly darker)
        int bevelCol = hover ? ColorUtil.a(210, 85, 45, 255) : ColorUtil.a(150, 160, 175, 230);
        matrices.push();
        matrices.translate(cx, cy + 0.4f + yOffset, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0f));
        draw.a(matrices, -barL * 0.5f, -barT * 0.5f, barL, barT, radius, bevelCol);
        draw.a(matrices, -barT * 0.5f, -barL * 0.5f, barT, barL, radius, bevelCol);
        matrices.pop();

        // 3. Front lit face
        int frontCol = hover ? ColorUtil.a(255, 125, 65, 255) : ColorUtil.a(240, 245, 255, 245);
        int specCol = hover ? ColorUtil.a(255, 220, 180, 255) : ColorUtil.a(255, 255, 255, 220);

        matrices.push();
        matrices.translate(cx, cy + yOffset, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0f));
        draw.a(matrices, -barL * 0.5f, -barT * 0.5f, barL, barT, radius, frontCol);
        draw.a(matrices, -barT * 0.5f, -barL * 0.5f, barT, barL, radius, frontCol);

        // 4. Gloss specular highlight line along top edge
        draw.a(matrices, -barL * 0.5f + 0.4f, -barT * 0.5f + 0.2f, barL - 0.8f, 0.65f, 0.325f, specCol);
        draw.a(matrices, -barT * 0.5f + 0.2f, -barL * 0.5f + 0.4f, 0.65f, barL - 0.8f, 0.325f, specCol);
        matrices.pop();
    }

    /**
     * Renders a volumetric 3D frosted badge with a 3D '+' cross (for "+ Новый мир" and "+ Добавить сервер").
     */
    public static void render3DPlusBadge(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, boolean hover, float anim, int accentColor) {
        float bSize = 28.0f;
        float bRad = 8.0f;
        float yOffset = hover ? -2.0f : 0.0f;

        // 1. Frosted square badge with outline
        draw.a(matrices, cx - bSize * 0.5f, cy - bSize * 0.5f + yOffset, bSize, bSize, bRad,
            ColorUtil.a(255, 255, 255, hover ? 45 : 22));
        int borderCol = hover ? accentColor : ColorUtil.a(255, 255, 255, 55);
        draw.a(matrices, cx - bSize * 0.5f, cy - bSize * 0.5f + yOffset, bSize, bSize, bRad, hover ? 1.25f : 1.0f, borderCol);

        // 2. 3D Volumetric Plus ('+') inside badge
        float barL = 12.0f;
        float barT = 2.6f;
        float rad = barT * 0.5f;

        // Drop shadow
        draw.a(matrices, cx - barL * 0.5f + 0.6f, cy - barT * 0.5f + 0.8f + yOffset, barL, barT, rad, ColorUtil.a(0, 0, 0, 75));
        draw.a(matrices, cx - barT * 0.5f + 0.6f, cy - barL * 0.5f + 0.8f + yOffset, barT, barL, rad, ColorUtil.a(0, 0, 0, 75));

        // Front lit face
        int plusCol = hover ? ColorUtil.a(255, 245, 210, 255) : ColorUtil.a(245, 250, 255, 240);
        draw.a(matrices, cx - barL * 0.5f, cy - barT * 0.5f + yOffset, barL, barT, rad, plusCol);
        draw.a(matrices, cx - barT * 0.5f, cy - barL * 0.5f + yOffset, barT, barL, rad, plusCol);

        // Specular highlight on horizontal bar
        draw.a(matrices, cx - barL * 0.5f + 0.4f, cy - barT * 0.5f + 0.3f + yOffset, barL - 0.8f, 0.75f, 0.375f, ColorUtil.a(255, 255, 255, 180));
    }
}
