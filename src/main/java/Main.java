import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

//import the OpenGL classes, Buffer utilities, and GLFW classes
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
 
//import all of the necessary OpenGL functions statically so they can be easily accessed
import static org.lwjgl.opengl.GL11.*;
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
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
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

		String vertexShaderStr = "#version 130\r\n" +
						"\r\n" +
						"in vec3 aPos;\r\n" +
						"\r\n" +
						"void main() {\r\n" +
						"    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);\r\n" +
						"}";

		String fragmentShaderStr = "#version 130\r\n" +
						"out vec4 FragColor;\r\n" +
						"void main()\r\n" +
						"{\r\n" +
						"    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\r\n" +
						"}";

		// Set the clear color
		glClearColor(0.0f, 0.0f, 1.0f, 0.0f);

		float[] vertices = {
			-0.5f, -0.5f, 0.0f,
			0.5f, -0.5f, 0.0f,
			0.0f, 0.5f, 0.0f
		};
		
		// TODO: move all this trash to proper classes (i.e shaders)
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
		vertexData.put(vertices);
		vertexData.flip();

		int VAO;
		int VBO;
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer VAObuf = stack.mallocInt(1);
			glGenVertexArrays(VAObuf);
			VAO = VAObuf.get();

			glBindVertexArray(VAO);

			IntBuffer VBObuf = stack.mallocInt(1);
			glGenBuffers(VBObuf);
			VBO = VBObuf.get();
			glBindBuffer(GL_ARRAY_BUFFER, VBO);
			glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
		}

		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexShaderStr);
		glCompileShader(vertexShader);

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentShaderStr);
		glCompileShader(fragmentShader);

		if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == 0) {
			System.out.println("Error compiling Shader code: " + glGetShaderInfoLog(vertexShader, 1024));
		}

		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == 0) {
			System.out.println("Error compiling Shader code: " + glGetShaderInfoLog(fragmentShader, 1024));
		}

		int shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);

		if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == 0) {
			System.out.println("Error linking Shader code: " + glGetProgramInfoLog(shaderProgram, 1024));
		}

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0);
		glEnableVertexAttribArray(0);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glUseProgram(shaderProgram);
			glBindVertexArray(VAO);
			glDrawArrays(GL_TRIANGLES, 0, 3);

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