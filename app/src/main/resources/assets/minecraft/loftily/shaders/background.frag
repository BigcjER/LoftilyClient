#version 300 es
precision highp float;

uniform vec2 iResolution;
uniform float iTime;
uniform vec4 iMouse;

out vec4 fragColor;

float hash21(vec2 x) {
    return fract(cos(mod(dot(x, vec2(13.9898, 8.141)), 3.14)) * 43758.5453);
}

vec2 hash22(vec2 uv) {
    uv = vec2(dot(uv, vec2(127.1, 311.7)),
    dot(uv, vec2(269.5, 183.3)));
    return 2.0 * fract(sin(uv) * 43758.5453123) - 1.0;
}

float perlinNoise(vec2 uv) {
    vec2 iuv = floor(uv);
    vec2 fuv = fract(uv);
    vec2 blur = smoothstep(0.0, 1.0, fuv);

    vec2 bl = vec2(0.0, 0.0);
    vec2 br = vec2(1.0, 0.0);
    vec2 tl = vec2(0.0, 1.0);
    vec2 tr = vec2(1.0, 1.0);

    vec2 bln = hash22(iuv + bl);
    vec2 brn = hash22(iuv + br);
    vec2 tln = hash22(iuv + tl);
    vec2 trn = hash22(iuv + tr);

    float b = mix(dot(bln, fuv - bl), dot(brn, fuv - br), blur.x);
    float t = mix(dot(tln, fuv - tl), dot(trn, fuv - tr), blur.x);
    return mix(b, t, blur.y);
}

float fbm(vec2 uv, int octaves) {
    float value = 0.0;
    float amplitude = 0.5;
    float freq = 2.0;
    for (int i = 0; i < octaves; i++) {
        value += perlinNoise(uv) * amplitude;
        uv *= freq;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec2 fragCoord = gl_FragCoord.xy;
    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;

    vec3 col = vec3(0.0);
    uv += fbm(uv + iTime * 0.5, 20);
    float dist = abs(uv.x);

    vec3 baseColor = vec3(1.2, 0.2, 0.3);

    if (iMouse.xy != vec2(0.0)) {
        vec2 mouseUV = iMouse.xy / iResolution.xy;
        vec3 blue = vec3(0.0, 0.2, 1.0);
        vec3 red = vec3(1.0, 0.1, 0.1);
        baseColor = mix(blue, red, mouseUV.x);
    }

    col = baseColor * mix(0.0, 0.05, hash21(vec2(iTime))) / dist;
    fragColor = vec4(col, 1.0);
}
