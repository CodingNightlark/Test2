public class Subject {
    private String name;
    private String description;
    private int credits;
    private int totalHours;
    private int hoursPerWeek;

    public Subject(String name, String description, int credits, int totalHours, int hoursPerWeek) {
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.totalHours = totalHours;
        this.hoursPerWeek = hoursPerWeek;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCredits() {
        return credits;
    }

    public int getTotalHours() {
        return totalHours;
    }
    public int getHoursPerWeek() {
        return hoursPerWeek;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }
    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }
    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }
    public void displaySubjectInfo() {
        System.out.println("Subject Name: " + name);
        System.out.println("Description: " + description);
        System.out.println("Credits: " + credits);
        System.out.println("Total Hours: " + totalHours);
        System.out.println("Hours per Week: " + hoursPerWeek);
    }
    
}
