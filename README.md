# PVPArena

PvP Arena event plugin. This plugin requires minimal admin interaction to launch a round based PvP game. At round start, all players on the server are teleported to the arena world's spawn location and given gear a few item pools. Each player who dies is put in spectator mode. The final player alive in survival mode wins the round. An admin is required to start the next round via command.

## Compatibility

Written for Paper 1.16.2, with the expectations that it will run on most future versions, maybe Spigot.

## Commands

- `pvparena start` Start a new round. The current world will be used as arena world, therefore a player is expected to use the command

## Permissions

- `pvparena.pvparena` Use the command above

## Areas

File name `pvparena`

- `spawn` Where player (teams) spawn

## Future plans

- Automatically start new round
- Persistent storage
- Handicap based gear distribution
- Clearer gear levels
- More interesting systems: Lives, upgrades, etc