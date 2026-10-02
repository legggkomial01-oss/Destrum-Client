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

void main() {
    vec2 uv = vScreen;
    float aspect = uResolution.x / max(1.0, uResolution.y);
    vec2 p = vec2(uv.x * aspect, uv.y);

    float t = uTime * uSpeed * 0.4;
    
    // Cyber horizon perspective
    float horizon = -0.15;
    vec3 col = vec3(0.03, 0.04, 0.08); // Deep dark void

    if (p.y < horizon) {
        // Floor perspective grid
        float py = -(p.y - horizon);
        float z = 1.0 / max(0.01, py);
        float x = p.x * z;

        vec2 gridUv = vec2(x * 0.8, z * 0.8 + t * 2.0);
        vec2 grid = abs(fract(gridUv - 0.5) - 0.5) / fwidth(gridUv);
        float line = 1.0 - min(min(grid.x, grid.y), 1.0);

        // Distance fog fade
        float depthFog = exp(-py * 4.5);
        vec3 gridCol = mix(vec3(0.1, 0.7, 1.0), uColor, sin(gridUv.y * 0.2 + t) * 0.5 + 0.5);
        col += gridCol * line * depthFog * 1.5;
        col += uColor * 0.15 * depthFog;
    } else {
        // Sky glow & digital sun/nebula
        float skyY = p.y - horizon;
        float sunDist = length(vec2(p.x, skyY - 0.5));
        float sunGlow = exp(-sunDist * 2.8) * 0.8;
        
        float horizonBeam = exp(-abs(p.y - horizon) * 12.0) * 0.6;
        vec3 neonPink = vec3(1.0, 0.2, 0.6);
        vec3 neonCyan = vec3(0.1, 0.8, 1.0);

        col += mix(neonPink, neonCyan, p.x * 0.5 + 0.5) * horizonBeam;
        col += uColor * sunGlow;
        col += vec3(0.02, 0.03, 0.06) * (1.0 - skyY);
    }

    fragColor = vec4(col, uAlpha);
}
