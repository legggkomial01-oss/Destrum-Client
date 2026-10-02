package platform.inject.mixin;


import aethereal.core.Delta;
import aethereal.core.EventManager;
import aethereal.event.HandAnimationEvent;
import aethereal.event.HandViewEvent;
import aethereal.core.IEvent;
import aethereal.util.Look;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public class HeldItemRendererMixin {
    @Redirect(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch(F)F"))
    private float redirectPitch(ClientPlayerEntity instance, float tickDelta) {
        return Look.c();
    }

    @Redirect(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw(F)F"))
    private float redirectYaw(ClientPlayerEntity instance, float tickDelta) {
        return Look.b();
    }

    @Redirect(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
    private float redirectPitchLerp(float delta, float start, float end) {
        return Look.c();
    }

    @Redirect(method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 1))
    private float redirectYawLerp(float delta, float start, float end) {
        return Look.b();
    }

    @WrapOperation(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V", ordinal = 2)})
    private void wrapHandAnimation(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) Hand hand) {
        HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress, armX);
        EventManager.a((IEvent) event);
        if (!event.a()) {
            original.call(new Object[]{instance, Float.valueOf(swingProgress), Float.valueOf(equipProgress), matrices, Integer.valueOf(armX), arm});
        }
    }

    @Inject(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)})
    private void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventManager.a((IEvent) new HandViewEvent(matrices, stack, hand));
    }

    @ModifyExpressionValue(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isInvisible()Z")})
    private boolean renderFirstPersonItem(boolean original) {
        if (Delta.h().d().t().T().m()) {
            return false;
        }
        return original;
    }

    @org.spongepowered.asm.mixin.Shadow
    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {}

    @Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
    private void onRenderFirstPersonItemTail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (aethereal.render.FramebufferRedirect.isFlag()) {
            return;
        }
        aethereal.module.render.HandsShader handsShader = aethereal.module.render.HandsShader.getInstance();
        if (handsShader != null && handsShader.m() && handsShader.isGlowMode()) {
            try {
                aethereal.render.PlayerOutlineEffect.onVertexConsumerProvider(vertexConsumers);
                aethereal.render.FramebufferRedirect.setFlag2();
                aethereal.render.ImmediateBufferSource maskBuffer = aethereal.render.ImmediateBufferSource.getInstance();
                this.renderFirstPersonItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, maskBuffer, light);
                maskBuffer.update();
                aethereal.render.PlayerOutlineEffect.setHasHandMask(true);
            } catch (Throwable ignored) {
            } finally {
                aethereal.render.FramebufferRedirect.setFlag();
                aethereal.render.PlayerOutlineEffect.restoreFramebuffer();
            }
        }
    }
}
