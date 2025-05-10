package mystudy;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String name;
    private double totalHours;
    private int hoursPerWeek;
    private List<testScores> testScoresList;
    private List<StudySession> studySessions;

    public Subject(String name, double totalHours, int hoursPerWeek, List<testScores> testScoresList) {
        this.name = name;
        this.totalHours = totalHours;
        this.hoursPerWeek = hoursPerWeek;
        this.testScoresList = testScoresList;
        this.studySessions = new ArrayList<>();
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

    public List<StudySession> getSessions() { return studySessions; }

    public void addSession(StudySession s) { studySessions.add(s); 
}
    public void updateTotalHours(double hours) {
        this.totalHours += hours;
    }

    public void displaySubjectInfo() {
        System.out.println(toString());
    }
}
