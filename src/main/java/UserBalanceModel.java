import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

public class UserBalanceModel {
    private DbManager dbManager;
    private int currentUserID = 0;
    private boolean authorised = false;
    private float fullIncome = 0;
    private float fullOutgoings = 0;
    private float mainBalance = 0;
    UserBalance balance;

    public UserBalanceModel(DbManager dbm) {
        dbManager = dbm;
        balance = new UserBalance();
    }

    public boolean isAuthorised() {
        return currentUserID > 0;
    }

    public void setUserID(int id) {
        currentUserID = id;
    }

    public int checkUser(String login, String pwd) throws SQLException {
        Statement stmt = dbManager.getDbConnection().createStatement();
        String query = "select id, password from users where login = '" + login + "'";
        if (!stmt.execute(query)) {
            return 0;
        }

        ResultSet res = stmt.getResultSet();
        int userId = res.getInt(1);
        if (userId == 0) return userId;
        String passwd = res.getString(2);
        if (passwd.equals(pwd)) {
            currentUserID = userId;

        }
        return currentUserID;
    }

    public int insertUser(String login, String pwd) throws SQLException {
        currentUserID = dbManager.getLastId("users") + 1;
        Statement stmt = dbManager.getDbConnection().createStatement();
        String query = "insert into users values (" + String.valueOf(currentUserID) + ", '" + login + "', '" + pwd + "')";
        stmt.execute(query);

        return currentUserID;
    }

    public TreeMap<String, Float> getCategoriesList(MainController.ECategoryType type) throws SQLException {
        Statement stmt = dbManager.getDbConnection().createStatement();
        ResultSet results = null;
        String query;
        String wherePart = "where t.user_id = " + String.valueOf(currentUserID);
        String groupByPart = " group by t.id ";
        switch(type) {
            case MainController.ECategoryType.ectIncome:
                query = "select t.id, t.name, sum(i.amount) from income_categories t "
                        + "full join incomes i on t.id = i.category_id "
                        + wherePart + groupByPart;
                stmt.execute(query);
                results = stmt.getResultSet();
                break;
            case MainController.ECategoryType.ectOutgoing:
                query = "select t.id, t.name, sum(o.amount) from outgoing_categories t \n"
                        + "full join outgoings o on t.id = o.category_id "
                        + wherePart + groupByPart;
                stmt.execute(query);
                results = stmt.getResultSet();
                break;
            case MainController.ECategoryType.ectBudget:
                query = "select oc.id, oc.name, t.limits from budgets t\n" +
                        "join outgoing_categories oc on t.category_id = oc.id \n";
                query += wherePart;
                stmt.execute(query);
                results = stmt.getResultSet();
                break;
        }

        TreeMap<String, Float> resultList = new TreeMap<String, Float> ();
        while (results.next()) {
            String catName = "id: " + Integer.toString(results.getInt(1)) + ", ";
            catName += results.getString(2);
            resultList.put(catName, results.getFloat(3));
        }
        return resultList;
    }

    public void insertNewCategory(String name, float limit, MainController.ECategoryType type) throws SQLException {
        Statement stmt = dbManager.getDbConnection().createStatement();
        int id;
        String tableName;
        String query = "insert into ";
        String mainPart = ", " + String.valueOf(currentUserID) + ", '" + name + "')";
        switch(type) {
            case MainController.ECategoryType.ectIncome:
                tableName = "income_categories";
                id = dbManager.getLastId(tableName) + 1;
                query += tableName + " values (" + String.valueOf(id) + mainPart;
                stmt.execute(query);
                break;
            case MainController.ECategoryType.ectOutgoing:
                tableName = "outgoing_categories";
                id = dbManager.getLastId(tableName) + 1;
                query += tableName + " values (" + String.valueOf(id) + mainPart;
                stmt.execute(query);
                break;
            case MainController.ECategoryType.ectBudget:
                tableName = "outgoing_categories";

                break;
        }
    }

    public void insertIncome(int categoryID, float amount, String date) throws SQLException {
        String query = "insert into incomes(id, user_id, category_id, date, amount) values(?,?,?,?,?)";
        PreparedStatement pstmt = dbManager.getDbConnection().prepareStatement(query);
        int id = dbManager.getLastId("incomes") + 1;
        pstmt.setInt(1, id);
        pstmt.setInt(2, currentUserID);
        pstmt.setInt(3, categoryID);

        pstmt.setString(4, date);
        pstmt.setFloat(5, amount);

        pstmt.executeUpdate();
        fullIncome += amount;
    }

    public void insertOutgoing(int categoryID, float amount) throws SQLException {
        String query = "insert into outgoings(id, user_id, category_id, date, amount) values(?,?,?,?,?)";
        PreparedStatement pstmt = dbManager.getDbConnection().prepareStatement(query);
        int id = dbManager.getLastId("outgoings") + 1;
        pstmt.setInt(1, id);
        pstmt.setInt(2, currentUserID);
        pstmt.setInt(3, categoryID);
        var t = Instant.now();
        Timestamp t_stamp = new Timestamp(t.toEpochMilli());
        pstmt.setTimestamp(4, t_stamp);
        pstmt.setFloat(5, amount);

        pstmt.executeUpdate();
    }

    public void insertLimits(int categoryID, float amount) throws SQLException {
        String query = "insert into budgets(id, user_id, category_id, limits) values(?,?,?,?)";
        PreparedStatement pstmt = dbManager.getDbConnection().prepareStatement(query);
        int id = dbManager.getLastId("budgets") + 1;
        pstmt.setInt(1, id);
        pstmt.setInt(2, currentUserID);
        pstmt.setInt(3, categoryID);
        pstmt.setFloat(4, amount);

        pstmt.executeUpdate();
    }

    public TreeMap<String, Float> checkLimits() throws SQLException {
        String query = "select oc.id, oc.name, (b.limits - o.amount) as remainder from budgets b " +
                "join outgoings o on b.category_id = o.category_id " +
                "join outgoing_categories oc on o.category_id = oc.id " +
                "where b.user_id = ? " +
                "group by oc.id";
        PreparedStatement pstmt = dbManager.getDbConnection().prepareStatement(query);
        pstmt.setInt(1, currentUserID);
        pstmt.execute();

        var results = pstmt.getResultSet();
        TreeMap<String, Float> resultSet = new TreeMap<String, Float>();
        while (results.next()) {
            //results.getInt(1);
            resultSet.put(results.getString(2),
                    results.getFloat(3));
        }
        return resultSet;
    }

    public UserBalance calculateUserBalance() throws SQLException {
        String query = "select outcome, income, (income - outcome) as balance from \n" +
                "(select (select sum(o.amount) " +
                " from outgoings o " +
                " where user_id = ?) as outcome, " +
                " (select sum(i.amount) " +
                " from incomes i " +
                " where user_id = ?) as income)";
        PreparedStatement pstmt = dbManager.getDbConnection().prepareStatement(query);
        pstmt.setInt(1, currentUserID);
        pstmt.setInt(2, currentUserID);
        pstmt.execute();
        var result = pstmt.getResultSet();
        while (result.next()) {
            balance.fullOutcome = result.getFloat(1);
            balance.fullIncome = result.getFloat(2);
            balance.mainBalance = result.getFloat(3);
        }
        balance.transactionID = dbManager.getLastId("transactions") + 1;
        balance.userID = currentUserID;

        return balance;
    }
}
