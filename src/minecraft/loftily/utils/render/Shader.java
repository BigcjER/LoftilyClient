package loftily.utils.render;

public enum Shader {
    RoundedRect("#version 120\n" +
            "\n" +
            "uniform vec2 location, rectSize;\n" +
            "uniform vec4 color;\n" +
            "uniform float radius;\n" +
            "\n" +
            "float roundSDF(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "    vec2 rectHalf = rectSize * .5;\n" +
            "    // Smooth the result (free antialiasing).\n" +
            "    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n" +
            "    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n" +
            "\n" +
            "}"),
    
    RoundRectOutline("#version 120\n" +
            "\n" +
            "uniform vec2 location, rectSize;\n" +
            "uniform vec4 color, outlineColor;\n" +
            "uniform float radius, outlineThickness;\n" +
            "\n" +
            "float roundedSDF(vec2 centerPos, vec2 size, float radius) {\n" +
            "    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n" +
            "\n" +
            "    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n" +
            "\n" +
            "    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n" +
            "    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n" +
            "\n" +
            "}"),
    
    
    Vertex("#version 120\n" +
            "\n" +
            "void main() {\n" +
            "    gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
            "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
            "}"),
    
    BackGround("#version 300 es\n" +
            "precision highp float;\n" +
            "\n" +
            "uniform vec2 iResolution;\n" +
            "uniform float iTime;\n" +
            "uniform vec4 iMouse;\n" +
            "\n" +
            "out vec4 fragColor;\n" +
            "\n" +
            "float hash21(vec2 x) {\n" +
            "    return fract(cos(mod(dot(x, vec2(13.9898, 8.141)), 3.14)) * 43758.5453);\n" +
            "}\n" +
            "\n" +
            "vec2 hash22(vec2 uv) {\n" +
            "    uv = vec2(dot(uv, vec2(127.1,311.7)),\n" +
            "              dot(uv, vec2(269.5,183.3)));\n" +
            "    return 2.0 * fract(sin(uv) * 43758.5453123) - 1.0;\n" +
            "}\n" +
            "\n" +
            "float perlinNoise(vec2 uv) {\n" +
            "    vec2 iuv = floor(uv);\n" +
            "    vec2 fuv = fract(uv);\n" +
            "    vec2 blur = smoothstep(0.0, 1.0, fuv);\n" +
            "    \n" +
            "    vec2 bl = vec2(0.0, 0.0);\n" +
            "    vec2 br = vec2(1.0, 0.0);\n" +
            "    vec2 tl = vec2(0.0, 1.0);\n" +
            "    vec2 tr = vec2(1.0, 1.0);\n" +
            "\n" +
            "    vec2 bln = hash22(iuv + bl);\n" +
            "    vec2 brn = hash22(iuv + br);\n" +
            "    vec2 tln = hash22(iuv + tl);\n" +
            "    vec2 trn = hash22(iuv + tr);\n" +
            "\n" +
            "    float b = mix(dot(bln, fuv - bl), dot(brn, fuv - br), blur.x);\n" +
            "    float t = mix(dot(tln, fuv - tl), dot(trn, fuv - tr), blur.x);\n" +
            "    return mix(b, t, blur.y);\n" +
            "}\n" +
            "\n" +
            "float fbm(vec2 uv, int octaves) {\n" +
            "    float value = 0.0;\n" +
            "    float amplitude = 0.5;\n" +
            "    float freq = 2.0;\n" +
            "    for(int i = 0; i < octaves; i++) {\n" +
            "        value += perlinNoise(uv) * amplitude;\n" +
            "        uv *= freq;\n" +
            "        amplitude *= 0.5;\n" +
            "    }\n" +
            "    return value;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    vec2 fragCoord = gl_FragCoord.xy;\n" +
            "    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;\n" +
            "\n" +
            "    vec3 col = vec3(0.0);\n" +
            "    uv += fbm(uv + iTime * 0.5, 20);\n" +
            "    float dist = abs(uv.x);\n" +
            "\n" +
            "    vec3 baseColor = vec3(1.2, 0.2, 0.3); // default pinkish red\n" +
            "\n" +
            "    if (iMouse.xy != vec2(0.0)) {\n" +
            "        vec2 mouseUV = iMouse.xy / iResolution.xy;\n" +
            "        vec3 blue = vec3(0.0, 0.2, 1.0);\n" +
            "        vec3 red = vec3(1.0, 0.1, 0.1);\n" +
            "        baseColor = mix(blue, red, mouseUV.x);\n" +
            "    }\n" +
            "\n" +
            "    col = baseColor * mix(0.0, 0.05, hash21(vec2(iTime))) / dist;\n" +
            "\n" +
            "    fragColor = vec4(col, 1.0);\n" +
            "}\n");
    
    public final String code;
    
    Shader(String code) {
        this.code = code;
    }
    
}
