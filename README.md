# Total War Claim
## Video Demo:  https://youtu.be/2C4Ls3LYiP4
## Description:
### Introduction
  Total War Claim (TWClaim) is a Minecraft plugin that allows players to create groups known as "tribes" to protect their land from other players. Many claiming plugins are too rigid with their claiming and war system such as requiring players to be a member of only one town, having too large claim sizes, and having un-fun systems to overcome player claims. TWClaim solves these problems with fluid and flexible member management and land claiming systems that allow players to be residents of multiple towns, nations, and guilds and allow for finely tuned areas of public and private property. TWClaim accomplishes these objectives in a simpler way than other plugins that have tried to solve the same problems, allowing players to leverage the improved functionality without having to bury their noses in wiki pages.  
  
### Member Management
  The member management system for total war is extremely simple. Instead of towns, nations, or factions that a player can only have one of, players can create and join tribes and assign blocks of land to them. Players can also claim blocks privately. Here is an example of this in action:
  
Bob lives in a town with his three friends Andy, Randy, and Brandy. His house is claimed to his person so that nobody can him can open the doors or break the walls. Andy, Randy, and Brandy live in a house across the street. Each of the three friends privately claims their bedrooms, but the dining room and living room are claimed under a tribe the three of them are members of, allowing any of them to make changes to the dining or living room and open the front door. The town hall at the end of the road is claimed by a tribe that everyone in the town is a member of, allowing them to enter, exit, and make improvements. Despite its size, the town has a Roads & Highways committee in charge of building roads around the town. Because nobody outside of the committee has the authority to change the roads, the roads are claimed to a tribe only the members of the Roads & Highways committee belong to, in this case Bob and Randy.

As you can see, just like in real life, the town operates with several groups of permissions determining who can go where and build or destroy what. Bob owns his house, but he can’t go into Andy, Randy, and Brandy’s house without permission. Andy, Randy, and Brandy own their rooms and can change the living and dining rooms, but they can’t go into each other’s rooms without permission. The Town Hall is open to everyone in the town. The roads can only be changed by Bob and Randy, who are in charge of building and maintaining them. 
Normally, towns are divided into 16 x 16 block plots of land that stretch from the top of the world to the bottom. Some plugins allow you to privatize these plots, but it is rare for players to be allowed to own areas smaller than this. With TWClaim, you can! You can even have buildings claimed to the same tribe that exist in multiple “towns”, such as an adventuring guild that has a headquarters in every town. Organizations not directly tied to a town or nation, that allow members from multiple towns and nations to participate in, is basically unheard of. 

Not all members of a tribe are equal. Each tribe has roles such as “member” and “leader” that give certain permissions. Here are the current permissions a role can have:

Kick – Remove players from tribe
Invite – Invite players to join tribe
Reinforce – Claim blocks under the tribe or break blocks claimed under the tribe
Switch – Open doors and containers claimed under the tribe
Bastion – Access a bastion claimed under the tribe

### Protecting Land
TWClaim allows players to protect their land by investing resources into it. Players sacrifice resources to give blocks in their area a level of protection against non-members of their tribe. The protection requires the attacking player to break the block continuously until the reinforcement runs out of durability, and the block finally breaks. The materials used for reinforcement and their durability can be configured in the config file.

There are three ways to protect land:
Reinforce
Hold the reinforcement material in your hand and right click (shift right click with containers and doors) to spend the material on protecting the block. 

Fortify
Hold the reinforcement material in your hand as you type the command to enter fortify mode. Then start placing blocks. As you place blocks, materials will be spent from your inventory to automatically protect them.

Claim
Use left and right click to select the corners of a rectangular prism. When you are satisfied, confirm the claim to protect all blocks inside of the rectangular prism using reinforcement materials from your inventory.

To avoid confusion leaving holes in protection, TWClaim offers two commands to check your reinforcements:
Inspect
Inspect allows you to click on a block and see the owner and reinforcement durability. 

Map
The map charts out the blocks in an 8 block radius around you, allowing you to view the tribe or player that owns the reinforcement by hovering over the block on the map. The compass allows you to orient the map to the cardinal direction you desire. The page system allows you to view different elevations without having to move up or down. 

  
### Bastions
Bastions are an additional form of protection for land. By default, when fueled, they prevent non-members from placing or claiming blocks in a radius around them.
Bastion upgrades:

Anti-Flight
Prevents non-members from using elytras to fly while in the radius.

Anti-Teleport
Prevents non-members from using ender pearls or chorus fruit to teleport inside the radius.

Surveillance
Applies the glowing potion effect to non-members moving inside the radius.

Range
Increases the radius of the bastion.

Exp Storage
Allows players to deposit and withdraw experience to a bank in the bastion.

GUI
Bastions use a GUI system for fueling and upgrades that opens up an inventory for the player to interact with. Listeners determine what options a player selects to navigate through the menus or take actions. Items are used as icons, and their ItemMeta are changed to describe their function. Events that would remove or add items to the GUI inventory are cancelled when appropriate to avoid item duplication or loss. 

Fueling GUI
The fueling GUI allows players to deposit items from their inventory and sacrifice them to add fuel to the bastion. When the “confirm” button is clicked, the plugin cycles through the inventory slots that aren’t icons, checking for fuel materials. Any fuel materials are deleted and contribute fuel to the bastion. Non-fuel items remain where they are and are returned to the player when he closes the menu or retrieves them manually. 

Upgrade GUI
The upgrade GUI informs the player which upgrades are available, what their costs are, and whether upgrades are active or inactive. If an upgrade is not owned, the player can click on it to purchase the upgrade, which removes the material cost from their inventory and marks the upgrade in the bastion’s PersistentDataContainer. If the upgrade is owned, clicking on it will toggle it active or inactive. Upgrades cause the bastion to burn more fuel when they are active, so players may wish to deactivate upgrades they are not currently using.


### Data Storage
Minecraft’s Persistent Data Containers allow plugins to store information in players, chunks, and certain blocks. This information is saved to the world file and remains after server restarts. However, not all blocks are able to have Persistent Data Containers by default, so a workaround was needed. The CustomBlockData plugin is used as a quick way of assigning Persistent Data Containers to any kind of block. In reality, the plugin uses the container of the chunk the block belongs to and uses a denotation to allow the plugin to store and retrieve only information belonging to the block requested. 

For data more complicated than strings and integers, such as TribeData and PlayerData, JSON file storage is used to keep the data between restarts. GSON allows for easy translation to and from JSON files. During startup, the information is loaded into HashMaps to avoid having to load from storage constantly.

  
  
  
  
  
  
