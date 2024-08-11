package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public interface ITaskScreen extends IScreen {
	@Nullable
	TileTaskScreenCore getScreen();

	@Override
	default void paint(IBlockState paint, EnumFacing facing, boolean all) {
		TileTaskScreenCore screen = getScreen();

		if (screen != null) {
			screen.skin = paint;
			boolean xaxis = screen.getBlockState().getValue(BlockHorizontal.FACING).getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < screen.size * 2 + 1; y++) {
				for (int x = -screen.size; x <= screen.size; x++) {
					int offX = xaxis ? 0 : x;
					int offZ = xaxis ? x : 0;
					BlockPos pos1 = new BlockPos(screen.getPos().getX() + offX, screen.getPos().getY() + y, screen.getPos().getZ() + offZ);
					IBlockState state1 = screen.getWorld().getBlockState(pos1);
					screen.getWorld().notifyBlockUpdate(pos1, state1, state1, 11);
				}
			}
		}
	}

	@Override
	default IBlockState getPaint() {
		TileTaskScreenCore core = getScreen();
		return core == null ? BlockUtils.AIR_STATE : core.skin;
	}
}