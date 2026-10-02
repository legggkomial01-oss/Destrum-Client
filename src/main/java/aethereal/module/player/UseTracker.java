package aethereal.module.player;

import aethereal.lib.javassist.TokenId;
import aethereal.module.render.EntityESP;
import aethereal.core.Interface;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.InterfaceC0020Opcode;
import aethereal.core.Module;
import aethereal.util.ChatUtil;
import aethereal.util.MathUtil;
import aethereal.util.ProjectUtil;
import aethereal.util.ServerUtil;

import aethereal.config.ThemeInfo;
import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.ModuleRegister;
import aethereal.event.DrawEvent;
import aethereal.event.PacketEvent;
import aethereal.event.PotionEvent;
import aethereal.event.TickEvent;
import aethereal.notification.Notification;
import aethereal.render.ColorUtil;
import aethereal.render.Fonts;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;

@ModuleRegister(a = "Use Tracker", b = "Отслеживает выбранные использования и уведомляет о них", c = Category.Player)
public class UseTracker extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Отслеживать использования", new BooleanSetting("Тотема", true), new BooleanSetting("Зелья", true), new BooleanSetting("Предмета", true));
    private final BooleanSetting visualPotionTag = new BooleanSetting("Визуальная плашка", true);

    private final List<PotionHitTag> potionHitTags = new CopyOnWriteArrayList<>();
    private final Map<Integer, PotionRecord> flyingPotions = new ConcurrentHashMap<>();

    public UseTracker() {
        a(this.b, this.visualPotionTag);
    }

    @Override
    public void c() {
        this.potionHitTags.clear();
        this.flyingPotions.clear();
        super.c();
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.a("Предмета").c().booleanValue() && aM_.world != null) {
            for (Entity _e : aM_.world.getEntities()) {
                if (!(_e instanceof PlayerEntity)) continue;
                PlayerEntity class_746Var2 = (PlayerEntity) _e;
                if (class_746Var2 != aM_.player) {
                    ItemStack active = class_746Var2.getActiveItem();
                    if ((active.getItem() instanceof PotionItem) || active.get(DataComponentTypes.FOOD) != null || active.getItem() == Items.MILK_BUCKET) {
                        if (class_746Var2.getItemUseTimeLeft() == 1) {
                            String color = active.getItem() instanceof PotionItem ? "&a" : "&c";
                            if (active.isOf(Items.MILK_BUCKET)) {
                                Delta.h().d().t().aa().q().removeIf(info -> info.b() == class_746Var2.getId());
                            }
                            ChatUtil.a("[" + j() + "]", class_746Var2.getName().getString() + " использовал \"" + color + active.getItem().getName().getString() + "&7\"");
                            Delta.h().d().m().a(new Notification(active.copy(), class_746Var2.getName().getString() + " использовал " + active.getItem().getName().getString(), 1500));
                        }
                    }
                }
            }
        }

        if (this.b.a("Зелья").c().booleanValue() && aM_.world != null) {
            long now = System.currentTimeMillis();
            for (Entity e : aM_.world.getEntities()) {
                if (e instanceof PotionEntity potion) {
                    this.flyingPotions.put(potion.getId(), new PotionRecord(potion.getStack().copy(), potion.getPos(), now));
                }
            }
            this.flyingPotions.entrySet().removeIf(entry -> now - entry.getValue().timestamp > 3000L);
        }
    }

    @EventTarget
    public void a(PotionEvent event) {
        if (!this.b.a("Зелья").c().booleanValue() || event.b() != PotionEvent.a.PARTICLES || aM_.world == null) {
            return;
        }

        BlockPos pos = event.d();
        Vec3d splash = pos.toCenterPos();

        // Check if there was a flying potion that landed near this splash position
        PotionRecord matchedFlyingPotion = null;
        double bestDist = 5.0d;
        long now = System.currentTimeMillis();
        for (PotionRecord record : this.flyingPotions.values()) {
            if (now - record.timestamp < 1200L) {
                double d = record.pos.distanceTo(splash);
                if (d < bestDist) {
                    bestDist = d;
                    matchedFlyingPotion = record;
                }
            }
        }

        String potionDisplayName = null;
        MutableText potionText = null;
        final List<Map.Entry<RegistryEntry<StatusEffect>, int[]>> matchedEffects = new ArrayList<>();

        if (matchedFlyingPotion != null) {
            ItemStack stack = matchedFlyingPotion.stack;
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents != null) {
                for (StatusEffectInstance inst : contents.getEffects()) {
                    matchedEffects.add(Map.entry(inst.getEffectType(), new int[]{inst.getDuration(), inst.getAmplifier()}));
                }
                contents.potion().ifPresent(pot -> {
                    for (StatusEffectInstance inst : pot.value().getEffects()) {
                        matchedEffects.add(Map.entry(inst.getEffectType(), new int[]{inst.getDuration(), inst.getAmplifier()}));
                    }
                });
            }
            if (!matchedEffects.isEmpty()) {
                potionDisplayName = stack.getName().getString();
                potionText = Text.literal(potionDisplayName);
            }
        }

        // Check custom server potions
        if (matchedEffects.isEmpty()) {
            for (a type : a.values()) {
                for (int color : type.d()) {
                    if ((color & 16777215) == (event.c() & 16777215)) {
                        matchedEffects.addAll(type.b());
                        potionDisplayName = type.c();
                        potionText = type.a();
                        break;
                    }
                }
                if (!matchedEffects.isEmpty()) break;
            }
        }

        // Fallback: check vanilla status effect colors
        if (matchedEffects.isEmpty()) {
            for (RegistryEntry<StatusEffect> effectEntry : List.of(
                StatusEffects.STRENGTH, StatusEffects.SPEED, StatusEffects.SLOWNESS, StatusEffects.REGENERATION,
                StatusEffects.RESISTANCE, StatusEffects.FIRE_RESISTANCE, StatusEffects.POISON, StatusEffects.WITHER,
                StatusEffects.WEAKNESS, StatusEffects.INVISIBILITY, StatusEffects.SLOW_FALLING, StatusEffects.JUMP_BOOST,
                StatusEffects.HASTE, StatusEffects.BLINDNESS
            )) {
                if ((effectEntry.value().getColor() & 16777215) == (event.c() & 16777215)) {
                    matchedEffects.add(Map.entry(effectEntry, new int[]{1800, 0}));
                    potionDisplayName = effectEntry.value().getName().getString();
                    potionText = Text.literal(potionDisplayName);
                    break;
                }
            }
        }

        if (matchedEffects.isEmpty()) {
            return;
        }

        Box box = new Box(pos.getX() - 4, pos.getY() - 4, pos.getZ() - 4, pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
        for (PlayerEntity class_746Var : aM_.world.getEntitiesByClass(PlayerEntity.class, box, LivingEntity::isAlive)) {
            Box boundingBox = class_746Var.getBoundingBox();
            double factor = 1.0d - (Math.sqrt((Math.pow(splash.x - MathHelper.clamp(splash.x, boundingBox.minX, boundingBox.maxX), 2.0d) + Math.pow(splash.y - MathHelper.clamp(splash.y, boundingBox.minY, boundingBox.maxY), 2.0d)) + Math.pow(splash.getZ() - MathHelper.clamp(splash.getZ(), boundingBox.minZ, boundingBox.maxZ), 2.0d)) / 4.0d);
            if (factor > 0.0d) {
                int hitPercent = (int) Math.round(factor * 100.0d);
                List<HitBuffInfo> hitBuffs = new ArrayList<>();
                List<StatusEffectInstance> espEffects = new ArrayList<>();

                for (Map.Entry<RegistryEntry<StatusEffect>, int[]> entry : matchedEffects) {
                    int baseDuration = entry.getValue()[0];
                    int amplifier = entry.getValue()[1];
                    double exactDurationSec = (baseDuration * factor) / 20.0d;
                    int actualTicks = Math.max(0, MathHelper.floor((baseDuration * factor) + 0.5d));

                    RegistryEntry<StatusEffect> effectType = entry.getKey();
                    StatusEffect effect = effectType.value();
                    String buffName = effect.getName().getString();
                    if (amplifier > 0) {
                        buffName += " " + MathUtil.a(amplifier);
                    }
                    Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(effectType);
                    int color = effect.getColor();

                    if (exactDurationSec > 0.5d || baseDuration == 0) {
                        hitBuffs.add(new HitBuffInfo(effectType, buffName, amplifier, exactDurationSec, color, sprite));
                    }

                    if (actualTicks > 20) {
                        espEffects.add(new StatusEffectInstance(effectType, actualTicks, amplifier));
                    }
                }

                if (!hitBuffs.isEmpty()) {
                    this.potionHitTags.removeIf(t -> t.entityId == class_746Var.getId());
                    this.potionHitTags.add(new PotionHitTag(
                        class_746Var.getId(),
                        class_746Var.getUuid(),
                        class_746Var.getName().getString(),
                        class_746Var.getPos(),
                        factor,
                        hitPercent,
                        hitBuffs,
                        System.currentTimeMillis(),
                        4200L
                    ));
                }

                if (class_746Var != aM_.player) {
                    ChatUtil.a((Object) ("[" + j() + "]"), ChatUtil.b(class_746Var.getName().getString() + " получил эффекты от \"").append(potionText != null ? potionText : Text.literal(potionDisplayName != null ? potionDisplayName : "Зелье")).append(ChatUtil.b("\"")));
                    ChatUtil.a("[" + j() + "]", "- Успешность: &a" + hitPercent + "%");
                    for (HitBuffInfo buff : hitBuffs) {
                        ChatUtil.a("[" + j() + "]", "- &c" + buff.name + " &7(" + String.format(Locale.US, "%.1fс", buff.initialDurationSec) + ")");
                    }
                }

                String firstBuffName = !hitBuffs.isEmpty() ? hitBuffs.get(0).name : (potionDisplayName != null ? potionDisplayName : "Зелье");
                String firstBuffSec = !hitBuffs.isEmpty() ? String.format(Locale.US, "%.1fс", hitBuffs.get(0).initialDurationSec) : "";
                MutableText notifMsg;
                if (class_746Var == aM_.player) {
                    notifMsg = Text.literal("Вы получили ").styled(style -> style.withColor(Delta.h().d().o().a(ThemeInfo.PRIMARY).a()))
                        .append(Text.literal(firstBuffName + " (" + firstBuffSec + ", " + hitPercent + "%)"));
                } else {
                    notifMsg = Text.literal(class_746Var.getName().getString() + " получил " + firstBuffName + " (" + firstBuffSec + ", " + hitPercent + "%)");
                }
                Delta.h().d().m().a(new Notification(new ItemStack(Items.SPLASH_POTION), notifMsg, 2500));

                if (!espEffects.isEmpty()) {
                    Delta.h().d().t().aa().q().add(new EntityESP.a(List.copyOf(espEffects), class_746Var.getId(), class_746Var.age));
                }
            }
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b()) {
            renderPotionHitTags(event);
        }
    }

    private void renderPotionHitTags(DrawEvent event) {
        if (!this.b.a("Зелья").c().booleanValue() || !this.visualPotionTag.c().booleanValue()) {
            return;
        }
        long now = System.currentTimeMillis();
        this.potionHitTags.removeIf(tag -> (now - tag.createTime) > tag.maxDurationMs);
        if (this.potionHitTags.isEmpty()) {
            return;
        }

        int primaryColor = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        MatrixStack matrices = event.i().getMatrices();

        for (PotionHitTag tag : this.potionHitTags) {
            long elapsed = now - tag.createTime;
            float alpha;
            if (elapsed < 200L) {
                alpha = Math.min(1.0f, elapsed / 200.0f);
            } else if (elapsed > (tag.maxDurationMs - 600L)) {
                alpha = Math.max(0.0f, (tag.maxDurationMs - elapsed) / 600.0f);
            } else {
                alpha = 1.0f;
            }
            if (alpha <= 0.005f) {
                continue;
            }

            float popScale = 0.82f + (0.18f * Math.min(1.0f, elapsed / 250.0f));

            boolean isSelfFirstPerson = (tag.entityId == aM_.player.getId()) && aM_.options.getPerspective().isFirstPerson();
            float screenCenterX;
            float screenCenterY;

            if (isSelfFirstPerson) {
                screenCenterX = aM_.getWindow().getScaledWidth() / 2.0f;
                screenCenterY = (aM_.getWindow().getScaledHeight() / 2.0f) + 38.0f;
            } else {
                Entity target = aM_.world.getEntityById(tag.entityId);
                if (target != null && target.isAlive()) {
                    tag.lastWorldPos = MathUtil.a(target, event.g());
                }
                float drift = (float) Math.min(0.35d, (elapsed / (double) tag.maxDurationMs) * 0.35d);
                double targetHeight = target != null ? target.getHeight() : 1.8d;
                Vec3d worldPos = tag.lastWorldPos.add(0.0d, targetHeight + 0.65d + drift, 0.0d);
                Vector2f screen = ProjectUtil.a(worldPos.x, worldPos.y, worldPos.z);
                if (!ProjectUtil.a(screen)) {
                    continue;
                }
                screenCenterX = screen.x();
                screenCenterY = screen.y();
            }

            float iconSize = 11.0f;
            float rowHeight = 13.0f;
            float padX = 6.5f;
            float padY = 5.0f;

            float maxRowW = 0.0f;
            List<String> timeStrings = new ArrayList<>();
            double elapsedSec = elapsed / 1000.0d;

            for (HitBuffInfo buff : tag.buffs) {
                double remaining = Math.max(0.0d, buff.initialDurationSec - elapsedSec);
                String timeStr;
                if (remaining >= 60.0d) {
                    int m = (int) (remaining / 60.0d);
                    double s = remaining % 60.0d;
                    timeStr = String.format(Locale.US, "%d:%04.1fс", m, s);
                } else if (buff.initialDurationSec <= 0.0d) {
                    timeStr = "Мгновенно";
                } else {
                    timeStr = String.format(Locale.US, "%.1fс", remaining);
                }
                timeStrings.add(timeStr);

                float nameW = Fonts.c.a(buff.name, 7.0f);
                float timeW = Fonts.e.a(timeStr, 6.5f);
                float rowW = iconSize + 4.5f + nameW + 6.0f + timeW;
                maxRowW = Math.max(maxRowW, rowW);
            }

            String hitStr = "Попадание: " + tag.hitPercent + "%";
            float hitTextW = Fonts.e.a(hitStr, 6.5f);
            float pillW = hitTextW + 10.0f;
            float pillH = 10.5f;

            float contentW = Math.max(maxRowW, pillW);
            float cardW = contentW + (padX * 2.0f);
            float cardH = padY + (tag.buffs.size() * rowHeight) + 3.0f + pillH + padY;

            float cardX = screenCenterX - (cardW / 2.0f);
            float cardY = screenCenterY - (cardH / 2.0f);

            matrices.push();
            matrices.translate(screenCenterX, screenCenterY, 0.0f);
            matrices.scale(popScale, popScale, 1.0f);
            matrices.translate(-screenCenterX, -screenCenterY, 0.0f);

            int bgColor = ColorUtil.a(12, 12, 16, (int) (225.0f * alpha));
            int outlineColor = ColorUtil.a(primaryColor, 0.45f * alpha);

            event.d().a(matrices, cardX, cardY, cardW, cardH, 4.0f, bgColor);
            event.d().a(matrices, cardX, cardY, cardW, cardH, 4.0f, 0.75f, outlineColor);

            float currY = cardY + padY;
            for (int i = 0; i < tag.buffs.size(); i++) {
                HitBuffInfo buff = tag.buffs.get(i);
                String timeStr = timeStrings.get(i);
                float rowX = cardX + padX;

                if (buff.sprite != null) {
                    event.e().a(event.i(), buff.sprite, rowX, currY + 1.0f, 0.0f, iconSize / 18.0f, alpha);
                } else {
                    event.e().a(event.i(), new ItemStack(Items.SPLASH_POTION), rowX - 1.0f, currY - 0.5f, 0, alpha, 12.0f / 16.0f, false);
                }

                float nameX = rowX + iconSize + 4.5f;
                Fonts.c.a(matrices, buff.name, nameX, currY + 2.0f, 7.0f, ColorUtil.a(-1, alpha));

                float nameW = Fonts.c.a(buff.name, 7.0f);
                float timeX = nameX + nameW + 6.0f;
                Fonts.e.a(matrices, timeStr, timeX, currY + 2.25f, 6.5f, ColorUtil.a(primaryColor, alpha), 0.0f);

                currY += rowHeight;
            }

            currY += 2.0f;
            int pillColor;
            if (tag.hitPercent >= 80) {
                pillColor = ColorUtil.a(74, 222, 128, 255);
            } else if (tag.hitPercent >= 50) {
                pillColor = ColorUtil.a(250, 204, 21, 255);
            } else {
                pillColor = ColorUtil.a(248, 113, 113, 255);
            }

            float pillX = cardX + (cardW - pillW) / 2.0f;
            float pillY = currY;

            event.d().a(matrices, pillX, pillY, pillW, pillH, 2.5f, ColorUtil.a(pillColor, 0.18f * alpha));
            event.d().a(matrices, pillX, pillY, pillW, pillH, 2.5f, 0.5f, ColorUtil.a(pillColor, 0.45f * alpha));
            Fonts.e.a(matrices, hitStr, pillX + 5.0f, pillY + 2.0f, 6.5f, ColorUtil.a(pillColor, alpha), 0.0f);

            matrices.pop();
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c()) {
            EntityAttributesS2CPacket class_2781VarD = (EntityAttributesS2CPacket) event.d();
            if (class_2781VarD instanceof EntityAttributesS2CPacket) {
                EntityAttributesS2CPacket packet = class_2781VarD;
                for (EntityAttributesS2CPacket.Entry entry : packet.getEntries()) {
                    if (entry.attribute().getKey().toString().contains("minecraft:movement_speed")) {
                        for (EntityAttributeModifier modifier : entry.modifiers()) {
                            if ((aM_.world.getEntityById(packet.getEntityId()) instanceof PlayerEntity) && modifier.id().toString().equals("minecraft:effect.speed") && modifier.value() <= 0.40000001199465773d && modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                                Delta.h().d().t().aa().q().removeIf(info -> info.b() == packet.getEntityId());
                            }
                        }
                    }
                }
            }
            EntityStatusS2CPacket class_2663VarD = (EntityStatusS2CPacket) event.d();
            if (class_2663VarD instanceof EntityStatusS2CPacket) {
                EntityStatusS2CPacket s2CPacket = class_2663VarD;
                Entity entityMethod_11469 = s2CPacket.getEntity(aM_.world);
                if (entityMethod_11469 instanceof LivingEntity) {
                    LivingEntity class_746Var = (LivingEntity) entityMethod_11469;
                    if (s2CPacket.getStatus() == 35) {
                        Delta.h().d().t().aa().q().removeIf(info2 -> info2.b() == class_746Var.getId());
                        if (this.b.a("Тотема").c().booleanValue()) {
                            ItemStack totem = class_746Var.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING ? class_746Var.getMainHandStack() : class_746Var.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING ? class_746Var.getOffHandStack() : null;
                            if (totem != null) {
                                String name = ServerUtil.a.a() ? ServerUtil.a.b(totem) : totem.getName().getString();
                                ChatUtil.a("[" + j() + "]", (class_746Var == aM_.player ? "Вы потеряли " : class_746Var.getName().getString() + " потерял ") + name + ", зачарован: " + ((name.startsWith("Талисман") || totem.hasGlint()) ? "&a●&7" : "&c●&7"));
                            }
                        }
                    }
                }
            }
        }
    }

    public static class HitBuffInfo {
        public final RegistryEntry<StatusEffect> effectType;
        public final String name;
        public final int amplifier;
        public final double initialDurationSec;
        public final int color;
        public final Sprite sprite;

        public HitBuffInfo(RegistryEntry<StatusEffect> effectType, String name, int amplifier, double initialDurationSec, int color, Sprite sprite) {
            this.effectType = effectType;
            this.name = name;
            this.amplifier = amplifier;
            this.initialDurationSec = initialDurationSec;
            this.color = color;
            this.sprite = sprite;
        }
    }

    public static class PotionHitTag {
        public final int entityId;
        public final UUID entityUuid;
        public final String playerName;
        public Vec3d lastWorldPos;
        public final double factor;
        public final int hitPercent;
        public final List<HitBuffInfo> buffs;
        public final long createTime;
        public final long maxDurationMs;

        public PotionHitTag(int entityId, UUID entityUuid, String playerName, Vec3d lastWorldPos, double factor, int hitPercent, List<HitBuffInfo> buffs, long createTime, long maxDurationMs) {
            this.entityId = entityId;
            this.entityUuid = entityUuid;
            this.playerName = playerName;
            this.lastWorldPos = lastWorldPos;
            this.factor = factor;
            this.hitPercent = hitPercent;
            this.buffs = buffs;
            this.createTime = createTime;
            this.maxDurationMs = maxDurationMs;
        }
    }

    private static class PotionRecord {
        final ItemStack stack;
        final Vec3d pos;
        final long timestamp;

        PotionRecord(ItemStack stack, Vec3d pos, long timestamp) {
            this.stack = stack;
            this.pos = pos;
            this.timestamp = timestamp;
        }
    }

    public enum a {
        POPPER_POTION(List.of(Map.entry(StatusEffects.SLOWNESS, new int[]{InterfaceC0020Opcode.aN, 9}), Map.entry(StatusEffects.SPEED, new int[]{TokenId.au_, 4}), Map.entry(StatusEffects.BLINDNESS, new int[]{100, 9}), Map.entry(StatusEffects.GLOWING, new int[]{3600, 0})), "[★] Хлопушка", new int[]{16738740}, new int[]{16711765, 16727869, 16743972, 16760076, 14410269, 9628759, 4846994, 65484}),
        HOLY_WATER(List.of(Map.entry(StatusEffects.REGENERATION, new int[]{900, 1}), Map.entry(StatusEffects.INVISIBILITY, new int[]{12000, 1}), Map.entry(StatusEffects.INSTANT_HEALTH, new int[]{0, 2})), "[★] Святая вода", new int[]{16777215}, new int[]{16777163, 16777148, 16777132, 16777117, 16777102, 16777087, 16776815, 16776800, 16776785, 16776769, 16776754}),
        RAGE_POTION(List.of(Map.entry(StatusEffects.STRENGTH, new int[]{600, 4}), Map.entry(StatusEffects.SLOWNESS, new int[]{600, 3})), "[★] Зелье Гнева", new int[]{10040115}, new int[]{9109504, 10620416, 12131328, 13707520, 15218432, 16729344, 16732928, 16736512, 16740352, 16743936, 16747520}),
        PALLADIN_POTION(List.of(Map.entry(StatusEffects.RESISTANCE, new int[]{12000, 0}), Map.entry(StatusEffects.FIRE_RESISTANCE, new int[]{12000, 0}), Map.entry(StatusEffects.HEALTH_BOOST, new int[]{1200, 2}), Map.entry(StatusEffects.INVISIBILITY, new int[]{18000, 2})), "[★] Зелье Палладина", new int[]{65535}, new int[]{13762395, 14090092, 14417789, 14745486, 15007648, 15335345, 15663042, 15990739, 15663042, 15335345, 15007648, 14745486, 14417789, 14090092, 13762395}),
        ASSASSIN_POTION(List.of(Map.entry(StatusEffects.STRENGTH, new int[]{1200, 3}), Map.entry(StatusEffects.SPEED, new int[]{6000, 2}), Map.entry(StatusEffects.HASTE, new int[]{1200, 0}), Map.entry(StatusEffects.INSTANT_DAMAGE, new int[]{0, 1})), "[★] Зелье Ассасина", new int[]{3355443}, new int[]{4277061, 4603456, 4929850, 5256245, 5516848, 5843242, 6169637, 6496032, 6822427, 7148821, 7409424, 7735819, 8062213, 8388608}),
        RADIATION_POTION(List.of(Map.entry(StatusEffects.POISON, new int[]{1200, 1}), Map.entry(StatusEffects.WITHER, new int[]{1200, 1}), Map.entry(StatusEffects.SLOWNESS, new int[]{1800, 2}), Map.entry(StatusEffects.HUNGER, new int[]{1200, 4}), Map.entry(StatusEffects.GLOWING, new int[]{2400, 0})), "[★] Зелье Радиации", new int[]{3329330}, new int[]{16774970, 16250192, 15659878, 15135100, 14545043, 14020265, 13429951, 12905919, 12382378, 11858836, 11269759, 10746217, 10222676, 9699134}),
        SLEEPING_PILL(List.of(Map.entry(StatusEffects.WEAKNESS, new int[]{1800, 1}), Map.entry(StatusEffects.MINING_FATIGUE, new int[]{InterfaceC0020Opcode.aN, 1}), Map.entry(StatusEffects.WITHER, new int[]{1800, 2}), Map.entry(StatusEffects.BLINDNESS, new int[]{InterfaceC0020Opcode.aN, 0})), "[★] Снотворное", new int[]{255, 4737096}, new int[]{4132250, 3219615, 2306725, 1394090, 481455, 812728, 2322884, 3833041, 5408733, 6918889});

        private final List<Map.Entry<RegistryEntry<StatusEffect>, int[]>> h;
        private final String i;
        private final int[] j;
        private final int[] k;

        @Generated
        a(final List effects, final String displayName, final int[] throwColor, final int[] nameColors) {
            this.h = effects;
            this.i = displayName;
            this.j = throwColor;
            this.k = nameColors;
        }

        @Generated
        public List<Map.Entry<RegistryEntry<StatusEffect>, int[]>> b() {
            return this.h;
        }

        @Generated
        public String c() {
            return this.i;
        }

        @Generated
        public int[] d() {
            return this.j;
        }

        @Generated
        public int[] e() {
            return this.k;
        }

        public MutableText a() {
            int start = this.i.indexOf(32) + 1;
            MutableText text = Text.literal("");
            int i = 0;
            while (i < this.i.length()) {
                int color = i < start ? this.k[0] : this.k[Math.min(i - start, this.k.length - 1)];
                text.append(Text.literal(String.valueOf(this.i.charAt(i))).setStyle(Style.EMPTY.withColor(color).withBold(true)));
                i++;
            }
            return text;
        }
    }
}
