package aethereal.module.render;

import static aethereal.core.Interface.aM_;
import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.DrawEvent;
import aethereal.event.JumpEvent;
import aethereal.render.ColorUtil;
import aethereal.render.JumpCircle;
import aethereal.render.JumpWave;
import aethereal.render.SnapshotFramebuffer;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@ModuleRegister(a = "Jump Circles", b = "Создает эффект кругов или волны при прыжке", c = Category.Render)
public class JumpCircles extends Module {
    private static final ShaderProgramKey SHADER_CIRCLE = new ShaderProgramKey(
        Identifier.of("delta", "core/jump_circle"), VertexFormats.POSITION_TEXTURE, Defines.EMPTY
    );
    private static final ShaderProgramKey SHADER_WAVE = new ShaderProgramKey(
        Identifier.of("delta", "core/jump_wave"), VertexFormats.POSITION_TEXTURE, Defines.EMPTY
    );

    private final ModeSetting mode = new ModeSetting("Режим", "Круги", "Круги", "Волна");
    private final ColorSetting color = new ColorSetting("Цвет", Integer.valueOf(ColorUtil.a(119, 101, 255, 255)));
    private final SliderSetting duration = new SliderSetting("Длительность", 800.0f, 200.0f, 3000.0f, 10.0f);
    private final SliderSetting radius = new SliderSetting("Радиус", 1.5f, 0.2f, 5.0f, 0.1f);
    private final SliderSetting waveStrength = new SliderSetting("Сила волны", 0.6f, 0.05f, 2.0f, 0.05f);
    private final SliderSetting waveRadius = new SliderSetting("Радиус волны", 3.0f, 0.5f, 10.0f, 0.1f);
    private final SliderSetting waveDuration = new SliderSetting("Длительность волны", 600.0f, 150.0f, 2500.0f, 10.0f);
    private final SliderSetting waveDistortion = new SliderSetting("Искажение", 1.0f, 0.1f, 2.0f, 0.05f);

    private final List<JumpCircle> circles = new ArrayList<>();
    private final List<JumpWave> waves = new ArrayList<>();
    private final SnapshotFramebuffer snapshotFramebuffer = new SnapshotFramebuffer();

    private ShaderProgram circleProgram;
    private GlUniform circleUniformTime;
    private GlUniform circleUniformAlpha;
    private GlUniform circleUniformTint;

    private ShaderProgram waveProgram;
    private GlUniform waveUniformProgress;
    private GlUniform waveUniformStrength;
    private GlUniform waveUniformFade;
    private GlUniform waveUniformCrestColor;

    public JumpCircles() {
        this.duration.a(() -> this.mode.l("Круги"));
        this.radius.a(() -> this.mode.l("Круги"));
        this.waveStrength.a(() -> this.mode.l("Волна"));
        this.waveRadius.a(() -> this.mode.l("Волна"));
        this.waveDuration.a(() -> this.mode.l("Волна"));
        this.waveDistortion.a(() -> this.mode.l("Волна"));
        a(this.mode, this.color, this.duration, this.radius, this.waveStrength, this.waveRadius, this.waveDuration, this.waveDistortion);
    }

    @Override
    public void c() {
        super.c();
        this.circles.clear();
        this.waves.clear();
        this.snapshotFramebuffer.close();
        this.circleProgram = null;
        this.waveProgram = null;
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (event.b() == aM_.player) {
            Vec3d pos = aM_.player.getPos();
            long now = System.currentTimeMillis();
            if (this.mode.l("Круги")) {
                this.circles.add(new JumpCircle(pos, now, this.duration.c()));
            } else {
                this.waves.add(new JumpWave(
                    pos, now, this.waveDuration.c(),
                    this.waveStrength.c() * this.waveDistortion.c(),
                    this.waveRadius.c()
                ));
            }
        }
    }

    @EventTarget
    public void onDraw(DrawEvent event) {
        if (!event.c() || aM_.player == null || aM_.world == null) return;
        long now = System.currentTimeMillis();
        this.circles.removeIf(c -> c.isExpired(now));
        this.waves.removeIf(w -> w.isExpired(now));
        if (this.circles.isEmpty() && this.waves.isEmpty()) return;

        updateShaderPrograms();

        Matrix4f matrix4f = event.h().peek().getPositionMatrix();
        Vec3d cameraPos = aM_.gameRenderer.getCamera().getPos();
        int col = this.color.c();
        float timeSec = (float) (now % 1000000L) / 1000.0f;

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        try {
            renderCircles(cameraPos, timeSec, now, matrix4f, col);
            renderWaves(col, now, cameraPos, matrix4f);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void updateShaderPrograms() {
        if (this.circleProgram == null) {
            try {
                this.circleProgram = aM_.getShaderLoader().getOrCreateProgram(SHADER_CIRCLE);
                if (this.circleProgram != null) {
                    this.circleUniformTime = this.circleProgram.getUniform("uTime");
                    this.circleUniformAlpha = this.circleProgram.getUniform("uAlpha");
                    this.circleUniformTint = this.circleProgram.getUniform("uTint");
                }
            } catch (Throwable ignored) {
            }
        }
        if (this.waveProgram == null) {
            try {
                this.waveProgram = aM_.getShaderLoader().getOrCreateProgram(SHADER_WAVE);
                if (this.waveProgram != null) {
                    this.waveUniformProgress = this.waveProgram.getUniform("uProgress");
                    this.waveUniformStrength = this.waveProgram.getUniform("uStrength");
                    this.waveUniformFade = this.waveProgram.getUniform("uFade");
                    this.waveUniformCrestColor = this.waveProgram.getUniform("uCrestColor");
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private void renderCircles(Vec3d cameraPos, float timeSec, long now, Matrix4f matrix, int col) {
        if (this.circleProgram == null || this.circles.isEmpty()) return;
        float baseRadius = this.radius.c();
        if (baseRadius <= 0.0f) return;

        RenderSystem.setShader(this.circleProgram);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
            GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE
        );

        if (this.circleUniformTime != null) {
            this.circleUniformTime.set(timeSec);
        }
        if (this.circleUniformTint != null) {
            this.circleUniformTint.set(
                (col >> 16 & 0xFF) * 0.003921569f,
                (col >> 8 & 0xFF) * 0.003921569f,
                (col & 0xFF) * 0.003921569f
            );
        }

        for (JumpCircle circle : this.circles) {
            float progress = circle.getProgress(now);
            if (this.circleUniformAlpha != null) {
                this.circleUniformAlpha.set(1.0f - progress);
            }
            float r = baseRadius * easeOutCubic(progress);
            drawQuad(r, cameraPos, matrix, circle.pos(), 0.04f);
        }
    }

    private void renderWaves(int col, long now, Vec3d cameraPos, Matrix4f matrix) {
        if (this.waveProgram == null || this.waves.isEmpty()) return;

        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShader(this.waveProgram);

        if (this.waveUniformCrestColor != null) {
            this.waveUniformCrestColor.set(
                (col >> 16 & 0xFF) * 0.003921569f,
                (col >> 8 & 0xFF) * 0.003921569f,
                (col & 0xFF) * 0.003921569f
            );
        }

        for (JumpWave wave : this.waves) {
            float progress = wave.getProgress(now);
            float fade = waveFade(progress);
            if (wave.strength() <= 0.001f || fade <= 0.003f) continue;

            SimpleFramebuffer sfb = this.snapshotFramebuffer.getSimpleFramebuffer();
            if (sfb == null) return;

            this.waveProgram.addSamplerTexture("SceneSampler", sfb.getColorAttachment());

            if (this.waveUniformProgress != null) {
                this.waveUniformProgress.set(easeOutCubic(progress));
            }
            if (this.waveUniformStrength != null) {
                this.waveUniformStrength.set(wave.strength());
            }
            if (this.waveUniformFade != null) {
                this.waveUniformFade.set(fade);
            }

            drawQuad(wave.maxRadius(), cameraPos, matrix, wave.pos(), 0.03f);
        }
    }

    private static float easeOutCubic(float value) {
        float f = 1.0f - MathHelper.clamp(value, 0.0f, 1.0f);
        return 1.0f - f * f * f;
    }

    private static float waveFade(float value) {
        float f = MathHelper.clamp(value, 0.0f, 1.0f);
        float in = MathHelper.clamp(f / 0.18f, 0.0f, 1.0f);
        float out = MathHelper.clamp((1.0f - f) / 0.82f, 0.0f, 1.0f);
        return MathHelper.clamp(in * out, 0.0f, 1.0f);
    }

    private void drawQuad(float r, Vec3d camPos, Matrix4f matrix, Vec3d circlePos, float yOffset) {
        float x = (float) (circlePos.x - camPos.x);
        float y = (float) (circlePos.y + yOffset - camPos.y);
        float z = (float) (circlePos.z - camPos.z);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x + r, y, z + r).texture(1.0f, 1.0f);
        buffer.vertex(matrix, x + r, y, z - r).texture(1.0f, 0.0f);
        buffer.vertex(matrix, x - r, y, z - r).texture(0.0f, 0.0f);
        buffer.vertex(matrix, x - r, y, z + r).texture(0.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
