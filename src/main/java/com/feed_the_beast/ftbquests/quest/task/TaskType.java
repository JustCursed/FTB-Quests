package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfig;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public final class TaskType extends IForgeRegistryEntry.Impl<TaskType> {
	private static ForgeRegistry<TaskType> REGISTRY;

	public static void createRegistry() {
		if (REGISTRY == null) {
			ResourceLocation registryName = new ResourceLocation(FTBQuests.MOD_ID, "tasks");
			REGISTRY = (ForgeRegistry<TaskType>) new RegistryBuilder<TaskType>().setType(TaskType.class).setName(registryName).create();
			MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<>(registryName, REGISTRY));
		}
	}

	public static ForgeRegistry<TaskType> getRegistry() {
		return REGISTRY;
	}

	@Nullable
	public static Task createTask(Quest quest, String id) {
		if (id.isEmpty()) {
			id = FTBQuests.MOD_ID + ":item";
		} else if (id.indexOf(':') == -1) {
			id = FTBQuests.MOD_ID + ':' + id;
		}

		TaskType type = REGISTRY.getValue(new ResourceLocation(id));

		if (type == null) {
			if (id.equals("ftbquests:ftb_money")) {
				return createTask(quest, "ftbmoney:money");
			}

			return null;
		}

		return type.provider.create(quest);
	}

	@FunctionalInterface
	public interface Provider {
		Task create(Quest quest);
	}

	public interface GuiProvider {
		@SideOnly(Side.CLIENT)
		void openCreationGui(IOpenableGui gui, Quest quest, Consumer<Task> callback);
	}

	public final Provider provider;
	private String displayName;
	private Icon icon;
	private GuiProvider guiProvider;

	public TaskType(Provider p) {
		provider = p;
		displayName = null;
		icon = GuiIcons.ACCEPT;
		guiProvider = new GuiProvider() {
			@Override
			@SideOnly(Side.CLIENT)
			public void openCreationGui(IOpenableGui gui, Quest quest, Consumer<Task> callback) {
				Task task = provider.create(quest);

				if (task instanceof ISingleLongValueTask) {
					new GuiEditConfigValue("value", ((ISingleLongValueTask) task).getDefaultValue(), (value, set) -> {
						gui.openGui();
						if (set) {
							((ISingleLongValueTask) task).setValue(value.getLong());
							callback.accept(task);
						}
					}).openGui();
					return;
				}

				ConfigGroup group = ConfigGroup.newGroup(FTBQuests.MOD_ID);
				task.getConfig(task.createSubGroup(group));
				new GuiEditConfig(group, (g1, sender) -> callback.accept(task)).openGui();
			}
		};
	}

	public String getTypeForNBT() {
		return getRegistryName().getNamespace().equals(FTBQuests.MOD_ID) ? getRegistryName().getPath() : getRegistryName().toString();
	}

	public TaskType setDisplayName(String name) {
		displayName = name;
		return this;
	}

	public String getDisplayName() {
		if (displayName != null) {
			return displayName;
		}

		ResourceLocation id = getRegistryName();
		return id == null ? "error" : I18n.format("ftbquests.task." + id.getNamespace() + '.' + id.getPath());
	}

	public TaskType setIcon(Icon i) {
		icon = i;
		return this;
	}

	public Icon getIcon() {
		return icon;
	}

	public TaskType setGuiProvider(GuiProvider p) {
		guiProvider = p;
		return this;
	}

	public GuiProvider getGuiProvider() {
		return guiProvider;
	}
}