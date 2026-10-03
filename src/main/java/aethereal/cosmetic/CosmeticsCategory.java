package aethereal.cosmetic;

import aethereal.cosmetic.Cosmetic;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.RotationAxis;

public enum CosmeticsCategory {
    BACK(Attachment.BODY),
    BODY(Attachment.BODY),
    HEAD(Attachment.HEAD),
    HAT(Attachment.HEAD),
    HALO(Attachment.HEAD),
    HORNS(Attachment.HEAD),
    WINGS(Attachment.BODY),

    f1(Attachment.BODY),
    f2(Attachment.BODY),
    f3_(Attachment.HEAD),
    f4(Attachment.HEAD),
    f5(Attachment.BODY),
    f6(Attachment.BODY);

    private final Attachment attachment;

    CosmeticsCategory(Attachment attachment) {
        this.attachment = attachment;
    }

    public void transform(MatrixStack matrices, PlayerEntityModel model, Cosmetic cosmetic) {
        this.attachment.part(model).rotate(matrices);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.scale(cosmetic.getScale(), cosmetic.getScale(), cosmetic.getScale());
        matrices.translate(cosmetic.getOffset().x, cosmetic.getOffset().y, cosmetic.getOffset().z());
    }

    private enum Attachment {
        BODY {
            @Override
            ModelPart part(PlayerEntityModel model) {
                return model.body;
            }
        },
        HEAD {
            @Override
            ModelPart part(PlayerEntityModel model) {
                return model.head;
            }
        };

        abstract ModelPart part(PlayerEntityModel model);
    }
}
