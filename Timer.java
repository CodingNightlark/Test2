public class Timer {
    private long startTime;
    private long endTime;

    public Timer() {
        
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.endTime = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return this.endTime - this.startTime;
    }

    public long timeInSeconds() {
        return getElapsedTime() / 1000;
    }

    public long timeInMinutes() {
        return getElapsedTime() / (1000 * 60);
    }

    public long timeInHours() {
        return getElapsedTime() / (1000 * 60 * 60);
    }

    
}
