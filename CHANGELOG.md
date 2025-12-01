# Changelog

All notable changes to the Villager Cycle mod will be documented in this file.

## [1.1.0] - 2025-12-01

### Added
- **Wandering Trader Support**: Can now cycle trades for wandering traders (operator level 4 required on servers)
- **Visual Config Screen**: In-game configuration GUI via Cloth Config API
- **Manual Config Support**: JSON config file for users who don't want ModMenu
- **Anti-Exploit Protection**: Items in trade slots are automatically returned to inventory when cycling
- **Trade Validation**: Prevents cycling wandering traders that have already been traded with
- **Enhanced Tooltips**: Config screen displays default values and ranges for all settings
- **Operator Permissions**: Wandering trader setting requires operator level 4 on multiplayer servers
- **Config Sync System**: Network packets properly sync wandering trader setting between client and server

### Fixed
- **Wandering Trader Detection**: Fixed instanceof check preventing false error messages for regular villagers
- **Button Focus**: Button no longer stays highlighted after multiple trade cycles
- **Config Persistence**: Config changes in GUI now properly sync to server in multiplayer

### Changed
- **Cloth Config**: Now a required dependency
- **ModMenu**: Now optional (only needed for in-game config access)
- Wandering trader cycling disabled by default for security
- Config tooltips show default values in gray text
- Server logs show operator actions when toggling settings

## [1.0.0] - Initial Release

### Added
- Cycle Trades button on villager trading GUI
- Real-time trade refresh without closing GUI
- Basic validation (profession check, experience check, trade-used check)
- Server-side security and validation
- Configurable button position and size
- Support for singleplayer and multiplayer
- Clear user feedback messages
