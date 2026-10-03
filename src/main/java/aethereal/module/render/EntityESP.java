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
import aethereal.ui.widget.NameTagWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@ModuleRegister(a = "NameTag", b = "Отображает информацию о сущностях над их головой", c = Category.Render)
public class EntityESP extends Module {
    private static EntityESP INSTANCE;

    private final ModeSetting mode = new ModeSetting("Режим", "Новый 1", "Новый 1", "Стандарт", "Новый 2");
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

        StreamerMode streamerMode = Delta.h().d().t().aE();
        Text name = entity.getName();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            name = Text.literal(streamerMode.a(name.getString())).setStyle(name.getStyle());
        }
        MutableText display = name.copy().setStyle(name.getStyle().withColor(16777215));
        display.getSiblings().replaceAll(sibling -> {
            return sibling.copy().setStyle(sibling.getStyle().withColor(16777215));
        });
        Text text = (entity.getScoreboardTeam() != null ? entity.getScoreboardTeam().getPrefix().copy().append(display) : display).copy().append(Text.literal(" " + ((int) ServerUtil.a.a((LivingEntity) entity))).setStyle(Style.EMPTY.withColor(16711680)));
        float textWidth = Fonts.e.a(text, fontSize);
        float textHeight = Fonts.e.d().lineHeight() * fontSize;
        float textX = screenPos.x() - (textWidth / 2.0f);
        float textY = screenPos.y();
        float bgX = textX - padding;
        float bgWidth = textWidth + (padding * 2.0f);
        event.d().a(event.i().getMatrices(), bgX, textY, bgWidth, textHeight, 0.0f, color);
        Fonts.e.a(event.i().getMatrices(), text, textX, textY, fontSize);
        a(entity, event, bgWidth, bgX, textY, color, textHeight);
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
        String healthStyle = w != null ? w.healthStyle.c() : "Текст";
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

        boolean hasSkin = showSkin && (entity instanceof PlayerEntity);
        float skinW = hasSkin ? skinSize : 0.0f;
        float nameW = Fonts.e.a(nameStr, fontSize);
        boolean drawHpText = showHealth && (healthStyle.equals("Текст") || healthStyle.equals("Текст и кольцо"));
        boolean drawHpRing = showHealth && (healthStyle.equals("Кольцо") || healthStyle.equals("Текст и кольцо"));
        float hpW = drawHpText ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float ringW = drawHpRing ? 8.0f : 0.0f;
        float ghpW = (showGHP && abs > 0.0f) ? Fonts.e.a(ghpText, fontSize) : 0.0f;
        float armorW = armorList.size() > 0 ? (armorList.size() * (itemSize + 1.0f)) : 0.0f;
        float handsW = handList.size() > 0 ? (handList.size() * (itemSize + 1.0f)) : 0.0f;

        float totalW = pad;
        if (skinW > 0) totalW += skinW + gap;
        totalW += nameW;
        if (hpW > 0) totalW += gap + hpW;
        if (ringW > 0) totalW += gap + ringW;
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
        int bg = ColorUtil.a(16, 17, 23, bgAlpha);
        int outline = ColorUtil.a(255, 255, 255, (int) (opacity * 25));

        event.d().a(matrices, x, y, totalW, h, radius, bg);
        event.d().a(matrices, x, y, totalW, h, radius, 0.5f, outline);

        float curX = x + pad;

        if (skinW > 0 && (entity instanceof PlayerEntity player)) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            Identifier skin = null;
            if (player instanceof AbstractClientPlayerEntity clientPlayer) {
                skin = clientPlayer.getSkinTextures().texture();
            }
            if (skin == null) {
                skin = DefaultSkinHelper.getSkinTextures(player.getUuid()).texture();
            }
            if (skin != null) {
                int texId = aM_.getTextureManager().getTexture(skin).getGlId();
                event.d().a(matrices, curX, avatarY, skinSize, skinSize, 2.0f, -1, 0.125f, 0.125f, 0.125f, 0.125f, texId);
                event.d().a(matrices, curX, avatarY, skinSize, skinSize, 2.0f, -1, 0.625f, 0.125f, 0.125f, 0.125f, texId);
            }
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
                    hpColor = ColorUtil.a(120, 255, 140, 255);
                }
            }
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (ringW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.0f) / 2.0f);
            int ringBg = ColorUtil.a(40, 42, 54, 200);
            event.d().a(matrices, curX, ringY, 8.0f, 8.0f, 4.0f, 1.0f, ringBg);
            int ringColor = (hp <= 6.0f) ? ColorUtil.a(255, 75, 75, 255) : ColorUtil.a(120, 255, 140, 255);
            event.d().a(matrices, curX + 2.0f, ringY + 2.0f, 4.0f, 4.0f, 2.0f, ringColor);
            curX += ringW;
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

    private void a(Entity entity, DrawEvent event, float nameTagWidth, float nameTagX, float nameTagY, int color, float textHeight) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            float spacing = textHeight * 0.3f;
            ItemStack[] stacks = {player.getMainHandStack(), player.getEquippedStack(EquipmentSlot.HEAD), player.getEquippedStack(EquipmentSlot.CHEST), player.getEquippedStack(EquipmentSlot.LEGS), player.getEquippedStack(EquipmentSlot.FEET), player.getOffHandStack()};
            int count = 0;
            for (ItemStack class_1799Var : stacks) {
                if (!class_1799Var.isEmpty()) {
                    count++;
                }
            }
            if (count > 0) {
                float x = nameTagX + ((nameTagWidth - ((count * textHeight) + ((count - 1) * spacing))) / 2.0f);
                float y = (nameTagY - textHeight) - spacing;
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty()) {
                        event.d().a(event.i().getMatrices(), x, y, textHeight, textHeight, 0.0f, color);
                        event.e().a(event.i(), InventoryUtil.a(stack), x, y, 0, 1.0f, textHeight / 16.0f, true);
                        x += textHeight + spacing;
                    }
                }
            }
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
