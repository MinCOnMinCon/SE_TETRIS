package tetris.gamescene;

import tetris.block.data.BlockData;
import tetris.gamescene.board.BoardElement;

/**
 * 현재 게임 화면을 그리는 데 필요한 상태를 모아 전달한다.
 * record라 기본적인 겟 함수는 다 존재한다.
 * 렌더러는 board나 nextBlocks를 절대로 수정하면 안된다.
 */
public record SceneRenderState(
        BoardElement[][] board,
        long score,
        BlockData[] blockQueue,
        BlockData blockHolding) {}
