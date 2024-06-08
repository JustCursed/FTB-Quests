package tech.funkyra.ftb.collections;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;
import net.minecraft.nbt.NBTTagCompound;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;
import static tech.funkyra.ftb.Database.queryOption;
import static tech.funkyra.ftb.Database.ftbDb;

public class QuestsCollection {
	private static final MongoCollection<Document> teamRewardsCollection = ftbDb.getCollection("quests");

	public static NBTTagCompound getTasks(short uid) {
		// осторожно! дальше говнокод!
		String tasks;

		NBTTagCompound nbt = new NBTTagCompound();

		try {
			tasks = (String) teamRewardsCollection.find(eq("uid", uid))
				.first().get("tasks");
		} catch (Exception ignored) {
			return nbt;
		}

		if (tasks == null)
			return nbt;

		for (String task : tasks.split(",")) {
			String[] kv = task.split(":"); // key|value (task id|progress)

			if (kv[1].endsWith("b"))
				nbt.setByte(kv[0], Byte.parseByte(kv[1].replace("b", "")));
			else if (kv[1].endsWith("s"))
				nbt.setShort(kv[0], Short.parseShort(kv[1].replace("s", "")));
			else if (kv[1].endsWith("l"))
				nbt.setLong(kv[0], Long.parseLong(kv[1].replace("l", "")));
			else
				nbt.setInteger(kv[0], Integer.parseInt(kv[1]));
		}

		return nbt;
	}

	public static boolean setTasks(short uid, NBTTagCompound tasks) {
		String strTasks = tasks.toString();
		String data = strTasks.substring(1, strTasks.length() - 1);
//		Document data = new Document("uid", uid)
//			.append("tasks", strTasks.substring(1, strTasks.length() - 1));
		return teamRewardsCollection
			.replaceOne(eq("uid", uid), new Document("tasks", data), queryOption).wasAcknowledged();
	}

	public static BasicDBList getClaimedTeamRewards(short uid) {
		try {
			return (BasicDBList) teamRewardsCollection.find(eq("uid", uid)).first()
				.get("claimedTeamRewards");
		} catch (Exception ignored) {
			return new BasicDBList();
		}
	}

	public static boolean setClaimedTeamRewards(short uid, BasicDBList data) {
		return teamRewardsCollection
			.replaceOne(eq("uid", uid), new Document("tasks", data), queryOption).wasAcknowledged();
	}

//	public static ArrayList<>
}
