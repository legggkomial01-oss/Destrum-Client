package aethereal.ui.widget;

import platform.inject.accessors.BossBarHudAccessor;
import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.EventManager;
import aethereal.core.EventTarget;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.MathUtil;
import aethereal.util.ServerUtil;

import aethereal.config.ThemeInfo;
import aethereal.core.Interface;
import aethereal.event.ClickEvent;
import aethereal.event.DrawEvent;
import aethereal.ui.element.DragInfo;
import aethereal.ui.widget.Widget;

import aethereal.setting.BooleanSetting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.screen.ChatScreen;

public class WatermarkWidget extends Widget implements Interface {
    private static WatermarkWidget INSTANCE;

    private float smoothedFps;
    private float smoothedTopWidth = 0.0f;
    private float smoothedBottomWidth = 0.0f;

    private final BooleanSetting centerSetting;
    private final BooleanSetting separateSetting;
    private final BooleanSetting fpsSetting;
    private final BooleanSetting pingSetting;
    private final BooleanSetting timeSetting;
    private final BooleanSetting loginSetting;
    private final BooleanSetting coordsSetting;
    private final BooleanSetting tpsSetting;
    private final BooleanSetting bpsSetting;

    private final List<WatermarkPill> pills = new ArrayList<>();
    private WatermarkPill draggedPill = null;

    public static boolean isPillInteracting() {
        return INSTANCE != null && INSTANCE.draggedPill != null;
    }

    public static boolean isInteractingWithPill(double mx, double my) {
        if (INSTANCE == null) return false;
        if (INSTANCE.draggedPill != null) return true;
        for (WatermarkPill p : INSTANCE.pills) {
            if (!"logo".equals(p.id) && p.isVisible() && !Float.isNaN(p.curX)) {
                if (MathUtil.a(mx, my, p.curX - 2.0f, p.curY - 2.0f, p.width + 4.0f, INSTANCE.d + 4.0f)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class WatermarkPill {
        public final String id;
        public final String icon;
        public final BooleanSetting setting;
        public final int defaultRow;
        public final int defaultOrder;
        public int row;
        public int order;
        public boolean detached = false;
        public float detachedX = 0.0f;
        public float detachedY = 0.0f;
        public float slotX = 0.0f;
        public float slotY = 0.0f;
        public float curX = Float.NaN;
        public float curY = Float.NaN;
        public float width = 0.0f;
        public float targetWidth = 0.0f;
        public boolean dragging = false;
        public float dragOffsetX;
        public float dragOffsetY;

        public WatermarkPill(String id, String icon, BooleanSetting setting, int defaultRow, int defaultOrder) {
            this.id = id;
            this.icon = icon;
            this.setting = setting;
            this.defaultRow = defaultRow;
            this.defaultOrder = defaultOrder;
            this.row = defaultRow;
            this.order = defaultOrder;
        }

        public boolean isVisible() {
            return this.setting == null || this.setting.c().booleanValue();
        }

        public void reset() {
            this.detached = false;
            this.row = this.defaultRow;
            this.order = this.defaultOrder;
            this.curX = Float.NaN;
            this.curY = Float.NaN;
        }
    }

    public WatermarkWidget() {
        super(new DragInfo("Инфо-панель", 0.0f, 0.0f, 0.0f, 0.0f));
        INSTANCE = this;
        this.centerSetting = new BooleanSetting("Центрировать", true);
        this.separateSetting = new BooleanSetting("Разделять элементы", false);
        this.fpsSetting = new BooleanSetting("Частота кадров", true);
        this.pingSetting = new BooleanSetting("Задержка игрока", true);
        this.timeSetting = new BooleanSetting("Текущее время", true);
        this.loginSetting = new BooleanSetting("Логин в клиенте", true);
        this.coordsSetting = new BooleanSetting("Координаты", true);
        this.tpsSetting = new BooleanSetting("Задержка сервера", true);
        this.bpsSetting = new BooleanSetting("Скорость игрока", true);

        // Define all pills with their default row and order
        this.pills.add(new WatermarkPill("logo", "a", null, 0, 0));
        this.pills.add(new WatermarkPill("login", "L", this.loginSetting, 0, 1));
        this.pills.add(new WatermarkPill("fps", "q", this.fpsSetting, 0, 2));
        this.pills.add(new WatermarkPill("ping", "P", this.pingSetting, 0, 3));
        this.pills.add(new WatermarkPill("time", "T", this.timeSetting, 0, 4));

        this.pills.add(new WatermarkPill("coords", "b", this.coordsSetting, 1, 0));
        this.pills.add(new WatermarkPill("tps", "g", this.tpsSetting, 1, 1));
        this.pills.add(new WatermarkPill("bps", "e", this.bpsSetting, 1, 2));

        j().a(this);
        j().a(0); // Fully draggable!

        a(this.centerSetting, this.separateSetting, this.fpsSetting, this.pingSetting, this.timeSetting, this.loginSetting, this.coordsSetting, this.tpsSetting, this.bpsSetting);
        EventManager.a(this);
    }

    @EventTarget
    public void onMouseClick(ClickEvent event) {
        if (aM_.currentScreen instanceof ChatScreen) {
            double mx = event.f();
            double my = event.g();
            if (event.b() && event.h() == 0) {
                if (net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
                    return;
                }
                // Check if clicked on any active detachable pill
                for (int i = this.pills.size() - 1; i >= 0; i--) {
                    WatermarkPill p = this.pills.get(i);
                    if (!"logo".equals(p.id) && p.isVisible() && !Float.isNaN(p.curX)) {
                        if (MathUtil.a(mx, my, p.curX - 2.0f, p.curY - 2.0f, p.width + 4.0f, this.d + 4.0f)) {
                            this.draggedPill = p;
                            p.dragging = true;
                            p.dragOffsetX = (float) (mx - p.curX);
                            p.dragOffsetY = (float) (my - p.curY);
                            break;
                        }
                    }
                }
            } else if (event.d() && this.draggedPill != null && event.h() == 0) {
                float newX = (float) (mx - this.draggedPill.dragOffsetX);
                float newY = (float) (my - this.draggedPill.dragOffsetY);
                this.draggedPill.curX = newX;
                this.draggedPill.curY = newY;

                // Check distance from its slot
                if (!this.draggedPill.detached) {
                    float dist = (float) Math.hypot(newX - this.draggedPill.slotX, newY - this.draggedPill.slotY);
                    if (dist > 25.0f) {
                        this.draggedPill.detached = true;
                        this.draggedPill.detachedX = newX;
                        this.draggedPill.detachedY = newY;
                        this.centerSetting.a(Boolean.valueOf(false));
                    } else {
                        // Check order swap with neighbor in same row
                        List<WatermarkPill> rowPills = getAttachedPillsInRow(this.draggedPill.row);
                        for (WatermarkPill other : rowPills) {
                            if (other != this.draggedPill && !"logo".equals(other.id)) {
                                float myCenter = newX + (this.draggedPill.width / 2.0f);
                                float otherCenter = other.curX + (other.width / 2.0f);
                                if (Math.abs(myCenter - otherCenter) < (this.draggedPill.width + other.width) * 0.35f) {
                                    int tempOrder = this.draggedPill.order;
                                    this.draggedPill.order = other.order;
                                    other.order = tempOrder;
                                    this.centerSetting.a(Boolean.valueOf(false));
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    this.draggedPill.detachedX = newX;
                    this.draggedPill.detachedY = newY;
                    // Check snap-attach back to watermark bar
                    float barX = j().a();
                    float barY = j().b();
                    float barW = j().f();
                    float barH = j().g();
                    if (MathUtil.a(newX, newY, barX - 20.0f, barY - 20.0f, barW + 40.0f, barH + 40.0f)) {
                        this.draggedPill.detached = false;
                    }
                }
            } else if (event.c() && event.h() == 0) {
                if (this.draggedPill != null) {
                    this.draggedPill.dragging = false;
                    this.draggedPill = null;
                }
            }
        }
    }

    private List<WatermarkPill> getAttachedPillsInRow(int row) {
        List<WatermarkPill> list = new ArrayList<>();
        for (WatermarkPill p : this.pills) {
            if (p.isVisible() && !p.detached && p.row == row) {
                list.add(p);
            }
        }
        list.sort(Comparator.comparingInt(a -> a.order));
        return list;
    }

    private List<WatermarkPill> getDetachedPills() {
        List<WatermarkPill> list = new ArrayList<>();
        for (WatermarkPill p : this.pills) {
            if (p.isVisible() && p.detached) {
                list.add(p);
            }
        }
        return list;
    }

    private String getPillText(WatermarkPill pill) {
        return switch (pill.id) {
            case "logo" -> null;
            case "login" -> Delta.h().g().b();
            case "fps" -> ((int) this.smoothedFps) + " FPS";
            case "ping" -> ServerUtil.d() + " ms";
            case "time" -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "coords" -> aM_.player != null ? "x " + ((int) aM_.player.getX()) + " y " + ((int) aM_.player.getY()) + " z " + ((int) aM_.player.getZ()) : "x 0 y 0 z 0";
            case "tps" -> String.format("%.1f TPS", Float.valueOf(Delta.h().d().v().j().a()));
            case "bps" -> String.format("%.2f BPS", Double.valueOf(ServerUtil.c()));
            default -> "";
        };
    }

    @Override
    public void a(DrawEvent event) {
        d().a(true);
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        // Anti-jitter: smooth FPS calculation
        int currentFps = aM_.getCurrentFps();
        this.smoothedFps = MathUtil.c(this.smoothedFps, currentFps, 0.05f);

        float iconSize = this.e - 0.5f;
        float logoSize = this.e + 1.0f;
        float startPadding = 5.0f;
        float sectionGap = !this.separateSetting.c().booleanValue() ? 5.0f : 2.0f;
        float iconTextGap = 3.0f;

        // Calculate and stabilize pill widths (anti-shaking)
        for (WatermarkPill p : this.pills) {
            if (!p.isVisible()) continue;
            String text = getPillText(p);
            float targetW;
            if ("logo".equals(p.id)) {
                targetW = (startPadding * 2.0f) + Fonts.a.a("a", logoSize);
            } else {
                float rawTextW = Fonts.e.a(text, this.e);
                // Stabilize numeric sections so width never jiggles
                if ("fps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("999 FPS", this.e), rawTextW);
                } else if ("ping".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("999 ms", this.e), rawTextW);
                } else if ("tps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("20.0 TPS", this.e), rawTextW);
                } else if ("bps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("0.00 BPS", this.e), rawTextW);
                }
                targetW = (startPadding * 2.0f) + Fonts.a.a(p.icon, iconSize) + iconTextGap + rawTextW;
            }
            p.targetWidth = targetW;
            if (p.width <= 0.0f) {
                p.width = targetW;
            } else {
                p.width = MathUtil.c(p.width, targetW, 0.15f);
            }
        }

        // Calculate row widths of attached pills
        List<WatermarkPill> row0 = getAttachedPillsInRow(0);
        List<WatermarkPill> row1 = getAttachedPillsInRow(1);

        float calcTopW = calculateRowWidth(row0, sectionGap, startPadding);
        float calcBottomW = calculateRowWidth(row1, sectionGap, startPadding);

        if (this.smoothedTopWidth <= 0.0f) {
            this.smoothedTopWidth = calcTopW;
            this.smoothedBottomWidth = calcBottomW;
        } else {
            this.smoothedTopWidth = MathUtil.c(this.smoothedTopWidth, calcTopW, 0.15f);
            this.smoothedBottomWidth = MathUtil.c(this.smoothedBottomWidth, calcBottomW, 0.15f);
        }

        float topWidth = this.smoothedTopWidth;
        float bottomWidth = this.smoothedBottomWidth;

        // Boss bar vertical offset
        float bossOffset = 0.0f;
        if (!((platform.inject.accessors.BossBarHudAccessor) aM_.inGameHud.getBossBarHud()).getBossBars().isEmpty()) {
            int size = ((platform.inject.accessors.BossBarHudAccessor) aM_.inGameHud.getBossBarHud()).getBossBars().size() - 1;
            Objects.requireNonNull(aM_.textRenderer);
            bossOffset = (((12 + (size * (10 + 9))) + 5) * aM_.getWindow().calculateScaleFactor(((Integer) aM_.options.getGuiScale().getValue()).intValue(), aM_.forcesUnicodeFont())) / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont());
        }

        // Check if watermark widget itself is being dragged: turns off center automatically
        if (Delta.h().d().s() != null && Delta.h().d().s().g() == j()) {
            this.centerSetting.a(Boolean.valueOf(false));
        }

        // Handle center setting: if center is enabled, position at top center and reset detached pills
        if (this.centerSetting.c().booleanValue()) {
            boolean anyChanged = this.pills.stream().anyMatch(p -> p.detached || p.order != p.defaultOrder || p.row != p.defaultRow);
            if (anyChanged) {
                for (WatermarkPill p : this.pills) {
                    p.reset();
                }
            }
            float centerX = (aM_.getWindow().getScaledWidth() - topWidth) / 2.0f;
            float centerY = 5.0f + bossOffset;
            j().a(centerX);
            j().b(centerY);
        }

        j().c(Math.max(topWidth, bottomWidth));
        j().d(row1.isEmpty() ? this.d : this.d + 3.0f + this.d);

        float x = j().a();
        float y = j().b();
        float bottomX = x + ((topWidth - bottomWidth) / 2.0f);

        int primaryColor = ColorUtil.a(Delta.h().d().o().a(ThemeInfo.PRIMARY).a(), 1.0f);
        float textYOffset = -0.5f;

        // 1. Draw attached Row 0 pills
        drawPillsRow(event, row0, x, y, topWidth, primaryColor, iconSize, logoSize, startPadding, sectionGap, iconTextGap, textYOffset);

        // 2. Draw attached Row 1 pills
        if (!row1.isEmpty()) {
            drawPillsRow(event, row1, bottomX, y + this.d + 3.0f, bottomWidth, primaryColor, iconSize, logoSize, startPadding, sectionGap, iconTextGap, textYOffset);
        }

        // 3. Draw detached pills (rendered individually at their detached positions)
        for (WatermarkPill p : getDetachedPills()) {
            if (Float.isNaN(p.curX)) {
                p.curX = p.detachedX;
                p.curY = p.detachedY;
            } else if (!p.dragging) {
                p.curX = MathUtil.c(p.curX, p.detachedX, 0.35f);
                p.curY = MathUtil.c(p.curY, p.detachedY, 0.35f);
            }
            drawSinglePill(event, p, p.curX, p.curY, primaryColor, iconSize, logoSize, startPadding, iconTextGap, textYOffset, p.dragging);
        }

        super.a(event);
    }

    private float calculateRowWidth(List<WatermarkPill> rowPills, float sectionGap, float startPadding) {
        if (rowPills.isEmpty()) return 0.0f;
        if (this.separateSetting.c().booleanValue()) {
            float w = 0.0f;
            for (WatermarkPill p : rowPills) {
                w += p.width + sectionGap;
            }
            return Math.max(0.0f, w - sectionGap);
        }
        float w = startPadding;
        for (int i = 0; i < rowPills.size(); i++) {
            if (i > 0) w += 1.0f + sectionGap;
            WatermarkPill p = rowPills.get(i);
            if ("logo".equals(p.id)) {
                w += Fonts.a.a("a", this.e + 1.0f) + 4.0f;
            } else {
                float rawTextW = Fonts.e.a(getPillText(p), this.e);
                if ("fps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("999 FPS", this.e), rawTextW);
                } else if ("ping".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("999 ms", this.e), rawTextW);
                } else if ("tps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("20.0 TPS", this.e), rawTextW);
                } else if ("bps".equals(p.id)) {
                    rawTextW = Math.max(Fonts.e.a("0.00 BPS", this.e), rawTextW);
                }
                w += Fonts.a.a(p.icon, this.e - 0.5f) + 3.0f + rawTextW + sectionGap;
            }
        }
        return w;
    }

    private void drawPillsRow(DrawEvent event, List<WatermarkPill> rowPills, float rowX, float rowY, float rowWidth, int primaryColor, float iconSize, float logoSize, float startPadding, float sectionGap, float iconTextGap, float textYOffset) {
        if (rowPills.isEmpty()) return;

        // If not separated, draw one combined background bar for all attached pills
        if (!this.separateSetting.c().booleanValue()) {
            a(event, rowX, rowY, rowWidth, this.d, true, 1.0f);
        }

        float cursor = rowX + (!this.separateSetting.c().booleanValue() ? startPadding : 0.0f);
        float textY = rowY + ((this.d - Fonts.e.a(this.e)) / 2.0f) + textYOffset;
        boolean watermarkDragging = Delta.h().d().s() != null && Delta.h().d().s().g() == j();

        for (int i = 0; i < rowPills.size(); i++) {
            WatermarkPill p = rowPills.get(i);
            p.slotX = cursor;
            p.slotY = rowY;

            if (Float.isNaN(p.curX)) {
                p.curX = p.slotX;
                p.curY = p.slotY;
            } else if (!p.dragging) {
                if (watermarkDragging) {
                    p.curX = p.slotX;
                    p.curY = p.slotY;
                } else {
                    p.curX = MathUtil.c(p.curX, p.slotX, 0.35f);
                    p.curY = MathUtil.c(p.curY, p.slotY, 0.35f);
                }
            }

            if (this.separateSetting.c().booleanValue()) {
                drawSinglePill(event, p, p.curX, p.curY, primaryColor, iconSize, logoSize, startPadding, iconTextGap, textYOffset, p.dragging);
                cursor += p.width + sectionGap;
            } else {
                if (i > 0) {
                    a(event, cursor, rowY, this.d, 1.0f);
                    cursor += 1.0f + sectionGap;
                }
                if (p.dragging) {
                    drawSinglePill(event, p, p.curX, p.curY, primaryColor, iconSize, logoSize, startPadding, iconTextGap, textYOffset, true);
                    cursor += p.width + sectionGap;
                } else {
                    if ("logo".equals(p.id)) {
                        Fonts.a.a(event.h(), "a", cursor, rowY + ((this.d - Fonts.a.a(logoSize)) / 2.0f), logoSize, primaryColor);
                        cursor += Fonts.a.a("a", logoSize) + 4.0f;
                    } else {
                        Fonts.a.a(event.h(), p.icon, cursor, rowY + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, primaryColor);
                        float textX = cursor + Fonts.a.a(p.icon, iconSize) + iconTextGap;
                        String text = getPillText(p);
                        Fonts.e.a(event.h(), text, textX, textY, this.e, -1);
                        cursor = textX + Fonts.e.a(text, this.e) + sectionGap;
                    }
                }
            }
        }
    }

    private void drawSinglePill(DrawEvent event, WatermarkPill p, float x, float y, int primaryColor, float iconSize, float logoSize, float startPadding, float iconTextGap, float textYOffset, boolean dragging) {
        float textY = y + ((this.d - Fonts.e.a(this.e)) / 2.0f) + textYOffset;

        // Elevated glow effect when dragging pill
        if (dragging) {
            int glowColor = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
            event.d().a(event.h(), x - 2.0f, y - 2.0f, p.width + 4.0f, this.d + 4.0f, 6.0f, ColorUtil.a(glowColor, 0.35f), 1.0f, ColorUtil.a(glowColor, 0.5f), 10.0f);
        }

        a(event, x, y, p.width, this.d, true, 1.0f);
        float innerX = x + startPadding;

        if ("logo".equals(p.id)) {
            Fonts.a.a(event.h(), "a", innerX, y + ((this.d - Fonts.a.a(logoSize)) / 2.0f), logoSize, primaryColor);
        } else {
            Fonts.a.a(event.h(), p.icon, innerX, y + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, primaryColor);
            float textX = innerX + Fonts.a.a(p.icon, iconSize) + iconTextGap;
            String text = getPillText(p);
            Fonts.e.a(event.h(), text, textX, textY, this.e, -1);
        }
    }
}
