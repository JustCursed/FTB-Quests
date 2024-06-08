package tech.funkyra.ftb.collections;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import static tech.funkyra.ftb.Database.ftbDb;

public class TeamsCollection {
	public static final MongoCollection<Document> teamRewardsCollection = ftbDb.getCollection("teams");
}
