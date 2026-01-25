import org.joml.*;
import java.lang.Math;

public class Chunk {
    public int[][][] blocks;
    public boolean dirty;

    public Chunk(int sizex, int sizey, int sizez) {
        this.blocks = new int[sizex][sizey][sizez];
        this.dirty = true;
    }

    public Block getBlock(int x, int y, int z) {
        try {
            return Block.types.get(this.blocks[x][y][z]);
        } catch (Exception e) {
            return null;
        }
    }

    public Block getBlock(Vector3i vector) {
        return this.getBlock(vector.x, vector.y, vector.z);
    }

    public void setBlock(Block block, int x, int y, int z) {
        if (this.getBlock(x, y, z)!=block) {
            this.dirty = true;
            this.blocks[x][y][z] = block.id;
        }
    }
}
