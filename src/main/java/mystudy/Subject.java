package mystudy;

public class Subject {
    private String name;
    private double totalHours;
    private int hoursPerWeek;

    public Subject(String name, double totalHours, int hoursPerWeek) {
        this.name = name;
        this.totalHours = totalHours;
        this.hoursPerWeek = hoursPerWeek;
    }

    @Override
    public String toString() {
        return String.format(
            "%s — Total: %.2f hrs, Weekly Target: %d hrs",
            name, totalHours, hoursPerWeek
        );
    }
    
    public String getName() {
        return name;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public int getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void updateTotalHours(double hours) {
        this.totalHours += hours;
    }

    public void displaySubjectInfo() {
        System.out.println(toString());
    }
}
