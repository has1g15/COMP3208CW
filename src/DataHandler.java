import com.almworks.sqlite4java.SQLiteBusyException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Hannah on 28/10/2017.
 */
public class DataHandler {

    final String database = "comp3208.db";
    final String trainingsetUserBased = "trainingUserBased";
    final String trainingsetItemBased = "trainingItemBased";
    final String testSet = "fullTest";

    public SQLiteConnection c;

    /**
     * The data is stored in a HashMap, which allows fast access.
     */
    public HashMap<Integer, HashMap<Integer, Float>> UBData, slopeOneData, temp;
    public Multimap<Integer, Integer> testData;

    /**
     * Open an existing database.
     */
    public DataHandler() {
        c = new SQLiteConnection(new File(database));
        try {
            c.open(false);
            System.out.println("Opened database successfully");
        } catch (SQLiteException e) {
            error(e);
        }
    }

    //Load training data

    public HashMap <Integer,HashMap<Integer,Float>> loadUserData()
    {
        SQLiteStatement stat;
        Integer user;
        Integer item;
        Float rating;

        System.out.println("Loading data from table " + trainingsetUserBased);
        try
        {
            stat = c.prepare("SELECT * FROM " + trainingsetUserBased);

            UBData = new HashMap<>();
            while (stat.step())
            {
                user = stat.columnInt(0);
                item = stat.columnInt(1);
                rating = (float) stat.columnDouble(2);

                HashMap<Integer, Float> userRatings = UBData.get(user);
                if (userRatings == null)
                {
                    userRatings = new HashMap<>();
                    UBData.put(user, userRatings);
                }
                userRatings.put(item, rating);
            }
            stat.dispose();
            System.out.println("Loaded " + UBData.size() + " users.");
        } catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return UBData;
    }

    public HashMap <Integer,HashMap<Integer,Float>> loadItemData()
    {
        SQLiteStatement stat;
        Integer user;
        Integer item;
        Float rating;

        System.out.println("Loading data from table " + trainingsetItemBased);
        try
        {
            stat = c.prepare("SELECT * FROM " + trainingsetItemBased + " ORDER BY itemID");
            slopeOneData = new HashMap<>();
            while (stat.step())
            {
                item = stat.columnInt(0);
                user = stat.columnInt(1);
                rating = (float) stat.columnDouble(2);

                HashMap<Integer, Float> itemRatings = slopeOneData.get(item);
                if (itemRatings == null)
                {
                    itemRatings = new HashMap<>();
                    slopeOneData.put(item, itemRatings);
                }
                itemRatings.put(user, rating);
            }
            stat.dispose();
            System.out.println("Loaded " + slopeOneData.size() + " items.");
        } catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return slopeOneData;
    }

    public Multimap<Integer, Integer> loadTestData()
    {
        SQLiteStatement stat;
        Integer user;
        Integer item;
        Float rating;

        System.out.println("Loading data from table " + testSet);
        try{
            stat = c.prepare("SELECT * FROM " + testSet);
            testData = ArrayListMultimap.create();
            while (stat.step())
            {
                user = stat.columnInt(0);
                item = stat.columnInt(1);
                rating = (float) stat.columnDouble(2);

                testData.put(user, item);
            }
            stat.dispose();
            System.out.println("Loaded " + testData.size() + " users.");
        } catch (SQLiteException e)
        {
            error(e);
        }
        return testData;
    }

    public void createPredictionsTable() {
        try {
            System.out.println("Creating/clearing table predictions");

            // create the table if it does not exist
            c.exec("CREATE TABLE IF NOT EXISTS predictions (Item1 INTEGER, Item2 INTEGER, Prediction REAL)");

            // delete entries from table in case it does exist
            c.exec("DELETE FROM predictions");

            System.out.println("Done");
        } catch (SQLiteException e) {
            error(e);
        }
    }

    public void updatePredictionsTable(int item1, int item2, float prediction)
    {
        try {
            // create the table if it does not exist
            c.exec("INSERT INTO predictions VALUES (" + item1 + "," + item2 + "," + prediction + ");");

        } catch (SQLiteException e) {
            error(e);
        }
    }

    public void createSimilarityMatrix()
    {
        try {
            System.out.println("Creating/clearing table similarities");
            c.exec("CREATE TABLE IF NOT EXISTS predictions (Item1 INTEGER, Item2 INTEGER, Similarity REAL)");
            c.exec("DELETE FROM predictions");
            System.out.println("Done");
        }  catch (SQLiteException e) {
            error(e);
        }
    }

    public void updateSimilarityMatrix(int user1, int user2, float similarity)
    {
        try {
             c.exec("INSERT INTO predictions VALUES (" + user1 + "," + user2 + "," + similarity + ");");
        }   catch (SQLiteException e) {
            error(e);
        }
    }

    public void error(SQLiteException e) {
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
        System.exit(0);
    }

    public void finish() {
        c.dispose();
    }

}
