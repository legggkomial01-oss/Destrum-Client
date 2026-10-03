package aethereal.cosmetic;

import org.joml.Vector3f;

public enum LocalCosmeticPreset {
    NONE("Отключено", null, 1.0f, new Vector3f()),
    WINGS("Крылья", CosmeticsCategory.WINGS, 0.0625f, new Vector3f(0.0f, -10.0f, 2.8f)),
    CROWN("Корона", CosmeticsCategory.HAT, 0.0625f, new Vector3f(0.0f, -9.5f, 0.0f)),
    HALO("Нимб", CosmeticsCategory.HALO, 0.0625f, new Vector3f(0.0f, -13.0f, 0.0f)),
    HORNS("Рога", CosmeticsCategory.HORNS, 0.0625f, new Vector3f(0.0f, -8.0f, 0.0f)),
    WIZARD_HAT("Шляпа мага", CosmeticsCategory.HAT, 0.0625f, new Vector3f(0.0f, -9.0f, 0.0f));

    public static final String[] NAMES = {
        NONE.displayName,
        WINGS.displayName,
        CROWN.displayName,
        HALO.displayName,
        HORNS.displayName,
        WIZARD_HAT.displayName
    };

    private final String displayName;
    private final CosmeticsCategory category;
    private final float scale;
    private final Vector3f offset;

    LocalCosmeticPreset(String displayName, CosmeticsCategory category, float scale, Vector3f offset) {
        this.displayName = displayName;
        this.category = category;
        this.scale = scale;
        this.offset = offset;
    }

    public String displayName() {
        return this.displayName;
    }

    public CosmeticsCategory category() {
        return this.category;
    }

    public float scale() {
        return this.scale;
    }

    public Vector3f offset() {
        return new Vector3f(this.offset);
    }

    public static LocalCosmeticPreset byDisplayName(String displayName) {
        for (LocalCosmeticPreset preset : values()) {
            if (preset.displayName.equalsIgnoreCase(displayName)) {
                return preset;
            }
        }
        return NONE;
    }
}
