/**
 * WIP.
 *
 * It's like `BasicStroke` except you can reuse the instance.
 * In addition, it adds utility methods to manipulate the dash phase.
 */
public class SlidingStroke extends BasicStroke {

    // returns a clone with dash phase changed
    public SlidingStroke withDashPhase(float phase) {}

    // Or maybe setOffset/setDashOffset a la CSS.
    public void setDashPhase(float phase) {}

    /**
     * Similar to setPhase(float);
     *
     * @param ratio in the range [0-1]
     */
    public void setDashPhaseRatio(double ratio) {}

    // ---

    // maybe it should return `float` or `double`
    public static int totalLengthOf(Shape shape);
    
    // maybe should return `SlidingStroke` instead of `Stroke`
    public static Stroke forShape(Shape shape);    
}
