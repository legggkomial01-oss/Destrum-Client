package aethereal.module.render;

import aethereal.core.Interface;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.InterfaceC0020Opcode;
import aethereal.core.Module;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.InventoryUtil;
import aethereal.util.MathUtil;
import aethereal.util.ProjectUtil;
import aethereal.util.ServerUtil;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.ModuleRegister;
import aethereal.event.DrawEvent;
import aethereal.module.misc.StreamerMode;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Generated;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.ClientPlayerEntity;
import org.joml.Vector2f;
import org.joml.Quaternionf;
import aethereal.ui.widget.NameTagWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@ModuleRegister(a = "NameTag", b = "Отображает информацию о сущностях над их головой", c = Category.Render)
public class EntityESP extends Module {
    private static EntityESP INSTANCE;

    private final ModeSetting mode = new ModeSetting("Режим", "Новый 1", "Новый 1", "Стандарт", "Новый 2", "Парящий");
    private final MultiModeSetting b = new MultiModeSetting("Отслеживаемые сущности", new BooleanSetting("Игроки", true), new BooleanSetting("Животные", false), new BooleanSetting("Мобы", false), new BooleanSetting("Предметы", false));
    private final List<a> c = new ArrayList();

    public static EntityESP getInstance() {
        return INSTANCE;
    }

    public void syncFromWidget(NameTagWidget widget) {
        if (widget == null) return;
        if (!this.mode.l(widget.mode.c())) {
            this.mode.a(widget.mode.c());
        }
    }

    @Generated
    public ModeSetting r() {
        return this.mode;
    }

    @Generated
    public List<a> q() {
        return this.c;
    }

    public EntityESP() {
        INSTANCE = this;
        a(this.mode, this.b);
    }

    @EventTarget
    public void a(DrawEvent event) {
        String str;
        if (event.b()) {
            NameTagWidget w = NameTagWidget.getInstance();
            if (w != null && !this.mode.l(w.mode.c())) {
                this.mode.a(w.mode.c());
            }

            for (Entity class_746Var : aM_.world.getEntities()) {
                if (class_746Var != aM_.player) {
                    if (class_746Var instanceof PlayerEntity) {
                        str = "Игроки";
                    } else if (class_746Var instanceof HostileEntity) {
                        str = "Мобы";
                    } else if ((class_746Var instanceof AnimalEntity) || (class_746Var instanceof ShulkerEntity) || (class_746Var instanceof VillagerEntity)) {
                        str = "Животные";
                    } else {
                        str = ((class_746Var instanceof ItemEntity) || (class_746Var instanceof ArrowEntity)) ? "Предметы" : null;
                    }
                    String key = str;
                    if (key != null && this.b.a(key).c().booleanValue()) {
                        int color = ((class_746Var instanceof PlayerEntity) && Delta.h().d().e().d(class_746Var.getName().getString())) ? ColorUtil.a(0, 100, 0, InterfaceC0020Opcode.bN) : ColorUtil.a(0, 0, 0, 80);
                        Vec3d interpolated = MathUtil.a((Entity) class_746Var, event.g());
                        Vec3d entityPos = interpolated.add(0.0d, class_746Var.getHeight() + 0.25f, 0.0d);
                        Vector2f screenPos = ProjectUtil.a(entityPos.getX(), entityPos.getY(), entityPos.getZ());
                        if (ProjectUtil.a(screenPos)) {
                            if ((class_746Var instanceof ItemEntity) || (class_746Var instanceof ArrowEntity)) {
                                c(class_746Var, event, screenPos, 7.5f, 2.0f, color);
                            } else {
                                a(class_746Var, event, screenPos, 7.5f, 2.0f, color);
                                b(class_746Var, event, ProjectUtil.a(interpolated.x, interpolated.y - 0.25d, interpolated.z), 7.5f, 2.0f, color);
                            }
                        }
                    }
                }
            }
        }
    }

    private void a(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        if (this.mode.l("Новый 1")) {
            renderNew1(entity, event, screenPos);
            return;
        }
        if (this.mode.l("Новый 2")) {
            renderFloatingArmor(entity, event, screenPos);
            return;
        }
        if (this.mode.l("Парящий")) {
            renderFloatingMinimal(entity, event, screenPos);
            return;
        }
        renderStandard(entity, event, screenPos);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Identifier getEntityTexture(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            if (player instanceof AbstractClientPlayerEntity clientPlayer) {
                Identifier skin = clientPlayer.getSkinTextures().texture();
                if (skin != null) return skin;
            }
            return DefaultSkinHelper.getSkinTextures(player.getUuid()).texture();
        }
        if (entity == null) return null;
        try {
            EntityRenderer renderer = aM_.getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof LivingEntityRenderer livingRenderer && entity instanceof LivingEntity living) {
                LivingEntityRenderState state = (LivingEntityRenderState) livingRenderer.getAndUpdateRenderState(living, 0.0f);
                Identifier id = livingRenderer.getTexture(state);
                if (id != null) return id;
            }
        } catch (Throwable ignored) {
        }

        String name = entity.getType().getUntranslatedName().toLowerCase();
        if (name.contains("cow") || name.contains("mooshroom")) return Identifier.ofVanilla("textures/entity/cow/cow.png");
        if (name.contains("pig")) return Identifier.ofVanilla("textures/entity/pig/pig.png");
        if (name.contains("sheep")) return Identifier.ofVanilla("textures/entity/sheep/sheep.png");
        if (name.contains("chicken")) return Identifier.ofVanilla("textures/entity/chicken.png");
        if (name.contains("creeper")) return Identifier.ofVanilla("textures/entity/creeper/creeper.png");
        if (name.contains("spider")) return Identifier.ofVanilla("textures/entity/spider/spider.png");
        if (name.contains("enderman")) return Identifier.ofVanilla("textures/entity/enderman/enderman.png");
        if (name.contains("villager") || name.contains("witch") || name.contains("trader")) return Identifier.ofVanilla("textures/entity/villager/villager.png");
        if (name.contains("wolf")) return Identifier.ofVanilla("textures/entity/wolf/wolf.png");
        if (name.contains("iron_golem")) return Identifier.ofVanilla("textures/entity/iron_golem/iron_golem.png");
        if (name.contains("skeleton")) return Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");
        if (name.contains("zombie")) return Identifier.ofVanilla("textures/entity/zombie/zombie.png");
        if (name.contains("blaze")) return Identifier.ofVanilla("textures/entity/blaze.png");
        if (name.contains("slime")) return Identifier.ofVanilla("textures/entity/slime/slime.png");
        if (name.contains("squid")) return Identifier.ofVanilla("textures/entity/squid/squid.png");
        return null;
    }

    public static void drawEntityHead(MatrixStack matrices, DrawEvent event, Entity entity, float x, float y, float size, float radius, float alpha) {
        if (entity == null) {
            event.d().a(matrices, x, y, size, size, radius, ColorUtil.a(50, 55, 70, (int) (220 * alpha)));
            return;
        }

        Identifier texture = getEntityTexture(entity);
        if (texture == null) {
            event.d().a(matrices, x, y, size, size, radius, ColorUtil.a(50, 55, 70, (int) (220 * alpha)));
            return;
        }

        int texId = aM_.getTextureManager().getTexture(texture).getGlId();
        int color = ColorUtil.a(255, 255, 255, (int) (255 * alpha));

        if (entity instanceof PlayerEntity) {
            event.d().a(matrices, x, y, size, size, radius, color, 0.125f, 0.125f, 0.125f, 0.125f, texId);
            event.d().a(matrices, x, y, size, size, radius, color, 0.625f, 0.125f, 0.125f, 0.125f, texId);
            return;
        }

        float u = 0.125f;
        float v = 0.125f;
        float uw = 0.125f;
        float vh = 0.125f;
        boolean drawHat = false;

        String name = entity.getType().getUntranslatedName().toLowerCase();

        if (name.contains("cow") || name.contains("mooshroom")) {
            u = 0.09375f;
            v = 0.1875f;
            uw = 0.125f;
            vh = 0.25f;
        } else if (name.contains("pig")) {
            u = 0.125f;
            v = 0.25f;
            uw = 0.125f;
            vh = 0.25f;
        } else if (name.contains("sheep")) {
            u = 0.125f;
            v = 0.25f;
            uw = 0.09375f;
            vh = 0.1875f;
        } else if (name.contains("chicken")) {
            u = 0.046875f;
            v = 0.09375f;
            uw = 0.0625f;
            vh = 0.1875f;
        } else if (name.contains("creeper")) {
            u = 0.125f;
            v = 0.25f;
            uw = 0.125f;
            vh = 0.25f;
        } else if (name.contains("spider")) {
            u = 0.625f;
            v = 0.375f;
            uw = 0.125f;
            vh = 0.1875f;
        } else if (name.contains("enderman")) {
            u = 0.125f;
            v = 0.25f;
            uw = 0.125f;
            vh = 0.25f;
        } else if (name.contains("villager") || name.contains("witch") || name.contains("trader")) {
            u = 0.125f;
            v = 0.125f;
            uw = 0.125f;
            vh = 0.15625f;
        } else if (name.contains("wolf")) {
            u = 0.09375f;
            v = 0.1875f;
            uw = 0.09375f;
            vh = 0.1875f;
        } else if (name.contains("iron_golem")) {
            u = 0.078125f;
            v = 0.078125f;
            uw = 0.0625f;
            vh = 0.078125f;
        } else if (name.contains("zombie") || name.contains("skeleton") || name.contains("drowned") || name.contains("husk") || name.contains("stray")) {
            u = 0.125f;
            v = 0.125f;
            uw = 0.125f;
            vh = 0.125f;
            drawHat = true;
        } else {
            u = 0.125f;
            v = 0.125f;
            uw = 0.125f;
            vh = 0.125f;
        }

        event.d().a(matrices, x, y, size, size, radius, color, u, v, uw, vh, texId);
        if (drawHat) {
            event.d().a(matrices, x, y, size, size, radius, color, 0.625f, 0.125f, uw, vh, texId);
        }
    }

    public static void drawHealthBadge(MatrixStack matrices, DrawEvent event, float rx, float ry, float hp, float maxHp, boolean colorLowHp, float opacity) {
        float size = 8.5f;
        int hpColor;
        if (colorLowHp) {
            if (hp <= 6.0f) {
                hpColor = ColorUtil.a(255, 75, 75, 255);
            } else if (hp <= 12.0f) {
                hpColor = ColorUtil.a(255, 205, 60, 255);
            } else {
                hpColor = ColorUtil.a(85, 245, 125, 255);
            }
        } else {
            hpColor = ColorUtil.a(85, 245, 125, 255);
        }

        int discBg = ColorUtil.a(18, 20, 28, (int) (225 * opacity));
        int discBorder = ColorUtil.a(50, 55, 75, (int) (190 * opacity));

        event.d().a(matrices, rx, ry, size, size, size / 2.0f, discBg);
        event.d().a(matrices, rx, ry, size, size, size / 2.0f, 0.75f, discBorder);

        float midX = rx + (size / 2.0f);
        float midY = ry + (size / 2.0f);
        float dSize = 3.6f;

        matrices.push();
        matrices.translate(midX, midY, 0.0f);
        matrices.multiply(new Quaternionf().rotationZ((float) (Math.PI / 4.0)));
        event.d().a(matrices, -dSize / 2.0f, -dSize / 2.0f, dSize, dSize, 0.8f, ColorUtil.a(hpColor, opacity));
        matrices.pop();
    }

    private void renderNew1(Entity entity, DrawEvent event, Vector2f screenPos) {
        NameTagWidget w = NameTagWidget.getInstance();
        boolean transparent = w != null ? w.transparentStyle.c().booleanValue() : true;
        boolean rounded = w != null ? w.roundedCorners.c().booleanValue() : true;
        boolean showGHP = w != null ? w.showGHP.c().booleanValue() : true;
        boolean showSkin = w != null ? w.showSkin.c().booleanValue() : true;
        boolean showArmor = w != null ? w.showArmor.c().booleanValue() : true;
        boolean colorLowHp = w != null ? w.colorLowHp.c().booleanValue() : true;
        boolean showHealth = w != null ? w.showHealth.c().booleanValue() : true;
        float scale = w != null ? w.getScale() : 0.9f;
        float opacity = w != null ? w.getBgOpacity() : 0.85f;

        StreamerMode streamerMode = Delta.h().d().t().aE();
        String nameStr = entity.getName().getString();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            nameStr = streamerMode.a(nameStr);
        }
        if (entity.getScoreboardTeam() != null) {
            nameStr = entity.getScoreboardTeam().getPrefix().getString() + nameStr;
        }

        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        float hp = living != null ? ServerUtil.a.a(living) : 20.0f;
        float maxHp = living != null ? living.getMaxHealth() : 20.0f;
        float abs = living != null ? living.getAbsorptionAmount() : 0.0f;
        String hpText = ((int) Math.ceil(hp)) + " HP";
        String ghpText = ((int) Math.ceil(abs)) + " GHP";

        List<ItemStack> armorList = new ArrayList<>();
        if (showArmor && living != null) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = living.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    armorList.add(stack);
                }
            }
        }

        List<ItemStack> handList = new ArrayList<>();
        if (living != null) {
            ItemStack offHand = living.getOffHandStack();
            ItemStack mainHand = living.getMainHandStack();
            if (!offHand.isEmpty()) {
                handList.add(offHand);
            }
            if (!mainHand.isEmpty()) {
                handList.add(mainHand);
            }
        }

        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 8.0f;

        boolean hasSkin = showSkin;
        float skinW = hasSkin ? skinSize : 0.0f;
        float nameW = Fonts.e.a(nameStr, fontSize);
        float hpW = showHealth ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = showHealth ? 8.5f : 0.0f;
        float ghpW = (showGHP && abs > 0.0f) ? Fonts.e.a(ghpText, fontSize) : 0.0f;
        float armorW = armorList.size() > 0 ? (armorList.size() * (itemSize + 1.0f)) : 0.0f;
        float handsW = handList.size() > 0 ? (handList.size() * (itemSize + 1.0f)) : 0.0f;

        float totalW = pad;
        if (skinW > 0) totalW += skinW + gap;
        totalW += nameW;
        if (hpW > 0) totalW += gap + hpW;
        if (badgeW > 0) totalW += gap + badgeW;
        if (ghpW > 0) totalW += gap + ghpW;
        if (armorW > 0) totalW += gap + armorW;
        if (handsW > 0) totalW += gap + handsW;
        totalW += pad;

        float x = screenPos.x() - (totalW / 2.0f);
        float y = screenPos.y() - h;

        MatrixStack matrices = event.i().getMatrices();
        boolean transform = Math.abs(scale - 1.0f) > 0.01f;
        if (transform) {
            matrices.push();
            matrices.translate(screenPos.x(), y + (h / 2.0f), 0.0f);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-screenPos.x(), -(y + (h / 2.0f)), 0.0f);
        }

        float radius = rounded ? (h / 2.0f) : 3.0f;
        int bgAlpha = (int) (opacity * (transparent ? 175 : 235));
        int bg = ColorUtil.a(11, 11, 22, bgAlpha);
        event.d().b(event.h(), x, y, totalW, h, radius, bg, opacity);

        float curX = x + pad;

        if (skinW > 0) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            drawEntityHead(matrices, event, entity, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = y + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), nameStr, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = -1;
            if (colorLowHp) {
                if (hp <= 6.0f) {
                    hpColor = ColorUtil.a(255, 75, 75, 255);
                } else if (hp <= 12.0f) {
                    hpColor = ColorUtil.a(255, 205, 60, 255);
                } else {
                    hpColor = ColorUtil.a(85, 245, 125, 255);
                }
            }
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.5f) / 2.0f);
            drawHealthBadge(matrices, event, curX, ringY, hp, maxHp, colorLowHp, opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
            curX += ghpW;
        }

        if (armorList.size() > 0) {
            curX += gap;
            for (ItemStack stack : armorList) {
                float itemY = y + ((h - itemSize) / 2.0f) - 0.5f;
                event.e().a(event.i(), InventoryUtil.a(stack), curX, itemY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemY + itemSize;
                    float barW = itemSize - 1.0f;
                    float dur = Math.max(0.0f, 1.0f - (float) stack.getDamage() / (float) stack.getMaxDamage());
                    event.d().a(matrices, curX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                    int barColor = ColorUtil.a(140, 120, 255, 240);
                    event.d().a(matrices, curX + 0.5f, barY, barW * dur, 0.9f, 0.45f, barColor);
                }
                curX += itemSize + 1.0f;
            }
        }

        if (handList.size() > 0) {
            curX += gap;
            for (ItemStack stack : handList) {
                float itemY = y + ((h - itemSize) / 2.0f) - 0.5f;
                event.e().a(event.i(), InventoryUtil.a(stack), curX, itemY, 0, 1.0f, itemSize / 16.0f, false);
                curX += itemSize + 1.0f;
            }
        }

        if (transform) {
            matrices.pop();
        }
    }

    private void renderStandard(Entity entity, DrawEvent event, Vector2f screenPos) {
        NameTagWidget w = NameTagWidget.getInstance();
        boolean transparent = w != null ? w.transparentStyle.c().booleanValue() : true;
        boolean rounded = w != null ? w.roundedCorners.c().booleanValue() : true;
        boolean showSkin = w != null ? w.showSkin.c().booleanValue() : true;
        boolean showArmor = w != null ? w.showArmor.c().booleanValue() : true;
        boolean colorLowHp = w != null ? w.colorLowHp.c().booleanValue() : true;
        boolean showHealth = w != null ? w.showHealth.c().booleanValue() : true;
        float scale = w != null ? w.getScale() : 0.9f;
        float opacity = w != null ? w.getBgOpacity() : 0.85f;

        StreamerMode streamerMode = Delta.h().d().t().aE();
        String nameStr = entity.getName().getString();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            nameStr = streamerMode.a(nameStr);
        }
        if (entity.getScoreboardTeam() != null) {
            nameStr = entity.getScoreboardTeam().getPrefix().getString() + nameStr;
        }

        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        float hp = living != null ? ServerUtil.a.a(living) : 20.0f;
        float maxHp = living != null ? living.getMaxHealth() : 20.0f;
        String hpText = ((int) Math.ceil(hp)) + " HP";

        List<ItemStack> rowStacks = new ArrayList<>();
        if (showArmor && living != null) {
            ItemStack offHand = living.getOffHandStack();
            if (!offHand.isEmpty()) rowStacks.add(offHand);
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = living.getEquippedStack(slot);
                if (!stack.isEmpty()) rowStacks.add(stack);
            }
            ItemStack mainHand = living.getMainHandStack();
            if (!mainHand.isEmpty()) rowStacks.add(mainHand);
        }

        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 8.5f;

        boolean hasSkin = showSkin;
        float skinW = hasSkin ? skinSize : 0.0f;
        float nameW = Fonts.e.a(nameStr, fontSize);
        float hpW = showHealth ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = showHealth ? 8.5f : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + pad;
        float itemsW = rowStacks.size() > 0 ? (rowStacks.size() * (itemSize + 2.0f)) : 0.0f;

        float x = screenPos.x() - (tagW / 2.0f);
        float y = screenPos.y() - h;

        MatrixStack matrices = event.i().getMatrices();
        boolean transform = Math.abs(scale - 1.0f) > 0.01f;
        if (transform) {
            matrices.push();
            matrices.translate(screenPos.x(), y + (h / 2.0f), 0.0f);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-screenPos.x(), -(y + (h / 2.0f)), 0.0f);
        }

        if (rowStacks.size() > 0) {
            float itemRowY = y - itemSize - 3.5f;
            float itemStartX = screenPos.x() - (itemsW / 2.0f);
            for (ItemStack stack : rowStacks) {
                event.d().a(matrices, itemStartX, itemRowY, itemSize, itemSize, 1.5f, ColorUtil.a(11, 11, 22, (int) (190 * opacity)));
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize - 0.5f;
                    float barW = itemSize - 1.0f;
                    float dur = Math.max(0.0f, 1.0f - (float) stack.getDamage() / (float) stack.getMaxDamage());
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.8f, 0.4f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.8f, 0.4f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.0f;
            }
        }

        int bgAlpha = (int) (opacity * (transparent ? 175 : 235));
        int bg = ColorUtil.a(11, 11, 22, bgAlpha);
        float radius = rounded ? (h / 2.0f) : 2.5f;
        event.d().b(event.h(), x, y, tagW, h, radius, bg, opacity);

        float curX = x + pad;
        if (skinW > 0) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            drawEntityHead(matrices, event, entity, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = y + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), nameStr, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = -1;
            if (colorLowHp) {
                if (hp <= 6.0f) {
                    hpColor = ColorUtil.a(255, 75, 75, 255);
                } else if (hp <= 12.0f) {
                    hpColor = ColorUtil.a(255, 205, 60, 255);
                } else {
                    hpColor = ColorUtil.a(85, 245, 125, 255);
                }
            }
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.5f) / 2.0f);
            drawHealthBadge(matrices, event, curX, ringY, hp, maxHp, colorLowHp, opacity);
        }

        if (transform) {
            matrices.pop();
        }
    }

    private void renderFloatingArmor(Entity entity, DrawEvent event, Vector2f screenPos) {
        NameTagWidget w = NameTagWidget.getInstance();
        boolean transparent = w != null ? w.transparentStyle.c().booleanValue() : true;
        boolean rounded = w != null ? w.roundedCorners.c().booleanValue() : true;
        boolean showGHP = w != null ? w.showGHP.c().booleanValue() : true;
        boolean showSkin = w != null ? w.showSkin.c().booleanValue() : true;
        boolean showArmor = w != null ? w.showArmor.c().booleanValue() : true;
        boolean colorLowHp = w != null ? w.colorLowHp.c().booleanValue() : true;
        boolean showHealth = w != null ? w.showHealth.c().booleanValue() : true;
        float scale = w != null ? w.getScale() : 0.9f;
        float opacity = w != null ? w.getBgOpacity() : 0.85f;

        StreamerMode streamerMode = Delta.h().d().t().aE();
        String nameStr = entity.getName().getString();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            nameStr = streamerMode.a(nameStr);
        }
        if (entity.getScoreboardTeam() != null) {
            nameStr = entity.getScoreboardTeam().getPrefix().getString() + nameStr;
        }

        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        float hp = living != null ? ServerUtil.a.a(living) : 20.0f;
        float maxHp = living != null ? living.getMaxHealth() : 20.0f;
        float abs = living != null ? living.getAbsorptionAmount() : 0.0f;
        String hpText = ((int) Math.ceil(hp)) + " HP";
        String ghpText = ((int) Math.ceil(abs)) + " GHP";

        List<ItemStack> floatingItems = new ArrayList<>();
        if (showArmor && living != null) {
            ItemStack offHand = living.getOffHandStack();
            if (!offHand.isEmpty()) floatingItems.add(offHand);
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = living.getEquippedStack(slot);
                if (!stack.isEmpty()) floatingItems.add(stack);
            }
            ItemStack mainHand = living.getMainHandStack();
            if (!mainHand.isEmpty()) floatingItems.add(mainHand);
        }

        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 9.0f;

        boolean hasSkin = showSkin;
        float skinW = hasSkin ? skinSize : 0.0f;
        float nameW = Fonts.e.a(nameStr, fontSize);
        float hpW = showHealth ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = showHealth ? 8.5f : 0.0f;
        float ghpW = (showGHP && abs > 0.0f) ? Fonts.e.a(ghpText, fontSize) : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + (ghpW > 0 ? gap + ghpW : 0.0f) + pad;
        float itemsW = floatingItems.size() > 0 ? (floatingItems.size() * (itemSize + 2.5f)) : 0.0f;

        float x = screenPos.x() - (tagW / 2.0f);
        float y = screenPos.y() - h;

        MatrixStack matrices = event.i().getMatrices();
        boolean transform = Math.abs(scale - 1.0f) > 0.01f;
        if (transform) {
            matrices.push();
            matrices.translate(screenPos.x(), y + (h / 2.0f), 0.0f);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-screenPos.x(), -(y + (h / 2.0f)), 0.0f);
        }

        if (floatingItems.size() > 0) {
            float itemRowY = y - itemSize - 4.0f;
            float itemStartX = screenPos.x() - (itemsW / 2.0f);
            for (ItemStack stack : floatingItems) {
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize;
                    float barW = itemSize - 1.0f;
                    float dur = Math.max(0.0f, 1.0f - (float) stack.getDamage() / (float) stack.getMaxDamage());
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.9f, 0.45f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.5f;
            }
        }

        float radius = rounded ? (h / 2.0f) : 3.0f;
        int bgAlpha = (int) (opacity * (transparent ? 175 : 235));
        int bg = ColorUtil.a(11, 11, 22, bgAlpha);
        event.d().b(event.h(), x, y, tagW, h, radius, bg, opacity);

        float curX = x + pad;
        if (skinW > 0) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            drawEntityHead(matrices, event, entity, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = y + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), nameStr, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = -1;
            if (colorLowHp) {
                if (hp <= 6.0f) {
                    hpColor = ColorUtil.a(255, 75, 75, 255);
                } else if (hp <= 12.0f) {
                    hpColor = ColorUtil.a(255, 205, 60, 255);
                } else {
                    hpColor = ColorUtil.a(85, 245, 125, 255);
                }
            }
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.5f) / 2.0f);
            drawHealthBadge(matrices, event, curX, ringY, hp, maxHp, colorLowHp, opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
        }

        if (transform) {
            matrices.pop();
        }
    }

    private void renderFloatingMinimal(Entity entity, DrawEvent event, Vector2f screenPos) {
        NameTagWidget w = NameTagWidget.getInstance();
        boolean showGHP = w != null ? w.showGHP.c().booleanValue() : true;
        boolean showSkin = w != null ? w.showSkin.c().booleanValue() : true;
        boolean showArmor = w != null ? w.showArmor.c().booleanValue() : true;
        boolean colorLowHp = w != null ? w.colorLowHp.c().booleanValue() : true;
        boolean showHealth = w != null ? w.showHealth.c().booleanValue() : true;
        float scale = w != null ? w.getScale() : 0.9f;
        float opacity = w != null ? w.getBgOpacity() : 0.85f;

        StreamerMode streamerMode = Delta.h().d().t().aE();
        String nameStr = entity.getName().getString();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            nameStr = streamerMode.a(nameStr);
        }
        if (entity.getScoreboardTeam() != null) {
            nameStr = entity.getScoreboardTeam().getPrefix().getString() + nameStr;
        }

        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        float hp = living != null ? ServerUtil.a.a(living) : 20.0f;
        float maxHp = living != null ? living.getMaxHealth() : 20.0f;
        float abs = living != null ? living.getAbsorptionAmount() : 0.0f;
        String hpText = ((int) Math.ceil(hp)) + " HP";
        String ghpText = ((int) Math.ceil(abs)) + " GHP";

        List<ItemStack> floatingItems = new ArrayList<>();
        if (showArmor && living != null) {
            ItemStack offHand = living.getOffHandStack();
            if (!offHand.isEmpty()) floatingItems.add(offHand);
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = living.getEquippedStack(slot);
                if (!stack.isEmpty()) floatingItems.add(stack);
            }
            ItemStack mainHand = living.getMainHandStack();
            if (!mainHand.isEmpty()) floatingItems.add(mainHand);
        }

        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 9.0f;

        boolean hasSkin = showSkin;
        float skinW = hasSkin ? skinSize : 0.0f;
        float nameW = Fonts.e.a(nameStr, fontSize);
        float hpW = showHealth ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = showHealth ? 8.5f : 0.0f;
        float ghpW = (showGHP && abs > 0.0f) ? Fonts.e.a(ghpText, fontSize) : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + (ghpW > 0 ? gap + ghpW : 0.0f) + pad;
        float itemsW = floatingItems.size() > 0 ? (floatingItems.size() * (itemSize + 2.5f)) : 0.0f;

        float x = screenPos.x() - (tagW / 2.0f);
        float y = screenPos.y() - h;

        MatrixStack matrices = event.i().getMatrices();
        boolean transform = Math.abs(scale - 1.0f) > 0.01f;
        if (transform) {
            matrices.push();
            matrices.translate(screenPos.x(), y + (h / 2.0f), 0.0f);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-screenPos.x(), -(y + (h / 2.0f)), 0.0f);
        }

        if (floatingItems.size() > 0) {
            float itemRowY = y - itemSize - 4.0f;
            float itemStartX = screenPos.x() - (itemsW / 2.0f);
            for (ItemStack stack : floatingItems) {
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize;
                    float barW = itemSize - 1.0f;
                    float dur = Math.max(0.0f, 1.0f - (float) stack.getDamage() / (float) stack.getMaxDamage());
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.9f, 0.45f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.5f;
            }
        }

        float curX = x + pad;
        if (skinW > 0) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            drawEntityHead(matrices, event, entity, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = y + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), nameStr, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = -1;
            if (colorLowHp) {
                if (hp <= 6.0f) {
                    hpColor = ColorUtil.a(255, 75, 75, 255);
                } else if (hp <= 12.0f) {
                    hpColor = ColorUtil.a(255, 205, 60, 255);
                } else {
                    hpColor = ColorUtil.a(85, 245, 125, 255);
                }
            }
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.5f) / 2.0f);
            drawHealthBadge(matrices, event, curX, ringY, hp, maxHp, colorLowHp, opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
        }

        if (transform) {
            matrices.pop();
        }
    }

    private void b(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        List<StatusEffectInstance> effects;
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            List<a> trackers = new ArrayList<>();
            for (int i = this.c.size() - 1; i >= 0; i--) {
                a entry = this.c.get(i);
                if (entry.b() == living.getId()) {
                    if (living.age < entry.c()) {
                        this.c.remove(i);
                    } else {
                        trackers.add(entry);
                    }
                }
            }
            if (!trackers.isEmpty()) {
                effects = new ArrayList<>();
                for (a tracker : trackers) {
                    for (StatusEffectInstance effectInstance : tracker.a()) {
                        int remaining = effectInstance.getDuration() - Math.max(0, living.age - tracker.c());
                        if (remaining > 0) {
                            StatusEffectInstance remainingEffect = effectInstance.getDuration() > 1000000 ? effectInstance : new StatusEffectInstance(effectInstance.getEffectType(), remaining, effectInstance.getAmplifier());
                            StatusEffectInstance existing = null;
                            for (StatusEffectInstance instance : effects) {
                                if (instance.getEffectType().equals(effectInstance.getEffectType())) {
                                    existing = instance;
                                    break;
                                }
                            }
                            if (existing == null) {
                                effects.add(remainingEffect);
                            } else if (remainingEffect.getAmplifier() > existing.getAmplifier() || (remainingEffect.getAmplifier() == existing.getAmplifier() && remainingEffect.getDuration() > existing.getDuration())) {
                                effects.remove(existing);
                                effects.add(remainingEffect);
                            }
                        }
                    }
                }
                if (effects.isEmpty()) {
                    effects = new ArrayList<>((Collection<? extends StatusEffectInstance>) living.getStatusEffects());
                }
            } else {
                effects = new ArrayList<>((Collection<? extends StatusEffectInstance>) living.getStatusEffects());
            }
            float lineHeight = Fonts.e.d().lineHeight() * fontSize;
            float maxWidth = 0.0f;
            for (StatusEffectInstance effect : effects) {
                int seconds = effect.getDuration() / 20;
                maxWidth = Math.max(maxWidth, Fonts.e.a(Text.translatable(((StatusEffect) effect.getEffectType().value()).getTranslationKey()).getString() + " " + MathUtil.a(effect.getAmplifier()) + (effect.getDuration() > 1000000 ? " ∞" : " - " + (seconds / 60) + ":" + String.format("%02d", Integer.valueOf(seconds % 60))), fontSize));
            }
            float textX = screenPos.x() - (maxWidth / 2.0f);
            float textY = screenPos.y() + padding;
            event.d().a(event.i().getMatrices(), textX - padding, textY, maxWidth + (padding * 2.0f), effects.size() * lineHeight, 0.0f, color);
            float lineY = textY;
            for (StatusEffectInstance effect2 : effects) {
                String duration = effect2.getDuration() > 1000000 ? " ∞" : " - " + ((effect2.getDuration() / 20) / 60) + ":" + String.format("%02d", Integer.valueOf((effect2.getDuration() / 20) % 60));
                String line = Text.translatable(((StatusEffect) effect2.getEffectType().value()).getTranslationKey()).getString() + " " + MathUtil.a(effect2.getAmplifier()) + duration;
                Fonts.e.a(event.i().getMatrices(), line, screenPos.x() - (Fonts.e.a(line, fontSize) / 2.0f), lineY, fontSize, ColorUtil.a(((StatusEffect) effect2.getEffectType().value()).getColor(), 1.0f), 0.0f);
                lineY += lineHeight;
            }
        }
    }

    private void c(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        MutableText text = entity instanceof ItemEntity ? ((ItemEntity) entity).getStack().getName().copy() : entity.getName().copy();
        if (entity instanceof ItemEntity) {
            ItemEntity item = (ItemEntity) entity;
            if (item.getStack().getCount() > 1) {
                text.append(Text.literal(" x" + item.getStack().getCount()));
            }
        }
        float textWidth = Fonts.e.a((Text) text, fontSize);
        float textHeight = Fonts.e.d().lineHeight() * fontSize;
        float textX = screenPos.x() - (textWidth / 2.0f);
        float textY = screenPos.y();
        event.d().a(event.i().getMatrices(), textX - padding, textY, textWidth + (padding * 2.0f), textHeight, 0.0f, color);
        Fonts.e.a(event.i().getMatrices(), text, textX, textY, fontSize);
    }

    public static final class a {
        private final List<StatusEffectInstance> a;
        private final int b;
        private final int c;

        public a(List<StatusEffectInstance> effects, int id, int age) {
            this.a = effects;
            this.b = id;
            this.c = age;
        }

        public List<StatusEffectInstance> a() {
            return this.a;
        }

        public int b() {
            return this.b;
        }

        public int c() {
            return this.c;
        }
    }
}
