package aethereal.render;

import aethereal.module.render.HandsShader;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public final class PlayerOutlineEffect {
    private static final ShaderProgramKey shaderProgramKey = new ShaderProgramKey(
        Identifier.ofVanilla("core/player_outline_blur"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static final ShaderProgramKey shaderProgramKey2 = new ShaderProgramKey(
        Identifier.ofVanilla("core/player_outline_blit"), VertexFormats.POSITION, Defines.EMPTY
    );
    private static SimpleFramebuffer simpleFramebuffer;
    private static SimpleFramebuffer simpleFramebuffer2;
    private static SimpleFramebuffer simpleFramebuffer3;
    private static ShaderProgram shaderProgram;
    private static GlUniform glUniform;
    private static GlUniform glUniform2;
    private static GlUniform glUniform3;
    private static ShaderProgram shaderProgram2;
    private static GlUniform glUniform4;
    private static GlUniform glUniform5;
    private static GlUniform glUniform6;
    private static GlUniform glUniform7;
    private static GlUniform glUniform8;
    private static GlUniform glUniform9;
    private static GlUniform glUniform10;
    private static final long time = System.nanoTime();

    private static Framebuffer originalFramebuffer;
    private static int prevFbo = -1;
    private static int prevBlitFbo = 0;
    private static boolean hasHandMask = false;

    private PlayerOutlineEffect() {}

    public static boolean hasHandMask() {
        return hasHandMask;
    }

    public static void setHasHandMask(boolean has) {
        hasHandMask = has;
    }

    public static void update() {
        if (simpleFramebuffer != null) {
            simpleFramebuffer.delete();
            simpleFramebuffer = null;
        }
        if (simpleFramebuffer2 != null) {
            simpleFramebuffer2.delete();
            simpleFramebuffer2 = null;
        }
        if (simpleFramebuffer3 != null) {
            simpleFramebuffer3.delete();
            simpleFramebuffer3 = null;
        }
        shaderProgram = null;
        shaderProgram2 = null;
        glUniform = null;
        glUniform2 = null;
        glUniform3 = null;
        glUniform4 = null;
        glUniform5 = null;
        glUniform6 = null;
        glUniform7 = null;
        glUniform8 = null;
        glUniform9 = null;
        glUniform10 = null;
    }

    private static void setShaderProgram(ShaderProgram program) {
        if (program != shaderProgram2) {
            shaderProgram2 = program;
            glUniform4 = program.getUniform("OutlineColor");
            glUniform5 = program.getUniform("OutlineColorTop");
            glUniform6 = program.getUniform("Intensity");
            glUniform7 = program.getUniform("FillAlpha");
            glUniform8 = program.getUniform("Time");
            glUniform9 = program.getUniform("FlameStrength");
            glUniform10 = program.getUniform("GradientSpeed");
        }
    }

    private static void setShaderProgram2(ShaderProgram program) {
        if (program != shaderProgram) {
            shaderProgram = program;
            glUniform = program.getUniform("MaskSize");
            glUniform2 = program.getUniform("Direction");
            glUniform3 = program.getUniform("Radius");
        }
    }

    private static void drawQuad() {
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION);
        bufferbuilder.vertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.vertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.vertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.vertex(0.0F, 1.0F, 0.0F);
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
    }

    private static void ensureFramebuffers(int width, int height) {
        int halfW = Math.max(1, width / 2);
        int halfH = Math.max(1, height / 2);
        simpleFramebuffer = getOrCreateFramebuffer(simpleFramebuffer, true, width, height);
        simpleFramebuffer2 = getOrCreateFramebuffer(simpleFramebuffer2, false, halfW, halfH);
        simpleFramebuffer3 = getOrCreateFramebuffer(simpleFramebuffer3, false, halfW, halfH);
    }

    private static SimpleFramebuffer getOrCreateFramebuffer(SimpleFramebuffer fb, boolean useDepth, int width, int height) {
        if (fb == null) {
            SimpleFramebuffer newFb = new SimpleFramebuffer(width, height, useDepth);
            setupLinearClamp(newFb);
            return newFb;
        } else {
            if (fb.textureWidth != width || fb.textureHeight != height) {
                fb.resize(width, height);
                setupLinearClamp(fb);
            }
            return fb;
        }
    }

    private static void setupLinearClamp(SimpleFramebuffer fb) {
        GlStateManager._activeTexture(33984);
        GlStateManager._bindTexture(fb.getColorAttachment());
        GlStateManager._texParameter(3553, 10241, 9729); // GL_LINEAR
        GlStateManager._texParameter(3553, 10240, 9729); // GL_LINEAR
        GlStateManager._texParameter(3553, 10242, 10496); // GL_CLAMP_TO_EDGE
        GlStateManager._texParameter(3553, 10243, 10496); // GL_CLAMP_TO_EDGE
        GlStateManager._bindTexture(0);
    }

    public static void onVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider) {
        if (vertexConsumerProvider instanceof Immediate immediate) {
            try {
                immediate.draw();
            } catch (Throwable ignored) {}
        }
        prevFbo = GL11.glGetInteger(36006);
        if (simpleFramebuffer != null) {
            simpleFramebuffer.beginWrite(false);
        }
    }

    public static void restoreFramebuffer() {
        int i = prevFbo;
        prevFbo = -1;
        if (i >= 0) {
            GlStateManager._glBindFramebuffer(36160, i);
        } else {
            Framebuffer fb = originalFramebuffer != null ? originalFramebuffer : MinecraftClient.getInstance().getFramebuffer();
            if (fb != null) {
                fb.beginWrite(false);
            }
        }
    }

    public static boolean isFramebuffer(Framebuffer framebuffer) {
        originalFramebuffer = framebuffer;
        int h = framebuffer.textureHeight;
        int w = framebuffer.textureWidth;
        ensureFramebuffers(w, h);
        if (simpleFramebuffer == null) {
            return false;
        }
        int k = GL11.glGetInteger(36006);
        simpleFramebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        simpleFramebuffer.beginWrite(false);
        simpleFramebuffer.clear();
        GlStateManager._glBindFramebuffer(36160, k);
        return true;
    }

    public static void onHandGlow(HandsShader handGlow, MinecraftClient minecraftClient) {
        if (handGlow == null) return;

        boolean anim = handGlow.getVklyuchitAnimaciyu();
        boolean grad = handGlow.getGradient();
        float rad = handGlow.getRadius();
        if (anim) {
            rad *= handGlow.getSizeAnimacii();
        }

        float colR = handGlow.getColorR();
        float colG = handGlow.getColorG();
        float colB = handGlow.getColorB();
        float colA = handGlow.getColorA();

        float colTopR = grad ? handGlow.getVtoroyColorR() : 0.0F;
        float colTopG = grad ? handGlow.getVtoroyColorG() : 0.0F;
        float colTopB = grad ? handGlow.getVtoroyColorB() : 0.0F;
        float colTopA = grad ? handGlow.getVtoroyColorA() : 0.0F;

        float intensity = handGlow.getYarkost();
        float flame = anim ? handGlow.getStrengthAnimacii() : 0.0F;
        float gradSpeed = grad ? handGlow.getSpeedGradienta() : 0.0F;

        renderGlow(minecraftClient, gradSpeed, colTopB, colTopR, flame, colR, colA, colTopG, colG, rad, intensity, colTopA, colB);
    }

    private static void renderGlow(
        MinecraftClient minecraftClient,
        float gradSpeed,
        float colTopB,
        float colTopR,
        float flameStrength,
        float colR,
        float colA,
        float colTopG,
        float colG,
        float radius,
        float intensity,
        float colTopA,
        float colB
    ) {
        if (simpleFramebuffer == null || simpleFramebuffer2 == null || simpleFramebuffer3 == null) return;

        prevBlitFbo = GL11.glGetInteger(36006);
        ShaderProgram shaderprogram;
        ShaderProgram shaderprogram1;
        try {
            shaderprogram = minecraftClient.getShaderLoader().getOrCreateProgram(shaderProgramKey);
            shaderprogram1 = minecraftClient.getShaderLoader().getOrCreateProgram(shaderProgramKey2);
        } catch (Throwable t) {
            return;
        }

        if (shaderprogram == null || shaderprogram1 == null) return;

        setShaderProgram2(shaderprogram);
        setShaderProgram(shaderprogram1);

        float f1 = radius / 2.0F;
        int i = simpleFramebuffer2.textureWidth;
        int j = simpleFramebuffer2.textureHeight;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.viewport(0, 0, i, j);
        RenderSystem.setShader(shaderprogram);

        if (glUniform != null) {
            glUniform.set((float) i, (float) j);
        }
        if (glUniform3 != null) {
            glUniform3.set(f1);
        }

        // Pass 1: Horizontal Gaussian Blur
        simpleFramebuffer2.beginWrite(false);
        shaderprogram.addSamplerTexture("MaskSampler", simpleFramebuffer.getColorAttachment());
        if (glUniform2 != null) {
            glUniform2.set(1.0F, 0.0F);
        }
        drawQuad();

        // Pass 2: Vertical Gaussian Blur
        simpleFramebuffer3.beginWrite(false);
        shaderprogram.addSamplerTexture("MaskSampler", simpleFramebuffer2.getColorAttachment());
        if (glUniform2 != null) {
            glUniform2.set(0.0F, 1.0F);
        }
        drawQuad();

        // Pass 3: Blit to Game Framebuffer with Glow & Flame & Gradient
        GlStateManager._glBindFramebuffer(36160, prevBlitFbo);
        RenderSystem.viewport(0, 0, simpleFramebuffer.textureWidth, simpleFramebuffer.textureHeight);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SrcFactor.ONE, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ONE_MINUS_SRC_ALPHA);

        shaderprogram1.addSamplerTexture("BlurSampler", simpleFramebuffer3.getColorAttachment());
        shaderprogram1.addSamplerTexture("MaskSampler", simpleFramebuffer.getColorAttachment());

        if (glUniform4 != null) {
            glUniform4.set(colR, colG, colB, colA);
        }
        if (glUniform5 != null) {
            glUniform5.set(colTopR, colTopG, colTopB, colTopA);
        }
        if (glUniform6 != null) {
            glUniform6.set(intensity);
        }
        if (glUniform7 != null) {
            glUniform7.set(0.0F); // FillAlpha = 0.0 so sword is NOT tinted
        }
        if (glUniform8 != null) {
            glUniform8.set((float)(System.nanoTime() - time) / 1.0E9F);
        }
        if (glUniform9 != null) {
            glUniform9.set(flameStrength);
        }
        if (glUniform10 != null) {
            glUniform10.set(gradSpeed);
        }

        RenderSystem.setShader(shaderprogram1);
        drawQuad();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static Framebuffer getSimpleFramebufferAsFramebuffer() {
        return simpleFramebuffer;
    }
}
