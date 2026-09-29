package tetris.settings;

// 설정 화면에서 공통으로 쓰이는 값을 한 곳에 모아 둔 클래스
public final class SettingsConstants {
    // 화면 크기 선택 옵션을 문자열 형태로 정의
    public static final String[] RESOLUTION_PRESETS = {
        "720x1280",
        "1080x1920",
        "1440x2560"
    };

    // 각 해상도 문자열에 대응되는 실제 픽셀 값
    public static final int[][] RESOLUTION_VALUES = {
        {720, 1280},
        {1080, 1920},
        {1440, 2560}
    };

    // 객체 생성 방지용 생성자
    private SettingsConstants() {
        // 외부에서 새로운 사이즈의 화면을 만들지 못하게 막음
    }
}
