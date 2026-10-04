package aethereal.ui.widget;

import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.util.InventoryUtil;
import aethereal.util.MathUtil;
import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;
import aethereal.event.DrawEvent;
import aethereal.module.render.EntityESP;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.ui.element.DragInfo;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class NameTagWidget extends Widget implements Interface {
    private static NameTagWidget INSTANCE;

    public final ModeSetting mode = new ModeSetting("Режим", "Новый 1", "Новый 1", "Стандарт", "Новый 2", "Парящий");
    public final BooleanSetting transparentStyle = new BooleanSetting("Прозрачный стиль", true);
    public final BooleanSetting roundedCorners = new BooleanSetting("Скруглённые углы", true);
    public final BooleanSetting showGHP = new BooleanSetting("Здоровье от голов", true);
    public final BooleanSetting showSkin = new BooleanSetting("Скин", true);
    public final BooleanSetting showArmor = new BooleanSetting("Броня", true);
    public final BooleanSetting colorLowHp = new BooleanSetting("Перекрашивать на низком здоровье", true);
    public final BooleanSetting showHealth = new BooleanSetting("Здоровье", true);
    public final ModeSetting healthStyle = new ModeSetting("Стиль здоровья", "Текст", "Текст", "Кольцо", "Текст и кольцо");

    private float modalX = Float.NaN;
    private float modalY = Float.NaN;
    private final float modalW = 236.0f;
    private final float modalH = 262.0f;

    private boolean draggingScaleSlider = false;
    private boolean draggingOpacitySlider = false;

    public static NameTagWidget getInstance() {
        return INSTANCE;
    }

    public NameTagWidget() {
        super(new DragInfo("NameTags", 160.0f, 100.0f, 142.0f, 15.0f));
        INSTANCE = this;
        j().a(this);
        this.widgetScale.a(Float.valueOf(0.9f));
        this.widgetBgOpacity.a(Float.valueOf(0.85f));
        a(this.mode, this.transparentStyle, this.roundedCorners, this.showGHP, this.showSkin,
          this.showArmor, this.colorLowHp, this.showHealth, this.healthStyle);
    }

    @Override
    public void a(DrawEvent event) {
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());

        boolean inChat = aM_.currentScreen instanceof ChatScreen;
        if (!inChat) {
            return;
        }

        EntityESP esp = EntityESP.getInstance();
        if (esp != null && !this.mode.l(esp.r().c())) {
            this.mode.a(esp.r().c());
        }

        float opacity = getBgOpacity();
        float x = j().a();
        float y = j().b();

        String currentMode = this.mode.c();
        float[] dims;
        if ("Стандарт".equals(currentMode)) {
            dims = renderSampleStandard(event, x, y, opacity);
        } else if ("Новый 2".equals(currentMode)) {
            dims = renderSampleFloatingArmor(event, x, y, opacity);
        } else if ("Парящий".equals(currentMode)) {
            dims = renderSampleFloatingMinimal(event, x, y, opacity);
        } else {
            dims = renderSampleNew1(event, x, y, opacity);
        }

        j().c(dims[0]);
        j().d(dims[1]);
    }

    private float[] renderSampleNew1(DrawEvent event, float x, float y, float opacity) {
        MatrixStack matrices = event.i().getMatrices();
        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 8.0f;

        String name = aM_.player != null ? aM_.player.getName().getString() : "zynacuza";
        String hpText = "20 HP";
        String ghpText = "16 GHP";

        float nameW = Fonts.e.a(name, fontSize);
        boolean hasSkin = this.showSkin.c().booleanValue();
        float skinW = hasSkin ? skinSize : 0.0f;
        float hpW = this.showHealth.c().booleanValue() ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = this.showHealth.c().booleanValue() ? 8.5f : 0.0f;
        float ghpW = this.showGHP.c().booleanValue() ? Fonts.e.a(ghpText, fontSize) : 0.0f;

        int armorCount = this.showArmor.c().booleanValue() ? 4 : 0;
        float armorW = armorCount > 0 ? (armorCount * (itemSize + 1.0f)) : 0.0f;
        float handsW = 2 * (itemSize + 1.0f);

        float totalW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW
                + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f)
                + (ghpW > 0 ? gap + ghpW : 0.0f) + (armorW > 0 ? gap + armorW : 0.0f)
                + gap + handsW + pad;

        boolean rounded = this.roundedCorners.c().booleanValue();
        float radius = rounded ? (h / 2.0f) : 3.0f;

        int bgAlpha = (int) (opacity * (this.transparentStyle.c().booleanValue() ? 175 : 235));
        int bg = ColorUtil.a(16, 17, 23, bgAlpha);
        int outline = ColorUtil.a(255, 255, 255, (int) (opacity * 25));

        event.d().a(matrices, x, y, totalW, h, radius, bg);
        event.d().a(matrices, x, y, totalW, h, radius, 0.5f, outline);

        float curX = x + pad;

        if (skinW > 0) {
            float avatarY = y + ((h - skinSize) / 2.0f);
            EntityESP.drawEntityHead(matrices, event, aM_.player, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = y + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), name, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = this.colorLowHp.c().booleanValue() ? ColorUtil.a(85, 245, 125, 255) : -1;
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = y + ((h - 8.5f) / 2.0f);
            EntityESP.drawHealthBadge(matrices, event, curX, ringY, 20.0f, 20.0f, this.colorLowHp.c().booleanValue(), opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
            curX += ghpW;
        }

        if (armorCount > 0) {
            curX += gap;
            ItemStack[] sampleArmor = {
                new ItemStack(Items.NETHERITE_HELMET),
                new ItemStack(Items.NETHERITE_CHESTPLATE),
                new ItemStack(Items.NETHERITE_LEGGINGS),
                new ItemStack(Items.NETHERITE_BOOTS)
            };
            for (int i = 0; i < sampleArmor.length; i++) {
                ItemStack stack = sampleArmor[i];
                float itemY = y + ((h - itemSize) / 2.0f) - 0.5f;
                event.e().a(event.i(), InventoryUtil.a(stack), curX, itemY, 0, 1.0f, itemSize / 16.0f, false);

                float barY = itemY + itemSize;
                float barW = itemSize - 1.0f;
                float dur = 0.90f - (i * 0.15f);
                event.d().a(matrices, curX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                int barColor = ColorUtil.a(140, 120, 255, 240);
                event.d().a(matrices, curX + 0.5f, barY, barW * dur, 0.9f, 0.45f, barColor);

                curX += itemSize + 1.0f;
            }
        }

        curX += gap;
        ItemStack[] sampleHands = {
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.TOTEM_OF_UNDYING)
        };
        for (ItemStack stack : sampleHands) {
            float itemY = y + ((h - itemSize) / 2.0f) - 0.5f;
            event.e().a(event.i(), InventoryUtil.a(stack), curX, itemY, 0, 1.0f, itemSize / 16.0f, false);
            curX += itemSize + 1.0f;
        }

        return new float[]{totalW, h};
    }

    private float[] renderSampleStandard(DrawEvent event, float x, float y, float opacity) {
        MatrixStack matrices = event.i().getMatrices();
        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 8.5f;

        String name = aM_.player != null ? aM_.player.getName().getString() : "zynacuza";
        String hpText = "20 HP";

        float nameW = Fonts.e.a(name, fontSize);
        boolean hasSkin = this.showSkin.c().booleanValue();
        float skinW = hasSkin ? skinSize : 0.0f;
        float hpW = this.showHealth.c().booleanValue() ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = this.showHealth.c().booleanValue() ? 8.5f : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + pad;

        ItemStack[] sampleItems = {
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.NETHERITE_HELMET),
            new ItemStack(Items.NETHERITE_CHESTPLATE),
            new ItemStack(Items.NETHERITE_LEGGINGS),
            new ItemStack(Items.NETHERITE_BOOTS),
            new ItemStack(Items.TOTEM_OF_UNDYING)
        };

        boolean showArmorItems = this.showArmor.c().booleanValue();
        int itemCount = showArmorItems ? sampleItems.length : 0;
        float itemsW = itemCount > 0 ? (itemCount * (itemSize + 2.0f)) : 0.0f;

        float maxW = Math.max(tagW, itemsW);
        float totalH = showArmorItems ? (itemSize + 3.5f + h) : h;

        float tagX = x + ((maxW - tagW) / 2.0f);
        float tagY = showArmorItems ? (y + itemSize + 3.5f) : y;

        if (showArmorItems) {
            float itemRowY = y;
            float itemStartX = x + ((maxW - itemsW) / 2.0f);
            for (int i = 0; i < sampleItems.length; i++) {
                ItemStack stack = sampleItems[i];
                event.d().a(matrices, itemStartX, itemRowY, itemSize, itemSize, 1.5f, ColorUtil.a(16, 17, 23, (int) (190 * opacity)));
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize - 0.5f;
                    float barW = itemSize - 1.0f;
                    float dur = 0.90f - (i * 0.15f);
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.8f, 0.4f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.8f, 0.4f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.0f;
            }
        }

        boolean rounded = this.roundedCorners.c().booleanValue();
        float radius = rounded ? (h / 2.0f) : 2.5f;
        int bgAlpha = (int) (opacity * (this.transparentStyle.c().booleanValue() ? 175 : 235));
        int bg = ColorUtil.a(16, 17, 23, bgAlpha);
        event.d().a(matrices, tagX, tagY, tagW, h, radius, bg);
        event.d().a(matrices, tagX, tagY, tagW, h, radius, 0.5f, ColorUtil.a(255, 255, 255, (int) (opacity * 25)));

        float curX = tagX + pad;
        if (skinW > 0) {
            float avatarY = tagY + ((h - skinSize) / 2.0f);
            EntityESP.drawEntityHead(matrices, event, aM_.player, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = tagY + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), name, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = this.colorLowHp.c().booleanValue() ? ColorUtil.a(85, 245, 125, 255) : -1;
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = tagY + ((h - 8.5f) / 2.0f);
            EntityESP.drawHealthBadge(matrices, event, curX, ringY, 20.0f, 20.0f, this.colorLowHp.c().booleanValue(), opacity);
        }

        return new float[]{maxW, totalH};
    }

    private float[] renderSampleFloatingArmor(DrawEvent event, float x, float y, float opacity) {
        MatrixStack matrices = event.i().getMatrices();
        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 9.0f;

        String name = aM_.player != null ? aM_.player.getName().getString() : "zynacuza";
        String hpText = "20 HP";
        String ghpText = "16 GHP";

        float nameW = Fonts.e.a(name, fontSize);
        boolean hasSkin = this.showSkin.c().booleanValue();
        float skinW = hasSkin ? skinSize : 0.0f;
        float hpW = this.showHealth.c().booleanValue() ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = this.showHealth.c().booleanValue() ? 8.5f : 0.0f;
        float ghpW = this.showGHP.c().booleanValue() ? Fonts.e.a(ghpText, fontSize) : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + (ghpW > 0 ? gap + ghpW : 0.0f) + pad;

        ItemStack[] floatingItems = {
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.NETHERITE_HELMET),
            new ItemStack(Items.NETHERITE_CHESTPLATE),
            new ItemStack(Items.NETHERITE_LEGGINGS),
            new ItemStack(Items.NETHERITE_BOOTS),
            new ItemStack(Items.TOTEM_OF_UNDYING)
        };

        boolean showArmorItems = this.showArmor.c().booleanValue();
        int itemCount = showArmorItems ? floatingItems.length : 0;
        float itemsW = itemCount > 0 ? (itemCount * (itemSize + 2.5f)) : 0.0f;

        float maxW = Math.max(tagW, itemsW);
        float totalH = showArmorItems ? (itemSize + 4.0f + h) : h;

        float tagX = x + ((maxW - tagW) / 2.0f);
        float tagY = showArmorItems ? (y + itemSize + 4.0f) : y;

        if (showArmorItems) {
            float itemRowY = y;
            float itemStartX = x + ((maxW - itemsW) / 2.0f);
            for (int i = 0; i < floatingItems.length; i++) {
                ItemStack stack = floatingItems[i];
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize;
                    float barW = itemSize - 1.0f;
                    float dur = 0.90f - (i * 0.15f);
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.9f, 0.45f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.5f;
            }
        }

        boolean rounded = this.roundedCorners.c().booleanValue();
        float radius = rounded ? (h / 2.0f) : 3.0f;
        int bgAlpha = (int) (opacity * (this.transparentStyle.c().booleanValue() ? 175 : 235));
        int bg = ColorUtil.a(16, 17, 23, bgAlpha);
        event.d().a(matrices, tagX, tagY, tagW, h, radius, bg);
        event.d().a(matrices, tagX, tagY, tagW, h, radius, 0.5f, ColorUtil.a(255, 255, 255, (int) (opacity * 25)));

        float curX = tagX + pad;
        if (skinW > 0) {
            float avatarY = tagY + ((h - skinSize) / 2.0f);
            EntityESP.drawEntityHead(matrices, event, aM_.player, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = tagY + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), name, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = this.colorLowHp.c().booleanValue() ? ColorUtil.a(85, 245, 125, 255) : -1;
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = tagY + ((h - 8.5f) / 2.0f);
            EntityESP.drawHealthBadge(matrices, event, curX, ringY, 20.0f, 20.0f, this.colorLowHp.c().booleanValue(), opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
        }

        return new float[]{maxW, totalH};
    }

    private float[] renderSampleFloatingMinimal(DrawEvent event, float x, float y, float opacity) {
        MatrixStack matrices = event.i().getMatrices();
        float fontSize = 5.75f;
        float h = 13.0f;
        float pad = 3.5f;
        float gap = 3.5f;
        float skinSize = 9.5f;
        float itemSize = 9.0f;

        String name = aM_.player != null ? aM_.player.getName().getString() : "zynacuza";
        String hpText = "20 HP";
        String ghpText = "16 GHP";

        float nameW = Fonts.e.a(name, fontSize);
        boolean hasSkin = this.showSkin.c().booleanValue();
        float skinW = hasSkin ? skinSize : 0.0f;
        float hpW = this.showHealth.c().booleanValue() ? Fonts.e.a(hpText, fontSize) : 0.0f;
        float badgeW = this.showHealth.c().booleanValue() ? 8.5f : 0.0f;
        float ghpW = this.showGHP.c().booleanValue() ? Fonts.e.a(ghpText, fontSize) : 0.0f;

        float tagW = pad + (skinW > 0 ? skinW + gap : 0.0f) + nameW + (hpW > 0 ? gap + hpW : 0.0f) + (badgeW > 0 ? gap + badgeW : 0.0f) + (ghpW > 0 ? gap + ghpW : 0.0f) + pad;

        ItemStack[] floatingItems = {
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.NETHERITE_HELMET),
            new ItemStack(Items.NETHERITE_CHESTPLATE),
            new ItemStack(Items.NETHERITE_LEGGINGS),
            new ItemStack(Items.NETHERITE_BOOTS),
            new ItemStack(Items.TOTEM_OF_UNDYING)
        };

        boolean showArmorItems = this.showArmor.c().booleanValue();
        int itemCount = showArmorItems ? floatingItems.length : 0;
        float itemsW = itemCount > 0 ? (itemCount * (itemSize + 2.5f)) : 0.0f;

        float maxW = Math.max(tagW, itemsW);
        float totalH = showArmorItems ? (itemSize + 4.0f + h) : h;

        float tagX = x + ((maxW - tagW) / 2.0f);
        float tagY = showArmorItems ? (y + itemSize + 4.0f) : y;

        if (showArmorItems) {
            float itemRowY = y;
            float itemStartX = x + ((maxW - itemsW) / 2.0f);
            for (int i = 0; i < floatingItems.length; i++) {
                ItemStack stack = floatingItems[i];
                event.e().a(event.i(), InventoryUtil.a(stack), itemStartX, itemRowY, 0, 1.0f, itemSize / 16.0f, false);
                if (stack.isDamageable()) {
                    float barY = itemRowY + itemSize;
                    float barW = itemSize - 1.0f;
                    float dur = 0.90f - (i * 0.15f);
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW, 0.9f, 0.45f, ColorUtil.a(25, 25, 30, 180));
                    event.d().a(matrices, itemStartX + 0.5f, barY, barW * dur, 0.9f, 0.45f, ColorUtil.a(140, 120, 255, 240));
                }
                itemStartX += itemSize + 2.5f;
            }
        }

        float curX = tagX + pad;
        if (skinW > 0) {
            float avatarY = tagY + ((h - skinSize) / 2.0f);
            EntityESP.drawEntityHead(matrices, event, aM_.player, curX, avatarY, skinSize, 2.0f, 1.0f);
            curX += skinW + gap;
        }

        float textY = tagY + ((h - Fonts.e.a(fontSize)) / 2.0f) - 0.25f;
        Fonts.e.a(event.h(), name, curX, textY, fontSize, -1);
        curX += nameW;

        if (hpW > 0) {
            curX += gap;
            int hpColor = this.colorLowHp.c().booleanValue() ? ColorUtil.a(85, 245, 125, 255) : -1;
            Fonts.e.a(event.h(), hpText, curX, textY, fontSize, hpColor);
            curX += hpW;
        }

        if (badgeW > 0) {
            curX += gap;
            float ringY = tagY + ((h - 8.5f) / 2.0f);
            EntityESP.drawHealthBadge(matrices, event, curX, ringY, 20.0f, 20.0f, this.colorLowHp.c().booleanValue(), opacity);
            curX += badgeW;
        }

        if (ghpW > 0) {
            curX += gap;
            int ghpColor = ColorUtil.a(255, 215, 0, 255);
            Fonts.e.a(event.h(), ghpText, curX, textY, fontSize, ghpColor);
        }

        return new float[]{maxW, totalH};
    }

    @Override
    protected void b(DrawEvent event) {
        float anim = this.c.c();
        if (anim <= 0.001f) {
            this.modalX = Float.NaN;
            this.modalY = Float.NaN;
            return;
        }

        int screenW = aM_.getWindow().getScaledWidth();
        int screenH = aM_.getWindow().getScaledHeight();

        if (Float.isNaN(this.modalX) || Float.isNaN(this.modalY)) {
            float rawW = j().getRawWidth();
            float defX = j().a() + (rawW / 2.0f) - (this.modalW / 2.0f);
            float defY = j().b() + j().getRawHeight() + 8.0f;
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

        Fonts.e.a(event.h(), "Настройки виджета NameTags", mx + 10.0f, my + 8.0f, 7.5f, ColorUtil.a(-1, anim));
        Fonts.e.a(event.h(), "Настройте стиль виджета по своему вкусу", mx + 10.0f, my + 17.5f, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), anim));

        float btnSize = 14.0f;
        float btnX = (mx + mw) - 10.0f - btnSize;
        float btnY = my + 8.0f;
        event.d().a(matrices, btnX, btnY, btnSize, btnSize, 4.0f, ColorUtil.a(primary, 0.22f * anim));
        Fonts.a.a(matrices, "J", btnX + 3.5f, btnY + 3.0f, 7.5f, ColorUtil.a(primary, anim));

        float cardW = (mw - 26.0f) / 2.0f;
        float cardH = 48.0f;
        float startCardY = my + 28.0f;

        drawStyleCard(event, mx + 10.0f, startCardY, cardW, cardH, "Широкий плоский с...", "Компактная, широкая и сжатая...", "Новый 1", anim, primary);
        drawStyleCard(event, mx + 16.0f + cardW, startCardY, cardW, cardH, "Стопкой", "Просторная, широкая раскладка...", "Стандарт", anim, primary);
        drawStyleCard(event, mx + 10.0f, startCardY + cardH + 4.0f, cardW, cardH, "С парящей бронёй", "Раскладка с парящей бронёй...", "Новый 2", anim, primary);
        drawStyleCard(event, mx + 16.0f + cardW, startCardY + cardH + 4.0f, cardW, cardH, "Парящий стиль", "Минималистичный парящий стиль...", "Парящий", anim, primary);

        float secY = startCardY + (cardH * 2.0f) + 10.0f;
        Fonts.e.a(event.h(), "НАСТРОЙКИ ВИДЖЕТА", mx + 10.0f, secY, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        float chkY = secY + 8.5f;
        float col2X = mx + (mw / 2.0f) + 4.0f;

        drawCheckbox(event, mx + 10.0f, chkY, "Прозрачный стиль", this.transparentStyle.c().booleanValue(), anim, primary);
        drawCheckbox(event, col2X, chkY, "Скруглённые углы", this.roundedCorners.c().booleanValue(), anim, primary);

        drawCheckbox(event, mx + 10.0f, chkY + 11.0f, "Здоровье от голов", this.showGHP.c().booleanValue(), anim, primary);
        drawCheckbox(event, col2X, chkY + 11.0f, "Скин", this.showSkin.c().booleanValue(), anim, primary);

        drawCheckbox(event, mx + 10.0f, chkY + 22.0f, "Броня", this.showArmor.c().booleanValue(), anim, primary);
        drawCheckbox(event, mx + 10.0f, chkY + 33.0f, "Перекрашивать на низком здоровье", this.colorLowHp.c().booleanValue(), anim, primary);
        drawCheckbox(event, mx + 10.0f, chkY + 44.0f, "Здоровье", this.showHealth.c().booleanValue(), anim, primary);

        float sliderY = chkY + 58.0f;
        drawSlider(event, mx + 10.0f, sliderY, mw - 20.0f, "Размер", String.format("%.2fx", getScale()), (getScale() - 0.5f) / 1.0f, anim, primary);
        drawSlider(event, mx + 10.0f, sliderY + 14.5f, mw - 20.0f, "Прозрачность", String.format("%d%%", (int)(getBgOpacity() * 100)), getBgOpacity(), anim, primary);

        float styleSecY = sliderY + 31.0f;
        Fonts.e.a(event.h(), "Стиль отображения здоровья игрока", mx + 10.0f, styleSecY, 5.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), 0.75f * anim));

        float tabY = styleSecY + 7.5f;
        float tabW = (mw - 24.0f) / 3.0f;
        drawTabButton(event, mx + 10.0f, tabY, tabW, 13.5f, "Текст", this.healthStyle.l("Текст"), anim, primary);
        drawTabButton(event, mx + 12.0f + tabW, tabY, tabW, 13.5f, "Кольцо", this.healthStyle.l("Кольцо"), anim, primary);
        drawTabButton(event, mx + 14.0f + (tabW * 2.0f), tabY, tabW, 13.5f, "Текст и кольцо", this.healthStyle.l("Текст и кольцо"), anim, primary);
    }

    private void drawStyleCard(DrawEvent event, float x, float y, float w, float h, String title, String sub, String modeTarget, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        boolean active = this.mode.l(modeTarget);

        int bg = ColorUtil.a(24, 25, 33, (int) (220 * anim));
        int border = active ? ColorUtil.a(primary, 0.95f * anim) : ColorUtil.a(255, 255, 255, (int) (18 * anim));

        event.d().a(matrices, x, y, w, h, 4.0f, bg);
        event.d().a(matrices, x, y, w, h, 4.0f, active ? 0.9f : 0.5f, border);

        float thumbW = w - 6.0f;
        float thumbH = 20.0f;
        float thumbX = x + 3.0f;
        float thumbY = y + 3.0f;
        event.d().a(matrices, thumbX, thumbY, thumbW, thumbH, 3.0f, ColorUtil.a(12, 13, 18, (int) (210 * anim)));

        if ("Новый 1".equals(modeTarget)) {
            float pillW = thumbW - 10.0f;
            float pillH = 7.0f;
            float pillX = thumbX + 5.0f;
            float pillY = thumbY + 6.5f;
            event.d().a(matrices, pillX, pillY, pillW, pillH, 3.5f, ColorUtil.a(35, 36, 48, (int) (230 * anim)));
            event.d().a(matrices, pillX + 1.5f, pillY + 1.5f, 4.0f, 4.0f, 1.0f, ColorUtil.a(primary, anim));
            event.d().a(matrices, pillX + 7.5f, pillY + 2.5f, 18.0f, 2.0f, 0.5f, ColorUtil.a(-1, (int) (200 * anim)));
            event.d().a(matrices, pillX + 28.0f, pillY + 2.5f, 9.0f, 2.0f, 0.5f, ColorUtil.a(120, 255, 140, (int) (220 * anim)));
            event.d().a(matrices, pillX + 39.0f, pillY + 2.5f, 8.0f, 2.0f, 0.5f, ColorUtil.a(255, 215, 0, (int) (220 * anim)));
            event.d().a(matrices, pillX + 50.0f, pillY + 1.5f, 14.0f, 4.0f, 1.0f, ColorUtil.a(140, 120, 255, (int) (200 * anim)));
        } else if ("Стандарт".equals(modeTarget)) {
            float midX = thumbX + (thumbW / 2.0f);
            event.d().a(matrices, midX - 18.0f, thumbY + 3.0f, 36.0f, 4.0f, 1.0f, ColorUtil.a(28, 30, 40, (int) (230 * anim)));
            event.d().a(matrices, midX - 16.0f, thumbY + 9.5f, 32.0f, 6.5f, 1.0f, ColorUtil.a(35, 36, 48, (int) (230 * anim)));
            event.d().a(matrices, midX - 13.0f, thumbY + 11.5f, 14.0f, 2.0f, 0.5f, ColorUtil.a(-1, (int) (200 * anim)));
            event.d().a(matrices, midX + 3.0f, thumbY + 11.5f, 8.0f, 2.0f, 0.5f, ColorUtil.a(120, 255, 140, (int) (220 * anim)));
        } else if ("Новый 2".equals(modeTarget)) {
            float midX = thumbX + (thumbW / 2.0f);
            event.d().a(matrices, midX - 18.0f, thumbY + 3.5f, 36.0f, 3.5f, 1.0f, ColorUtil.a(primary, (int) (200 * anim)));
            event.d().a(matrices, midX - 20.0f, thumbY + 9.5f, 40.0f, 6.5f, 3.25f, ColorUtil.a(35, 36, 48, (int) (230 * anim)));
            event.d().a(matrices, midX - 16.0f, thumbY + 11.5f, 16.0f, 2.0f, 0.5f, ColorUtil.a(-1, (int) (200 * anim)));
            event.d().a(matrices, midX + 2.0f, thumbY + 11.5f, 8.0f, 2.0f, 0.5f, ColorUtil.a(120, 255, 140, (int) (220 * anim)));
        } else {
            float midX = thumbX + (thumbW / 2.0f);
            event.d().a(matrices, midX - 18.0f, thumbY + 4.0f, 36.0f, 3.5f, 1.0f, ColorUtil.a(primary, (int) (180 * anim)));
            event.d().a(matrices, midX - 16.0f, thumbY + 11.0f, 18.0f, 2.0f, 0.5f, ColorUtil.a(-1, (int) (190 * anim)));
            event.d().a(matrices, midX + 4.0f, thumbY + 11.0f, 8.0f, 2.0f, 0.5f, ColorUtil.a(120, 255, 140, (int) (200 * anim)));
        }

        float tagW = Fonts.e.a("v2.0", 5.0f) + 4.0f;
        float tagX = (x + w) - 4.0f - tagW;
        event.d().a(matrices, tagX, y + 26.5f, tagW, 6.5f, 2.0f, ColorUtil.a(primary, 0.25f * anim));
        Fonts.e.a(event.h(), "v2.0", tagX + 2.0f, y + 27.25f, 5.0f, ColorUtil.a(primary, anim));

        Fonts.e.a(event.h(), title, x + 4.0f, y + 26.5f, 5.75f, ColorUtil.a(active ? primary : -1, anim));
        Fonts.e.a(event.h(), sub, x + 4.0f, y + 36.0f, 4.75f, ColorUtil.a(ColorUtil.a(160, 165, 180, 255), 0.75f * anim));
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

    private void drawSlider(DrawEvent event, float x, float y, float w, String label, String valueText, float percent, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        float trackH = 2.5f;
        float trackY = y + 8.5f;

        Fonts.e.a(event.h(), label, x, y, 5.25f, ColorUtil.a(-1, anim));
        float valW = Fonts.e.a(valueText, 5.25f);
        Fonts.e.a(event.h(), valueText, (x + w) - valW, y, 5.25f, ColorUtil.a(primary, anim));

        event.d().a(matrices, x, trackY, w, trackH, 1.25f, ColorUtil.a(30, 32, 42, (int) (230 * anim)));
        float fillW = MathUtil.b(w * percent, 0.0f, w);
        event.d().a(matrices, x, trackY, fillW, trackH, 1.25f, ColorUtil.a(primary, anim));

        float thumbSize = 5.0f;
        float thumbX = MathUtil.b(x + fillW - (thumbSize / 2.0f), x, (x + w) - thumbSize);
        float thumbY = trackY + ((trackH - thumbSize) / 2.0f);
        event.d().a(matrices, thumbX, thumbY, thumbSize, thumbSize, 2.5f, ColorUtil.a(-1, anim));
    }

    private void drawTabButton(DrawEvent event, float x, float y, float w, float h, String text, boolean active, float anim, int primary) {
        MatrixStack matrices = event.i().getMatrices();
        int bg = active ? ColorUtil.a(primary, 0.85f * anim) : ColorUtil.a(25, 27, 35, (int) (220 * anim));
        int border = active ? ColorUtil.a(primary, anim) : ColorUtil.a(255, 255, 255, (int) (20 * anim));

        event.d().a(matrices, x, y, w, h, 3.0f, bg);
        event.d().a(matrices, x, y, w, h, 3.0f, 0.5f, border);

        float tw = Fonts.e.a(text, 5.25f);
        Fonts.e.a(event.h(), text, x + ((w - tw) / 2.0f), y + ((h - Fonts.e.a(5.25f)) / 2.0f), 5.25f, ColorUtil.a(active ? -1 : ColorUtil.a(170, 175, 190, 255), anim));
    }

    @Override
    public boolean handleMouse(double mouseX, double mouseY, int button, int action) {
        if (!g() || !(aM_.currentScreen instanceof ChatScreen) || Float.isNaN(this.modalX)) {
            return false;
        }

        float mx = this.modalX;
        float my = this.modalY;
        float mw = this.modalW;
        float mh = this.modalH;

        boolean inside = MathUtil.a(mouseX, mouseY, mx, my, mw, mh);
        if (!inside && action == 0 && button == 0) {
            j().e().a(false);
            return true;
        }

        if (!inside && !this.draggingScaleSlider && !this.draggingOpacitySlider) {
            return false;
        }

        float cardW = (mw - 26.0f) / 2.0f;
        float cardH = 48.0f;
        float startCardY = my + 28.0f;

        if (action == 0 && button == 0) {
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, startCardY, cardW, cardH)) {
                this.mode.a("Новый 1");
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 16.0f + cardW, startCardY, cardW, cardH)) {
                this.mode.a("Стандарт");
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, startCardY + cardH + 4.0f, cardW, cardH)) {
                this.mode.a("Новый 2");
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 16.0f + cardW, startCardY + cardH + 4.0f, cardW, cardH)) {
                this.mode.a("Парящий");
                syncWithEntityESP();
                return true;
            }

            float secY = startCardY + (cardH * 2.0f) + 10.0f;
            float chkY = secY + 8.5f;
            float col2X = mx + (mw / 2.0f) + 4.0f;
            float chkHitW = (mw / 2.0f) - 10.0f;

            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY, chkHitW, 10.0f)) {
                this.transparentStyle.a(!this.transparentStyle.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, col2X, chkY, chkHitW, 10.0f)) {
                this.roundedCorners.a(!this.roundedCorners.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY + 11.0f, chkHitW, 10.0f)) {
                this.showGHP.a(!this.showGHP.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, col2X, chkY + 11.0f, chkHitW, 10.0f)) {
                this.showSkin.a(!this.showSkin.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY + 22.0f, chkHitW, 10.0f)) {
                this.showArmor.a(!this.showArmor.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY + 33.0f, mw - 20.0f, 10.0f)) {
                this.colorLowHp.a(!this.colorLowHp.c().booleanValue());
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, chkY + 44.0f, chkHitW, 10.0f)) {
                this.showHealth.a(!this.showHealth.c().booleanValue());
                syncWithEntityESP();
                return true;
            }

            float sliderY = chkY + 58.0f;
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

            float styleSecY = sliderY + 31.0f;
            float tabY = styleSecY + 7.5f;
            float tabW = (mw - 24.0f) / 3.0f;
            if (MathUtil.a(mouseX, mouseY, mx + 10.0f, tabY, tabW, 13.5f)) {
                this.healthStyle.a("Текст");
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 12.0f + tabW, tabY, tabW, 13.5f)) {
                this.healthStyle.a("Кольцо");
                syncWithEntityESP();
                return true;
            }
            if (MathUtil.a(mouseX, mouseY, mx + 14.0f + (tabW * 2.0f), tabY, tabW, 13.5f)) {
                this.healthStyle.a("Текст и кольцо");
                syncWithEntityESP();
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
        syncWithEntityESP();
    }

    private void updateOpacityFromMouse(double mouseX, float trackX, float trackW) {
        float pct = MathUtil.b((float) ((mouseX - trackX) / trackW), 0.0f, 1.0f);
        float val = Math.round(pct * 20.0f) / 20.0f;
        this.widgetBgOpacity.a(Float.valueOf(val));
        syncWithEntityESP();
    }

    public void syncWithEntityESP() {
        EntityESP esp = EntityESP.getInstance();
        if (esp != null) {
            esp.syncFromWidget(this);
        }
    }
}
