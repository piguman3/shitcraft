import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.*;
import org.lwjgl.BufferUtils;

public class ShaderProgram {
    int id;

    public ShaderProgram() {
        id = glCreateProgram();
    }

    public void attachShader(Shader shader) {
        glAttachShader(this.id, shader.id);
    }
    
    public void link() throws Exception {
        glLinkProgram(this.id);

        if (glGetProgrami(this.id, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(this.id, 1024));
        }
    }

    public void use() {
        glUseProgram(this.id);
    }

    void setBool(String name, Boolean value) {
        glUniform1i(glGetUniformLocation(this.id, name), value ? 1 : 0);
    }

    void setInt(String name, int value)  {
        glUniform1i(glGetUniformLocation(this.id, name), value);
    }

    void setFloat(String name, float value)  {
        glUniform1f(glGetUniformLocation(this.id, name), value);
    }

    void setMat(String name, Matrix4f mat) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        mat.get(fb);
        glUniformMatrix4fv(glGetUniformLocation(this.id, name), false, fb);
    }
}
