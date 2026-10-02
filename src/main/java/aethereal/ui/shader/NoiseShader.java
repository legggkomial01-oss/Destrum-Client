package aethereal.ui.shader;

import static aethereal.core.Interface.aM_;
import aethereal.core.EventManager;

import aethereal.core.EventTarget;
import aethereal.core.Interface;
import aethereal.event.ResizeEvent;
import aethereal.ui.shader.Shader;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class NoiseShader extends Shader implements Interface {
    private static final Identifier c = Identifier.of("delta", "core/noise/noise_shader");
    private final Matrix4f d;
    private SimpleFramebuffer e;
    private Uniform f;
    private Uniform g;
    private Uniform uniformSecondColor;
    private Uniform uniformMode;
    private Uniform uniformResolution;
    private Uniform uniformRadius;
    private Uniform uniformIntensity;
    private Uniform uniformGradientSpeed;
    private Uniform uniformFlameStrength;

    public NoiseShader() {
        super(c, VertexFormats.POSITION_COLOR);
        this.d = new Matrix4f();
        EventManager.a(this);
    }

    @EventTarget
    public void a(ResizeEvent event) {
        if (this.e != null) {
            this.e.delete();
        }
        this.e = new SimpleFramebuffer(aM_.getWindow().getFramebufferWidth(), aM_.getWindow().getFramebufferHeight(), true);
    }

    public void e() {
        int width = aM_.getWindow().getFramebufferWidth();
        int height = aM_.getWindow().getFramebufferHeight();
        if (this.e == null || this.e.textureWidth != width || this.e.textureHeight != height) {
            if (this.e != null) {
                this.e.delete();
            }
            this.e = new SimpleFramebuffer(width, height, true);
        }
        this.e.copyDepthFrom(aM_.getFramebuffer());
    }

    @Override
    protected void b() {
        this.f = a("TintColor");
        this.g = a("Time");
        this.uniformSecondColor = a("SecondColor");
        this.uniformMode = a("Mode");
        this.uniformResolution = a("Resolution");
        this.uniformRadius = a("Radius");
        this.uniformIntensity = a("Intensity");
        this.uniformGradientSpeed = a("GradientSpeed");
        this.uniformFlameStrength = a("FlameStrength");
    }

    public void a(float[] color) {
        a(0, color, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 16.0f, 1.0f, 1.0f, 0.0f);
    }

    public void a(int mode, float[] primaryColor, float[] secondColor, float radius, float intensity, float gradSpeed, float flameStrength) {
        if (this.e != null) {
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(this.d, ProjectionType.PERSPECTIVE);
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix().identity();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShaderTexture(0, aM_.getFramebuffer().getColorAttachment());
            RenderSystem.setShaderTexture(1, aM_.getFramebuffer().getDepthAttachment());
            RenderSystem.setShaderTexture(2, this.e.getDepthAttachment());
            a();
            if (this.f != null) {
                this.f.set(primaryColor[0], primaryColor[1], primaryColor[2], primaryColor[3]);
            }
            if (this.uniformSecondColor != null) {
                this.uniformSecondColor.set(secondColor[0], secondColor[1], secondColor[2], secondColor[3]);
            }
            if (this.g != null) {
                this.g.set((System.currentTimeMillis() % 100000) / 1000.0f);
            }
            if (this.uniformMode != null) {
                this.uniformMode.set((float) mode);
            }
            if (this.uniformResolution != null) {
                this.uniformResolution.set((float) aM_.getWindow().getFramebufferWidth(), (float) aM_.getWindow().getFramebufferHeight());
            }
            if (this.uniformRadius != null) {
                this.uniformRadius.set(radius);
            }
            if (this.uniformIntensity != null) {
                this.uniformIntensity.set(intensity);
            }
            if (this.uniformGradientSpeed != null) {
                this.uniformGradientSpeed.set(gradSpeed);
            }
            if (this.uniformFlameStrength != null) {
                this.uniformFlameStrength.set(flameStrength);
            }
            int white = new Color(255, 255, 255, 255).getRGB();
            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            builder.vertex(-1.0f, -1.0f, 0.0f).color(white);
            builder.vertex(-1.0f, 1.0f, 0.0f).color(white);
            builder.vertex(1.0f, 1.0f, 0.0f).color(white);
            builder.vertex(1.0f, -1.0f, 0.0f).color(white);
            BufferRenderer.drawWithGlobalProgram(builder.end());
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(1, 0);
            RenderSystem.setShaderTexture(2, 0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }
}
