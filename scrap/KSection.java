/**
 * WIP.
 *
 * A la HTML "section" or "div".
 * It should mimic CSS visibility and the box model (content > padding > border > margin).
 */
public class KSection extends JPanel {

    public static enum Visibility {
        VISIBLE;
        HIDDEN;
    }

    @Override
    public void paint(Graphics g) {
        if (visibility == HIDDEN) {
            return;
        }

        super.paint(g);
    }

    private Visibility visibility = Visibility.VISIBLE;

    public void setVisibility(Visibility val) {
        this.visibility = val;
        this.repaint();
    }

    private Border padding;
    private Border margin;
    private Border border;
    
    public void setPadding(Border border) {
        this.padding = border;
        onBorderChange();
    }
    public void setBorder(Border border) {
        this.border = border;
        onBorderChange();
    }
    public void setMargin(Border border) {
        this.margin = border;
        onBorderChange();
    }

    private void onBorderChange() {
        super.setBorder(getCombinedBorder());
    }
    
    private Border getCombinedBorder() {
        var paddingAndBorder = new CompoundBorder(border, margin);
        var all = new CompoundBorder(paddingAndBorder, margin);
        return all;
    }

}
