# Villager Cycle Configuration

After running the mod once, a config file will be generated at `.minecraft/config/villagercycle.json`

## Configuration Options

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

### Options Explained

- **enableCycleButton** (default: `true`)
  - Enable or disable the cycle trades button entirely
  - Set to `false` to hide the button

- **allowWanderingTraders** (default: `false`)
  - Allow cycling trades for wandering traders
  - Set to `true` to enable wandering trader support

- **buttonOffsetX** (default: `6`)
  - Horizontal position offset from the left edge of the trading GUI
  - Adjust if the button overlaps with other UI elements

- **buttonOffsetY** (default: `-25`)
  - Vertical position offset from the top edge of the trading GUI
  - Negative values place the button above the GUI
  - Positive values place it below

- **buttonWidth** (default: `100`)
  - Width of the button in pixels

- **buttonHeight** (default: `20`)
  - Height of the button in pixels

## Notes

- Changes to the config require a game restart to take effect
- The config file will be created automatically when you first launch the game with the mod installed
