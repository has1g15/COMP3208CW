import java.util.HashMap;
import java.util.List;
import java.util.Map;
        import com.almworks.sqlite4java.SQLiteException;
        import com.almworks.sqlite4java.SQLiteStatement;
        import com.google.common.collect.Multimap;
/**
 * Created by Hannah on 30/10/2017.
 */
public class UserBasedCollabFiltering {

    private DataHandler dataHandler;
    private HashMap<Integer, HashMap<Integer, Float>> data, itemData;
    private Multimap<Integer, Integer> testData;
    private HashMap<Integer, Float> averages = new HashMap<>();
    private HashMap<Integer, Float> itemAverages = new HashMap<>();

    public UserBasedCollabFiltering()
    {
        dataHandler = new DataHandler();
        loadData();
        createSimilarityMatrix();
        makePredictions();
        dataHandler.finish();
    }

    public void loadData()
    {
        data = dataHandler.loadUserData();
        itemData = dataHandler.loadItemData();
        testData = dataHandler.loadTestData();
        calculateAverages();
        calculateItemAverages();
    }

    public void createSimilarityMatrix()
    {
        dataHandler.createSimilarityMatrix();
        for (int i = 1; i < data.size(); i++)
        {
            for (int j = i+1; j <= data.size(); j++)
            {
                    dataHandler.updateSimilarityMatrix(i, j, (float)calculateSimilarity(i,j));
                    System.out.println(i + "" + j + "" + calculateSimilarity(i,j));
            }
        }
    }

    /*public boolean checkUserRating(int user, int item)
    {
        if (data.get(user).equals(null) || data.get(user).get(item).equals(null))
            return false;
        else return true;
    }*/

    public double calculateSimilarity(int user1, int user2)
    {
        float similarityNumerator = 0;
        float item1Squared = 0;
        float item2Squared = 0;
        float user1ItemRatingCalc = 0;
        float user2ItemRatingCalc = 0;

            for (Integer item : itemData.keySet())
            {
                    user1ItemRatingCalc = findUserItemRating(user1, item) - averages.get(user1);
                    user2ItemRatingCalc = findUserItemRating(user2, item) - averages.get(user2);
                    similarityNumerator += (user1ItemRatingCalc) * (user2ItemRatingCalc);
                    item1Squared += user1ItemRatingCalc * user1ItemRatingCalc;
                    item2Squared += user2ItemRatingCalc * user2ItemRatingCalc;
            }
            return similarityNumerator/((Math.sqrt(item1Squared))*(Math.sqrt(item2Squared)));
    }

    public void makePredictions()
    {
        int item;
        dataHandler.createPredictionsTable();
        float prediction;
        for (Map.Entry<Integer,Integer> entry: testData.entries())
        {
            item = entry.getValue();
            prediction = calculatePrediction(entry.getKey(), item);
            if (prediction > 10.49)
            {
                prediction = 10;
            }
            if (prediction < 0.5)
            {
                prediction = 1;
            }
            System.out.println("Prediction: " + prediction);
            dataHandler.updatePredictionsTable(entry.getKey(), item, Math.round(prediction));
        }
    }

    public float calculatePrediction(int user, int item)
    {
        float simTotalNum = 0;
        float simTotal = 0;
        float sim;

        for (Map.Entry<Integer,Integer> entry: testData.entries())
        {
            sim = getSimilarity(user, entry.getKey());
            simTotalNum += sim * (findUserItemRating(entry.getKey(), item) - averages.get(entry.getKey()));
            simTotal += sim;

        }
        //avg user rating + ((for all nbs of user: (sim(user, nb of user))*(nb rating item - avg nb eating))/
        //                                 for all nbs of user: sim(user, nb of user))
        //
        return simTotalNum/simTotal;
    }

    //dealing with cold start problem: if user hasnt rated item, return avg item rating
    public float findUserItemRating(int user, int item)
    {
        float userItemRating = 0;
        if ((data.get(user).get(item)) != null)
        {
            userItemRating = data.get(user).get(item);
        }
        else
        {
            userItemRating = itemAverages.get(item);
        }
        return userItemRating;
    }

    public void calculateAverages () {
        try {
            SQLiteStatement statement = dataHandler.c.prepare("select userID, cast(avg(rating) as real) from trainingUserBased group by userID;");
            while (statement.step()) {
                averages.put(statement.columnInt(0), (float) statement.columnInt(1));
                System.out.println(statement.columnInt(0) + " " + (float) statement.columnInt(1));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void calculateItemAverages () {
        try {
            SQLiteStatement statement = dataHandler.c.prepare("select itemID, cast(avg(rating) as real) from trainingItemBased group by itemID;");
            while (statement.step()) {
                itemAverages.put(statement.columnInt(0), (float) statement.columnInt(1));
                System.out.println(statement.columnInt(0) + " " + (float) statement.columnInt(1));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public float getSimilarity(int user1, int user2)
    {
        float similarity = 0;

        try {
            SQLiteStatement statement = dataHandler.c.prepare("SELECT Similarity FROM WHERE user1=" + user1 + " AND user2=" + user2 + ";");
            while (statement.step()) {
                    similarity = statement.columnInt(0);
            }

            } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return similarity;
    }
}
