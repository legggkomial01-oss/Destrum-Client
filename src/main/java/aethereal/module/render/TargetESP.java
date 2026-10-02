package aethereal.module.render;

import static aethereal.core.Interface.aM_;
import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.AttackEvent;
import aethereal.event.DrawEvent;
import aethereal.render.ColorUtil;
import aethereal.render.targetesp.ParticleBuffer;
import aethereal.render.targetesp.TargetEffect;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@ModuleRegister(a = "Target ESP", b = "Отображает визуальный эффект вокруг атакуемой цели", c = Category.Render)
public class TargetESP extends Module {
    private static final ShaderProgramKey SHADER_TARGET = new ShaderProgramKey(
        Identifier.of("delta", "core/targetesp_glow"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY
    );

    private final ModeSetting effect = new ModeSetting("Эффект", "Кольцо", TargetEffect.NAMES.toArray(new String[0]));
    private final ColorSetting color = new ColorSetting("Цвет", Integer.valueOf(ColorUtil.a(119, 101, 255, 255)));
    private final SliderSetting speed = new SliderSetting("Скорость", 0.7f, 0.1f, 2.0f, 0.1f);
    private final SliderSetting duration = new SliderSetting("Длительность", 2.3f, 0.3f, 10.0f, 0.1f);
    private final SliderSetting thickness = new SliderSetting("Толщина", 0.12f, 0.02f, 0.5f, 0.01f);
    private final SliderSetting radius = new SliderSetting("Радиус", 1.2f, 0.5f, 3.0f, 0.1f);
    private final SliderSetting density = new SliderSetting("Плотность", 48.0f, 12.0f, 96.0f, 1.0f);
    private final SliderSetting tail = new SliderSetting("Хвост", 5.0f, 0.0f, 10.0f, 1.0f);
    private final SliderSetting glow = new SliderSetting("Свечение", 1.0f, 0.2f, 2.0f, 0.05f);

    private LivingEntity target1;
    private LivingEntity target2;

    private float alpha1 = 0.0f;
    private float alpha2 = 0.0f;
    private float prevAlpha1 = 0.0f;
    private float prevAlpha2 = 0.0f;

    private float phase1 = 0.0f;
    private float phase2 = 0.0f;
    private float prevPhase1 = 0.0f;
    private float prevPhase2 = 0.0f;

    private long attackTime = 0L;
    private boolean active = false;
    private boolean isReversing = false;
    private boolean isTarget1Active = false;
    private boolean isTarget2Active = false;

    private long lastFrameTime = 0L;
    private ShaderProgram shaderProgram;
    private final ParticleBuffer particleBuffer = new ParticleBuffer();
    private final Vector3f camRight = new Vector3f();
    private final Vector3f camUp = new Vector3f();

    public TargetESP() {
        a(this.effect, this.color, this.speed, this.duration, this.thickness, this.radius, this.density, this.tail, this.glow);
    }

    @Override
    public void b() {
        super.b();
        this.lastFrameTime = System.currentTimeMillis();
        reset();
    }

    @Override
    public void c() {
        super.c();
        reset();
        this.target1 = null;
        this.target2 = null;
        this.shaderProgram = null;
    }

    private void reset() {
        this.alpha1 = 0.0f;
        this.alpha2 = 0.0f;
        this.prevAlpha1 = 0.0f;
        this.prevAlpha2 = 0.0f;
        this.phase1 = 0.0f;
        this.phase2 = 0.0f;
        this.prevPhase1 = 0.0f;
        this.prevPhase2 = 0.0f;
        this.attackTime = 0L;
        this.active = false;
        this.isReversing = false;
        this.isTarget1Active = false;
        this.isTarget2Active = false;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!m() || aM_.world == null) return;
        if (event.b() instanceof LivingEntity living && living.isAlive() && living != aM_.player) {
            if (this.target1 != living) {
                this.target2 = this.target1;
                this.prevAlpha2 = this.alpha1;
                this.prevPhase2 = this.phase1;
                this.target1 = living;
                this.alpha1 = 0.0f;
                this.phase1 = 0.0f;
            }
            this.attackTime = System.currentTimeMillis();
            this.active = true;
        }
    }

    @EventTarget
    public void onDraw(DrawEvent event) {
        if (!event.c() || aM_.player == null || aM_.world == null) return;
        if (this.target1 == null && this.target2 == null) return;

        updateState();

        float t = Math.min((float) (System.currentTimeMillis() - this.lastFrameTime) / 50.0f, 1.0f);
        float a1 = lerp(this.prevAlpha1, this.alpha1, t);
        float a2 = lerp(this.prevAlpha2, this.alpha2, t);

        boolean drawT1 = this.target1 != null && this.isTarget1Active && a1 > 0.03f;
        boolean drawT2 = this.target2 != null && this.isTarget2Active && a2 > 0.03f;
        if (!drawT1 && !drawT2) return;

        ensureShader();
        if (this.shaderProgram == null) return;

        Camera camera = aM_.gameRenderer.getCamera();
        this.camRight.set(1.0f, 0.0f, 0.0f).rotate(camera.getRotation());
        this.camUp.set(0.0f, 1.0f, 0.0f).rotate(camera.getRotation());
        Matrix4f matrix4f = event.h().peek().getPositionMatrix();

        int col = this.color.c();
        TargetEffect targetEffect = TargetEffect.getByName(this.effect.c());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(this.shaderProgram);

        try {
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            if (drawT2) {
                float p2 = lerp(this.prevPhase2, this.phase2, t);
                renderTarget(buffer, targetEffect, p2, a2, camera, col, t, this.target2, false, matrix4f);
            }
            if (drawT1) {
                float p1 = lerp(this.prevPhase1, this.phase1, t);
                renderTarget(buffer, targetEffect, p1, a1, camera, col, t, this.target1, this.isReversing, matrix4f);
            }
            BuiltBuffer built = buffer.endNullable();
            if (built != null) {
                BufferRenderer.drawWithGlobalProgram(built);
            }
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void updateState() {
        this.prevAlpha1 = this.alpha1;
        this.prevAlpha2 = this.alpha2;
        this.prevPhase1 = this.phase1;
        this.prevPhase2 = this.phase2;

        float speedVal = this.speed.c();
        long now = System.currentTimeMillis();
        boolean expired = (float) (now - this.attackTime) >= this.duration.c() * 1000.0f;
        boolean canTarget1 = this.target1 != null && this.target1.isAlive();
        boolean canTarget2 = this.target2 != null && this.target2.isAlive();

        if (this.active && !expired && canTarget1) {
            this.alpha1 = Math.min(1.0f, this.alpha1 + 0.08f * speedVal);
            this.phase1 += speedVal;
            this.isTarget1Active = true;
        } else {
            this.alpha1 = Math.max(0.0f, this.alpha1 - 0.05f * speedVal);
            this.phase1 += speedVal;
            if (this.alpha1 <= 0.0f) {
                this.target1 = null;
                this.isTarget1Active = false;
                this.active = false;
            }
        }

        if (this.target2 != null && canTarget2) {
            this.alpha2 = Math.max(0.0f, this.alpha2 - 0.06f * speedVal);
            this.phase2 += speedVal;
            this.isTarget2Active = true;
            if (this.alpha2 <= 0.0f) {
                this.target2 = null;
                this.isTarget2Active = false;
            }
        } else {
            this.target2 = null;
            this.alpha2 = 0.0f;
            this.isTarget2Active = false;
        }

        this.lastFrameTime = now;
    }

    private void renderTarget(
        BufferBuilder buffer, TargetEffect targetEffect, float phase, float alpha,
        Camera camera, int col, float tickDelta, LivingEntity entity, boolean reversed, Matrix4f matrix
    ) {
        if (alpha <= 0.0f || entity == null) return;

        float smoothAlpha = alpha * alpha * (3.0f - 2.0f * alpha);
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        double entX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double entY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double entZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

        float relX = (float) (entX - camX);
        float relY = (float) (entY - camY);
        float relZ = (float) (entZ - camZ);

        float entWidth = entity.getWidth() * this.radius.c();
        float entHeight = entity.getHeight();
        float lineThick = this.thickness.c();
        float curPhase = phase * 0.04f;
        int dens = (int) this.density.c().floatValue();
        int tailVal = (int) this.tail.c().floatValue();
        float glowVal = this.glow.c();

        this.particleBuffer.setup(
            col, buffer, relX, relY, matrix, entHeight, this.camUp, this.camRight,
            dens, lineThick, tailVal, glowVal, relZ, smoothAlpha, entWidth, reversed, curPhase
        );
        targetEffect.render(this.particleBuffer);
    }

    private void ensureShader() {
        if (this.shaderProgram == null) {
            try {
                this.shaderProgram = aM_.getShaderLoader().getOrCreateProgram(SHADER_TARGET);
            } catch (Throwable ignored) {
            }
        }
    }

    private static float lerp(float a, float b, float t) {
        float d = b - a;
        return Math.abs(d) < 0.001f ? b : a + d * t;
    }
}
