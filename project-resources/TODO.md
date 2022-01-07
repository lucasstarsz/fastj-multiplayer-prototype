- [ ] Finish implementing encrypted connections (have both options available? Nah, encrypted only)

## Before Starting Gameplay
- [ ] Add ping-loop of some sort?
- [x] Fix player transforms bug somehow
- [x] Fix server connecting somehow
- [ ] Acquire the linux laptop to generate deb installers/executables
- [ ] Use dad's macbook to generate macOS executables/installers???
- [ ] Test on separate machines (do that before the next bit) -- might need to force accept unverified cert?



## Multiplayer PvP

- [x] Players make snowballs, then throw them
  - [x] Takes 1 second to make a snowball
  - [x] Can only carry 5 at a time
  - [ ] Carrying 4+ without a powerup reduces movement/rotation speed by 20%
  - [x] Takes .5 to throw a snowball
  - [x] snowball travels 700-1000 pixels before petering out
- [x] Player has temp bar & damage bar
  - [x] If they stop moving for too long, their temp bar depletes
  - [x] If they get hit with a snowball, their temp bar + their damage bar depletes
  - [x] If either reaches 0, player freezes/gets knocked out (lose, death message in chat?)
- [ ] If We Have Time: Powerups
  - [ ] warm coat
    - [ ] reduce temp depletion by 90%
    - [ ] Decrease movement speed by 10%
  - [ ] mobile snowball launcher
    - [ ] Decrease movement/rotation speed by 50% automatically
    - [ ] Make snowball in .333 seconds, fire instantly, .333 second cooldown
  - [ ] mittens
    - [ ] Decrease snowball making/throwing time by 50%
    - [ ] Increase carrying capacity to 10 snowballs
  - [ ] Powerups last for 10 seconds each
  - [ ] Only one powerup can be used at a time
  - [ ] If you run over a powerup, you pick it up automatically and discard the old one (ha)

## Messaging System
- [ ] Bottom left, display when things happen
  - [ ] Client-only: picked up powerup
    - [ ] You picked up a
  - [ ] All: player has been knocked out/frozen (witty messages?)
    - [ ] Player couldn't handle the heat. I mean, cold. (Left the game)
    - [ ] Player was bashed around too much. (Sometimes: Medic!) (Damage bar 0, default)
    - [ ] Player was frozen solid. (Sometimes: We'll need a flamethrower to thaw them out.) (Temp bar 0, default)
    - [ ] Player took a snowball to the knee. (Damage bar 0)
    - [ ] Player should have worn warmer clothes. (Temp bar 0, warm coat powerup)
    - [ ] Player couldn't handle holding 8 snowballs (damage bar 0, warm mittens powerup)
    - [ ] Player discovered "The cold doesn't bother me anyways" does not apply to them. (Temp bar 0, took temp damage from standing still)
    - [ ] Player was sniped. (Death by snowball that traveled 777+ pixels)
  - [ ] All: player chat
    - [ ] option to have messages appear above people's heads
      - [ ] in cabin, default above
      - [ ] in-game, default to bottom left chat

## Lobbies
- [x] Specify what host to connect to
- [ ] Join lobby, everyone confirm readiness
  - [ ] Max 8 to a lobby?
  - [ ] lobby should be INSIDE A WOOD CABIN
    - [ ] references - sub? brack? fj? pkcord?
  - [ ] everyone leaves cabin once everyone is ready
- [ ] Position everyone in a circle around spawn
  - [ ] run from cabin out to spawn
- [ ] 3 2 1 go
- [x] on player win, message dialog showing who won
- [x] then, leave lobby
  - [ ] or join next game


## Music

- [ ] Gamemode Settings
  - [ ] Computer to setup gamemode settings (spidertyler2005:tm:)


- [ ] Check voice recorder for references
- [ ] cabin has radio
  - [ ] change the style of music on the radio
  - [ ] chosen style of music carries over to the game battle




## Reeeeeeally Basic Sound Effects
- [ ] Snowball throw
- [ ] Snowball travel?
- [ ] Snowball hit
- [ ] Player stun
- [ ] Player freeze (sprite gets overlaid with frozen.png, player loses)



## Art

- [ ] Prototype psdf looks in krita or even blender
- [ ] Players are psdf ice climber knockoffs 2d homages to ice climbers -- coat color represents player number
- [ ] Snowballs are snowballs
- [ ] Main menu art is an ice climber knockoff throwing a snowball, somehow make this happen idk krita???
- [ ] Mittens
- [ ] Coat
- [ ] Snowball launcher



## UI Elements (yeah, make these) -- maybe mvc for updating them based on player info change?

- [x] Health bars (for Lauren:tm:)
  - [x] Temp/damage bars bottom left above all
- [x] Stat-showing box
  - [x] Snowball count on bottom left of bars (snowball_image: x/y)
  - [ ] Current powerup on bottom right of bars (hover over to see what the powerup does)
  - [ ] Combined buffs/debuffs in top right (hover over to see individual buffs/debuffs?) (+/-x% agility image)
  - [ ] Players currently in lobby on top middle of screen, hold tab to view
- [ ] Debugging text (all top left)
  - [ ] Current player position
  - [ ] Current player rotation
  - [ ] Active snowball list (current position, trajectory)
- [x] Button (FastJ Default)
  - [x] Choose multiplayer from main menu on center of screen
  - [x] Open settings from main menu below multiplayer button
  - [ ] Confirm ready from lobby, bottom center of screen
  - [ ] Leave session to return to multiplayer, top left door with arrow image (open dialog for confirmation)
- [x] Arrowed Buttons
  - [x] Choose Graphics Settings (antialiasing, color rendering, subpixel rendering) (use the names for each setting state as the option shown)
- [x] Slider? Wow this is a lot of work
  - [x] Audio level (0-100, convert to audio level for fastj)
  - [ ] Sfx level (0-100, convert to audio level for fastj)
- [ ] Radio Knob (aie)
  - [ ] 



## Game Name

- [x] Name the dang game why don't you
  - [x] call it "Hit!"
  - [x] Or Snowball Fight or something



## Game Map

- [ ] Mostly white background, tiny hints of blue and super light gray
- [ ] Rocks that cannot be hit through? Or maybe just cosmetic for now



## Optional: Particles. Maybe.

- [ ] Trail behind the snowball
- [ ] Outward particles when the snowball hits something



## How To Play Screen

- [ ] Take screenshots in-game to use as images to display actions
- [ ] Make snowball
- [ ] Throw snowball
- [ ] Dodge opponents' snowballs!
- [ ] Keep an eye on your damage and temperature bars
- [ ] Pick up powerups!

--- feedback friday pog ---

- [ ] Udp (yay more encrypted nonsense)
- [ ] Networking library documentation out the wazoo
- [ ] Unit testing
- [ ] Release networking library

--- back to school maybe ---

- [ ] Client data verification/watching
- [ ] Server ip-ban/kick(/mute?)
- [x] Game settings
- [ ] Art
- [ ] Music
- [ ] Sound effects
- [ ] Release game alpha (maybe?)

--- back to school maybe here actually ---

- [ ] Fix shit
- [ ] Singleplayer gameplay
- [ ] More game modes
- [ ] Lobby settings?
- [ ] Extras
do {
• Open playtest (beta? idfk how this works yet)
• Fix more shit
} while (not ready for release)
- [ ] Release 1.0

--- the future awaits us ---