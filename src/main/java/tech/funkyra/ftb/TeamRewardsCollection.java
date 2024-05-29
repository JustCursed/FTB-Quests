package tech.funkyra.ftb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import static tech.funkyra.ftb.Database.ftbDb;

// collection for claimed team rewards
public class TeamRewardsCollection {
    public static final MongoCollection<Document> teamRewardsCollection = ftbDb.getCollection("claimedTeamRewards");

    public TeamRewardsCollection() {

    }

    public boolean delete(int id) {


        return true;
    }

    public boolean deleteAll(String nick) {


        return true;
    }

    public boolean add(int id) {


        return true;
    }

    public boolean contains(int id) {


        return true;
    }
}
