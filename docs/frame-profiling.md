# 프레임 성능 측정

기본 실행에서는 측정 객체를 생성하지 않는다. Gradle 실행 시 다음 플래그로 켠다.

```powershell
.\gradlew.bat run -PframeProfile=true
```

IDE에서 Main 또는 GameSceneTest를 직접 실행한다면 VM 옵션에 `-Dtetris.profile=true`를 넣는다.

일시정지 또는 게임 종료(StopGame) 시 작업 디렉터리의 `performance-reports/frame-profile-*.txt`에 UTF-8 보고서를 저장한다. 같은 게임에서는 같은 파일을 갱신한다. 창을 바로 닫거나 프로세스를 강제 종료하면 마지막 결과가 저장되지 않을 수 있으므로, 중간 결과를 남기려면 먼저 일시정지한다.

보고서 내용:

- 전체 유효 호출 간격 수
- 16.7ms 이하 / 초과 비율
- 100ms 초과 비율 (전체 유효 간격 기준, 정확히 50ms는 제외)
- 최대 호출 간격과 첫 측정부터의 경과 시간 (경과 시간에는 일시정지 포함)
- 최대 간격 직전 핸들러 실행 시간
- OS, Java 버전, 논리 프로세서 수

첫 호출과 재개 후 첫 호출에는 직전 간격이 없으므로 간격 집계에서 제외한다. 실행 시간과 시작 간격 모두 System.nanoTime을 사용한다. 직전 시작부터 현재 시작까지의 간격에 직전 핸들러의 작업이 포함되기 때문에 직전 실행 기록을 연결한다. 게임 처리 함수 updateFrame을 FrameProfiler.measure로 감싸 시간만 측정하며 함수 호출 경로는 기록하지 않는다. 전체 프레임 이력은 보관하지 않는다.

16.7ms는 분포 확인 기준이며 조금 초과했다고 성능 실패를 뜻하지 않는다. 이 보고서는 JavaFX 핸들러의 실행/호출 시간을 측정하며 모니터 표시 완료 시간이나 입력 반응 시간을 측정하지 않는다. 파일 저장은 측정 종료 후 수행하며, 측정 코드 자체의 작은 실행 비용은 존재한다.

비교 시 CPU 모델, RAM, 화면 주사율, 게임 모드, 플레이 시간과 조작 조건을 별도로 기록한다.

검증:

```powershell
.\gradlew.bat test --tests tetris.gamescene.FrameProfilerTest --tests tetris.gamescene.GameControllerTest
```
