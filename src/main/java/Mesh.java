import org.lwjgl.system.*;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import static org.lwjgl.system.MemoryStack.*;

public class Mesh {
    int VAO;
	int VBO;
    float[] vertices;
    FloatBuffer vertexBuffer;
	Texture texture;

	public static int VERTEX_SIZE = 8;

    public void upload() {
        if (this.vertexBuffer == null || this.vertexBuffer.capacity() != this.vertices.length)
            this.vertexBuffer = BufferUtils.createFloatBuffer(this.vertices.length);
        this.vertexBuffer.put(this.vertices);
		this.vertexBuffer.flip();

		glBindVertexArray(VAO);
		glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
		glBufferData(GL_ARRAY_BUFFER, this.vertexBuffer, GL_DYNAMIC_DRAW);
    }

    public Mesh(float[] vertices) {
		this.vertices = vertices;

		try ( MemoryStack stack = stackPush() ) {
			IntBuffer VAObuf = stack.mallocInt(1);
			glGenVertexArrays(VAObuf);
			this.VAO = VAObuf.get();

			glBindVertexArray(VAO);

			IntBuffer VBObuf = stack.mallocInt(1);
			glGenBuffers(VBObuf);
			this.VBO = VBObuf.get();
			glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
		}

		// Position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
		glEnableVertexAttribArray(0);

		// Vertex color
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4);
		glEnableVertexAttribArray(1);

		// UVs
		glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4);
		glEnableVertexAttribArray(2);

        this.upload();
    }

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public void draw() {
		if (this.texture!=null) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, this.texture.id);
		}
		glBindVertexArray(this.VAO);
		glDrawArrays(GL_TRIANGLES, 0, this.vertexBuffer.capacity() / 8);
	}
}
