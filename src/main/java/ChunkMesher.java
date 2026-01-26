import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

import org.joml.*;
import java.lang.Math;

public class ChunkMesher {
    public HashMap<Vector3i, Mesh> meshes;
    public Vector3i chunkSize;
    public Texture atlas;
    public Vector2i atlasSize;

    public static HashMap<Vector3i, Float[]> FACES = new HashMap<>();

    static  {
        //back
        FACES.put(new Vector3i(0, 0, -1), new Float[] {
            0.0f, 0.0f, 0.0f, 0.75f, 0.75f, 0.75f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.75f, 0.75f, 0.75f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f, 0.75f, 0.75f, 0.75f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 0.75f, 0.75f, 0.75f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 0.75f, 0.75f, 0.75f, 1.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 0.75f, 0.75f, 0.75f, 1.0f, 0.0f});

        // front
        FACES.put(new Vector3i(0, 0, 1), new Float[] {
            0.0f, 0.0f, 1.0f, 0.9f, 0.9f, 0.9f, 0.0f, 0.0f,
            1.0f, 0.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 1.0f, 
            0.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.9f, 0.9f, 0.9f, 0.0f, 0.0f});

        // left
        FACES.put(new Vector3i(-1, 0, 0), new Float[] {
            0.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 0.9f, 0.9f, 0.9f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 0.9f, 0.9f, 0.9f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.9f, 0.9f, 0.9f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 1.0f, 1.0f});

        // right
        FACES.put(new Vector3i(1, 0, 0), new Float[] {
            1.0f, 1.0f, 1.0f, 0.8f, 0.8f, 0.8f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 0.8f, 0.8f, 0.8f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 0.8f, 0.8f, 0.8f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.8f, 0.8f, 0.8f, 1.0f, 0.0f,
            1.0f, 0.0f, 1.0f, 0.8f, 0.8f, 0.8f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 0.8f, 0.8f, 0.8f, 0.0f, 1.0f});

        // bottom
        FACES.put(new Vector3i(0, -1, 0), new Float[] {
            0.0f, 0.0f, 0.0f, 0.7f, 0.7f, 0.7f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 0.7f, 0.7f, 0.7f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.7f, 0.7f, 0.7f, 1.0f, 0.0f,
            1.0f, 0.0f, 1.0f, 0.7f, 0.7f, 0.7f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.7f, 0.7f, 0.7f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.7f, 0.7f, 0.7f, 0.0f, 1.0f});

        // top
        FACES.put(new Vector3i(0, 1, 0), new Float[] {
            0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f});
    }

    public ChunkMesher(Vector3i chunkSize, Vector2i atlasSize, Texture atlas) {
        this.meshes = new HashMap<>();

        this.chunkSize = chunkSize;
        this.atlasSize = atlasSize;
        this.atlas = atlas;
    }

    public void genChunk(Vector3i pos, Chunk chunk) {
        ArrayList<Float> data = new ArrayList<>();

        for (int x=0; x<chunk.blocks.length; x++) {
            for (int y=0; y<chunk.blocks[x].length; y++) {
                for (int z=0; z<chunk.blocks[x][y].length; z++) {
                    for (Vector3i key : FACES.keySet()) {
                        Vector3i checkPos = new Vector3i(x + key.x, y + key.y, z + key.z);
                        Block checkBlock = chunk.getBlock(checkPos);
                        Block currentBlock = chunk.getBlock(x, y, z);

                        if ((checkBlock==Block.air || checkBlock==null)&& 
                            currentBlock!=Block.air) {
                            Float[] face = FACES.get(key);
                            Collections.addAll(data, face);

                            for (int vertex=0; vertex<face.length; vertex+=Mesh.VERTEX_SIZE) {
                                int index = vertex + data.size() - face.length;
                                // Position
                                data.set(index, data.get(index) + x);
                                data.set(index + 1, data.get(index + 1) + y);
                                data.set(index + 2, data.get(index + 2) + z);

                                // UVs
                                float uvx = currentBlock.sideTextures.get(key) % this.atlasSize.x;
                                float uvy = this.atlasSize.y - 1 - Math.round(currentBlock.sideTextures.get(key) / this.atlasSize.x);
                                data.set(index + 6, (data.get(index + 6) + uvx) / 16.0f);
                                data.set(index + 7, (data.get(index + 7) + uvy) / 16.0f);
                            }
                        }
                    }
                }
            }
        }

        float[] vertices = new float[data.size()];

		for (int x=0; x<data.size(); x++) {
			vertices[x] = (float)data.get(x);
		}

        Mesh mesh = new Mesh(vertices);
        mesh.setTexture(this.atlas);
        this.meshes.put(pos, mesh);
    }

    public void draw(ShaderProgram program) {
        float time = (float)glfwGetTime();
        for (Vector3i key : this.meshes.keySet()) {
            Vector3i chunkPos = new Vector3i();
            key.mul(this.chunkSize, chunkPos);
            Matrix4f model = new Matrix4f()
                        .rotate((float)(time * Math.PI * 0.1f), 0.0f, 1.0f, 0.0f)
						.translate(new Vector3f(chunkPos).sub(new Vector3f(this.chunkSize).mul(0.5f)));

			program.setMat("model", model);
            this.meshes.get(key).draw();
        }
    }
}