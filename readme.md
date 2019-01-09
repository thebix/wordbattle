# WordBattle

## How much time was invested
- About 8 hours

## How was the time distributed
- Concept: 0.5 hour
- Domain layer: 4 hours
- Tests: 1.5 hour
- View and game mechanics: 2 hours

## Decisions made to to solve certain aspects of the game
- Android animations for game mechanics
- Architecture: MVI. Because it's convenient and has pretty good separation of layers
- DI: for this small app better to use ServiceLocator pattern, but i've used Dagger2 just to show that i'm okay with Dagger

## Decisions made because if restricted time
- Hardcoded strings and constants. Marked them with // TODO:
- Lack of tests. I've done tests for some essential classes to show tests approach. Rest of classes should be covered the same way
- No persistent storage. Repository should interact with DataBase
- No cache for Api request
- No loader
- No dimens.xml file to store layout dimensions
- No text styles
- Left some minor // TODOs in code

## What would be the first thing to improve or add if there had been more time (from major to minor)
- More tests
- Persistent storage
- UX (Stop/pause button, loader, styles)
