package aethereal.ui.screen;

import aethereal.config.MainMenuConfig;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.render.ColorUtil;
import aethereal.render.DestrumIconRenderer;
import aethereal.render.Draw2DProcessor;
import aethereal.render.Fonts;
import aethereal.render.ScaleUtil;
import aethereal.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern Destrum-V2 Settings screen with continuous sunset background,
 * glassmorphic cards, and smooth cinematic transitions.
 */
public class DestrumSettingsScreen extends Screen {
    private final Screen parent;
    private final List<SettingCard> cards = new ArrayList<>();
    private final Map<Integer, Float> hoverAnimations = new HashMap<>();

    private float scrollX = 0.0f;
    private float targetScrollX = 0.0f;
    private boolean isDragging = false;
    private double lastDragMouseX = 0.0;

    private final long openStartTime = System.currentTimeMillis();
    private boolean isExiting = false;
    private long exitStartTime = 0L;
    private static final float TRANSITION_MS = 240.0f;

    public static class SettingCard {
        public final String title;
        public final String subtitle;
        public final int iconType; // 0=Video, 1=Sound, 2=Keys, 3=Language, 4=Background, 5=AllOptions
        public final Runnable action;

        public SettingCard(String title, String subtitle, int iconType, Runnable action) {
            this.title = title;
            this.subtitle = subtitle;
            this.iconType = iconType;
            this.action = action;
        }
    }

    public DestrumSettingsScreen(Screen parent) {
        super(Text.literal("Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.cards.clear();

        // 1. Video Options
        this.cards.add(new SettingCard("Графика", "Видео, FPS, шейдеры", 0, () -> {
            if (this.client != null) {
                this.client.setScreen(new VideoOptionsScreen(this, this.client, this.client.options));
            }
        }));

        // 2. Sound Options
        this.cards.add(new SettingCard("Звуки и музыка", "Громкость, окружение", 1, () -> {
            if (this.client != null) {
                this.client.setScreen(new SoundOptionsScreen(this, this.client.options));
            }
        }));

        // 3. Controls / Keybinds
        this.cards.add(new SettingCard("Управление", "Клавиши, мышь, бинды", 2, () -> {
            if (this.client != null) {
                this.client.setScreen(new KeybindsScreen(this, this.client.options));
            }
        }));

        // 4. Language
        this.cards.add(new SettingCard("Язык", "Выбор языка игры", 3, () -> {
            if (this.client != null) {
                this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
            }
        }));

        // 5. Main Menu Background mode cycle
        this.cards.add(new SettingCard("Фон меню", "Destrum-V2 / Другие", 4, () -> {
            MainMenuConfig cfg = MainMenuConfig.getInstance();
            MainMenuConfig.BackgroundMode[] modes = MainMenuConfig.BackgroundMode.values();
            int nextIdx = (cfg.getBackgroundMode().ordinal() + 1) % modes.length;
            cfg.setBackgroundMode(modes[nextIdx]);
        }));

        // 6. Vanilla All Options
        this.cards.add(new SettingCard("Все настройки", "Классическое меню", 5, () -> {
            if (this.client != null) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int width = this.width;
        int height = this.height;
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        int sunsetAccent = ColorUtil.a(255, 175, 65, 255);

        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);

        // 1. Background: continuous live Destrum-V2 sunset panorama
        MainScreen.renderDestrumV2Background(context, width, height, mouseX, mouseY);

        // Transition progress calculation
        float animProgress;
        if (this.isExiting) {
            float elapsed = (System.currentTimeMillis() - this.exitStartTime) / TRANSITION_MS;
            animProgress = 1.0f - Math.min(1.0f, Math.max(0.0f, elapsed));
            if (elapsed >= 1.0f) {
                if (this.client != null) {
                    this.client.setScreen(this.parent);
                }
                return;
            }
        } else {
            float elapsed = (System.currentTimeMillis() - this.openStartTime) / TRANSITION_MS;
            animProgress = Math.min(1.0f, Math.max(0.0f, elapsed));
        }

        // Cubic ease-out
        float ease = (float) (1.0 - Math.pow(1.0 - animProgress, 3.0));

        matrices.push();
        matrices.translate(width * 0.5f, height * 0.5f + (1.0f - ease) * 26.0f, 0.0f);
        float enterScale = 0.95f + 0.05f * ease;
        matrices.scale(enterScale, enterScale, 1.0f);
        matrices.translate(-width * 0.5f, -height * 0.5f, 0.0f);

        // Scroll limits
        float cardW = 120.0f;
        float cardH = 120.0f;
        float gap = 20.0f;
        int totalItems = this.cards.size();
        float totalWidth = totalItems * cardW + (totalItems - 1) * gap;
        float maxScroll = Math.max(0.0f, totalWidth - (width - 120.0f));
        this.targetScrollX = MathHelper.clamp(this.targetScrollX, 0.0f, maxScroll);
        this.scrollX = MathUtil.c(this.scrollX, this.targetScrollX, 14.0f);

        // Subtle ambient dim overlay for readability
        draw.a(matrices, 0.0f, 0.0f, (float) width, (float) height, 0.0f, ColorUtil.a(0, 0, 0, (int) (40.0f * ease)));

        // 2. Top Header (Header slides down smoothly)
        float headerOffset = (1.0f - ease) * -22.0f;

        // Back button "←"
        float backX = 22.0f;
        float backY = 18.0f + headerOffset;
        float backSize = 22.0f;
        boolean backHover = MathUtil.a(dA, dA2, backX, backY, backSize, backSize);
        draw.a(matrices, backX, backY, backSize, backSize, backSize * 0.5f, ColorUtil.a(20, 24, 34, backHover ? 210 : 150));
        draw.a(matrices, backX, backY, backSize, backSize, backSize * 0.5f, 1.0f, backHover ? sunsetAccent : ColorUtil.a(255, 255, 255, 40));
        Fonts.c.a(matrices, "←", backX + 7.0f, backY + 5.0f, 8.5f, backHover ? sunsetAccent : -1);

        // Title & Subtitle
        String title = "Настройки";
        float titleW = Fonts.c.a(title, 13.0f);
        Fonts.c.a(matrices, title, (width - titleW) * 0.5f, 18.0f + headerOffset, 13.0f, -1);

        String subtitle = "(Параметры графики, звука, управления и фона)";
        float subW = Fonts.c.a(subtitle, 7.0f);
        Fonts.c.a(matrices, subtitle, (width - subW) * 0.5f, 34.5f + headerOffset, 7.0f, ColorUtil.a(180, 185, 200, 200));

        // 3. Settings Cards Carousel
        float startX = 60.0f;
        if (totalWidth < (width - 120.0f)) {
            startX = (width - totalWidth) * 0.5f;
        }
        float cardY = (height - cardH) * 0.5f + 10.0f;

        for (int i = 0; i < this.cards.size(); i++) {
            SettingCard card = this.cards.get(i);
            float curCardX = startX + i * (cardW + gap) - this.scrollX;

            // Frustum cull
            if (curCardX + cardW < -20.0f || curCardX > width + 20.0f) {
                continue;
            }

            boolean cardHover = MathUtil.a(dA, dA2, curCardX, cardY, cardW, cardH) && !this.isExiting;
            float anim = this.hoverAnimations.getOrDefault(i, 0.0f);
            anim = MathUtil.c(anim, cardHover ? 1.0f : 0.0f, 12.0f);
            this.hoverAnimations.put(i, anim);

            float drawCardY = cardY - (anim * 4.0f);
            float radius = 16.0f;

            // Card frosted glass body
            draw.a(matrices, curCardX, drawCardY, cardW, cardH, radius, ColorUtil.a(16, 20, 28, (int) (160.0f + anim * 50.0f)));
            int borderColor = ColorUtil.a(ColorUtil.a(255, 255, 255, 30), sunsetAccent, anim);
            draw.a(matrices, curCardX, drawCardY, cardW, cardH, radius, 1.0f + anim * 0.5f, borderColor);

            // Icon circle badge inside card
            float badgeSize = 44.0f;
            float badgeX = curCardX + (cardW - badgeSize) * 0.5f;
            float badgeY = drawCardY + 20.0f;
            draw.a(matrices, badgeX, badgeY, badgeSize, badgeSize, badgeSize * 0.5f, ColorUtil.a(255, 255, 255, cardHover ? 35 : 18));
            draw.a(matrices, badgeX, badgeY, badgeSize, badgeSize, badgeSize * 0.5f, 1.0f, cardHover ? sunsetAccent : ColorUtil.a(255, 255, 255, 45));

            float icX = badgeX + badgeSize * 0.5f;
            float icY = badgeY + badgeSize * 0.5f;
            renderSettingIcon(matrices, draw, card.iconType, icX, icY, cardHover, sunsetAccent);

            // Card title & subtitle
            float tW = Fonts.c.a(card.title, 8.5f);
            Fonts.c.a(matrices, card.title, curCardX + (cardW - tW) * 0.5f, drawCardY + 74.0f, 8.5f, cardHover ? sunsetAccent : -1);

            float sW = Fonts.c.a(card.subtitle, 6.25f);
            Fonts.c.a(matrices, card.subtitle, curCardX + (cardW - sW) * 0.5f, drawCardY + 89.0f, 6.25f, ColorUtil.a(165, 175, 190, 190));
        }

        matrices.pop();
        ScaleUtil.a(context);
    }

    private void renderSettingIcon(MatrixStack matrices, Draw2DProcessor draw, int type, float cx, float cy, boolean hover, int accent) {
        switch (type) {
            case 0 -> {
                // Video monitor icon
                float mw = 16.0f, mh = 11.0f;
                draw.a(matrices, cx - mw * 0.5f, cy - mh * 0.5f - 2.0f, mw, mh, 2.5f, ColorUtil.a(255, 255, 255, hover ? 60 : 35));
                draw.a(matrices, cx - mw * 0.5f, cy - mh * 0.5f - 2.0f, mw, mh, 2.5f, 1.0f, hover ? accent : ColorUtil.a(255, 255, 255, 220));
                // Stand
                draw.a(matrices, cx - 1.0f, cy + 4.5f, 2.0f, 3.5f, 0.5f, hover ? accent : -1);
                draw.a(matrices, cx - 4.5f, cy + 8.0f, 9.0f, 1.2f, 0.6f, hover ? accent : -1);
            }
            case 1 -> {
                // Sound speaker icon
                float sw = 4.0f, sh = 8.0f;
                draw.a(matrices, cx - 6.0f, cy - sh * 0.5f, sw, sh, 1.0f, hover ? accent : -1);
                draw.a(matrices, cx - 3.0f, cy - 6.0f, 4.5f, 12.0f, 1.5f, hover ? accent : -1);
                // Sound wave arcs
                draw.a(matrices, cx + 4.0f, cy - 4.0f, 1.2f, 8.0f, 0.6f, hover ? accent : ColorUtil.a(255, 255, 255, 200));
                draw.a(matrices, cx + 7.5f, cy - 6.0f, 1.2f, 12.0f, 0.6f, hover ? accent : ColorUtil.a(255, 255, 255, 150));
            }
            case 2 -> {
                // Controls / keyboard keycap icon
                float kw = 16.0f, kh = 14.0f;
                draw.a(matrices, cx - kw * 0.5f, cy - kh * 0.5f, kw, kh, 3.0f, ColorUtil.a(255, 255, 255, hover ? 50 : 25));
                draw.a(matrices, cx - kw * 0.5f, cy - kh * 0.5f, kw, kh, 3.0f, 1.1f, hover ? accent : -1);
                Fonts.c.a(matrices, "WASD", cx - 9.0f, cy - 3.0f, 6.0f, hover ? accent : -1);
            }
            case 3 -> {
                // Globe icon
                DestrumIconRenderer.render3DGlobe(matrices, draw, cx, cy, 24.0f, hover, 1.0f, accent);
            }
            case 4 -> {
                // Sunset theme icon (isometric cube block)
                DestrumIconRenderer.render3DCube(matrices, draw, cx, cy, 24.0f, hover, 1.0f, accent);
            }
            case 5 -> {
                // Gear icon
                DestrumIconRenderer.render3DGear(matrices, draw, cx, cy, 24.0f, hover, 1.0f, accent);
            }
        }
    }

    public void triggerClose() {
        if (!this.isExiting) {
            this.isExiting = true;
            this.exitStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isExiting) return true;
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);

        // Back button "←"
        float backX = 22.0f;
        float backY = 18.0f;
        float backSize = 22.0f;
        if (button == 0 && MathUtil.a(dA, dA2, backX, backY, backSize, backSize)) {
            triggerClose();
            return true;
        }

        // Cards click
        float cardW = 120.0f;
        float cardH = 120.0f;
        float gap = 20.0f;
        int totalItems = this.cards.size();
        float totalWidth = totalItems * cardW + (totalItems - 1) * gap;
        float startX = 60.0f;
        if (totalWidth < (this.width - 120.0f)) {
            startX = (this.width - totalWidth) * 0.5f;
        }
        float cardY = (this.height - cardH) * 0.5f + 10.0f;

        if (button == 0) {
            for (int i = 0; i < this.cards.size(); i++) {
                float curCardX = startX + i * (cardW + gap) - this.scrollX;
                if (MathUtil.a(dA, dA2, curCardX, cardY, cardW, cardH)) {
                    this.cards.get(i).action.run();
                    return true;
                }
            }
            this.isDragging = true;
            this.lastDragMouseX = dA;
        }
        return super.mouseClicked(dA, dA2, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double dA = MathUtil.scale(mouseX, 2);
        if (this.isDragging) {
            float dx = (float) (dA - this.lastDragMouseX);
            this.targetScrollX -= dx * 1.2f;
            this.lastDragMouseX = dA;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.targetScrollX -= (float) verticalAmount * 45.0f;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            triggerClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
