import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static final int LO_DEBUG = 1, MED_DEBUG = 4, HI_DEBUG = 8;
    public static int DEBUG_LEVEL = MED_DEBUG;
    public static String baseDir = "data_out\\";

    public static void main(String[] args) {
        //makeIssuesFile();
        //makeAllActivityFiles();
        makeAllAttractiveFiles();
        //makeProjectAttractFile("HHH");
    }

    private static void makeIssuesFile()
    {
        HashMap<String, Issue> issues;
        try {
            System.out.println("Importing from file...");
            issues = FileHelper.importIssuesPoliteness("comments_affectiveness.csv", -1);
            System.out.println("Import done");
        } catch (FileNotFoundException e) {
            System.out.println("Error importing file!");
            e.printStackTrace();
            return;
        }

        DBInterface database = new DBInterface();
        System.out.println("Assigning fixing times...");
        database.openConnection();
        ArrayList<String> removals = new ArrayList<>();
        for (String id : issues.keySet())
        {
            Issue curIssue = issues.get(id);
            curIssue.setFixingTime(database.getIssueFixingTime(id));
            if (curIssue.getFixingTime() == null) removals.add(id);
            if (DEBUG_LEVEL == HI_DEBUG) { System.out.println(issues.get(id)); }
        }
        database.closeConnection();
        System.out.println("Done!");

        System.out.println("Removing " + removals.size() + " items from set of " + issues.keySet().size());
        for (String curRemove : removals) { issues.remove(curRemove); }
        System.out.println(issues.keySet().size() + " remaining");

        System.out.println("Writing issues to file...");
        FileHelper.outputIssuesFile(baseDir + "orig_issues_processed.csv", issues.values());
        System.out.println("Done");
    }

    private static void makeAllActivityFiles()
    {
        DBInterface database = new DBInterface();
        database.openConnection();

        System.out.print("Fetching all projects with analyzed comments...");
        ArrayList<String> projects = database.getAnalyzedProjects();
        System.out.println("Done");

        int cnt = 0;
        System.out.println("Found " + projects.size() + " total projects; commencing output");
        for (String curProject : projects)
        {
            cnt++;
            System.out.print(cnt + "/" + projects.size() + " ");
            makeProjectActivityFile(curProject);
        }
        System.out.println("Finished outputting all files");
    }

    private static void makeProjectActivityFile(String project)
    {
        DBInterface database = new DBInterface();
        database.openConnection();

        System.out.print("Fetching user activities for " + project + "...");
        ArrayList<UserActivity> userActivities = database.getUserActivity(project);
        System.out.println("Done");
        database.closeConnection();

        System.out.print("Writing activities to file...");
        FileHelper.outputActivityFile(baseDir + "\\user_activities\\" + project + "_activity.csv", userActivities, project);
        System.out.println("Done");
    }

    private static void makeAllAttractiveFiles()
    {
        DBInterface database = new DBInterface();
        database.openConnection();

        System.out.print("Fetching all projects with analyzed comments...");
        ArrayList<String> projects = database.getAnalyzedProjects();
        System.out.println("Done");

        int cnt = 0;
        System.out.println("Found " + projects.size() + " total projects; commencing output");
        for (String curProject : projects)
        {
            cnt++;
            System.out.print(cnt + "/" + projects.size() + " ");
            makeProjectAttractFile(curProject);
        }
        System.out.println("Finished outputting all files");
    }

    private static void makeProjectAttractFile(String project)
    {
        DBInterface database = new DBInterface();
        database.openConnection();

        System.out.print("Fetching user history for " + project + "...");
        HashMap<String, Set<String>> userHistory = database.getUserHistory(project);
        System.out.println("Done");

        System.out.print("Fetching politeness levels for " + project + "...");
        HashMap<String, Integer[]> politeHistory = database.getPolitenessHistory(project);
        System.out.println("Done");
        database.closeConnection();

        System.out.print("Calculating stats for " + project + "...");
        Attractiveness stats = new Attractiveness(project);
        stats.buildStats(userHistory, politeHistory);
        System.out.println("Done");

        System.out.print("Writing attractiveness to file...");
        FileHelper.outputAttractiveFile(baseDir + "\\attractiveness\\" + project + "_attract.csv", stats);
        System.out.println("Done");
    }
}
