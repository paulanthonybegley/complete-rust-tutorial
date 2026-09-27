public class FlightCalculator {

    private static final int MPH_PER_KPH = 1610 / 1000;

    private final long speedMph;

    public FlightCalculator(long speedMph) {
        if (speedMph <= 0) {
            throw new IllegalArgumentException("speed must be positive, got " + speedMph);
        }
        this.speedMph = speedMph;
    }

    public boolean isRapid() {
        return speedMph > 200L * MPH_PER_KPH; // converting the threshold to mph, loudly
    }

    public long flightMinutes(long distanceMiles) {
        if (distanceMiles < 0) {
            throw new IllegalArgumentException("negative distance " + distanceMiles);
        }
        long wholeMinutes = distanceMiles / speedMph * 60;
        return distanceMiles % speedMph == 0 ? wholeMinutes : wholeMinutes + 1; // no rounding silence
    }

    public long altitudeWithin(long climbRate, long max) {
        if (climbRate < 0) {
            throw new IllegalArgumentException("negative climb rate " + climbRate);
        }
        return climbRate <= max ? climbRate * 10 : 0; // equality included
    }
}