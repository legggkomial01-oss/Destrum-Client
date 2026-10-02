package aethereal.render.targetesp;

import java.util.Arrays;
import java.util.List;

public enum TargetEffect {
    RING("Кольцо") {
        @Override
        public void render(ParticleBuffer pb) {
            runLayout(pb.density, pb, RING_LAYOUT, 0.9f);
        }
    },
    TRIANGLE("Треугольник") {
        @Override
        public void render(ParticleBuffer pb) {
            int segments = Math.max(4, pb.density / 3) * 3;
            runLayout(segments, pb, TRIANGLE_LAYOUT, 0.85f);
        }
    },
    LIGHTNING("Молния") {
        @Override
        public void render(ParticleBuffer pb) {
            float[] points = pb.getFloatArray(45);
            int[] colors = pb.getIntArray2(15);
            int[] colorsBg = pb.getIntArray(15);

            for (int i = 0; i < 6; i++) {
                float f = pb.phase * 8.0f + hashFloat(i * 7717);
                int j = (int) f;
                float f1 = f - j;
                int seed = j * 7919 + i * 104729;
                float heightRel = 0.1f + hashFloat(seed) * 0.8f;
                float angleBase = hashFloat(seed + 3) * (float) (Math.PI * 2.0) + f1 * 0.5f;
                float direction = hashFloat(seed + 5) < 0.5f ? -1.0f : 1.0f;
                float angleSpan = 1.6f + hashFloat(seed + 7) * 2.0f;
                float alphaScale = (0.35f + 0.65f * (float) Math.sin(Math.PI * f1)) * pb.getFade(heightRel);

                for (int l = 0; l <= 14; l++) {
                    float t = l / 14.0f;
                    float jitterX = hashFloat(seed + l * 53) - 0.5f + (hashFloat(seed + l * 191) - 0.5f) * 0.45f;
                    float jitterR = hashFloat(seed + l * 97) - 0.5f + (hashFloat(seed + l * 313) - 0.5f) * 0.45f;
                    float jitterY = hashFloat(seed + l * 17) - 0.5f + (hashFloat(seed + l * 421) - 0.5f) * 0.45f;
                    float angle = angleBase + direction * angleSpan * t + jitterX * 0.32f;
                    float r = pb.radius * (0.9f + jitterR * 0.42f + noise(seed + l, pb.phase * 55.0f) * 0.06f);
                    float y = (heightRel + jitterY * 0.2f + noise(seed + l * 5, pb.phase * 45.0f) * 0.025f) * pb.height;

                    points[l * 3] = (float) Math.cos(angle) * r;
                    points[l * 3 + 1] = y;
                    points[l * 3 + 2] = (float) Math.sin(angle) * r;

                    float arcAlpha = 0.25f + 0.75f * (float) Math.sin(Math.PI * t);
                    colors[l] = pb.computeColor(alphaScale * 0.85f * arcAlpha);
                    colorsBg[l] = pb.computeColor(alphaScale * 0.2f * arcAlpha);
                }

                pb.drawRibbon(pb.thickness * 1.15f, points, colorsBg, false, 15);
                pb.drawRibbon(pb.thickness * 0.34f, points, colors, false, 15);

                int branchIdx = 3 + (int) (hashFloat(seed + 13) * 9.0f);
                float bx = points[branchIdx * 3];
                float by = points[branchIdx * 3 + 1];
                float bz = points[branchIdx * 3 + 2];
                float dx = bx - points[(branchIdx - 1) * 3];
                float dz = bz - points[(branchIdx - 1) * 3 + 2];
                float dLen = (float) Math.sqrt(dx * dx + dz * dz);
                if (dLen >= 1.0e-5f) {
                    float turn = hashFloat(seed + 19) < 0.5f ? -0.9f : 0.9f;
                    float branchDirX = (dx * (float) Math.cos(turn) - dz * (float) Math.sin(turn)) / dLen;
                    float branchDirZ = (dx * (float) Math.sin(turn) + dz * (float) Math.cos(turn)) / dLen;

                    for (int i1 = 0; i1 < 4; i1++) {
                        float bt = i1 / 3.0f;
                        float bDist = pb.radius * 0.85f * bt;
                        points[i1 * 3] = bx + branchDirX * bDist + pb.radius * (hashFloat(seed + i1 * 71) - 0.5f) * 0.2f;
                        points[i1 * 3 + 1] = by - pb.height * bt * 0.14f;
                        points[i1 * 3 + 2] = bz + branchDirZ * bDist + pb.radius * (hashFloat(seed + i1 * 83) - 0.5f) * 0.2f;
                        colors[i1] = pb.computeColor(alphaScale * 0.5f * (1.0f - bt));
                    }
                    pb.drawRibbon(pb.thickness * 0.28f, points, colors, false, 4);
                }
            }
        }
    },
    HELIX("Спираль") {
        @Override
        public void render(ParticleBuffer pb) {
            int segments = Math.max(28, pb.density / 4);
            float baseAngle = pb.phase * (float) (Math.PI * 2.0) * 0.9f + noise(31, pb.phase * 0.8f) * 0.55f;
            float ribWidth = pb.thickness * 0.6f;
            float[] points = pb.getFloatArray(segments * 3);
            int[] colors = pb.getIntArray(segments);

            for (int j = 0; j < segments; j++) {
                float t = (float) j / (segments - 1);
                float r = pb.radius * (1.0f - 0.12f * t + 0.09f * noise(32, pb.phase * 1.1f + t * 3.0f));
                float angle = baseAngle + t * 2.2f * (float) (Math.PI * 2.0);
                points[j * 3] = (float) Math.cos(angle) * r;
                points[j * 3 + 1] = t * pb.height;
                points[j * 3 + 2] = (float) Math.sin(angle) * r;
                colors[j] = pb.computeColor(0.8f * (0.45f + 0.55f * (float) Math.sin(Math.PI * t)) * pb.getFade(t));
            }

            pb.drawRibbon(ribWidth, points, colors, false, segments);

            for (int k = 0; k < segments; k++) {
                points[k * 3] = -points[k * 3];
                points[k * 3 + 2] = -points[k * 3 + 2];
            }
            pb.drawRibbon(ribWidth, points, colors, false, segments);
        }
    },
    ORBIT("Орбита") {
        @Override
        public void render(ParticleBuffer pb) {
            int rings = (pb.tail + 4) * 3;
            float r = pb.radius * 1.2f;
            float centerY = pb.height * 0.55f;
            float[] points = pb.getFloatArray(168);

            for (int j = 0; j < 3; j++) {
                float tilt = j * (float) Math.PI / 3.0f + noise(41 + j, pb.phase * 0.5f) * 0.3f;
                float wobble = 1.0f + noise(51 + j, pb.phase * 0.45f) * 0.22f;
                float cosTilt = (float) Math.cos(tilt) * r;
                float sinTilt = (float) Math.sin(tilt) * r;
                float ux = -(float) Math.sin(tilt) * (float) Math.cos(wobble) * r;
                float uy = -(float) Math.sin(wobble) * r;
                float uz = (float) Math.cos(tilt) * (float) Math.cos(wobble) * r;

                for (int k = 0; k < 56; k++) {
                    float a = (float) (Math.PI * 2.0) * k / 56.0f;
                    points[k * 3] = cosTilt * (float) Math.cos(a) + ux * (float) Math.sin(a);
                    points[k * 3 + 1] = centerY + uy * (float) Math.sin(a);
                    points[k * 3 + 2] = sinTilt * (float) Math.cos(a) + uz * (float) Math.sin(a);
                }

                float fade = pb.getFade((j + 0.9f) / 3.0f);
                int ringColor = pb.computeColor(0.2f * fade);
                pb.drawRibbonSingleColor(true, 56, ringColor, points, pb.thickness * 0.28f);

                float particleAngle = pb.phase * (float) (Math.PI * 2.0) * (1.1f + j * 0.17f) + j * 2.1f + noise(61 + j, pb.phase * 0.7f) * 0.9f;
                for (int l = 0; l < rings; l++) {
                    float a = particleAngle - l * 0.032f;
                    float trailFade = 1.0f - (float) l / rings;
                    float px = cosTilt * (float) Math.cos(a) + ux * (float) Math.sin(a);
                    float py = centerY + uy * (float) Math.sin(a);
                    float pz = sinTilt * (float) Math.cos(a) + uz * (float) Math.sin(a);
                    float pSize = pb.thickness * (0.5f + 0.7f * trailFade);
                    int col = pb.computeColor(trailFade * trailFade * fade * 0.35f);
                    pb.drawBillboardQuad(px, pSize, pz, py, col);
                }
            }
        }
    },
    EMBERS("Искры") {
        @Override
        public void render(ParticleBuffer pb) {
            for (int i = 0; i < pb.density; i++) {
                float life = fract(pb.phase * 0.35f + hashFloat(i * 7919));
                float angle = hashFloat(i * 104729) * (float) (Math.PI * 2.0)
                    + life * 0.7f
                    + noise(i * 13, pb.phase * 0.6f) * 0.5f;
                float r = pb.radius * (0.25f + 0.8f * hashFloat(i * 15486))
                    + noise(i * 29, pb.phase * 0.9f + life * 2.0f) * 0.07f;
                float alpha = (float) Math.sin(Math.PI * life) * pb.getFade(life);
                float px = (float) Math.cos(angle) * r;
                float py = life * pb.height * 1.25f;
                float pz = (float) Math.sin(angle) * r;
                float pSize = pb.thickness * (0.45f + 0.65f * hashFloat(i * 32452));
                int col = pb.computeColor(alpha * 0.75f);
                pb.drawBillboardQuad(px, pSize, pz, py, col);
            }
        }
    },
    RUNES("Руны") {
        @Override
        public void render(ParticleBuffer pb) {
            int subSegments = Math.max(3, pb.density / 24);
            int circleSegs = Math.max(24, pb.density / 2);

            float r1 = pb.radius * 1.25f;
            float rot1 = pb.phase * (float) (Math.PI * 2.0) * 0.3f + noise(71, pb.phase * 0.5f) * 0.35f;
            drawRuneCircle(subSegments, pb, 0.55f, rot1, 12, pb.thickness, r1, 0.75f);

            float r2 = pb.radius * 0.8f;
            float rot2 = -pb.phase * (float) (Math.PI * 2.0) * 0.45f + noise(72, pb.phase * 0.55f) * 0.3f;
            drawRuneCircle(subSegments, pb, 0.45f, rot2, 7, pb.thickness * 0.85f, r2, 0.6f);

            for (int k = 0; k < circleSegs; k++) {
                float t = (float) k / circleSegs;
                float a = (float) (Math.PI * 2.0) * t;
                float px = (float) Math.cos(a) * pb.radius;
                float pz = (float) Math.sin(a) * pb.radius;
                int col = pb.computeColor(0.18f * pb.getFade(t));
                pb.drawBillboardQuad(px, pb.thickness * 0.55f, pz, 0.03f, col);
            }
        }
    },
    PULSE("Пульс") {
        @Override
        public void render(ParticleBuffer pb) {
            int segments = Math.max(24, pb.density);

            for (int j = 0; j < 3; j++) {
                float progress = fract(pb.phase * 0.85f + j / 3.0f + noise(91 + j, pb.phase * 0.5f) * 0.05f);
                float r = pb.radius * (0.3f + progress * 1.6f);
                float y = 0.03f + progress * pb.height * 0.15f;
                float alpha = (1.0f - progress) * (1.0f - progress) * Math.min(1.0f, progress * 5.0f) * pb.getFade(progress);
                float size = pb.thickness * (1.0f - 0.35f * progress);
                int col = pb.computeColor(alpha * 0.8f);

                for (int l = 0; l < segments; l++) {
                    float a = (float) (Math.PI * 2.0) * l / segments;
                    float px = (float) Math.cos(a) * r;
                    float pz = (float) Math.sin(a) * r;
                    pb.drawBillboardQuad(px, size, pz, y, col);
                }
            }
        }
    };

    private final String text;
    private static final ParticleLayout RING_LAYOUT = new RingParticleLayout();
    private static final ParticleLayout TRIANGLE_LAYOUT = new TriangleParticleLayout();
    public static final List<String> NAMES = Arrays.stream(values()).map(TargetEffect::getText).toList();

    TargetEffect(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public abstract void render(ParticleBuffer pb);

    public static TargetEffect getByName(String name) {
        for (TargetEffect effect : values()) {
            if (effect.text.equalsIgnoreCase(name)) {
                return effect;
            }
        }
        return RING;
    }

    public static float fract(float value) {
        return value - (float) Math.floor(value);
    }

    public static float triangleWave(float value) {
        return 0.5f * (1.0f - (float) Math.cos(value * (float) (Math.PI * 2.0)));
    }

    public static float hashFloat(int seed) {
        seed = seed ^ 61 ^ (seed >>> 16);
        seed *= 9;
        seed ^= seed >>> 4;
        seed *= 668265261;
        seed ^= seed >>> 15;
        return (seed & 0x00FFFFFF) / 16777215.0f;
    }

    public static float noise(int seed, float value) {
        int i = (int) Math.floor(value);
        float f = value - i;
        f = f * f * (3.0f - 2.0f * f);
        float f1 = hashFloat(seed + i * 8191) * 2.0f - 1.0f;
        float f2 = hashFloat(seed + (i + 1) * 8191) * 2.0f - 1.0f;
        return f1 + (f2 - f1) * f;
    }

    public static void runLayout(int count, ParticleBuffer pb, ParticleLayout layout, float alphaMult) {
        int tailSteps = pb.tail > 0 ? Math.min(10, 2 + pb.tail) : 1;
        int tailStepsM1 = Math.max(1, tailSteps - 1);
        float tailOffset = pb.tail * 0.009f;
        float[] frameBuffer = pb.getFloatArray(tailSteps * 2);
        float[] trailPoints = pb.getFloatArray4(tailSteps * 3);
        int[] trailColors = pb.getIntArray(tailSteps);
        int[] trailColorsTemp = pb.getIntArray2(tailSteps);
        float[] tempPos = pb.vertexPos;

        for (int k = 0; k < tailSteps; k++) {
            float phaseT = pb.phase - tailOffset * k / tailStepsM1;
            layout.frame(pb, frameBuffer, phaseT, k * 2);
        }

        float trailSpacing = pb.thickness * 0.3f;
        for (int l = 0; l < tailSteps; l++) {
            layout.place(0, frameBuffer, tempPos, l * 2, pb);
            trailPoints[l * 3] = tempPos[0];
            trailPoints[l * 3 + 1] = tempPos[1];
            trailPoints[l * 3 + 2] = tempPos[2];

            float distFactor = 1.0f;
            if (l > 0) {
                float dx = trailPoints[l * 3] - trailPoints[(l - 1) * 3];
                float dy = trailPoints[l * 3 + 1] - trailPoints[(l - 1) * 3 + 1];
                float dz = trailPoints[l * 3 + 2] - trailPoints[(l - 1) * 3 + 2];
                distFactor = Math.min(1.0f, (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / trailSpacing);
            }
            float decay = 1.0f - (float) l / tailSteps;
            trailColors[l] = pb.computeColor(alphaMult * decay * decay * distFactor);
        }

        for (int j = 0; j < count; j++) {
            float segFade = pb.getFade((float) j / count);
            if (segFade <= 0.0f) continue;

            for (int k = 0; k < tailSteps; k++) {
                layout.place(j, frameBuffer, tempPos, k * 2, pb);
                trailPoints[k * 3] = tempPos[0];
                trailPoints[k * 3 + 1] = tempPos[1];
                trailPoints[k * 3 + 2] = tempPos[2];
            }

            int[] activeColors = trailColors;
            if (segFade < 1.0f) {
                for (int l = 0; l < tailSteps; l++) {
                    int c = trailColors[l];
                    int a = (int) (((c >>> 24) & 0xFF) * segFade);
                    trailColorsTemp[l] = (Math.min(255, Math.max(0, a)) << 24) | (c & 0x00FFFFFF);
                }
                activeColors = trailColorsTemp;
            }

            pb.drawBillboardQuad(trailPoints[0], pb.thickness, trailPoints[2], trailPoints[1], activeColors[0]);
            if (tailSteps > 1) {
                pb.drawRibbon(pb.thickness * 0.8f, trailPoints, activeColors, false, tailSteps);
            }
        }
    }

    private static void drawRuneCircle(
        int subSteps, ParticleBuffer pb, float arcScale, float startAngle,
        int runeCount, float thickness, float radius, float alphaMult
    ) {
        float arcStep = (float) (Math.PI * 2.0) / runeCount * arcScale;

        for (int i = 0; i < runeCount; i++) {
            float baseA = startAngle + (float) (Math.PI * 2.0) * i / runeCount;
            int col = pb.computeColor(alphaMult * pb.getFade((float) i / runeCount));

            for (int k = 0; k <= subSteps; k++) {
                float a = baseA + arcStep * k / subSteps;
                float px = (float) Math.cos(a) * radius;
                float pz = (float) Math.sin(a) * radius;
                pb.drawBillboardQuad(px, thickness, pz, 0.03f, col);
            }
        }
    }
}
