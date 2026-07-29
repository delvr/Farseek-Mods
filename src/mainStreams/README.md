**Please read this note carefully before downloading!**

This is a **prerelease** version of Streams 1.0, intended for early testers. It may contain significant bugs or performance issues, or corrupt existing worlds. **Do not use with an existing world!**

As before, Streams requires a compatible version of the Farseek library, which is also released on this repo as a pre-release.

Note that in this new version, vanilla static rivers are *disabled*. Instead you will find two distinct types of Streams rivers:

1. [Tributary networks](https://en.wikipedia.org/wiki/Tributary), similar to those in the previous versions of Streams. They have a small fixed size, many sources, flow down with the terrain, and end by joining a main stem.
2. [Main stems](https://en.wikipedia.org/wiki/Main_stem), which are completely new in this version. Those are much larger than tributaries, form meanders and flow to an ocean or other large-scale outlet. They have potentially unlimited length until they encounter an ocean.

*However* in the context of infinite worlds where we cannot know much of the terrain in advance, there are some major limitations resulting from this infinite length:
  - The main stems are at sea level for their entire course (they still have a flow current of course, which is what Streams is all about!)
  - The main stems go in a straight line (despite the meanders) and in the same direction (currently East to West, this will be configurable later).
  - As a consequence of the above, the main stems are all parallel to each other, with a fixed distance between East-West "corridors" where they can appear.

Given these limitations, even though an individual main-stem river can look quite nice at "eye level", taking a higher view of the terrain will reveal less-realistic repeating patterns. Those are especially obvious if using a map or minimap mod. This will be mitigated and somewhat configurable as the mod evolves, but will always remain as a fundamental trade-off of unlimited-length directional generation in Minecraft.

Another limitation in the current version is that a solid floor is created in all Overworld "noise"-type cave systems at sea level, with a thickness of about 8 blocks. This is necessary to prevent Streams rivers from flowing in mid-air at these locations; as the mod evolves we'll try to find more elegant solutions to this issue. Note that this only affects "noise caves" that are part of the raw terrain generation; "carver" caves that run after world generation can still cross the sea-level zone and will simply "avoid" any Streams they encounter.

In addition to the worldgen changes described above, the new Streams allows generating rivers from scratch using enchanted buckets that force a fixed flow on "flat" water blocks. I won't go into much detail for now (see [this video](https://www.youtube.com/watch?v=IcrG1d5vTnI) for a demo); note that the enchantments are called "Flow", have 3 levels for 3 flow speeds, and must be applied on a bucket that already has a liquid in it (empty buckets can be enchanted but will have no effect).

That's about it for now. Please use GitHub issues for comments, suggestions and bug reports. If possible, try to specify which subtype of streams you're discussing (main stems or tributary networks) and make sure you're aware of their characteristics and limitations described above, before filing a bug. Thanks, and enjoy!

-delvr.
