import org.joml.*;
import org.lwjgl.system.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;
import java.nio.*;

public class Camera {
    Vector3f pos;
    Vector3f front;
    Vector3f up;
    float FOV;

    public Camera(Vector3f pos, Vector3f front, Vector3f up, float FOV) {
        this.pos = pos;
        this.front = front;
        this.up = up;
        this.FOV = FOV;
    }

    public void apply(ShaderProgram program, long window) {
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			glfwGetWindowSize(window, pWidth, pHeight);

            float ratio = (float)pWidth.get() / (float)pHeight.get();

            Matrix4f perspective = new Matrix4f().perspective((float) Math.toRadians(FOV), ratio, 0.01f, 100.0f);
            program.setMat("projection", perspective);
        }

        Vector3f target = new Vector3f(); 
        this.pos.add(this.front, target); 
        Matrix4f view = new Matrix4f().lookAt(this.pos, target, this.up);
        program.setMat("view", view);
    }
}