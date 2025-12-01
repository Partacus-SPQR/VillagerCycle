# Villager Cycle

A simple Quality of Life mod for Minecraft 1.21.10 Fabric that adds a convenient button to villager trading screens, allowing you to refresh their trades before making any purchases.

## Features

- **Cycle Trades Button**: A button appears above the villager trading GUI for easy access
- **Real-Time Updates**: Trade offers refresh instantly without closing the GUI
- **Smart Validation**: Only works on villagers that meet specific criteria:
  - Must have a profession (excludes Nitwits and Unemployed villagers)
  - Cannot have been traded with previously
  - No trades can have been used
- **Server-Side Security**: All validation and trade cycling happens server-side to prevent exploitation
- **Anti-Exploit Protection**: Automatically clears trade input slots to prevent item duplication
- **Clear User Feedback**: Receive immediate feedback messages about success or failure
- **Configurable Settings**: In-game configuration screen via ModMenu
- **Wandering Trader Support**: Optional feature that can be enabled by server operators (operator level 4 required)

## Compatibility

- **Singleplayer**: Works perfectly in singleplayer worlds. Simply install the mod and its dependencies.
- **Multiplayer**: Fully compatible with multiplayer servers. Server operators control wandering trader functionality.
- **Modpacks**: Free to include in modpacks under MIT License.

## Installation

1. Download the latest release from the [Releases](https://github.com/Partacus-SPQR/VillagerCycle/releases) page
2. Place the `.jar` file in your `.minecraft/mods` folder
3. Ensure you have the required dependencies installed:
   - **[Fabric API](https://modrinth.com/mod/fabric-api)** 0.138.3+1.21.10 or higher (Required)
   - **[Cloth Config](https://modrinth.com/mod/cloth-config)** 20.0.149 or higher (Required)
   - **[ModMenu](https://modrinth.com/mod/modmenu)** (Optional - only needed for in-game config GUI)
4. Launch Minecraft 1.21.10 with Fabric Loader 0.16.9 or higher

### Dependency Clarification

- **Fabric API + Cloth Config**: Required for the mod to function. The mod will not load without these.
- **ModMenu**: Optional. Provides in-game GUI access to the config screen.
  - **With ModMenu**: Access config via Mods menu → Villager Cycle → Config icon
  - **Without ModMenu**: Manually edit the config file (see below)

## How to Use

1. Locate a villager with a profession
2. Right-click to open the trading interface
3. Click the "Cycle Trades" button positioned above the trading GUI
4. The villager's Level 1 trades will instantly regenerate with new random offers

## Configuration

### Accessing Config

**Option 1: In-Game Config Screen (Recommended)**
1. Install [ModMenu](https://modrinth.com/mod/modmenu)
2. Open the main menu or pause menu
3. Click "Mods" button
4. Find "Villager Cycle" in the list
5. Click the config icon (⚙️) next to the mod name

**Option 2: Manual Config File**

If you don't want to install ModMenu, you can manually edit the configuration file:

**File Location:**
- **Windows**: `%appdata%\.minecraft\config\villagercycle.json`
- **macOS**: `~/Library/Application Support/minecraft/config/villagercycle.json`
- **Linux**: `~/.minecraft/config/villagercycle.json`

The config file is automatically created on first launch with default values. Simply open it in any text editor to modify settings.

### Configuration Options

```json
{
  "enableCycleButton": true,
  "allowWanderingTraders": false,
  "buttonOffsetX": 6,
  "buttonOffsetY": -25,
  "buttonWidth": 100,
  "buttonHeight": 20
}
```

#### Options Explained

- **enableCycleButton** (default: `true`)
  - Enable or disable the cycle trades button entirely
  - Set to `false` to hide the button

- **allowWanderingTraders** (default: `false`)
  - Allow cycling trades for wandering traders
  - **Operator Permission Required**: On multiplayer servers, only operators with level 4 permission can toggle this setting
  - When enabled, wandering trader offers will be completely refreshed
  - Players without operator permissions will receive a message if they attempt to change this setting

- **buttonOffsetX** (default: `6`)
  - Horizontal position offset from the left edge of the trading GUI
  - Adjust if the button overlaps with other UI elements

- **buttonOffsetY** (default: `-25`)
  - Vertical position offset from the top edge of the trading GUI
  - Negative values place the button above the GUI
  - Positive values place it below

- **buttonWidth** (default: `100`)
  - Width of the button in pixels
  - Range: 50-200 pixels

- **buttonHeight** (default: `20`)
  - Height of the button in pixels
  - Range: 10-40 pixels

### Config Notes

- Changes to the config take effect immediately when you click "Done" in the config screen
- On multiplayer servers, only operators with level 4 permission can toggle the wandering trader setting
- The config file is created automatically when you first launch the game with the mod installed

## Limitations

- Only works on villagers you have not traded with yet
- Only works on villagers with valid professions (not Nitwits or Unemployed)
- Cannot cycle trades if any trades have been used
- Only refreshes Level 1 trades (villagers must be at experience level 0)
- Wandering trader cycling requires operator permission on multiplayer servers
- Items in trade input slots are automatically returned to your inventory when cycling

## Technical Requirements

- **Minecraft**: 1.21.10
- **Fabric Loader**: 0.16.9 or higher
- **Java**: 21 or higher

### Required Dependencies
- **Fabric API**: 0.138.3+1.21.10 or higher
- **Cloth Config**: 20.0.149 or higher

### Optional Dependencies
- **ModMenu**: Only required if you want in-game GUI access to config. Not needed if manually editing config file.

## License

This mod is licensed under the MIT License. You are free to include it in modpacks.

## Support

If you encounter any bugs or issues, please [open an issue](https://github.com/Partacus-SPQR/VillagerCycle/issues) on GitHub with a detailed description and your game logs.

## Author

Created by Partacus-SPQR
