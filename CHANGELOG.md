# Changelog

All notable changes to the Villager Cycle mod will be documented in this file.

## [1.2.0] - 2025-12-07

### Added
- **Draggable Button**: Reposition the cycle button via drag screen (set keybind to access)
- **Villager Cycle Limit**: Configurable limit on cycles per villager (operator only, default: unlimited)
- **Wandering Trader Cycle Limit**: Configurable limit on cycles per trader (operator only, default: 1)
- **Show Villager Success Message**: Client-side toggle to hide success messages (reduces chat spam)
- **Show Wandering Trader Success Message**: Client-side toggle to hide trader success messages
- **Button Size Sliders**: Configurable button width and height in config
- **Keybinds**: All unbound by default, set in Controls menu:
  - Toggle button visibility
  - Open button position (drag) screen
  - Open config screen
  - Reload config file
  - Cycle trades (works while merchant screen is open)

### Changed
- Success message settings are now client-side (each player controls their own)
- Cycle limits are operator-only settings on multiplayer servers
- All features work in singleplayer with full control

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
