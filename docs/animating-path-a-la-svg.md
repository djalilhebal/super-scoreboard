# Java Swing: Animating a path a la SVG

A la League Mute All button.

<video src="mute-player-button--2023-09-17.mp4" autoplay loop></video>

## SVG

### Demo

<AnimatedSvgStrokeDemo />

![](./images/heart.svg)


## Swing

Let's start from the beginning.
What we know to do: Drawing basic shapes like Rect.
So, let's draw gray rect.
We can change its line width (AKA stroke) using [BasicStroke][BasicStroke].
```java
var shape = new Rectangle(0, 0, 200, 150);
g.setStroke(new BasicStroke(3));
g.setColor(Color.LIGHT_GRAY);
g.draw(shape);
```


Remember that `BasicStroke`? We can actually define a dash pattern, similar to how we do it in SVG.
```java
var len = getTotalLength(path);
// Phase
float dashArray[] = {len};
float dashOffset = len;

/**
 * @param p - percentage must be in the range [0; 1]
 */
public void setAnimationProgess(float p) {
    var phase = len - (p * len);
    stroke.setPhase(phase);
    component.repaint();
}
```

Result:
<video src="animated-rect-swing.mp4" controls></video>


## Length of the path

In JavaScript, we can simply access [`getTotalLength`][mdn-getTotalLength] (in addition to [`getPointAtLength`][mdn-getPointAtLength], which may be useful).

We can use [Batik's `PathLength`][batik-PathLength] ("utilitiy class for length calculations of paths") to get these values: `lengthOfPath` and `pointAtLength` respectively.

For our specific use case, we only care about Rects, so we don't have to add a 3rd party library:
```java
public static double totalLengthOf(Shape shape) {
    if (shape instanceof Rectangle2D rect) {
      return rect.getWidth() * 2 + rect.getHeight() * 2;
    } else {
      throw new IllegalArgumentException("Only rects are supported for now.");
    }
}
```


## Also see

### SVG/CSS

- [stroke-dasharray - SVG: Scalable Vector Graphics | MDN](https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/stroke-dasharray)

- [How SVG Line Animation Works | CSS-Tricks - CSS-Tricks](https://css-tricks.com/svg-line-animation-works/)
- [Animated line drawing in SVG - JakeArchibald.com](https://jakearchibald.com/2013/animated-line-drawing-svg/)


[mdn-getTotalLength]: https://developer.mozilla.org/en-US/docs/Web/API/SVGGeometryElement/getTotalLength
[mdn-getPointAtLength]: https://developer.mozilla.org/en-US/docs/Web/API/SVGGeometryElement/getPointAtLength

[BasicStroke]: https://docs.oracle.com/javase/8/docs/api/java/awt/BasicStroke.html
[StrokeBorder]: https://docs.oracle.com/javase/8/docs/api/javax/swing/border/StrokeBorder.html

[batik-PathLength]: https://xmlgraphics.apache.org/batik/javadoc/org/apache/batik/ext/awt/geom/PathLength.html
