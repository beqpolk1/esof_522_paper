import java.io.*;
import java.nio.Buffer;
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
