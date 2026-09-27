package tetris.block.move;

import tetris.block.data.BlockData;

public class BlockMove {
    public static void MoveRight(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(1, blockData, board)) {
            blockData.SetX(blockData.GetX() + 1);
        }
    }
    public static void MoveLeft(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(-1, blockData, board)) {
            blockData.SetX(blockData.GetX() - 1);
        }
    }
    public static void MoveDown(BlockData blockData, int[][] board) {
        if (CanMove.IsNoBlock(0, blockData, board)) {
            blockData.SetY(blockData.GetY() + 1);
        }
    }
}