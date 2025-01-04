import java.sql.Timestamp;
import java.util.TreeMap;

public class UserBalance {
    public int transactionID = 0;
    public int userID = 0;

    public Timestamp dateOfTransaction;

    public float fullIncome = 0;
    public float fullOutcome = 0;
    public float mainBalance = 0;

    public TreeMap<String, Float> incomeByCategory;
    public TreeMap<String, Float> outcomeByCategory;
    public TreeMap<String, Float> balanceOnLimits;

    public String toString() {
        return "Full income: " + Float.toString(fullIncome) + "\n"
                + "Full outgoing: " + Float.toString(fullOutcome) + "\n"
                + "Main balance: " + Float.toString(mainBalance) + "\n";
    }
}
