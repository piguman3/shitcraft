import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.*;
import org.joml.*;
import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;
 
//import all of the necessary OpenGL functions statically so they can be easily accessed
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
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

		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
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
		glClearColor(0.5f, 0.8f, 0.9f, 0.0f);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);

		// TODO: fix camera, create Input class so callbacks are used instead of glfwGetKey directly
		// TODO: World class with loadChunk() and a queue to inform ChunkMesher what to do
		// TODO: chunk mesh unloading
		// TODO: start work on networking (and server)

		Chunk[][][] world = new Chunk[8][8][8];

		Vector3i chunkSize = new Vector3i(32, 32, 32);
		Vector2i atlasSize = new Vector2i(16, 16);
		Texture atlas = Texture.loadTexture("atlas.png");
		ChunkMesher mesher = new ChunkMesher(chunkSize, atlasSize, atlas);

		NoiseGenerator noise = new NoiseGenerator(76688437);

		// World gen
		System.out.println("World gen started " + glfwGetTime());
		for (int cx=0; cx<world.length; cx++) {
			for (int cz=0; cz<world[cx].length; cz++) {
				for (int cy=0; cy<world[cx][cz].length; cy++) {
					world[cx][cz][cy] = new Chunk(chunkSize.x, chunkSize.y, chunkSize.z);
					Chunk chunk = world[cx][cz][cy];
					for (int x=0; x<chunk.blocks.length; x++) {
						for (int z=0; z<chunk.blocks[x][0].length; z++) {
							float sampleX = x + cx * chunkSize.x;
							float sampleY = z + cz * chunkSize.z;
							int height = (int)Math.round(noise.noise(sampleX / 1.5f, sampleY / 1.5f) * 20 + 40);
							height = Math.max(3, height);
							for (int y=0; y<chunk.blocks[x].length; y++) {
								int yCoord = y + cy * chunkSize.y;
								chunk.setBlock(height > yCoord ? Block.dirt : Block.air,
									x, y, z);
								if (height == yCoord) {
									chunk.setBlock(Block.grass, x, y, z);
								}
								if (height - 5 >= yCoord) {
									chunk.setBlock(Block.stone, x, y, z);
								}
								if (yCoord<40 && chunk.getBlock(x, y, z) == Block.air) {
									chunk.setBlock(Block.water, x, y, z);
								}
							}
						}
					}
				}
			}
		}
		System.out.println("World gen finished " + glfwGetTime());

		// Mesh gen
		System.out.println("Mesh gen started " + glfwGetTime());
		for (int cx=0; cx<world.length; cx++) {
			for (int cz=0; cz<world[cx].length; cz++) {
				for (int cy=0; cy<world[cx][cz].length; cy++) {
					Chunk center = world[cx][cz][cy];
					Chunk north = null;
					Chunk south = null;
					Chunk east = null;
					Chunk west = null;
					Chunk up = null;
					Chunk down = null;
					if (cz + 1 < world[0].length) {north = world[cx][cz+1][cy];}
					if (cz - 1 >= 0) south = world[cx][cz-1][cy];
					if (cx + 1 < world.length) east = world[cx+1][cz][cy];
					if (cx - 1 >= 0) west = world[cx-1][cz][cy];
					if (cy + 1 < world[0][0].length) up = world[cx][cz][cy+1];
					if (cy - 1 >= 0) down = world[cx][cz][cy-1];
					mesher.genChunk(new Vector3i(cx, cy, cz), center, north, south, east, west, up, down);
				}
			}
		}
		System.out.println("Mesh gen finished " + glfwGetTime());

		System.out.println("Shader started " + glfwGetTime());
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

		System.out.println("Shader finished " + glfwGetTime());

		Camera camera = new Camera(window,
			new Vector3f(0.0f, 0.0f, 0.0f), 
			new Vector3f(0.0f, 0.0f, 0.0f), 
			new Vector3f(0.0f, 1.0f, 0.0f), 
			90.0f, 0.01f);

		float deltaTime = 0.0f; // Time between current frame and last frame
		float lastFrame = 0.0f; // Time of last frame

		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			// Update
			camera.update(window, deltaTime);

			// Render
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			program.use();

			camera.apply(program, window);

			mesher.draw(program);

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();

			float currentFrame = (float)glfwGetTime();
			deltaTime = currentFrame - lastFrame;
			lastFrame = currentFrame;
		}
	}

	public static void main(String[] args) {
		new Main().run();
	}

}