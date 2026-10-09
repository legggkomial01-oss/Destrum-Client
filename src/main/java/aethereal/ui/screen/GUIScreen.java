package aethereal.ui.screen;

import aethereal.render.ScaleUtil;
import aethereal.core.NativeMethodLookup;
import static aethereal.core.Interface.aM_;
import aethereal.core.Delta;
import aethereal.core.Interface;
import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.render.ColorUtil;
import aethereal.render.Draw2DProcessor;
import aethereal.util.MathUtil;
import aethereal.config.ThemeInfo;
import aethereal.config.ThemeProcessor;

import aethereal.ui.screen.GUIPanel;

import aethereal.render.AnimationUtil;
import aethereal.ui.element.TextField;
import aethereal.api.Compile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import lombok.Generated;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import org.joml.Vector4f;

public class GUIScreen extends Screen {
    private final TextField a;
    private final AnimationUtil b;
    private final List<GUIPanel> c;
    private String d;

    @Compile
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        double dSum = this.c.stream().mapToDouble(new ToDoubleFunction() {
            @Override
            public double applyAsDouble(Object obj) {
                return ((GUIPanel) obj).f().z;
            }
        }).sum();
        float size = (this.c.size() - 1) * 8.0f;
        MinecraftClient class_310Var = Interface.aM_;
        int iMethod_4486 = class_310Var.getWindow().getScaledWidth();
        float f = size + ((float) dSum);
        float f2 = (iMethod_4486 - f) * 0.5f;
        float f3 = f2;
        float f4 = 0.0f;
        for (final GUIPanel gUIPanel : this.c) {
            Vector4f vector4fF = gUIPanel.f();
            gUIPanel.a(Delta.h().d().t().e().stream().filter(obj -> this.a(gUIPanel, (Module) obj)).sorted(Comparator.comparing(new Function() {
                @Override
                public Object apply(Object obj) {
                    return ((Module) obj).j();
                }
            }, String.CASE_INSENSITIVE_ORDER)).toList());
            vector4fF.x = f3;
            vector4fF.y = (class_310Var.getWindow().getScaledHeight() - vector4fF.w) * 0.5f;
            f4 = vector4fF.y;
            gUIPanel.a(context, (int) dA, (int) dA2, delta);
            f3 += vector4fF.z + 8.0f;
        }
        Iterator<GUIPanel> it = this.c.iterator();
        while (it.hasNext()) {
            it.next().a(context, dA, dA2, delta);
        }
        float f5 = ((GUIPanel) this.c.getFirst()).f().w;
        MatrixStack class_4587VarMethod_51448 = context.getMatrices();
        float fC = ((GUIPanel) this.c.getFirst()).b().c();
        float fEase = EasingList.s.ease(fC);
        class_4587VarMethod_51448.push();
        float fEase2 = EasingList.p.ease(fC);
        float f6 = (0.5f * f) + f2;
        float f7 = f5 + f4;
        float f8 = 12.0f + f7 + 10.0f;
        float f9 = ((1.0f - fEase2) * 14.0f) + f8;
        float f10 = (0.15f * fEase) + 0.85f;
        class_4587VarMethod_51448.translate(f6, f9, 0.0f);
        class_4587VarMethod_51448.scale(f10, f10, 1.0f);
        class_4587VarMethod_51448.translate(-f6, -f8, 0.0f);
        a(context, f6, f7, (int) dA, (int) dA2, delta);
        class_4587VarMethod_51448.pop();
        a(context.getMatrices(), f6, f4, delta);
        ScaleUtil.a(context);
    }

    @Compile
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        TextField textField = this.a;
        List<GUIPanel> list = this.c;
        double scaledX = MathUtil.scale(mouseX, 2);
        double scaledY = MathUtil.scale(mouseY, 2);
        if (button == 0 && isWaypointButton(scaledX, scaledY)) {
            aM_.setScreen(new WaypointScreen(this));
            return true;
        }
        textField.a(scaledX, scaledY, button);
        if (list.stream().filter(obj -> GUIScreen.f((GUIPanel) obj)).anyMatch(obj -> ((GUIPanel) obj).a(MathUtil.scale(mouseX, 2), MathUtil.scale(mouseY, 2), button))) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Compile
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (this.c.stream().filter(obj -> GUIScreen.e((GUIPanel) obj)).anyMatch(obj -> ((GUIPanel) obj).b(MathUtil.scale(mouseX, 2), MathUtil.scale(mouseY, 2), button))) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Compile
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        TextField textField = this.a;
        List<GUIPanel> list = this.c;
        textField.b(MathUtil.scale(mouseX, 2), MathUtil.scale(mouseY, 2), button);
        if (list.stream().filter(obj -> GUIScreen.d((GUIPanel) obj)).anyMatch(obj -> ((GUIPanel) obj).a(MathUtil.scale(mouseX, 2), MathUtil.scale(mouseY, 2), button, MathUtil.scale(deltaX, 2), MathUtil.scale(deltaY, 2)))) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Compile
    public boolean mouseScrolled(final double mouseX, final double mouseY, double horizontalAmount, final double verticalAmount) {
        double scaledX = MathUtil.scale(mouseX, 2);
        double scaledY = MathUtil.scale(mouseY, 2);
        for (GUIPanel panel : this.c) {
            if (panel.d() == null) {
                continue;
            }
            Vector4f bounds = panel.f();
            if (MathUtil.a(scaledX, scaledY, bounds.x, bounds.y, bounds.z, bounds.w)) {
                return panel.a(scaledX, scaledY, verticalAmount);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Compile
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        TextField textField = this.a;
        List<GUIPanel> list = this.c;
        if (keyCode == 70 && (modifiers & 2) != 0) {
            textField.a(!textField.j());
            return true;
        }
        if (textField.j()) {
            textField.a(keyCode, scanCode, modifiers);
            return true;
        }
        if (list.stream().filter(obj -> GUIScreen.b((GUIPanel) obj)).anyMatch(obj -> ((GUIPanel) obj).a(keyCode, scanCode, modifiers))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Compile
    public boolean charTyped(final char character, final int modifiers) {
        TextField textField = this.a;
        List<GUIPanel> list = this.c;
        if (textField.j()) {
            textField.a(character, modifiers);
            return true;
        }
        if (list.stream().filter(obj -> GUIScreen.a((GUIPanel) obj)).anyMatch(obj -> ((GUIPanel) obj).a(character, modifiers))) {
            return true;
        }
        return super.charTyped(character, modifiers);
    }

    static {
        NativeMethodLookup.lookup(GUIScreen.class, 5);
    }

    @Generated
    public TextField a() {
        return this.a;
    }

    @Generated
    public AnimationUtil b() {
        return this.b;
    }

    @Generated
    public List<GUIPanel> c() {
        return this.c;
    }

    @Generated
    public String d() {
        return this.d;
    }

    public GUIScreen(Text title) {
        super(title);
        this.a = new TextField(TextField.a.GUI);
        this.b = new AnimationUtil();
        this.c = new ArrayList();
        for (Category category : Category.values()) {
            this.c.add(new GUIPanel(category));
        }
        this.a.a("Поиск по модулям");
    }

    public void close() {
        super.close();
        this.c.forEach(panel -> {
            panel.b().c(0.0f);
        });
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private static final Set<String> EXCLUDED_MODULE_NAMES = Set.of(
        "fly", "strafe", "chest stealer", "cheststealer",
        "auto respawn", "autorespawn", "death coords", "deathcoords",
        "fake lags", "fakelags", "open walls", "openwalls",
        "ancient farmer", "ancientfarmer", "apple farmer", "applefarmer",
        "auto warden", "autowarden", "communication", "portal bypass",
        "portalbypass", "server joiner", "serverjoiner", "x ray", "xray"
    );

    private static final String RU_KEYBOARD = "йцукенгшщзхъфывапролджэячсмитьбю.ё";
    private static final String EN_KEYBOARD = "qwertyuiop[]asdfghjkl;'zxcvbnm,./`";

    private static String ruToEn(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = RU_KEYBOARD.indexOf(Character.toLowerCase(c));
            if (idx >= 0 && idx < EN_KEYBOARD.length()) {
                sb.append(EN_KEYBOARD.charAt(idx));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String enToRu(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = EN_KEYBOARD.indexOf(Character.toLowerCase(c));
            if (idx >= 0 && idx < RU_KEYBOARD.length()) {
                sb.append(RU_KEYBOARD.charAt(idx));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public boolean a(GUIPanel panel, Module module) {
        if (module == null || module.j() == null) {
            return false;
        }
        String name = module.j().toLowerCase().trim();
        if (EXCLUDED_MODULE_NAMES.contains(name)) {
            return false;
        }
        if (module.l() != panel.c()) {
            return false;
        }
        String query = this.a.g().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            return true;
        }
        return matchesSearch(module, query);
    }

    private boolean matchesSearch(Module module, String query) {
        String name = module.j().toLowerCase();
        String desc = module.k() != null ? module.k().toLowerCase() : "";
        String cleanName = name.replace(" ", "");
        String cleanQuery = query.replace(" ", "");

        // 1. Direct match on name or description
        if (name.contains(query) || cleanName.contains(cleanQuery)) {
            return true;
        }
        if (desc.contains(query)) {
            return true;
        }

        // 2. Keyboard layout switch (e.g. user typed Russian on English layout or vice-versa)
        String queryEn = ruToEn(query);
        String cleanQueryEn = queryEn.replace(" ", "");
        if (name.contains(queryEn) || cleanName.contains(cleanQueryEn)) {
            return true;
        }
        String queryRu = enToRu(query);
        if (desc.contains(queryRu)) {
            return true;
        }

        // 3. Common Russian/slang aliases:
        // KillAura / Aura
        if ((cleanQuery.contains("кил") || cleanQuery.contains("аур") || cleanQuery.contains("killaura") || cleanQuery.contains("aura")) 
                && (cleanName.contains("killaura") || cleanName.contains("aura"))) {
            return true;
        }
        // Target ESP
        if ((cleanQuery.contains("таргет") || cleanQuery.contains("target")) 
                && cleanName.contains("target")) {
            return true;
        }
        // ESP modules
        if ((cleanQuery.equals("есп") || cleanQuery.equals("эсп") || cleanQuery.equals("esp")) 
                && (cleanName.contains("esp") || desc.contains("esp") || desc.contains("подсвет"))) {
            return true;
        }
        // Jump Circles
        if ((cleanQuery.contains("джамп") || cleanQuery.contains("круг") || cleanQuery.contains("jump") || cleanQuery.contains("circle")) 
                && cleanName.contains("jumpcircles")) {
            return true;
        }
        // Sky Shader
        if ((cleanQuery.contains("скай") || cleanQuery.contains("небо") || cleanQuery.contains("sky")) 
                && cleanName.contains("skyshader")) {
            return true;
        }
        // Hands Shader
        if ((cleanQuery.contains("хэнд") || cleanQuery.contains("рук") || cleanQuery.contains("hand")) 
                && cleanName.contains("handsshader")) {
            return true;
        }
        // Shaders general
        if ((cleanQuery.contains("шейдер") || cleanQuery.contains("shader")) 
                && cleanName.contains("shader")) {
            return true;
        }
        // Velocity
        if ((cleanQuery.contains("велос") || cleanQuery.contains("акб") || cleanQuery.contains("отдач")) 
                && cleanName.contains("velocity")) {
            return true;
        }
        // AntiBot
        if ((cleanQuery.contains("бот") || cleanQuery.contains("антибот")) 
                && cleanName.contains("antibot")) {
            return true;
        }
        // AutoArmor
        if ((cleanQuery.contains("брон") || cleanQuery.contains("армор")) 
                && cleanName.contains("autoarmor")) {
            return true;
        }
        // AutoTotem
        if ((cleanQuery.contains("тотем") || cleanQuery.contains("totem")) 
                && cleanName.contains("autototem")) {
            return true;
        }
        // TriggerBot
        if ((cleanQuery.contains("триг") || cleanQuery.contains("тригер") || cleanQuery.contains("trigger")) 
                && cleanName.contains("triggerbot")) {
            return true;
        }
        // HitBoxes
        if ((cleanQuery.contains("хитбокс") || cleanQuery.contains("hitbox")) 
                && cleanName.contains("hitboxes")) {
            return true;
        }
        // Scaffold
        if ((cleanQuery.contains("скаф") || cleanQuery.contains("мост") || cleanQuery.contains("строит")) 
                && cleanName.contains("scaffold")) {
            return true;
        }
        // FullBright
        if ((cleanQuery.contains("гамм") || cleanQuery.contains("ярк") || cleanQuery.contains("свет")) 
                && cleanName.contains("fullbright")) {
            return true;
        }
        // Cosmetics
        if ((cleanQuery.contains("космет") || cleanQuery.contains("крыл") || cleanQuery.contains("плащ") || cleanQuery.contains("шапк")) 
                && cleanName.contains("cosmetics")) {
            return true;
        }
        // Predictions
        if ((cleanQuery.contains("предикт") || cleanQuery.contains("перл") || cleanQuery.contains("траект")) 
                && cleanName.contains("predictions")) {
            return true;
        }
        // NameTag / Entity ESP
        if ((cleanQuery.contains("nametag") || cleanQuery.contains("нейм") || cleanQuery.contains("тег") || cleanQuery.contains("tag") || cleanQuery.contains("entityesp") || cleanQuery.contains("enety") || cleanQuery.contains("ники") || cleanQuery.contains("ник")) 
                && (cleanName.contains("nametag") || cleanName.contains("entityesp"))) {
            return true;
        }

        return false;
    }

    private void a(DrawContext context, float centerX, float panelBottom, int mouseX, int mouseY, float delta) {
        this.a.b(new Vector2f(100.0f, 20.0f));
        this.a.a(new Vector2f(centerX - 62.0f, panelBottom + 12.0f));
        this.a.a(context, mouseX, mouseY, delta, 1.0f);
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        MatrixStack matrices = context.getMatrices();
        float buttonX = centerX + 44.0f;
        float buttonY = panelBottom + 12.0f;
        boolean hovered = MathUtil.a(mouseX, mouseY, buttonX, buttonY, 20.0f, 20.0f);
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        int background = ColorUtil.a(theme.a(ThemeInfo.BACKGROUND_GUI).a(), primary, hovered ? 0.12f : 0.05f);
        draw.a(matrices, buttonX, buttonY, 20.0f, 20.0f, 6.0f, ColorUtil.a(background, 0.92f));
        Fonts.a.a(matrices, "F", buttonX + ((20.0f - Fonts.a.a("F", 8.0f)) / 2.0f), buttonY + ((20.0f - Fonts.a.a(8.0f)) / 2.0f), 8.0f, hovered ? primary : theme.a(ThemeInfo.TEXT).a());
    }

    private boolean isWaypointButton(double mouseX, double mouseY) {
        if (this.c.isEmpty()) {
            return false;
        }
        GUIPanel first = this.c.getFirst();
        GUIPanel last = this.c.getLast();
        float centerX = (first.f().x + last.f().x + last.f().z) * 0.5f;
        float buttonY = first.f().y + first.f().w + 12.0f;
        return MathUtil.a(mouseX, mouseY, centerX + 44.0f, buttonY, 20.0f, 20.0f);
    }

    private void a(MatrixStack matrices, float centerX, float panelTop, float delta) {
        Delta.h().d().o();
        Module hovered = (Module) this.c.stream().map((v0) -> {
            return v0.e();
        }).filter(module -> {
            return (module == null || module.k() == null || module.k().isEmpty()) ? false : true;
        }).findFirst().orElse(null);
        if (hovered != null && !hovered.k().equals(this.d)) {
            this.d = hovered.k();
            this.b.c(0.0f);
        }
        this.b.a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
        this.b.a(hovered != null);
        float fade = EasingList.p.ease(this.b.c());
        if (fade > 0.0f && this.d != null) {
            float x = centerX - (Fonts.c.a(this.d, 10.0f) / 2.0f);
            float y = ((panelTop - Fonts.c.a(10.0f)) - 8.0f) + ((1.0f - fade) * 4.0f);
            Fonts.c.a(matrices, this.d, x + 0.5f, y + 0.5f, 10.0f, ColorUtil.a(ColorUtil.a(0, 0, 0, 255), 0.5f * fade));
            Fonts.c.a(matrices, this.d, x, y, 10.0f, ColorUtil.a(ColorUtil.a(255, 255, 255, 255), fade));
        }
    }

    public static boolean f(GUIPanel panel) {
        return panel.d() != null;
    }

    public static boolean e(GUIPanel panel) {
        return panel.d() != null;
    }

    public static boolean d(GUIPanel panel) {
        return panel.d() != null;
    }

    public static boolean c(GUIPanel panel) {
        return panel.d() != null;
    }

    public static boolean b(GUIPanel panel) {
        return panel.d() != null;
    }

    public static boolean a(GUIPanel panel) {
        return panel.d() != null;
    }
}
