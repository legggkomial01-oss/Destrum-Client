package platform.inject.mixin;

import aethereal.render.FramebufferRedirect;
import aethereal.render.PlayerOutlineEffect;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Framebuffer.class)
public class FramebufferMixin {
    @Inject(method = "beginWrite", at = @At("HEAD"), cancellable = true)
    private void onBeginWrite(boolean setViewport, CallbackInfo ci) {
        if (FramebufferRedirect.isFlag()) {
            Framebuffer target = PlayerOutlineEffect.getSimpleFramebufferAsFramebuffer();
            if (target != null && (Object) this != target) {
                target.beginWrite(setViewport);
                ci.cancel();
            }
        }
    }
}
