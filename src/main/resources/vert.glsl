#version 130

in vec3 aPos; // position has attribute position 0
in vec3 aColor; // color has attribute position 1
in vec2 aTexCoord; // UV has attribute position 2

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;

out vec3 ourColor; // output a color to the fragment shader
out vec2 texCoord;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    ourColor = aColor; // set ourColor to input color from the vertex data
    texCoord = aTexCoord;
}