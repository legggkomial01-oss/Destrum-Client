package aethereal.module.render;

import static aethereal.core.Interface.aM_;
import aethereal.config.ThemeInfo;
import aethereal.core.Category;
import aethereal.core.Delta;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.HandEvent;
import aethereal.render.ColorUtil;
import aethereal.render.PlayerOutlineEffect;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import aethereal.ui.shader.NoiseShader;
import net.minecraft.client.option.Perspective;

@ModuleRegister(a = "Hands Shader", b = "Накладывает шейдер на руку от первого лица", c = Category.Render)
public class HandsShader extends Module {
    private static HandsShader INSTANCE;

    private final ModeSetting mode = new ModeSetting("Режим", "Свечение", "Свечение", "Шум");
    private final BooleanSetting useTheme = new BooleanSetting("Использовать тему", false);
    private final ColorSetting color = new ColorSetting("Цвет", Integer.valueOf(ColorUtil.a(0, 230, 255, 255)));
    private final BooleanSetting gradient = new BooleanSetting("Градиент", false);
    private final ColorSetting vtoroyColor = new ColorSetting("Второй цвет", Integer.valueOf(ColorUtil.a(255, 140, 50, 255)));
    private final SliderSetting speedGradienta = new SliderSetting("Скорость градиента", 1.0f, 0.0f, 5.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("Радиус", 16.0f, 4.0f, 64.0f, 1.0f);
    private final SliderSetting yarkost = new SliderSetting("Яркость", 1.5f, 0.3f, 4.0f, 0.05f);
    private final SliderSetting opacity = new SliderSetting("Непрозрачность", 1.0f, 0.0f, 1.0f, 0.05f);
    private final BooleanSetting vklyuchitAnimaciyu = new BooleanSetting("Включить анимацию", false);
    private final SliderSetting strengthAnimacii = new SliderSetting("Сила анимации", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting sizeAnimacii = new SliderSetting("Скорость анимации", 1.0f, 0.2f, 3.0f, 0.05f);

    public static HandsShader getInstance() {
        return INSTANCE;
    }

    public HandsShader() {
        INSTANCE = this;
        this.color.a(() -> !this.useTheme.c());
        this.vtoroyColor.a(this.gradient::c);
        this.speedGradienta.a(this.gradient::c);
        this.radius.a(() -> this.mode.l("Свечение"));
        this.strengthAnimacii.a(this.vklyuchitAnimaciyu::c);
        this.sizeAnimacii.a(this.vklyuchitAnimaciyu::c);

        a(
            this.mode,
            this.useTheme,
            this.color,
            this.gradient,
            this.vtoroyColor,
            this.speedGradienta,
            this.radius,
            this.yarkost,
            this.opacity,
            this.vklyuchitAnimaciyu,
            this.strengthAnimacii,
            this.sizeAnimacii
        );
    }

    public boolean isGlowMode() {
        return this.mode.l("Свечение");
    }

    public boolean getVklyuchitAnimaciyu() {
        return this.vklyuchitAnimaciyu.c().booleanValue();
    }

    public boolean getGradient() {
        return this.gradient.c().booleanValue();
    }

    public float getRadius() {
        return this.radius.c().floatValue();
    }

    public float getSizeAnimacii() {
        return this.sizeAnimacii.c().floatValue();
    }

    public float getColorR() {
        int c = this.useTheme.c().booleanValue() ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : this.color.c().intValue();
        return ((c >> 16) & 0xFF) / 255.0f;
    }

    public float getColorG() {
        int c = this.useTheme.c().booleanValue() ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : this.color.c().intValue();
        return ((c >> 8) & 0xFF) / 255.0f;
    }

    public float getColorB() {
        int c = this.useTheme.c().booleanValue() ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : this.color.c().intValue();
        return (c & 0xFF) / 255.0f;
    }

    public float getColorA() {
        int c = this.useTheme.c().booleanValue() ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a() : this.color.c().intValue();
        return (((c >> 24) & 0xFF) / 255.0f) * this.opacity.c().floatValue();
    }

    public float getVtoroyColorR() {
        int c = this.vtoroyColor.c().intValue();
        return ((c >> 16) & 0xFF) / 255.0f;
    }

    public float getVtoroyColorG() {
        int c = this.vtoroyColor.c().intValue();
        return ((c >> 8) & 0xFF) / 255.0f;
    }

    public float getVtoroyColorB() {
        int c = this.vtoroyColor.c().intValue();
        return (c & 0xFF) / 255.0f;
    }

    public float getVtoroyColorA() {
        int c = this.vtoroyColor.c().intValue();
        return (((c >> 24) & 0xFF) / 255.0f) * this.opacity.c().floatValue();
    }

    public float getYarkost() {
        return this.yarkost.c().floatValue();
    }

    public float getStrengthAnimacii() {
        return this.strengthAnimacii.c().floatValue();
    }

    public float getSpeedGradienta() {
        return this.speedGradienta.c().floatValue();
    }

    @EventTarget
    public void onHand(HandEvent event) {
        if (aM_.options.getPerspective() != Perspective.FIRST_PERSON) return;

        if (isGlowMode()) {
            if (event.b()) { // PRE
                PlayerOutlineEffect.setHasHandMask(false);
                PlayerOutlineEffect.isFramebuffer(aM_.getFramebuffer());
            } else if (event.c()) { // POST
                if (PlayerOutlineEffect.hasHandMask()) {
                    PlayerOutlineEffect.onHandGlow(this, aM_);
                    PlayerOutlineEffect.setHasHandMask(false);
                }
            }
            return;
        }

        // Legacy Noise mode
        NoiseShader shader = Delta.h().d().i().f();
        if (shader == null) return;

        if (event.b()) {
            shader.e();
        }
        if (event.c()) {
            int primaryCol = this.useTheme.c().booleanValue()
                ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a()
                : this.color.c().intValue();

            float r1 = ((primaryCol >> 16) & 0xFF) / 255.0f;
            float g1 = ((primaryCol >> 8) & 0xFF) / 255.0f;
            float b1 = (primaryCol & 0xFF) / 255.0f;
            float alpha = this.opacity.c().floatValue();

            int secondCol = this.vtoroyColor.c().intValue();
            float r2 = ((secondCol >> 16) & 0xFF) / 255.0f;
            float g2 = ((secondCol >> 8) & 0xFF) / 255.0f;
            float b2 = (secondCol & 0xFF) / 255.0f;
            boolean hasSecond = this.gradient.c().booleanValue();
            float a2 = hasSecond ? alpha : 0.0f;

            float brightnessMult = this.yarkost.c().floatValue();
            float rad = this.radius.c().floatValue();
            float flameStrength = 0.0f;

            if (this.vklyuchitAnimaciyu.c().booleanValue()) {
                float animSpeed = this.sizeAnimacii.c().floatValue();
                float animStrength = this.strengthAnimacii.c().floatValue();
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.003 * animSpeed) * animStrength * 0.25f;
                brightnessMult = Math.max(0.1f, brightnessMult + pulse);
                flameStrength = animStrength;
                rad = Math.max(2.0f, rad * (1.0f + pulse * 0.3f));
            }

            float[] primary = new float[] { r1, g1, b1, alpha };
            float[] second = new float[] { r2, g2, b2, a2 };

            shader.a(0, primary, second, rad, brightnessMult, this.speedGradienta.c().floatValue(), flameStrength);
        }
    }
}
