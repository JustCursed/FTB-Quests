package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageOpenQuestBook extends MessageToClient {

    @Override
    public NetworkWrapper getWrapper() {
        return FTBQuestsNetHandler.GENERAL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onMessage() {
        ClientQuestFile.INSTANCE.openQuestGui(Minecraft.getMinecraft().player);
    }
}
