# Lazarus Status

Forge 1.20.1 mod for Project Lazarus. A small survivor portrait button beside the other inventory actions opens a private survivor record screen. No status overlay appears during play. The panel uses original artwork inspired by the pack's worn, dark inventory style.

The server stores statistics per player:

- **World Day:** Overworld day number (`dayTime / 24000`).
- **Days Survived:** in-world time alive during the current run, shown as days and hours. Offline time and Creative/Spectator time do not count; sleeping advances the world clock and does count while connected.
- **Zombies Killed:** kills credited to that player since their last death. Counts vanilla zombies, the Apocalypse Now walkers/runners/crawlers, and the Undead Revamped zombie types used by Lazarus. A canceled death does not count.
- **Longest Run, Total Zombies Killed, Deaths:** personal records that persist across deaths.

Death resets the current-run counters. Traveling to the End does not. On upgrade from 1.0.x, completed survival days and current-run kills are preserved; earlier fractional hours and deaths cannot be reconstructed.

Build with Java 17 using `gradlew.bat build` (which also runs the clock self-check). Install the resulting JAR from `build/libs` on both server and clients. Remove the previous version before installing an update. This mod does not require Lazarus Moodles. The Back button and Esc return to the inventory.
