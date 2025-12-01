# Villager Cycle

A simple Quality of Life mod for Minecraft 1.21.10 Fabric that adds a convenient button to villager trading screens, allowing you to refresh their trades before making any purchases.

## Features

- **Cycle Trades Button**: A button appears above the villager trading GUI for easy access
- **Real-Time Updates**: Trade offers refresh instantly without closing the GUI
- **Smart Validation**: Only works on villagers that meet specific criteria:
  - Must have a profession (excludes Nitwits and Unemployed villagers)
  - Cannot have been traded with previously
  - No trades can have been used
- **Server-Side Security**: All validation happens server-side to prevent exploitation
- **Clear User Feedback**: Receive immediate feedback messages about success or failure

## Installation

1. Download the latest release from the [Releases](https://github.com/Partacus-SPQR/VillagerCycle/releases) page
2. Place the `.jar` file in your `.minecraft/mods` folder
3. Ensure you have [Fabric API](https://modrinth.com/mod/fabric-api) installed
4. Launch Minecraft 1.21.10 with Fabric Loader 0.16.9 or higher

## How to Use

1. Locate a villager with a profession
2. Right-click to open the trading interface
3. Click the "Cycle Trades" button positioned above the trading GUI
4. The villager's Level 1 trades will instantly regenerate with new random offers

## Limitations

- Only works on villagers you have not traded with yet
- Only works on villagers with valid professions (not Nitwits or Unemployed)
- Cannot cycle trades if any trades have been used
- Only refreshes Level 1 trades (villagers must be at experience level 0)

**Note:** This mod does not work with wandering traders. If this becomes a feature that users want, a config option will be added to enable/disable in future updates.

## Technical Requirements

- **Minecraft**: 1.21.10
- **Fabric Loader**: 0.16.9 or higher
- **Fabric API**: 0.138.3+1.21.10 or higher
- **Java**: 21 or higher

## License

This mod is licensed under the MIT License. You are free to include it in modpacks.

## Support

If you encounter any bugs or issues, please [open an issue](https://github.com/Partacus-SPQR/VillagerCycle/issues) on GitHub with a detailed description and your game logs.

## Author

Created by Partacus-SPQR
