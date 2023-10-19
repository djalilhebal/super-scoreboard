# Passing Data Deeply a la React (with Context)

## Problem: Prop drilling

```jsx
<App>
    <KScoreboard game>
        <KTeam game teamId>
            <KParticipant game participantId>
                <KSpell game participantId spellId>
```

...


## How React's `useContext` works

Finds the first ancestor context provider and returns its value if it finds it.
Otherwise, it returns the default value.
It causes the component to rerender.

```jsx
import { useContext, createContext } from 'react';

const ThemeContext = createContext(null);

export default function MyPage() {
  return (
    <ThemeContext.Provider value="dark">
      <MyPanel />
    </ThemeContext.Provider>
  );
}

function MyPanel() {
  return (
    <div>
        <MyButton text="Click Me!" />
    </div>
  )
}

function MyButton({ text }) {
    const theme = useContext(ThemeContext);
    const style = {
        backgroundColor: theme == 'dark' ? 'black' : 'white',
        color: 'rgb(255 107 107)', // coral
    }
    
    return (
        <button style={style}>
            {text}
        </button>
    )
}
```


## Our `useContext` semantics

Imagine if we could implement something similar to the previous React code.
- Define a context (`ThemeContext`).
- Define a context provider (any component that implements `ThemeContext`).
- Child components can access (`useContext`) the first parent provider's value. \
If none is found, it returns `null`.
- No automatic rerendering.

Let's call our class Swing **Surge** (you know, React, Flow, Surge, anything that sounds dynamic, reactive/proactive, and has momentum).

```java
//import static scoreboard.Surge.useContext;
import scoreboard.SurgeUser;

//Defining a context provider
interface ThemeContext {
  String getTheme();
}

class MyPage extends JPanel implements ThemeContext {

  public MyPage() {
      add(
          new MyPanel()
      );
  
  }
  
  private String theme = "dark";
  public String getTheme() {
      return theme;
  }
}

class MyPanel extends JPanel {
  public MyPanel() {
      add(
          new MyButton("Click Me!")
      );
  }
}

class MyButton extends JButton implements SurgeUser {
  
  public MyButton(String text) {
      super();

      // NOTE: At this point, the parent is not set, so this will return null.
      // Just use it when you know the parent exists,
      // for example in event listeners, `paint` methods or even `addNotify`.
      //String theme = useContext(ThemeContext.class);
      //this.setBackground("dark".equals(theme) ? Color.BLACK : Color.WHITE);
      //this.setForeground(new Color(255, 107, 107)); // coral
      this.setText(text);
  }
  
  @Override
  public void addNotify() {
	  super.addNotify();

      String theme = useContext(ThemeContext.class);
      this.setBackground("dark".equals(theme) ? Color.BLACK : Color.WHITE);
      this.setForeground(new Color(255, 107, 107)); // coral
  }
  
}
```


## Implementation

Using Java 8 features:
- Reflection API
- Functional interfaces
- Default methods

...



## Making hooks nicer to use

```java
// Before:
import scoreboard.Surge;

class KButton extends JButton implements SurgeUser {

  @Override
  public void paint(Graphics g) {
    String theme = Surge.useContext(ThemeContext.class, this);
    // ...
  }
}
```

Using [static imports][java-static-import]:
```java
// After
import static scoreboard.Surge.useContext;

class KButton extends JButton implements SurgeUser {

  @Override
  public void paint(Graphics g) {
    String theme = useContext(ThemeContext.class, this);
    // ...
  }
}
```

There is room for improvement.

We can use (or misuse?) Java 8's default interface methods to automatically implicitly pass the second argument.

```java
public interface SurgeUser {
    default <T> T useContext(Class<?> clazz) {
        return Surge.useContext(clazz, (Component)this);
    }
}
```

```java
// Final result

class KButton extends JButton implements SurgeUser {

  @Override
  public void paint(Graphics g) {
    String theme = useContext(ThemeContext.class);
    // ...
  }
}
```


## Remarks

<details>

- Autoboxing and unboxing allows you to use primitives in context providers and users.
However, if the context provider could be unavailable, you should use the object wrapper;
otherwise, an exception will be thrown.
```java
// it's like calling `((Integer)null).intValue()`
int val = useContext(CounterContext.class);
```

- For now, we've only implemented `useContext`, but other hooks/features can be added.
`useState`, `useMemo`, `use(Promise)` (or `Future`), `Suspense`, and **Error Boundaries**.
For example, `useState` could call the component's `repaint` method.

- Since we are using an interface,
we can even move the static method `Surge.useContext` to the interface `SurgeUser`, but that sounds wrong.

</details>


## Read more

- [useContext | React.dev](https://react.dev/reference/react/useContext)
- [Passing Data Deeply with Context | React.dev](https://react.dev/learn/passing-data-deeply-with-context)


[java-static-import]: https://docs.oracle.com/javase/8/docs/technotes/guides/language/static-import.html
