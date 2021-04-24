import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    public ArrayList<String> getAnalyzedProjects()
    {
        String query = "SELECT DISTINCT rprt.project\n" +
                "FROM jira_issue_report rprt\n" +
                "INNER JOIN jira_issue_comment cmnt ON cmnt.issue_report_id = rprt.id\n" +
                "WHERE cmnt.politeness IS NOT NULL";
        ArrayList<String> projList = new ArrayList<>();

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            ResultSet results = select.executeQuery();


            while (results.next())
            {
                projList.add(results.getString("project"));
            }

            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getAnalyzedProjects statement failed!");
            throwables.printStackTrace();
        }

        return projList;
    }

    public ArrayList<UserActivity> getUserActivity(String project)
    {
        String query = "SELECT cmnt.author_id, COUNT(cmnt.*) comment_count, COUNT(DISTINCT cmnt.issue_report_id) iss_count\n" +
                "FROM  jira_issue_comment cmnt\n" +
                "INNER JOIN jira_issue_report rprt ON rprt.id = cmnt.issue_report_id\n" +
                "WHERE rprt.project = ?\n" +
                "GROUP BY cmnt.author_id\n" +
                "ORDER BY iss_count DESC";
        ArrayList<UserActivity> userList = new ArrayList<>();

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            select.setString(1, project);
            ResultSet results = select.executeQuery();

            while (results.next())
            {
                UserActivity curUser = new UserActivity(Integer.toString(results.getInt("author_id")),
                        results.getInt("comment_count"),
                        results.getInt("iss_count"));
                userList.add(curUser);
            }

            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getUserActivity statement failed!");
            throwables.printStackTrace();
        }

        return userList;
    }

    public String[] getIssueInfo(String issueId)
    {
        String query = "SELECT rprt.project, rprt.assignee_id\n" +
                "FROM jira_issue_report rprt\n" +
                "WHERE rprt.id = ?";
        String[] ret = new String[2];

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            select.setInt(1, Integer.parseInt(issueId));
            ResultSet results = select.executeQuery();
            int count = 0;

            while(results.next())
            {
                count++;
                ret[0] = results.getString("project");
                ret[1] = Integer.toString(results.getInt("assignee_id"));

                if (count > 1)
                {
                    throw new SQLException("getIssueInfo selected too many rows!");
                }
            }

            if (count == 0) throw new SQLException("getIssueInfo selected no rows!");
            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getIssueInfo statement failed!");
            throwables.printStackTrace();
        }

        return ret;
    }

    public HashMap<String, Set<String>> getUserHistory(String project)
    {
        String query = "SELECT DISTINCT TO_CHAR(cmnt.creationdate, 'YYYY-MM') period, cmnt.author_id\n" +
                "FROM jira_issue_comment cmnt\n" +
                "INNER JOIN jira_issue_report rprt ON rprt.id = cmnt.issue_report_id\n" +
                "WHERE rprt.project = ?\n" +
                "ORDER BY period";
        HashMap<String, Set<String>> history = new HashMap<>();

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            select.setString(1, project);
            ResultSet results = select.executeQuery();

            while (results.next())
            {
                String curPeriod = results.getString("period");
                if (!(history.containsKey(curPeriod)))
                {
                    history.put(curPeriod, new HashSet());
                }

                history.get(curPeriod).add(Integer.toString(results.getInt("author_id")));
            }

            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getUserActivity statement failed!");
            throwables.printStackTrace();
        }

        return history;
    }

    public HashMap<String, Integer[]> getPolitenessHistory(String project)
    {
        String query = "WITH tot_cmnts AS (\n" +
                "    SELECT TO_CHAR(cmnt.creationdate, 'YYYY-MM') period, rprt.project, COUNT(*) tot_cmnt\n" +
                "    FROM jira_issue_comment cmnt\n" +
                "             INNER JOIN jira_issue_report rprt ON rprt.id = cmnt.issue_report_id\n" +
                "    WHERE rprt.project = ?\n" +
                "      AND cmnt.politeness IS NOT NULL\n AND cmnt.politeness_confidence_level > .7" +
                "    GROUP BY TO_CHAR(cmnt.creationdate, 'YYYY-MM'), rprt.project\n" +
                ")\n" +
                "SELECT tot_cmnts.period, tot_cmnts.tot_cmnt,\n" +
                "(\n" +
                "    SELECT COUNT(*) pol_cmnt\n" +
                "    FROM jira_issue_comment cmnt\n" +
                "    INNER JOIN jira_issue_report rprt ON rprt.id = cmnt.issue_report_id\n" +
                "    WHERE rprt.project = tot_cmnts.project\n" +
                "      AND cmnt.politeness = 'POLITE' AND cmnt.politeness_confidence_level > .7\n" +
                "    AND TO_CHAR(cmnt.creationdate, 'YYYY-MM') = tot_cmnts.period\n" +
                ") pol_cmnt\n" +
                "FROM tot_cmnts";
        HashMap<String, Integer[]> history = new HashMap<>();

        try {
            PreparedStatement select = dbConn.prepareStatement(query);
            select.setString(1, project);
            ResultSet results = select.executeQuery();

            while (results.next())
            {
                String curPeriod = results.getString("period");
                if (!(history.containsKey(curPeriod)))
                {
                    history.put(curPeriod, new Integer[2]);
                }

                history.get(curPeriod)[0] = results.getInt("tot_cmnt");
                history.get(curPeriod)[1] = results.getInt("pol_cmnt");
            }

            select.close();
            results.close();
        } catch (SQLException throwables) {
            System.out.println("getUserActivity statement failed!");
            throwables.printStackTrace();
        }

        return history;
    }
}
