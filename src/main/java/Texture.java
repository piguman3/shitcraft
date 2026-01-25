import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;

public class Texture {
    public int width;
    public int height;
    public int id;

    private static HashMap<String, Texture> map = new HashMap<>();

    public Texture(int width, int height, int id) {
        this.width = width;
        this.height = height;
        this.id = id;
    }

    public static Texture loadTexture(String filename) {
        if(map.containsKey(filename)) {
            return map.get(filename);
        }

		int width;
		int height;
		ByteBuffer imageBuffer;
		int id = -1;

		try ( MemoryStack stack = stackPush() ) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);
			IntBuffer tid = stack.mallocInt(1);

			ByteBuffer imageContents = Resource.getResourceFileAsByteBuffer("atlas.png");

			STBImage.stbi_set_flip_vertically_on_load(true);
			imageBuffer = STBImage.stbi_load_from_memory(imageContents, w, h, channels, 4);
			
			width = w.get();
			height = h.get();
		
			glGenTextures(tid);
			id = tid.get();
			glBindTexture(GL_TEXTURE_2D, id);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGBA,
						 GL_UNSIGNED_BYTE, imageBuffer);
			glGenerateMipmap(GL_TEXTURE_2D);

            map.put(filename, new Texture(width, height, id));

			STBImage.stbi_image_free(imageBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

        return map.get(filename);
    }
}
