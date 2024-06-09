package tech.funkyra.ftb.collections;

import com.mongodb.client.MongoCollection;
import net.minecraft.nbt.NBTTagCompound;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static tech.funkyra.ftb.DBUtil.fromDocument;
import static tech.funkyra.ftb.DBUtil.toDocument;
import static tech.funkyra.ftb.Database.ftbDb;
import static tech.funkyra.ftb.Database.queryOption;

public class QuestsCollection {
	private static final MongoCollection<Document> teamRewardsCollection = ftbDb.getCollection("quests");

	public static NBTTagCompound getData(short uid) {
		try {
			Document data = teamRewardsCollection.find(eq("uid", uid)).first();
			assert data != null;

			return fromDocument((Document) data.get("data"));
		} catch (Exception ignored) {
			return new NBTTagCompound();
		}
	}

	public static boolean setData(short uid, NBTTagCompound data) {
		try {
			Document doc = new Document("uid", uid).append("data", toDocument(data));
			return teamRewardsCollection.replaceOne(eq("uid", uid), doc, queryOption).wasAcknowledged();
		} catch (Exception err) {
			err.printStackTrace();
			return false;
		}
	}
}
