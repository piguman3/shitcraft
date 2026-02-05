import java.util.HashMap;

import org.joml.*;
import java.lang.Math;

public class World {
    public HashMap<Vector3i, Chunk> chunks = new HashMap<>();
    public Vector3i chunkSize;

    // MOVE TO WORLD LOADER CLASS ON SEPARATE THREAD
    private NoiseGenerator noise = new NoiseGenerator(76688437);

    public World(Vector3i chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void loadChunk(Vector3i chunkCoord) {
        // MOVE TO WORLD LOADER CLASS ON SEPARATE THREAD
        Chunk chunk = new Chunk(chunkSize.x, chunkSize.y, chunkSize.z);
        for (int x=0; x<chunk.blocks.length; x++) {
            for (int z=0; z<chunk.blocks[x][0].length; z++) {
                float sampleX = x + chunkCoord.x * chunkSize.x;
                float sampleY = z + chunkCoord.z * chunkSize.z;
                int height = (int)Math.round(noise.noise(sampleX / 1.5f, sampleY / 1.5f) * 20 + 40);
                height = Math.max(3, height);
                for (int y=0; y<chunk.blocks[x].length; y++) {
                    int yCoord = y + chunkCoord.y * chunkSize.y;
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
        this.chunks.put(chunkCoord, chunk);
    }

    public Chunk getChunk(Vector3i chunkCoord) {
        return this.chunks.get(chunkCoord);
    }
}
