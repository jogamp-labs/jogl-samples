#version 420 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

vec4 diffuse = vec4(1.0, 0.5, 0.0, 1.0);

in Block
{
    float instance;
} inBlock;

layout (depth_greater) out float gl_FragDepth;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = diffuse * inBlock.instance * 0.25;
}
