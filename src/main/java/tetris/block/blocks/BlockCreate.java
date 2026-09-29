package tetris.block.blocks;

/*
    BlockCreate.Block(int)
        case 0:     return new BlockI();
        case 1:     return new BlockJ();
        case 2:     return new BlockL();
        case 3:     return new BlockO();
        case 4:     return new BlockS();
        case 5:     return new BlockT();
        case 6:     return new BlockZ();
 */

public class BlockCreate {
    public static Block Block(int idx) {
        switch (idx) {
            case 0:     return new BlockI();

            case 1:     return new BlockJ();

            case 2:     return new BlockL();

            case 3:     return new BlockO();

            case 4:     return new BlockS();

            case 5:     return new BlockT();

            case 6:     return new BlockZ();
            
            default:    throw new IllegalArgumentException("Invalid block index");
        }
    }
}
