package tech.funkyra.ftb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class Database {
	private static final ConnectionString connectionStr =
		new ConnectionString("mongodb://admin:5HMV77zvNx5evGCif7sZCDz78XB@95.217.56.50:27017/?tls=false");

	private static final MongoClient mongo = MongoClients.create(connectionStr);
	public static final MongoDatabase ftbDb = mongo.getDatabase("ftb");

	public static final ReplaceOptions queryOption = new ReplaceOptions().upsert(true);

	public static void closeConnection() {
		mongo.close();
	}
}
