import java.util.HashMap;

import org.joml.*;
import java.lang.Math;

public class Block {
    public int id;
    public HashMap<Vector3i, Integer> sideTextures;
    public static HashMap<Integer, Block> types = new HashMap<>();

    public Block(int id, int top, int side, int bottom) {
        this.sideTextures = new HashMap<>();

        this.sideTextures.put(new Vector3i(0, 0, -1), side);
        this.sideTextures.put(new Vector3i(0, 0, 1), side);
        this.sideTextures.put(new Vector3i(0, -1, 0), bottom);
        this.sideTextures.put(new Vector3i(0, 1, 0), top);
        this.sideTextures.put(new Vector3i(-1, 0, 0), side);
        this.sideTextures.put(new Vector3i(1, 0, 0), side);

        this.id = id;
        types.put(id, this);
    }

    // Cool list of blocks (in the exact same terrible way Minecraft (beta) did it)
    public static Block air = new Block(0, 0, 0, 0);
    public static Block grass = new Block(1, 2, 0, 1);
    public static Block dirt = new Block(2, 1, 1, 1);
    public static Block water = new Block(3, 3, 3, 3);
    public static Block stone = new Block(4, 4, 4, 4);
}
