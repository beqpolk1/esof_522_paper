import java.sql.Connection;
import java.sql.DriverManager;

public class test {
    public static void testing()
    {
        Connection dbConn = null;
        try {
            Class.forName("org.postgresql.Driver");
            dbConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jira_dataset", "postgres", "password");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Opened database successfully!");
    }
}
