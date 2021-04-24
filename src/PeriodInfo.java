public class PeriodInfo {
    private double magnet, sticky, politeness;
    private int numComments;

    public PeriodInfo(double newMagnet, double newSticky, double newPolite, int newNumComments)
    {
        magnet = newMagnet;
        sticky = newSticky;
        politeness = newPolite;
        numComments = newNumComments;
    }

    public double getMagnet() { return magnet; }
    public double getSticky() { return sticky; }
    public double getPoliteness() { return politeness; }
    public int getNumComments() { return numComments; }
}
