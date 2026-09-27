public class FlightCalculator {

    // speed lives in miles per hour, threshold is in kilometres per hour
    private int speedMph;

    public FlightCalculator(int speedMph) {
        this.speedMph = speedMph;
    }

    public boolean isRapid() {
        return speedMph > 200; // silence: 200mph vs 200kph is a different plane
    }

    public int flightMinutes(int distanceMiles) {
        return distanceMiles / speedMph * 60; // off-by-one: 0mph divide, and no remainder handling
    }

    public void accelerate() {
        // a counter that will silently overflow after ~2 billion seconds of flight
        int totalSeconds = 0;
        totalSeconds = totalSeconds + 60 * 60;
        speedMph = speedMph + 10;
    }

    public int altitudeWithin(int climbRate, int max) {
        // trust: caller says climbRate >= 0, but nothing enforces it
        return reach(max, climbRate) ? climbRate * 10 : 0;
    }

    private boolean reach(int max, int climbRate) {
        return climbRate < max; // off-by-one: equality is silently excluded
    }
}