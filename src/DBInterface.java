import java.sql.*;

public class DBInterface {
    private Connection dbConn;

    public void DBInteface() {
        dbConn = null;
    }

    public void openConnection()
    {
        try {
            Class.forName("org.postgresql.Driver");
            dbConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jira_dataset", "postgres", "password");
            System.out.println("Opened database successfully!");
        }
        catch (Exception e)
        {
            System.out.println("Failed to connect to database!");
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void closeConnection()
    {
        try {
            dbConn.close();
        } catch (Exception e) {
            System.out.println("Failed to close database!");
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public Double getIssueFixingTime(String issueId) {
        String query = "SELECT\n" +
                "id, ROUND(CAST(EXTRACT(EPOCH FROM resolved - created) AS NUMERIC) / 3600, 4) fixing_time\n" +
                "FROM jira_issue_report\n" +
                "WHERE id = ?";
        Double fixingTime = null;

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            select.setInt(1, Integer.parseInt(issueId));
            ResultSet results = select.executeQuery();
            int count = 0;

            while(results.next())
            {
                count++;
                fixingTime = results.getDouble("fixing_time");
                if (results.wasNull()) { fixingTime = null; }
                if (count > 1)
                {
                    throw new SQLException("getIssueFixingTime selected too many rows!");
                }
            }

            if (count == 0) throw new SQLException("getIssueFixingTime selected no rows!");
            if (fixingTime == null && Main.DEBUG_LEVEL == Main.MED_DEBUG) { System.out.println("Warning - NULL fixing time for issue " + issueId); }
            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getIssueFixingTime statement failed!");
            throwables.printStackTrace();
        }

        return fixingTime;
    }
}
