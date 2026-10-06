package tetris.settings;

import java.io.IOException; // 파일 입출력 예외
import java.io.InputStream; // 설정 파일 입력 스트림
import java.io.OutputStream; // 설정 파일 출력 스트림
import java.nio.file.Files; // 파일 생성과 읽기/쓰기
import java.nio.file.Path; // 설정 파일 경로
import java.util.Arrays; // 저장된 키 문자열 분리
import java.util.List; // 키 목록
import java.util.Properties; // key=value 형식 저장소
import java.util.stream.Collectors; // 키 목록을 문자열로 변환

import javafx.scene.input.KeyCode; // 키 이름 변환
import tetris.settings.KeyBindingSettings.ActionType; // 역할 목록

// 사용자 설정을 홈 디렉터리의 properties 파일에 저장하고 불러오는 클래스
public final class SettingsStore {
    private static final Path SETTINGS_FILE = Path.of( // 사용자 설정 저장 위치
            System.getProperty("user.home"), ".se_tetris", "settings.properties");
    private static GameSettings gameSettings; // 마지막으로 저장되거나 불러온 게임 설정 스냅샷

    private SettingsStore() { // 인스턴스 생성 방지
    }

    // 저장된 설정을 현재 공유 설정 객체에 적용
    public static void load() { // 파일의 설정을 메모리로 불러옴
        if (!Files.exists(SETTINGS_FILE)) {
            gameSettings = createGameSettings(); // 설정 파일이 없으면 기본값 스냅샷을 생성
            return;
        }

        Properties properties = new Properties(); // 저장된 key=value 모음
        try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("설정 파일을 읽지 못했습니다: " + e.getMessage());
            gameSettings = createGameSettings(); // 읽기 실패 시 현재 기본값으로 게임 설정 생성
            return;
        }

        SettingsConstants.setResolutionPreset(properties.getProperty(
                "resolution", SettingsConstants.getResolutionPreset()));

        String colorMode = properties.getProperty("colorBlindMode");
        if (colorMode != null) {
            try {
                SettingsConstants.setColorBlindMode(
                        SettingsConstants.ColorBlindMode.valueOf(colorMode));
            } catch (IllegalArgumentException e) {
                System.err.println("저장된 색상 모드를 읽을 수 없습니다: " + colorMode);
            }
        }

        KeyBindingSettings inputSettings = SettingsConstants.getInputSettingData();
        for (ActionType actionType : ActionType.values()) {
            String savedKeys = properties.getProperty("key." + actionType.name());
            if (savedKeys == null || savedKeys.isBlank()) {
                continue;
            }

            List<KeyCode> keyCodes = Arrays.stream(savedKeys.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(SettingsStore::parseKeyCode)
                    .filter(keyCode -> keyCode != null)
                    .collect(Collectors.toList());
            inputSettings.setKeyCodes(actionType, keyCodes);
        }

        gameSettings = createGameSettings(); // 파일에서 읽은 값을 게임용 객체로 확정
    }

    // 현재 해상도, 색상 모드, 모든 역할의 키 배정을 저장하고 게임 설정 객체 반환
    public static GameSettings save() { // 저장 버튼에서 호출해 파일과 실행용 설정을 함께 갱신
        Properties properties = new Properties(); // 저장할 key=value 모음
        properties.setProperty("resolution", SettingsConstants.getResolutionPreset());
        properties.setProperty("colorBlindMode", SettingsConstants.getColorBlindMode().name());

        SettingsConstants.getInputSettingData().getAllBindings().forEach((actionType, keyCodes) -> {
            String values = keyCodes.stream()
                    .map(KeyCode::name)
                    .collect(Collectors.joining(","));
            properties.setProperty("key." + actionType.name(), values);
        });

        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(SETTINGS_FILE)) {
                properties.store(output, "Tetris user settings");
            }
        } catch (IOException e) {
            System.err.println("설정을 저장하지 못했습니다: " + e.getMessage());
            return null; // 저장에 실패하면 게임에 저장되지 않은 설정을 전달하지 않음
        }

        gameSettings = createGameSettings(); // 저장된 현재값으로 불변 객체 생성
        return gameSettings; // 생성한 객체를 게임 시작 흐름에서 사용
    }

    // 게임 시작 화면에서 마지막 저장 설정 객체를 조회
    public static GameSettings getGameSettings() {
        if (gameSettings == null) {
            gameSettings = createGameSettings(); // 아직 로드되지 않은 경우 기본값으로 초기화
        }
        return gameSettings;
    }

    // 공유 설정값을 해상도, 색상 모드, 키 배정이 포함된 객체로 복사
    private static GameSettings createGameSettings() {
        return new GameSettings(
                SettingsConstants.getResolutionPreset(),
                SettingsConstants.getScreenWidth(),
                SettingsConstants.getScreenHeight(),
                SettingsConstants.getColorBlindMode().getColorMode(),
                SettingsConstants.getInputSettingData().getAllBindings());
    }

    private static KeyCode parseKeyCode(String value) { // 문자열을 JavaFX 키 코드로 변환
        try {
            return KeyCode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}