package aethereal.ui.widget;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.core.InterfaceC0020Opcode;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.MathUtil;

import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;
import aethereal.core.GlobalEvent;
import aethereal.event.BackendEvent;
import aethereal.event.DrawEvent;
import aethereal.event.PacketEvent;
import aethereal.ui.element.Element_2;
import aethereal.ui.element.SliderElement;
import aethereal.ui.element.ModeElement;

import aethereal.render.AnimationUtil;
import aethereal.ui.element.DragInfo;
import aethereal.setting.Setting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Generated;
import aethereal.setting.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.ChatScreen;

public class Widget {
    private final List<Setting<?>> f = new ObjectArrayList();
    private final List<Element_2<?>> g = new ObjectArrayList();
    protected final AnimationUtil a = new AnimationUtil();
    protected final AnimationUtil b = new AnimationUtil();
    protected final AnimationUtil c = new AnimationUtil();
    private boolean h = false;
    protected float d = 12.5f;
    protected float e = 7.0f;
    private final DragInfo i;
    protected final SliderSetting widgetScale = new SliderSetting("Размер", 1.0f, 0.5f, 1.5f, 0.05f);
    protected final SliderSetting widgetBgOpacity = new SliderSetting("Прозрачность фона", 1.0f, 0.0f, 1.0f, 0.05f);

    public float getScale() {
        return this.widgetScale != null ? this.widgetScale.c().floatValue() : 1.0f;
    }

    public float getBgOpacity() {
        return this.widgetBgOpacity != null ? this.widgetBgOpacity.c().floatValue() : 1.0f;
    }

    @Generated
    public List<Setting<?>> b() {
        return this.f;
    }

    @Generated
    public List<Element_2<?>> c() {
        return this.g;
    }

    @Generated
    public AnimationUtil d() {
        return this.a;
    }

    @Generated
    public AnimationUtil e() {
        return this.b;
    }

    @Generated
    public AnimationUtil f() {
        return this.c;
    }

    @Generated
    public void a(boolean status) {
        this.h = status;
    }

    @Generated
    public boolean g() {
        return this.h;
    }

    @Generated
    public float h() {
        return this.d;
    }

    @Generated
    public float i() {
        return this.e;
    }

    @Generated
    public DragInfo j() {
        return this.i;
    }

    public Widget(DragInfo dragInfo) {
        this.i = dragInfo;
        dragInfo.a(this);
        a(this.widgetScale, this.widgetBgOpacity);
    }

    public boolean handleMouse(double mouseX, double mouseY, int button, int action) {
        return false;
    }

    protected final void a(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.f.add(setting);
            this.g.add(setting.d());
        }
    }

    protected float animatedScale = 1.0f;
    private float popupX = Float.NaN;
    private float popupY = Float.NaN;

    public void render(DrawEvent event) {
        if (this.i != null) {
            this.i.update(event.g());
        }
        float targetScale = getScale();
        this.animatedScale = MathUtil.c(this.animatedScale, targetScale, 0.25f);
        float scale = this.animatedScale;
        boolean transform = Math.abs(scale - 1.0f) > 0.001f;
        MatrixStack matrices = event.i().getMatrices();
        float wx = this.i != null ? this.i.a() : 0.0f;
        float wy = this.i != null ? this.i.b() : 0.0f;
        if (transform) {
            matrices.push();
            matrices.translate(wx, wy, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-wx, -wy, 0.0f);
        }

        a(event);

        if (transform) {
            matrices.pop();
        }

        boolean open = this.h && (Interface.aM_.currentScreen instanceof ChatScreen);
        this.c.a(open);
        this.c.a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        if (this.c.c() > 0.0f) {
            b(event);
        } else {
            this.popupX = Float.NaN;
            this.popupY = Float.NaN;
        }
    }

    public void a(DrawEvent event) {
        e().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
    }

    public void a(GlobalEvent event) {
        e().a(this.i == Delta.h().d().s().g());
    }

    public void a(PacketEvent event) {
    }

    public void a(BackendEvent event) {
    }

    protected void b(DrawEvent event) {
        List<Element_2<?>> visible = this.g.stream().filter(e -> {
            return e.e().e().get().booleanValue();
        }).toList();
        if (!visible.isEmpty()) {
            float panelWidth = ((Float) visible.stream().map(e2 -> {
                float extra = (e2 instanceof SliderElement || e2 instanceof ModeElement) ? 75.0f : 28.0f;
                return Float.valueOf(19.5f + Fonts.e.a(e2.e().i(), 6.5f) + extra);
            }).reduce(Float.valueOf(0.0f), (v0, v1) -> {
                return Math.max(v0, v1);
            })).floatValue();
            panelWidth = Math.max(panelWidth, 115.0f);
            float totalHeight = (12.0f * visible.size()) + (visible.size() - 1);
            float anim = this.c.c() * a();

            if (Delta.h().d().s() != null && Delta.h().d().s().g() == this.i) {
                this.popupX = Float.NaN;
                this.popupY = Float.NaN;
            }

            if (Float.isNaN(this.popupX) || Float.isNaN(this.popupY)) {
                float rawW = this.i.getRawWidth();
                float baseX = (this.i.b() - totalHeight) - 2.0f >= 0.0f ? (this.i.a() + (rawW / 2.0f)) - (panelWidth / 2.0f) : this.i.a() + rawW + 2.0f;
                float baseY = (this.i.b() - totalHeight) - 2.0f >= 0.0f ? (this.i.b() - totalHeight) - 2.0f : this.i.b();
                this.popupX = Math.min(Math.max(baseX, 0.0f), (Interface.aM_.getWindow().getScaledWidth() - panelWidth) - 2.0f);
                this.popupY = Math.min(Math.max(baseY, 0.0f), Interface.aM_.getWindow().getScaledHeight() - totalHeight);
            }

            float baseX2 = this.popupX;
            float baseY2 = this.popupY;

            a(event, baseX2, baseY2, panelWidth, totalHeight, true, anim);
            float y = baseY2;
            for (Element_2<?> element : visible) {
                element.d().set(baseX2, y, panelWidth, 12.0f);
                element.a(event, baseX2, y, panelWidth, anim);
                y += 12.0f + 1.0f;
                if (element != visible.getLast()) {
                    event.d().a(event.i().getMatrices(), baseX2, y - 1.0f, panelWidth, 0.75f, 0.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.2f * anim));
                }
            }
        }
    }

    protected void a(DrawEvent event, String icon, Object title, float width, float animation) {
        a(event, icon, title, width, animation, Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
    }

    protected void a(DrawEvent event, String icon, Object title, float width, float animation, int iconColor) {
        a(event, this.i.a(), this.i.b(), icon, title, width, animation, iconColor);
    }

    protected void a(DrawEvent event, float x, float y, String icon, Object title, float width, float animation, int iconColor) {
        a(event, x, y, icon, null, title, width, animation, iconColor);
    }

    protected void a(DrawEvent event, float x, float y, ItemStack icon, Object title, float width, float animation, int iconColor) {
        a(event, x, y, null, icon, title, width, animation, iconColor);
    }

    private void a(DrawEvent event, float x, float y, String icon, ItemStack stack, Object title, float width, float animation, int iconColor) {
        if (animation > 0.0f) {
            float iconSize = this.e + 1.0f;
            a(event, x, y, width, this.d, true, animation);
            if (stack != null) {
                Delta.h().d().j().a(event.i(), stack, x + 3.0f, (y + ((this.d - 8.0f) / 2.0f)) - 0.25f, 0, animation, 0.5f, false);
            } else {
                Fonts.a.a(event.h(), icon, x + 3.0f, y + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, ColorUtil.a(iconColor, animation));
            }
            a(event, x + 13.5f, y, this.d, animation);
            if (title instanceof Text) {
                Text text = (Text) title;
                Fonts.e.a(event.h(), text, x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, animation);
            } else {
                Fonts.e.a(event.h(), String.valueOf(title), x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, ColorUtil.a(-1, animation));
            }
        }
    }

    protected void a(DrawEvent event, float x, float y, float width, float height, boolean glow, float animation) {
        a(event, x, y, width, height, 5.0f, glow, animation);
    }

    protected void a(DrawEvent event, float x, float y, float width, float height, float radius, boolean glow, float animation) {
        if (animation > 0.0f) {
            ThemeProcessor themeProcessor = Delta.h().d().o();
            float bgMult = getBgOpacity();
            float alpha = themeProcessor.a(ThemeInfo.BACKGROUND_HUD).b() * animation * bgMult;
            int background = ColorUtil.a(themeProcessor.a(ThemeInfo.BACKGROUND_HUD).a(), themeProcessor.a(ThemeInfo.PRIMARY).a(), themeProcessor.a(ThemeInfo.PRIMARY).b() / 6.0f);
            themeProcessor.a(ThemeInfo.BACKGROUND_HUD).e(InterfaceC0020Opcode.cY);
            if (alpha > 0.001f) {
                if (glow) {
                    event.d().a(event.h(), x, y, width, height, radius + (1.0f * this.b.c()), ColorUtil.a(background, alpha), animation * bgMult, ColorUtil.a(background, alpha), 8.0f + (2.0f * this.b.c()));
                } else {
                    event.d().b(event.h(), x, y, width, height, radius, ColorUtil.a(background, alpha), animation * bgMult);
                }
            }
        }
    }

    protected void a(DrawEvent event, float x, float y, float height, float animation) {
        float separatorHeight = height / 2.0f;
        event.d().a(event.i().getMatrices(), x, y + ((height - separatorHeight) / 2.0f), 0.75f, separatorHeight, 0.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.5f * animation));
    }

    public float a() {
        return this.a.c() * (1.0f - (0.1f * this.b.c()));
    }
}
