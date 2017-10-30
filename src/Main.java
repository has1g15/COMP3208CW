/**
 * Created by Hannah on 23/10/2017.
 */
public class Main {

    public static void main (String[] args)
    {
        new Main();
    }

    public Main()
    {
        DataHandler dataHandler = new DataHandler();
        dataHandler.loadRatings();
    }
}
