# Streams Changelog

All notable changes to this mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-beta.0] - 2026-09-10

### Changed

- Update Farseek dependency.
- Refactor build logic.

### Removed

- Different animated textures for different flow speeds, which are not possible in Minecraft 26+ without extensive duplication of core render methods. Various flow speeds still exist (slower on river edges and faster in the middle), but are not visually differentiated.

## [1.0.0-alpha.2] - 2025-11-21

### Fixed

- Some issues with elevated tributaries having their shores dug out by world carvers.
- An issue where rivers could end abruptly on land above an underground flooded cave.

### Changed

- Updated Farseek dependency.

## [1.0.0-alpha.1] - 2025-06-15

Initial alpha release.

[Unreleased]: https://github.com/delvr/Farseek-Mods/compare/streams-1.0.0-beta.0...HEAD

[1.0.0-beta.0]: https://github.com/delvr/Farseek-Mods/compare/streams-1.0.0-alpha.2...streams-1.0.0-beta.0

[1.0.0-alpha.2]: https://github.com/delvr/Farseek-Mods/compare/streams-1.0.0-alpha.1...streams-1.0.0-alpha.2

[1.0.0-alpha.1]: https://github.com/delvr/Farseek-Mods/releases/tag/streams-1.0.0-alpha.1
