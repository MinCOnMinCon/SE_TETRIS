package tetris.block.blocks;

public class BlockCreate {
    public static Block Block(int idx, int colorMode) {
        switch (idx) {
            case 0:     return new BlockI(colorMode);
            case 1:     return new BlockJ(colorMode);
            case 2:     return new BlockL(colorMode);
            case 3:     return new BlockO(colorMode);
            case 4:     return new BlockS(colorMode);
            case 5:     return new BlockT(colorMode);
            case 6:     return new BlockZ(colorMode);            
            default:    throw new IllegalArgumentException("Invalid block index");
        }
    }
    public static Block Block(int idx) {
        return BlockCreate.Block(idx, 0);
    }
}
