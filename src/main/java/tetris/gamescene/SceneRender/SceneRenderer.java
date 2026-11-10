package tetris.gamescene.scenerender;


import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.Objects;
import java.util.List;

import tetris.block.data.BlockData;
import tetris.gamescene.board.*;
import tetris.block.data.ColorType;
import tetris.settings.GameSettings.PlayMode;

public class SceneRenderer  {
    private final ItemRenderer itemRenderer = new ItemRenderer();
    private final BlockPatternRenderer blockPatternRenderer = new BlockPatternRenderer();
    private RenderConfig renderConfig;
    private Canvas boardCanvas;
    private Canvas blockQueueCanvas;
    private Canvas blockHoldingCanvas;
    private Canvas scoreCanvas;
    private Canvas attackedBoard;
    private final PlayMode playMode;
    private VBox leftVBox;
    private VBox rightVBox;
    private HBox contentHBox; 
    private BorderPane sceneLayout;

    

    public SceneRenderer(RenderConfig config) {
        this(config, PlayMode.SOLO);
    }

    public SceneRenderer(RenderConfig config, PlayMode playMode) {
        this.playMode = Objects.requireNonNull(playMode);
        SetRenderConfig(config);
    }

    // 설정 교체만 한다. 기존 화면의 크기와 배치를 다시 적용하는 처리는 추후 구현한다. 일시정지 메뉴에서 설정 교체가 일어났을 때 필요할 수 있다.
    private void SetRenderConfig(RenderConfig config) {
        renderConfig = Objects.requireNonNull(config);
    }

    private void CreateBoardCanvas(BoardElement[][] board){ // 보드 캔버스를 처음 생성하고 그린다.
        double blockSide = renderConfig.blockSide;
        boardCanvas = new Canvas(board[0].length * blockSide, board.length * blockSide);

    }

    private void UpdateBoardCanvas(BoardElement[][] board, BlockData currentBlock){ // 기존 보드 캔버스를 업데이트한다.
        BoardElement[][] renderBoard = MergeBoardAndCurrentBlock(board, currentBlock);
        // 절대 renderboard의 boardElement에 직접 접근하여 수정하지 말 것. 그렇게 하면 원본 보드에 영향이 가, 임시 보드를 만든 이유가 없다
        double blockSide = renderConfig.blockSide;
        GraphicsContext graphicsContext = boardCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        DrawBoard(graphicsContext, renderBoard, blockSide, 0, currentBlock);
        if (currentBlock != null) {
            itemRenderer.DrawBlockItem(graphicsContext, currentBlock,
                    currentBlock.GetX() * blockSide, currentBlock.GetY() * blockSide, blockSide);
        }
    }

    private void DrawBoard(GraphicsContext graphicsContext, BoardElement[][] board,
            double blockSide, double offsetY, BlockData currentBlock) {
        graphicsContext.setStroke(renderConfig.borderColor);

        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                double x = col * blockSide;
                double y = offsetY + row * blockSide;

                graphicsContext.setFill(board[row][col].getElementColor());
                graphicsContext.fillRect(x, y, blockSide, blockSide);
                // 아이템 기호를 그리는 칸은 배경 패턴을 생략한다.
                boolean isItemCell = currentBlock != null
                        && itemRenderer.IsItemCell(currentBlock, col - currentBlock.GetX(), row - currentBlock.GetY());
                if (renderConfig.colorMode != 0 && board[row][col].isBlock() && !isItemCell) {
                    blockPatternRenderer.DrawPattern(graphicsContext, board[row][col].getPatternType(),
                            x, y, blockSide, board[row][col].getElementColor());
                }
                graphicsContext.strokeRect(x, y, blockSide, blockSide);
            }
        }
    }

    private void CreateAttackBoard() {
        attackedBoard = new Canvas(renderConfig.attackedBoardWidth, renderConfig.attackedBoardHeight);
    }

    private void UpdateAttackedBoard(List<BoardElement[][]> versusBoards) {
        GraphicsContext graphicsContext = attackedBoard.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, attackedBoard.getWidth(), attackedBoard.getHeight());
        graphicsContext.setFill(renderConfig.panelColor);
        graphicsContext.fillRect(0, 0, attackedBoard.getWidth(), attackedBoard.getHeight());

        double blockSide = attackedBoard.getWidth() / renderConfig.attackedBoardColumns;
        int totalRows = 0;
        if (versusBoards != null) {
            for (BoardElement[][] board : versusBoards) {
                totalRows += board.length;
            }
            int row = renderConfig.attackedBoardRows
                    - Math.min(totalRows, renderConfig.attackedBoardRows);
            for (BoardElement[][] board : versusBoards) {
                if (row >= renderConfig.attackedBoardRows) break;
                DrawBoard(graphicsContext, board, blockSide, row * blockSide, null);
                row += board.length;
            }
        }
        graphicsContext.setStroke(renderConfig.borderColor);
        graphicsContext.strokeRect(0.5, 0.5, attackedBoard.getWidth() - 1, attackedBoard.getHeight() - 1);
    }

    // 기존 화면을 유지하면서 삭제된 행만 100ms마다 흰색/검은색으로 덮는다.
    public void DrawLineClearEffect(int[] clearedRows, int elapsedMillis) {
        GraphicsContext graphicsContext = boardCanvas.getGraphicsContext2D();
        double blockSide = renderConfig.blockSide;
        graphicsContext.setFill((elapsedMillis / 100) % 2 == 0
            ? Color.WHITE : Color.BLACK);

        for (int row : clearedRows) {
            if (row == -1) break;
            graphicsContext.fillRect(0, row * blockSide,
                boardCanvas.getWidth(), blockSide);
        }
    }

    // 원본 보드는 수정하지 않고, 현재 블록을 합친 렌더링용 임시 보드를 만든다.
    private BoardElement[][] MergeBoardAndCurrentBlock(BoardElement[][] board, BlockData currentBlock) {
        BoardElement[][] renderBoard = new BoardElement[board.length][];
        // 절대 renderboard의 boardElement에 직접 접근하여 수정하지 말 것. 그렇게 하면 원본 보드에 영향이 가, 임시 보드를 만든 이유가 없다
        for (int row = 0; row < board.length; row++) {
            renderBoard[row] = board[row].clone();
        }
        if (currentBlock == null) return renderBoard;

        boolean[][] shape = currentBlock.GetShape();
        ColorType colorType = currentBlock.GetColorType();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (!shape[row][col]) continue;
                int boardRow = currentBlock.GetY() + row;
                int boardCol = currentBlock.GetX() + col;
                if (boardRow < 0 || boardRow >= renderBoard.length
                        || boardCol < 0 || boardCol >= renderBoard[boardRow].length) continue;
                // 새 요소에 패턴을 복사해 원본 보드의 요소는 수정하지 않는다.
                BoardElement element = new BoardElement(colorType, true, currentBlock.GetColorMode());
                element.setPatternType(currentBlock.GetPatternType());
                renderBoard[boardRow][boardCol] = element;
            }
        }
        return renderBoard;
    }

    private void CreateBlockQueueCanvas() {
        blockQueueCanvas = new Canvas(renderConfig.blockQueueCanvasWidth,
                renderConfig.blockQueueCanvasHeight);
    }

    private void UpdateBlockQueueCanvas(BlockData[] blockQueue) {
        DrawPanel(blockQueueCanvas, "NEXT");
        if (blockQueue == null) return;
        int count = Math.min(renderConfig.blockQueueDisplayCount, blockQueue.length); // 화면에 보일 블럭 개수 설정 - 만약 수가 고정되면 없어도 됨
        for (int i = 0; i < count; i++) {
            double top = renderConfig.panelTitleHeight
                    + i * (renderConfig.previewSlotHeight + renderConfig.previewSlotGap);
            DrawBlockPreview(blockQueueCanvas, blockQueue[i], top, renderConfig.previewSlotHeight);
        }
    }

    private void CreateBlockHoldingCanvas() {
        blockHoldingCanvas = new Canvas(renderConfig.blockHoldingCanvasWidth,
                renderConfig.blockHoldingCanvasHeight);
    }

    private void UpdateBlockHoldingCanvas(BlockData blockHolding) {
        DrawPanel(blockHoldingCanvas, "HOLD");
        DrawBlockPreview(blockHoldingCanvas, blockHolding, renderConfig.panelTitleHeight,
                blockHoldingCanvas.getHeight() - renderConfig.panelTitleHeight);
    }

    private void CreateScoreCanvas() {
        scoreCanvas = new Canvas(renderConfig.scoreCanvasWidth, renderConfig.scoreCanvasHeight);
    }

    private void UpdateScoreCanvas(long score) {
        DrawPanel(scoreCanvas, "SCORE");
        GraphicsContext graphicsContext = scoreCanvas.getGraphicsContext2D();
        graphicsContext.setFont(Font.font(renderConfig.scoreFontSize));
        graphicsContext.setTextBaseline(javafx.geometry.VPos.CENTER);
        graphicsContext.fillText(Long.toString(score), scoreCanvas.getWidth() / 2,
                (renderConfig.panelTitleHeight + scoreCanvas.getHeight() - renderConfig.panelBottomPadding) / 2,
                scoreCanvas.getWidth() - 2 * renderConfig.scoreHPadding);
    }

    private void CreateLayout() { // 씬을 구성할 레이아웃 (Hbox, VBox, borderpane)를 생성해서 정렬한다.
        leftVBox = new VBox(renderConfig.canvasVGap, blockQueueCanvas);
        rightVBox = new VBox(renderConfig.canvasVGap, blockHoldingCanvas, scoreCanvas);
        if (playMode != PlayMode.SOLO) {
            rightVBox.getChildren().add(attackedBoard);
        }
        leftVBox.setAlignment(Pos.TOP_CENTER); 
        rightVBox.setAlignment(Pos.TOP_CENTER);

        // 세 영역을 묶어 창이 넓어져도 보드와 좌우 패널 사이의 간격을 유지한다.
        contentHBox = new HBox(renderConfig.sidePanelGap,
                leftVBox, boardCanvas, rightVBox);
        contentHBox.setAlignment(Pos.TOP_CENTER);
        contentHBox.setMinWidth(Region.USE_PREF_SIZE);
        contentHBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        sceneLayout = new BorderPane();
        sceneLayout.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(renderConfig.sceneColor,
                        javafx.scene.layout.CornerRadii.EMPTY, Insets.EMPTY)));
        sceneLayout.setPadding(new Insets(renderConfig.scenePadding));
        sceneLayout.setCenter(contentHBox);
        BorderPane.setAlignment(contentHBox, Pos.CENTER);
    }

    public Parent CreateRoot(SceneRenderState state){ // 게임 화면의 루트를 생성하고 초기 상태를 그린다.
        Objects.requireNonNull(state, "state must not be null");

        CreateBoardCanvas(state.board());
        CreateBlockQueueCanvas();
        CreateBlockHoldingCanvas();
        CreateScoreCanvas();
        if (playMode != PlayMode.SOLO) {
            CreateAttackBoard();
        }
        CreateLayout();
        UpdateRoot(state);
        return sceneLayout;
    }
    public void UpdateRoot(SceneRenderState state){ // 게임 씬 업데이트
        Objects.requireNonNull(state, "state must not be null");
        UpdateBoardCanvas(state.board(), state.currentBlock());
        UpdateBlockQueueCanvas(state.blockQueue());
        UpdateBlockHoldingCanvas(state.blockHolding());
        UpdateScoreCanvas(state.score());
        if (playMode != PlayMode.SOLO) {
            UpdateAttackedBoard(state.versusBoards());
        }
        
    }




    // 주어진 캔바스에 패널을 그리는 함수. 점수판, 블럭 큐, 블럭 홀딩의 기본 패널을 그리기 위한 함수.
    private void DrawPanel(Canvas canvas, String title) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setFill(renderConfig.panelColor);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setStroke(renderConfig.borderColor);
        graphicsContext.strokeRect(0.5, 0.5, canvas.getWidth() - 1, canvas.getHeight() - 1);
        graphicsContext.setFill(renderConfig.textColor);
        graphicsContext.setFont(Font.font(renderConfig.titleFontSize));
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(javafx.geometry.VPos.CENTER);
        graphicsContext.fillText(title, canvas.getWidth() / 2, renderConfig.panelTitleHeight / 2,
                canvas.getWidth() - 2 * renderConfig.panelTitleHPadding);
    }

    // 다음에 올 블럭, 블럭 홀딩에 나타나는 블럭을 그리기 위한 함수
    private void DrawBlockPreview(Canvas canvas, BlockData block, double top, double height) { 
        if (block == null) return;
        boolean[][] shape = block.GetShape(); 
        int minRow = shape.length;
        int maxRow = -1;
        int minCol = Integer.MAX_VALUE;
        int maxCol = -1;
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (!shape[row][col]) continue;
                minRow = Math.min(minRow, row);
                maxRow = Math.max(maxRow, row);
                minCol = Math.min(minCol, col);
                maxCol = Math.max(maxCol, col);
            }
        }
        
        if (maxRow < 0) return;
        int rows = maxRow - minRow + 1;
        int cols = maxCol - minCol + 1;
        double side = Math.min(renderConfig.previewBlockSide,
                Math.min((canvas.getWidth() - 2 * renderConfig.blockPreviewPadding) / cols,
                        (height - 2 * renderConfig.blockPreviewPadding) / rows));
        if (side <= 0) return;
        double startX = (canvas.getWidth() - cols * side) / 2;
        double startY = top + (height - rows * side) / 2;
        Color color = block.GetColor();
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(color);
        graphicsContext.setStroke(renderConfig.borderColor);
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (!shape[row][col]) continue;
                double x = startX + (col - minCol) * side;
                double y = startY + (row - minRow) * side;
                graphicsContext.fillRect(x, y, side, side);
                if (renderConfig.colorMode != 0 && !itemRenderer.IsItemCell(block, col, row)) {
                    blockPatternRenderer.DrawPattern(graphicsContext, block.GetPatternType(),
                            x, y, side, color);
                }
                graphicsContext.strokeRect(x, y, side, side);
            }
        }
        itemRenderer.DrawBlockItem(graphicsContext, block,
                startX - minCol * side, startY - minRow * side, side);
    }
}

// create함수는 빈 캔버스, 빈 씬 등을 최초로 생성만 함.
// 내용물은 Update 함수로 채워야 함.
