package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
public class BotaniaIntegration {
    public static TaskType MANA_TASK;

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(BotaniaIntegration.class);
        GameRegistry.registerTileEntity(TileTaskScreenCoreBotania.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core_botania"));
        GameRegistry.registerTileEntity(TileTaskScreenPartBotania.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part_botania"));
    }

    @SubscribeEvent
    public static void registerTasks(RegistryEvent.Register<TaskType> event) {
        event.getRegistry().register(MANA_TASK = new TaskType(ManaTask::new).setRegistryName("botania_mana").setIcon(Icon.getIcon(ManaTask.FULL_TEXTURE.toString()).combineWith(Icon.getIcon(ManaTask.EMPTY_TEXTURE.toString()))));
    }
}