import static org.lwjgl.opengl.GL20.*;

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
}
