package aethereal.ui.screen;

import aethereal.config.ThemeInfo;
import aethereal.core.Delta;
import aethereal.notification.Notification;
import aethereal.render.ColorUtil;
import aethereal.render.Draw2DProcessor;
import aethereal.render.Fonts;
import aethereal.render.ScaleUtil;
import aethereal.render.ScissorUtil;
import aethereal.ui.element.TextField;
import aethereal.util.KeyUtil;
import aethereal.util.MathUtil;
import aethereal.waypoint.Waypoint;
import aethereal.waypoint.WaypointIcon;
import aethereal.waypoint.WaypointIconRenderer;
import aethereal.waypoint.WaypointProcessor;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import static aethereal.core.Interface.aM_;

public class WaypointScreen extends Screen {
    private static final float PANEL_WIDTH = 430.0f;
    private static final float PANEL_HEIGHT = 270.0f;

    private final Screen parent;
    private final TextField name = new TextField(TextField.a.GUI_SETTING);
    private final TextField x = new TextField(TextField.a.GUI_SETTING);
    private final TextField y = new TextField(TextField.a.GUI_SETTING);
    private final TextField z = new TextField(TextField.a.GUI_SETTING);
    private WaypointIcon selectedIcon = WaypointIcon.HOME;
    private boolean binding;
    private float scroll;

    public WaypointScreen(Screen parent) {
        super(Text.literal("Метки"));
        this.parent = parent;
        this.name.a("Название метки");
        this.x.a("X");
        this.y.a("Y");
        this.z.a("Z");
        fillPlayerCoordinates();
    }

    @Override
    public void render(DrawContext context, int rawMouseX, int rawMouseY, float delta) {
        super.render(context, rawMouseX, rawMouseY, delta);
        double mouseX = MathUtil.scale(rawMouseX, 2);
        double mouseY = MathUtil.scale(rawMouseY, 2);
        ScaleUtil.a(context, 2);
        float panelX = (aM_.getWindow().getScaledWidth() - PANEL_WIDTH) * 0.5f;
        float panelY = (aM_.getWindow().getScaledHeight() - PANEL_HEIGHT) * 0.5f;
        drawPanel(context, panelX, panelY, (int) mouseX, (int) mouseY, delta);
        ScaleUtil.a(context);
    }

    private void drawPanel(DrawContext context, float panelX, float panelY, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Delta.h().d().i();
        int background = Delta.h().d().o().a(ThemeInfo.BACKGROUND_GUI).a();
        int outline = Delta.h().d().o().a(ThemeInfo.OUTLINE_MEDIUM).a();
        int text = Delta.h().d().o().a(ThemeInfo.TEXT).a();
        int disabled = Delta.h().d().o().a(ThemeInfo.TEXT_DISABLED).a();
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();

        draw.a(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 8.0f, ColorUtil.a(background, 0.97f), 1.0f, ColorUtil.a(background, 0.9f), 18.0f);
        draw.a(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 8.0f, 0.5f, outline);
        draw.a(matrices, panelX, panelY + 31.0f, PANEL_WIDTH, 0.5f, 0.0f, ColorUtil.a(outline, 0.9f));

        Fonts.a.a(matrices, "F", panelX + 11.0f, panelY + 10.0f, 10.0f, primary);
        Fonts.c.a(matrices, "Метки и вайпоинты", panelX + 27.0f, panelY + 9.5f, 9.0f, text);
        String count = manager().waypoints().size() + " меток";
        Fonts.c.a(matrices, count, panelX + PANEL_WIDTH - 31.0f - Fonts.c.a(count, 6.5f), panelY + 11.0f, 6.5f, disabled);
        drawIconButton(matrices, draw, panelX + PANEL_WIDTH - 25.0f, panelY + 7.0f, 18.0f, "c", mouseX, mouseY, text);

        float leftX = panelX + 10.0f;
        float top = panelY + 42.0f;
        float leftWidth = 168.0f;
        draw.a(matrices, leftX, top, leftWidth, 218.0f, 6.0f, ColorUtil.a(primary, 0.025f));
        draw.a(matrices, leftX, top, leftWidth, 218.0f, 6.0f, 0.5f, ColorUtil.a(outline, 0.8f));
        Fonts.c.a(matrices, "НОВАЯ МЕТКА", leftX + 8.0f, top + 8.0f, 6.25f, disabled);

        layoutFields(leftX, top);
        this.name.a(context, mouseX, mouseY, delta, 1.0f);
        this.x.a(context, mouseX, mouseY, delta, 1.0f);
        this.y.a(context, mouseX, mouseY, delta, 1.0f);
        this.z.a(context, mouseX, mouseY, delta, 1.0f);

        Fonts.c.a(matrices, "Иконка", leftX + 8.0f, top + 72.0f, 6.5f, disabled);
        drawIconChoices(context, leftX + 8.0f, top + 83.0f, mouseX, mouseY, primary, outline);
        drawButton(matrices, draw, leftX + 8.0f, top + 112.0f, leftWidth - 16.0f, 20.0f, "Добавить метку", primary, mouseX, mouseY);

        Fonts.c.a(matrices, "Быстрая метка", leftX + 8.0f, top + 145.0f, 6.5f, disabled);
        String bindText = this.binding ? "Нажмите клавишу" : KeyUtil.b(manager().quickMarkerKey());
        drawButton(matrices, draw, leftX + 8.0f, top + 155.0f, leftWidth - 16.0f, 20.0f, bindText, text, mouseX, mouseY);
        Fonts.c.a(matrices, "Автометка смерти", leftX + 8.0f, top + 190.0f, 7.0f, text);
        drawToggle(matrices, draw, leftX + leftWidth - 33.0f, top + 186.0f, manager().deathMarkers(), primary);

        float listX = panelX + 188.0f;
        float listWidth = 232.0f;
        Fonts.c.a(matrices, "ВСЕ МЕТКИ", listX, top + 1.0f, 6.25f, disabled);
        drawWaypointList(context, listX, top + 14.0f, listWidth, 204.0f, mouseX, mouseY, primary, outline, text, disabled);
    }

    private void layoutFields(float leftX, float top) {
        this.name.a(new Vector2f(leftX + 8.0f, top + 20.0f));
        this.name.b(new Vector2f(152.0f, 18.0f));
        float coordinateY = top + 44.0f;
        this.x.a(new Vector2f(leftX + 8.0f, coordinateY));
        this.y.a(new Vector2f(leftX + 60.0f, coordinateY));
        this.z.a(new Vector2f(leftX + 112.0f, coordinateY));
        this.x.b(new Vector2f(48.0f, 18.0f));
        this.y.b(new Vector2f(48.0f, 18.0f));
        this.z.b(new Vector2f(48.0f, 18.0f));
    }

    private void drawIconChoices(DrawContext context, float x, float y, int mouseX, int mouseY, int primary, int outline) {
        Draw2DProcessor draw = Delta.h().d().i();
        for (int i = 0; i < WaypointIcon.values().length; i++) {
            WaypointIcon icon = WaypointIcon.values()[i];
            float itemX = x + (i * 30.0f);
            boolean selected = icon == this.selectedIcon;
            boolean hovered = MathUtil.a(mouseX, mouseY, itemX, y, 24.0f, 24.0f);
            draw.a(context.getMatrices(), itemX, y, 24.0f, 24.0f, 4.0f, ColorUtil.a(primary, selected ? 0.16f : hovered ? 0.08f : 0.025f));
            draw.a(context.getMatrices(), itemX, y, 24.0f, 24.0f, 4.0f, 0.5f, selected ? ColorUtil.a(primary, 0.9f) : outline);
            WaypointIconRenderer.draw(context.getMatrices(), icon, itemX + 4.0f, y + 4.0f, 16.0f, selected ? primary : Delta.h().d().o().a(ThemeInfo.TEXT).a());
        }
    }

    private void drawWaypointList(DrawContext context, float x, float y, float width, float height, int mouseX, int mouseY, int primary, int outline, int text, int disabled) {
        List<Waypoint> waypoints = manager().waypoints();
        Draw2DProcessor draw = Delta.h().d().i();
        MatrixStack matrices = context.getMatrices();
        float contentHeight = waypoints.size() * 31.0f;
        float minScroll = Math.min(0.0f, height - contentHeight);
        this.scroll = MathUtil.b(this.scroll, minScroll, 0.0f);
        ScissorUtil.a(matrices, x, y, width, height);
        if (waypoints.isEmpty()) {
            Fonts.a.a(matrices, "F", x + (width - Fonts.a.a("F", 16.0f)) * 0.5f, y + 73.0f, 16.0f, ColorUtil.a(disabled, 0.7f));
            String empty = "Здесь пока нет меток";
            Fonts.c.a(matrices, empty, x + (width - Fonts.c.a(empty, 7.0f)) * 0.5f, y + 96.0f, 7.0f, disabled);
        }
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);
            float rowY = y + this.scroll + (i * 31.0f);
            if (rowY + 27.0f < y || rowY > y + height) {
                continue;
            }
            boolean hovered = MathUtil.a(mouseX, mouseY, x, rowY, width, 27.0f);
            draw.a(matrices, x, rowY, width, 27.0f, 5.0f, ColorUtil.a(primary, hovered ? 0.07f : 0.025f));
            draw.a(matrices, x, rowY, width, 27.0f, 5.0f, 0.5f, ColorUtil.a(outline, hovered ? 1.0f : 0.65f));
            WaypointIconRenderer.draw(matrices, waypoint.icon(), x + 5.0f, rowY + 5.0f, 16.0f, primary);
            Fonts.c.a(matrices, waypoint.name(), x + 25.0f, rowY + 5.0f, 7.0f, text);
            String coordinates = formatCoordinates(waypoint.position()) + (waypoint.temporary() ? "  ·  10 сек" : "");
            Fonts.c.a(matrices, coordinates, x + 25.0f, rowY + 15.5f, 5.75f, disabled);
            Fonts.a.a(matrices, "c", x + width - 17.0f, rowY + 9.0f, 7.0f, hovered ? ColorUtil.a(230, 85, 85, 255) : disabled);
        }
        ScissorUtil.a(matrices);
    }

    private void drawButton(MatrixStack matrices, Draw2DProcessor draw, float x, float y, float width, float height, String label, int color, int mouseX, int mouseY) {
        boolean hovered = MathUtil.a(mouseX, mouseY, x, y, width, height);
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        draw.a(matrices, x, y, width, height, 5.0f, ColorUtil.a(primary, hovered ? 0.13f : 0.07f));
        draw.a(matrices, x, y, width, height, 5.0f, 0.5f, ColorUtil.a(primary, hovered ? 0.75f : 0.35f));
        Fonts.c.a(matrices, label, x + (width - Fonts.c.a(label, 7.0f)) * 0.5f, y + (height - Fonts.c.a(7.0f)) * 0.5f, 7.0f, color);
    }

    private void drawIconButton(MatrixStack matrices, Draw2DProcessor draw, float x, float y, float size, String icon, int mouseX, int mouseY, int color) {
        boolean hovered = MathUtil.a(mouseX, mouseY, x, y, size, size);
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        draw.a(matrices, x, y, size, size, 5.0f, ColorUtil.a(primary, hovered ? 0.12f : 0.04f));
        Fonts.a.a(matrices, icon, x + (size - Fonts.a.a(icon, 7.0f)) * 0.5f, y + (size - Fonts.a.a(7.0f)) * 0.5f, 7.0f, color);
    }

    private void drawToggle(MatrixStack matrices, Draw2DProcessor draw, float x, float y, boolean enabled, int primary) {
        int background = enabled ? primary : Delta.h().d().o().a(ThemeInfo.TEXT_DISABLED).a();
        draw.a(matrices, x, y, 25.0f, 13.0f, 6.5f, ColorUtil.a(background, enabled ? 0.85f : 0.4f));
        draw.a(matrices, x + (enabled ? 13.5f : 1.5f), y + 1.5f, 10.0f, 10.0f, 5.0f, ColorUtil.a(255, 255, 255, 255));
    }

    @Override
    public boolean mouseClicked(double rawMouseX, double rawMouseY, int button) {
        if (this.binding) {
            manager().setQuickMarkerKey(button >= 0 && button <= 7 ? -100 + button : button);
            this.binding = false;
            return true;
        }
        double mouseX = MathUtil.scale(rawMouseX, 2);
        double mouseY = MathUtil.scale(rawMouseY, 2);
        float panelX = (aM_.getWindow().getScaledWidth() - PANEL_WIDTH) * 0.5f;
        float panelY = (aM_.getWindow().getScaledHeight() - PANEL_HEIGHT) * 0.5f;
        float leftX = panelX + 10.0f;
        float top = panelY + 42.0f;
        layoutFields(leftX, top);
        this.name.a(mouseX, mouseY, button);
        this.x.a(mouseX, mouseY, button);
        this.y.a(mouseX, mouseY, button);
        this.z.a(mouseX, mouseY, button);

        if (MathUtil.a(mouseX, mouseY, panelX + PANEL_WIDTH - 25.0f, panelY + 7.0f, 18.0f, 18.0f)) {
            close();
            return true;
        }
        for (int i = 0; i < WaypointIcon.values().length; i++) {
            if (MathUtil.a(mouseX, mouseY, leftX + 8.0f + (i * 30.0f), top + 83.0f, 24.0f, 24.0f)) {
                this.selectedIcon = WaypointIcon.values()[i];
                return true;
            }
        }
        if (MathUtil.a(mouseX, mouseY, leftX + 8.0f, top + 112.0f, 152.0f, 20.0f)) {
            addWaypoint();
            return true;
        }
        if (MathUtil.a(mouseX, mouseY, leftX + 8.0f, top + 155.0f, 152.0f, 20.0f)) {
            this.binding = true;
            return true;
        }
        if (MathUtil.a(mouseX, mouseY, leftX + 135.0f, top + 186.0f, 25.0f, 16.0f)) {
            manager().setDeathMarkers(!manager().deathMarkers());
            return true;
        }
        List<Waypoint> waypoints = manager().waypoints();
        float listX = panelX + 188.0f;
        float listY = top + 14.0f;
        for (int i = 0; i < waypoints.size(); i++) {
            float rowY = listY + this.scroll + (i * 31.0f);
            if (rowY >= listY && rowY <= listY + 204.0f && MathUtil.a(mouseX, mouseY, listX + 210.0f, rowY, 22.0f, 27.0f)) {
                manager().remove(waypoints.get(i).id());
                return true;
            }
        }
        return super.mouseClicked(rawMouseX, rawMouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double scaledX = MathUtil.scale(mouseX, 2);
        double scaledY = MathUtil.scale(mouseY, 2);
        this.name.b(scaledX, scaledY, button);
        this.x.b(scaledX, scaledY, button);
        this.y.b(scaledX, scaledY, button);
        this.z.b(scaledX, scaledY, button);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll += (float) verticalAmount * 18.0f;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.binding = false;
            } else {
                manager().setQuickMarkerKey(keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
                this.binding = false;
            }
            return true;
        }
        if (anyFieldFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                addWaypoint();
            } else {
                focusedField().a(keyCode, scanCode, modifiers);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (anyFieldFocused()) {
            focusedField().a(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        aM_.setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void addWaypoint() {
        try {
            double px = Double.parseDouble(this.x.g().toString().trim());
            double py = Double.parseDouble(this.y.g().toString().trim());
            double pz = Double.parseDouble(this.z.g().toString().trim());
            manager().add(this.name.g().toString(), new Vec3d(px, py, pz), this.selectedIcon);
            this.name.a();
            fillPlayerCoordinates();
            Delta.h().d().m().a(new Notification("F", "Метка добавлена", 1500));
        } catch (NumberFormatException exception) {
            Delta.h().d().m().a(new Notification("!", ColorUtil.a(230, 85, 85, 255), "Проверьте координаты метки", 1800));
        }
    }

    private void fillPlayerCoordinates() {
        if (aM_.player == null) {
            return;
        }
        setField(this.x, Integer.toString(aM_.player.getBlockX()));
        setField(this.y, Integer.toString(aM_.player.getBlockY()));
        setField(this.z, Integer.toString(aM_.player.getBlockZ()));
    }

    private void setField(TextField field, String value) {
        field.a();
        field.g().append(value);
    }

    private boolean anyFieldFocused() {
        return this.name.j() || this.x.j() || this.y.j() || this.z.j();
    }

    private TextField focusedField() {
        if (this.name.j()) {
            return this.name;
        }
        if (this.x.j()) {
            return this.x;
        }
        if (this.y.j()) {
            return this.y;
        }
        return this.z;
    }

    private WaypointProcessor manager() {
        return Delta.h().d().w();
    }

    private String formatCoordinates(Vec3d position) {
        return (int) position.x + ", " + (int) position.y + ", " + (int) position.z;
    }
}
