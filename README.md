# SelectiveAdventure

A Paper plugin that lets you protect areas like adventure mode, but with exceptions. You pick a region, pick who is allowed, and pick which blocks they can break or place.

## What it does

- Protects regions so normal players cannot break or place blocks
- Lets specific players break or place specific block types inside a region
- Each region has its own player list and its own allowed block lists
- Regions can be turned on or off without deleting them
- Works without touching normal building outside your regions

## Requirements

- Paper 1.21.11
- Java 21

## Install

- Build the jar (see below) or grab it from the build folder
- Drop `SelectiveAdventure-1.0.0.jar` into your server `plugins` folder
- Start the server once to generate the config
- Edit `plugins/SelectiveAdventure/config.yml` if needed and run `/sa reload`

## Build

- Run `./gradlew build` (or `gradlew.bat build` on Windows)
- The jar is created at `build/libs/SelectiveAdventure-1.0.0.jar`

## Quick start

- `/sa wand` to get the selection tool
- Left click one corner, right click the other corner
- `/sa create library` to save the region
- `/sa addplayer library <name>` to allow a player
- `/sa allowbreak library bookshelf` to let them break bookshelves
- `/sa allowplace library bookshelf` to let them place bookshelves

## Commands

- `/sa wand` give the selection wand
- `/sa pos1` and `/sa pos2` set corners at your location
- `/sa create <name>` create a region from your selection
- `/sa delete <name>` delete a region
- `/sa list` list all regions
- `/sa info <name>` show region details
- `/sa here` show which regions you are standing in
- `/sa enable <name>` and `/sa disable <name>`
- `/sa addplayer <name> <player>` and `/sa removeplayer <name> <player>`
- `/sa listplayers <name>`
- `/sa allowbreak <name> <block>` and `/sa denybreak <name> <block>`
- `/sa allowplace <name> <block>` and `/sa denyplace <name> <block>`
- `/sa togglebreak <name> <block>` and `/sa toggleplace <name> <block>`
- `/sa allowbreakhand <name>` add the block you are looking at or holding
- `/sa allowplacehand <name>` add the block you are holding
- `/sa listblocks <name>` show the allowed block lists
- `/sa visualize <name>` show a particle outline of the region
- `/sa reload` reload config and regions
- `/sa version` show the plugin version

## Permissions

- `selectiveadventure.admin` all commands and bypass
- `selectiveadventure.bypass` ignore all region restrictions
- `selectiveadventure.wand` use the selection wand
- `selectiveadventure.region.create` create regions
- `selectiveadventure.region.manage` manage regions
- `selectiveadventure.reload` reload the plugin

All permissions default to op.

## Config

- `global-adventure-protection` deny building everywhere unless inside an allowed region
- `force-adventure-in-regions` put players in adventure mode while inside a region
- `selection-tool` the item used as the wand, default GOLDEN_AXE
- `show-actionbar-deny-message` show the deny message in the action bar
- `deny-message` the message shown when an action is blocked
- `save-file` the regions file name
- `debug` extra console logging

## Notes

- One region with `default-deny` set to false lets allowed players build freely in that area
- Protection covers block break and block place only
