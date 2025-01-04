import java.sql.SQLException;
import java.util.*;

public class ConsoleView {
    private MainController controller;
    private Scanner scanner;
    private boolean exited = false;

    private void message(String str) {
        System.out.println(str);
    }

    private String reader() {
        String line = "";
        while (line.isEmpty()) {
            line = scanner.nextLine();
        }
        return line;
    }

    private boolean answer() {
        var ans = reader();
        return ans.equals("Y") || ans.equals("y");
    }

    private boolean userAuthorisation() throws SQLException {
        while (!controller.isAuthorisedUser()) {
            message("Please, tell me who you are!");
            message("Login:");
            var login = scanner.nextLine();
            message("Password: ");
            var pwd = scanner.nextLine();
            if (controller.authorize(login, pwd) == 0) {
                message("Current login-password pair is not found. " +
                        "Would you like to register as new user (Y/N)?");
                if (answer()) {
                    controller.register(login, pwd);
                } else {
                    message("Would you like to try another login attempt (Y/N)?");
                    if (!answer()) {
                        exited = true;
                        return false;
                    }
                    continue;
                }
            } else {
                message("Successfully logged in!");
            }
        }
        return true;
    }

    private boolean printCategories(MainController.ECategoryType type) throws SQLException {
        TreeMap<String, Float> list = switch (type) {
            case MainController.ECategoryType.ectIncome -> controller.getIncomesCategoriesList();
            case MainController.ECategoryType.ectOutgoing -> controller.getOutgoingsCategoriesList();
            case MainController.ECategoryType.ectBudget -> controller.getLimitsList();
        };
        if (list.isEmpty())  {
            message("No categories");
            return false;
        }
        for (var item : list.entrySet()) {
            message(String.valueOf(item.getKey()) + ", amount: " + item.getValue());
        }
        return true;
    }

    private void addCategory(MainController.ECategoryType type) throws SQLException {
        message("Enter the names separated by semicolons:");
        String names = reader();
        String[] arr = names.split(":");
        for (var name : arr) {
            controller.createCategory(name, 0, type);
        }
        printCategories(type);
    }

    private void limitsHandle(TreeMap<String, Float> limits, MainController.ECategoryType type) {
        if (type == MainController.ECategoryType.ectIncome) return;

        for (var entry : limits.entrySet()) {
            String categoryName = entry.getKey();
            float limitRemain = entry.getValue();
            if (limitRemain <= 0) {
                message("ATTENTION! Limits are ended or overflown for category: \n\t"
                        + categoryName + "\ncurrent remain is: \n\t"
                        + Float.toString(limitRemain));
            }
        }
    }

    private void categoriesHandle(MainController.ECategoryType type) throws SQLException {
        String info = "";
        if (type == MainController.ECategoryType.ectBudget) {
            message("You have the next limits for your outgoings:");
            printCategories(type);
            info = "Enter the limits for categories <category_id>:<limit> or <E> for the end.";
        } else {
            String name = type == MainController.ECategoryType.ectIncome ? "income" : "outgoing";
            message("You have the next " + name + "'s categories: ");
            if (!printCategories(type)) {
                message("You should create at least 1 category of your " + name);
                addCategory(type);
            }
            message("Would you like to add another one (Y/N)?");
            if (answer()) {
                addCategory(type);
            }
            info = "Enter the amount of " + name
                    + " by categories <category_id>:<your_"
                    + name + "> or <E> for end.";
        }
        message(info);
        String s = reader();
        var limitsOverflown = controller.checkLimits();
        while (!s.equals("E") && !s.equals("e")) {
            String[] s_splitted = s.split(":");
            try {
                if (type == MainController.ECategoryType.ectIncome) {
                    controller.setIncomeForCategory(Integer.parseInt(s_splitted[0]),
                            Float.parseFloat(s_splitted[1]), "");
                } else if (type == MainController.ECategoryType.ectOutgoing) {
                    controller.setOutgoingForCategory(Integer.parseInt(s_splitted[0]),
                            Float.parseFloat(s_splitted[1]));
                    limitsOverflown = controller.checkLimits();
                } else {
                    controller.setLimitForCategory(Integer.parseInt(s_splitted[0]),
                            Float.parseFloat(s_splitted[1]));
                }
                printCategories(type);
            } catch(Exception e) {
                message("Wrong input.");
                message(info);
            }
            s = reader();
        }
        limitsHandle(limitsOverflown, type);
    }

    public void calculateBalance() throws SQLException {
        message("Current user balance:");
        message(controller.calculateBalance());
    }

    public void quitHandler() {
        message("Would you like to continue? (Y/N)");
        if (!answer()) {
            message("See you the next time!");
            exited = true;
        }
    }

    public ConsoleView(MainController mc) {
        controller = mc;
        scanner = new Scanner(System.in);
    }

    public boolean isExited() {
        return exited;
    }

    public void run() throws SQLException {
        if (!userAuthorisation()) return;
        categoriesHandle(MainController.ECategoryType.ectIncome);
        categoriesHandle(MainController.ECategoryType.ectOutgoing);
        categoriesHandle(MainController.ECategoryType.ectBudget);
        calculateBalance();
        quitHandler();
    }
}
