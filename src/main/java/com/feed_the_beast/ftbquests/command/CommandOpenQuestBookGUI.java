package com.feed_the_beast.ftbquests.command;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandOpenQuestBookGUI extends CommandFTBQuestsBase {
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            ClientQuestFile.INSTANCE.openQuestGui((EntityPlayerMP) sender);
        }
    }
}