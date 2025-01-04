import java.sql.SQLException;
import java.util.TreeMap;

public class MainController {
    private UserBalanceModel usermodel;

    public enum ECategoryType {
        ectIncome,
        ectOutgoing,
        ectBudget
    }

    public MainController(UserBalanceModel model) {
        usermodel = model;
    }

    public int authorize(String login, String password) throws SQLException {
        return usermodel.checkUser(login, password);
    }

    public int register(String login, String password) throws SQLException {
        return usermodel.insertUser(login, password);
    }

    public void createCategory(String name, float limit, ECategoryType type) throws SQLException {
        usermodel.insertNewCategory(name, limit, type);
    }

    public void setIncomeForCategory(int categoryId, float amount, String date) throws SQLException {
        usermodel.insertIncome(categoryId, amount, date);
    }

    public void setOutgoingForCategory(int categoryId, float amount) throws SQLException {
        usermodel.insertOutgoing(categoryId, amount);
    }

    public void setLimitForCategory(int category_id, float amount) throws SQLException {
        usermodel.insertLimits(category_id, amount);
    }

    public UserBalance getUserBalance() {
        throw new RuntimeException("Not implemented yet");
    }

    public boolean isAuthorisedUser() {
        return usermodel.isAuthorised();
    }

    public TreeMap<String, Float> getIncomesCategoriesList() throws SQLException {
        return usermodel.getCategoriesList(ECategoryType.ectIncome);
    }

    public TreeMap<String, Float> getOutgoingsCategoriesList() throws SQLException {
        return usermodel.getCategoriesList(ECategoryType.ectOutgoing);
    }

    public TreeMap<String, Float> getLimitsList() throws SQLException {
        return usermodel.getCategoriesList(ECategoryType.ectBudget);
    }

    public TreeMap<String, Float> checkLimits() throws SQLException {
        return usermodel.checkLimits();
    }

    public String calculateBalance() throws SQLException {
        return usermodel.calculateUserBalance().toString();
    }
}
