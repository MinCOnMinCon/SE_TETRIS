package tetris.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.Random;

import org.junit.jupiter.api.Test;
import tetris.block.blocks.BlockGenerator;
import tetris.settings.GameSettings.Difficulty;

// 지정한 시행 횟수, 난이도, 시드로 블록 출현 분포를 확인한다.
public class BlockGenerationTest {
    @Test
    void rejectsNonPositiveTrialCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> Test(0, Difficulty.EASY, 252L));
        assertThrows(IllegalArgumentException.class,
                () -> Test(-1, Difficulty.EASY, 252L));
    }

    // Java 실행 진입점은 규칙에 따라 소문자 main을 사용한다.
    public static void main(String[] args) {
        Test(100000, Difficulty.EASY, 252L);
    }

    public static void Test(int trials, Difficulty difficulty, long seed) {
        if (trials <= 0) {
            throw new IllegalArgumentException("시행 횟수는 양수여야 합니다.");
        }

        BlockGenerator generator = new BlockGenerator(difficulty, new Random(seed), 0);
        double[] targetProbabilities = generator.GetTargetProbabilities();
        int[] counts = new int[targetProbabilities.length];

        for (int i = 0; i < trials; i++) {
            int index = generator.GenerateBlockIndex();
            counts[index]++;
        }

        System.out.printf("난이도: %s, 시드: %d, 시행 횟수: %d%n",
                difficulty, seed, generator.GetGeneratedCount());
        System.out.println("인덱스 | 출현 횟수 | 실제 비율 | 목표 비율 | 차이(%p)");

        for (int i = 0; i < counts.length; i++) {
            double actualPercent = counts[i] * 100.0 / trials;
            double targetPercent = targetProbabilities[i] * 100.0;
            System.out.printf(Locale.ROOT, "%d | %d | %.4f%% | %.4f%% | %+.4f%n",
                    i, counts[i], actualPercent, targetPercent,
                    actualPercent - targetPercent);
        }
    }
}
