package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.net.MessageOpenQuestBook;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandOpenQuestBookGUI extends CommandFTBQuestsBase {
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            new MessageOpenQuestBook().sendTo((EntityPlayerMP) sender);
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}