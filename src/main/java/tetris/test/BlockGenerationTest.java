package tetris.test;

import java.util.Locale;
import java.util.Random;

import tetris.block.blocks.Block;
import tetris.block.blocks.BlockGenerator;
import tetris.block.blocks.BlockI;
import tetris.block.blocks.BlockJ;
import tetris.block.blocks.BlockL;
import tetris.block.blocks.BlockO;
import tetris.block.blocks.BlockS;
import tetris.block.blocks.BlockT;
import tetris.block.blocks.BlockZ;
import tetris.settings.GameSettings.Difficulty;

// 지정한 시행 횟수, 난이도, 시드로 블록 출현 분포를 확인한다.
public class BlockGenerationTest {
    // Java 실행 진입점은 규칙에 따라 소문자 main을 사용한다.
    public static void main(String[] args) {
        Test(100, Difficulty.HARD, 252L);
    }

    public static void Test(int trials, Difficulty difficulty, long seed) {
        if (trials <= 0) {
            throw new IllegalArgumentException("시행 횟수는 양수여야 합니다.");
        }

        BlockGenerator generator = new BlockGenerator(difficulty, new Random(seed), 0);
        double[] targetProbabilities = generator.GetTargetProbabilities();
        int[] counts = new int[targetProbabilities.length];

        for (int i = 0; i < trials; i++) {
            Block block = generator.GetBlock();
            int index = FindBlockIndex(block);
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

    private static int FindBlockIndex(Block block) {
        // 제너레이터의 확률 배열과 같은 순서: I, J, L, O, S, T, Z
        if (block instanceof BlockI) return 0;
        if (block instanceof BlockJ) return 1;
        if (block instanceof BlockL) return 2;
        if (block instanceof BlockO) return 3;
        if (block instanceof BlockS) return 4;
        if (block instanceof BlockT) return 5;
        if (block instanceof BlockZ) return 6;
        throw new IllegalArgumentException("알 수 없는 블록 클래스입니다.");
    }
}
