#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ColorProcessing {
    vec3 Mul;
    vec3 Add;
    // 1.21.11 UBO layout fix: Mojang's Std140Builder advances a full 16 bytes after a vec3, so the
    // post_effect JSON order (Mul, Add, Contrast, Saturation) writes Contrast at byte offset 32.
    // Reserve the vec3 tail slot (offset 28); otherwise Contrast/Saturation would be misaligned and
    // Saturation would read a skipped zero, fully desaturating the photo/viewfinder to grayscale.
    float _paddingAfterAdd;
    float Contrast;
    float Saturation;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    vec3 RGB = InTexel.rgb * Mul + Add;

    vec3 Gray = vec3(0.3, 0.59, 0.11);
    float Luma = dot(RGB, Gray);
    vec3 Chroma = RGB - Luma;
    RGB = (Chroma * Saturation) + Luma;

    RGB = (RGB - 0.5) * Contrast + 0.5;

    fragColor = vec4(RGB, 1.0);
}
