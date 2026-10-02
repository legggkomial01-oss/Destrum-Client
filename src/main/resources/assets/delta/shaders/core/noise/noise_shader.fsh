#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform vec4 TintColor;
uniform vec4 SecondColor;
uniform float Time;
uniform float Mode;
uniform vec2 Resolution;
uniform float Radius;
uniform float Intensity;
uniform float GradientSpeed;
uniform float FlameStrength;

in vec2 TexCoord;
in vec4 FragColor;
out vec4 OutColor;

float noiseMask(vec2 uv) {
    float now = texture(Sampler1, uv).r;
    float was = texture(Sampler2, uv).r;
    return smoothstep(0.0001, 0.0004, was - now);
}

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p = p * 2.02 + vec2(8.4, 5.7);
        a *= 0.5;
    }
    return v;
}

float ridged(vec2 p) {
    float v = 0.0;
    float a = 0.55;
    for (int i = 0; i < 4; i++) {
        float r = 1.0 - abs(noise(p) * 2.0 - 1.0);
        v += r * a;
        p = p * 2.18 + vec2(3.1, 9.2);
        a *= 0.52;
    }
    return v;
}

const vec2 POISSON[16] = vec2[](
    vec2(-0.3262, -0.4058),
    vec2(-0.8401, -0.0735),
    vec2(-0.6959,  0.4571),
    vec2(-0.2033,  0.6207),
    vec2( 0.9623, -0.1949),
    vec2( 0.4734, -0.4800),
    vec2( 0.5194,  0.7670),
    vec2( 0.1854, -0.8931),
    vec2( 0.5074,  0.0644),
    vec2( 0.8964,  0.4124),
    vec2(-0.3219, -0.9326),
    vec2(-0.7915, -0.5977),
    vec2(-0.1500,  0.1500),
    vec2( 0.1500,  0.2500),
    vec2(-0.2500,  0.1000),
    vec2( 0.2000, -0.1500)
);

void main() {
    vec2 uv = TexCoord;
    int m = int(Mode + 0.5);

    // MODE 0: Шум (Оригинальный шейдер шума DELTA)
    if (m == 0) {
        float mask = noiseMask(uv);
        if (mask < 0.01) discard;

        float t = Time;
        vec2 flow = uv * 2.5;
        vec2 drift = vec2(t * 0.20, -t * 0.15);

        vec2 warp = vec2(
            fbm(flow * 0.90 + drift * 0.75 + vec2(0.0, 4.1)),
            fbm(flow * 0.78 - drift * 0.48 + vec2(3.7, 1.8))
        );
        vec2 q = flow + (warp - 0.5) * 1.8;

        float mist = fbm(q * 0.72 - drift * 0.24 + vec2(4.2, 8.1));
        float veins = pow(clamp(ridged(q * 1.85 + vec2(mist * 2.5, mist * 1.6) - drift * 0.55), 0.0, 1.0), 2.4);
        float sA = pow(clamp(1.0 - abs(sin((q.x * 1.08 + q.y * 0.42) * 1.7 + t * 0.85 + mist * 4.3)), 0.0, 1.0), 4.8);
        float sB = pow(clamp(1.0 - abs(sin((q.x * -0.58 + q.y * 1.12) * 1.45 - t * 0.65 - mist * 2.9)), 0.0, 1.0), 5.4);

        float energy = clamp(mist * 0.22 + veins * 0.88 + sA * 0.55 + sB * 0.32, 0.0, 1.0);
        float core = smoothstep(0.18, 0.98, energy);
        float accent = pow(clamp(max(veins, sA), 0.0, 1.0), 1.25);

        vec3 base = TintColor.rgb;
        if (SecondColor.a > 0.01) {
            base = mix(TintColor.rgb, SecondColor.rgb, clamp(mist * 1.4, 0.0, 1.0));
        }

        vec3 col = mix(base, mix(base, vec3(1.0), 0.4), clamp(core * 0.75 + sB * 0.25, 0.0, 1.0));
        float fill = mask * (0.26 + core * 0.82 + accent * 0.28) * Intensity;
        float outA = clamp(TintColor.a * fill * 0.92 * mask, 0.0, 1.0);

        if (outA <= 0.001) discard;
        OutColor = vec4(col * fill, outA);
        return;
    }

    // MODE 1: Свечение (HandGlow из System DLC)
    if (m == 1) {
        vec2 sampleCoord = uv;
        float flicker = 1.0;

        if (FlameStrength > 0.001) {
            vec2 flameCoord = uv * 4.0;
            flameCoord.y -= Time * 0.9;
            float distortion = (fbm(flameCoord) - 0.5) * FlameStrength * 0.02;
            sampleCoord += vec2(distortion * 0.6, distortion * 1.5);
            flicker = 1.0 + FlameStrength * 0.35 * (fbm(vec2(Time * 3.0, 0.0)) - 0.5);
        }

        vec2 stepSize = (Radius / Resolution);
        float blurSum = 0.0;
        for (int i = 0; i < 16; i++) {
            blurSum += noiseMask(sampleCoord + POISSON[i] * stepSize);
        }
        float blur = (blurSum / 16.0) * flicker;
        float mask = noiseMask(uv);

        float inside = FlameStrength > 0.001
            ? smoothstep(0.55, 0.95, mask)
            : smoothstep(0.35, 0.75, mask);
        float outsideMask = FlameStrength > 0.001 ? (1.0 - step(0.5, mask)) : 1.0;

        float outerGlow = blur * (1.0 - inside) * Intensity * outsideMask;
        float fillGlow = mask * (TintColor.a * 0.5);

        if (outerGlow + fillGlow <= 0.002) discard;

        outerGlow = clamp(outerGlow, 0.0, 1.0);
        fillGlow = clamp(fillGlow, 0.0, 1.0);

        vec4 glowCol = TintColor;
        if (SecondColor.a > 0.001) {
            float offset = 0.0;
            if (GradientSpeed > 0.001) {
                offset = sin(Time * GradientSpeed) * 0.35;
            }
            float wobble = 0.0;
            if (FlameStrength > 0.001) {
                wobble = (fbm(vec2(uv.x * 3.0, Time * 0.7)) - 0.5) * FlameStrength * 0.25;
            }
            float t = clamp(uv.y + offset + wobble, 0.0, 1.0);
            t = smoothstep(0.0, 1.0, t);
            glowCol = mix(TintColor, SecondColor, t);
        }

        float outlineA = glowCol.a * outerGlow;
        float fillA = TintColor.a * fillGlow;
        vec3 outlineRgb = vec3(1.0) - exp(-glowCol.rgb * outlineA * 2.5);
        vec3 fillRgb = vec3(1.0) - exp(-TintColor.rgb * fillA * 2.5);

        float alpha = outlineA + fillA * (1.0 - outlineA);
        vec3 rgb = outlineRgb + fillRgb * (1.0 - outlineA);

        OutColor = vec4(rgb, alpha);
        return;
    }

    discard;
}
