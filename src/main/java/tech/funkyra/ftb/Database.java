package tech.funkyra.ftb;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class Database {
	private static final MongoClientOptions.Builder options = MongoClientOptions.builder()
		.connectionsPerHost(32)
		.maxConnectionIdleTime(60000)
		.maxConnectionLifeTime(120000)
		.readConcern(ReadConcern.AVAILABLE)
		.writeConcern(WriteConcern.ACKNOWLEDGED);

	private static final MongoClientURI settings = new MongoClientURI("mongodb://user:passwd@127.0.0.1:1224/", options);
	private static final MongoClient mongo = new MongoClient(settings);
	public static final MongoDatabase ftbDb = mongo.getDatabase("ftb");

	public static final ReplaceOptions queryOption = new ReplaceOptions().upsert(true);

	public static void closeConnection() {
		mongo.close();
	}
}
