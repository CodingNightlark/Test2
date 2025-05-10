package mystudy;

public class Timer {
    private long startTime;
    private boolean isRunning;
    
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.isRunning = true;
    }

    public void stop() {
        this.isRunning = false;
    }

    public long getElapsedTime() {
        return isRunning ? 
            System.currentTimeMillis() - startTime : 
            0;
    }

    public long getStartTime() {
        return startTime;
    }
    public boolean getIsRunning() {
        return isRunning;
    }
}