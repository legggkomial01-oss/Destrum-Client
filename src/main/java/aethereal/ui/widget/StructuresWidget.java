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
import aethereal.module.player.Structures;
import aethereal.ui.element.DragInfo;
import aethereal.ui.widget.Widget;

import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class StructuresWidget extends Widget implements Interface {
    private static StructuresWidget INSTANCE;

    private static final Identifier INTERFACE_STYLE_TEXTURE = Identifier.of("delta", "pictures/structures/interface.png");
    private static final Identifier STANDARD_STYLE_TEXTURE = Identifier.of("delta", "pictures/structures/standard.png");

    private final ModeSetting mode;
    private final BooleanSetting transparentStyle;
    private final BooleanSetting roundedCorners;
    private final BooleanSetting showTimeText;
    private final ModeSetting timeFormat;

    private float modalX = Float.NaN;
    private float modalY = Float.NaN;
    private final float modalW = 210.0f;
    private final float modalH = 215.0f;

    private boolean draggingScaleSlider = false;
    private boolean draggingOpacitySlider = false;

    public static StructuresWidget getInstance() {
        return INSTANCE;
    }

    public static class StructureData {
        public final String displayName;
        public final Item item;
        public final float remaining;
        public final float progress;

        public StructureData(String displayName, Item item, float remaining, float progress) {
            this.displayName = displayName;
            this.item = item;
            this.remaining = remaining;
            this.progress = progress;
        }
    }

    public StructuresWidget() {
        super(new DragInfo("Структуры", 0.0f, 0.0f, 0.0f, 0.0f));
        INSTANCE = this;

        this.mode = new ModeSetting("Режим", "Интерфейс", "Интерфейс", "Стандарт");
        this.transparentStyle = new BooleanSetting("Прозрачный стиль", true);
        this.roundedCorners = new BooleanSetting("Закруглённые углы", true);
        this.showTimeText = new BooleanSetting("Формат отображения времени", true);
        this.timeFormat = new ModeSetting("Формат времени", "9.2c", "9.2c", "09:22");

        j().a(this);
        j().a(0);
        a(this.mode, this.transparentStyle, this.roundedCorners, this.showTimeText, this.timeFormat);
    }

    @Override
    public void a(DrawEvent event) {
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        if (this.mode.l("Стандарт")) {
            renderStandard(event);
        } else {
            renderCapsule(event);
        }

        j().a(0);
    }

    /**
     * Mode: "Интерфейс"
     * Matches image 2 & image 3:
     * Capsule pill with netherite scrap icon, "ТРАПКА ИСЧЕЗАЕТ",
     * and darker inner sub-pill with progress ring and timer.
     */
    private void renderCapsule(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        float anim = a();
        float bgFactor = anim * getBgOpacity();

        List<StructureData> list = getActiveStructures();
        if (list.isEmpty()) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        float h = 16.5f;
        float gap = 3.5f;
        float maxW = 0.0f;
        float curY = y;

        boolean rounded = this.roundedCorners.c().booleanValue();
        float radius = rounded ? (h / 2.0f) : 3.5f;

        for (StructureData data : list) {
            String label = data.displayName.toUpperCase() + " ИСЧЕЗАЕТ";
            float labelW = Fonts.d.a(label, 5.75f);

            float ringSize = 7.0f;
            String timeStr = formatTime(data.remaining);
            boolean showText = this.showTimeText.c().booleanValue();
            float timeW = showText ? Fonts.e.a(timeStr, 5.75f) : 0.0f;

            float innerPillW = 3.5f + ringSize + (showText ? 3.0f + timeW : 0.0f) + 3.5f;
            float innerPillH = 12.0f;
            float innerRadius = rounded ? (innerPillH / 2.0f) : 2.5f;

            float totalW = 4.5f + 10.0f + 4.0f + labelW + 6.0f + innerPillW + 3.5f;
            maxW = Math.max(maxW, totalW);

            // Capsule background (respects background opacity)
            a(event, x, curY, totalW, h, radius, true, anim);

            // Left icon (Netherite scrap item)
            ItemStack stack = data.item.getDefaultStack();
            event.e().a(event.i(), stack, x + 4.5f, curY + 3.25f, 0, anim, 0.58f, false);

            // Text "ТРАПКА ИСЧЕЗАЕТ"
            float labelY = curY + ((h - Fonts.d.a(5.75f)) / 2.0f) - 0.25f;
            Fonts.d.a(matrices, label, x + 17.5f, labelY, 5.75f, ColorUtil.a(-1, anim));

            // Right inner pill container
            float innerX = (x + totalW) - 3.5f - innerPillW;
            float innerY = curY + ((h - innerPillH) / 2.0f);
            a(event, innerX, innerY, innerPillW, innerPillH, innerRadius, false, anim);

            // Ring
            float rx = innerX + 3.5f;
            float ry = innerY + ((innerPillH - ringSize) / 2.0f);

            boolean expiring = data.remaining <= 4.0f;
            int ringColor = expiring ? ColorUtil.a(235, 175, 75, 255) : ColorUtil.a(140, 120, 255, 255);

            // Ring track & outline
            event.d().a(matrices, rx, ry, ringSize, ringSize, ringSize / 2.0f, 0.7f, ColorUtil.a(ringColor, 0.25f * anim));
            event.d().a(matrices, rx, ry, ringSize, ringSize, ringSize / 2.0f, 0.85f, ColorUtil.a(ringColor, anim));

            // Time text
            if (showText) {
                float tx = rx + ringSize + 3.0f;
                float ty = innerY + ((innerPillH - Fonts.e.a(5.75f)) / 2.0f) - 0.25f;
                Fonts.e.a(event.h(), timeStr, tx, ty, 5.75f, ColorUtil.a(-1, anim));
            }

            curY += h + gap;
        }

        j().c(maxW);
        j().d(Math.max(h, (curY - y) - gap));
        super.a(event);
    }

    /**
     * Mode: "Стандарт"
     * Matches image 1:
     * Header bar "Structures" and row list below.
     */
    private void renderStandard(DrawEvent event) {
        float x = j().a();
        float y = j().b();
        MatrixStack matrices = event.i().getMatrices();
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        float anim = a();
        float bgFactor = anim * getBgOpacity();

        List<StructureData> list = getActiveStructures();
        if (list.isEmpty()) {
            j().c(0.0f);
            j().d(0.0f);
            super.a(event);
            return;
        }

        float headerH = 14.5f;
        float rowH = 13.0f;
        float gap = 2.0f;

        float maxRowW = 14.5f + Fonts.a.a("U", 7.0f) + 3.5f + Fonts.e.a("Structures", 7.0f) + 12.0f;
        for (StructureData data : list) {
            String time = formatTime(data.remaining);
            float w = 18.0f + Fonts.e.a(data.displayName, 6.25f) + 12.0f + Fonts.e.a(time, 5.75f) + 8.0f;
            maxRowW = Math.max(maxRowW, w);
        }

        int count = list.size();
        float totalW = maxRowW;
        float totalH = headerH + (count * rowH) + Math.max(0, count - 1) * gap + 4.0f;

        j().c(totalW);
        j().d(totalH);

        boolean rounded = this.roundedCorners.c().booleanValue();
        float radius = rounded ? 5.5f : 3.5f;

        a(event, x, y, totalW, totalH, radius, true, anim);

        // Header: "U" icon + "Structures"
        Fonts.a.a(matrices, "U", x + 5.5f, y + 4.0f, 7.0f, ColorUtil.a(primary, anim));
        float titleX = x + 5.5f + Fonts.a.a("U", 7.0f) + 3.5f;
        Fonts.e.a(event.h(), "Structures", titleX, y + 4.0f, 6.75f, ColorUtil.a(-1, anim));

        float curY = y + headerH + 1.0f;
        for (StructureData data : list) {
            float rowX = x + 3.0f;
            float rowW = totalW - 6.0f;

            a(event, rowX, curY, rowW, rowH, 3.0f, false, anim);

            ItemStack stack = data.item.getDefaultStack();
            event.e().a(event.i(), stack, rowX + 2.5f, curY + 2.0f, 0, anim, 0.50f, false);

            Fonts.e.a(event.h(), data.displayName, rowX + 16.0f, curY + 3.25f, 6.0f, ColorUtil.a(-1, anim));

            String timeStr = formatTime(data.remaining);
            float timeW = Fonts.e.a(timeStr, 5.5f);
            float timeX = (rowX + rowW) - timeW - 4.0f;
            Fonts.e.a(event.h(), timeStr, timeX, curY + 3.5f, 5.5f, ColorUtil.a(180, 185, 200, (int) (210 * anim)));

            curY += rowH + gap;
        }

        super.a(event);
    }

    /**
     * Settings Window
     * Matches image 3 ("Настройки виджета Trap Timer")
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

        int modalBg = ColorUtil.a(11, 11, 22, (int) (245 * anim));
        int modalOutline = ColorUtil.a(primary, (int) (35 * anim));
        event.d().a(event.h(), mx, my, mw, mh, 8.0f, modalBg, anim, ColorUtil.a(primary, 0.12f * anim), 12.0f);
        event.d().a(matrices, mx, my, mw, mh, 8.0f, 0.5f, modalOutline);

        // Header with "U" trap structure icon
        Fonts.a.a(matrices, "U", mx + 10.0f, my + 8.5f, 7.5f, ColorUtil.a(primary, anim));
        float titleX = mx + 10.0f + Fonts.a.a("U", 7.5f) + 4.0f;
        Fonts.e.a(event.h(), "Настройки виджета Trap Timer", titleX, my + 8.0f, 7.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Настройте стиль виджета по своему вкусу", mx + 10.0f, my + 17.5f, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), anim));

        float btnSize = 13.0f;
        float btnX = (mx + mw) - 10.0f - btnSize;
        float btnY = my + 8.0f;
        event.d().a(matrices, btnX, btnY, btnSize, btnSize, 3.5f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.a.a(matrices, "J", btnX + 3.0f, btnY + 2.5f, 7.0f, ColorUtil.a(primary, anim));

        // Style Cards (Top section)
        float cardW = (mw - 24.0f) / 2.0f;
        float cardH = 42.0f;
        float startCardY = my + 27.0f;

        drawStyleCard(event, mx + 10.0f, startCardY, cardW, cardH, "Интерфейс", "Капсула ТРАПКА ИСЧЕЗАЕТ.", "Интерфейс", anim, primary);
        drawStyleCard(event, mx + 14.0f + cardW, startCardY, cardW, cardH, "Стандарт", "Классическая вкладка.", "Стандарт", anim, primary);

        // SECTION: НАСТРОЙКИ ВИДЖЕТА
        float secY = startCardY + cardH + 6.0f;
        Fonts.e.a(event.h(), "НАСТРОЙКИ ВИДЖЕТА", mx + 10.0f, secY, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        // Checkboxes: Прозрачный стиль, Закруглённые углы, Формат отображения времени
        float chkY1 = secY + 8.5f;
        drawCheckbox(event, mx + 10.0f, chkY1, "Прозрачный стиль", this.transparentStyle.c().booleanValue(), anim, primary);

        float chkY2 = chkY1 + 11.0f;
        drawCheckbox(event, mx + 10.0f, chkY2, "Закруглённые углы", this.roundedCorners.c().booleanValue(), anim, primary);

        float chkY3 = chkY2 + 11.0f;
        drawCheckbox(event, mx + 10.0f, chkY3, "Формат отображения времени", this.showTimeText.c().booleanValue(), anim, primary);

        // SECTION: Формат отображения времени
        float timeSecY = chkY3 + 14.0f;
        Fonts.e.a(event.h(), "Формат отображения времени", mx + 10.0f, timeSecY, 5.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Показывать время текстом рядом с кругом прогресса", mx + 10.0f, timeSecY + 7.5f, 4.75f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        float timeBtnY = timeSecY + 15.0f;
        drawSegmented(event, mx + 10.0f, timeBtnY, mw - 20.0f, 13.0f,
                new String[]{"9.2c", "09:22"}, this.timeFormat.c(), anim, primary);

        // Sliders: Размер & Прозрачность
        float sliderY = timeBtnY + 17.5f;
        drawSlider(event, mx + 10.0f, sliderY, mw - 20.0f, "Размер", String.format("%.2fx", getScale()), (getScale() - 0.5f) / 1.0f, anim, primary);
        drawSlider(event, mx + 10.0f, sliderY + 14.5f, mw - 20.0f, "Прозрачность", String.format("%d%%", (int)(getBgOpacity() * 100)), getBgOpacity(), anim, primary);
    }

    private void drawStyleCard(DrawEvent event, float x, float y, float w, float h, String title, String sub, String modeTarget, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        boolean active = this.mode.l(modeTarget);

        int bg = ColorUtil.a(11, 11, 22, (int) (220 * anim));
        int border = active ? ColorUtil.a(primary, 0.95f * anim) : ColorUtil.a(255, 255, 255, (int) (14 * anim));

        event.d().a(matrices, x, y, w, h, 4.0f, bg);
        event.d().a(matrices, x, y, w, h, 4.0f, active ? 0.9f : 0.5f, border);

        float tagW = Fonts.e.a("v2.0", 5.0f) + 4.0f;
        float tagX = (x + w) - 4.0f - tagW;
        event.d().a(matrices, tagX, y + 3.0f, tagW, 6.0f, 2.0f, ColorUtil.a(primary, 0.25f * anim));
        Fonts.e.a(event.h(), "v2.0", tagX + 2.0f, y + 3.5f, 4.75f, ColorUtil.a(primary, anim));

        float thumbW = w - 6.0f;
        float thumbH = 16.0f;
        float thumbX = x + 3.0f;
        float thumbY = y + 3.0f;
        event.d().a(matrices, thumbX, thumbY, thumbW, thumbH, 3.0f, ColorUtil.a(8, 8, 14, (int) (220 * anim)));

        if ("Интерфейс".equals(modeTarget)) {
            // Image 5: interface.png (pill)
            float imgH = thumbH - 1.0f;
            float imgW = Math.min(thumbW - 2.0f, imgH * (228.0f / 73.0f));
            float imgX = thumbX + (thumbW - imgW) / 2.0f;
            float imgY = thumbY + (thumbH - imgH) / 2.0f;
            event.d().a(matrices, INTERFACE_STYLE_TEXTURE, imgX, imgY, imgW, imgH, 2.0f, ColorUtil.a(-1, anim));
        } else {
            // Image 3: standard.png (classic structures header & row)
            float imgH = thumbH - 1.0f;
            float imgW = Math.min(thumbW - 2.0f, imgH * (156.0f / 93.0f));
            float imgX = thumbX + (thumbW - imgW) / 2.0f;
            float imgY = thumbY + (thumbH - imgH) / 2.0f;
            event.d().a(matrices, STANDARD_STYLE_TEXTURE, imgX, imgY, imgW, imgH, 2.0f, ColorUtil.a(-1, anim));
        }

        Fonts.e.a(event.h(), title, x + 4.0f, y + 23.0f, 5.75f, ColorUtil.a(active ? primary : -1, anim));
        Fonts.e.a(event.h(), sub, x + 4.0f, y + 31.5f, 4.5f, ColorUtil.a(ColorUtil.a(160, 165, 180, 255), 0.75f * anim));
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
        float cardH = 42.0f;
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

            // Style Card 1 (Интерфейс)
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, startCardY, cardW, cardH)) {
                this.mode.a("Интерфейс");
                return true;
            }

            // Style Card 2 (Стандарт)
            if (MathUtil.a(mouseX, mouseY, mx + 14.0f + cardW, startCardY, cardW, cardH)) {
                this.mode.a("Стандарт");
                return true;
            }

            // Checkbox 1: Прозрачный стиль
            float secY = startCardY + cardH + 6.0f;
            float chkY1 = secY + 8.5f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY1, mw - 20.0f, 9.0f)) {
                this.transparentStyle.a(!this.transparentStyle.c().booleanValue());
                return true;
            }

            // Checkbox 2: Закруглённые углы
            float chkY2 = chkY1 + 11.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY2, mw - 20.0f, 9.0f)) {
                this.roundedCorners.a(!this.roundedCorners.c().booleanValue());
                return true;
            }

            // Checkbox 3: Формат отображения времени
            float chkY3 = chkY2 + 11.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY3, mw - 20.0f, 9.0f)) {
                this.showTimeText.a(!this.showTimeText.c().booleanValue());
                return true;
            }

            // Time format buttons
            float timeSecY = chkY3 + 14.0f;
            float timeBtnY = timeSecY + 15.0f;
            float btnW = (mw - 22.0f) / 2.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, timeBtnY, btnW, 13.0f)) {
                this.timeFormat.a("9.2c");
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 12.0f + btnW, timeBtnY, btnW, 13.0f)) {
                this.timeFormat.a("09:22");
                return true;
            }

            // Sliders
            float sliderY = timeBtnY + 17.5f;
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
            float timeSecY = startCardY + cardH + 6.0f + 8.5f + 22.0f + 14.0f;
            float timeBtnY = timeSecY + 15.0f;
            float sliderY = timeBtnY + 17.5f;
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

    private String formatTime(float seconds) {
        if (this.timeFormat.l("09:22")) {
            int totalSec = (int) seconds;
            return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
        }
        return String.format(Locale.US, "%.1fs", seconds);
    }

    private List<StructureData> getActiveStructures() {
        List<StructureData> result = new ArrayList<>();
        Structures module = Structures.getInstance();
        if (module != null) {
            for (Structures.a struct : module.getStructures()) {
                long elapsed = struct.c().c();
                long total = struct.d().d();
                float remaining = Math.max(0.0f, (total - elapsed) / 1000.0f);
                float progress = Math.min(1.0f, Math.max(0.0f, (total - elapsed) / (float) total));
                result.add(new StructureData(struct.d().a(), struct.d().e(), remaining, progress));
            }
        }
        if (result.isEmpty() && (aM_.currentScreen instanceof ChatScreen)) {
            result.add(new StructureData("Трапка", Items.NETHERITE_SCRAP, 6.1f, 6.1f / 15.0f));
        }
        return result;
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = aM_.currentScreen instanceof ChatScreen;
        Structures module = Structures.getInstance();
        if (module != null && module.m() && !module.getStructures().isEmpty()) {
            visible = true;
        }
        d().a(visible);
        super.a(event);
    }
}
