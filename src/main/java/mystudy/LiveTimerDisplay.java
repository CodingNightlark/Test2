package mystudy;

public class LiveTimerDisplay extends Thread {
    private final Timer timer;
    private volatile boolean running = true;

    public LiveTimerDisplay(Timer timer) {
        this.timer = timer;
    }

    public void stopDisplay() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            long elapsedMillis = System.currentTimeMillis() - timer.getStartTime();
            long seconds = (elapsedMillis / 1000) % 60;
            long minutes = (elapsedMillis / 1000) / 60;
            System.out.printf("\rTime elapsed: %02d:%02d", minutes, seconds);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("\nTimer interrupted.");
                break;
            }
        }
    }
}