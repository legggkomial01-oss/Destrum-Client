package aethereal.render;

import static aethereal.core.Interface.aM_;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public final class SnapshotFramebuffer {
    private SimpleFramebuffer simpleFramebuffer;

    public SimpleFramebuffer getSimpleFramebuffer() {
        if (aM_ == null) return null;
        Framebuffer framebuffer = aM_.getFramebuffer();
        if (framebuffer == null) return null;

        int width = framebuffer.textureWidth;
        int height = framebuffer.textureHeight;
        if (width <= 0 || height <= 0) return null;

        int boundFbo = GL11.glGetInteger(36006); // GL_FRAMEBUFFER_BINDING
        if (boundFbo == 0) {
            boundFbo = framebuffer.fbo;
        }

        if (this.simpleFramebuffer == null) {
            this.simpleFramebuffer = new SimpleFramebuffer(width, height, false);
            setupTextureParameters(this.simpleFramebuffer);
        } else if (this.simpleFramebuffer.textureWidth != width || this.simpleFramebuffer.textureHeight != height) {
            this.simpleFramebuffer.resize(width, height);
            setupTextureParameters(this.simpleFramebuffer);
        }

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, boundFbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.simpleFramebuffer.fbo);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, boundFbo);

        return this.simpleFramebuffer;
    }

    private static void setupTextureParameters(SimpleFramebuffer sfb) {
        GlStateManager._activeTexture(33984); // GL_TEXTURE0
        GlStateManager._bindTexture(sfb.getColorAttachment());
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._bindTexture(0);
    }

    public void close() {
        if (this.simpleFramebuffer != null) {
            try {
                this.simpleFramebuffer.delete();
            } catch (Throwable ignored) {
            }
            this.simpleFramebuffer = null;
        }
    }
}
