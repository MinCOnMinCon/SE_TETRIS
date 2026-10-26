package tetris.gamescene;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.LongSupplier;

/** FX 스레드에서만 사용한다. 전체 프레임 대신 집계와 최대 간격 기록만 보관한다. */
public final class FrameProfiler {
    private final LongSupplier clock;
    private final Path reportPath;
    private boolean active, hasPrevious, hasOrigin, savePending;
    private long start, previousStart, origin, previousExecution;
    private long count, withinBudget, over100ms, maxInterval, maxExecution, maxAt;
    /*
     * 변수 설명 (시간 값의 단위는 나노초)
     * clock: 현재 시각을 제공하는 함수. 실제 실행은 System.nanoTime, 테스트는 가짜 시계 사용.
     * reportPath: 보고서를 저장할 파일 경로.
     * active: 현재 프레임의 실행 시간을 측정 중인지 여부.
     * hasPrevious: 호출 간격을 계산할 이전 프레임 기록이 있는지 여부. 일시정지/재개 시 초기화.
     * hasOrigin: 최초 측정 시작 시점(origin)이 설정되었는지 여부.
     * savePending: 측정 도중 들어온 파일 저장 요청을 종료 후 처리하기 위한 표시.
     * start: 현재 프레임의 측정 시작 시점.
     * previousStart: 직전 프레임의 측정 시작 시점. start와의 차이가 호출 간격.
     * origin: 최초 측정 시작 시점. 최대 간격의 발생 시점을 계산할 때 사용.
     * previousExecution: 직전 프레임의 실행 시간. 다음 호출 간격과 연결해 사용.
     * count: 집계한 호출 간격 수. 첫 호출과 재개 후 첫 호출은 제외.
     * withinBudget: 호출 간격이 16.7ms 이하인 횟수. 초과 횟수는 count - withinBudget.
     * over100ms: 호출 간격이 100ms를 초과한 횟수.
     * maxInterval: 지금까지 측정한 가장 긴 호출 간격.
     * maxExecution: 최대 호출 간격 직전 프레임의 실행 시간.
     * maxAt: 최대 간격을 확인한 시점에서 origin을 뺀 경과 시간. 일시정지 시간도 포함.
     */

    public FrameProfiler() {
        this(System::nanoTime, Path.of("performance-reports", "frame-profile-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))
                + "-" + java.util.UUID.randomUUID() + ".txt"));
    }

    FrameProfiler(LongSupplier clock, Path reportPath) {
        this.clock = clock;
        this.reportPath = reportPath;
    }

    public void measure(Runnable update) {
        beginFrame();
        try {
            update.run();
        } finally {
            endFrame();
        }
    }

    private void beginFrame() {
        start = clock.getAsLong();
        if (!hasOrigin) {
            origin = start;
            hasOrigin = true;
        }
        if (hasPrevious) {
            long interval = start - previousStart;
            count++;
            if (interval <= 16_700_000L) withinBudget++;
            if (interval > 100_000_000L) over100ms++;
            if (interval > maxInterval) {
                maxInterval = interval;
                maxExecution = previousExecution;
                maxAt = start - origin;
            }
        }
        active = true;
    }

    private void endFrame() {
        previousExecution = clock.getAsLong() - start;
        previousStart = start;
        hasPrevious = true;
        active = false;
        if (savePending) saveReport();
    }

    public void resetContinuity() {
        hasPrevious = false;
    }

    /** 핸들러 안의 저장 요청은 endFrame 이후 처리해 파일 I/O를 측정에서 제외한다. */
    public void saveReport() {
        if (active) {
            savePending = true;
            return;
        }
        savePending = false;
        try {
            Files.createDirectories(reportPath.toAbsolutePath().getParent());
            Files.writeString(reportPath, summary(), StandardCharsets.UTF_8);
            System.out.println("Frame profile: " + reportPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("프레임 보고서 저장 실패: " + e.getMessage());
        }
    }

    String summary() {
        String header = String.format(Locale.ROOT,
                "측정 호출 간격 수: %d%n<= 16.7ms: %.3f%%%n> 16.7ms: %.3f%%%n> 100ms: %.3f%% (전체 기준)%n",
                count, percent(withinBudget), percent(count - withinBudget), percent(over100ms));
        if (count == 0) return header + "최대 간격: 측정 자료 없음\n";
        return header + String.format(Locale.ROOT,
                "최대 호출 간격: %.3fms%n발생 시점: 첫 측정 시작 후 %.3f초 (일시정지 포함)%n"
                + "직전 핸들러 실행 시간: %.3fms%n"
                + "첫 호출 및 재개 후 첫 호출은 간격 집계 제외.%n"
                + "실행 시간은 동기 작업 기준이며 실제 화면 표시 시간이 아님.%n"
                + "환경: OS=%s %s, Java=%s, 논리 프로세서=%d%n",
                maxInterval / 1e6, maxAt / 1e9, maxExecution / 1e6,
                System.getProperty("os.name"), System.getProperty("os.arch"),
                System.getProperty("java.version"), Runtime.getRuntime().availableProcessors());
    }

    private double percent(long value) {
        return count == 0 ? 0 : value * 100.0 / count;
    }
}
