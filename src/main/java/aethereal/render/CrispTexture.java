package aethereal.render;

import aethereal.core.Interface;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class CrispTexture implements AutoCloseable {
    private final Identifier id;
    private NativeImageBackedTexture texture;
    private static final Identifier FALLBACK = Identifier.ofVanilla("textures/misc/unknown_server.png");

    public CrispTexture(String name) {
        this.id = Identifier.of("delta", "crisp/" + name);
    }

    public static NativeImage upscaleToCrisp(NativeImage src) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= 0 || h <= 0) return null;
        if (w >= 128 || h >= 128) {
            NativeImage copy = new NativeImage(w, h, false);
            copy.copyFrom(src);
            return copy;
        }
        int scale = 4;
        NativeImage upscaled = new NativeImage(w * scale, h * scale, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color = src.getColorArgb(x, y);
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        upscaled.setColorArgb(x * scale + dx, y * scale + dy, color);
                    }
                }
            }
        }
        return upscaled;
    }

    public void upload(NativeImage image) {
        if (image == null) return;
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.texture != null) {
            try {
                Interface.aM_.getTextureManager().destroyTexture(this.id);
                this.texture.close();
            } catch (Exception ignored) {
            }
            this.texture = null;
        }
        this.texture = new NativeImageBackedTexture(image);
        this.texture.setClamp(true);
        this.texture.setFilter(true, false);
        Interface.aM_.getTextureManager().registerTexture(this.id, this.texture);
    }

    public Identifier getId() {
        return this.texture != null ? this.id : FALLBACK;
    }

    @Override
    public void close() {
        if (this.texture != null) {
            try {
                Interface.aM_.getTextureManager().destroyTexture(this.id);
                this.texture.close();
            } catch (Exception ignored) {
            }
            this.texture = null;
        }
    }
}
