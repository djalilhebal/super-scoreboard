# Animated path options

Approaches I've considered...

## Approach: Just use SVG

This is usually the first thing that comes to my mind.
If I know how to do it using web technologies, why not just use them?
(not unlike what I did in **SeerStone** and **PhiloFX**.)

Displaying an interactive SVG:
- Embegging an entire browser sounds ridiculous (CEF)
- **Apache Batik** SVG Toolkit
    * [Batik Swing components](https://xmlgraphics.apache.org/batik/using/swing.html)
    * [Scripting With Java](https://xmlgraphics.apache.org/batik/using/scripting/java.html)

- I really was considering using SVG for the UI.
Batik works with Swing, allows the manipulation of elements, and even registering event listeners for, e.g. click and mousemove events.
    * [SVG Event Attributes - SVG: Scalable Vector Graphics | MDN](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/Events)

Why SVG may be better than "normal" Swing: \
Drawing the scoreboard using exact numbers while still being able to resize it.


## Approach: A la SVG

Using Swing's **BasicStroke**, do what you would normally do in SVG.


## Approach: `PathIterator`

Using `PathIterator` and `Path2D`,
we can specify when we should start drawing the path and when to stop.

(Best approach? Not sure, but it was the first idea that came to my mind and made the most sense.)

If we want to partially draw something, we need to know:
- Its total length
- When to stop drawing
- Steps required to draw the shape (segments)

**Example**: Draw 37.5% of a 100x100 square.

- The square is represented by 4 line segments.

- The segments we need to draw are 1 and 2.
    * Seg 1 represents 25% of the total length.
    * Seg 2 also represents 25% of the total length.
    * But this is not the result we want: 25% + 25% == 50% != 37.5%.

- Solution: Truncate Seg 2, obvio. \
Since each segment is a line, we can take the ending point and move it back by a specific distance,
creating a new segment Seg 2' that represents 12.5% of the shape.

- Create a `Path2D` using a custom `PathIterator`:
Seg 1 (25%) + Seg 2' (12.5%) = 37.5% of the path.

- Draw the new custom path.

- Done.

Show it would look in Java:
```java
// Init
var rect = new Rectangle(100, 100);
var pp = new PartialPart(rect);

// Update
pp.setEndPercent(37.5);
partialRect = pp.getPath();

// Draw
g2.setColor(Color.LIGHT_GRAY);
g2.draw(rect);
g2.setColor(Color.MAGENTA);
g2.draw(partialRect);
```

```java
/**
 * A class to generate partial paths. WIP.
 *
 * Prob uses `PathLength`.
 * https://github.com/apache/xmlgraphics-batik/blob/main/batik-awt-util/src/main/java/org/apache/batik/ext/awt/geom/PathLength.java
 */
public class PartialPath {

    public PartialPath(Shape shape) {
        // ...
    }
    
    public double getTotalLength() {
        // ...
    }

    public Path2D getPath() {
        Path2D result = new Path2D.Double();
        result.append(getPartialIterator(), true);
        return result;
    }

    public void setEndPoint(Point point) {
        // ...
    }

    public void setEndPercent(double perc) {
        // ...
    }

    PathIterator getPartialIterator() {
        // ...
    }

    
    /**
     * The start and end points are used to calculate the direction vector
     * then the <b>unit vector</b>.
     *
     * - See https://en.wikipedia.org/wiki/Unit_vector
     * - Also https://math.stackexchange.com/a/3932128
     * (Same method, but different way of looking at the problem)
     *
     * @returns a new point, as if "end" was moved by some distance "d" in the same direction.
     */
    private static movedPoint(Point start, Point end, double d) {
        // Direction vector
        var v = new Point(end.x - start.x, end.y - start.y);
        
        // Unit vector
        var magnitude = Math.sqrt(Math.pow(v.x, 2), Math.pow(v.y, 2));
        var unitVector = new Point(v.x / magnitude, v.y / magnitude);
        
        // New point
        var result = new Point(end.x + d * unitVector.x, end.y + d * unitVector.y);
        return result;
    }
    
    final static class PartialPathIterator implements PathIterator {
    
        getCurrent() {
            var lastSegIndex = getSegmentAtLength(maxLength);
            var lastSegLength = segments.get(lastSegIndex);
            altLastSegment = null;
            if (lastSegLength > maxLength) {
                diff = lastSegLength - maxLength;
                newStart = lastSeg.start;
                newEnd = movedPoint(lastSeg.start, lastSeg.end, -diff);
                altLastSegment = new Segment(MOVETO, newStart, newEnd);
            }
            
            if (altLastSegment == null) {
                return partialSegments[last];
            } else {
                return altLastSegment;
            }

        }
    
    }

}
```
