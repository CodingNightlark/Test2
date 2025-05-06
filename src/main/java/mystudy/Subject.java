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
        System.out.println("Subject Name: " + name);
        System.out.printf("Total Hours: %.2f%n", totalHours);
        System.out.println("Hours per Week: " + hoursPerWeek);
    }
}