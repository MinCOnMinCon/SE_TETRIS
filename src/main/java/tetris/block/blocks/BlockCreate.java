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
        switch (idx) {
            case 0:     return new BlockI(0);
            case 1:     return new BlockJ(0);
            case 2:     return new BlockL(0);
            case 3:     return new BlockO(0);
            case 4:     return new BlockS(0);
            case 5:     return new BlockT(0);
            case 6:     return new BlockZ(0);            
            default:    throw new IllegalArgumentException("Invalid block index");
        }
    }
}
