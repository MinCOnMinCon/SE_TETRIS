package tetris.onlinemulti;

import tetris.block.data.BlockData;
import tetris.gamescene.GameController;
import tetris.gamescene.board.BoardElement;
import tetris.gamescene.scenerender.SceneRenderState;

// 게임 갱신과 같은 JavaFX 스레드에서 자기 상태를 복사해 전송 데이터에 채운다.
public class NetworkDataExtractor {
    private final GameController controller;

    // 매 프레임 상태를 조회할 자기 게임 컨트롤러를 보관한다.
    public NetworkDataExtractor(GameController controller) {
        this.controller = controller;
    }

    // 현재 화면 정보와 소비한 새 공격을 복사해 받은 데이터에 넣고 관리자 담당 필드는 유지한다.
    public void ExtractInto(NetworkData data) {
        SceneRenderState state = controller.GetRenderState();
        data.SetBoard(CopyBoard(state.board()));
        data.SetBlock(CopyBlock(state.currentBlock()));

        BlockData[] nextBlocks = new BlockData[state.blockQueue().length];
        for (int i = 0; i < nextBlocks.length; i++) {
            nextBlocks[i] = CopyBlock(state.blockQueue()[i]);
        }
        data.SetNextBlocks(nextBlocks);
        data.SetBlockHolding(CopyBlock(state.blockHolding()));
        data.SetScore(controller.GetScore());
        data.SetDeletedBoard(CopyBoard(controller.GetDelBoard()));

        // TODO: NetworkData에 표시용 공격 대기열 필드가 생기면 state.versusBoards()를 복사해 설정한다.
        // TODO: NetworkData에 이펙트 실행 여부 필드가 생기면 controller.GetLineClearEffectRunning()을 설정한다.
        // TODO: NetworkData에 이펙트 ID 필드가 생기면 controller.GetEffectId()를 설정한다.
        // TODO: NetworkData에 삭제 행 위치 필드가 생기면 controller.GetClearedRows()를 복사해 설정한다.
    }

    // 고정 보드와 새 공격에 공용으로 사용하며 각 셀까지 복사하고 없는 공격은 null로 반환한다.
    private BoardElement[][] CopyBoard(BoardElement[][] source) {
        if (source == null) return null;
        BoardElement[][] copy = new BoardElement[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = new BoardElement[source[row].length];
            for (int col = 0; col < source[row].length; col++) {
                BoardElement element = source[row][col];
                copy[row][col] = new BoardElement(element.getElementColorType(), element.isBlock(),
                        element.getElementColorMode());
                copy[row][col].setPatternType(element.getPatternType());
                copy[row][col].setElementColor(element.getElementColor());
            }
        }
        return copy;
    }

    // 현재 블록과 NEXT 및 HOLD에 공용으로 사용하며 모양과 아이템 위치까지 복사한다.
    private BlockData CopyBlock(BlockData source) {
        if (source == null) return null;
        boolean[][] shape = new boolean[source.GetShape().length][];
        for (int row = 0; row < shape.length; row++) {
            shape[row] = source.GetShape()[row].clone();
        }
        int[] itemPos = source.GetItemPos();
        BlockData copy = new BlockData(shape, source.GetColorType(), source.GetColorMode(),
                source.GetX(), source.GetY(), source.IsItem(), source.GetItemType(),
                itemPos == null ? null : itemPos.clone());
        copy.SetPatternType(source.GetPatternType());
        return copy;
    }
}
