#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D AlphaMask;
uniform float GlobalAlpha;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord0);
    float maskAlpha = texture(AlphaMask, texCoord0).r;
    fragColor = vec4(color.rgb, color.a * maskAlpha * GlobalAlpha);
}