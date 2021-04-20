import java.util.Objects;

public class Issue {
    private String id, projectName;
    private Integer politeness;
    public static final int POLITE = 1, MIXED = 0, IMPOLITE = 2;
    public static final String[] POLITE_LEVEL = { "mixed", "polite", "impolite"};
    private Double fixingTime;

    public Issue(String newId) {
        id = newId;
        politeness = null;
    }

    public Issue(String newId, Double newTime)
    {
        id = newId;
        setFixingTime(newTime);
    }

    public String getId() { return id; }

    public void setProjectName(String newName)
    {
        projectName = newName;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setFixingTime(Double newTime)
    {
        fixingTime = newTime;
    }

    public Double getFixingTime() { return fixingTime; }

    public Integer getPoliteness() { return politeness; }

    public void setPoliteness(Integer newPoliteness)
    {
        if (newPoliteness != POLITE && newPoliteness != MIXED && newPoliteness != IMPOLITE)
        {
            System.out.println("ERROR: setting invalid politeness of " + newPoliteness);
        }
        politeness = newPoliteness;
    }

    public String toString()
    {
        return getId() + " - " + getProjectName() + " - " + POLITE_LEVEL[getPoliteness()] + " - " + getFixingTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return getId().equals(issue.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
