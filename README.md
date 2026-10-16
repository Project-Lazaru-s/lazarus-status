# Lazarus Status

Forge 1.20.1 mod for Project Lazarus. A small **S** button beside the player inventory opens a private status screen. No status overlay appears during play.

The server stores statistics per player:

- **World Day:** Overworld day number (`dayTime / 24000`).
- **Days Survived:** world day minus the player's day of last death or first login.
- **Zombies Killed:** kills credited to that player since their last death. Counts vanilla zombies, the Apocalypse Now walkers/runners/crawlers, and the Undead Revamped zombie types used by Lazarus. A canceled death does not count.

Death resets the two personal counters. Traveling to the End does not. Existing worlds start each player's personal run on their first login with this mod; historical kills and survival days cannot be reconstructed.

Build with Java 17 using `gradlew.bat build`. Install the resulting `build/libs/lazarusstatus-1.0.0.jar` on both server and clients. This mod does not require Lazarus Moodles.
