package aethereal.render;

import aethereal.render.ColorUtil;
import aethereal.render.Draw2DProcessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DestrumIconRenderer {

    /**
     * Renders an isometric 3D Minecraft grass/dirt block cube with authentic axonometric lighting.
     */
    public static void render3DCube(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float r = size * 0.46f;
        float yOffset = hover ? -1.0f : 0.0f;

        // 1. Drop shadow underneath
        draw.a(matrices, cx - r * 1.1f, cy + r * 0.6f + yOffset, r * 2.2f, r * 0.8f, r * 0.4f, ColorUtil.a(0, 0, 0, 75));

        // Colors
        int topFace = hover ? ColorUtil.a(255, 230, 160, 255) : ColorUtil.a(235, 240, 250, 240);
        int leftFace = hover ? ColorUtil.a(230, 155, 65, 255) : ColorUtil.a(160, 175, 195, 230);
        int rightFace = hover ? ColorUtil.a(180, 110, 40, 255) : ColorUtil.a(110, 125, 145, 230);

        // 2. Left side face
        draw.a(matrices, cx - r * 0.95f, cy - r * 0.1f + yOffset, r * 0.95f, r * 1.15f, new Vector4f(0.5f, 0.5f, 2.0f, 0.5f), leftFace);

        // 3. Right side face
        draw.a(matrices, cx, cy - r * 0.1f + yOffset, r * 0.95f, r * 1.15f, new Vector4f(0.5f, 0.5f, 0.5f, 2.0f), rightFace);

        // 4. Center vertical seam shadow
        draw.a(matrices, cx - 0.4f, cy - r * 0.1f + yOffset, 0.8f, r * 1.15f, 0.4f, ColorUtil.a(0, 0, 0, 40));

        // 5. Isometric Top Face (Exact 4-point diamond)
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy - r * 0.68f + yOffset, 0.0f).color(topFace);
        buffer.vertex(matrix, cx + r * 0.95f, cy - r * 0.1f + yOffset, 0.0f).color(topFace);
        buffer.vertex(matrix, cx, cy + r * 0.48f + yOffset, 0.0f).color(topFace);
        buffer.vertex(matrix, cx - r * 0.95f, cy - r * 0.1f + yOffset, 0.0f).color(topFace);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
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
        draw.a(matrices, cx - bodyW * 0.5f, cy + r * 0.05f + yOffset, bodyW, bodyH, new Vector4f(bodyH * 0.8f, bodyH * 0.8f, 2.0f, 2.0f), bodyCol);

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
     * Renders a tactile 3D rounded 'X' cross with realistic bevel, depth extrusion, and specular gloss.
     * Uses direct POSITION_COLOR hardware rendering for 100% reliable visibility and crisp anti-aliased appearance.
     */
    public static void render3DCrossExit(MatrixStack matrices, Draw2DProcessor draw, float cx, float cy, float size, boolean hover, float anim, int accentColor) {
        float span = 4.4f;
        float thick = 2.2f;
        float yOffset = hover ? -1.0f : 0.0f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // 1. Drop shadow layer underneath (offset down-right)
        float shX = cx + 0.8f;
        float shY = cy + 1.2f + yOffset;
        int shadowCol = ColorUtil.a(0, 0, 0, 95);
        drawCapsule(buffer, matrix, shX - span, shY - span, shX + span, shY + span, thick + 0.6f, shadowCol);
        drawCapsule(buffer, matrix, shX + span, shY - span, shX - span, shY + span, thick + 0.6f, shadowCol);

        // 2. Volumetric 3D Bottom Bevel (depth extrusion downwards)
        float bevY = cy + 1.0f + yOffset;
        int bevelCol = hover ? ColorUtil.a(190, 75, 35, 255) : ColorUtil.a(115, 120, 135, 245);
        drawCapsule(buffer, matrix, cx - span, bevY - span, cx + span, bevY + span, thick + 0.3f, bevelCol);
        drawCapsule(buffer, matrix, cx + span, bevY - span, cx - span, bevY + span, thick + 0.3f, bevelCol);

        // 3. Volumetric Mid Bevel (ambient transition)
        float midY = cy + 0.5f + yOffset;
        int midCol = hover ? ColorUtil.a(230, 110, 45, 255) : ColorUtil.a(165, 175, 195, 250);
        drawCapsule(buffer, matrix, cx - span, midY - span, cx + span, midY + span, thick, midCol);
        drawCapsule(buffer, matrix, cx + span, midY - span, cx - span, midY + span, thick, midCol);

        // 4. Front Lit Face
        float faceY = cy + yOffset;
        int frontCol = hover ? ColorUtil.a(255, 150, 60, 255) : ColorUtil.a(245, 248, 255, 255);
        drawCapsule(buffer, matrix, cx - span, faceY - span, cx + span, faceY + span, thick, frontCol);
        drawCapsule(buffer, matrix, cx + span, faceY - span, cx - span, faceY + span, thick, frontCol);

        // 5. Specular highlight line along top-facing edges
        float specY = cy - 0.4f + yOffset;
        int specCol = hover ? ColorUtil.a(255, 240, 200, 255) : ColorUtil.a(255, 255, 255, 255);
        float specSpan = span * 0.75f;
        drawBar(buffer, matrix, cx - specSpan, specY - specSpan, cx + specSpan, specY + specSpan, 0.85f, specCol);
        drawBar(buffer, matrix, cx + specSpan, specY - specSpan, cx - specSpan, specY + specSpan, 0.85f, specCol);

        // 6. Center glossy boss / facet
        drawCap(buffer, matrix, cx, faceY, thick * 0.65f, frontCol);
        drawCap(buffer, matrix, cx - 0.2f, faceY - 0.3f, thick * 0.35f, specCol);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
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

        // 2. 3D Volumetric Plus ('+') inside badge using direct hardware rendering
        float arm = 5.2f;
        float thick = 2.4f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Drop shadow
        float shX = cx + 0.6f;
        float shY = cy + 0.9f + yOffset;
        int shadowCol = ColorUtil.a(0, 0, 0, 80);
        drawCapsule(buffer, matrix, shX - arm, shY, shX + arm, shY, thick + 0.4f, shadowCol);
        drawCapsule(buffer, matrix, shX, shY - arm, shX, shY + arm, thick + 0.4f, shadowCol);

        // Bottom bevel
        float bevY = cy + 0.8f + yOffset;
        int bevCol = hover ? ColorUtil.a(190, 80, 40, 255) : ColorUtil.a(130, 140, 155, 240);
        drawCapsule(buffer, matrix, cx - arm, bevY, cx + arm, bevY, thick + 0.2f, bevCol);
        drawCapsule(buffer, matrix, cx, bevY - arm, cx, bevY + arm, thick + 0.2f, bevCol);

        // Front face
        float faceY = cy + yOffset;
        int plusCol = hover ? ColorUtil.a(255, 245, 210, 255) : ColorUtil.a(245, 250, 255, 245);
        drawCapsule(buffer, matrix, cx - arm, faceY, cx + arm, faceY, thick, plusCol);
        drawCapsule(buffer, matrix, cx, faceY - arm, cx, faceY + arm, thick, plusCol);

        // Specular highlight on horizontal bar
        float specY = faceY - 0.4f;
        int specCol = hover ? ColorUtil.a(255, 255, 240, 255) : ColorUtil.a(255, 255, 255, 230);
        drawBar(buffer, matrix, cx - arm * 0.8f, specY, cx + arm * 0.8f, specY, 0.85f, specCol);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawBar(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float thick, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return;
        float nx = dx / len;
        float ny = dy / len;
        float px = -ny * (thick * 0.5f);
        float py = nx * (thick * 0.5f);

        buffer.vertex(matrix, x1 + px, y1 + py, 0.0f).color(color);
        buffer.vertex(matrix, x2 + px, y2 + py, 0.0f).color(color);
        buffer.vertex(matrix, x2 - px, y2 - py, 0.0f).color(color);
        buffer.vertex(matrix, x1 - px, y1 - py, 0.0f).color(color);
    }

    private static void drawCapsule(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float thick, int color) {
        drawBar(buffer, matrix, x1, y1, x2, y2, thick, color);
        drawCap(buffer, matrix, x1, y1, thick * 0.5f, color);
        drawCap(buffer, matrix, x2, y2, thick * 0.5f, color);
    }

    private static void drawCap(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float r, int color) {
        int segs = 8;
        for (int i = 0; i < segs; i += 2) {
            float a0 = (float) (i * Math.PI * 2.0 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2.0 / segs);
            float a2 = (float) ((i + 2) * Math.PI * 2.0 / segs);
            buffer.vertex(matrix, cx, cy, 0.0f).color(color);
            buffer.vertex(matrix, cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r, 0.0f).color(color);
            buffer.vertex(matrix, cx + (float) Math.cos(a1) * r, cy + (float) Math.sin(a1) * r, 0.0f).color(color);
            buffer.vertex(matrix, cx + (float) Math.cos(a2) * r, cy + (float) Math.sin(a2) * r, 0.0f).color(color);
        }
    }
}
