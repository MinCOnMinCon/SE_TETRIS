package tetris.gamescene.scenerender;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import tetris.block.blocks.Block4VLines;
import tetris.block.blocks.BlockO;
import tetris.block.blocks.Block_dot;
import tetris.block.data.BlockData;
import tetris.block.data.CurrentBlock.ItemType;

class ItemRendererTest {
    private final ItemRenderer renderer = new ItemRenderer();

    @ParameterizedTest
    @EnumSource(value = ItemType.class, names = {"DEL_HLINE", "DEL_VLINE"})
    void lineItemsMatchOnlyTheirItemPosition(ItemType type) {
        BlockData block = new BlockO(0).GetBlockData();
        block.SetItem(true);
        block.SetItemType(type);
        // x와 y를 다르게 두어 좌표를 뒤바꿔 비교하는 오류도 확인한다.
        block.SetItemPos(new int[]{1, 0});

        assertTrue(renderer.IsItemCell(block, 1, 0), "아이템 좌표");
        assertFalse(renderer.IsItemCell(block, 0, 1), "아이템이 아닌 블록 칸");
    }

    @ParameterizedTest
    @EnumSource(value = ItemType.class, names = {"DEL_VEMPTY", "DEL_AEMPTY"})
    void dotItemsMatchOnlyTheOrigin(ItemType type) {
        BlockData block = new Block_dot(0).GetBlockData();
        block.SetItem(true);
        block.SetItemType(type);
        block.SetItemPos(new int[]{0, 0});

        assertTrue(renderer.IsItemCell(block, 0, 0), "dot 아이템 좌표");
        // dot은 한 칸이므로 다른 좌표는 블록 영역 밖이다.
        assertFalse(renderer.IsItemCell(block, 1, 0), "아이템이 아닌 좌표");
        assertFalse(renderer.IsItemCell(block, 0, 1), "아이템이 아닌 좌표");
    }

    @Test
    void fourVerticalLineItemHasNoSymbolCell() {
        BlockData block = new Block4VLines(0).GetBlockData();
        block.SetItem(true);
        block.SetItemType(ItemType.DEL_4VLINE);

        assertNull(block.GetItemPos());
        assertFalse(renderer.IsItemCell(block, 1, 0), "블록이 있는 좌표라도 아이템 기호 칸은 아니다");
    }

    @Test
    void normalBlockHasNoItemCell() {
        BlockData block = new BlockO(0).GetBlockData();

        assertFalse(renderer.IsItemCell(block, 0, 0));
    }

    @Test
    void disabledItemDoesNotMatchEvenWhenPositionMatches() {
        BlockData block = new BlockO(0).GetBlockData();
        block.SetItemType(ItemType.DEL_HLINE);
        block.SetItemPos(new int[]{1, 0});
        block.SetItem(false);

        assertFalse(renderer.IsItemCell(block, 1, 0));
    }
}
