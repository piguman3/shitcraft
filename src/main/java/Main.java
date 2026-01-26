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

		glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
			glViewport(0, 0, width, height);
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

		glEnable(GL_DEPTH_TEST);

		// TODO: camera debug input
		// TODO: World class with loadChunk() and a queue to inform ChunkMesher what to do
		// TODO: chunk mesh unloading
		// TODO: start work on networking (and server)

		Chunk[][][] world = new Chunk[3][3][1];

		Vector3i chunkSize = new Vector3i(16, 16, 16);
		Vector2i atlasSize = new Vector2i(16, 16);
		Texture atlas = Texture.loadTexture("atlas.png");
		ChunkMesher mesher = new ChunkMesher(chunkSize, atlasSize, atlas);

		NoiseGenerator noise = new NoiseGenerator(67);

		// World gen
		for (int cx=0; cx<world.length; cx++) {
			for (int cz=0; cz<world[cx].length; cz++) {
				for (int cy=0; cy<world[cx][cz].length; cy++) {
					world[cx][cz][cy] = new Chunk(chunkSize.x, chunkSize.y, chunkSize.z);
					Chunk chunk = world[cx][cz][cy];
					for (int x=0; x<chunk.blocks.length; x++) {
						for (int z=0; z<chunk.blocks[x][0].length; z++) {
							int height = (int)Math.round(noise.noise(x + cx * chunkSize.x, z + cz * chunkSize.z) * 16 + 8);
							for (int y=0; y<chunk.blocks[x].length; y++) {
								chunk.setBlock(height > y ? Block.dirt : Block.air,
									x, y, z);
								if (height == y)
									chunk.setBlock(Block.grass, x, y, z);
							}
						}
					}
				}
			}
		}

		// Mesh gen
		for (int cx=0; cx<world.length; cx++) {
			for (int cz=0; cz<world[cx].length; cz++) {
				for (int cy=0; cy<world[cx][cz].length; cy++) {
					Chunk center = world[cx][cz][cy];
					Chunk north = null;
					Chunk south = null;
					Chunk east = null;
					Chunk west = null;
					if (cz + 1 < world[0].length) {north = world[cx][cz+1][cy];}
					if (cz - 1 >= 0) south = world[cx][cz-1][cy];
					if (cx + 1 < world.length) east = world[cx+1][cz][cy];
					if (cx - 1 >= 0) west = world[cx-1][cz][cy];
					mesher.genChunk(new Vector3i(cx, cy, cz), center, north, south, east, west);
				}
			}
		}

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

		Camera camera = new Camera(
			new Vector3f(0.0f, 12.0f, -12.0f), 
			new Vector3f(0.0f, -1.0f, 1.0f), 
			new Vector3f(0.0f, 1.0f, 0.0f), 90.0f);

		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			program.use();

			camera.apply(program, window);

			mesher.draw(program);

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