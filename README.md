# JBCrates

The plugin that will revolutionize crates. *totally*. Anyways, this plugin will be the crates plugin used on the PrismForge Jailbreak server. 

Built on Paper 1.19.4. So all releases support Paper 1.19.4.
## Commands


### /getCrateRewards
Perm: jbcrates.getcraterewards

`/getCrateRewards` Gives the player any rewards they may not have gotten.

### /jbcrates

| Command & Subcommands      | Permission                    | Description                                     |
|----------------------------|-------------------------------|-------------------------------------------------|
| `/jbcrates`                | jbcrates.admin                | Brings up the menu for managing crates.         |
| `reload`                   | jbcrates.admin                | Will reload the config                          |
| `give <player> <crate_id>` | jbcrates.admin, jbcrates.give | Gives the input player a crate based on the id. |

## Other

### Logging
Any activity will be logged in `JBCrates/logs/`

Players with `jbcrates.log` permission will receive the logs in chat as well.

ex. `[OFFLINE] CONSOLE gave Mortality_ crate sp1free`

## To-Do
I will track my progress here and try to think of every possible task. 

Features
- [ ] Disable & Enable a Crate 
- [X] Give crate to offline player will add to "/getcraterewards" for player
- [X] Logs: date, time, sender, reciever, status
- [ ] Lock crate editing to one person per crate to avoid overlapping changes.
- [ ] Add ability to do multi lined descriptions.
- [ ] Maybe convert data storage to SQLite
- [ ] Create Animation System (Multiple Animations & maybe custom animation maker)
- [ ] Auto Updating Lore, Name, Rewards for crates.

Clean Up & Refactor
- [ ] Add any input corrections and checks.
- [ ] Check & fix regex's
- [X] Change to aikar's commands instead.

Bugs
  - [ ] Sometimes after a reward is changed back from a command item, the reward breaks and loses its data and cannot be fixed. (like chance for example)
  - [ ] The permission for `give` is expecting `jbcrates.admin|jbcrates.give` 
  - [ ] A crate will show 8 rewards, and have "...1 more" under it when there are only 8 inside.
