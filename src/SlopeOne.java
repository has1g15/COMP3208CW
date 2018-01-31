import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.collect.Multimap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by Hannah on 27/10/2017.
 */
public class SlopeOne {

    private DataHandler dataHandler;
    private HashMap<Integer, HashMap<Integer, Float>> data, userData;
    private Multimap<Integer, Integer> testData;
    private HashMap<Integer, Float> averages = new HashMap<>();

    public SlopeOne()
    {
        dataHandler = new DataHandler();
        loadData();
        makePredictions();
        dataHandler.finish();
    }

    public void loadData()
    {
        data = dataHandler.loadItemData();
        userData = dataHandler.loadUserData();
        testData = dataHandler.loadTestData();
        calculateAverages();
    }

    //iterate through test data set, pass users and items to calc prediction then add data to predictions hashmap
    public void makePredictions()
    {
        //dataHandler.createPredictionsTable();
        int item;
        float prediction;
        for (Map.Entry<Integer,Integer> entry: testData.entries())
        {
            item = entry.getValue();
            prediction = calcPrediction(entry.getKey(), item);
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

    //calculate prediction - average item rating for user + average of differences
    public float calcPrediction(int user, int item)
    {
        float avg = averages.get(user);
        float totalDifference = 0;
        int count = 0;
        float difference = calculateItemDiffAvg(item);
        {
            totalDifference = totalDifference + difference;
            count++;
        }
        float diffAvg = totalDifference/count;
        return avg + difference;
    }

    public float calculateItemDiffAvg(int item)
    {
        //List<Float> itemDifferences = new ArrayList<>();
        //dataHandler.createDifferencesTable("Differences");
        float itemDiffTotal = 0;
        int count;
        int counter = 0;
        HashMap<Integer, Float> itemIRatings, itemJRatings;
        List<Integer> userGroupx, userGroupy;
        float differences;
        int xUser, yUser;
        float rating1, rating2;

        itemIRatings = data.get(item);

        for (int i = 1; i <= data.size(); i++)
        {
            itemJRatings = data.get(i);
            differences = 0;
            count = 0;

            if (itemIRatings != null && itemJRatings != null)
            {
                userGroupx = new ArrayList<>(itemIRatings.keySet());
                userGroupy = new ArrayList<>(itemJRatings.keySet());

                for (int x = 0; x < itemIRatings.size(); x++)
                {
                    xUser = userGroupx.get(x);

                    for (int y = 0; y < itemJRatings.size(); y++)
                    {
                        yUser = userGroupy.get(y);

                        if (xUser == yUser)
                        {
                            rating1 = itemIRatings.get(xUser);
                            rating2 = itemJRatings.get(yUser);
                            differences = differences + (rating1 - rating2);
                            count++;
                        }
                    }
                }
                if (count > 1) {
                    itemDiffTotal = itemDiffTotal + (differences / count);
                }
                counter++;
                    /*if (differences.size() > 1)
                    {
                        difference = calculateAvgItemDiff(differences);

                        //itemDifferences.add(difference);
                    }
                    else if (differences.size() == 1)
                    {
                        difference = differences.get(0);
                        //itemDifferences.add(difference);

                    }*/
            }
        }
        return itemDiffTotal/counter;
    }

    /*public float calculateAvgItemDiff(List<Float> itemRatingDifferences)
    {
        float total = 0;
        for(Float difference: itemRatingDifferences)
        {
            total = total + difference;
        }
        return total/itemRatingDifferences.size();
    }*/

    /*public float calcAvgUserRating(int user)
    {
        return averages.get(user);
        /*float total = 0;
        int count = 0;
        float avg;

        for (Float rating: userData.get(user).values())
        {
            total = total + rating;
            count++;
        }
        avg = total/count;
        return avg;*/
    //}

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
}
