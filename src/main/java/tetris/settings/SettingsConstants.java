package tetris.settings;

// 설정 화면에서 공통으로 쓰이는 값을 한 곳에 모아 둔 클래스
public final class SettingsConstants {
    // 설정 화면과 게임 입력 처리에서 공유하는 키 설정
    private static final KeyBindingSettings INPUT_SETTING_DATA = new KeyBindingSettings();

    public static KeyBindingSettings getInputSettingData() { // 현재 키 설정 객체 반환
        return INPUT_SETTING_DATA; // 설정 화면과 저장소가 같은 객체를 사용
    }

    // 색상 모드를 블록 색상 모드 번호와 연결
    public enum ColorBlindMode {
        NORMAL(0, "일반 모드"),
        RED_GREEN(1, "적록색맹 모드"),
        BLUE_YELLOW(2, "청황색맹 모드");

        private final int colorMode; // 게임에서 사용할 색상 모드 번호
        private final String displayName; // 설정 화면에 보여줄 이름

        ColorBlindMode(int colorMode, String displayName) { // 모드의 번호와 표시명 저장
            this.colorMode = colorMode; // 색상 모드 번호 저장
            this.displayName = displayName; // 화면 표시명 저장
        }

        //어느 색맹 모드인지를 구분해주는 int값 도출
        public int getColorMode() {
            return colorMode;
        }

        //재정의를 통해 읽기 힘든 주소값을 실제 저장된 값을 호출
        @Override
        public String toString() {
            return displayName;
        }
    }

    // 현재 선택된 색상 모드
    private static ColorBlindMode colorBlindMode = ColorBlindMode.NORMAL; // 현재 색상 모드

    //캡슐화를 통해 수정은 불가하되 읽기만 가능
    public static ColorBlindMode getColorBlindMode() {
        return colorBlindMode;
    }

    //혹시라도 잘못된 값이 들어오면 기존의 설정을 유지
    public static void setColorBlindMode(ColorBlindMode mode) {
        if (mode != null) {
            colorBlindMode = mode;
        }
    }

    // 화면 크기 선택 옵션을 문자열 형태로 정의
    public static final String[] RESOLUTION_PRESETS = {
        "1280x720",
        "1920x1080",
        "2560x1440"
    };

    // 각 해상도 문자열에 대응되는 실제 픽셀 값
    public static final int[][] RESOLUTION_VALUES = {
        {1280, 720},
        {1920, 1080},
        {2560, 1440}
    };

    private static String resolutionPreset = RESOLUTION_PRESETS[0];

    public static String getResolutionPreset() { // 현재 해상도 이름 반환
        return resolutionPreset; // 예: 720x1280
    }

    public static void setResolutionPreset(String preset) { // 지원되는 해상도만 저장
        for (String supportedPreset : RESOLUTION_PRESETS) {
            if (supportedPreset.equals(preset)) {
                resolutionPreset = preset;
                return;
            }
        }
    }

    public static int getScreenWidth() { // 현재 화면의 가로 크기 반환
        return getResolutionValues()[0];
    }

    public static int getScreenHeight() { // 현재 화면의 세로 크기 반환
        return getResolutionValues()[1];
    }

    private static int[] getResolutionValues() {
        for (int index = 0; index < RESOLUTION_PRESETS.length; index++) {
            if (RESOLUTION_PRESETS[index].equals(resolutionPreset)) {
                return RESOLUTION_VALUES[index];
            }
        }
        return RESOLUTION_VALUES[0];
    }

    // 객체 생성 방지용 생성자
    private SettingsConstants() {
        // 외부에서 새로운 사이즈의 화면을 만들지 못하게 막음
    }
}
