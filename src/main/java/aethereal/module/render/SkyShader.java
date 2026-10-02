package aethereal.module.render;

import static aethereal.core.Interface.aM_;
import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.render.ColorUtil;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@ModuleRegister(a = "Sky Shader", b = "Накладывает анимированный шейдер на небо", c = Category.Render)
public class SkyShader extends Module {
    private static SkyShader INSTANCE;

    private static final ShaderProgramKey SHADER_NEBULA = new ShaderProgramKey(
        Identifier.of("delta", "core/sky_nebula"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static final ShaderProgramKey SHADER_AURORA = new ShaderProgramKey(
        Identifier.of("delta", "core/sky_aurora"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static final ShaderProgramKey SHADER_STARS = new ShaderProgramKey(
        Identifier.of("delta", "core/sky_stars"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static final ShaderProgramKey SHADER_PLASMA = new ShaderProgramKey(
        Identifier.of("delta", "core/sky_plasma"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static final ShaderProgramKey SHADER_NEON = new ShaderProgramKey(
        Identifier.of("delta", "core/sky_neon"), VertexFormats.POSITION, Defines.EMPTY
    );

    private final ModeSetting mode = new ModeSetting("Режим", "Туманность", "Туманность", "Аврора", "Звезды", "Плазма", "Неон");
    private final ColorSetting color = new ColorSetting("Цвет", Integer.valueOf(ColorUtil.a(50, 150, 255, 255)));
    private final ModeSetting neonPalette = new ModeSetting("Палитра неона", "RGB", "RGB", "Пастель", "Светлый", "Свой");
    private final ColorSetting neonColor1 = new ColorSetting("Неон цвет 1", Integer.valueOf(ColorUtil.a(255, 50, 50, 255)));
    private final ColorSetting neonColor2 = new ColorSetting("Неон цвет 2", Integer.valueOf(ColorUtil.a(50, 255, 50, 255)));
    private final ColorSetting neonColor3 = new ColorSetting("Неон цвет 3", Integer.valueOf(ColorUtil.a(50, 50, 255, 255)));
    private final SliderSetting speed = new SliderSetting("Скорость", 1.0f, 0.1f, 5.0f, 0.1f);
    private final SliderSetting size = new SliderSetting("Размер", 5.0f, 1.0f, 20.0f, 0.5f);
    private final SliderSetting intensity = new SliderSetting("Интенсивность", 0.01f, 0.001f, 0.05f, 0.001f);
    private final SliderSetting opacity = new SliderSetting("Непрозрачность", 1.0f, 0.1f, 1.0f, 0.05f);

    private final Map<String, ShaderProgram> programCache = new HashMap<>();
    private final Matrix4f projBackup = new Matrix4f();
    private final Matrix4f orthoMatrix = new Matrix4f();
    private final Matrix4f identityMatrix = new Matrix4f();

    private long startTime = -1L;

    public SkyShader() {
        INSTANCE = this;
        this.color.a(() -> !this.mode.l("Неон"));
        this.neonPalette.a(() -> this.mode.l("Неон"));
        this.neonColor1.a(() -> this.mode.l("Неон") && this.neonPalette.l("Свой"));
        this.neonColor2.a(() -> this.mode.l("Неон") && this.neonPalette.l("Свой"));
        this.neonColor3.a(() -> this.mode.l("Неон") && this.neonPalette.l("Свой"));

        a(this.mode, this.color, this.neonPalette, this.neonColor1, this.neonColor2, this.neonColor3, this.speed, this.size, this.intensity, this.opacity);
    }

    public static SkyShader getInstance() {
        return INSTANCE;
    }

    @Override
    public void b() {
        super.b();
        this.startTime = -1L;
    }

    @Override
    public void c() {
        super.c();
        this.programCache.clear();
    }

    public void renderSky() {
        if (!m() || aM_ == null || aM_.world == null || aM_.player == null) return;

        ShaderProgram program = getProgramForMode();
        if (program == null) return;

        if (this.startTime < 0L) {
            this.startTime = System.currentTimeMillis();
        }
        float elapsedSec = (float) (System.currentTimeMillis() - this.startTime) / 1000.0f;
        float width = (float) aM_.getWindow().getFramebufferWidth();
        float height = (float) aM_.getWindow().getFramebufferHeight();

        Camera camera = aM_.gameRenderer.getCamera();
        float yawRad = (float) Math.toRadians(camera.getYaw());
        float pitchRad = (float) Math.toRadians(camera.getPitch());
        float fov = (float) aM_.options.getFov().getValue().intValue();

        float cosYaw = (float) Math.cos(-yawRad);
        float sinYaw = (float) Math.sin(-yawRad);
        float cosPitch = (float) Math.cos(-pitchRad);
        float sinPitch = (float) Math.sin(-pitchRad);

        this.projBackup.set(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(this.orthoMatrix, ProjectionType.ORTHOGRAPHIC);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(program);

        setUniform(program, "uTime", elapsedSec);
        setUniform2f(program, "uResolution", width, height);

        int col = this.color.c();
        setUniform3f(program, "uColor", (col >> 16 & 0xFF) * 0.003921569f, (col >> 8 & 0xFF) * 0.003921569f, (col & 0xFF) * 0.003921569f);
        setUniform(program, "uAlpha", this.opacity.c());
        setUniform(program, "uSpeed", this.speed.c());
        setUniform(program, "uScale", this.size.c());
        setUniform(program, "uIntensity", this.intensity.c());

        // Camera basis vectors
        setUniform3f(program, "uCamRight", cosYaw, 0.0f, -sinYaw);
        setUniform3f(program, "uCamUp", sinYaw * sinPitch, cosPitch, cosYaw * sinPitch);
        setUniform3f(program, "uCamForward", -sinYaw * cosPitch, sinPitch, -cosYaw * cosPitch);
        setUniform(program, "uFov", fov);

        if (this.mode.l("Неон")) {
            applyNeonPalette(program);
        }

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(this.identityMatrix, -1.0f, -1.0f, 1.0f);
        buffer.vertex(this.identityMatrix, 1.0f, -1.0f, 1.0f);
        buffer.vertex(this.identityMatrix, 1.0f, 1.0f, 1.0f);
        buffer.vertex(this.identityMatrix, -1.0f, 1.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setProjectionMatrix(this.projBackup, ProjectionType.PERSPECTIVE);
    }

    private void applyNeonPalette(ShaderProgram program) {
        float[] c1 = new float[3];
        float[] c2 = new float[3];
        float[] c3 = new float[3];

        if (this.neonPalette.l("Пастель")) {
            setRgb(c1, 0.8f, 0.8f, 0.8f);
            setRgb(c2, 0.95f, 0.95f, 0.95f);
            setRgb(c3, 1.0f, 1.0f, 1.0f);
        } else if (this.neonPalette.l("Светлый")) {
            setRgb(c1, 0.1f, 0.1f, 0.1f);
            setRgb(c2, 0.25f, 0.25f, 0.25f);
            setRgb(c3, 0.45f, 0.45f, 0.45f);
        } else if (this.neonPalette.l("Свой")) {
            int nc1 = this.neonColor1.c();
            int nc2 = this.neonColor2.c();
            int nc3 = this.neonColor3.c();
            setRgb(c1, (nc1 >> 16 & 0xFF) * 0.003921569f, (nc1 >> 8 & 0xFF) * 0.003921569f, (nc1 & 0xFF) * 0.003921569f);
            setRgb(c2, (nc2 >> 16 & 0xFF) * 0.003921569f, (nc2 >> 8 & 0xFF) * 0.003921569f, (nc2 & 0xFF) * 0.003921569f);
            setRgb(c3, (nc3 >> 16 & 0xFF) * 0.003921569f, (nc3 >> 8 & 0xFF) * 0.003921569f, (nc3 & 0xFF) * 0.003921569f);
        } else {
            // Default RGB
            setRgb(c1, 1.0f, 0.0f, 0.0f);
            setRgb(c2, 0.0f, 1.0f, 0.0f);
            setRgb(c3, 0.0f, 0.0f, 1.0f);
        }

        setUniform3f(program, "uColor1", c1[0], c1[1], c1[2]);
        setUniform3f(program, "uColor2", c2[0], c2[1], c2[2]);
        setUniform3f(program, "uColor3", c3[0], c3[1], c3[2]);
    }

    private static void setRgb(float[] arr, float r, float g, float b) {
        arr[0] = r;
        arr[1] = g;
        arr[2] = b;
    }

    private ShaderProgram getProgramForMode() {
        String currentMode = this.mode.c();
        ShaderProgram cached = this.programCache.get(currentMode);
        if (cached != null) return cached;

        ShaderProgramKey key = switch (currentMode) {
            case "Аврора" -> SHADER_AURORA;
            case "Звезды" -> SHADER_STARS;
            case "Плазма" -> SHADER_PLASMA;
            case "Неон" -> SHADER_NEON;
            default -> SHADER_NEBULA;
        };

        try {
            ShaderProgram prog = aM_.getShaderLoader().getOrCreateProgram(key);
            if (prog != null) {
                this.programCache.put(currentMode, prog);
            }
            return prog;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void setUniform(ShaderProgram program, String name, float value) {
        GlUniform u = program.getUniform(name);
        if (u != null) u.set(value);
    }

    private static void setUniform2f(ShaderProgram program, String name, float x, float y) {
        GlUniform u = program.getUniform(name);
        if (u != null) u.set(x, y);
    }

    private static void setUniform3f(ShaderProgram program, String name, float x, float y, float z) {
        GlUniform u = program.getUniform(name);
        if (u != null) u.set(x, y, z);
    }
}
