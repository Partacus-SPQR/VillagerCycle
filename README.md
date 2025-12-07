# Villager Cycle

A Quality of Life mod for Minecraft 1.21.10 Fabric that adds a button to villager trading screens, allowing you to refresh their trades before making any purchases.

## Features

- **Cycle Trades Button**: Customizable button above the villager trading GUI
- **Draggable Button Position**: Reposition the button via drag screen
- **Real-Time Updates**: Trade offers refresh instantly without closing the GUI
- **Keybind Support**: Optional keybinds for all mod functions
- **Configurable Cycle Limits**: Set limits on how many times trades can be cycled (this works per villager only)
- **Wandering Trader Support**: Optional feature (operator-controlled on servers)
- **Smart Validation**: Only works on valid villagers (must have profession, not traded with)
- **Server-Side Security**: All validation happens server-side
- **Anti-Exploit Protection**: Clears trade input slots to prevent item duplication

## Installation

1. Download from [Releases](https://github.com/Partacus-SPQR/VillagerCycle/releases)
2. Place `.jar` in your `.minecraft/mods` folder
3. Required dependencies:
   - **[Fabric API](https://modrinth.com/mod/fabric-api)** 0.138.3+1.21.10+
   - **[Cloth Config](https://modrinth.com/mod/cloth-config)** 20.0.149+
4. Optional: **[ModMenu](https://modrinth.com/mod/modmenu)** for in-game config GUI
5. Launch Minecraft 1.21.10 with Fabric Loader 0.16.9+

## How to Use

1. Open a villager's trading interface
2. Click the "Cycle Trades" button (or use the keybind)
3. The villager's Level 1 trades will regenerate with new offers

## Configuration

### Config File Location
- **Windows**: `%appdata%\.minecraft\config\villagercycle.json`
- **macOS**: `~/Library/Application Support/minecraft/config/villagercycle.json`
- **Linux**: `~/.minecraft/config/villagercycle.json`

### Options

```json
{
  "enableCycleButton": true,
  "allowWanderingTraders": false,
  "showSuccessMessage": true,
  "showWanderingTraderSuccessMessage": true,
  "villagerCycleLimit": -1,
  "wanderingTraderCycleLimit": 1,
  "buttonOffsetX": 6,
  "buttonOffsetY": -25,
  "buttonWidth": 100,
  "buttonHeight": 20
}
```

| Option | Default | Description |
|--------|---------|-------------|
| `enableCycleButton` | `true` | Show/hide the cycle button |
| `showSuccessMessage` | `true` | Show villager cycle success message (client-side) |
| `showWanderingTraderSuccessMessage` | `true` | Show wandering trader success message (client-side) |
| `allowWanderingTraders` | `false` | Enable wandering trader cycling (**operator only** on servers) |
| `villagerCycleLimit` | `-1` | Max cycles per villager: -1=unlimited (**operator only** on servers) |
| `wanderingTraderCycleLimit` | `1` | Max cycles per wandering trader: -1=unlimited (**operator only** on servers) |
| `buttonOffsetX/Y` | `6`/`-25` | Button position offset from GUI |
| `buttonWidth/Height` | `100`/`20` | Button dimensions (pixels) |

### Keybinds

All keybinds are **unbound by default**. Set them in Options → Controls → Villager Cycle.

| Keybind | Function |
|---------|----------|
| Toggle Button Visibility | Show/hide the cycle button |
| Open Button Position Screen | Open drag screen to reposition button |
| Open Config Screen | Open the mod configuration screen |
| Reload Config File | Reload config from disk |
| Cycle Trades | Cycle trades while in merchant screen |

## Singleplayer vs Multiplayer

- **Singleplayer**: All features available with full control over all settings.
- **Multiplayer**: Operator permission (level 4) required for:
  - Allow Wandering Traders toggle
  - Villager Cycle Limit
  - Wandering Trader Cycle Limit

Success message toggles are client-side — each player controls their own preference.

## Limitations

- Only works on villagers not yet traded with
- Only works on villagers with valid professions (not Nitwits/Unemployed)
- Only refreshes Level 1 trades (experience level 0)
- Items in trade slots are returned to inventory when cycling

## Technical Requirements

- **Minecraft**: 1.21.10
- **Fabric Loader**: 0.16.9+
- **Java**: 21+
- **Fabric API**: 0.138.3+1.21.10+
- **Cloth Config**: 20.0.149+

## License

MIT License — Free to include in modpacks.

## Support

[Open an issue](https://github.com/Partacus-SPQR/VillagerCycle/issues) on GitHub for bugs or suggestions.

## Author

Created by Partacus-SPQR
