#version 150

in vec2 vScreen;

uniform float uTime;
uniform vec2 uResolution;
uniform vec3 uColor;
uniform float uAlpha;
uniform float uSpeed;
uniform float uScale;
uniform float uIntensity;
uniform vec3 uCamRight;
uniform vec3 uCamUp;
uniform vec3 uCamForward;
uniform float uFov;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x),
        f.y
    );
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p *= 2.05;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 uv = vScreen;
    float aspect = uResolution.x / max(1.0, uResolution.y);
    vec2 p = vec2(uv.x * aspect, uv.y);

    float t = uTime * uSpeed * 0.15;
    float dist = length(p);
    float angle = atan(p.y, p.x);

    // Swirling vortex coordinates
    vec2 swirl = vec2(angle * 1.5 + t * 0.4, dist * 2.0 - t * 0.3);
    float neb = fbm(swirl * (uScale * 0.3) + vec2(t * 0.2, -t * 0.1));
    float neb2 = fbm(p * (uScale * 0.5) - vec2(neb * 0.5, t * 0.15));

    // Star specks
    float starGrid = hash(floor(p * 90.0));
    float star = 0.0;
    if (starGrid > 0.985) {
        float twinkle = sin(uTime * 3.0 + starGrid * 60.0) * 0.5 + 0.5;
        star = (starGrid - 0.985) / 0.015 * twinkle;
    }

    vec3 baseCol = uColor * 0.25;
    vec3 glowCol = mix(uColor, vec3(0.3, 0.8, 1.0), neb2);
    vec3 finalRgb = baseCol + glowCol * (neb * 0.85 + neb2 * 0.4) + vec3(star * 0.9);

    // Dark edges vignette
    float vig = smoothstep(1.8, 0.2, dist);
    finalRgb *= vig;

    fragColor = vec4(finalRgb, uAlpha);
}
