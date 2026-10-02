package aethereal.ui.element;

import aethereal.core.NativeMethodLookup;
import aethereal.core.Delta;
import aethereal.core.InterfaceC0020Opcode;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.MathUtil;

import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;
import aethereal.event.DrawEvent;
import aethereal.render.Draw2DProcessor;
import aethereal.setting.SliderSetting;
import aethereal.ui.element.Element_2;

import aethereal.api.Compile;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector4f;

public class SliderElement extends Element_2<SliderSetting> {
    private boolean d;

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (vector4f.w <= 14.0f) {
            if (MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, vector4f.w)) {
                if (button == 0) {
                    this.d = true;
                    updatePopupSlider(mouseX);
                    return true;
                } else if (button == 2) {
                    ((SliderSetting) setting).b();
                    return true;
                }
            }
            return false;
        }
        if (!MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y + Fonts.c.a(6.5f), vector4f.z, 11.0f)) {
            return false;
        }
        if (button == 0) {
            this.d = true;
            a(mouseX);
            return true;
        }
        if (button != 2) {
            return false;
        }
        if (!(setting instanceof SliderSetting)) {
            throw new ClassCastException();
        }
        ((SliderSetting) setting).b();
        return true;
    }

    @Override
    @Compile
    public boolean b(double mouseX, double mouseY, int button) {
        this.d = false;
        return false;
    }

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.d) {
            if (this.a.w <= 14.0f) {
                updatePopupSlider(mouseX);
            } else {
                a(mouseX);
            }
            return true;
        }
        return false;
    }

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, double amount) {
        var setting = this.b;
        Vector4f vector4f = this.a;
        if (!(setting instanceof SliderSetting)) {
            throw new ClassCastException();
        }
        SliderSetting sliderSetting = (SliderSetting) setting;
        if (!sliderSetting.e || !MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, ((vector4f.y + Fonts.c.a(6.5f)) + 9.5f) - vector4f.y)) {
            return false;
        }
        Float fC = sliderSetting.c();
        if (!(fC instanceof Float)) {
            throw new ClassCastException();
        }
        sliderSetting.a(Float.valueOf(MathUtil.b(Math.round((fC.floatValue() + (((float) Math.signum(amount)) * sliderSetting.c)) / sliderSetting.c) * sliderSetting.c, sliderSetting.a, sliderSetting.b)));
        return true;
    }

    static {
        NativeMethodLookup.lookup(SliderElement.class, 13);
    }

    public SliderElement(SliderSetting setting) {
        super(setting);
        this.a.w = 22.0f;
    }

    @Override
    public void a(DrawContext context, double mouseX, double mouseY, float delta, float extend) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        this.a.w = 18.0f;
        if (this.d) {
            a(mouseX);
        }
        b().c(MathUtil.c(b().a(), (((SliderSetting) this.b).c().floatValue() - ((SliderSetting) this.b).a) / (((SliderSetting) this.b).b - ((SliderSetting) this.b).a), 1.0f));
        float progress = b().a();
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        float current = ((SliderSetting) this.b).a + ((((SliderSetting) this.b).b - ((SliderSetting) this.b).a) * progress);
        String value = ((SliderSetting) this.b).c % 1.0f == 0.0f ? String.valueOf(Math.round(current)) : String.valueOf(Math.round(current * 100.0f) / 100.0f);
        float boxWidth = Fonts.c.a(value, 6.25f) + 6.0f;
        float boxHeight = Fonts.c.a(6.25f) + 2.0f;
        float boxX = (this.a.x + this.a.z) - boxWidth;
        a(matrices, Fonts.c, ((SliderSetting) this.b).i(), this.a.x, this.a.y + 0.5f, Fonts.c.a(6.5f), 6.5f, theme.a(ThemeInfo.TEXT).a(), (boxX - this.a.x) - 4.0f, hovered, extend, delta);
        draw.a(matrices, boxX, this.a.y, boxWidth, boxHeight, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.03137255f * extend));
        draw.a(matrices, boxX, this.a.y, boxWidth, boxHeight, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * extend));
        Fonts.c.b(matrices, value, boxX + (boxWidth / 2.0f), (this.a.y + ((boxHeight - Fonts.c.a(6.25f)) / 2.0f)) - 0.5f, 6.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), extend));
        float trackY = this.a.y + Fonts.c.a(6.5f) + 6.5f;
        draw.a(matrices, this.a.x, trackY, this.a.z, 3.0f, 0.75f, ColorUtil.a(ColorUtil.a(50, 52, 60, 255), extend * 0.35f));
        draw.a(matrices, this.a.x, trackY, this.a.z * progress, 3.0f, 0.75f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), extend));
        draw.a(matrices, this.a.x + ((this.a.z - 6.0f) * progress), (trackY + 1.5f) - 3.0f, 6.0f, 6.0f, 2.0f, ColorUtil.a(ColorUtil.a(255, 255, 255, 255), extend));
    }

    @Override
    public void a(DrawEvent event, float x, float y, float width, float animation) {
        ThemeProcessor theme = Delta.h().d().o();
        int primary = theme.a(ThemeInfo.PRIMARY).a();

        String name = ((SliderSetting) this.b).i();
        String icon = "g";
        if (name.contains("Размер")) {
            icon = "c";
        } else if (name.contains("Прозрачность")) {
            icon = "o";
        }
        Fonts.a.a(event.h(), icon, x + 5.0f, y + ((12.0f - Fonts.a.a(6.5f)) / 2.0f), 6.5f, ColorUtil.a(primary, animation));

        event.d().a(event.i().getMatrices(), x + 15.5f, y + 3.0f, 0.75f, 6.0f, 0.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.5f * animation));

        float textX = x + 19.5f;
        Fonts.e.a(event.h(), name, textX, (y + ((12.0f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f, 6.5f, ColorUtil.a(-1, animation));

        float current = ((SliderSetting) this.b).c().floatValue();
        float min = ((SliderSetting) this.b).a;
        float max = ((SliderSetting) this.b).b;
        float progress = MathUtil.b((current - min) / (max - min), 0.0f, 1.0f);

        String valText;
        if (name.contains("Прозрачность")) {
            valText = ((int) Math.round(current * 100.0f)) + "%";
        } else if (name.contains("Размер")) {
            valText = String.format(Locale.US, "%.2fx", current);
        } else {
            valText = ((SliderSetting) this.b).c % 1.0f == 0.0f ? String.valueOf(Math.round(current)) : String.format(Locale.US, "%.2f", current);
        }

        float valW = Fonts.e.a(valText, 6.0f);
        float valX = (x + width) - valW - 4.0f;
        Fonts.e.a(event.h(), valText, valX, (y + ((12.0f - Fonts.e.a(6.0f)) / 2.0f)) - 0.5f, 6.0f, ColorUtil.a(-1, 0.8f * animation));

        float trackW = 28.0f;
        float trackH = 2.5f;
        float trackX = valX - trackW - 4.0f;
        float trackY = y + ((12.0f - trackH) / 2.0f);

        event.d().a(event.i().getMatrices(), trackX, trackY, trackW, trackH, 1.25f, ColorUtil.a(ColorUtil.a(50, 52, 60, 255), 0.5f * animation));
        if (progress > 0.01f) {
            event.d().a(event.i().getMatrices(), trackX, trackY, trackW * progress, trackH, 1.25f, ColorUtil.a(primary, animation));
        }

        float knobSize = 5.0f;
        float knobX = (trackX + (trackW * progress)) - (knobSize / 2.0f);
        float knobY = y + ((12.0f - knobSize) / 2.0f);
        event.d().a(event.i().getMatrices(), knobX, knobY, knobSize, knobSize, knobSize / 2.0f, ColorUtil.a(-1, animation));
    }

    private void updatePopupSlider(double mouseX) {
        float trackW = 28.0f;
        float trackX = (this.a.x + this.a.z) - 26.0f - trackW - 4.0f;
        float progress = MathUtil.b(((float) (mouseX - trackX)) / trackW, 0.0f, 1.0f);
        float val = ((SliderSetting) this.b).a + ((((SliderSetting) this.b).b - ((SliderSetting) this.b).a) * progress);
        ((SliderSetting) this.b).a(Float.valueOf(MathUtil.b(Math.round(val / ((SliderSetting) this.b).c) * ((SliderSetting) this.b).c, ((SliderSetting) this.b).a, ((SliderSetting) this.b).b)));
    }

    private void a(double mouseX) {
        float progress = MathUtil.b(((float) (mouseX - ((double) this.a.x))) / this.a.z, 0.0f, 1.0f);
        float value = ((SliderSetting) this.b).a + ((((SliderSetting) this.b).b - ((SliderSetting) this.b).a) * progress);
        ((SliderSetting) this.b).a(Float.valueOf(MathUtil.b(Math.round(value / ((SliderSetting) this.b).c) * ((SliderSetting) this.b).c, ((SliderSetting) this.b).a, ((SliderSetting) this.b).b)));
    }
}
