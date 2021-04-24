import java.io.*;
import java.util.*;

public class FileHelper
{
    public static HashMap<String, Issue> importIssuesPoliteness(String filePath, int limit) throws FileNotFoundException
    {
        File file = new File(filePath);
        Scanner inFile = new Scanner(file);
        String[] parseLine;
        String curId = "";
        Issue curIssue = null;
        Integer politeness, count = 0;
        HashMap<String, Issue> issues = new HashMap<>();

        inFile.nextLine(); //skip header
        while (inFile.hasNextLine() && (count < limit || limit == -1))
        {
            parseLine = parseCsvLine(inFile.nextLine());

            if (Double.parseDouble(parseLine[15]) > 0.7) {
                if (parseLine[14].equals("POLITE")) politeness = Issue.POLITE;
                else politeness = Issue.IMPOLITE;

                if (curIssue == null || !(curId.equals(parseLine[0]))) {
                    curId = parseLine[0];

                    if (issues.containsKey(curId)) {
                        System.out.println("ERROR! Issue ID changed but the new issue already exists");
                    }

                    curIssue = new Issue(curId);
                    issues.put(curId, curIssue);
                    count++;

                    curIssue.setPoliteness(politeness);
                    curIssue.setProjectName(parseLine[11]);
                } else {
                    if (!Issue.POLITE_LEVEL[curIssue.getPoliteness()].equals(parseLine[14].trim().toLowerCase())
                            && !curIssue.getPoliteness().equals(Issue.MIXED)) {
                        curIssue.setPoliteness(Issue.MIXED);
                    }
                }
            }
        }

        return issues;
    }

    public static void outputIssuesFile(String filePath, Collection<Issue> issues)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("IssueID,ProjectName,Politeness,FixingTime_(hours)" + System.lineSeparator()); //header

            for (Issue curIssue : issues)
            {
                String output = curIssue.getId() + "," + curIssue.getProjectName() + "," + Issue.POLITE_LEVEL[curIssue.getPoliteness()] + "," + curIssue.getFixingTime();
                output += System.lineSeparator();
                writer.write(output);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Failed opening/closing output file!");
            e.printStackTrace();
        }
    }

    public static void outputActivityFile(String filePath, ArrayList<UserActivity> activities, String project)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Project,AuthorID,CommentCount,IssueCount" + System.lineSeparator()); //header

            for (UserActivity curActivity : activities)
            {
                String output = project + "," + curActivity.getUserId() + "," + curActivity.getCommentCnt() + "," + curActivity.getIssueCnt();
                output += System.lineSeparator();
                writer.write(output);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Failed opening/close output file!");
            e.printStackTrace();
        }
    }

    public static ArrayList<UserActivity> importActivityFile(String filePath) throws FileNotFoundException
    {
        File file = new File(filePath);
        Scanner inFile = new Scanner(file);
        String[] parseLine;
        ArrayList<UserActivity> activityList = new ArrayList<>();

        inFile.nextLine(); //skip header
        while (inFile.hasNextLine())
        {
            parseLine = parseCsvLine(inFile.nextLine());

            UserActivity activity = new UserActivity(parseLine[1], Integer.parseInt(parseLine[2]), Integer.parseInt(parseLine[3]), parseLine[0]);
            activityList.add(activity);
        }

        return activityList;
    }

    public static void outputRankedActivityFile(String filePath, ArrayList<UserActivity> activities, HashMap<String, String> rankings)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Project,AuthorID,CommentCount,IssueCount,Ranking" + System.lineSeparator()); //header

            for (UserActivity curActivity : activities)
            {
                String output = curActivity.getProject() + "," + curActivity.getUserId() + "," + curActivity.getCommentCnt() + "," + curActivity.getIssueCnt() + "," + rankings.get(curActivity.getUserId());
                output += System.lineSeparator();
                writer.write(output);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Failed opening/close output file!");
            e.printStackTrace();
        }
    }

    public static void outputIssuesAllFile(String filePath)
    {

    }

    public static ArrayList<Issue> importProcessedIssues(String filePath) throws FileNotFoundException
    {
        File file = new File(filePath);
        Scanner inFile = new Scanner(file);
        String[] parseLine;
        ArrayList<Issue> issues = new ArrayList<>();

        inFile.nextLine(); //skip header
        while (inFile.hasNextLine())
        {
            parseLine = parseCsvLine(inFile.nextLine());
            Issue newIssue = new Issue(parseLine[0]);
            newIssue.setProjectName(parseLine[1]);
            newIssue.setPoliteness(parseLine[2]);
            newIssue.setFixingTime(Double.parseDouble(parseLine[3]));

            issues.add(newIssue);
        }

        return issues;
    }

    public static HashMap<String, HashMap<String, String>> importAllRankings(String filePath) throws FileNotFoundException
    {
        String[] allFiles = getAllFiles(filePath);
        HashMap<String, HashMap<String, String>> allRankings = new HashMap<>();

        for (int i = 0; i < allFiles.length; i++)
        {
            File file = new File(filePath + allFiles[i]);
            Scanner inFile = new Scanner(file);
            String[] parseLine;
            String curProj = null;
            HashMap<String, String> authorRanks = new HashMap<>();

            inFile.nextLine(); //skip header
            while (inFile.hasNextLine())
            {
                parseLine = parseCsvLine(inFile.nextLine());

                if (curProj == null)
                {
                    curProj = parseLine[0];
                }

                authorRanks.put(parseLine[1], parseLine[4]);
            }

            allRankings.put(curProj, authorRanks);
        }

        return allRankings;
    }

    public static void outputAttractiveFile(String filePath, Attractiveness attractiveness)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Project,Period,Magnet,Sticky,Politeness,Tot_Comment" + System.lineSeparator()); //header
            HashMap<String, PeriodInfo> stats = attractiveness.getStats();
            String[] allPeriods = stats.keySet().toArray(new String[0]);
            Arrays.sort(allPeriods);

            for (int i = 0; i < allPeriods.length; i++)
            {
                String period = allPeriods[i];
                PeriodInfo curPeriod = stats.get(period);
                String output = attractiveness.getProject() + "," + period + "," + curPeriod.getMagnet() + "," + curPeriod.getSticky() + ","
                        + (curPeriod.getPoliteness() >= 0 ? curPeriod.getPoliteness() : "nan") + ","
                        + (curPeriod.getNumComments() >= 0 ? curPeriod.getNumComments() : "nan");
                output += System.lineSeparator();
                writer.write(output);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Failed opening/close output file!");
            e.printStackTrace();
        }
    }

    public static void outputAttractiveDiffFile(String filePath, Attractiveness attractiveness)
    {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Project,Period,Magnet,Sticky,Politeness,Tot_Comment" + System.lineSeparator()); //header
            HashMap<String, PeriodInfo> stats = attractiveness.getStats();
            String[] allPeriods = stats.keySet().toArray(new String[0]);
            Arrays.sort(allPeriods);

            for (int i = 0; i < allPeriods.length - 1; i++)
            {
                String period = allPeriods[i];
                PeriodInfo curPeriod = stats.get(period), nextPeriod = stats.get(allPeriods[i + 1]);

                double diffMagnet = nextPeriod.getMagnet() - curPeriod.getMagnet(), diffSticky = nextPeriod.getSticky() - curPeriod.getSticky(),
                        diffPolite;
                if (nextPeriod.getPoliteness() >= 0 && curPeriod.getPoliteness() >= 0)
                {
                    diffPolite = nextPeriod.getPoliteness() - curPeriod.getPoliteness();
                }
                else
                {
                    diffPolite = Double.NaN;
                }
                PeriodInfo diffed = new PeriodInfo(diffMagnet, diffSticky, diffPolite, 0);

                String output = attractiveness.getProject() + "," + period + "," + diffed.getMagnet() + "," + diffed.getSticky() + ","
                        + (!(Double.isNaN(diffed.getPoliteness())) ? diffed.getPoliteness() : "nan") + ","
                        + diffed.getNumComments();
                output += System.lineSeparator();
                writer.write(output);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Failed opening/close output file!");
            e.printStackTrace();
        }
    }

    public static String[] getAllFiles(String dirPath)
    {
        File directoryPath = new File(dirPath);
        String[] contents = directoryPath.list();
        if (Main.DEBUG_LEVEL >= Main.HI_DEBUG)
        {
            for (int i = 0; i < contents.length; i++) System.out.println(contents[i]);
        }
        return contents;
    }

    public static BufferedWriter openOutput(String filePath) throws IOException {
        return new BufferedWriter(new FileWriter(filePath));
    }

    public static void writeOutputLine(BufferedWriter writer, String output) {
        try {
            writer.write(output);
        } catch (IOException e) {
            System.out.println("Error writing to output file!");
            e.printStackTrace();
        }
    }

    public static void closeOutput(BufferedWriter writer) throws IOException {
        writer.close();
    }

    public static boolean checkIfExists(String filePath)
    {
        File checkFile = new File(filePath);
        return checkFile.exists();
    }

    //helper function to parse a csv line that contains literals enclosed in quotes ("")
    private static String[] parseCsvLine(String line)
    {
        String[] splitString = line.split(",");
        String[] parsedList = new String[splitString.length];
        String literal;
        int elementCnt = 0;

        for (int i = 0; i < splitString.length; i++)
        {
            if (!splitString[i].startsWith("\""))
            {
                parsedList[elementCnt] = splitString[i];
            }
            else
            {
                literal = splitString[i].substring(1);

                while (i < splitString.length - 1 && !(splitString[i].endsWith("\"")))
                {
                    i++;
                    literal += "," + splitString[i];
                }

                literal = literal.substring(0,literal.length() - 1);
                parsedList[elementCnt] = literal;
            }
            elementCnt++;
        }

        String[] retVal = new String[elementCnt];
        for (int i = 0; i < elementCnt; i++) retVal[i] = parsedList[i];
        return retVal;
    }
}
