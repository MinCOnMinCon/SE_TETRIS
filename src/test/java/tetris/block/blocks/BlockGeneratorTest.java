package tetris.block.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock.ItemType;
import tetris.settings.GameSettings.Difficulty;

class BlockGeneratorTest {
    // 아이템 좌표는 [x, y], 모양 배열은 [행][열]이다.
    @ParameterizedTest
    @EnumSource(value = ItemType.class, names = {"DEL_HLINE", "DEL_VLINE"})
    void lineItemPositionIsOnOccupiedCell(ItemType type) {
        BlockData block = generatorForItem(type).GetItemBlock();
        boolean[][] shape = block.GetShape();
        int[] position = block.GetItemPos();

        assertEquals(2, position.length);
        int x = position[0];
        int y = position[1];
        assertTrue(y >= 0 && y < shape.length, "아이템 행이 모양 범위 안이어야 한다");
        assertTrue(x >= 0 && x < shape[y].length, "아이템 열이 모양 범위 안이어야 한다");
        assertTrue(shape[y][x], "아이템은 실제 블록이 있는 칸에 있어야 한다");
    }

    @ParameterizedTest
    @EnumSource(value = ItemType.class, names = {"DEL_VEMPTY", "DEL_AEMPTY"})
    void dotItemHasDotShapeAndPositionAtOrigin(ItemType type) {
        BlockData block = generatorForItem(type).GetItemBlock();

        assertArrayEquals(new boolean[][]{{true}}, block.GetShape(), "한 칸짜리 dot 모양");
        assertArrayEquals(new int[]{0, 0}, block.GetItemPos(), "dot 아이템 위치");
    }

    @Test
    void fourVerticalLineItemHasDedicatedShape() {
        BlockData block = generatorForItem(ItemType.DEL_4VLINE).GetItemBlock();
        boolean[][] expectedShape = {
            {false, true, true, false},
            {true, true, true, true}
        };

        assertArrayEquals(expectedShape, block.GetShape(), "4VLINE 전용 모양");
    }

    @ParameterizedTest
    @EnumSource(value = ItemType.class, names = "NORMAL", mode = EnumSource.Mode.EXCLUDE)
    void itemMetadataMatchesSelectedType(ItemType type) {
        BlockData block = generatorForItem(type).GetItemBlock();

        assertTrue(block.IsItem(), "아이템 블록으로 표시되어야 한다");
        assertEquals(type, block.GetItemType(), "선택한 아이템 종류가 유지되어야 한다");
    }

    // 첫 nextInt 호출(아이템 종류 선택)만 통제하고 나머지는 고정 시드로 생성한다.
    private static BlockGenerator generatorForItem(ItemType type) {
        ItemType[] candidates = Arrays.stream(ItemType.values())
                .filter(candidate -> candidate != ItemType.NORMAL)
                .toArray(ItemType[]::new);
        int index = Arrays.asList(candidates).indexOf(type);
        if (index < 0) {
            throw new IllegalArgumentException("아이템 종류가 필요합니다");
        }

        Random random = new Random(42) {
            private boolean first = true;

            @Override
            public int nextInt(int bound) {
                if (first) {
                    first = false;
                    return index;
                }
                return super.nextInt(bound);
            }
        };
        return new BlockGenerator(Difficulty.NORMAL, random, 0);
    }

    @Test
    void seededGeneratorReturnsValidNonRepeatingConsecutiveBlocks() {
        BlockGenerator generator = new BlockGenerator(Difficulty.NORMAL, new Random(42), 0);
        Class<? extends Block> previous = null;

        for (int i = 0; i < 100; i++) {
            Block block = generator.GetBlock();
            assertTrue(block instanceof BlockI || block instanceof BlockJ
                    || block instanceof BlockL || block instanceof BlockO
                    || block instanceof BlockS || block instanceof BlockT || block instanceof BlockZ);
            Class<? extends Block> generated = block.getClass();
            if (previous != null) {
                assertNotEquals(previous, generated);
            }
            previous = generated;
        }

        assertEquals(100, generator.GetGeneratedCount());
    }
}
