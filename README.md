# JBCrates

The plugin that will revolutionize crates. *totally*. Anyways, this plugin will be the crates plugin used on the PrismForge Jailbreak server. 

Built on Paper 1.19.4. So all releases support Paper 1.19.4.
## Commands


### /getCrateRewards
Perm: jbcrates.getcraterewards

`/getCrateRewards` Gives the player any rewards they may not have gotten.

### /jbcrates 
Perm: jbcrates.admin

`/jbcrates` Brings up the menu for managing crates.

`/jbcrates give <player> <crate_id>` Gives the input player a crate based on the id.

## To-Do
I will track my progress here and try to think of every possible task. 

Features
- [X] Command Reward Item
- [X] Make editing crate rewards itemstack have a "...5 more" for if theres more.
- [ ] Make Reward Display Sorted (add options and maybe shift left or right to move)
- [ ] Disable & Enable a Crate 
- [ ] Maybe convert data storage to SQLite
- [ ] Create Animation System (Multiple Animations & maybe custom animation maker)
  - [X] Add Default Animation

Clean Up & Refactor
- [ ] Add any input corrections and checks.
- [ ] Check & fix regex's
- [ ] Change to aikar's commands instead.

Bugs
  - [ ] Sometimes after a reward is changed back from a command item, the reward breaks and loses its data and cannot be fixed. (like chance for example)
  - [X] CrateRewards menu is not large enough, make expanding (pages not necessary)

Possible Additions
- [ ] Lore & Name updating for crates.
