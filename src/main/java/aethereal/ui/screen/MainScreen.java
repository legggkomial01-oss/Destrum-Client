package aethereal.ui.screen;

import aethereal.render.ScaleUtil;
import aethereal.core.NativeMethodLookup;
import aethereal.ui.shader.GradientUtil;
import aethereal.ui.widget.EffectMarker;
import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.core.InterfaceC0020Opcode;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.MathUtil;

import aethereal.config.ThemeInfo;
import aethereal.render.Draw2DProcessor;
import aethereal.ui.screen.AltScreen;
import aethereal.module.render.SkyShader;

import aethereal.render.AnimationUtil;
import aethereal.ui.element.Button;
import aethereal.api.Compile;
import aethereal.config.MainMenuConfig;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import org.joml.Quaternionf;

public class MainScreen extends Screen {
    private static final float[] a;
    private static final String SPLASH_TEXT = "Destrum Client — добро пожаловать в игру. Приготовьтесь к победам!";
    private static final float SPLASH_FADE_IN = 480.0f;
    private static final float SPLASH_TYPE_START = 320.0f;
    private static final float SPLASH_TYPE_TIME = 2800.0f;
    private static final float SPLASH_HOLD = 900.0f;
    private static final float SPLASH_FADE_OUT_AT = SPLASH_TYPE_START + SPLASH_TYPE_TIME + SPLASH_HOLD;
    private static final float SPLASH_FADE_OUT = 950.0f;
    private static boolean splashPlayed;
    private final AnimationUtil b;
    private final Button c;
    private final Button d;
    private final Button e;
    private final Button f;
    private final List<Button> g;
    private final List<EffectMarker.a> h;
    private float i;
    private float j;
    private float k;
    private float l;
    private final long m;
    private final boolean n;
    private final AnimationUtil gearAnimation = new AnimationUtil();
    private final AnimationUtil modalAnimation = new AnimationUtil();
    private final TypewriterEffect typewriter = new TypewriterEffect();
    private boolean modalOpen = false;
    private static long destrumV2StartTime = -1L;
    private static float destrumV2CameraX = 0.0f;
    private static float destrumV2CameraY = 0.0f;
    private static final Identifier DESTRUM_V2_TEXTURE = Identifier.of("delta", "pictures/bg_destrum_v2.png");
    private static final Identifier BLOOM_TEXTURE = Identifier.of("delta", "pictures/bloom.png");

    public static class TypewriterEffect {
        private static final String[] RU_PHRASES = {
            "Привет! Вас приветствует Destrum Client",
            "Плавный фреймрейт и максимальная производительность",
            "Идеальный баланс эстетики и превосходства",
            "Готов к новым победам и эпичным сражениям",
            "Создан для тех, кто ценит стиль и контроль",
            "Добро пожаловать в новую эру геймплея",
            "Destrum Client — твоё ключевое преимущество",
            "Безупречная отзывчивость каждого клика",
            "Абсолютный контроль над каждым движением",
            "Чистый дизайн, непревзойденная скорость",
            "Покоряй вершины с совершенным арсеналом",
            "Эстетика минимализма и мощь технологий",
            "Твой верный союзник в самых жарких битвах",
            "Побеждай красиво, доминируй уверенно",
            "Мгновенный отклик и сверхточный расчет",
            "Новые горизонты твоих игровых возможностей",
            "Destrum Client — выбор истинных чемпионов",
            "Никаких преград между тобой и триумфом",
            "Каждая деталь выверена до идеала",
            "Эволюция интерфейса и максимальный комфорт",
            "Всегда на шаг впереди соперников",
            "Качественный рендер и чистая картинка",
            "Вдохновлен лучшими, создан для лучших",
            "Уверенность в каждом движении и ударе",
            "Destrum: точность, грация, превосходство"
        };

        private static final String[] EN_PHRASES = {
            "Hello! Welcome to Destrum Client",
            "Ultra-smooth framerates and peak performance",
            "Perfect balance of aesthetics and supremacy",
            "Ready for victory and epic battles",
            "Engineered for style, speed, and precision",
            "Welcome to the next era of gameplay",
            "Destrum Client — your definitive advantage",
            "Flawless tactile response in every click",
            "Absolute mastery over every motion",
            "Pure aesthetics, unmatched responsiveness",
            "Conquer every arena with superior gear",
            "Minimalist elegance powered by elite tech",
            "Your steadfast ally in the heat of battle",
            "Win in style, dominate with composure",
            "Instant reaction and surgical calculation",
            "Unlocking new heights of gaming prowess",
            "Destrum Client — forged for true champions",
            "Zero compromises between you and triumph",
            "Obsessively crafted to absolute perfection",
            "Next-gen interface and seamless control",
            "Always one step ahead of the competition",
            "Razor-sharp visuals and fluid rendering",
            "Inspired by the finest, built for the best",
            "Total confidence in every clutch and strike",
            "Destrum: precision, grace, supremacy"
        };

        private enum State {
            TYPING,
            PAUSED,
            DELETING
        }

        private State state = State.TYPING;
        private int phraseIndex = 0;
        private int charIndex = 0;
        private long lastTime = System.currentTimeMillis();
        private final Random random = new Random();

        public void update(boolean isEnglish) {
            String[] phrases = isEnglish ? EN_PHRASES : RU_PHRASES;
            if (phraseIndex >= phrases.length) {
                phraseIndex = 0;
            }
            String currentPhrase = phrases[phraseIndex];
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;

            switch (state) {
                case TYPING:
                    if (elapsed >= 55) {
                        lastTime = now;
                        charIndex++;
                        if (charIndex >= currentPhrase.length()) {
                            charIndex = currentPhrase.length();
                            state = State.PAUSED;
                            lastTime = now;
                        }
                    }
                    break;
                case PAUSED:
                    if (elapsed >= 2800) {
                        state = State.DELETING;
                        lastTime = now;
                    }
                    break;
                case DELETING:
                    if (elapsed >= 25) {
                        lastTime = now;
                        charIndex--;
                        if (charIndex <= 0) {
                            charIndex = 0;
                            state = State.TYPING;
                            lastTime = now;
                            int next;
                            do {
                                next = random.nextInt(phrases.length);
                            } while (phrases.length > 1 && next == phraseIndex);
                            phraseIndex = next;
                        }
                    }
                    break;
            }
        }

        public String getCurrentText(boolean isEnglish) {
            String[] phrases = isEnglish ? EN_PHRASES : RU_PHRASES;
            if (phraseIndex >= phrases.length) {
                phraseIndex = 0;
            }
            String currentPhrase = phrases[phraseIndex];
            int len = Math.min(charIndex, currentPhrase.length());
            return currentPhrase.substring(0, len);
        }
    }

    @Compile
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.b.a((Interface.aM_.currentScreen instanceof MainScreen) && q());
        this.b.a(0.0f, 1.0f, 0.15f, EasingList.g, delta);
        float fMin = Math.min(1.0f, this.b.c() / 0.9f);
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        int iMethod_4486 = Interface.aM_.getWindow().getScaledWidth();
        int iMethod_4502 = Interface.aM_.getWindow().getScaledHeight();
        a(context, iMethod_4486, iMethod_4502, (int) dA, (int) dA2, 1.25f - (EasingList.s.ease(fMin) * 0.2f));
        Delta.h().d().i().e().a(context.getMatrices());
        a(iMethod_4486, iMethod_4502);
        a(context, iMethod_4486 * 0.5f, ((iMethod_4502 - this.c.c()) * 0.5f) - 58.0f, fMin);
        Iterator<Button> it = this.g.iterator();
        while (it.hasNext()) {
            it.next().a(context, (int) dA, (int) dA2, delta, fMin);
        }
        a(context, fMin, (int) dA);
        renderGearButton(context, iMethod_4486, (int) dA, (int) dA2, delta, fMin);
        EffectMarker.a(context.getMatrices(), delta, this.h);
        renderSettingsModal(context, iMethod_4486, iMethod_4502, (int) dA, (int) dA2, delta);
        a(context, iMethod_4486, iMethod_4502);
        ScaleUtil.a(context);
    }

    @Compile
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (p()) {
            return true;
        }
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        if (this.modalOpen) {
            if (handleModalClick(dA, dA2, button)) {
                return true;
            }
        }
        float gearSize = 20.0f;
        float gearX = Interface.aM_.getWindow().getScaledWidth() - gearSize - 12.0f;
        float gearY = 12.0f;
        if (button == 0 && MathUtil.a(dA, dA2, gearX, gearY, gearSize, gearSize)) {
            this.modalOpen = true;
            return true;
        }
        List<EffectMarker.a> list = this.h;
        List<Button> list2 = this.g;
        EffectMarker.a(list, (float) dA, (float) dA2);
        float f = this.k + 1.75f + (this.i * 59.5f);
        if (MathUtil.a(dA, dA2, f, this.l + 1.75f, 16.0f, 16.0f)) {
            this.j = ((float) dA) - f;
            return true;
        }
        for (Button button2 : list2) {
            if (button2.e() != null && MathUtil.a(dA, dA2, button2.f(), button2.g(), button2.b(), button2.c())) {
                button2.e().run();
                return true;
            }
        }
        return super.mouseClicked(dA, dA2, button);
    }

    @Compile
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (p() || this.modalOpen) {
            return true;
        }
        if (this.j >= 0.0f) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Compile
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (p() || this.modalOpen) {
            return true;
        }
        if (this.j < 0.0f) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        double dA = MathUtil.scale(mouseX, 2);
        float fMethod_15363 = MathHelper.clamp((((((float) dA) - this.j) - this.k) - 1.75f) / 59.5f, 0.0f, 1.0f);
        this.j = -1.0f;
        if (fMethod_15363 < 0.95f) {
            return true;
        }
        Interface.aM_.scheduleStop();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.modalOpen && keyCode == 256) {
            this.modalOpen = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    static {
        NativeMethodLookup.lookup(MainScreen.class, 16);
        a = new float[2];
    }

    public MainScreen() {
        super(Text.empty());
        this.b = new AnimationUtil();
        this.h = new ArrayList();
        this.j = -1.0f;
        this.m = System.currentTimeMillis();
        this.n = !splashPlayed;
        splashPlayed = true;
        if (Interface.aM_.currentScreen instanceof MainScreen) {
            this.b.c(1.0f);
            this.b.d(1.0f);
            this.b.e(1.0f);
        }
        MainMenuConfig cfg = MainMenuConfig.getInstance();
        this.c = new Button(70.0f, 21.0f, cfg.getSingleplayerText(), () -> {
            Interface.aM_.setScreen(new SelectWorldScreen((Screen) null));
        });
        this.d = new Button(70.0f, 21.0f, cfg.getMultiplayerText(), () -> {
            Interface.aM_.setScreen(new MultiplayerScreen((Screen) null));
        });
        this.e = new Button(144.0f, 20.0f, cfg.getAltManagerText(), () -> {
            Interface.aM_.setScreen(new AltScreen());
        });
        this.f = new Button(70.0f, 18.0f, cfg.getSettingsText(), () -> {
            Interface.aM_.setScreen(new OptionsScreen((Screen) null, Interface.aM_.options));
        });
        this.g = List.of(this.c, this.d, this.e, this.f);
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        MainMenuConfig cfg = MainMenuConfig.getInstance();
        this.c.a(cfg.getSingleplayerText());
        this.d.a(cfg.getMultiplayerText());
        this.e.a(cfg.getAltManagerText());
        this.f.a(cfg.getSettingsText());
    }

    public MusicSound getMusic() {
        return MusicType.MENU;
    }

    public void close() {
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public static void a(DrawContext context, int width, int height, int mouseX, int mouseY, float scale) {
        float marginX = width * 0.025f;
        float marginY = height * 0.025f;
        float[] fArr = a;
        fArr[0] = fArr[0] + ((MathHelper.clamp((((mouseX / width) - 0.5f) * 2.0f) * marginX, (-marginX) * 0.9f, marginX * 0.9f) - a[0]) * 0.03f);
        float[] fArr2 = a;
        fArr2[1] = fArr2[1] + ((MathHelper.clamp((((mouseY / height) - 0.5f) * 2.0f) * marginY, (-marginY) * 0.9f, marginY * 0.9f) - a[1]) * 0.03f);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(width / 2.0f, height / 2.0f, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate((-width) / 2.0f, (-height) / 2.0f, 0.0f);
        Delta.h().d().i().e().a(context.getMatrices());
        MainMenuConfig cfg = MainMenuConfig.getInstance();
        if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DARK) {
            Draw2DProcessor draw = Delta.h().d().i();
            draw.a(context, 0, 0, width, height, ColorUtil.a(12, 13, 17, 255));
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.SHADER) {
            SkyShader.renderMenuBackground(cfg.getShaderBackground(), mouseX, mouseY, width, height);
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.WALLPAPER) {
            Delta.h().d().i().a(matrices, cfg.getWallpaperBackground().getIdentifier(), (-marginX) + a[0], (-marginY) + a[1], width + (marginX * 2.0f), height + (marginY * 2.0f), 0.0f, -1);
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.CUSTOM && cfg.getCustomTextureIdentifier() != null) {
            Delta.h().d().i().a(matrices, cfg.getCustomTextureIdentifier(), (-marginX) + a[0], (-marginY) + a[1], width + (marginX * 2.0f), height + (marginY * 2.0f), 0.0f, -1);
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DESTRUM_V2) {
            renderDestrumV2Background(context, matrices, width, height, mouseX, mouseY, marginX, marginY);
        } else {
            Delta.h().d().i().a(matrices, Identifier.of("delta", "pictures/main.png"), (-marginX) + a[0], (-marginY) + a[1], width + (marginX * 2.0f), height + (marginY * 2.0f), 0.0f, -1);
        }
        matrices.pop();
    }

    public static void renderDestrumV2Background(DrawContext context, MatrixStack matrices, int width, int height, int mouseX, int mouseY, float marginX, float marginY) {
        if (destrumV2StartTime < 0L) {
            destrumV2StartTime = System.currentTimeMillis();
        }
        float time = (float) (System.currentTimeMillis() - destrumV2StartTime) / 1000.0f;

        // Cinematic slow camera drift (gentle breathing + natural motion)
        float driftX = (float) Math.sin(time * 0.16f) * (width * 0.016f) + (float) Math.sin(time * 0.07f) * (width * 0.008f);
        float driftY = (float) Math.cos(time * 0.12f) * (height * 0.012f) + (float) Math.sin(time * 0.05f) * (height * 0.006f);
        float zoomBreath = 1.045f + (float) Math.sin(time * 0.10f) * 0.012f;

        // Smooth parallax interpolation
        destrumV2CameraX += ((a[0] + driftX) - destrumV2CameraX) * 0.05f;
        destrumV2CameraY += ((a[1] + driftY) - destrumV2CameraY) * 0.05f;

        // Draw with expanded margin so no edges are ever visible during drift/zoom
        float extraPadX = marginX * 1.8f;
        float extraPadY = marginY * 1.8f;
        float renderX = (-extraPadX) + destrumV2CameraX;
        float renderY = (-extraPadY) + destrumV2CameraY;
        float renderW = width + (extraPadX * 2.0f);
        float renderH = height + (extraPadY * 2.0f);

        matrices.push();
        matrices.translate(width * 0.5f, height * 0.5f, 0.0f);
        matrices.scale(zoomBreath, zoomBreath, 1.0f);
        matrices.translate(-width * 0.5f, -height * 0.5f, 0.0f);

        Draw2DProcessor draw = Delta.h().d().i();

        // 1. Base pristine 4K sunset artwork
        draw.a(matrices, DESTRUM_V2_TEXTURE, renderX, renderY, renderW, renderH, 0.0f, -1);

        // 2. Atmospheric sun warmth & pulsating glow (sun is located at approx x=0.770, y=0.482)
        float sunX = renderX + (renderW * 0.770f);
        float sunY = renderY + (renderH * 0.482f);
        float sunPulse = (float) (Math.sin(time * 0.75f) * 0.5f + 0.5f);

        float outerRadius = width * 0.36f;
        int outerBloomAlpha = (int) (38.0f + 22.0f * sunPulse);
        draw.a(matrices, BLOOM_TEXTURE, sunX - outerRadius, sunY - outerRadius, outerRadius * 2.0f, outerRadius * 2.0f, 0.0f, ColorUtil.a(255, 170, 55, outerBloomAlpha));

        float innerRadius = width * 0.18f;
        int innerBloomAlpha = (int) (55.0f + 30.0f * sunPulse);
        draw.a(matrices, BLOOM_TEXTURE, sunX - innerRadius, sunY - innerRadius, innerRadius * 2.0f, innerRadius * 2.0f, 0.0f, ColorUtil.a(255, 215, 110, innerBloomAlpha));

        // 3. Cinematic depth grading / vignette
        // Top dusk twilight vignette (subtle deep violet/navy)
        draw.a(matrices, 0.0f, 0.0f, (float) width, height * 0.28f, 0.0f,
            ColorUtil.a(18, 10, 26, 65), ColorUtil.a(18, 10, 26, 65),
            ColorUtil.a(18, 10, 26, 0), ColorUtil.a(18, 10, 26, 0));

        // Bottom grounding vignette (deep warm obsidian to contrast main menu buttons)
        draw.a(matrices, 0.0f, height * 0.60f, (float) width, height * 0.40f, 0.0f,
            ColorUtil.a(10, 8, 14, 0), ColorUtil.a(10, 8, 14, 0),
            ColorUtil.a(10, 8, 14, 115), ColorUtil.a(10, 8, 14, 115));

        // Left mountain depth shade
        draw.a(matrices, 0.0f, 0.0f, width * 0.28f, (float) height, 0.0f,
            ColorUtil.a(12, 8, 16, 45), ColorUtil.a(12, 8, 16, 0),
            ColorUtil.a(12, 8, 16, 65), ColorUtil.a(12, 8, 16, 0));

        matrices.pop();
    }

    private void a(int width, int height) {
        float gap = 4.0f;
        float mainY = (height - this.c.c()) / 2.0f;
        float mainX = (((width - this.c.b()) - gap) - this.d.b()) / 2.0f;
        this.c.a(mainX, mainY);
        this.d.a(mainX + this.c.b() + gap, mainY);
        this.e.a((width - this.e.b()) / 2.0f, mainY + this.c.c() + gap);
        this.k = (width - 79.0f) / 2.0f;
        this.l = height * 0.85f;
        this.f.a((width - this.f.b()) / 2.0f, (this.l - this.f.c()) - 5.0f);
    }

    private void a(DrawContext context, float open, int mouseX) {
        float target = this.j >= 0.0f ? MathHelper.clamp((((mouseX - this.j) - this.k) - 1.75f) / 59.5f, 0.0f, 1.0f) : 0.0f;
        this.i += (target - this.i) * 0.25f;
        float scale = 0.85f + (0.15f * EasingList.s.ease(open));
        Draw2DProcessor draw = Delta.h().d().i();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(this.k + 39.5f, this.l + 9.75f, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate((-this.k) - 39.5f, (-this.l) - 9.75f, 0.0f);
        float knobX = this.k + 1.75f + (this.i * 59.5f);
        float knobY = this.l + 1.75f;
        float centerX = knobX + 8.0f;
        float centerY = knobY + 8.0f;
        draw.b(matrices, this.k, this.l, 79.0f, 19.5f, 8.0f, ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.bN), open);
        draw.a(matrices, this.k, this.l, 79.0f, 19.5f, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (15.0f * open)));
        Fonts.e.c(matrices, MainMenuConfig.getInstance().getExitText(), this.k + 9.0f, this.l + 5.75f, 7.0f, ColorUtil.a(ColorUtil.a(220, 80, 80, 255), this.i * open), ((knobX - 3.0f) - this.k) - 9.0f);
        int knob = ColorUtil.a(ColorUtil.a(255, 255, 255, 13), ColorUtil.a(220, 80, 80, 40), this.i);
        draw.a(matrices, knobX, knobY, 16.0f, 16.0f, 7.0f, ColorUtil.a(knob, (ColorUtil.b(knob)[3] / 255.0f) * open));
        matrices.push();
        matrices.translate(centerX, centerY, 0.0f);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians((-90.0f) + (180.0f * this.i))));
        matrices.translate(-centerX, -centerY, 0.0f);
        Fonts.a.a(matrices, "c", (centerX - (Fonts.a.a("c", 8.5f) / 2.0f)) + 1.0f, centerY - 4.5f, 8.5f, ColorUtil.a(ColorUtil.a(-1, ColorUtil.a(220, 80, 80, 255), this.i), open));
        matrices.pop();
        matrices.pop();
    }

    private void a(DrawContext context, float centerX, float titleY, float open) {
        float titleWidth = Fonts.e.a("Destrum Client", 12.0f);
        MatrixStack matrices = context.getMatrices();
        float scale = 0.85f + (0.15f * EasingList.s.ease(open));
        matrices.push();
        matrices.translate(centerX, titleY + 8.0f, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-centerX, (-titleY) - 8.0f, 0.0f);
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        Fonts.e.a(matrices, (Text) GradientUtil.a("Destrum Client", primary, 5.0f, 0.5f), centerX - (titleWidth / 2.0f), titleY + 1.5f, 12.0f, 0.0f, open);
        Fonts.e.a(matrices, "1.21.4", centerX - (Fonts.e.a("1.21.4", 12.0f) / 2.0f), titleY + 15.0f, 12.0f, ColorUtil.a(255, 255, 255, (int) (160.0f * open)));

        // Typewriter animated subtitle below 1.21.4
        boolean isEn = MainMenuConfig.getInstance().getLanguage() == MainMenuConfig.Language.ENGLISH;
        this.typewriter.update(isEn);
        String typed = this.typewriter.getCurrentText(isEn);
        float typedSize = 7.0f;
        float typedW = Fonts.c.a(typed, typedSize);
        float typedY = titleY + 31.0f;
        Fonts.c.a(matrices, typed, centerX - (typedW / 2.0f), typedY, typedSize, ColorUtil.a(215, 225, 240, (int) (175.0f * open)));

        boolean cursorBlink = (System.currentTimeMillis() % 800) < 450;
        if (cursorBlink && open > 0.05f) {
            float cursorX = centerX + (typedW / 2.0f) + 1.5f;
            Draw2DProcessor draw = Delta.h().d().i();
            draw.a(matrices, cursorX, typedY, 0.75f, 7.0f, 0.0f, ColorUtil.a(primary, (int) (220.0f * open)));
        }

        matrices.pop();
    }

    private void renderGearButton(DrawContext context, int width, int mouseX, int mouseY, float delta, float open) {
        float gearSize = 20.0f;
        float gearX = width - gearSize - 12.0f;
        float gearY = 12.0f;
        boolean hover = MathUtil.a(mouseX, mouseY, gearX, gearY, gearSize, gearSize);
        this.gearAnimation.a(hover);
        this.gearAnimation.a(0.0f, 1.0f, 0.3f, EasingList.g, delta);
        float gearHover = this.gearAnimation.c();

        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();

        draw.b(matrices, gearX, gearY, gearSize, gearSize, 6.0f, ColorUtil.a(12, 13, 17, InterfaceC0020Opcode.bN), open);
        draw.a(matrices, gearX, gearY, gearSize, gearSize, 6.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) ((15.0f + (gearHover * 35.0f)) * open)));

        float gearCx = gearX + (gearSize / 2.0f);
        float gearCy = gearY + (gearSize / 2.0f);
        matrices.push();
        matrices.translate(gearCx, gearCy, 0.0f);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(gearHover * 45.0f)));
        matrices.translate(-gearCx, -gearCy, 0.0f);
        int gearIconColor = ColorUtil.a(ColorUtil.a(190, 195, 210, (int) (190.0f * open)), ColorUtil.a(primary, (int) (255.0f * open)), gearHover);
        Fonts.a.a(matrices, "%", gearCx - (Fonts.a.a("%", 9.5f) / 2.0f), gearCy - (Fonts.a.a(9.5f) / 2.0f), 9.5f, gearIconColor);
        matrices.pop();
    }

    private void renderSettingsModal(DrawContext context, int width, int height, int mouseX, int mouseY, float delta) {
        this.modalAnimation.a(this.modalOpen);
        this.modalAnimation.a(0.0f, 1.0f, 0.25f, EasingList.g, delta);
        float mAlpha = this.modalAnimation.c();
        if (mAlpha <= 0.005f) {
            return;
        }

        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        MainMenuConfig cfg = MainMenuConfig.getInstance();

        // Screen dimming
        draw.a(matrices, 0.0f, 0.0f, width, height, 0.0f, ColorUtil.a(0, 0, 0, (int) (140.0f * mAlpha)));

        float mWidth = 296.0f;
        float mHeight = 205.0f;
        float mX = (width - mWidth) / 2.0f;
        float mY = (height - mHeight) / 2.0f;
        float mScale = 0.90f + (0.10f * EasingList.s.ease(mAlpha));

        matrices.push();
        float mcX = mX + (mWidth / 2.0f);
        float mcY = mY + (mHeight / 2.0f);
        matrices.translate(mcX, mcY, 0.0f);
        matrices.scale(mScale, mScale, 1.0f);
        matrices.translate(-mcX, -mcY, 0.0f);

        // Modal background & border
        draw.b(matrices, mX, mY, mWidth, mHeight, 9.0f, ColorUtil.a(13, 14, 19, 248), mAlpha);
        draw.a(matrices, mX, mY, mWidth, mHeight, 9.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (22.0f * mAlpha)));

        // Header
        Fonts.a.a(matrices, "%", mX + 12.0f, mY + 8.5f, 9.0f, ColorUtil.a(primary, (int) (240.0f * mAlpha)));
        Fonts.d.a(matrices, cfg.getMenuSettingsTitle(), mX + 25.0f, mY + 8.5f, 8.0f, ColorUtil.a(255, 255, 255, (int) (235.0f * mAlpha)));

        // Close button
        float closeX = mX + mWidth - 19.0f;
        float closeY = mY + 7.5f;
        boolean closeHover = MathUtil.a(mouseX, mouseY, closeX - 2.0f, closeY - 2.0f, 15.0f, 15.0f);
        int closeCol = closeHover ? ColorUtil.a(255, 100, 100, (int) (255.0f * mAlpha)) : ColorUtil.a(160, 165, 180, (int) (180.0f * mAlpha));
        Fonts.a.a(matrices, "u", closeX, closeY, 8.0f, closeCol);

        // Separator
        draw.a(matrices, mX + 10.0f, mY + 22.0f, mWidth - 20.0f, 0.5f, 0.0f, ColorUtil.a(255, 255, 255, (int) (14.0f * mAlpha)));

        // 1. Language section
        Fonts.c.a(matrices, cfg.getLanguageLabel(), mX + 12.0f, mY + 27.5f, 6.75f, ColorUtil.a(160, 165, 180, (int) (200.0f * mAlpha)));
        float pY = mY + 38.0f;
        float pW = 75.0f;
        float pH = 15.5f;
        drawPill(matrices, draw, mX + 12.0f, pY, pW, pH, "Русский", cfg.getLanguage() == MainMenuConfig.Language.RUSSIAN, mouseX, mouseY, primary, mAlpha, 6.75f);
        drawPill(matrices, draw, mX + 12.0f + pW + 6.0f, pY, pW, pH, "English", cfg.getLanguage() == MainMenuConfig.Language.ENGLISH, mouseX, mouseY, primary, mAlpha, 6.75f);

        // 2. Background Mode section
        float bgSecY = mY + 59.0f;
        Fonts.c.a(matrices, cfg.getBackgroundLabel(), mX + 12.0f, bgSecY, 6.75f, ColorUtil.a(160, 165, 180, (int) (200.0f * mAlpha)));
        float bgPillY = bgSecY + 10.5f;
        float bgW = 87.0f;
        float bgH = 15.5f;
        float bgGap = 5.0f;
        drawPill(matrices, draw, mX + 12.0f, bgPillY, bgW, bgH, cfg.getBackgroundMode().DEFAULT.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DEFAULT, mouseX, mouseY, primary, mAlpha, 6.5f);
        drawPill(matrices, draw, mX + 12.0f + bgW + bgGap, bgPillY, bgW, bgH, cfg.getBackgroundMode().DESTRUM_V2.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DESTRUM_V2, mouseX, mouseY, primary, mAlpha, 6.5f);
        drawPill(matrices, draw, mX + 12.0f + (bgW + bgGap) * 2.0f, bgPillY, bgW, bgH, cfg.getBackgroundMode().DARK.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DARK, mouseX, mouseY, primary, mAlpha, 6.5f);

        float bgPillY2 = bgPillY + bgH + 3.0f;
        drawPill(matrices, draw, mX + 12.0f, bgPillY2, bgW, bgH, cfg.getBackgroundMode().SHADER.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.SHADER, mouseX, mouseY, primary, mAlpha, 6.5f);
        drawPill(matrices, draw, mX + 12.0f + bgW + bgGap, bgPillY2, bgW, bgH, cfg.getBackgroundMode().WALLPAPER.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.WALLPAPER, mouseX, mouseY, primary, mAlpha, 6.5f);
        drawPill(matrices, draw, mX + 12.0f + (bgW + bgGap) * 2.0f, bgPillY2, bgW, bgH, cfg.getBackgroundMode().CUSTOM.getDisplay(cfg.getLanguage()), cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.CUSTOM, mouseX, mouseY, primary, mAlpha, 6.5f);

        // 3. Dynamic sub-options
        if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.SHADER) {
            Fonts.c.a(matrices, cfg.getShaderSelectionLabel(), mX + 12.0f, mY + 108.5f, 6.5f, ColorUtil.a(160, 165, 180, (int) (200.0f * mAlpha)));
            float sY1 = mY + 119.0f;
            float sW1 = 86.0f;
            float sH1 = 15.0f;
            drawPill(matrices, draw, mX + 12.0f + 0 * 92.0f, sY1, sW1, sH1, MainMenuConfig.ShaderBackground.NEBULA.getDisplay(cfg.getLanguage()), cfg.getShaderBackground() == MainMenuConfig.ShaderBackground.NEBULA, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 1 * 92.0f, sY1, sW1, sH1, MainMenuConfig.ShaderBackground.AURORA.getDisplay(cfg.getLanguage()), cfg.getShaderBackground() == MainMenuConfig.ShaderBackground.AURORA, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 2 * 92.0f, sY1, sW1, sH1, MainMenuConfig.ShaderBackground.STARS.getDisplay(cfg.getLanguage()), cfg.getShaderBackground() == MainMenuConfig.ShaderBackground.STARS, mouseX, mouseY, primary, mAlpha, 6.25f);

            float sY2 = sY1 + sH1 + 3.0f;
            float sW2 = 133.0f;
            drawPill(matrices, draw, mX + 12.0f + 0 * 139.0f, sY2, sW2, sH1, MainMenuConfig.ShaderBackground.PLASMA.getDisplay(cfg.getLanguage()), cfg.getShaderBackground() == MainMenuConfig.ShaderBackground.PLASMA, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 1 * 139.0f, sY2, sW2, sH1, MainMenuConfig.ShaderBackground.NEON.getDisplay(cfg.getLanguage()), cfg.getShaderBackground() == MainMenuConfig.ShaderBackground.NEON, mouseX, mouseY, primary, mAlpha, 6.25f);
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.WALLPAPER) {
            Fonts.c.a(matrices, cfg.getWallpaperSelectionLabel(), mX + 12.0f, mY + 108.5f, 6.5f, ColorUtil.a(160, 165, 180, (int) (200.0f * mAlpha)));
            float wY = mY + 120.0f;
            float wW = 65.0f;
            float wH = 16.0f;
            drawPill(matrices, draw, mX + 12.0f + 0 * 69.0f, wY, wW, wH, MainMenuConfig.WallpaperBackground.COSMIC.getDisplay(cfg.getLanguage()), cfg.getWallpaperBackground() == MainMenuConfig.WallpaperBackground.COSMIC, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 1 * 69.0f, wY, wW, wH, MainMenuConfig.WallpaperBackground.EMERALD.getDisplay(cfg.getLanguage()), cfg.getWallpaperBackground() == MainMenuConfig.WallpaperBackground.EMERALD, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 2 * 69.0f, wY, wW, wH, MainMenuConfig.WallpaperBackground.SUNSET.getDisplay(cfg.getLanguage()), cfg.getWallpaperBackground() == MainMenuConfig.WallpaperBackground.SUNSET, mouseX, mouseY, primary, mAlpha, 6.25f);
            drawPill(matrices, draw, mX + 12.0f + 3 * 69.0f, wY, wW, wH, MainMenuConfig.WallpaperBackground.AURORA.getDisplay(cfg.getLanguage()), cfg.getWallpaperBackground() == MainMenuConfig.WallpaperBackground.AURORA, mouseX, mouseY, primary, mAlpha, 6.25f);

            String wpNote = cfg.getLanguage() == MainMenuConfig.Language.ENGLISH ? "Ultra HD 3840x2160 aesthetic blurred wallpapers" : "Ultra HD 3840x2160 эстетичные обои с размытием";
            Fonts.c.a(matrices, wpNote, mX + 13.0f, mY + 142.5f, 6.25f, ColorUtil.a(150, 160, 180, (int) (180.0f * mAlpha)));
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.CUSTOM) {
            float fBtnY = mY + 115.0f;
            float fBtnX = mX + 12.0f;
            float fBtnW = 125.0f;
            float fBtnH = 17.5f;
            boolean fHover = MathUtil.a(mouseX, mouseY, fBtnX, fBtnY, fBtnW, fBtnH);
            draw.b(matrices, fBtnX, fBtnY, fBtnW, fBtnH, 4.5f, ColorUtil.a(18, 20, 28, 220), mAlpha);
            draw.a(matrices, fBtnX, fBtnY, fBtnW, fBtnH, 4.5f, 0.5f, ColorUtil.a(255, 255, 255, (int) ((fHover ? 30.0f : 15.0f) * mAlpha)));
            Fonts.a.a(matrices, "#", fBtnX + 6.0f, fBtnY + ((fBtnH - 8.0f) / 2.0f), 8.0f, ColorUtil.a(primary, (int) (220.0f * mAlpha)));
            Fonts.c.a(matrices, cfg.getSelectFileText(), fBtnX + 18.0f, (fBtnY + ((fBtnH - 6.75f) / 2.0f)) - 0.5f, 6.75f, ColorUtil.a(240, 240, 245, (int) (230.0f * mAlpha)));

            float rBtnX = fBtnX + fBtnW + 8.0f;
            float rBtnW = 55.0f;
            float rBtnH = 17.5f;
            boolean rHover = MathUtil.a(mouseX, mouseY, rBtnX, fBtnY, rBtnW, rBtnH);
            draw.b(matrices, rBtnX, fBtnY, rBtnW, rBtnH, 4.5f, ColorUtil.a(18, 20, 28, 220), mAlpha);
            draw.a(matrices, rBtnX, fBtnY, rBtnW, rBtnH, 4.5f, 0.5f, ColorUtil.a(255, 255, 255, (int) ((rHover ? 30.0f : 15.0f) * mAlpha)));
            float rTextW = Fonts.c.a(cfg.getResetText(), 6.75f);
            Fonts.c.a(matrices, cfg.getResetText(), rBtnX + ((rBtnW - rTextW) / 2.0f), (fBtnY + ((rBtnH - 6.75f) / 2.0f)) - 0.5f, 6.75f, ColorUtil.a(180, 185, 200, (int) (200.0f * mAlpha)));

            if (cfg.getCustomTextureIdentifier() != null) {
                String fname = new File(cfg.getCustomImagePath()).getName();
                Fonts.a.a(matrices, "m", mX + 13.0f, mY + 139.0f, 7.5f, ColorUtil.a(100, 230, 140, (int) (230.0f * mAlpha)));
                Fonts.c.a(matrices, fname, mX + 24.0f, mY + 139.0f, 6.75f, ColorUtil.a(220, 230, 245, (int) (210.0f * mAlpha)));
            } else {
                Fonts.c.a(matrices, cfg.getNoImageText(), mX + 13.0f, mY + 139.0f, 6.75f, ColorUtil.a(140, 145, 160, (int) (170.0f * mAlpha)));
            }
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DEFAULT) {
            String note = cfg.getLanguage() == MainMenuConfig.Language.ENGLISH ? "Original Destrum Client atmospheric artwork" : "Оригинальный атмосферный арт Destrum Client";
            Fonts.c.a(matrices, note, mX + 13.0f, mY + 124.0f, 6.5f, ColorUtil.a(160, 170, 190, (int) (190.0f * mAlpha)));
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DESTRUM_V2) {
            String note = cfg.getLanguage() == MainMenuConfig.Language.ENGLISH ? "Cinematic Minecraft sunset with dynamic camera and atmospheric lighting" : "Кинематографичный закат Minecraft с динамической камерой и атмосферой";
            Fonts.c.a(matrices, note, mX + 13.0f, mY + 124.0f, 6.5f, ColorUtil.a(160, 170, 190, (int) (190.0f * mAlpha)));
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.DARK) {
            String note = cfg.getLanguage() == MainMenuConfig.Language.ENGLISH ? "Minimalist deep dark OLED background" : "Минималистичный глубокий тёмный OLED фон";
            Fonts.c.a(matrices, note, mX + 13.0f, mY + 124.0f, 6.5f, ColorUtil.a(160, 170, 190, (int) (190.0f * mAlpha)));
        }

        // Close/Done pill
        float doneW = 68.0f;
        float doneH = 16.5f;
        float doneX = mX + ((mWidth - doneW) / 2.0f);
        float doneY = (mY + mHeight) - doneH - 8.5f;
        boolean doneHover = MathUtil.a(mouseX, mouseY, doneX, doneY, doneW, doneH);
        draw.b(matrices, doneX, doneY, doneW, doneH, 4.0f, ColorUtil.a(255, 255, 255, (int) ((doneHover ? 20.0f : 10.0f) * mAlpha)), mAlpha);
        draw.a(matrices, doneX, doneY, doneW, doneH, 4.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (25.0f * mAlpha)));
        String doneLabel = cfg.getLanguage() == MainMenuConfig.Language.ENGLISH ? "Done" : "Готово";
        float doneLabelW = Fonts.c.a(doneLabel, 6.75f);
        Fonts.c.a(matrices, doneLabel, doneX + ((doneW - doneLabelW) / 2.0f), (doneY + ((doneH - 6.75f) / 2.0f)) - 0.5f, 6.75f, ColorUtil.a(240, 240, 245, (int) (230.0f * mAlpha)));

        matrices.pop();
    }

    private void drawPill(MatrixStack matrices, Draw2DProcessor draw, float px, float py, float pw, float ph, String text, boolean selected, int mouseX, int mouseY, int primary, float mAlpha, float fontSize) {
        boolean hover = MathUtil.a(mouseX, mouseY, px, py, pw, ph);
        if (selected) {
            draw.b(matrices, px, py, pw, ph, 4.0f, ColorUtil.a(primary, 0.28f * mAlpha), mAlpha);
            draw.a(matrices, px, py, pw, ph, 4.0f, 0.5f, ColorUtil.a(primary, 0.85f * mAlpha));
            float tw = Fonts.c.a(text, fontSize);
            Fonts.c.a(matrices, text, px + ((pw - tw) / 2.0f), (py + ((ph - fontSize) / 2.0f)) - 0.5f, fontSize, ColorUtil.a(255, 255, 255, (int) (250.0f * mAlpha)));
        } else {
            draw.b(matrices, px, py, pw, ph, 4.0f, ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.bN), mAlpha);
            draw.a(matrices, px, py, pw, ph, 4.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) ((hover ? 25.0f : 12.0f) * mAlpha)));
            float tw = Fonts.c.a(text, fontSize);
            Fonts.c.a(matrices, text, px + ((pw - tw) / 2.0f), (py + ((ph - fontSize) / 2.0f)) - 0.5f, fontSize, ColorUtil.a(160, 165, 180, (int) (190.0f * mAlpha)));
        }
    }

    private boolean handleModalClick(double dA, double dA2, int button) {
        if (!this.modalOpen || button != 0) {
            return false;
        }
        int width = Interface.aM_.getWindow().getScaledWidth();
        int height = Interface.aM_.getWindow().getScaledHeight();
        float mWidth = 296.0f;
        float mHeight = 205.0f;
        float mX = (width - mWidth) / 2.0f;
        float mY = (height - mHeight) / 2.0f;

        MainMenuConfig cfg = MainMenuConfig.getInstance();

        // Close button
        float closeX = mX + mWidth - 19.0f;
        float closeY = mY + 7.5f;
        if (MathUtil.a(dA, dA2, closeX - 4.0f, closeY - 4.0f, 18.0f, 18.0f)) {
            this.modalOpen = false;
            return true;
        }

        // Language pills
        float pY = mY + 38.0f;
        float pW = 75.0f;
        float pH = 15.5f;
        if (MathUtil.a(dA, dA2, mX + 12.0f, pY, pW, pH)) {
            cfg.setLanguage(MainMenuConfig.Language.RUSSIAN);
            updateButtonLabels();
            return true;
        }
        if (MathUtil.a(dA, dA2, mX + 12.0f + pW + 6.0f, pY, pW, pH)) {
            cfg.setLanguage(MainMenuConfig.Language.ENGLISH);
            updateButtonLabels();
            return true;
        }

        // Background pills Row 1: DEFAULT, DESTRUM_V2, DARK
        float bgPillY = mY + 69.5f;
        float bgW = 87.0f;
        float bgH = 15.5f;
        float bgGap = 5.0f;
        if (MathUtil.a(dA, dA2, mX + 12.0f, bgPillY, bgW, bgH)) {
            cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.DEFAULT);
            return true;
        }
        if (MathUtil.a(dA, dA2, mX + 12.0f + bgW + bgGap, bgPillY, bgW, bgH)) {
            cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.DESTRUM_V2);
            return true;
        }
        if (MathUtil.a(dA, dA2, mX + 12.0f + (bgW + bgGap) * 2.0f, bgPillY, bgW, bgH)) {
            cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.DARK);
            return true;
        }

        // Background pills Row 2: SHADER, WALLPAPER, CUSTOM
        float bgPillY2 = bgPillY + bgH + 3.0f;
        if (MathUtil.a(dA, dA2, mX + 12.0f, bgPillY2, bgW, bgH)) {
            cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.SHADER);
            return true;
        }
        if (MathUtil.a(dA, dA2, mX + 12.0f + bgW + bgGap, bgPillY2, bgW, bgH)) {
            cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.WALLPAPER);
            return true;
        }
        if (MathUtil.a(dA, dA2, mX + 12.0f + (bgW + bgGap) * 2.0f, bgPillY2, bgW, bgH)) {
            if (cfg.getCustomTextureIdentifier() != null) {
                cfg.setBackgroundMode(MainMenuConfig.BackgroundMode.CUSTOM);
            } else {
                openFileDialogAsync();
            }
            return true;
        }

        // Sub-options
        if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.SHADER) {
            float sY1 = mY + 119.0f;
            float sW1 = 86.0f;
            float sH1 = 15.0f;
            if (MathUtil.a(dA, dA2, mX + 12.0f + 0 * 92.0f, sY1, sW1, sH1)) {
                cfg.setShaderBackground(MainMenuConfig.ShaderBackground.NEBULA);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 1 * 92.0f, sY1, sW1, sH1)) {
                cfg.setShaderBackground(MainMenuConfig.ShaderBackground.AURORA);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 2 * 92.0f, sY1, sW1, sH1)) {
                cfg.setShaderBackground(MainMenuConfig.ShaderBackground.STARS);
                return true;
            }

            float sY2 = sY1 + sH1 + 3.0f;
            float sW2 = 133.0f;
            if (MathUtil.a(dA, dA2, mX + 12.0f + 0 * 139.0f, sY2, sW2, sH1)) {
                cfg.setShaderBackground(MainMenuConfig.ShaderBackground.PLASMA);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 1 * 139.0f, sY2, sW2, sH1)) {
                cfg.setShaderBackground(MainMenuConfig.ShaderBackground.NEON);
                return true;
            }
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.WALLPAPER) {
            float wY = mY + 120.0f;
            float wW = 65.0f;
            float wH = 16.0f;
            if (MathUtil.a(dA, dA2, mX + 12.0f + 0 * 69.0f, wY, wW, wH)) {
                cfg.setWallpaperBackground(MainMenuConfig.WallpaperBackground.COSMIC);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 1 * 69.0f, wY, wW, wH)) {
                cfg.setWallpaperBackground(MainMenuConfig.WallpaperBackground.EMERALD);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 2 * 69.0f, wY, wW, wH)) {
                cfg.setWallpaperBackground(MainMenuConfig.WallpaperBackground.SUNSET);
                return true;
            }
            if (MathUtil.a(dA, dA2, mX + 12.0f + 3 * 69.0f, wY, wW, wH)) {
                cfg.setWallpaperBackground(MainMenuConfig.WallpaperBackground.AURORA);
                return true;
            }
        } else if (cfg.getBackgroundMode() == MainMenuConfig.BackgroundMode.CUSTOM) {
            float fBtnY = mY + 115.0f;
            float fBtnX = mX + 12.0f;
            float fBtnW = 125.0f;
            float fBtnH = 17.5f;
            if (MathUtil.a(dA, dA2, fBtnX, fBtnY, fBtnW, fBtnH)) {
                openFileDialogAsync();
                return true;
            }
            float rBtnX = fBtnX + fBtnW + 8.0f;
            float rBtnW = 55.0f;
            float rBtnH = 17.5f;
            if (MathUtil.a(dA, dA2, rBtnX, fBtnY, rBtnW, rBtnH)) {
                cfg.resetBackground();
                return true;
            }
        }

        // Done button
        float doneW = 68.0f;
        float doneH = 16.5f;
        float doneX = mX + ((mWidth - doneW) / 2.0f);
        float doneY = (mY + mHeight) - doneH - 8.5f;
        if (MathUtil.a(dA, dA2, doneX, doneY, doneW, doneH)) {
            this.modalOpen = false;
            return true;
        }

        // Click outside modal
        if (!MathUtil.a(dA, dA2, mX, mY, mWidth, mHeight)) {
            this.modalOpen = false;
            return true;
        }

        return true;
    }

    private void openFileDialogAsync() {
        new Thread(() -> {
            try {
                String file = TinyFileDialogs.tinyfd_openFileDialog(
                    "Выберите изображение для фона",
                    "",
                    null,
                    "Изображения (*.png, *.jpg, *.jpeg)",
                    false
                );
                if (file != null && !file.trim().isEmpty()) {
                    File imgFile = new File(file.trim());
                    Interface.aM_.execute(() -> {
                        MainMenuConfig.getInstance().loadCustomImage(imgFile);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Destrum-FilePicker").start();
    }

    private void a(DrawContext context, int width, int height) {
        if (!this.n) {
            return;
        }
        float elapsed = o();
        float fadeIn = MathHelper.clamp(elapsed / SPLASH_FADE_IN, 0.0f, 1.0f);
        float fadeOut = 1.0f - MathHelper.clamp((elapsed - SPLASH_FADE_OUT_AT) / SPLASH_FADE_OUT, 0.0f, 1.0f);
        float alpha = MathHelper.clamp(EasingList.y.ease(fadeIn) * EasingList.y.ease(fadeOut), 0.0f, 1.0f);
        if (alpha <= 0.001f) {
            return;
        }
        Draw2DProcessor draw = Delta.h().d().i();
        MatrixStack matrices = context.getMatrices();
        draw.a(matrices, 0.0f, 0.0f, width, height, 0.0f, ColorUtil.a(0, 0, 0, (int) (200.0f * alpha)));
        float size = 8.75f;
        float x = (width - Fonts.e.a(SPLASH_TEXT, size)) * 0.5f;
        float y = ((height * 0.5f) - 5.0f) + ((1.0f - EasingList.y.ease(fadeIn)) * 6.0f);
        float typed = SPLASH_TEXT.length() * MathHelper.clamp((elapsed - SPLASH_TYPE_START) / SPLASH_TYPE_TIME, 0.0f, 1.0f);
        int shown = Math.min((int) typed, SPLASH_TEXT.length());
        String head = SPLASH_TEXT.substring(0, shown);
        if (shown > 0) {
            Fonts.e.a(matrices, head, x, y, size, ColorUtil.a(255, 255, 255, (int) (255.0f * alpha)));
        }
        if (shown >= SPLASH_TEXT.length()) {
            return;
        }
        float headWidth = Fonts.e.a(head, size);
        float fraction = EasingList.p.ease(typed - shown);
        String next = String.valueOf(SPLASH_TEXT.charAt(shown));
        Fonts.e.a(matrices, next, x + headWidth, y, size, ColorUtil.a(255, 255, 255, (int) (255.0f * alpha * fraction)));
        float blink = (float) ((Math.sin(elapsed / 140.0f) + 1.0d) * 0.5d);
        draw.a(matrices, x + headWidth + (Fonts.e.a(next, size) * fraction), y - 0.5f, 0.7f, size, 0.0f, ColorUtil.a(255, 255, 255, (int) (170.0f * alpha * (0.4f + (0.6f * blink)))));
    }

    private float o() {
        return System.currentTimeMillis() - this.m;
    }

    private boolean q() {
        return !this.n || o() >= SPLASH_FADE_OUT_AT;
    }

    private boolean p() {
        return this.n && o() < SPLASH_FADE_OUT_AT;
    }
}
