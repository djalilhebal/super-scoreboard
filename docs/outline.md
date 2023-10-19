# Outline

One of my 1st year univ profs used to tell us to "stop writing _Java a la C_!"
So, here I am, writing Java a la JavaScript (or Web tech in general).

<aside>
This includes conventions and even the usage of specific language features like "arrow functions" (functional interfaces in Java) and the `var` keyword.

This might not be the best approach/arch, but, ey, it works and makes sense if you believe hard enough.
</aside>

---

- [~] [Animating a path a la SVG](./animating-path-a-la-svg.md)
    * [~] [Animated path options](./animated-path-options.md)

- [~] [Passing data deeply a la React (with Context)](./passing-data-deeply-a-la-react.md)

- [~] [Building an overlay a la Electron](./building-an-overlay-a-la-electron.md)
    - [~] [Overlay options](./overlay-options.md)
    - [~] [Input forwarding options](./input-forwarding-options.md)

---

There is nothing super or extraordinary about it.
It is more like a learning project:
- Long press event
- Long press indicator similar to League's (animated path/border)
- Timer indicator similar to League's (drawing a rectangle inside of a progress pie/arc/circle)
- Java 2D APIs

And most importantly, the non-magical magic part:
Intercepting input native events and forwarding them to a transparent window
using WinAPI's low level hooks.
