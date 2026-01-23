import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.joml.*;
import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;
 
//import all of the necessary OpenGL functions statically so they can be easily accessed
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

	// The window handle
	private long window;

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(800, 600, "shitcraft Lambda v0.0.1", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// TODO: change mesh format so vertices, colors and textureUVs are separate arrays
		float[] verticesTri = {
			// positions        // colors         // UVs
			-0.5f, -0.5f, 0.5f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
			0.5f, -0.5f, 0.5f,  1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			-0.5f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			-0.5f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.5f, -0.5f, 0.5f,  1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.5f, 0.5f, 0.5f,   1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
		};

		Texture texture = Texture.loadTexture("test.png");

		Mesh triangle = new Mesh(verticesTri);
		triangle.setTexture(texture);

		Shader vertexShader = new Shader(GL_VERTEX_SHADER);
		Shader fragmentShader = new Shader(GL_FRAGMENT_SHADER);

		try {
			vertexShader.loadFile("vert.glsl");
			fragmentShader.loadFile("frag.glsl");
		} catch (Exception e) {
			System.out.println("Error loading shaders: " + e);
		}

		ShaderProgram program = new ShaderProgram();
		program.attachShader(vertexShader);
		program.attachShader(fragmentShader);

		try {
			program.link();
		} catch (Exception e) {
			System.out.println(e);
		}

		vertexShader.delete();
		fragmentShader.delete();

		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		program.use();
		Matrix4f perspective = new Matrix4f().perspective((float) Math.toRadians(45.0f), 8.0f/6.0f, 0.01f, 100.0f);
		program.setMat("projection", perspective);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			program.use();

			Matrix4f view = new Matrix4f().lookAt(0.0f, 0.0f, 4.0f,
                    					 0.0f, 0.0f, 0.0f,
                      						 0.0f, 1.0f, 0.0f);
			program.setMat("view", view);

			Matrix4f model = new Matrix4f().rotate((float)(glfwGetTime() * Math.PI * 0.5f), 1.0f, 0.0f, 0.0f);
			program.setMat("model", model);

			triangle.draw();

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		new Main().run();
	}

}