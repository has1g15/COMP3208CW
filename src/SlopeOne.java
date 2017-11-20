import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Hannah on 27/10/2017.
 */
public class SlopeOne {

    private float[] predictions;
    private DataHandler dataHandler;
    private HashMap<Integer, HashMap<Integer, Float>> data;
    private int entries;

    public SlopeOne()
    {
        dataHandler = new DataHandler();
        loadData();
        entries = dataHandler.getEntryNo();
        calculateItemDiff();
        dataHandler.finish();
    }

    public void loadData()
    {
        data = dataHandler.loadRatings("slopeOne");
    }


    public float[] getPredictions()
    {
        return predictions;
    }


    public void calculateItemDiff()
    {
        dataHandler.createDifferencesTable("differences");
        float difference = 1;
        HashMap<Integer, Float> itemIRatings, itemJRatings;
        List<Integer> userGroupx, userGroupy;
        List<Float> differences;
        int xUser, yUser;
        float rating1, rating2;

        for (int i = 1; i < entries; i++)
        {
            System.out.println("Item: " + i);
            for (int j = i+1; j <= entries; j++)
            {
                System.out.println("Item " + j);
                itemIRatings = data.get(i);
                itemJRatings = data.get(j);
                differences = new ArrayList<>();

                if (itemIRatings != null && itemJRatings != null)
                {
                    userGroupx = new ArrayList<>(itemIRatings.keySet());
                    userGroupy = new ArrayList<>(itemJRatings.keySet());

                    for (int x = 0; x < itemIRatings.size(); x++)
                    {
                        xUser = userGroupx.get(x);
                        //System.out.println("X Value: " + x);

                        for (int y = 0; y < itemJRatings.size(); y++)
                        {
                            yUser = userGroupy.get(y);
                            //System.out.println("Y Value: " + y);

                            if (xUser == yUser)
                            {
                                rating1 = itemIRatings.get(xUser);
                                rating2 = itemJRatings.get(yUser);
                                System.out.println("User x: " + xUser);
                                System.out.println("User y: " + yUser);
                                System.out.println("Item: " + i);
                                System.out.println("Item: " + j);
                                System.out.println(rating1);
                                System.out.println(rating2);
                                System.out.println(rating1 - rating2);
                                differences.add(rating1 - rating2);
                            }
                        }
                    }
                    if (differences.size() > 1)
                    {
                        System.out.println(calculateAvgItemDiff(differences));
                        difference = calculateAvgItemDiff(differences);
                        System.out.println("**********");
                    }
                    else if (differences.size() == 1)
                    {
                        difference = differences.get(0);
                    }
                    dataHandler.updateDifferencesTable(i, j, difference);
                }
            }
        }
    }

    public float calculateAvgItemDiff(List<Float> itemRatingDifferences)
    {
        float total = 0;
        for(Float difference: itemRatingDifferences)
        {
            total = total + difference;
        }
        return total/itemRatingDifferences.size();
    }
}
