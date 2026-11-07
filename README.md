# Cinema Sign (CineBloco addon) - NeoForge 1.21.1

1. Put the CineBloco NeoForge 1.21.1 jar in `libs/`.
2. Edit `CineBlocoBridge.java` (two methods) to match CineBloco's real projector block + playback state.
3. `gradlew build` -> `build/libs/cinemasign-1.0.0.jar`. (Generate the wrapper with `gradle wrapper` if needed.)

In game: craft a Projector Linker, sneak-click the projector, then sneak-click the Cinema Sign.
Sign shows [CLOSED] or [NOW SHOWING: title].
