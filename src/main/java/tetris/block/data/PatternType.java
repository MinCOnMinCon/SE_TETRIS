package tetris.block.data;

// BlockData, BoardElement, 패턴 렌더러가 공통으로 사용하는 무늬 종류.
public enum PatternType {
    NONE,                 // 무늬 없음
    VERTICAL_STRIPES,     // I: 세로 줄무늬
    HORIZONTAL_STRIPES,   // J: 가로 줄무늬
    DIAGONAL_UP,          // L: 오른쪽 위로 올라가는 사선 (/)
    CHECKERBOARD,         // O: 체커보드
    DIAGONAL_DOWN,        // S: 오른쪽 아래로 내려가는 사선 (역슬래시)
    GRID,                 // T: 가로·세로 격자무늬
    DOTS                  // Z: 작은 점무늬
}
