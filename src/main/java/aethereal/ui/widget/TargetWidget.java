package aethereal.ui.widget;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.InventoryUtil;
import aethereal.util.MathUtil;
import aethereal.util.ServerUtil;

import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;
import aethereal.core.GlobalEvent;
import aethereal.event.DrawEvent;
import aethereal.module.misc.StreamerMode;
import aethereal.module.render.EntityESP;
import aethereal.ui.element.DragInfo;
import aethereal.ui.widget.Widget;

import aethereal.render.AnimationUtil;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

public class TargetWidget extends Widget {
    private static TargetWidget INSTANCE;

    private final ModeSetting mode;
    private final BooleanSetting transparentStyle;
    private final BooleanSetting showOnHover;
    private final BooleanSetting showTargetEffects;
    private final BooleanSetting showTargetCooldowns;
    private final BooleanSetting effectTimeFormat;

    private final AnimationUtil hpAnim;
    private final AnimationUtil lineAnim;
    private LivingEntity currentTarget;

    private float modalX = Float.NaN;
    private float modalY = Float.NaN;
    private final float modalW = 220.0f;
    private final float modalH = 188.0f;

    private boolean draggingScaleSlider = false;
    private boolean draggingOpacitySlider = false;

    public static TargetWidget getInstance() {
        return INSTANCE;
    }

    public TargetWidget() {
        super(new DragInfo("Таргет-худ", 0.0f, 0.0f, 0.0f, 0.0f));
        INSTANCE = this;

        this.mode = new ModeSetting("Режим", "Встроенный", "Встроенный", "Стандарт");
        this.transparentStyle = new BooleanSetting("Прозрачный стиль", true);
        this.showOnHover = new BooleanSetting("Показывать при наводке", false);
        this.showTargetEffects = new BooleanSetting("Эффекты цели", false);
        this.showTargetCooldowns = new BooleanSetting("Откаты цели", true);
        this.effectTimeFormat = new BooleanSetting("Формат времени эффектов", true);

        this.hpAnim = new AnimationUtil();
        this.lineAnim = new AnimationUtil();

        j().a(this);
        j().a(0);
        a(this.mode, this.transparentStyle, this.showOnHover, this.showTargetEffects, this.showTargetCooldowns, this.effectTimeFormat);
    }

    @Override
    public void a(DrawEvent event) {
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        if (a() <= 0.0f) {
            super.a(event);
            return;
        }

        LivingEntity target = getActiveTarget();
        if (target == null) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        if (this.mode.l("Встроенный")) {
            renderIntegrated(event, target);
        } else {
            renderStandard(event, target);
        }

        j().a(0);
    }

    /**
     * Mode: "Встроенный стиль"
     * Matches the user screenshot:
     * Positioned above the hotbar:
     * 1. Cooldowns row floating above (Gapple, Potion, Pearl, active "ИСПОЛЬЗУЕТ 1.32s" badge)
     * 2. Centered coral label "ПРОТИВНИК"
     * 3. Row "13 ◯ Vorilr2kz49oi ◯ 6"
     * 4. Sleek animated gradient health bar underneath
     */
    private void renderIntegrated(DrawEvent event, LivingEntity target) {
        float sw = Interface.aM_.getWindow().getScaledWidth();
        float sh = Interface.aM_.getWindow().getScaledHeight();
        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        float opacity = a() * getBgOpacity();

        // Default position centered above hotbar if not dragged
        if (j().a() == 0.0f && j().b() == 0.0f) {
            j().a((sw - 140.0f) / 2.0f);
            j().b(sh - 68.0f);
        }

        float x = j().a();
        float y = j().b();
        float cx = x + (j().getRawWidth() / 2.0f);

        boolean isPreview = (target == aM_.player) && (aM_.currentScreen instanceof ChatScreen);
        String name = isPreview ? "Vorilr2kz49oi" : getTargetName(target);
        int hp = isPreview ? 13 : (int) Math.ceil(ServerUtil.a.a(target));
        int stat = isPreview ? 6 : (int) Math.round(aM_.player.distanceTo(target));

        String hpStr = String.valueOf(hp);
        String statStr = String.valueOf(stat);
        float ringSize = 6.0f;

        float hpW = Fonts.d.a(hpStr, 7.5f);
        float nameW = Fonts.d.a(name, 7.5f);
        float statW = Fonts.d.a(statStr, 7.5f);
        float rowW = hpW + 4.0f + ringSize + 4.0f + nameW + 4.0f + ringSize + 4.0f + statW;

        float totalW = Math.max(140.0f, rowW + 20.0f);
        j().c(totalW);
        j().d(38.0f);

        // 1. Cooldowns row ("Откаты цели") above ПРОТИВНИК
        if (this.showTargetCooldowns.c().booleanValue()) {
            boolean isUsing = (target.isUsingItem()) || isPreview;
            float topY = y - 24.0f;

            float slotSize = 15.0f;
            float slotGap = 3.5f;

            ItemStack[] cooldownItems = {
                new ItemStack(Items.GOLDEN_APPLE),
                new ItemStack(Items.SPLASH_POTION),
                new ItemStack(Items.ENDER_PEARL)
            };

            float slotsW = (cooldownItems.length * slotSize) + ((cooldownItems.length - 1) * slotGap);
            float activeBadgeW = 28.0f;
            float activeBadgeH = 22.0f;

            float rowTotalW = slotsW + (isUsing ? activeBadgeW + 5.0f : 0.0f);
            float startCdX = cx - (rowTotalW / 2.0f);

            // Cooldown icons
            for (int i = 0; i < cooldownItems.length; i++) {
                float sx = startCdX + (i * (slotSize + slotGap));
                float sy = topY + 4.0f;

                int slotBg = ColorUtil.a(20, 22, 30, (int) (190 * opacity));
                event.d().a(matrices, sx, sy, slotSize, slotSize, 3.5f, slotBg);
                event.d().a(matrices, sx, sy, slotSize, slotSize, 3.5f, 0.5f, ColorUtil.a(255, 255, 255, (int) (20 * opacity)));

                event.e().a(event.i(), cooldownItems[i], sx + 2.0f, sy + 2.0f, 0, opacity, 0.50f, false);

                // Small cooldown indicator bar
                float barY = sy + slotSize - 2.0f;
                event.d().a(matrices, sx + 2.0f, barY, slotSize - 4.0f, 1.0f, 0.5f, ColorUtil.a(primary, 0.85f * opacity));
            }

            // Active item using badge: [ИСПОЛЬЗУЕТ / 1.32s]
            if (isUsing) {
                float bx = startCdX + slotsW + 5.0f;
                float by = topY + 0.5f;

                float timeLeft = target.isUsingItem() ? ((float) target.getItemUseTimeLeft() / 20.0f) : 1.32f;
                ItemStack usingStack = target.isUsingItem() ? target.getActiveItem() : new ItemStack(Items.GOLDEN_APPLE);

                // Warm yellow/amber badge matching screenshot
                int badgeBg = ColorUtil.a(215, 175, 95, (int) (215 * opacity));
                event.d().a(matrices, bx, by, activeBadgeW, activeBadgeH, 4.0f, badgeBg);
                event.d().a(matrices, bx, by, activeBadgeW, activeBadgeH, 4.0f, 0.5f, ColorUtil.a(255, 220, 130, (int) (120 * opacity)));

                // "ИСПОЛЬЗУЕТ"
                String useTitle = "ИСПОЛЬЗУЕТ";
                float utw = Fonts.e.a(useTitle, 4.25f);
                Fonts.e.a(event.h(), useTitle, bx + ((activeBadgeW - utw) / 2.0f), by + 2.25f, 4.25f, ColorUtil.a(45, 35, 20, (int) (230 * opacity)));

                // Using item icon
                event.e().a(event.i(), usingStack, bx + ((activeBadgeW - 8.0f) / 2.0f) - 1.0f, by + 6.5f, 0, opacity, 0.45f, false);

                // Time remaining e.g. "1.32s"
                String timeStr = String.format(Locale.US, "%.2fs", timeLeft);
                float tsw = Fonts.d.a(timeStr, 5.25f);
                Fonts.d.a(matrices, timeStr, bx + ((activeBadgeW - tsw) / 2.0f), by + 15.0f, 5.25f, ColorUtil.a(40, 30, 15, (int) (245 * opacity)));
            }
        }

        // 2. Coral red label "ПРОТИВНИК"
        String enemyLabel = "ПРОТИВНИК";
        float elW = Fonts.e.a(enemyLabel, 5.0f);
        Fonts.e.a(event.h(), enemyLabel, cx - (elW / 2.0f), y, 5.0f, ColorUtil.a(255, 95, 115, (int) (235 * opacity)));

        // 3. Name & Stats row: "13 ◯ Vorilr2kz49oi ◯ 6"
        float textY = y + 7.5f;
        float startX = cx - (rowW / 2.0f);

        // HP number (13)
        Fonts.d.a(matrices, hpStr, startX, textY, 7.5f, ColorUtil.a(-1, opacity));

        // Purple circular ring ◯
        float rx1 = startX + hpW + 4.0f;
        float ry = textY + 1.25f;
        int ring1Color = ColorUtil.a(140, 120, 255, 255);
        event.d().a(matrices, rx1, ry, ringSize, ringSize, ringSize / 2.0f, 0.7f, ColorUtil.a(ring1Color, 0.25f * opacity));
        event.d().a(matrices, rx1, ry, ringSize, ringSize, ringSize / 2.0f, 0.85f, ColorUtil.a(ring1Color, opacity));

        // Name
        float nx = rx1 + ringSize + 4.0f;
        Fonts.d.a(matrices, name, nx, textY, 7.5f, ColorUtil.a(-1, opacity));

        // Amber circular ring ◯
        float rx2 = nx + nameW + 4.0f;
        int ring2Color = ColorUtil.a(235, 175, 75, 255);
        event.d().a(matrices, rx2, ry, ringSize, ringSize, ringSize / 2.0f, 0.7f, ColorUtil.a(ring2Color, 0.25f * opacity));
        event.d().a(matrices, rx2, ry, ringSize, ringSize, ringSize / 2.0f, 0.85f, ColorUtil.a(ring2Color, opacity));

        // Distance / stat (6)
        float sx = rx2 + ringSize + 4.0f;
        Fonts.d.a(matrices, statStr, sx, textY, 7.5f, ColorUtil.a(-1, opacity));

        // 4. Sleek gradient health bar underneath
        float barW = Math.max(105.0f, rowW);
        float barH = 1.75f;
        float barX = cx - (barW / 2.0f);
        float barY = textY + 10.5f;

        float maxHp = isPreview ? 20.0f : target.getMaxHealth();
        float curHp = isPreview ? 13.0f : ServerUtil.a.a(target);
        float targetPercent = MathUtil.b(curHp / Math.max(1.0f, maxHp), 0.0f, 1.0f);
        float smoothHp = this.lineAnim.a(targetPercent, targetPercent, 0.4f);

        event.d().a(matrices, barX, barY, barW, barH, 0.75f, ColorUtil.a(255, 255, 255, (int) (30 * opacity)));
        event.d().a(matrices, barX, barY, barW * smoothHp, barH, 0.75f, ColorUtil.a(primary, opacity));

        super.a(event);
    }

    /**
     * Mode: "Стандарт" (Стиль виджета)
     * Matches card 1:
     * Compact card with avatar, name, health, armor row,
     * effects list on right, and animated health bar.
     */
    private void renderStandard(DrawEvent event, LivingEntity target) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        float opacity = a() * getBgOpacity();

        boolean showEffects = this.showTargetEffects.c().booleanValue();
        float cardW = showEffects ? 172.0f : 122.0f;
        float cardH = 38.0f;

        j().c(cardW);
        j().d(cardH);

        int bgAlpha = (int) (opacity * (this.transparentStyle.c().booleanValue() ? 175 : 240));
        event.d().a(matrices, x, y, cardW, cardH, 5.0f, ColorUtil.a(16, 17, 23, bgAlpha));
        event.d().a(matrices, x, y, cardW, cardH, 5.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (20 * opacity)));

        boolean isPreview = (target == aM_.player) && (aM_.currentScreen instanceof ChatScreen);
        String name = isPreview ? "annihilatorq" : getTargetName(target);
        int hp = isPreview ? 18 : (int) Math.ceil(ServerUtil.a.a(target));

        // Target Avatar Head
        float headSize = 20.0f;
        float headX = x + 5.0f;
        float headY = y + 5.0f;
        EntityESP.drawEntityHead(matrices, event, target, headX, headY, headSize, 3.0f, opacity);

        // Nickname
        float textX = headX + headSize + 5.0f;
        Fonts.e.a(event.h(), name, textX, headY + 1.0f, 6.75f, ColorUtil.a(-1, opacity));

        // Health + Ring
        String hpStr = hp + " ◯";
        float hpW = Fonts.e.a(hpStr, 6.0f);
        float hpX = (showEffects ? x + 105.0f : (x + cardW) - 6.0f) - hpW;
        Fonts.e.a(event.h(), hpStr, hpX, headY + 1.5f, 6.0f, ColorUtil.a(primary, opacity));

        // Armor row below nickname
        float itemSize = 8.5f;
        float itemY = headY + 11.5f;
        float curItemX = textX;

        ItemStack[] armorStacks = {
            target.getEquippedStack(EquipmentSlot.FEET),
            target.getEquippedStack(EquipmentSlot.LEGS),
            target.getEquippedStack(EquipmentSlot.CHEST),
            target.getEquippedStack(EquipmentSlot.HEAD),
            target.getOffHandStack(),
            target.getMainHandStack()
        };

        for (ItemStack stack : armorStacks) {
            if (stack.isEmpty() && isPreview) {
                stack = new ItemStack(Items.NETHERITE_CHESTPLATE);
            }
            event.d().a(matrices, curItemX, itemY, itemSize, itemSize, 2.0f, ColorUtil.a(28, 30, 40, (int) (220 * opacity)));
            if (!stack.isEmpty()) {
                event.e().a(event.i(), InventoryUtil.a(stack), curItemX + 0.5f, itemY + 0.5f, 0, opacity, itemSize / 16.0f, false);
            }
            curItemX += itemSize + 1.5f;
        }

        // Right side: Effects list
        if (showEffects) {
            float divX = x + 112.0f;
            event.d().a(matrices, divX, y + 4.0f, 0.5f, cardH - 8.0f, 0.0f, ColorUtil.a(255, 255, 255, (int) (18 * opacity)));

            List<StatusEffectInstance> effects = getTargetEffects(target, isPreview);
            float effectY = y + 4.5f;
            int maxDraw = Math.min(3, effects.size());

            for (int i = 0; i < maxDraw; i++) {
                StatusEffectInstance inst = effects.get(i);
                Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(inst.getEffectType());
                if (sprite != null) {
                    event.e().a(event.i(), sprite, divX + 4.0f, effectY + 1.0f, 0, 0.40f, opacity);
                }

                String eName = Text.translatable(((StatusEffect) inst.getEffectType().value()).getTranslationKey()).getString();
                if (eName.length() > 6) eName = eName.substring(0, 6);
                if (inst.getAmplifier() > 0) eName += " " + (inst.getAmplifier() + 1);

                boolean harmful = ((StatusEffect) inst.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;
                int nColor = harmful ? ColorUtil.a(255, 76, 79, 255) : -1;
                Fonts.e.a(event.h(), eName, divX + 13.0f, effectY + 1.5f, 5.0f, ColorUtil.a(nColor, opacity));

                int sec = inst.getDuration() / 20;
                String timeStr = String.format("%02d:%02d", sec / 60, sec % 60);
                float tw = Fonts.e.a(timeStr, 4.75f);
                Fonts.e.a(event.h(), timeStr, (x + cardW) - tw - 4.0f, effectY + 1.5f, 4.75f, ColorUtil.a(180, 185, 200, (int) (200 * opacity)));

                effectY += 9.5f;
            }
        }

        // Bottom animated health bar
        float barX = x + 5.0f;
        float barY = (y + cardH) - 4.0f;
        float barW = cardW - 10.0f;
        float barH = 2.0f;

        float maxHp = isPreview ? 20.0f : target.getMaxHealth();
        float curHp = isPreview ? 18.0f : ServerUtil.a.a(target);
        float targetPercent = MathUtil.b(curHp / Math.max(1.0f, maxHp), 0.0f, 1.0f);
        float smoothHp = this.lineAnim.a(targetPercent, targetPercent, 0.4f);

        event.d().a(matrices, barX, barY, barW, barH, 1.0f, ColorUtil.a(255, 255, 255, (int) (30 * opacity)));
        event.d().a(matrices, barX, barY, barW * smoothHp, barH, 1.0f, ColorUtil.a(primary, opacity));

        super.a(event);
    }

    /**
     * Settings Window
     * Matches image: "Настройки виджета TargetHud"
     */
    @Override
    protected void b(DrawEvent event) {
        float anim = this.c.c();
        if (anim <= 0.0f) return;

        float screenW = Interface.aM_.getWindow().getScaledWidth();
        float screenH = Interface.aM_.getWindow().getScaledHeight();

        if (Float.isNaN(this.modalX) || Float.isNaN(this.modalY)) {
            float defX = j().a() + j().f() + 8.0f;
            float defY = j().b();
            if (defX + this.modalW > screenW - 10.0f) {
                defX = j().a() - this.modalW - 8.0f;
            }
            if (defY + this.modalH > screenH - 10.0f) {
                defY = j().b() - this.modalH - 8.0f;
            }
            this.modalX = MathUtil.b(defX, 10.0f, screenW - this.modalW - 10.0f);
            this.modalY = MathUtil.b(defY, 10.0f, screenH - this.modalH - 10.0f);
        }

        float mx = this.modalX;
        float my = this.modalY;
        float mw = this.modalW;
        float mh = this.modalH;

        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();

        int modalBg = ColorUtil.a(16, 17, 23, (int) (245 * anim));
        int modalOutline = ColorUtil.a(255, 255, 255, (int) (25 * anim));
        event.d().a(event.h(), mx, my, mw, mh, 8.0f, modalBg, anim, ColorUtil.a(primary, 0.12f * anim), 12.0f);
        event.d().a(matrices, mx, my, mw, mh, 8.0f, 0.5f, modalOutline);

        // Header
        Fonts.e.a(event.h(), "Настройки виджета TargetHud", mx + 10.0f, my + 8.0f, 7.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Настройте стиль виджета по своему вкусу", mx + 10.0f, my + 17.5f, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), anim));

        float btnSize = 13.0f;
        float btnX = (mx + mw) - 10.0f - btnSize;
        float btnY = my + 8.0f;
        event.d().a(matrices, btnX, btnY, btnSize, btnSize, 3.5f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.a.a(matrices, "J", btnX + 3.0f, btnY + 2.5f, 7.0f, ColorUtil.a(primary, anim));

        // Style Cards
        float cardW = (mw - 24.0f) / 2.0f;
        float cardH = 44.0f;
        float startCardY = my + 27.0f;

        drawStyleCard(event, mx + 10.0f, startCardY, cardW, cardH, "Стиль виджета", "Компактный виджет, плотно набитый содержимым.", "Стандарт", anim, primary);
        drawStyleCard(event, mx + 14.0f + cardW, startCardY, cardW, cardH, "Встроенный стиль", "Совершенно новый и свежий стиль в мире Minecraft.", "Встроенный", anim, primary);

        // SECTION: НАСТРОЙКИ ВИДЖЕТА
        float secY = startCardY + cardH + 6.0f;
        Fonts.e.a(event.h(), "НАСТРОЙКИ ВИДЖЕТА", mx + 10.0f, secY, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        // 2 Columns of Checkboxes
        float col2X = mx + (mw / 2.0f) + 4.0f;
        float chkY1 = secY + 8.5f;

        drawCheckbox(event, mx + 10.0f, chkY1, "Прозрачный стиль", this.transparentStyle.c().booleanValue(), anim, primary);
        drawCheckbox(event, col2X, chkY1, "Показывать при наводке", this.showOnHover.c().booleanValue(), anim, primary);

        float chkY2 = chkY1 + 11.0f;
        drawCheckbox(event, mx + 10.0f, chkY2, "Эффекты цели", this.showTargetEffects.c().booleanValue(), anim, primary);
        drawCheckbox(event, col2X, chkY2, "Откаты цели", this.showTargetCooldowns.c().booleanValue(), anim, primary);

        float chkY3 = chkY2 + 11.0f;
        drawCheckbox(event, mx + 10.0f, chkY3, "Формат времени эффектов", this.effectTimeFormat.c().booleanValue(), anim, primary);

        // Sliders: Размер & Прозрачность
        float sliderY = chkY3 + 17.5f;
        drawSlider(event, mx + 10.0f, sliderY, mw - 20.0f, "Размер", String.format("%.2fx", getScale()), (getScale() - 0.5f) / 1.0f, anim, primary);
        drawSlider(event, mx + 10.0f, sliderY + 14.5f, mw - 20.0f, "Прозрачность", String.format("%d%%", (int)(getBgOpacity() * 100)), getBgOpacity(), anim, primary);
    }

    private void drawStyleCard(DrawEvent event, float x, float y, float w, float h, String title, String sub, String modeTarget, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        boolean active = this.mode.l(modeTarget);

        int bg = ColorUtil.a(24, 25, 33, (int) (220 * anim));
        int border = active ? ColorUtil.a(primary, 0.95f * anim) : ColorUtil.a(255, 255, 255, (int) (18 * anim));

        event.d().a(matrices, x, y, w, h, 4.0f, bg);
        event.d().a(matrices, x, y, w, h, 4.0f, active ? 0.9f : 0.5f, border);

        float tagW = Fonts.e.a("v2.0", 5.0f) + 4.0f;
        float tagX = (x + w) - 4.0f - tagW;
        event.d().a(matrices, tagX, y + 3.0f, tagW, 6.0f, 2.0f, ColorUtil.a(primary, 0.25f * anim));
        Fonts.e.a(event.h(), "v2.0", tagX + 2.0f, y + 3.5f, 4.75f, ColorUtil.a(primary, anim));

        float thumbW = w - 6.0f;
        float thumbH = 17.0f;
        float thumbX = x + 3.0f;
        float thumbY = y + 3.0f;
        event.d().a(matrices, thumbX, thumbY, thumbW, thumbH, 3.0f, ColorUtil.a(12, 13, 18, (int) (210 * anim)));

        if ("Встроенный".equals(modeTarget)) {
            // Mini integrated preview: hotbar outline + arc / enemy text
            float midX = thumbX + (thumbW / 2.0f);
            event.d().a(matrices, midX - 10.0f, thumbY + 3.0f, 20.0f, 2.0f, 0.5f, ColorUtil.a(255, 95, 115, (int) (220 * anim)));
            event.d().a(matrices, midX - 14.0f, thumbY + 6.5f, 28.0f, 3.0f, 0.5f, ColorUtil.a(-1, (int) (220 * anim)));
            event.d().a(matrices, midX - 16.0f, thumbY + 11.5f, 32.0f, 1.5f, 0.5f, ColorUtil.a(primary, (int) (200 * anim)));
        } else {
            // Mini standard card preview
            event.d().a(matrices, thumbX + 2.0f, thumbY + 3.0f, 10.0f, 10.0f, 1.5f, ColorUtil.a(primary, (int) (200 * anim)));
            event.d().a(matrices, thumbX + 14.0f, thumbY + 4.0f, 18.0f, 2.5f, 0.5f, ColorUtil.a(-1, (int) (200 * anim)));
            event.d().a(matrices, thumbX + 14.0f, thumbY + 8.5f, 22.0f, 2.5f, 0.5f, ColorUtil.a(120, 255, 140, (int) (200 * anim)));
        }

        Fonts.e.a(event.h(), title, x + 4.0f, y + 24.0f, 5.75f, ColorUtil.a(active ? primary : -1, anim));
        Fonts.e.a(event.h(), sub, x + 4.0f, y + 33.0f, 4.5f, ColorUtil.a(ColorUtil.a(160, 165, 180, 255), 0.75f * anim));
    }

    private void drawCheckbox(DrawEvent event, float x, float y, String label, boolean checked, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        float boxSize = 7.5f;

        int boxBg = checked ? ColorUtil.a(primary, anim) : ColorUtil.a(30, 32, 42, (int) (220 * anim));
        int boxBorder = checked ? ColorUtil.a(primary, anim) : ColorUtil.a(255, 255, 255, (int) (35 * anim));

        event.d().a(matrices, x, y, boxSize, boxSize, 2.0f, boxBg);
        event.d().a(matrices, x, y, boxSize, boxSize, 2.0f, 0.5f, boxBorder);

        if (checked) {
            Fonts.a.a(event.h(), "W", x + 1.25f, y + 1.25f, 5.0f, ColorUtil.a(-1, anim));
        }

        Fonts.e.a(event.h(), label, x + boxSize + 4.0f, y + 0.5f, 5.25f, ColorUtil.a(-1, anim));
    }

    private void drawSlider(DrawEvent event, float x, float y, float w, String label, String valueText, float percent, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        float trackH = 2.5f;
        float trackY = y + 8.5f;

        Fonts.e.a(event.h(), label, x, y, 5.25f, ColorUtil.a(-1, anim));
        float valW = Fonts.e.a(valueText, 5.25f);
        Fonts.e.a(event.h(), valueText, (x + w) - valW, y, 5.25f, ColorUtil.a(primary, anim));

        event.d().a(matrices, x, trackY, w, trackH, 1.25f, ColorUtil.a(30, 32, 42, (int) (230 * anim)));
        event.d().a(matrices, x, trackY, w * percent, trackH, 1.25f, ColorUtil.a(primary, anim));

        float thumbX = x + (w * percent);
        event.d().a(matrices, thumbX - 2.5f, trackY - 1.25f, 5.0f, 5.0f, 2.5f, ColorUtil.a(-1, anim));
    }

    @Override
    public boolean handleMouse(double mouseX, double mouseY, int button, int action) {
        if (!g() || this.c.c() <= 0.05f) {
            return false;
        }

        float mx = this.modalX;
        float my = this.modalY;
        float mw = this.modalW;
        float mh = this.modalH;

        boolean inside = MathUtil.a(mouseX, mouseY, mx, my, mw, mh);
        if (!inside && action == 0) {
            return false;
        }

        float cardW = (mw - 24.0f) / 2.0f;
        float cardH = 44.0f;
        float startCardY = my + 27.0f;

        if (action == 0 && button == 0) {
            // Close button
            float btnSize = 13.0f;
            float btnX = (mx + mw) - 10.0f - btnSize;
            float btnY = my + 8.0f;
            if (MathUtil.a(mouseX, mouseY, btnX, btnY, btnSize, btnSize)) {
                a(false);
                return true;
            }

            // Style Card 1 (Стандарт / Стиль виджета)
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, startCardY, cardW, cardH)) {
                this.mode.a("Стандарт");
                return true;
            }

            // Style Card 2 (Встроенный стиль)
            if (MathUtil.a(mouseX, mouseY, mx + 14.0f + cardW, startCardY, cardW, cardH)) {
                this.mode.a("Встроенный");
                return true;
            }

            // Checkboxes
            float col2X = mx + (mw / 2.0f) + 4.0f;
            float chkW = (mw / 2.0f) - 10.0f;
            float secY = startCardY + cardH + 6.0f;
            float chkY1 = secY + 8.5f;

            // Прозрачный стиль
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY1, chkW, 9.0f)) {
                this.transparentStyle.a(!this.transparentStyle.c().booleanValue());
                return true;
            }
            // Показывать при наводке
            if (MathUtil.a(mouseX, mouseY, col2X, chkY1, chkW, 9.0f)) {
                this.showOnHover.a(!this.showOnHover.c().booleanValue());
                return true;
            }

            float chkY2 = chkY1 + 11.0f;
            // Эффекты цели
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY2, chkW, 9.0f)) {
                this.showTargetEffects.a(!this.showTargetEffects.c().booleanValue());
                return true;
            }
            // Откаты цели
            if (MathUtil.a(mouseX, mouseY, col2X, chkY2, chkW, 9.0f)) {
                this.showTargetCooldowns.a(!this.showTargetCooldowns.c().booleanValue());
                return true;
            }

            float chkY3 = chkY2 + 11.0f;
            // Формат времени эффектов
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY3, chkW, 9.0f)) {
                this.effectTimeFormat.a(!this.effectTimeFormat.c().booleanValue());
                return true;
            }

            // Sliders
            float sliderY = chkY3 + 17.5f;
            float sliderW = mw - 20.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, sliderY + 4.0f, sliderW, 10.0f)) {
                this.draggingScaleSlider = true;
                updateScaleFromMouse(mouseX, mx + 10.0f, sliderW);
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, sliderY + 18.5f, sliderW, 10.0f)) {
                this.draggingOpacitySlider = true;
                updateOpacityFromMouse(mouseX, mx + 10.0f, sliderW);
                return true;
            }
        }

        if (action == 1) {
            float secY = startCardY + cardH + 6.0f;
            float sliderY = secY + 8.5f + 22.0f + 17.5f;
            float sliderW = mw - 20.0f;
            if (this.draggingScaleSlider) {
                updateScaleFromMouse(mouseX, mx + 10.0f, sliderW);
                return true;
            }
            if (this.draggingOpacitySlider) {
                updateOpacityFromMouse(mouseX, mx + 10.0f, sliderW);
                return true;
            }
        }

        if (action == 2) {
            this.draggingScaleSlider = false;
            this.draggingOpacitySlider = false;
        }

        return inside;
    }

    private void updateScaleFromMouse(double mouseX, float trackX, float trackW) {
        float pct = MathUtil.b((float) ((mouseX - trackX) / trackW), 0.0f, 1.0f);
        float val = Math.round((0.5f + (pct * 1.0f)) * 20.0f) / 20.0f;
        this.widgetScale.a(Float.valueOf(val));
    }

    private void updateOpacityFromMouse(double mouseX, float trackX, float trackW) {
        float pct = MathUtil.b((float) ((mouseX - trackX) / trackW), 0.0f, 1.0f);
        float val = Math.round(pct * 20.0f) / 20.0f;
        this.widgetBgOpacity.a(Float.valueOf(val));
    }

    private String getTargetName(LivingEntity target) {
        StreamerMode streamer = Delta.h().d().t().aE();
        if (streamer != null && streamer.m() && streamer.r().c().booleanValue()) {
            return streamer.a(target.getName().getString());
        }
        return target.getName().getString();
    }

    private List<StatusEffectInstance> getTargetEffects(LivingEntity target, boolean isPreview) {
        List<StatusEffectInstance> list = new ArrayList<>();
        if (!isPreview && target instanceof PlayerEntity player) {
            list.addAll(player.getStatusEffects());
        }
        if (list.isEmpty() || isPreview) {
            list.add(new StatusEffectInstance(StatusEffects.POISON, 580, 2));
            list.add(new StatusEffectInstance(StatusEffects.SPEED, 260, 1));
            list.add(new StatusEffectInstance(StatusEffects.REGENERATION, 1520, 1));
            list.add(new StatusEffectInstance(StatusEffects.STRENGTH, 20, 0));
        }
        return list;
    }

    private LivingEntity getActiveTarget() {
        LivingEntity target = null;
        if (Delta.h().d().t().B() != null && Delta.h().d().t().B().s() != null) {
            target = Delta.h().d().t().B().s();
        } else if (Delta.h().d().t().X() != null && Delta.h().d().t().X().s() != null) {
            target = Delta.h().d().t().X().s();
        }

        if (target == null && this.showOnHover.c().booleanValue()) {
            if (Interface.aM_.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
                if (living != Interface.aM_.player) {
                    target = living;
                }
            }
        }

        if (target == null && (Interface.aM_.currentScreen instanceof ChatScreen)) {
            target = Interface.aM_.player;
        }

        return target;
    }

    @Override
    public void a(GlobalEvent event) {
        LivingEntity target = getActiveTarget();
        boolean visible = target != null;
        if (target != null) {
            this.currentTarget = target;
        }
        d().a(visible);
        if (!visible && d().a() <= 0.0f) {
            this.currentTarget = null;
        }
        super.a(event);
    }
}
