package aethereal.ui.element;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.util.MathUtil;

import aethereal.core.Interface;

import aethereal.ui.widget.Widget;
import lombok.Generated;

public class DragInfo implements Interface {
    private Widget b;
    private float c;
    private float d;
    private float e;
    private float f;
    private final String i;
    private double g = 0.0d;
    private double h = 0.0d;
    private int j = 0;

    @Generated
    public void a(Widget widget) {
        this.b = widget;
    }

    @Generated
    public void a(float x) {
        this.c = x;
    }

    @Generated
    public void b(float y) {
        this.d = y;
    }

    @Generated
    public void c(float width) {
        this.e = width;
    }

    @Generated
    public void d(float height) {
        this.f = height;
    }

    @Generated
    public void a(double offsetX) {
        this.g = offsetX;
    }

    @Generated
    public void b(double offsetY) {
        this.h = offsetY;
    }

    @Generated
    public void a(int status) {
        this.j = status;
    }

    @Generated
    public Widget e() {
        return this.b;
    }

    private float renderX = Float.NaN;
    private float renderY = Float.NaN;
    private float dragAnim = 0.0f;

    @Generated
    public float f() {
        float scale = (this.b != null) ? this.b.getScale() : 1.0f;
        return this.e * scale;
    }

    @Generated
    public float g() {
        float scale = (this.b != null) ? this.b.getScale() : 1.0f;
        return this.f * scale;
    }

    public float getRawWidth() {
        return this.e;
    }

    public float getRawHeight() {
        return this.f;
    }

    @Generated
    public double h() {
        return this.g;
    }

    @Generated
    public double i() {
        return this.h;
    }

    @Generated
    public String j() {
        return this.i;
    }

    @Generated
    public int k() {
        return this.j;
    }

    public DragInfo(String name, float x, float y, float width, float height) {
        this.i = name;
        this.c = x;
        this.d = y;
        this.e = width;
        this.f = height;
        Delta.h().d().s().e().add(this);
    }

    public void update(float tickDelta) {
        float maxW = (aM_.getWindow().getFramebufferWidth() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - f();
        float maxH = (aM_.getWindow().getFramebufferHeight() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - g();
        float targetX = MathUtil.b(this.c, 0.0f, Math.max(0.0f, maxW));
        float targetY = MathUtil.b(this.d, 0.0f, Math.max(0.0f, maxH));
        boolean isDragging = Delta.h().d().s() != null && Delta.h().d().s().g() == this;
        this.dragAnim = MathUtil.c(this.dragAnim, isDragging ? 1.0f : 0.0f, 0.25f);
        if (Float.isNaN(this.renderX) || isDragging) {
            this.renderX = targetX;
            this.renderY = targetY;
        } else {
            this.renderX = MathUtil.c(this.renderX, targetX, 0.35f);
            this.renderY = MathUtil.c(this.renderY, targetY, 0.35f);
        }
    }

    public float a() {
        if (Float.isNaN(this.renderX)) {
            float maxW = (aM_.getWindow().getFramebufferWidth() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - f();
            return MathUtil.b(this.c, 0.0f, Math.max(0.0f, maxW));
        }
        return this.renderX;
    }

    public float b() {
        if (Float.isNaN(this.renderY)) {
            float maxH = (aM_.getWindow().getFramebufferHeight() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - g();
            return MathUtil.b(this.d, 0.0f, Math.max(0.0f, maxH));
        }
        return this.renderY;
    }

    public float getRawA() {
        float maxW = (aM_.getWindow().getFramebufferWidth() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - f();
        return MathUtil.b(this.c, 0.0f, Math.max(0.0f, maxW));
    }

    public float getRawB() {
        float maxH = (aM_.getWindow().getFramebufferHeight() / aM_.getWindow().calculateScaleFactor(2, aM_.forcesUnicodeFont())) - g();
        return MathUtil.b(this.d, 0.0f, Math.max(0.0f, maxH));
    }

    public float getDragAnim() {
        return this.dragAnim;
    }

    public float getDragScale() {
        return 1.0f + (0.04f * this.dragAnim);
    }

    public float c() {
        return this.c;
    }

    public float d() {
        return this.d;
    }
}
