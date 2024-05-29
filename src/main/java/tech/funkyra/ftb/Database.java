package tech.funkyra.ftb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Database {
    private static final ConnectionString MongoUri = new ConnectionString("mongodb://user:passwd@127.0.0.1:1224/");
    public static final MongoClient mongo = MongoClients.create(MongoUri);
    public static final MongoDatabase ftbDb = mongo.getDatabase("ftb");
}
