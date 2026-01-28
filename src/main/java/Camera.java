import org.joml.*;
import org.lwjgl.system.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;
import java.nio.*;

// Camera also handles mouse movement.
// TODO: should cameras do this? maybe a base Camera class could exist and then a separate "DebugCamera" class
// TODO: refactor
public class Camera {
    Vector3f pos;
    Vector3f front;
    Vector3f dir;
    Vector3f up;
    Vector2f lastMousePos;
    float FOV;
    float sensitivity;

    public Vector3f anglesToVec(Vector3f angles) {
        Vector3f front = new Vector3f(
            (float)(Math.cos(this.dir.y) * Math.cos(this.dir.x)),
            (float)Math.sin(this.dir.x),
            (float)(Math.sin(this.dir.y) * Math.cos(this.dir.x)));
        return front.normalize();
    }

    public Camera(long window, Vector3f pos, Vector3f dir, Vector3f up, float FOV, float sensitivity) {
        this.pos = pos;
        this.dir = dir;
        this.front = anglesToVec(this.dir);
        this.up = up;
        this.sensitivity = sensitivity;

        try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			glfwGetWindowSize(window, pWidth, pHeight);

            lastMousePos = new Vector2f(pWidth.get() / 2, pHeight.get() / 2);
        }

        glfwSetCursorPosCallback(window, (windowid, xpos, ypos) -> {
            float xoffset = (float)xpos - lastMousePos.x;
            float yoffset = lastMousePos.y - (float)ypos; // reversed: y ranges bottom to top
            
            lastMousePos.x = (float)xpos;
            lastMousePos.y = (float)ypos;

            xoffset *= this.sensitivity;
            yoffset *= this.sensitivity;

            this.dir.x += yoffset;
            this.dir.y += xoffset;

            if(this.dir.x > Math.PI / 2 - 0.001f)
                this.dir.x = (float)Math.PI / 2 - 0.001f;
            if(this.dir.x < -(Math.PI / 2 - 0.01f))
                this.dir.x = -((float)Math.PI / 2 - 0.01f);

            this.front = anglesToVec(this.dir);
        });

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

    public void update(Long window, float deltaTime) {
        float cameraSpeed = 10.0f * deltaTime; // adjust accordingly

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            this.pos.add(new Vector3f(this.front).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            this.pos.sub(new Vector3f(this.front).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            this.pos.sub(new Vector3f(this.front).cross(this.up).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            this.pos.add(new Vector3f(this.front).cross(this.up).mul(cameraSpeed));
    }
}