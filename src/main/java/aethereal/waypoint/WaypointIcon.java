package aethereal.waypoint;

import net.minecraft.util.Identifier;

public enum WaypointIcon {
    HOME("Дом", "home"),
    ENEMY("Враг", "swords"),
    EVENT("Событие", "event"),
    DEATH("Смерть", "skull"),
    POINT("Точка", "pin");

    private final String displayName;
    private final Identifier texture;

    WaypointIcon(String displayName, String texture) {
        this.displayName = displayName;
        this.texture = Identifier.of("delta", "pictures/waypoints/" + texture + ".png");
    }

    public String displayName() {
        return this.displayName;
    }

    public Identifier texture() {
        return this.texture;
    }

}
