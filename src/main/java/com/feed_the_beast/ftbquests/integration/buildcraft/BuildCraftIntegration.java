package com.feed_the_beast.ftbquests.integration.buildcraft;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class BuildCraftIntegration {
	public static TaskType MJ_TASK;

	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(BuildCraftIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event) {
		event.getRegistry().register(MJ_TASK = new TaskType(MJTask::new).setRegistryName("buildcraft_mj").setIcon(Icon.getIcon(MJTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(MJTask.FULL_TEXTURE.toString()))));
	}
}