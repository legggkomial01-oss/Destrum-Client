package aethereal.ui.widget;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.MathUtil;

import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;
import aethereal.core.GlobalEvent;
import aethereal.event.DrawEvent;
import aethereal.mixin.IStatusEffectInstance;
import aethereal.notification.Notification;
import aethereal.ui.element.DragInfo;
import aethereal.ui.widget.Widget;

import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

public class PotionWidget extends Widget implements Interface {
    private static PotionWidget INSTANCE;

    private final ModeSetting mode;
    private final BooleanSetting transparentStyle;
    private final ColorSetting harmfulColor;
    private final ColorSetting expiringColor;
    private final ModeSetting timeFormat;
    private final ModeSetting nameOverflow;
    private final ModeSetting gridColumns;

    private final StatusEffectInstance sampleSpeed;
    private List<StatusEffectInstance> mockEffects;

    private float modalX = Float.NaN;
    private float modalY = Float.NaN;
    private final float modalW = 215.0f;
    private final float modalH = 288.0f;

    private boolean draggingScaleSlider = false;
    private boolean draggingOpacitySlider = false;

    public static PotionWidget getInstance() {
        return INSTANCE;
    }

    public PotionWidget() {
        super(new DragInfo("Зелья", 0.0f, 0.0f, 0.0f, 0.0f));
        INSTANCE = this;

        this.mode = new ModeSetting("Режим", "Карточки", "Карточки", "Инлайн", "Бафф", "Стандарт");
        this.transparentStyle = new BooleanSetting("Прозрачный стиль", true);
        this.harmfulColor = new ColorSetting("Цвет негативных", ColorUtil.a(255, 76, 79, 255));
        this.expiringColor = new ColorSetting("Цвет заканчивающихся", ColorUtil.a(227, 186, 99, 255));
        this.timeFormat = new ModeSetting("Формат времени", "00:10", "00:10", "2m 10s");
        this.nameOverflow = new ModeSetting("Длинные названия", "Обрезать", "Обрезать", "Расширять виджет");
        this.gridColumns = new ModeSetting("Количество колонок", "2 колонки", "2 колонки", "3 колонки");

        this.sampleSpeed = new StatusEffectInstance(StatusEffects.SPEED, 6360, 0);

        j().a(this);
        j().a(0);
        a(this.mode, this.transparentStyle, this.harmfulColor, this.expiringColor, this.timeFormat, this.nameOverflow, this.gridColumns);
    }

    @Override
    public void a(DrawEvent event) {
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        for (StatusEffectInstance effect : k()) {
            if (effect instanceof IStatusEffectInstance instance) {
                instance.getAnimation().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
            }
        }

        if (this.mode.l("Бафф")) {
            renderFloatingPills(event);
        } else if (this.mode.l("Инлайн")) {
            renderInlined(event);
        } else if (this.mode.l("Стандарт")) {
            renderStandardList(event);
        } else {
            renderCardGrid(event);
        }

        j().a(0);
    }

    /**
     * Mode 1: Card Grid Style
     * Matches image 1 (card 1) & image 2:
     * Header with "Effects" and sparkle icon, 2 or 3 columns of rounded cards,
     * watermark background icon, sprite + circular progress ring + timer in top row,
     * and effect name in bottom row.
     */
    private void renderCardGrid(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        float anim = a();
        float bgFactor = anim * getBgOpacity();

        List<StatusEffectInstance> effects = k();
        if (effects.isEmpty() && !(aM_.currentScreen instanceof ChatScreen)) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        int cols = this.gridColumns.l("3 колонки") ? 3 : 2;
        float gap = 3.5f;
        float cardH = 27.0f;
        float headerH = 14.5f;

        float maxNameWidth = 0.0f;
        for (StatusEffectInstance inst : effects) {
            String name = getEffectDisplayName(inst);
            maxNameWidth = Math.max(maxNameWidth, Fonts.e.a(name, 5.75f));
        }

        float cardW;
        if (this.nameOverflow.l("Расширять виджет")) {
            cardW = Math.max(cols == 3 ? 58.0f : 68.0f, maxNameWidth + 10.0f);
        } else {
            cardW = cols == 3 ? 58.0f : 68.0f;
        }

        int count = Math.max(1, effects.size());
        int rows = (int) Math.ceil((double) count / cols);

        float gridW = (cols * cardW) + ((cols - 1) * gap);
        float totalW = gridW + 8.0f;
        float totalH = headerH + 3.0f + (rows * cardH) + Math.max(0, rows - 1) * gap + 4.0f;

        j().c(totalW);
        j().d(totalH);

        int bgAlpha = (int) (bgFactor * (this.transparentStyle.c().booleanValue() ? 150 : 235));
        int outerBg = ColorUtil.a(16, 17, 23, bgAlpha);
        int outerOutline = ColorUtil.a(255, 255, 255, (int) (bgFactor * 20));

        event.d().a(matrices, x, y, totalW, totalH, 5.5f, outerBg);
        event.d().a(matrices, x, y, totalW, totalH, 5.5f, 0.5f, outerOutline);

        // Header with "E" potion icon
        Fonts.a.a(matrices, "E", x + 5.5f, y + 4.0f, 7.0f, ColorUtil.a(primary, anim));
        float titleX = x + 5.5f + Fonts.a.a("E", 7.0f) + 3.5f;
        Fonts.e.a(event.h(), "Effects", titleX, y + 4.0f, 6.75f, ColorUtil.a(-1, anim));

        float sparkleSize = 10.0f;
        float sparkleX = (x + totalW) - 5.5f - sparkleSize;
        float sparkleY = y + 2.5f;
        event.d().a(matrices, sparkleX, sparkleY, sparkleSize, sparkleSize, 2.5f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.e.a(event.h(), "✦", sparkleX + 1.75f, sparkleY + 1.75f, 5.75f, ColorUtil.a(primary, anim));

        // Cards grid
        float startGridX = x + 4.0f;
        float startGridY = y + headerH + 1.5f;

        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance effect = effects.get(i);
            int row = i / cols;
            int col = i % cols;
            float cx = startGridX + (col * (cardW + gap));
            float cy = startGridY + (row * (cardH + gap));

            int cardAlpha = (int) (bgFactor * (this.transparentStyle.c().booleanValue() ? 175 : 242));
            int cardBg = ColorUtil.a(25, 26, 35, cardAlpha);
            int cardOutline = ColorUtil.a(255, 255, 255, (int) (bgFactor * 16));

            event.d().a(matrices, cx, cy, cardW, cardH, 4.0f, cardBg);
            event.d().a(matrices, cx, cy, cardW, cardH, 4.0f, 0.5f, cardOutline);

            Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(effect.getEffectType());

            // Watermark behind content (scales with bgFactor)
            if (sprite != null) {
                event.e().a(event.i(), sprite, cx + cardW - 20.0f, cy + cardH - 20.0f, 0.0f, 1.05f, 0.09f * bgFactor);
            }

            // Top row: Sprite icon on left
            if (sprite != null) {
                event.e().a(event.i(), sprite, cx + 3.5f, cy + 3.0f, 0.0f, 0.52f, anim);
            }

            // Top row: Progress ring and timer on right
            int seconds = effect.getDuration() / 20;
            boolean expiring = seconds <= 15;
            boolean harmful = ((StatusEffect) effect.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;

            int ringColor = expiring ? this.expiringColor.c().intValue()
                    : (harmful ? this.harmfulColor.c().intValue() : primary);

            String timeStr = formatDuration(seconds);
            float timeW = Fonts.e.a(timeStr, 5.5f);
            float ringSize = 6.5f;
            float timeX = (cx + cardW) - timeW - 3.5f;
            float ringX = timeX - ringSize - 2.5f;
            float ringY = cy + 4.0f;

            // Circular ring outline
            event.d().a(matrices, ringX, ringY, ringSize, ringSize, ringSize / 2.0f, 0.7f, ColorUtil.a(ringColor, 0.28f * anim));
            event.d().a(matrices, ringX, ringY, ringSize, ringSize, ringSize / 2.0f, 0.85f, ColorUtil.a(ringColor, anim));

            // Timer text
            int timeTextColor = expiring ? this.expiringColor.c().intValue() : ColorUtil.a(-1, 0.85f * anim);
            Fonts.e.a(event.h(), timeStr, timeX, cy + 4.25f, 5.5f, timeTextColor);

            // Bottom row: Effect name + level
            String fullText = getEffectDisplayName(effect);
            String displayName = this.nameOverflow.l("Обрезать") ? truncateText(fullText, 5.75f, cardW - 7.0f) : fullText;
            int nameColor = harmful ? this.harmfulColor.c().intValue() : -1;
            Fonts.e.a(event.h(), displayName, cx + 4.0f, cy + 15.5f, 5.75f, ColorUtil.a(nameColor, anim));
        }

        super.a(event);
    }

    /**
     * Mode 2: Inlined Style
     * Matches image 1 (card 2):
     * Header with "E" + "Effects" and sparkle icon, vertical rows with sprite,
     * effect name, and circular ring + timer on right.
     */
    private void renderInlined(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        float anim = a();
        float bgFactor = anim * getBgOpacity();

        List<StatusEffectInstance> effects = k();
        if (effects.isEmpty() && !(aM_.currentScreen instanceof ChatScreen)) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        float headerH = 14.5f;
        float rowH = 13.0f;
        float rowGap = 2.0f;

        float maxRowW = 110.0f;
        for (StatusEffectInstance inst : effects) {
            String name = getEffectDisplayName(inst);
            String time = formatDuration(inst.getDuration() / 20);
            float w = 18.0f + Fonts.e.a(name, 6.25f) + 12.0f + 6.5f + 3.0f + Fonts.e.a(time, 5.75f) + 8.0f;
            maxRowW = Math.max(maxRowW, w);
        }

        int count = Math.max(1, effects.size());
        float totalW = maxRowW;
        float totalH = headerH + 3.0f + (count * rowH) + Math.max(0, count - 1) * rowGap + 4.0f;

        j().c(totalW);
        j().d(totalH);

        int bgAlpha = (int) (bgFactor * (this.transparentStyle.c().booleanValue() ? 150 : 235));
        int outerBg = ColorUtil.a(16, 17, 23, bgAlpha);
        int outerOutline = ColorUtil.a(255, 255, 255, (int) (bgFactor * 20));

        event.d().a(matrices, x, y, totalW, totalH, 5.5f, outerBg);
        event.d().a(matrices, x, y, totalW, totalH, 5.5f, 0.5f, outerOutline);

        // Header with "E" potion icon
        Fonts.a.a(matrices, "E", x + 5.5f, y + 4.0f, 7.0f, ColorUtil.a(primary, anim));
        float titleX = x + 5.5f + Fonts.a.a("E", 7.0f) + 3.5f;
        Fonts.e.a(event.h(), "Effects", titleX, y + 4.0f, 6.75f, ColorUtil.a(-1, anim));

        float sparkleSize = 10.0f;
        float sparkleX = (x + totalW) - 5.5f - sparkleSize;
        float sparkleY = y + 2.5f;
        event.d().a(matrices, sparkleX, sparkleY, sparkleSize, sparkleSize, 2.5f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.e.a(event.h(), "✦", sparkleX + 1.75f, sparkleY + 1.75f, 5.75f, ColorUtil.a(primary, anim));

        float curY = y + headerH + 2.0f;
        for (StatusEffectInstance effect : effects) {
            float rowX = x + 3.5f;
            float rowW = totalW - 7.0f;

            int rowAlpha = (int) (bgFactor * (this.transparentStyle.c().booleanValue() ? 120 : 210));
            event.d().a(matrices, rowX, curY, rowW, rowH, 3.0f, ColorUtil.a(25, 26, 35, rowAlpha));

            Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            if (sprite != null) {
                event.e().a(event.i(), sprite, rowX + 3.0f, curY + 2.0f, 0.0f, 0.48f, anim);
            }

            int seconds = effect.getDuration() / 20;
            boolean expiring = seconds <= 15;
            boolean harmful = ((StatusEffect) effect.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;

            int ringColor = expiring ? this.expiringColor.c().intValue()
                    : (harmful ? this.harmfulColor.c().intValue() : primary);

            String name = getEffectDisplayName(effect);
            int nameColor = harmful ? this.harmfulColor.c().intValue() : -1;
            Fonts.e.a(event.h(), name, rowX + 16.0f, curY + 3.25f, 6.0f, ColorUtil.a(nameColor, anim));

            String timeStr = formatDuration(seconds);
            float timeW = Fonts.e.a(timeStr, 5.5f);
            float ringSize = 6.0f;
            float timeX = (rowX + rowW) - timeW - 3.5f;
            float ringX = timeX - ringSize - 2.5f;
            float ringY = curY + 3.5f;

            event.d().a(matrices, ringX, ringY, ringSize, ringSize, ringSize / 2.0f, 0.7f, ColorUtil.a(ringColor, 0.28f * anim));
            event.d().a(matrices, ringX, ringY, ringSize, ringSize, ringSize / 2.0f, 0.85f, ColorUtil.a(ringColor, anim));

            int timeTextColor = expiring ? this.expiringColor.c().intValue() : ColorUtil.a(-1, 0.85f * anim);
            Fonts.e.a(event.h(), timeStr, timeX, curY + 3.5f, 5.5f, timeTextColor);

            curY += rowH + rowGap;
        }

        super.a(event);
    }

    /**
     * Mode 3: "Бафф" (Compact Floating Pills)
     * Matches image 3:
     * Standalone rounded pill for each effect:
     * Left: effect icon. Right: Line 1 name, Line 2 duration.
     */
    private void renderFloatingPills(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        float anim = a();
        float bgFactor = anim * getBgOpacity();

        List<StatusEffectInstance> effects = k();
        if (effects.isEmpty() && !(aM_.currentScreen instanceof ChatScreen)) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        float pillH = 22.5f;
        float pillGap = 3.5f;
        float maxW = 0.0f;

        float curY = y;
        for (StatusEffectInstance effect : effects) {
            String name = getEffectDisplayName(effect);
            int seconds = effect.getDuration() / 20;
            String timeStr = formatDuration(seconds);

            float textW = Math.max(Fonts.e.a(name, 6.5f), Fonts.e.a(timeStr, 5.75f));
            float pillW = 20.0f + textW + 8.0f;
            maxW = Math.max(maxW, pillW);

            int bgAlpha = (int) (bgFactor * (this.transparentStyle.c().booleanValue() ? 150 : 240));
            int pillBg = ColorUtil.a(18, 19, 26, bgAlpha);
            int pillOutline = ColorUtil.a(255, 255, 255, (int) (bgFactor * 20));

            event.d().a(matrices, x, curY, pillW, pillH, 4.5f, pillBg);
            event.d().a(matrices, x, curY, pillW, pillH, 4.5f, 0.5f, pillOutline);

            Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            if (sprite != null) {
                event.e().a(event.i(), sprite, x + 4.5f, curY + 3.75f, 0.0f, 0.68f, anim);
            }

            boolean harmful = ((StatusEffect) effect.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;
            boolean expiring = seconds <= 15;

            int nameColor = harmful ? this.harmfulColor.c().intValue() : -1;
            Fonts.e.a(event.h(), name, x + 20.5f, curY + 3.5f, 6.5f, ColorUtil.a(nameColor, anim));

            int timeColor = expiring ? this.expiringColor.c().intValue() : ColorUtil.a(175, 180, 195, (int) (210 * anim));
            Fonts.e.a(event.h(), timeStr, x + 20.5f, curY + 12.25f, 5.75f, timeColor);

            curY += pillH + pillGap;
        }

        j().c(maxW);
        j().d(Math.max(pillH, (curY - y) - pillGap));

        super.a(event);
    }

    /**
     * Mode 4: "Стандарт" (Classic Destrum Potion-list)
     * Matches the original client style:
     * Header with "E" and "Effects",
     * list of effect rows with sprite, name, duration and divider.
     */
    private void renderStandardList(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        float anim = a();
        float targetWidth = 14.5f + Fonts.e.a("Effects", this.e) + 5.0f + 2.0f;
        float contentY = y + this.d + 3.0f;

        List<StatusEffectInstance> effects = k();
        if (effects.isEmpty() && !(aM_.currentScreen instanceof ChatScreen)) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        boolean active = false;
        for (StatusEffectInstance inst : effects) {
            String name = getEffectDisplayName(inst);
            int seconds = inst.getDuration() / 20;
            String duration = formatDuration(seconds);
            targetWidth = Math.max(targetWidth, 19.0f + Fonts.e.a(name, 6.5f) + 8.0f + Fonts.e.a(duration, 6.5f) + 5.0f + 2.0f);
            active = true;
        }

        float width = MathUtil.c(j().getRawWidth(), targetWidth, 0.5f);
        j().c(width);

        if (anim > 0.0f) {
            a(event, "E", "Effects", width, anim);
        }

        for (StatusEffectInstance inst : effects) {
            String name = getEffectDisplayName(inst);
            int seconds = inst.getDuration() / 20;
            String duration = formatDuration(seconds);
            float durationWidth = Fonts.e.a(duration, 6.5f);
            float textY = (contentY + ((11.5f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;

            // Row plate using Widget's standard drawer (which respects getBgOpacity)
            a(event, x, contentY, width, 11.5f, false, anim);
            a(event, x + 15.0f, contentY, 11.5f, anim);

            Sprite sprite = aM_.getStatusEffectSpriteManager().getSprite(inst.getEffectType());
            if (sprite != null) {
                event.e().a(event.i(), sprite, x + 5.0f, contentY + 2.0f, 0.0f, 0.4f, anim);
            }

            boolean harmful = ((StatusEffect) inst.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;
            int nameColor = harmful ? this.harmfulColor.c().intValue() : -1;
            Fonts.e.a(event.h(), name, x + 19.0f, textY, 6.5f, ColorUtil.a(nameColor, anim));
            Fonts.e.a(event.h(), duration, (((x + width) - 5.0f) - durationWidth) - 1.0f, textY, 6.5f, ColorUtil.a(-1, 0.75f * anim));

            contentY += 13.5f;
        }

        j().d(active ? (contentY - y) - 2.0f : this.d);
        super.a(event);
    }

    /**
     * Settings Window
     * Matches image 1 ("Настройки виджета Effects")
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
        Fonts.e.a(event.h(), "Настройки виджета Effects", mx + 10.0f, my + 8.0f, 7.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Настройте стиль виджета по своему вкусу", mx + 10.0f, my + 17.5f, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), anim));

        float btnSize = 13.0f;
        float btnX = (mx + mw) - 10.0f - btnSize;
        float btnY = my + 8.0f;
        event.d().a(matrices, btnX, btnY, btnSize, btnSize, 3.5f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.a.a(matrices, "J", btnX + 3.0f, btnY + 2.5f, 7.0f, ColorUtil.a(primary, anim));

        // Style cards (2x2 grid of 4 cards)
        float cardW = (mw - 24.0f) / 2.0f;
        float cardH = 36.0f;
        float startCardY = my + 27.0f;

        drawStyleCard(event, mx + 10.0f, startCardY, cardW, cardH, "Card Grid Style", "Карточный дизайн.", "Карточки", anim, primary);
        drawStyleCard(event, mx + 14.0f + cardW, startCardY, cardW, cardH, "Inlined Style", "Компактный список.", "Инлайн", anim, primary);

        float row2Y = startCardY + cardH + 4.0f;
        drawStyleCard(event, mx + 10.0f, row2Y, cardW, cardH, "Бафф (Compact)", "Отдельные плашки.", "Бафф", anim, primary);
        drawStyleCard(event, mx + 14.0f + cardW, row2Y, cardW, cardH, "Стандарт (Classic)", "Классический список.", "Стандарт", anim, primary);

        // SECTION: НАСТРОЙКИ ВИДЖЕТА
        float secY = row2Y + cardH + 5.0f;
        Fonts.e.a(event.h(), "НАСТРОЙКИ ВИДЖЕТА", mx + 10.0f, secY, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        // Checkbox: Прозрачный стиль
        float chkY = secY + 8.5f;
        drawCheckbox(event, mx + 10.0f, chkY, "Прозрачный стиль", this.transparentStyle.c().booleanValue(), anim, primary);

        // Colors row: Цвет негативных & Цвет заканчивающихся
        float colorsY = chkY + 12.5f;
        float halfW = (mw - 24.0f) / 2.0f;

        drawColorBox(event, mx + 10.0f, colorsY, halfW, "Цвет негативных", this.harmfulColor.c().intValue(),
                new int[]{ColorUtil.a(255, 76, 79, 255), ColorUtil.a(224, 53, 56, 255), ColorUtil.a(255, 107, 107, 255)},
                "#FF4C4F", anim, primary);

        drawColorBox(event, mx + 14.0f + halfW, colorsY, halfW, "Цвет заканчивающихся", this.expiringColor.c().intValue(),
                new int[]{ColorUtil.a(227, 186, 99, 255), ColorUtil.a(255, 184, 0, 255), ColorUtil.a(255, 160, 64, 255)},
                "#E3BA63", anim, primary);

        // Format time
        float timeSecY = colorsY + 28.5f;
        Fonts.e.a(event.h(), "Формат времени", mx + 10.0f, timeSecY, 5.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Формат остатка времени рядом с кругом прогресса", mx + 10.0f, timeSecY + 7.5f, 4.75f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        float timeBtnY = timeSecY + 15.0f;
        drawSegmented(event, mx + 10.0f, timeBtnY, mw - 20.0f, 13.0f,
                new String[]{"00:10", "2m 10s"}, this.timeFormat.c(), anim, primary);

        // Overflow
        float flowSecY = timeBtnY + 17.5f;
        Fonts.e.a(event.h(), "Длинные названия", mx + 10.0f, flowSecY, 5.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Что делать с названием эффекта, когда оно не помещается", mx + 10.0f, flowSecY + 7.5f, 4.75f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        float flowBtnY = flowSecY + 15.0f;
        drawSegmented(event, mx + 10.0f, flowBtnY, mw - 20.0f, 13.0f,
                new String[]{"Обрезать", "Расширять виджет"}, this.nameOverflow.c(), anim, primary);

        // Columns
        float colSecY = flowBtnY + 17.5f;
        Fonts.e.a(event.h(), "Количество колонок", mx + 10.0f, colSecY, 5.5f, ColorUtil.a(-1, anim));

        float colBtnY = colSecY + 8.5f;
        drawSegmented(event, mx + 10.0f, colBtnY, mw - 20.0f, 13.0f,
                new String[]{"2 колонки", "3 колонки"}, this.gridColumns.c(), anim, primary);

        // Sliders: Размер & Прозрачность
        float sliderY = colBtnY + 17.5f;
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

        float thumbW = w - 6.0f;
        float thumbH = 15.0f;
        float thumbX = x + 3.0f;
        float thumbY = y + 3.0f;
        event.d().a(matrices, thumbX, thumbY, thumbW, thumbH, 2.5f, ColorUtil.a(12, 13, 18, (int) (210 * anim)));

        if ("Карточки".equals(modeTarget)) {
            // Mini 2x2 grid
            float cw = (thumbW - 3.0f) / 2.0f;
            float ch = 5.5f;
            event.d().a(matrices, thumbX + 1.0f, thumbY + 1.0f, cw, ch, 1.5f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f + cw, thumbY + 1.0f, cw, ch, 1.5f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 1.0f, thumbY + 7.5f, cw, ch, 1.5f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f + cw, thumbY + 7.5f, cw, ch, 1.5f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
        } else if ("Инлайн".equals(modeTarget)) {
            // Mini horizontal strips
            event.d().a(matrices, thumbX + 2.0f, thumbY + 2.0f, thumbW - 4.0f, 3.0f, 1.0f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f, thumbY + 6.0f, thumbW - 4.0f, 3.0f, 1.0f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f, thumbY + 10.0f, thumbW - 4.0f, 3.0f, 1.0f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
        } else if ("Бафф".equals(modeTarget)) {
            // Mini pills
            event.d().a(matrices, thumbX + 2.0f, thumbY + 2.0f, (thumbW - 6.0f) * 0.7f, 4.5f, 2.0f, ColorUtil.a(40, 42, 55, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f, thumbY + 8.0f, (thumbW - 6.0f) * 0.85f, 4.5f, 2.0f, ColorUtil.a(40, 42, 55, (int) (220 * anim)));
        } else {
            // Mini classic Destrum header + rows
            event.d().a(matrices, thumbX + 2.0f, thumbY + 1.5f, thumbW - 4.0f, 3.5f, 1.0f, ColorUtil.a(primary, (int) (180 * anim)));
            event.d().a(matrices, thumbX + 2.0f, thumbY + 6.0f, thumbW - 4.0f, 3.0f, 1.0f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
            event.d().a(matrices, thumbX + 2.0f, thumbY + 10.0f, thumbW - 4.0f, 3.0f, 1.0f, ColorUtil.a(35, 37, 48, (int) (220 * anim)));
        }

        Fonts.e.a(event.h(), title, x + 4.0f, y + 20.0f, 5.5f, ColorUtil.a(active ? primary : -1, anim));
        Fonts.e.a(event.h(), sub, x + 4.0f, y + 27.5f, 4.5f, ColorUtil.a(ColorUtil.a(160, 165, 180, 255), 0.75f * anim));
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

        Fonts.e.a(event.h(), label, x + boxSize + 4.0f, y + 0.5f, 5.5f, ColorUtil.a(-1, anim));
    }

    private void drawColorBox(DrawEvent event, float x, float y, float w, String title, int currentColor, int[] presets, String hexLabel, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        Fonts.e.a(event.h(), title, x, y, 5.25f, ColorUtil.a(-1, anim));

        float boxY = y + 7.5f;
        float boxH = 14.5f;

        event.d().a(matrices, x, boxY, w, boxH, 3.0f, ColorUtil.a(24, 25, 33, (int) (220 * anim)));
        event.d().a(matrices, x, boxY, w, boxH, 3.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (18 * anim)));

        // Dropper icon
        Fonts.a.a(matrices, "D", x + 3.5f, boxY + 3.0f, 6.5f, ColorUtil.a(currentColor, anim));

        // Preset dots
        float dotX = x + 16.0f;
        float dotSize = 7.0f;
        for (int pColor : presets) {
            boolean sel = (currentColor == pColor);
            event.d().a(matrices, dotX, boxY + 3.75f, dotSize, dotSize, dotSize / 2.0f, ColorUtil.a(pColor, anim));
            if (sel) {
                Fonts.a.a(event.h(), "W", dotX + 1.0f, boxY + 4.0f, 4.5f, ColorUtil.a(-1, anim));
            }
            dotX += dotSize + 3.5f;
        }

        // Hex text
        float hexW = Fonts.e.a(hexLabel, 5.0f);
        Fonts.e.a(event.h(), hexLabel, (x + w) - hexW - 4.0f, boxY + 4.25f, 5.0f, ColorUtil.a(themeColorSecondary(), 0.85f * anim));
    }

    private void drawSegmented(DrawEvent event, float x, float y, float w, float h, String[] options, String current, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        float btnW = (w - (options.length - 1) * 2.0f) / options.length;

        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            boolean sel = opt.equals(current);
            float bx = x + (i * (btnW + 2.0f));

            int bg = sel ? ColorUtil.a(primary, 0.25f * anim) : ColorUtil.a(24, 25, 33, (int) (220 * anim));
            int border = sel ? ColorUtil.a(primary, 0.9f * anim) : ColorUtil.a(255, 255, 255, (int) (18 * anim));

            event.d().a(matrices, bx, y, btnW, h, 3.0f, bg);
            event.d().a(matrices, bx, y, btnW, h, 3.0f, 0.5f, border);

            float tw = Fonts.e.a(opt, 5.5f);
            Fonts.e.a(event.h(), opt, bx + ((btnW - tw) / 2.0f), y + ((h - Fonts.e.a(5.5f)) / 2.0f), 5.5f, ColorUtil.a(sel ? primary : -1, anim));
        }
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
        float cardH = 36.0f;
        float startCardY = my + 27.0f;
        float row2Y = startCardY + cardH + 4.0f;
        float secY = row2Y + cardH + 5.0f;

        if (action == 0 && button == 0) {
            // Close button
            float btnSize = 13.0f;
            float btnX = (mx + mw) - 10.0f - btnSize;
            float btnY = my + 8.0f;
            if (MathUtil.a(mouseX, mouseY, btnX, btnY, btnSize, btnSize)) {
                a(false);
                return true;
            }

            // Style Card 1 (Card Grid)
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, startCardY, cardW, cardH)) {
                this.mode.a("Карточки");
                return true;
            }

            // Style Card 2 (Inlined)
            if (MathUtil.a(mouseX, mouseY, mx + 14.0f + cardW, startCardY, cardW, cardH)) {
                this.mode.a("Инлайн");
                return true;
            }

            // Style Card 3 (Бафф)
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, row2Y, cardW, cardH)) {
                this.mode.a("Бафф");
                return true;
            }

            // Style Card 4 (Стандарт)
            if (MathUtil.a(mouseX, mouseY, mx + 14.0f + cardW, row2Y, cardW, cardH)) {
                this.mode.a("Стандарт");
                return true;
            }

            // Checkbox: Прозрачный стиль
            float chkY = secY + 8.5f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY, mw - 20.0f, 9.0f)) {
                this.transparentStyle.a(!this.transparentStyle.c().booleanValue());
                return true;
            }

            // Colors presets
            float colorsY = chkY + 12.5f;
            float halfW = (mw - 24.0f) / 2.0f;

            // Harmful presets
            float dotX1 = mx + 10.0f + 16.0f;
            int[] harmfulPresets = {ColorUtil.a(255, 76, 79, 255), ColorUtil.a(224, 53, 56, 255), ColorUtil.a(255, 107, 107, 255)};
            for (int pColor : harmfulPresets) {
                if (MathUtil.a(mouseX, mouseY, dotX1, colorsY + 10.0f, 8.0f, 8.0f)) {
                    this.harmfulColor.a(Integer.valueOf(pColor));
                    return true;
                }
                dotX1 += 7.0f + 3.5f;
            }

            // Expiring presets
            float dotX2 = mx + 14.0f + halfW + 16.0f;
            int[] expiringPresets = {ColorUtil.a(227, 186, 99, 255), ColorUtil.a(255, 184, 0, 255), ColorUtil.a(255, 160, 64, 255)};
            for (int pColor : expiringPresets) {
                if (MathUtil.a(mouseX, mouseY, dotX2, colorsY + 10.0f, 8.0f, 8.0f)) {
                    this.expiringColor.a(Integer.valueOf(pColor));
                    return true;
                }
                dotX2 += 7.0f + 3.5f;
            }

            // Format time buttons
            float timeSecY = colorsY + 28.5f;
            float timeBtnY = timeSecY + 15.0f;
            float btnW = (mw - 22.0f) / 2.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, timeBtnY, btnW, 13.0f)) {
                this.timeFormat.a("00:10");
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 12.0f + btnW, timeBtnY, btnW, 13.0f)) {
                this.timeFormat.a("2m 10s");
                return true;
            }

            // Name overflow buttons
            float flowSecY = timeBtnY + 17.5f;
            float flowBtnY = flowSecY + 15.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, flowBtnY, btnW, 13.0f)) {
                this.nameOverflow.a("Обрезать");
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 12.0f + btnW, flowBtnY, btnW, 13.0f)) {
                this.nameOverflow.a("Расширять виджет");
                return true;
            }

            // Columns buttons
            float colSecY = flowBtnY + 17.5f;
            float colBtnY = colSecY + 8.5f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, colBtnY, btnW, 13.0f)) {
                this.gridColumns.a("2 колонки");
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 12.0f + btnW, colBtnY, btnW, 13.0f)) {
                this.gridColumns.a("3 колонки");
                return true;
            }

            // Sliders
            float sliderY = colBtnY + 17.5f;
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

    private int themeColorSecondary() {
        return Delta.h().d().o().a(ThemeInfo.TEXT_DISABLED).a();
    }

    private String getEffectDisplayName(StatusEffectInstance effect) {
        String baseName = Text.translatable(((StatusEffect) effect.getEffectType().value()).getTranslationKey()).getString();
        int amp = effect.getAmplifier();
        if (amp > 0) {
            return baseName + " " + (amp + 1);
        }
        return baseName;
    }

    private String truncateText(String text, float fontSize, float maxWidth) {
        if (Fonts.e.a(text, fontSize) <= maxWidth) {
            return text;
        }
        String ell = "...";
        float ellW = Fonts.e.a(ell, fontSize);
        if (ellW >= maxWidth) return "";
        int len = text.length();
        while (len > 0 && Fonts.e.a(text.substring(0, len) + ell, fontSize) > maxWidth) {
            len--;
        }
        return text.substring(0, len) + ell;
    }

    private String formatDuration(int totalSeconds) {
        if (totalSeconds > 50000) return "∞";
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        if (this.timeFormat.l("2m 10s")) {
            if (m > 0) {
                return m + "m " + s + "s";
            }
            return s + "s";
        }
        return m + ":" + String.format("%02d", s);
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = aM_.currentScreen instanceof ChatScreen;
        for (StatusEffectInstance effect : k()) {
            if (!effect.getEffectType().equals(StatusEffects.NIGHT_VISION)) {
                if (effect instanceof IStatusEffectInstance inst) {
                    inst.getAnimation().a((aM_.currentScreen instanceof ChatScreen) || effect.getDuration() > 20);
                    if (inst.getAnimation().c() > 0.0d) {
                        visible = true;
                    }
                }
                if (effect.getDuration() == 100 && (effect.getEffectType().equals(StatusEffects.STRENGTH)
                        || effect.getEffectType().equals(StatusEffects.SPEED)
                        || effect.getEffectType().equals(StatusEffects.HEALTH_BOOST)
                        || effect.getEffectType().equals(StatusEffects.INVISIBILITY))) {
                    Delta.h().d().m().a(new Notification("E", Text.literal("Эффект ").append(Text.translatable(((StatusEffect) effect.getEffectType().value()).getTranslationKey()).append(" " + (effect.getAmplifier() + 1)).styled(style -> {
                        return style.withColor(Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
                    })).append(Text.literal(" заканчивается")), 2500));
                }
            }
        }
        d().a(visible);
        super.a(event);
    }

    private List<StatusEffectInstance> k() {
        List<StatusEffectInstance> effects = new ArrayList<>();
        if (aM_.player != null) {
            effects.addAll((Collection<? extends StatusEffectInstance>) aM_.player.getStatusEffects());
        }

        boolean onlyNightVision = effects.stream().allMatch(e -> e.getEffectType().equals(StatusEffects.NIGHT_VISION));
        if ((effects.isEmpty() || onlyNightVision) && (aM_.currentScreen instanceof ChatScreen)) {
            return getSampleEffects();
        }

        effects.sort(Comparator.comparingInt(effect2 -> {
            if (effect2.getEffectType().equals(StatusEffects.STRENGTH)) {
                return 0;
            }
            return effect2.getEffectType().equals(StatusEffects.WEAKNESS) ? 1 : 2;
        }));
        return effects;
    }

    private List<StatusEffectInstance> getSampleEffects() {
        if (this.mockEffects == null) {
            this.mockEffects = new ArrayList<>();
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.RESISTANCE, 5840, 0));       // Сопротивление (4:52)
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 5840, 0));  // Огнестойкость (4:52)
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.REGENERATION, 240, 1));       // Регенерация 2 (0:12)
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.ABSORPTION, 2240, 3));       // Поглощение 4 (1:52)
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.STRENGTH, 3540, 0));         // Сила 1 (2:57)
            this.mockEffects.add(new StatusEffectInstance(StatusEffects.POISON, 580, 2));            // Отравление 3 (0:29)
        }
        return this.mockEffects;
    }
}
