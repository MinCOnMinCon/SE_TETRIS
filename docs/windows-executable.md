# Windows 실행파일 생성

```powershell
.\gradlew.bat createExe
```

Gradle이 Launch4j를 내려받아 실행하므로 별도 Launch4j 설치는 필요 없다.
Windows에서 빌드하면 `build/launch4j/Tetris.exe`와 실행에 필요한 `lib` 폴더가 생성된다.
배포할 때는 실행파일과 `lib` 폴더를 함께 전달해야 한다.

실행 PC에는 64비트 Java 21 이상이 필요하다. JAVA_HOME 또는 PATH에서 Java를 찾으며,
JavaFX 라이브러리는 lib에 포함된다. 실행파일은 자신의 폴더를 작업 디렉터리로 사용한다.

게임 아이콘은 아직 지정하지 않았다. `.ico` 파일을 준비한 뒤 build.gradle의
launch4j 블록에서 icon 경로를 지정하고 createExe를 다시 실행한다.

이 명령은 실행파일 생성용이며 테스트를 실행하지 않는다. 기존 `build` 명령과 별개다.
