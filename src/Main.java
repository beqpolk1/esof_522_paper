import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Main {
    public static final int LO_DEBUG = 1, MED_DEBUG = 4, HI_DEBUG = 8;
    public static int DEBUG_LEVEL = LO_DEBUG;
    public static String baseDir = "data_out\\";

    public static void main(String[] args) {
        //makeIssuesFile();

        //makeProjectActivityFile("CAMEL");
        //makeRankedActivityFile(baseDir + "user_activities\\CAMEL_activity.csv");

        //makeAllActivityFiles();
        //makeAllRankedActivityFiles();

        makeIssuesAllFile();

        //makeAllAttractiveFiles();
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
        FileHelper.outputActivityFile(baseDir + "user_activities\\" + project + "_activity.csv", userActivities, project);
        System.out.println("Done");
    }

    private static void makeAllRankedActivityFiles()
    {
        String rootPath = baseDir + "user_activities\\";
        String[] files = FileHelper.getAllFiles(rootPath);
        for (int i = 0; i < files.length; i++)
        {
            makeRankedActivityFile(rootPath + files[i]);
        }
    }

    private static void makeRankedActivityFile(String filepath)
    {
        ArrayList<UserActivity> activities;

        System.out.print("Importing activity file " + filepath + "...");
        try{
            activities = FileHelper.importActivityFile(filepath);
        } catch (FileNotFoundException e) {
            System.out.println("Error importing file!");
            e.printStackTrace();
            return;
        }
        System.out.println("Done");

        int totalUsers = activities.size();
        HashMap<String, String> rankings = new HashMap<>();
        int count = 0, vCount = 0, hCount = 0, sCount = 0;

        System.out.print("Computing user levels...");
        for (UserActivity curActivity : activities)
        {
            count++;
            if (DEBUG_LEVEL >= MED_DEBUG) System.out.println(count + " / " + totalUsers);

            int moreActive = 0, curComments = curActivity.getIssueCnt();
            for (UserActivity checkActivity : activities)
            {
                if (checkActivity.getIssueCnt() >= curComments) moreActive++;
            }

            double percentile = 100 - (100 * (double) moreActive / totalUsers);
            String level;
            if (percentile > 99)
            {
                level = "VHIGH";
                vCount++;
            }
            else if (percentile > 90){
                level = "HIGH";
                hCount++;
            }
            else
            {
                level = "STD";
                sCount++;
            }

            rankings.put(curActivity.getUserId(), level);
        }
        System.out.println("Done");

        if (DEBUG_LEVEL >= LO_DEBUG) System.out.printf("%.2f%% very high, %.2f%% high, %.2f%% std" + System.lineSeparator(),
                (100 * (double) vCount / totalUsers), (100 * (double) hCount / totalUsers), (100 * (double) sCount / totalUsers));

        System.out.print("Exporting user rankings...");
        FileHelper.outputRankedActivityFile(filepath, activities, rankings);
        System.out.println("Done");
    }

    private static void makeIssuesAllFile()
    {
        ArrayList<Issue> processed;
        HashMap<String, HashMap<String, String>> allRanks;

        try {
            System.out.print("Importing issues file...");
            processed = FileHelper.importProcessedIssues(baseDir + "orig_issues_processed.csv");
            System.out.println("Done");
            System.out.print("Importing all activity rank files...");
            allRanks = FileHelper.importAllRankings(baseDir + "user_activities\\");
            System.out.println("Done");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Error importing file!");
            e.printStackTrace();
            return;
        }

        DBInterface database = new DBInterface();
        database.openConnection();
        System.out.print("Outputting cross referenced issue data...");
        try {
            BufferedWriter outFile = FileHelper.openOutput(baseDir + "issues_all_fields.csv");

            for (Issue curIssue : processed)
            {
                String[] projAssignee = database.getIssueInfo(curIssue.getId());
                String project = (projAssignee[0] == null ? "nan" : projAssignee[0]), assignee = (projAssignee[1] == null ? "nan" : projAssignee[1]);
                String assigneeRank = allRanks.get(project).get(assignee);
                String output = curIssue.getId() + "," + curIssue.getProjectName() + "," + Issue.POLITE_LEVEL[curIssue.getPoliteness()] + "," + curIssue.getFixingTime()
                        + "," + assignee + "," + assigneeRank + "," + project + System.lineSeparator();
                FileHelper.writeOutputLine(outFile, output);
            }

            FileHelper.closeOutput(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");

        database.closeConnection();
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
        FileHelper.outputAttractiveFile(baseDir + "attractiveness\\" + project + "_attract.csv", stats);
        FileHelper.outputAttractiveDiffFile(baseDir + "attractiveness\\diff\\" + project + "_attract_diff.csv", stats);
        System.out.println("Done");
    }
}
