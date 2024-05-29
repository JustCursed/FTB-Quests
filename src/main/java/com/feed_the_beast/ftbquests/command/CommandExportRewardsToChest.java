package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.loot.RewardTable;
import com.feed_the_beast.ftbquests.quest.loot.WeightedReward;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CommandExportRewardsToChest extends CommandFTBQuestsBase {
    @Override
    public String getName() {
        return "export_rewards_to_chest";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(ServerQuestFile.INSTANCE.rewardTables.size());

            for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables) {
                if (table.lootCrate != null) {
                    list.add(table.lootCrate.stringID);
                }
            }

            for (RewardTable table : ServerQuestFile.INSTANCE.rewardTables) {
                if (table.lootCrate == null) {
                    list.add(QuestObjectBase.getCodeString(table));
                }
            }

            return getListOfStringsMatchingLastWord(args, list);
        }

        return super.getTabCompletions(server, sender, args, pos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        RewardTable table = ServerQuestFile.INSTANCE.getRewardTable(args[0]);

        if (table == null) {
            throw FTBLib.error(sender, "commands.ftbquests.import_rewards_from_chest.invalid_id", args[0]);
        }

        RayTraceResult ray = MathUtils.rayTrace(player, false);

        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            TileEntity tileEntity = player.world.getTileEntity(ray.getBlockPos());

            if (tileEntity != null) {
                IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ray.sideHit);

                if (handler != null) {
                    int r = 0;

                    for (WeightedReward reward : table.rewards) {
                        Object object = reward.reward.getIngredient();

                        if (object instanceof ItemStack && !((ItemStack) object).isEmpty()) {
                            ItemStack stack1 = ((ItemStack) object).copy();
                            stack1.setCount(1);

                            if (ItemHandlerHelper.insertItem(handler, stack1, false).isEmpty()) {
                                r++;
                            }
                        }
                    }

                    sender.sendMessage(new TextComponentTranslation("commands.ftbquests.export_rewards_to_chest.text", Integer.toString(r), Integer.toString(table.rewards.size()), table.getTitle()));
                }
            }
        }
    }
}