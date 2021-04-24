import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Attractiveness {
    private String project;
    private HashMap<String, PeriodInfo> stats;

    public Attractiveness(String newProject)
    {
        project = newProject;
        stats = new HashMap<>();
    }

    public String getProject() { return project; }
    public HashMap<String, PeriodInfo> getStats() { return stats; }

    public void buildStats(HashMap<String, Set<String>> userHistory, HashMap<String, Integer[]> politeHistory)
    {
        Set<String> lastSet = null, curSet = null, nextSet = null, magnetSet, stickySet;
        PeriodInfo periodInfo;
        int totalDevs;
        double sticky, magnet, politeness;
        String[] allPeriods = userHistory.keySet().toArray(new String[0]);
        Arrays.sort(allPeriods);

        for (int i = 1; i < allPeriods.length; i++)
        {
            if (project.equals("HHH") && i == 1) i++;
            lastSet = userHistory.get(allPeriods[i - 1]);
            curSet = userHistory.get(allPeriods[i]);

            totalDevs = lastSet.size() + curSet.size();
            magnetSet = new HashSet(curSet);
            magnetSet.removeAll(lastSet);
            magnet = (double) magnetSet.size() / totalDevs;

            if (i < allPeriods.length - 1) {
                nextSet = userHistory.get(allPeriods[i + 1]);
                stickySet = new HashSet(curSet);
                stickySet.retainAll(nextSet);
                sticky = (double) stickySet.size() / curSet.size();
            }
            else
            {
                sticky = 0;
            }

            if (!politeHistory.containsKey(allPeriods[i]))
            {
                periodInfo = new PeriodInfo(magnet, sticky, -1, -1);
            }
            else {
                Integer[] politeCounts = politeHistory.get(allPeriods[i]);
                politeness = (double) politeCounts[1] / politeCounts[0];
                periodInfo = new PeriodInfo(magnet, sticky, politeness, politeCounts[0]);
            }
            stats.put(allPeriods[i], periodInfo);
        }
    }
}
