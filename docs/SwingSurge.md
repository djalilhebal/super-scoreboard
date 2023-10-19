# SwingSurge

React-like utilities for Java Swing.


## Name

"So, the name "React" is a nod to its core concept of reactivity in UI development."

- ReactifySwing (React-like utilites for Swing)

- Swing Flow (sounds too similar to Vaadin Flow)

- **Swing Surge** (like a flow, except it's a surge(?))


## Other possibilities / TODO

### Automatic repaint

- Might use `RepaintManager.markCompletelyDirty(JComponent)`.

- Could use `JComponent#putClientProperty` and `getClientProperty`,
and let users access them using  `Container#addPropertyChangeListener`. \
The properties `name` will be `key.toString()`, e.g. `ThemeContext.class.toString()` which returns `"interface scoreboard.ThemeContext"`.

Oooor `SurgeUser` automatically registers a prop listener
```java
interface ContextListener extends PropertyChangeListener {
    WeakReference<Component> _target;
    // ...
    onChange() {
        var target = _target.get();
        if (target == null) {
            context.removePropertyChangeListener(this);
            return;
        }
        target.repaint();
    }
}

useContext(clazz, thiz)
    Component context;
    contextListener = new ContextListener(thiz);
    context.addPropertyChangeListener(clazz.toString(), contextListener);
    return val;
```

Keeping track of context state externally:
```java
// in paint
String theme = useContext(ThemeContext.class);

public class App {
    // like createContext();
    public interface ThemeContext {

        default String getTheme() {
            return SurgeContext.getContextValue(ThemeContext.class);
        }

        default void setTheme(String val) {
            SurgeContext.setContextValue(ThemeContext.class, val);
            SurgeContext.changed(ThemeContext.class);
        }
    }
}

useContext(ThemeContext.class);
useContext(clazz, thiz) {
    SurgeContext.track(clazz, thiz);
    return contextValue;
}

class SurgeContext {

    private static WeakHashMap<Component, Class> trackingMap is a weak map of components and contexts
    or maybe a map of weak sets

    public static track(Class, Component) adds it to the set

    public void changed(clazz) {
        Set<Component> components = trackingMap.getOrDefault(clazz, Collections.emptySet());
        components.forEach(c -> c.repaint());
    }
    
}
```


### `useState`

WIP

```java
final class Surge {

    private static WeakHashMap<Component, List<KHolder>> hooks;

    public static <T> KHolder<T> useState(T defaultValue, Component thiz) {
        var holders = hooks.get(thiz);
        return holders.get(i);
    }
    
    public static <T> KHolder<T> useState(Supplier<T> defaultValueFn, Component thiz);

}

/**
 * Value holder
 */
class KHolder<T> {

    private Component c;

    private T value;

    KHolder(T initialValue, Component c) {
        this.c = c;
        this.value = initialValue;
    }
    
    
    public void setValue(Object newValue) {
        var changed = !Objects.equals(value, newValue);
        this.value = newValue;
        if (changed) {
            c.repaint();
        }
    }
    
    public T getValue() {
        return value;
    }
}
```

Example
```java
// Init
var age = useState(0);
incButton.onClick(() -> age.setValue(curr -> curr + 1));

// Paint
ageLabel.setText("" + age.getValue());
```

---

END.
