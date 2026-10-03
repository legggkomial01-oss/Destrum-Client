package aethereal.cosmetic;

import com.google.gson.JsonParser;
import java.util.UUID;
import net.minecraft.client.texture.NativeImage;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public final class LocalCosmeticFactory {
    private LocalCosmeticFactory() {
    }

    public static Cosmetic create(UUID uuid, LocalCosmeticPreset preset) {
        BakedGeoModel model = bake(modelJson(preset));
        NativeImage texture = texture(preset);
        Vector3f offset = preset.offset();
        return new Cosmetic("local_" + preset.name().toLowerCase(), uuid, preset.category(), preset.scale(), offset, model, null, texture, true);
    }

    private static BakedGeoModel bake(String geometry) {
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(JsonParser.parseString(geometry), Model.class);
        return BakedModelFactory.getForNamespace("delta").constructGeoModel(GeometryTree.fromModel(model));
    }

    private static NativeImage texture(LocalCosmeticPreset preset) {
        NativeImage image = new NativeImage(64, 64, false);
        int base = switch (preset) {
            case WINGS -> 0xD0406CFF;
            case CROWN -> 0xFFFFD34D;
            case HALO -> 0xE8FFF78A;
            case HORNS -> 0xFF2C2538;
            case WIZARD_HAT -> 0xFF6C49E8;
            default -> 0xFFFFFFFF;
        };
        int accent = switch (preset) {
            case WINGS -> 0xF0BDE6FF;
            case CROWN -> 0xFFFFF2A6;
            case HALO -> 0xFFFFFFFF;
            case HORNS -> 0xFFB74E6F;
            case WIZARD_HAT -> 0xFFFFD35C;
            default -> base;
        };
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                image.setColorArgb(x, y, ((x + y) & 8) == 0 ? base : accent);
            }
        }
        return image;
    }

    private static String modelJson(LocalCosmeticPreset preset) {
        return switch (preset) {
            case WINGS -> """
                {"format_version":"1.12.0","minecraft:geometry":[{"description":{"identifier":"geometry.delta.local_wings","texture_width":64,"texture_height":64,"visible_bounds_width":3,"visible_bounds_height":3,"visible_bounds_offset":[0,8,0]},"bones":[{"name":"wings","pivot":[0,10,3],"cubes":[{"origin":[-12,5,3],"size":[8,10,1],"uv":[0,0],"rotation":[0,0,-18],"pivot":[-2,10,3]},{"origin":[4,5,3],"size":[8,10,1],"uv":[0,16],"rotation":[0,0,18],"pivot":[2,10,3]},{"origin":[-10,2,3.2],"size":[6,5,1],"uv":[18,0],"rotation":[0,0,-32],"pivot":[-2,8,3]},{"origin":[4,2,3.2],"size":[6,5,1],"uv":[18,12],"rotation":[0,0,32],"pivot":[2,8,3]}]}]}]}
                """;
            case CROWN -> """
                {"format_version":"1.12.0","minecraft:geometry":[{"description":{"identifier":"geometry.delta.local_crown","texture_width":64,"texture_height":64,"visible_bounds_width":2,"visible_bounds_height":2,"visible_bounds_offset":[0,8,0]},"bones":[{"name":"crown","pivot":[0,8,0],"cubes":[{"origin":[-5,8,-5],"size":[10,2,10],"uv":[0,0]},{"origin":[-5,10,-5],"size":[2,4,2],"uv":[0,14]},{"origin":[3,10,-5],"size":[2,4,2],"uv":[8,14]},{"origin":[-1,10,-5],"size":[2,5,2],"uv":[16,14]},{"origin":[-5,10,3],"size":[2,4,2],"uv":[24,14]},{"origin":[3,10,3],"size":[2,4,2],"uv":[32,14]}]}]}]}
                """;
            case HALO -> """
                {"format_version":"1.12.0","minecraft:geometry":[{"description":{"identifier":"geometry.delta.local_halo","texture_width":64,"texture_height":64,"visible_bounds_width":2,"visible_bounds_height":2,"visible_bounds_offset":[0,12,0]},"bones":[{"name":"halo","pivot":[0,13,0],"cubes":[{"origin":[-6,13,-1],"size":[12,1,2],"uv":[0,0]},{"origin":[-1,13,-6],"size":[2,1,12],"uv":[0,4]},{"origin":[-5,13,-5],"size":[3,1,3],"uv":[28,0]},{"origin":[2,13,2],"size":[3,1,3],"uv":[28,8]},{"origin":[2,13,-5],"size":[3,1,3],"uv":[40,0]},{"origin":[-5,13,2],"size":[3,1,3],"uv":[40,8]}]}]}]}
                """;
            case HORNS -> """
                {"format_version":"1.12.0","minecraft:geometry":[{"description":{"identifier":"geometry.delta.local_horns","texture_width":64,"texture_height":64,"visible_bounds_width":2,"visible_bounds_height":2,"visible_bounds_offset":[0,9,0]},"bones":[{"name":"horns","pivot":[0,8,0],"cubes":[{"origin":[-5,8,-3],"size":[2,5,2],"uv":[0,0],"rotation":[0,0,-18],"pivot":[-4,8,-2]},{"origin":[3,8,-3],"size":[2,5,2],"uv":[8,0],"rotation":[0,0,18],"pivot":[4,8,-2]},{"origin":[-5,12,-3],"size":[2,2,2],"uv":[16,0],"rotation":[0,0,-35],"pivot":[-4,12,-2]},{"origin":[3,12,-3],"size":[2,2,2],"uv":[24,0],"rotation":[0,0,35],"pivot":[4,12,-2]}]}]}]}
                """;
            case WIZARD_HAT -> """
                {"format_version":"1.12.0","minecraft:geometry":[{"description":{"identifier":"geometry.delta.local_wizard_hat","texture_width":64,"texture_height":64,"visible_bounds_width":2,"visible_bounds_height":3,"visible_bounds_offset":[0,10,0]},"bones":[{"name":"wizard_hat","pivot":[0,8,0],"cubes":[{"origin":[-6,8,-6],"size":[12,1,12],"uv":[0,0]},{"origin":[-4,9,-4],"size":[8,4,8],"uv":[0,18]},{"origin":[-3,13,-3],"size":[6,4,6],"uv":[32,18]},{"origin":[-2,17,-2],"size":[4,4,4],"uv":[0,36]},{"origin":[-1,21,-1],"size":[2,3,2],"uv":[18,36]}]}]}]}
                """;
            default -> throw new IllegalArgumentException("No local cosmetic model for " + preset);
        };
    }
}
