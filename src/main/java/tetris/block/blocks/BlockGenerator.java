package tetris.block.blocks;

import java.util.Arrays;
import java.util.Random;

import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock.ItemType;
import tetris.block.data.CurrentShape;
import tetris.settings.GameSettings.Difficulty;

/*
 * 블록 객체 생성과 화면 처리 없이 다음 블록의 인덱스만 추첨한다.
 *
 * W_i: 블록 i의 가중치
 * P_i = W_i / sum(W_j): 블록 i의 목표 확률
 * E_i: 목표 누적 출현량 (expectedCounts), 초기값 0
 * C_i: 실제 출현 횟수 (actualCounts), 초기값 0
 *
 * 매 추첨 전에 E_i += P_i로 목표량을 누적한다. (E_i에는 결과적으로 현재까지의 시행 횟수만큼 뽑았을 때, i번째 블럭이 나오는 기댓값을 나타낸다.)
 * 확률이 고정이면 N회 누적한 E_i는 N * P_i와 같다.
 * 확률이 바뀌어도 기존 E_i는 유지하고 이후 확률만 더한다.
 *
 * D_i = E_i - C_i: 부족량 (양수이면 목표보다 적게 출현) (C_i는 실제로 i번째 블럭이 나온 횟수이다.)
 * R_i: 직전 블록이면 0, 두 번째 이전 블록이면 0.2, 나머지는 1
 *      두 조건이 겹치면 직전 블록 규칙을 우선한다.
 * Score_i = exp(k * D_i) * R_i, k = 0.5
 * 단, W_i가 0이면 Score_i도 0으로 처리한다.
 * Q_i = Score_i / sum(Score_j): 룰렛 추첨의 최종 확률
 *
 * 실제 계산에서는 후보의 최대 지수 M = max(k * D_i)를 빼서
 * Score_i = exp(k * D_i - M) * R_i로 오버플로를 방지한다.
 * 모든 점수에 같은 배율을 적용하므로 최종 확률 Q_i는 유지된다.
 *
 * 추첨 후 선택된 C_i와 generatedCount를 1 증가시키고 최근 기록을 갱신한다.
 * 직전 중복 금지일 때는 R_i를 0으로, 두번째 아전 블록에 대해서는 R_i를 0.2로 설정해 중복되는 블럭이 나올 가능성을 줄인다.
 */
public class BlockGenerator {
    private static final int BLOCK_COUNT = 7;
    private static final double K = 0.5;
    private static final double REPEAT_FACTOR = 0.2;
    private final int colorMode;

    // 기존 BlockCreate의 순서: I, J, L, O, S, T, Z
    private final double[] weights;
    private final double[] expectedCounts = new double[BLOCK_COUNT]; 
    private final long[] actualCounts = new long[BLOCK_COUNT];
    private final Random random;

    private long generatedCount; // 게임 시작하고 블럭 생성한 수
    private int previous = -1;
    private int secondPrevious = -1;

    public BlockGenerator(Difficulty difficulty, int colorMode) {
        this(difficulty, new Random(), colorMode);
    }

    // 테스트에서는 시드를 지정한 Random을 주입한다.
    public BlockGenerator(Difficulty difficulty, Random random, int colorMode) {
        this.random = random;
        this.colorMode = colorMode;
        switch (difficulty) { 
            case EASY:
                weights = new double[]{1.2, 1, 1, 1, 1, 1, 1};
                break;
            case NORMAL:
                weights = new double[]{1.0, 1, 1, 1, 1, 1, 1};
                break;
            case HARD:
                weights = new double[]{0.8, 1, 1, 1, 1, 1, 1};
                break;
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }

    private int GenerateBlockIndex() {
        double totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        // 이번 추첨의 목표 확률을 먼저 누적한다.
        for (int i = 0; i < BLOCK_COUNT; i++) {
            expectedCounts[i] += weights[i] / totalWeight;
        }

        // 후보 중 가장 큰 지수를 빼서 exp 계산의 오버플로를 막는다.
        double maxExponent = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            if (weights[i] == 0 || i == previous) {
                continue;
            }

            double deficit = expectedCounts[i] - actualCounts[i];
            maxExponent = Math.max(maxExponent, K * deficit);
        }

        double[] scores = new double[BLOCK_COUNT];
        double totalScore = 0;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            if (weights[i] == 0 || i == previous) {
                continue;
            }

            double deficit = expectedCounts[i] - actualCounts[i];
            double repeat = (i == secondPrevious) ? REPEAT_FACTOR : 1.0;
            scores[i] = Math.exp(K * deficit - maxExponent) * repeat;
            totalScore += scores[i];
        }

        if (!(totalScore > 0) || !Double.isFinite(totalScore)) {
            // 추첨 실패 시 이번 목표량 증가도 되돌린다.
            for (int i = 0; i < BLOCK_COUNT; i++) {
                expectedCounts[i] -= weights[i] / totalWeight;
            }
            throw new IllegalStateException("No valid block scores");
        }

        double target = random.nextDouble() * totalScore;
        double cumulative = 0;
        int selected = -1;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            if (scores[i] <= 0) {
                continue;
            }

            // 합산 오차로 구간을 벗어나면 마지막 양수 점수 후보를 사용한다.
            selected = i;
            cumulative += scores[i];
            if (target < cumulative) {
                break;
            }
        }

        actualCounts[selected]++;
        generatedCount++;
        secondPrevious = previous;
        previous = selected;
        return selected;
    }
    public Block GetBlock() {
        switch (GenerateBlockIndex()) {
            case 0:     return new BlockI(colorMode);
            case 1:     return new BlockJ(colorMode);
            case 2:     return new BlockL(colorMode);
            case 3:     return new BlockO(colorMode);
            case 4:     return new BlockS(colorMode);
            case 5:     return new BlockT(colorMode);
            case 6:     return new BlockZ(colorMode);            
            default:    throw new IllegalArgumentException("Invalid block index");
        }
    }

    public BlockData GetItemBlock() {
        // NORMAL을 제외한 모든 아이템을 같은 확률로 추첨한다.
        ItemType[] candidates = Arrays.stream(ItemType.values())
                .filter(type -> type != ItemType.NORMAL)
                .toArray(ItemType[]::new);
        ItemType itemType = candidates[random.nextInt(candidates.length)];

        switch (itemType) {
            case DEL_HLINE:
            case DEL_VLINE:
                BlockData blockData = GetBlock().GetBlockData();
                int[][] positions = CurrentShape.currentShapeIndex(blockData.GetShape());
                int[] position = positions[random.nextInt(positions.length)]; // 테트로미노 중, 블럭이 존재하는 위치 중 동일 가중치로 아이템이 있을 위치 하나를 정함.

                blockData.SetItem(true);
                blockData.SetItemType(itemType);
                // CurrentShape의 [행, 열]을 아이템 좌표 [x, y]로 변환한다.
                blockData.SetItemPos(new int[]{position[1], position[0]});
                return blockData;
            case DEL_4VLINE:
                BlockData weightBlock = new Block4VLines(colorMode).GetBlockData();
                weightBlock.SetItem(true);
                weightBlock.SetItemType(itemType);
                return weightBlock;
            case DEL_VEMPTY:
            case DEL_AEMPTY:
                BlockData dotBlockData = new Block_dot(colorMode).GetBlockData();
                dotBlockData.SetItem(true);
                dotBlockData.SetItemType(itemType);
                dotBlockData.SetItemPos(new int[]{0, 0});
                return dotBlockData;
            default:
                throw new IllegalArgumentException("Invalid item type: " + itemType);
        }
    }

    public long GetGeneratedCount() {
        return generatedCount;
    }

    // 내부 가중치를 노출하지 않고 현재 목표 확률의 새 배열을 반환한다.
    public double[] GetTargetProbabilities() {
        double totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        double[] probabilities = new double[BLOCK_COUNT];
        for (int i = 0; i < BLOCK_COUNT; i++) {
            probabilities[i] = weights[i] / totalWeight;
        }
        return probabilities;
    }
}
