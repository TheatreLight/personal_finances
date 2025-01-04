import java.sql.SQLException;

public class MainPersonalFinance {
    public static void main(String[] args) throws SQLException {
        DbManager dbManager = new DbManager("jdbc:sqlite:base.db");
        if (dbManager.getDbConnection() == null) {
            throw new RuntimeException("Can't create or connect to database");
        }
        dbManager.createMainTables();

        UserBalanceModel uModel = new UserBalanceModel(dbManager);
        MainController mController = new MainController(uModel);
        ConsoleView cView = new ConsoleView(mController);

        while (!cView.isExited()) {
            cView.run();
        }
    }
}
