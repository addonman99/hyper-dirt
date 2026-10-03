#version 150

uniform sampler2D DiffuseSampler;
uniform float Mode;
uniform float Contrast;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 c = texture2D(DiffuseSampler, texCoord);
    float g = dot(c.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 v = vec3(g);

    if (Mode > 0.5)
        v = 1.0 - v;

    v = (v - 0.5) * Contrast + 0.5;
    fragColor = vec4(clamp(v, 0.0, 1.0), 1.0);
}
