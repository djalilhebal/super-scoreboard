<style>
    .amy {
        color: deeppink;
    }

    .ttv {
        color: #9146ff;
    }
    .ttv * {
        color: inherit;
    }

    .blunderland {
        color: coral;
    }
</style>

# Introducing SuperScoreboard

Making a spell tracker is simple. Anyone can do it. Many people have done it.

Super Scoreboard is nothing super or extraordinary.

I wanted to make my own, but simpler and braindead easy to use.

This is a blog post series that explains how some of its functionalities were implemented.
See [Outline](./outline.md).

KAITO: reference Amy Poehler? \
<q class="amy">I wrote it ugly and in pieces</q> - Amy Poehler

Thoughts are kinda random, so skip whatever sections you already know.


## Motivation

While watching <b class="ttv">[Kayle 1v9][kayle_1v9]</b> (a Kayle OTP),
one of my pet peeves was seeing him use a spell tracker app (Mobalytics???) to start tracking a summ
then using the scoreboard to ping it to inform his teammates (in team chat).
That feels redundent.

Why not (double) click once and it automatically pings it and starts tracking? \
Why not make the overlay on top of (superimposed) the scoreboard instead of putting it on the side (juxtaposed)? \
Why not make the overlay's UI similar to League's? \
Does the overlay have to consume a lot of resources? \

The answer to all of those questions is "<strong>no reason at no all!</strong>"

KAITO: reference _Alice in Blunderland_?
- <q class="blunderland matter">There is no reason why it should not be so manufactured</q>
- <q class="blunderland alice">No reason at all," said Alice. "I wonder no one has ever thought of that before.</q>
- <q class="blunderland matter">Anyhow, we have gone in for it, and I see no reason why it should not work as well as any other \[solution].</q>


## Imagine

It would be amazing if we could implement something like this:
```jsx
var root = ReactDOM.createRoot(
  document.getElementById('root')
);

var leagueClient = new LeagueClient();

leagueClient.onNewGame(newGame => {
    root.render(
        <KScoreboard game={newGame} />
    );
});
```


[kayle_1v9]: https://www.twitch.tv/kayle_1v9
