package aethereal.waypoint;

import aethereal.config.BaseProcessor;
import aethereal.config.ThemeInfo;
import aethereal.core.Delta;
import aethereal.core.EventTarget;
import aethereal.event.DrawEvent;
import aethereal.event.KeyEvent;
import aethereal.event.TickEvent;
import aethereal.notification.Notification;
import aethereal.render.ColorUtil;
import aethereal.render.Fonts;
import aethereal.util.ProjectUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import static aethereal.core.Interface.aM_;

public class WaypointProcessor extends BaseProcessor {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long QUICK_MARKER_LIFETIME = 10_000L;

    private final List<Waypoint> waypoints = new ArrayList<>();
    private final Set<UUID> deadPlayers = new HashSet<>();
    private final File file = new File(aM_.runDirectory, "configs\\general\\waypoints.json");
    private int quickMarkerKey = GLFW.GLFW_KEY_G;
    private boolean deathMarkers = true;
    private ClientWorld lastWorld;

    @Override
    public void setup() {
        load();
    }

    @Override
    public void unSetup() {
        save();
    }

    public List<Waypoint> waypoints() {
        return List.copyOf(this.waypoints);
    }

    public int quickMarkerKey() {
        return this.quickMarkerKey;
    }

    public void setQuickMarkerKey(int key) {
        this.quickMarkerKey = key;
        save();
    }

    public boolean deathMarkers() {
        return this.deathMarkers;
    }

    public void setDeathMarkers(boolean enabled) {
        this.deathMarkers = enabled;
        save();
    }

    public void add(String name, Vec3d position, WaypointIcon icon) {
        if (aM_.world == null) {
            return;
        }
        String normalized = name == null || name.isBlank() ? icon.displayName() : name.trim();
        if (normalized.length() > 24) {
            normalized = normalized.substring(0, 24);
        }
        this.waypoints.add(new Waypoint(UUID.randomUUID(), normalized, position, icon, dimension(), 0L));
        save();
    }

    public void remove(UUID id) {
        if (this.waypoints.removeIf(waypoint -> waypoint.id().equals(id))) {
            save();
        }
    }

    public void addQuickMarker() {
        if (aM_.player == null || aM_.world == null) {
            return;
        }
        HitResult target = aM_.crosshairTarget;
        Vec3d position;
        if (target instanceof EntityHitResult entityHit) {
            position = entityHit.getEntity().getPos().add(0.0d, entityHit.getEntity().getHeight() + 0.35d, 0.0d);
        } else if (target != null && target.getType() == HitResult.Type.BLOCK) {
            position = target.getPos().add(0.0d, 0.35d, 0.0d);
        } else {
            HitResult longRange = aM_.player.raycast(128.0d, aM_.getRenderTickCounter().getTickDelta(false), false);
            position = longRange.getPos().add(0.0d, 0.35d, 0.0d);
        }
        this.waypoints.removeIf(Waypoint::temporary);
        this.waypoints.add(new Waypoint(UUID.randomUUID(), "Быстрая метка", position, WaypointIcon.POINT, dimension(), System.currentTimeMillis() + QUICK_MARKER_LIFETIME));
        notifyUser("Быстрая метка установлена на 10 секунд");
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if (event.d() == GLFW.GLFW_PRESS && aM_.currentScreen == null && this.quickMarkerKey != GLFW.GLFW_KEY_UNKNOWN && event.b() == this.quickMarkerKey) {
            addQuickMarker();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        long now = System.currentTimeMillis();
        this.waypoints.removeIf(waypoint -> waypoint.expired(now));
        if (aM_.world == null) {
            this.deadPlayers.clear();
            this.lastWorld = null;
            return;
        }
        if (this.lastWorld != aM_.world) {
            this.lastWorld = aM_.world;
            this.deadPlayers.clear();
        }
        if (!this.deathMarkers) {
            this.deadPlayers.clear();
            return;
        }
        for (AbstractClientPlayerEntity player : aM_.world.getPlayers()) {
            boolean dead = player.isDead() || player.getHealth() <= 0.0f;
            UUID uuid = player.getUuid();
            if (dead && this.deadPlayers.add(uuid)) {
                addDeathMarker(player);
            } else if (!dead) {
                this.deadPlayers.remove(uuid);
            }
        }
    }

    @EventTarget
    public void onDraw(DrawEvent event) {
        if (!event.b() || aM_.player == null || aM_.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Vec3d eyes = aM_.player.getEyePos();
        for (Waypoint waypoint : List.copyOf(this.waypoints)) {
            if (!waypoint.expired(now) && waypoint.dimension().equals(dimension())) {
                drawWaypoint(event, waypoint, eyes, now);
            }
        }
    }

    private void addDeathMarker(PlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        String name = player == aM_.player ? "Моя смерть" : "Смерть " + playerName;
        Vec3d position = player.getPos().add(0.0d, 1.0d, 0.0d);
        this.waypoints.add(new Waypoint(UUID.randomUUID(), name, position, WaypointIcon.DEATH, dimension(), 0L));
        save();
        notifyUser("Метка смерти: " + playerName);
    }

    private void drawWaypoint(DrawEvent event, Waypoint waypoint, Vec3d eyes, long now) {
        Vector2f screen = ProjectUtil.a(waypoint.position().x, waypoint.position().y, waypoint.position().z);
        if (!ProjectUtil.a(screen)) {
            return;
        }
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int background = Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).a();
        String distance = String.format(Locale.US, "%.1fм", eyes.distanceTo(waypoint.position()));
        Text text = Text.literal(waypoint.name()).append(Text.literal("  /  ").setStyle(Style.EMPTY.withColor(primary))).append(Text.literal(distance));
        float alpha = waypoint.temporary() ? Math.min(1.0f, Math.max(0.2f, (waypoint.expiresAt() - now) / 1000.0f)) : 1.0f;
        float width = 17.0f + Fonts.e.a(text, 6.25f);
        float x = screen.x() - (width / 2.0f);
        float y = screen.y() - 6.0f;
        event.d().a(event.h(), x, y, width, 12.0f, 3.5f, ColorUtil.a(background, Delta.h().d().o().a(ThemeInfo.BACKGROUND_HUD).b() * alpha), 1.0f, ColorUtil.a(primary, 0.22f * alpha), 7.0f);
        WaypointIconRenderer.draw(event.h(), waypoint.icon(), x + 2.5f, y + 2.2f, 7.5f, ColorUtil.a(primary, alpha));
        Fonts.e.a(event.h(), text, x + 12.5f, y + ((12.0f - Fonts.e.a(6.25f)) / 2.0f) - 0.25f, 6.25f, 0.0f, alpha);
    }

    private String dimension() {
        return aM_.world.getRegistryKey().getValue().toString();
    }

    private void notifyUser(String message) {
        Delta.h().d().m().a(new Notification("F", message, 1800));
    }

    private void load() {
        if (!this.file.exists()) {
            save();
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(this.file.toPath())).getAsJsonObject();
            this.quickMarkerKey = root.has("quickMarkerKey") ? root.get("quickMarkerKey").getAsInt() : GLFW.GLFW_KEY_G;
            this.deathMarkers = !root.has("deathMarkers") || root.get("deathMarkers").getAsBoolean();
            JsonArray markers = root.has("markers") ? root.getAsJsonArray("markers") : new JsonArray();
            for (JsonElement element : markers) {
                JsonObject marker = element.getAsJsonObject();
                this.waypoints.add(new Waypoint(
                        UUID.fromString(marker.get("id").getAsString()),
                        marker.get("name").getAsString(),
                        new Vec3d(marker.get("x").getAsDouble(), marker.get("y").getAsDouble(), marker.get("z").getAsDouble()),
                        WaypointIcon.valueOf(marker.get("icon").getAsString()),
                        marker.get("dimension").getAsString(),
                        0L));
            }
        } catch (Exception ignored) {
            this.waypoints.clear();
        }
    }

    public void save() {
        try {
            File parent = this.file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            JsonObject root = new JsonObject();
            root.addProperty("quickMarkerKey", this.quickMarkerKey);
            root.addProperty("deathMarkers", this.deathMarkers);
            JsonArray markers = new JsonArray();
            for (Waypoint waypoint : this.waypoints) {
                if (waypoint.temporary()) {
                    continue;
                }
                JsonObject marker = new JsonObject();
                marker.addProperty("id", waypoint.id().toString());
                marker.addProperty("name", waypoint.name());
                marker.addProperty("x", waypoint.position().x);
                marker.addProperty("y", waypoint.position().y);
                marker.addProperty("z", waypoint.position().z);
                marker.addProperty("icon", waypoint.icon().name());
                marker.addProperty("dimension", waypoint.dimension());
                markers.add(marker);
            }
            root.add("markers", markers);
            Files.writeString(this.file.toPath(), GSON.toJson(root));
        } catch (Exception ignored) {
        }
    }
}
