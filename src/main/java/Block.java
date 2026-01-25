import java.util.HashMap;

public class Block {
    public int id;
    public int[] sideTextures;
    public static HashMap<Integer, Block> types = new HashMap<>();

    public Block(int id, int top, int side, int bottom) {
        this.sideTextures = new int[3];
        this.sideTextures[0] = top;
        this.sideTextures[1] = side;
        this.sideTextures[2] = bottom;
        this.id = id;
        types.put(id, this);
    }

    // Cool list of blocks (in the exact same terrible way Minecraft (beta) did it)
    public static Block air = new Block(0, 0, 0, 0);
    public static Block grass = new Block(1, 2, 0, 1);
    public static Block dirt = new Block(2, 1, 1, 1);
}
