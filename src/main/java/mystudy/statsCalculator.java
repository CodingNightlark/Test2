package mystudy;

import java.util.List;

public class statsCalculator {
    public static String getCorrelation(List<testScores> scores) {
        int n = scores.size();
        if (n < 2) return "Not enough data to compute correlation.";

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (testScores ts : scores) {
            double x = ts.getHoursStudiedBefore();
            double y = ts.getScore();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0) return "No correlation (undefined).";

        double r = numerator / denominator;

        if (r > 0.5) return "Positive correlation";
        if (r < -0.5) return "Negative correlation";
        return "No significant correlation";
    }
}