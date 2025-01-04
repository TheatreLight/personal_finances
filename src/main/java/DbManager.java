import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {
    private final String url;
    private Connection connection;

    public DbManager(String pathToDb) {
        url = pathToDb;
    }

    public Connection getDbConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    public void createMainTables() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("""
                create table if not exists users ( \
                id integer not null primary key, \s
                login varchar, password text)""");

        stmt.execute("""
                create table if not exists income_categories (\
                id integer not null primary key,
                \tuser_id integer,\s
                \tname varchar unique,\s
                \tforeign key(user_id) references users(id))""");

        stmt.execute("""
                create table if not exists incomes (id integer not null primary key,\s
                \tuser_id integer,\s
                \tcategory_id integer,\s
                \tdate timestamp,\s
                \tamount float,\s
                \tforeign key(user_id) references users(id),
                \tforeign key(category_id) references income_categories(id))""");

        stmt.execute("""
                create table if not exists outgoing_categories (id integer not null primary key,
                \tuser_id integer,\s
                \tname varchar unique,\s
                \tforeign key(user_id) references users(id))""");

        stmt.execute("""
                create table if not exists outgoings (id integer not null primary key,
                \tuser_id integer,\s
                \tcategory_id integer,\s
                \tdate timestamp,\s
                \tamount float,\s
                \tforeign key (user_id) references users (id),
                \tforeign key(category_id) references outgoing_categories(id))""");

        stmt.execute("""
                create table if not exists transactions (id integer not null primary key autoincrement,
                \tuser_id integer not null,\s
                \tincome float null,\s
                \toutgoing float null,\s
                \tdate_of_transaction timestamp,\s
                \tbalances_on_limits float null,
                \tmain_balance float null,
                \tforeign key (user_id) references users(id))""");

        stmt.execute("""
                create table if not exists budgets (id integer not null primary key,
                \tuser_id integer,\s
                \tcategory_id integer,\s
                \tlimits float,
                \tforeign key (user_id) references users(id),
                \tforeign key(category_id) references outgoing_categories(id))""");
    }

    public int getLastId(String tableName) throws SQLException {
        if (connection == null) {
            throw new RuntimeException("No connection to DB");
        }

        Statement stmt = connection.createStatement();
        String query = "select max(id) from " + tableName;
        stmt.execute(query);
        var result = stmt.getResultSet();
        return result.getInt(1);
    }
}
