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
import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import aethereal.ui.shader.NoiseShader;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

@ModuleRegister(a = "Hands Shader", b = "Накладывает шейдер на руку от первого лица", c = Category.Render)
public class HandsShader extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Шум", "Шум", "Свечение", "Градиент", "Неон");
    private final BooleanSetting useTheme = new BooleanSetting("Использовать тему", false);
    private final ColorSetting color = new ColorSetting("Цвет", Integer.valueOf(ColorUtil.a(119, 101, 255, 255)));
    private final BooleanSetting gradient = new BooleanSetting("Градиент", false);
    private final ColorSetting vtoroyColor = new ColorSetting("Второй цвет", Integer.valueOf(ColorUtil.a(255, 100, 100, 255)));
    private final SliderSetting speedGradienta = new SliderSetting("Скорость градиента", 1.0f, 0.0f, 5.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("Радиус", 16.0f, 4.0f, 64.0f, 1.0f);
    private final SliderSetting yarkost = new SliderSetting("Яркость", 1.2f, 0.3f, 4.0f, 0.05f);
    private final SliderSetting opacity = new SliderSetting("Непрозрачность", 0.6f, 0.0f, 1.0f, 0.05f);
    private final BooleanSetting vklyuchitAnimaciyu = new BooleanSetting("Включить анимацию", false);
    private final SliderSetting strengthAnimacii = new SliderSetting("Сила анимации", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting sizeAnimacii = new SliderSetting("Скорость анимации", 1.0f, 0.2f, 3.0f, 0.05f);

    public HandsShader() {
        this.color.a(() -> !this.useTheme.c());
        this.gradient.a(() -> !this.mode.l("Градиент"));
        this.vtoroyColor.a(() -> this.gradient.c() || this.mode.l("Градиент") || this.mode.l("Свечение") || this.mode.l("Неон"));
        this.speedGradienta.a(() -> this.gradient.c() || this.mode.l("Градиент") || this.mode.l("Свечение"));
        this.radius.a(() -> this.mode.l("Свечение") || this.mode.l("Неон"));
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

    @EventTarget
    public void onHand(HandEvent event) {
        if (aM_.options.getPerspective() != Perspective.FIRST_PERSON) return;
        NoiseShader shader = Delta.h().d().i().f();
        if (shader == null) return;

        if (event.b()) {
            shader.e();
        }
        if (event.c()) {
            int primaryCol = this.useTheme.c()
                ? Delta.h().d().o().a(ThemeInfo.PRIMARY).a()
                : this.color.c();

            float r1 = (primaryCol >> 16 & 0xFF) / 255.0f;
            float g1 = (primaryCol >> 8 & 0xFF) / 255.0f;
            float b1 = (primaryCol & 0xFF) / 255.0f;
            float alpha = this.opacity.c();

            int secondCol = this.vtoroyColor.c();
            float r2 = (secondCol >> 16 & 0xFF) / 255.0f;
            float g2 = (secondCol >> 8 & 0xFF) / 255.0f;
            float b2 = (secondCol & 0xFF) / 255.0f;
            boolean hasSecond = this.gradient.c() || this.mode.l("Градиент") || this.mode.l("Свечение") || this.mode.l("Неон");
            float a2 = hasSecond ? alpha : 0.0f;

            int modeIndex = 0;
            if (this.mode.l("Свечение")) {
                modeIndex = 1;
            } else if (this.mode.l("Градиент")) {
                modeIndex = 2;
            } else if (this.mode.l("Неон")) {
                modeIndex = 3;
            }

            float brightnessMult = this.yarkost.c();
            float rad = this.radius.c();
            float flameStrength = 0.0f;

            if (this.vklyuchitAnimaciyu.c()) {
                float animSpeed = this.sizeAnimacii.c();
                float animStrength = this.strengthAnimacii.c();
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.003 * animSpeed) * animStrength * 0.25f;
                brightnessMult = Math.max(0.1f, brightnessMult + pulse);
                flameStrength = animStrength;
                rad = Math.max(2.0f, rad * (1.0f + pulse * 0.3f));
            }

            float[] primary = new float[] {
                r1,
                g1,
                b1,
                alpha
            };

            float[] second = new float[] {
                r2,
                g2,
                b2,
                a2
            };

            shader.a(modeIndex, primary, second, rad, brightnessMult, this.speedGradienta.c(), flameStrength);
        }
    }
}
