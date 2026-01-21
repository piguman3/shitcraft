import static org.lwjgl.opengl.GL20.*;

import java.io.FileNotFoundException;

public class Shader {
    int id;

    public Shader(int type) {
        this.id = glCreateShader(type);
    }

    public void loadString(String code) throws Exception {
        glShaderSource(this.id, code);
		glCompileShader(this.id);

        if (glGetShaderi(this.id, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(this.id, 1024));
		}
    }

    public void loadFile(String fileName) throws Exception {
        String code = Resource.getResourceFileAsString(fileName);
        if (code==null) throw new Exception("Error loading shader file: " + fileName);
        loadString(code);
    }

    public void delete() {
        glDeleteShader(this.id);
    }
}
