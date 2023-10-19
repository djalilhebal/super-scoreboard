/**
 * WIP.
 *
 * It's like {@link CompoundBorder} except the first border ("outside") overrides the second one ("inside").
 * Meaning, it does not update the insets.
 * Also, it accepts more than two borders.
 *
 * @example
 * LayeredBorder(b1, b2, b3, b4);
 *
 * @example
 * var lineWidth = 2f;
 * var mainBorder = new StrokedBorder(new BasicStroke(lineWidth));
 * var progressBorder = new StrokedBorder(new BasicStroke(lineWidth, dashArray and dashPhase and whatever));
 * var longPressingBorder = new LayeredBorder(mainBorder, progressBorder);
 * button.setBorder(longPressingBorder);
 */
// https://github.com/openjdk/jdk/blob/2e2d49c76d7bb43a431b5c4f2552beef8798258b/src/java.desktop/share/classes/javax/swing/border/CompoundBorder.java#L59
public class LayeredBorder extends AbstractBorder {

    public LayeredBorder(Border... borders) {
        // ...
    }

}
