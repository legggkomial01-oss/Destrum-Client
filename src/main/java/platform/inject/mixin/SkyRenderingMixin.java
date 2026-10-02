package platform.inject.mixin;

import aethereal.module.render.SkyShader;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRendering.class)
public class SkyRenderingMixin {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(float red, float green, float blue, CallbackInfo ci) {
        SkyShader skyShader = SkyShader.getInstance();
        if (skyShader != null && skyShader.m()) {
            skyShader.renderSky();
            ci.cancel();
        }
    }

    @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
    private void onRenderEndSky(CallbackInfo ci) {
        SkyShader skyShader = SkyShader.getInstance();
        if (skyShader != null && skyShader.m()) {
            skyShader.renderSky();
            ci.cancel();
        }
    }

    @Inject(method = "renderCelestialBodies", at = @At("HEAD"), cancellable = true)
    private void onRenderCelestialBodies(
        MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
        float rot, int phase, float alpha, float starAlpha, Fog fog, CallbackInfo ci
    ) {
        SkyShader skyShader = SkyShader.getInstance();
        if (skyShader != null && skyShader.m()) {
            ci.cancel();
        }
    }
}
