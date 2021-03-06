/*
    GriefPreventionPlus-Cities
    Copyright (C) 2015 Antonino Kai Pocorobba

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.kaikk.mc.gppcities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.GriefPreventionPlus;

public class CommandExec implements CommandExecutor {
	public Map<Integer, City> citiesMap = GPPCities.gppc.ds.citiesMap; // GPPCities database
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console commands are WIP!");
			return true;
		}
		Player player = (Player) sender;
		PlayerData playerData=GPPCities.gppc.ds.playerData.get(player.getUniqueId()); 

		// GP's commands override!
		if (cmd.getName().equalsIgnoreCase("abandonallclaims") || cmd.getName().equalsIgnoreCase("deleteallclaims")) {
			// need to check if this player has cities
			City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
			if (city==null) {
				GPPCities.gppc.getServer().getPluginCommand("claimslist").getExecutor().onCommand(sender, cmd, label, args); // Pass the command to GP
			} else {
				player.sendMessage(Messages.CantRunCommandYouHaveCity.get(Integer.toString(city.claim.getLesserBoundaryCorner().getBlockX()), Integer.toString(city.claim.getLesserBoundaryCorner().getBlockZ())));
			}
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("abandonclaim") || cmd.getName().equalsIgnoreCase("abandontoplevelclaim") || cmd.getName().equalsIgnoreCase("deleteclaim") || cmd.getName().equalsIgnoreCase("transferclaim")) {
			Claim claim = GriefPreventionPlus.instance.dataStore.getClaimAt(player.getLocation(), false, playerData.lastClaim);
			if (claim==null) {
				GPPCities.gppc.getServer().getPluginCommand("claimslist").getExecutor().onCommand(sender, cmd, label, args); // No claim here, pass the command to GP
			} else if (GPPCities.gppc.ds.citiesMap.get((claim.parent!=null?claim.parent.getID():claim.getID()))==null) {
				GPPCities.gppc.getServer().getPluginCommand("claimslist").getExecutor().onCommand(sender, cmd, label, args); // No city here, pass the command to GP
			} else {
				player.sendMessage(Messages.YouCantRunCommandOnACity.get());
			}
			return true;
		}

		// GPPCities commands
		if (!player.hasPermission("gppc.city")) {
			player.sendMessage(Messages.NoPermission.get());
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("city")) {
			if (args.length==0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("info")) {
				City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
				if ((city==null && (args.length==0 || !args[0].equalsIgnoreCase("info"))) || (args.length!=0 && args[0].equalsIgnoreCase("help"))) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6===== GriefPreventionPlus-Cities Help ====="));
					player.sendMessage("- help - show this help");
					if (player.hasPermission("gppc.citychat") && city!=null) {
						player.sendMessage("- cc - send a message to your citizens or toggle the default send channel");
					}
					player.sendMessage("- list [join] - show city list, specify join for joinable cities");
					player.sendMessage("- join - join a city");
					player.sendMessage("- info - info about a city");
					player.sendMessage("- res - info about a player");
					player.sendMessage("- new [city name] - make a new city");
					if (city!=null) {
						Citizen citizen;
						if ((citizen = city.getCitizen(player.getUniqueId())) != null) {
							if (citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Mayor's commands:"));
							} else if (citizen.checkPerm(CitizenPermission.Assistant.perm)) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Assistant's commands:"));
							} else {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Citizen's commands:"));
							}
							
							player.sendMessage("- spawn");
							player.sendMessage("- leave - Leave the city");
							player.sendMessage("- plot - plot management");
							
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Spawn.perm)) {
								player.sendMessage("- setspawn");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage("- plot [new|assign|delete|autoclaim|motd]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Motd.perm)) {
								player.sendMessage("- motd [res|out|current]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm)) {
								player.sendMessage("- invite [player]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Expel.perm)) {
								player.sendMessage("- expel [citizen name]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
								player.sendMessage("- perm [citizen name] (set|unset) (A|E|M|I|P|S) - Manage permissions");
								player.sendMessage("- autojoin [true|false] - Players can auto join this city");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage("- rename [city name]");
								player.sendMessage("- mayor [citizen name] - Change mayor");
								player.sendMessage("- delete [city name]");
							}
						}
					}
					return true;
				}
				
				
				if (args.length==2) {
					city = GPPCities.gppc.ds.getCity(args[1]);
					if (city==null) {
						player.sendMessage(Messages.WrongCityName.get());
						return false;
					}
				}
				if (city!=null) {
					player.sendMessage(city.info());
				} else {
					player.sendMessage(Messages.YouNotInACity.get());
				}
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (citiesMap.isEmpty()) {
					player.sendMessage(Messages.NoCities.get());
					return true;
				}
				
				ArrayList<City> cities = new ArrayList<City>(citiesMap.values());
				Collections.sort(cities, Collections.reverseOrder(new CitySizeComparator()));
				
				if (args.length==2 && args[1].equalsIgnoreCase("join")) {
					player.sendMessage(Messages.JoinableList.get());

					for (City city : cities) {
						if (city.isJoinable) {
							player.sendMessage(Messages.CitiesListFormat.get(city.name, Integer.toString(city.citizens.size())));
						}
					}
					return true;
				}
				
				player.sendMessage(Messages.CitiesList.get());
				
				for (City city : cities) {
					player.sendMessage(Messages.CitiesListFormat.get(city.name, Integer.toString(city.citizens.size())));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("spawn")) {
				if (!player.hasPermission("gppc.city.spawn")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.YouNotCitizen.get());
						return true;
					}
					
					if (city.spawn==null || !city.claim.contains(city.spawn, false, false)) {
						city.spawn=null;
						player.sendMessage(Messages.CitySpawnMissing.get());
						return true;
					}
					
					player.teleport(city.spawn);
					return true;
				}
				
				if (!player.hasPermission("gppc.city.spawn.any")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
					
				City city = GPPCities.gppc.ds.getCity(args[1]);

				if (city==null) {
					player.sendMessage(Messages.ThisCityNotExists.get());
					return true;
				}
				
				if (city.spawn==null || !city.claim.contains(city.spawn, false, false)) {
					city.spawn=null;
					player.sendMessage(Messages.CitySpawnMissing.get());
					return true;
				}

				player.teleport(city.spawn);
				return true;
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length==0) {
					player.sendMessage("Usage: /city invite [playerName]");
					return false;
				}
				
				City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
				
				if (city==null) {
					player.sendMessage(Messages.InviteNotAllowed.get());
					return false;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm)) {
					player.sendMessage(Messages.InviteNotAllowed.get());
					return false;
				}
				
				Player player2 = DataStore.getOnlinePlayer(args[1]);
				
				if (player2 == null) {
					player.sendMessage(Messages.PlayerOfflineOrWrongName.get());
					return false;
				}
				
				if (GPPCities.gppc.ds.getCity(player2.getUniqueId())!=null) {
					player.sendMessage(Messages.PlayerAlreadyOnAnotherCity.get());
					return false;
				}
				
				if (city.bannedPlayers.contains(player2.getUniqueId())) {
					player.sendMessage(Messages.PlayerIsBanned.get(city.name));
					return false;
				}
				
				PlayerData playerData2=GPPCities.gppc.ds.playerData.get(player2.getPlayer().getUniqueId()); 
				Date now = new Date();
				if (playerData2.lastInvitedTime == 0 || playerData2.lastInvitedTime+120000 < now.getTime()) {
					playerData2.invited(city, player.getUniqueId());
					player.sendMessage(Messages.CityInvitationSent.get(args[1]));
					player2.sendMessage(Messages.CityInvitationReceived.get(player.getName(), city.name));
					player2.sendMessage(Messages.CityInvitationAcceptOrReject.get());
					return true;
				}
				player.sendMessage(Messages.CityInvitationExpiresOn.get(Long.toString((120-((System.currentTimeMillis()-playerData2.lastInvitedTime)/1000)))));
				return false;
			} else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("reject")) {
				Date now = new Date();
				if (playerData.lastInvitedTime==0 || playerData.lastInvitedTime+120000 < now.getTime()) {
					player.sendMessage(Messages.CityInvitationNoPending.get());
					return false;
				}
				
				if (args[0].equalsIgnoreCase("accept")) {
					player.sendMessage(Messages.CityInvitationAccepted.get());
					GPPCities.gppc.getServer().getPlayer(playerData.lastInvitedFrom).sendMessage(Messages.CityInvitationHasBeenAccepted.get(player.getName()));
					playerData.lastInvitedCity.newCitizen(player.getUniqueId());
				} else {
					player.sendMessage(Messages.CityInvitationRejected.get());
					GPPCities.gppc.getServer().getPlayer(playerData.lastInvitedFrom).sendMessage(Messages.CityInvitationHasBeenRejected.get(player.getName()));
				}
				playerData.removeInvite();
				return true;
			} else if (args[0].equalsIgnoreCase("join")) {
				// permette ad un tizio che non è già in una città
				// di entrare in una città che permette l'ingresso
				
				String cityName=DataStore.mergeStringArrayFromIndex(args, 1);

				if (cityName.length()==0){
					player.sendMessage(Messages.YouAlreadyOnAnotherCity.get());
					return false;
				}
				City city = GPPCities.gppc.ds.getCity(cityName);
				if (city==null) {
					player.sendMessage(Messages.CityNotExists.get(cityName));
					return false;
				}
				
				if (GPPCities.gppc.ds.getCity(player.getUniqueId())!=null) {
					player.sendMessage(Messages.YouAlreadyOnAnotherCity.get());
					return false;
				}
				
				if (city.bannedPlayers.contains(player.getUniqueId())) {
					player.sendMessage(Messages.YouAreBanned.get(city.name));
					return false;
				}
				
				if (city.isJoinable) {
					city.sendMessageToAllCitizens(Messages.PlayerJoinedYourCity.get(player.getDisplayName()));
					city.newCitizen(player.getUniqueId());
					player.sendMessage(Messages.YouJoinedCity.get(city.name));
				} else {
					player.sendMessage(Messages.CityAutojoinNotPermitted.get(city.name));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("delete")) {
				// cancella la città di cui si è sindaci
				// richiedi anche il nome della città per sicurezza
				
				String cityName=DataStore.mergeStringArrayFromIndex(args, 1);
				
				if (cityName == null || cityName=="") {
					player.sendMessage("Usage: /city delete townname");
				}
				
				City city = GPPCities.gppc.ds.getCity(cityName);
				if (city==null) {
					player.sendMessage(Messages.WrongCityNameCS.get());
					return false;
				}
				
				if (player.hasPermission("gppc.cityAdmin")) { // Admins can delete a city
					GPPCities.gppc.ds.deleteCity(city.claim.getID());
					return true;
				}
				
				if (player.getUniqueId()==city.getMayor().id) { // The mayor can delete a city
					GPPCities.gppc.ds.deleteCity(city.claim.getID());
					return true;
				}
				
				player.sendMessage(Messages.NoPermission.get());
				return false;
			} else if (args[0].equalsIgnoreCase("autojoin")) {
				if (args.length<2) {
					player.sendMessage("Usage: /city autojoin [true|false]");
					return false;
				}
				
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
					player.sendMessage(Messages.NotAuthorized.get());
					return true;
				}
				
				if (args[1].equalsIgnoreCase("true")) {
					city.isJoinable=true;
					player.sendMessage(Messages.CityAutojoinOn.get());
					return true;
				}
				if (args[1].equalsIgnoreCase("false")) {
					city.isJoinable=false;
					player.sendMessage(Messages.CityAutojoinOff.get());
					return true;
				}				
				
				player.sendMessage("Usage: /city autojoin [true|false]");
				return false;
			} else if (args[0].equalsIgnoreCase("motd")) {
				if (args.length<2) {
					player.sendMessage("Usage: /city motd [res|out|current] [message]");
					return false;
				}
				
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Motd.perm)) {
					player.sendMessage(Messages.NotAuthorized.get());
					return true;
				}
				
				String motd=DataStore.mergeStringArrayFromIndex(args, 2);
				
				if (args[1].equalsIgnoreCase("res") || args[1].equalsIgnoreCase("out")) {
					if (city.getMayor().id.equals(player.getUniqueId())) {
						city.setMotd(motd, args[1].equalsIgnoreCase("res"));
						player.sendMessage(Messages.MotdSet.get());
						return true;
					}
					player.sendMessage(Messages.NoPermission.get());
					return false;
				} else if (args[1].equalsIgnoreCase("current")) {
					// show both current motd
					player.sendMessage(Messages.MotdRes.get(ChatColor.translateAlternateColorCodes('&', "&a"+city.motdRes))); 
					player.sendMessage(Messages.MotdOut.get(ChatColor.translateAlternateColorCodes('&', "&a"+city.motdOut)));
					return true;
				}
				player.sendMessage("Usage: /city motd [res|out|current] [message]");
				return false;
			} else if (args[0].equalsIgnoreCase("leave")) {
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				player.sendMessage(Messages.YouLeftCity.get(city.name));
				city.removeCitizen(player.getUniqueId());
				city.sendMessageToAllCitizens(Messages.PlayerLeftCity.get(player.getDisplayName()));
				return true;
			} else if (args[0].equalsIgnoreCase("expel")) {
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Expel.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city expel [CitizenName]");
					return false;
				}
				
				citizen = city.getCitizen(args[1]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				if (GPPCities.gppc.getServer().getPlayer(citizen.id) != null) {
					GPPCities.gppc.getServer().getPlayer(citizen.id).sendMessage(Messages.YouveBeenExpelled.get(city.name));
				}
				city.removeCitizen(citizen.id);
				player.sendMessage(Messages.CitizenExpelled.get());
				return true;
			} else if (args[0].equalsIgnoreCase("res")) {
				if (args.length==1) {
					City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.YouNotInACity.get());
						return false;
					}
					player.sendMessage(Messages.CitizensList.get(city.name));
					
					ArrayList<Citizen> citizens = new ArrayList<Citizen>(city.citizens.values());
					Collections.sort(citizens, new CitizenLastPlayedComparator());
					
					for (Citizen citizen : citizens) {
						player.sendMessage(Messages.CitizensListFormat.get(citizen.getDisplayName(), Long.toString((System.currentTimeMillis()-citizen.getLastPlayed())/86400000)));
					}
					return true;
				} else if (args.length==2) {
					OfflinePlayer cPlayer=GriefPreventionPlus.instance.resolvePlayer(args[1]);
					if (!cPlayer.hasPlayedBefore()) {
						player.sendMessage(Messages.PlayerOfflineOrWrongName.get());
						return false;
					}

					City city = GPPCities.gppc.ds.getCity(cPlayer.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.PlayerIsNotACitizen.get());
						return false;
					}
					
					Citizen citizen = city.getCitizen(cPlayer.getUniqueId());
					player.sendMessage(Messages.CitizenInfo.get(citizen.getDisplayName(), city.name, DateFormat.getDateTimeInstance().format(citizen.joinedOn), DateFormat.getDateTimeInstance().format(citizen.getLastPlayed())));
					return true;
				}
				player.sendMessage(Messages.WrongCommand.get());
			} else if (args[0].equalsIgnoreCase("mayor")) {
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city mayor [CitizenName]");
					return false;
				}
				Citizen citizen = city.getCitizen(args[1]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				
				city.changeOwner(citizen.id);
				return true;
			} else if (args[0].equalsIgnoreCase("rename")) {
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city rename [CityName]");
					return false;
				}
				
				String newName=DataStore.mergeStringArrayFromIndex(args, 1);
				if (newName==null || newName.isEmpty()) {
					player.sendMessage(Messages.WrongCityName.get());
					return false;
				}
				
				City existentCity = GPPCities.gppc.ds.getCity(newName);
				if (existentCity!=null) {
					player.sendMessage(Messages.CitySameNameExists.get());
					return false;
				}
				
				city.rename(newName);
				player.sendMessage(Messages.CityRenamed.get());
				return true;
			} else if (args[0].equalsIgnoreCase("perm")) {
				if (args.length==1) {
					player.sendMessage("Usage: /city perm [citizen name] (set|unset) (A|I|E|M|P|S)");
					player.sendMessage("[A-ssistant, I-nvite, E-xpel, M-otd, P-lot, S-pawn]");
					return false;
				}
				
				City city=GPPCities.gppc.ds.getCity(player.getUniqueId());
				
				Citizen senderCitizen = city.getCitizen(player.getUniqueId());
				if (!senderCitizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				Citizen citizen = city.getCitizen(args[1]);
				if (citizen==null) {
					player.sendMessage(Messages.PlayerIsNotACitizen.get());
					return false;
				}

				if (args.length==4) {
					if (senderCitizen.checkPerm(CitizenPermission.Assistant) && args[3].toLowerCase().contains("a")) {
						player.sendMessage(Messages.NoPermission.get());
						return false;
					}
					
					if (args[2].equalsIgnoreCase("set")) {
						if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
							citizen.setPerm(Citizen.parsePerms(args[3]));
						}
					} else if (args[2].equalsIgnoreCase("unset")) {
						citizen.unsetPerm(Citizen.parsePerms(args[3]));
					}
				}
				
				player.sendMessage(Messages.CitizenPermissions.get(args[1], citizen.permsToString()));
				return true;
			} else {
				// questi comandi necessitano dei dati del claim in cui ci si trova al momento
				Claim claim = GriefPreventionPlus.instance.dataStore.getClaimAt(player.getLocation(), false, playerData.lastClaim);

				if (claim == null) {
					// comandi da fare SOLO fuori dai claim, se mai ce ne fossero
					player.sendMessage(Messages.YouNotOnAClaim.get());
					return true;
				}
				Claim pClaim; // this contains the parent claim or itself if there's no parent
				if (claim.parent != null) {
					pClaim = claim.parent;
				} else {
					pClaim = claim;
				}
				
				City city = GPPCities.gppc.ds.citiesMap.get(pClaim.getID());

				if (pClaim.isAdminClaim()) {
					player.sendMessage(Messages.CitiesNotAvailableOnAdminClaims.get());
					return false;
				}
				
				if (args[0].equalsIgnoreCase("new")) {
					if (!player.getUniqueId().equals(pClaim.ownerID)) {
						player.sendMessage(Messages.NoPermission.get());
						return false;
					}
					
					if (args.length==1) {
						player.sendMessage("Usage: /city new [city name]");
						return false;
					}

					if (city != null) {
						player.sendMessage(Messages.ClaimAlreadyACity.get());
						return false;
					}
					
					if (pClaim.getArea()<GPPCities.gppc.config.CityMinSize) {
						player.sendMessage(Messages.ClaimTooSmall.get(Integer.toString(GPPCities.gppc.config.CityMinSize)));
						return false;
					}
					
					String cityName=DataStore.mergeStringArrayFromIndex(args, 1);
					String result = GPPCities.gppc.ds.newCity(pClaim, cityName, player.getUniqueId(), player.getLocation());
					if (result=="") {
						player.sendMessage(Messages.ClaimAlreadyACity.get());
					} else {
						player.sendMessage(result);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("setspawn")) {
					Citizen citizen = city.getCitizen(player.getUniqueId());
					if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Spawn.perm)) {
						player.sendMessage(Messages.NoPermission.get());
						return false;
					}
					if (city.claim.contains(player.getLocation(), false, false)) {
						city.setSpawn(player.getLocation());
						player.sendMessage(Messages.CitySpawnSet.get());
					} else {
						player.sendMessage(Messages.CitySpawnInvalid.get());
					}
					return true;
				} else if (args[0].equalsIgnoreCase("plot")) {
					if (claim.parent == null) {
						player.sendMessage(Messages.StandOnPlotOrSubClaim.get());
						return true;
					}
					if (city == null) {
						player.sendMessage(Messages.StandOnPlotOrSubClaim.get());
						return true;
					}
					Plot plot = city.getPlot(claim);
					if (plot!=null){
						// esiste già un plot in questo subclaim
						if (args.length==1) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage("Usage: /city plot [assign|unassign|claim|autoclaim|delete]");
							} else {
								player.sendMessage("Usage: /city plot [claim|motd]");
							}
							return false;
						}
						
						if (args[1].equalsIgnoreCase("assign")) {
							if (args.length!=3) {
								player.sendMessage("Usage: /city plot assign [citizenName]");
								return false;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							citizen = city.getCitizen(args[2]);
							if (citizen == null) {
								player.sendMessage(Messages.PlayerIsNotACitizen.get());
								return false;
							}
							
							if (plot.citizen != null && GPPCities.gppc.getServer().getPlayer(plot.citizen.id) != null) {
								GPPCities.gppc.getServer().getPlayer(plot.citizen.id).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							city.assignPlot(claim, citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							
							if (GPPCities.gppc.getServer().getPlayer(citizen.id) != null) {
								GPPCities.gppc.getServer().getPlayer(citizen.id).sendMessage(Messages.YouGotAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							return true;
						}
						if (args[1].equalsIgnoreCase("unassign")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							

							if (GPPCities.gppc.getServer().getPlayer(plot.citizen.id) != null) {
								GPPCities.gppc.getServer().getPlayer(plot.citizen.id).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							plot.unassign();
							player.sendMessage(Messages.PlotUnassigned.get());
							return true;
						}
						
						if (args[1].equalsIgnoreCase("delete")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (plot.citizen != null && GPPCities.gppc.getServer().getPlayer(plot.citizen.id) != null) {
								GPPCities.gppc.getServer().getPlayer(plot.citizen.id).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							city.deletePlot(plot);
							
							player.sendMessage(Messages.PlotDeleted.get());
							return true;
						}
						
						if (args[1].equalsIgnoreCase("motd")) {
							if (args.length<2) {
								player.sendMessage("Usage: /city plot motd [message]");
								return false;
							}
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm) && (plot.citizen==null || !plot.citizen.id.equals(player.getUniqueId()))) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							String motd=DataStore.mergeStringArrayFromIndex(args, 2);
							if (motd==null) {
								motd="";
							}
							
							plot.motd(motd);
							player.sendMessage(Messages.PlotMotdEdited.get());
							return true;
						}
						
						if (args[1].equalsIgnoreCase("claim")) {
							if (!plot.isAutoClaimable) {
								player.sendMessage(Messages.PlotAutoAssignNotAllowed.get());
								return true;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (citizen==null) {
								player.sendMessage(Messages.YouNotACitizenHere.get());
								return true;
							}
							
							if (city.citizenHasAssignedPlot(citizen)) {
								player.sendMessage(Messages.PlotAutoAssignYouHaveAPlotAlready.get());
								return true;
							}
							
							plot.assign(citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							
							return true;
						}
						
						
						if (args[1].equalsIgnoreCase("autoclaim")) {
							if (args.length<3) {
								player.sendMessage("Usage: /city plot autoclaim (true|false)");
								return false;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (args[2].equalsIgnoreCase("true")) {
								plot.autoClaimable(true);
								player.sendMessage("Plot auto-claim: on");
							} else if (args[2].equalsIgnoreCase("false")) {
								plot.autoClaimable(false);
								player.sendMessage("Plot auto-claim: off");
							}
							return true;
						}
						
						player.sendMessage(Messages.WrongCommand.get());
						return false;
					} else {
						// non esiste un plot in questo subclaim
						if (args.length==1) {
							player.sendMessage("Usage: /city plot [new|assign]");
							return false;
						}
						
						
						if (args[1].equalsIgnoreCase("new")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							city.newPlot(claim);
							player.sendMessage(Messages.PlotCreated.get());
							return true;
						} 
						
						if (args[1].equalsIgnoreCase("assign")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (args.length!=3) {
								player.sendMessage("Usage: /city plot assign [citizenName]");
								return false;
							}
							
							citizen = city.getCitizen(args[2]);
							if (citizen == null) {
								player.sendMessage(Messages.PlayerIsNotACitizen.get());
								return false;
							}
							
							city.assignPlot(claim, citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							
							if (GPPCities.gppc.getServer().getPlayer(citizen.id) != null) {
								GPPCities.gppc.getServer().getPlayer(citizen.id).sendMessage(Messages.YouGotAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							return true;
						}
						
						player.sendMessage(Messages.WrongCommand.get());
						return false;
					}
				}

				player.sendMessage(Messages.WrongCommand.get());
				return true;
			}
		}
		
		
		if (cmd.getName().equalsIgnoreCase("citychat")) {
			if (!player.hasPermission("gppc.citychat")) {
				player.sendMessage(Messages.NoPermission.get());
				return false;
			}
			
			if (args.length==0) {
				if (GPPCities.gppc.ds.cityChat.contains(player.getUniqueId())) {
					GPPCities.gppc.ds.cityChat.remove(player.getUniqueId());
					player.sendMessage(Messages.CityChatOff.get());
				} else {
					GPPCities.gppc.ds.cityChat.add(player.getUniqueId());
					player.sendMessage(Messages.CityChatOn.get());
				}
				return true;
			}
			
			City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
			if (city==null) {
				player.sendMessage(Messages.YouNotCitizen.get());
				return false;
			}
			String message=DataStore.mergeStringArrayFromIndex(args, 0);
			String cMessage=Messages.CityChatFormat.get(city.name, player.getDisplayName(), message);
			city.sendMessageToAllCitizens(cMessage);
			GPPCities.gppc.ds.cityChatSpy(cMessage);
			GPPCities.gppc.log("CC["+city.name+"] <"+player.getName()+"> "+message);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("cityadmin")) {
			if (args.length==0) {
				player.sendMessage("Admin commands:\n- spy (on|off) - CityChat Social Spy");
				return false;
			}
			
			if (args[0].equalsIgnoreCase("spy")) {
				if (args.length==1) {
					player.sendMessage("Usage: /cityadmin spy (on|off)");
					return false;
				}
				
				if (args[1].equalsIgnoreCase("on")) {
					GPPCities.gppc.ds.cityChatSpy.add(player.getUniqueId());
					player.sendMessage("City Social Spy On");
					return true;
				} else if (args[1].equalsIgnoreCase("off")) {
					GPPCities.gppc.ds.cityChatSpy.remove(player.getUniqueId());
					player.sendMessage("City Social Spy Off");
					return true;
				}
			}
		}
		
		player.sendMessage(Messages.WrongCommand.get());
		return false;
	}
}
