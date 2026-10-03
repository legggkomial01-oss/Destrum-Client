package aethereal.module.render;

import static aethereal.core.Interface.aM_;

import aethereal.core.Category;
import aethereal.core.Delta;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.cosmetic.LocalCosmeticPreset;
import aethereal.event.TickEvent;
import aethereal.setting.ModeSetting;
import java.util.UUID;

@ModuleRegister(a = "Cosmetics", b = "Добавляет локальные аксессуары на модель игрока", c = Category.Render)
public class Cosmetics extends Module {
    private final ModeSetting preset = new ModeSetting("Аксессуар", "Крылья", LocalCosmeticPreset.NAMES).a(value -> {
        apply();
    });

    private UUID appliedUuid;
    private LocalCosmeticPreset appliedPreset = LocalCosmeticPreset.NONE;

    public Cosmetics() {
        a(this.preset);
    }

    @Override
    public void b() {
        super.b();
        apply();
    }

    @Override
    public void c() {
        clear();
        super.c();
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.world == null) {
            return;
        }
        LocalCosmeticPreset selected = selectedPreset();
        UUID uuid = aM_.player.getUuid();
        if (!uuid.equals(this.appliedUuid) || selected != this.appliedPreset || !Delta.h().d().r().hasLocalCosmetic(uuid, selected)) {
            apply();
        }
    }

    private void apply() {
        if (!m() || aM_.player == null) {
            return;
        }
        LocalCosmeticPreset selected = selectedPreset();
        UUID uuid = aM_.player.getUuid();
        Delta.h().d().r().setLocalCosmetic(uuid, selected);
        this.appliedUuid = uuid;
        this.appliedPreset = selected;
    }

    private void clear() {
        if (this.appliedUuid != null) {
            Delta.h().d().r().unregisterLocalCosmetic(this.appliedUuid);
        }
        this.appliedUuid = null;
        this.appliedPreset = LocalCosmeticPreset.NONE;
    }

    private LocalCosmeticPreset selectedPreset() {
        return LocalCosmeticPreset.byDisplayName(this.preset.c());
    }
}
