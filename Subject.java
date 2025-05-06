package myStudy;
import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String name;
    private double totalHours;
    private int hoursPerWeek;
    private List<testScores> testScores = new ArrayList<>();

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

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public void updateTotalHours(double hours) {
        this.totalHours += hours;
    }

    public void displaySubjectInfo() {
        System.out.println("Subject Name: " + name);
        System.out.printf("Total Hours: %.2f%n", totalHours);
        System.out.println("Hours per Week: " + hoursPerWeek);
    }

    

    public void addTestScore(testScores score) {
    testScores.add(score);
    }

    public List<testScores> getTestScores() {
    return testScores;
    }   

}
