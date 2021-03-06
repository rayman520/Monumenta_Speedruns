package mmsr.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;

public class Leaderboard
{
	Plugin plugin;
	
	public Leaderboard(Plugin pl) {
		plugin = pl;
	}

	public void leaderboard(CommandSender send, String[] args)
	{
		//verify args count
		if (args.length < 2)
		{
			send.sendMessage("invalid parameter count.\nUsage: /speedrun leaderboard <sub-command>");
			return;
		}
		String subcommand = args[1].toLowerCase();
		switch (subcommand)
		{
			case	 "view":
				leaderboard_view(send, args);
				break;
			case "add":
				leaderboard_add(send, args);
				break;
			case "remove":
				leaderboard_remove(send, args);
				break;
			case "generate":
				leaderboard_generate(send, args);
				break;
			default:
				send.sendMessage(" Unknown '" + args[1] + "' subcommand given.\n" +
								" Available subcommands:\n" +
								" -view\n" +
								" -add\n" + 
								" -remove" +
								" -generate");
		}
	}
	
	private void leaderboard_generate(CommandSender sender, String[] args)
	{
		if (args.length != 5)
		{
			sender.sendMessage("invalid parameter count.\nUsage: /speedrun leaderboard generate <scoreboard_name> <keep_order> <startpoint> \nExemple: '/speedrun generate <RLcompletion> true 1'");
			return;
		}
		Objective obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(args[2]);
		List<String> nameList = new ArrayList<String>();
		List<Integer> scoreList = new ArrayList<Integer>();
		int score;
		for(String name : obj.getScoreboard().getEntries())
		{
			score = obj.getScore(name).getScore();
			for (int i = 0; i <= scoreList.size(); i++)
			{
				if (i == scoreList.size() || score > scoreList.get(i))
				{
					scoreList.add(i, score);
					nameList.add(i, name);
					break;
				}
			}
		}
		
		if (args[3].equalsIgnoreCase("false"))
		{
			List<String> nameTmp = new ArrayList<String>();
			List<Integer> scoreTmp = new ArrayList<Integer>();
			for (int i = nameList.size() - 1; i >= 0; i--)
			{
				nameTmp.add(nameList.get(i));
				scoreTmp.add(scoreList.get(i));
			}
			nameList = nameTmp;
			scoreList = scoreTmp;
		}
		
		int startLine = Integer.parseInt(args[4]) - 1;
		startLine = ((int)(startLine / 10)) * 10;
		// print header
		sender.sendMessage(" " +  String.format("%s - %s", "" + ChatColor.AQUA + "Leaderboard", "" + ChatColor.YELLOW + args[2].toLowerCase()) + "          ");
		sender.sendMessage(" ");
		// print leaderboard itself
		sender.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " Rank  |        Name      |    Score");
		String[] currentLine;
		String name;
		int time_ms;
		String color;
		int index;
		for (int i = 0; i < 10; i++)
		{
			index = i + startLine;
			if ((index) == nameList.size())
				break;
			
			switch (index)
			{
			case (0):
				color = "" + ChatColor.GOLD + ChatColor.BOLD;
				break;
			case (1):
				color = "" + ChatColor.WHITE + ChatColor.BOLD;
				break;
			case (2):
				color = "" + ChatColor.DARK_RED + ChatColor.BOLD;
				break;
			default:
				color = "" + ChatColor.GRAY;
			}
			
			String line = String.format("%s%s - %-15s -    %s", color, index + 1, nameList.get(index), scoreList.get(index));
			sender.sendMessage(line);
		}
		sender.sendMessage(" ");
		Entity send = Utils.calleeEntity(sender);
		if (send instanceof Player)
		{
			int pageCount = (nameList.size() - 1) / 10;
			int actualPage = startLine / 10;
			if (pageCount == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == pageCount)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview"+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			send.sendMessage(" ");
		}
		/*
		//print footer
		if (send instanceof Player)
		{
			int pageCount = (lines.length - 1) / 10;
			int actualPage = startLine / 10;
			if (pageCount == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == pageCount)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview"+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			send.sendMessage(" ");
		}
		*/
	}
	
	public void leaderboard_remove(CommandSender send, String[] args)
	{
		//verify args count
		if (args.length != 4)
		{
			send.sendMessage("invalid parameter count.\nUsage: /speedrun leaderboard remove <racefile> <playername>\nExemple: '/speedrun remove r1/tutorial+ rayman520'");
			return;
		}
		
		//leaderboard
		String file = plugin.getDataFolder().toString() +  "/speedruns" + File.separator + "leaderboards" + File.separator + args[2].toLowerCase() + ".leaderboard";
		String content;
		try {
			content = FileUtils.readFile(file);
		} catch (FileNotFoundException e) {
			send.sendMessage("File not found: " + file);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String playerName = args[3];
		List<String> lines = new ArrayList<String>(Arrays.asList(content.split("\n")));
		String splitedLine[];
		int i = 0;
		for (String str : lines)
		{

			splitedLine = str.split(" ");
			//find the line corresponding to the player
			if (splitedLine[0].equals(playerName))
			{
				lines.remove(i);
				send.sendMessage("entry removed from leaderboard");
				break;
			}
			i++;
		}
		if (i == lines.size())
			send.sendMessage("no leaderboard entry found for "+playerName);
		//rewrite file
		Path path = Paths.get(file);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
			Files.write(path, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//recordedrings
		path = Paths.get( plugin.getDataFolder().toString() +  "/speedruns" + File.separator + "playerdata/recorded_ring_times" + File.separator + args[2].toLowerCase() + File.separator + playerName + ".recorded");
		try {
			Files.deleteIfExists(path);
			send.sendMessage("ringFile removed for "+playerName);
		} catch (IOException e) {
			send.sendMessage("could not remove recorded rings file." + e.getStackTrace());
		}
	}
	
	public void leaderboard_add(CommandSender sender, String[] args)
	{
		//verify args count
		if (args.length != 4)
		{
			sender.sendMessage("invalid parameter count.\nUsage: /speedrun leaderboard add <racefile> <Time(ms)>\nExemple: '/speedrun add race01 3600000'");
			return;
		}
		Entity send = Utils.calleeEntity(sender);
		//get leaderboard file content to a line by line array
		String file = plugin.getDataFolder().toString() +  "/speedruns" + File.separator + "leaderboards" + File.separator + args[2].toLowerCase() + ".leaderboard";
		String content;
		try {
			content = FileUtils.readFile(file);
		} catch (FileNotFoundException e) {
			send.sendMessage("File not found: " + file);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		List<String> lines = new ArrayList<String>(Arrays.asList(content.split("\n")));
		// get already stored time
		int oldTime;
		int newTime = Integer.parseInt(args[3]);
		String playerName = Utils.calleeEntity(send).getName();
		String[] splitedLine;
		//parse the file
		for (String str : lines)
		{
			splitedLine = str.split(" ");
			//find the line corresponding to the player
			if (splitedLine[0].equals(playerName))
			{
				oldTime = Integer.parseInt(splitedLine[1]);
				// if the new entry time is better than the old one, remove the line with the old time, otherwise end the whole function
				if (newTime < oldTime)
				{
					lines.remove(lines.indexOf(str));
					break;
				}
				else
					return;
			}
		}
		//if this point is reached, the player got a better time
		//finding the time that just got beated
		for (String str : lines)
		{
			splitedLine = str.split(" ");
			//if this time is found, add a new entry just before, play a win sound, then exit the function
			if (Integer.parseInt(splitedLine[1]) > newTime)
			{
				lines.add(lines.indexOf(str), playerName + " " + newTime);
				send.sendMessage("");
				send.sendMessage("        " + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + "New Personal Best!");
				String medalColor = Utils.getMedalColor(plugin, send, newTime, args[2]);
				send.sendMessage("  " + ChatColor.BLUE + "New Time: " + medalColor + Utils.msToTimeString(newTime) + ChatColor.BLUE + "     New Position: " + medalColor + lines.indexOf(str) + ChatColor.GRAY + ChatColor.ITALIC + "/" + lines.size());
				send.sendMessage("");
				if (lines.indexOf(str) == 1)
				{
					//world record
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [\"\",{\"text\":\""+playerName+"\",\"color\":\"gold\"},{\"text\":\" has beaten \",\"color\":\"white\",\"bold\":true},{\"text\":\""+args[2].toLowerCase().substring(args[2].lastIndexOf("/") + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\" old World Record.\",\"color\":\"white\",\"bold\":true}]");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title "+playerName+" title [\"\",{\"text\":\"New World Record\",\"color\":\"blue\",\"bold\":true}]");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title "+playerName+" subtitle [\"\",{\"text\":\""+Utils.msToTimeString(newTime)+"\",\"color\":\"aqua\",\"bold\":true}]");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute "+playerName+" ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:10,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:2,Flicker:1b,Colors:[I;1017855,4040191,857599,2424780]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute "+playerName+" ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:15,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:2,Flicker:1b,Colors:[I;1017855,4040191,857599,2424780]}]}}}}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute "+playerName+" ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:20,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:2,Flicker:1b,Colors:[I;1017855,4040191,857599,2424780]}]}}}}");
					Location loc = ((Entity) send).getLocation();
					loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2, 1.2f);
				}
				break;
			}
		}
		//rewrite the whole file
		Path path = Paths.get(file);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
			Files.write(path, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void leaderboard_view(CommandSender sender, String[] args)
	{
		//verify args count
		if (args.length != 4)
		{
			sender.sendMessage("invalid parameter count.\nUsage: /speedrun leaderboard view <racefile> <starting rank>\nExemple: '/speedrun view race01 1'");
			return;
		}
		Entity send = Utils.calleeEntity(sender);
		//get leaderboard file content to a line by line array
		String file =  plugin.getDataFolder().toString() + "/speedruns" + File.separator + "leaderboards" + File.separator + args[2].toLowerCase() + ".leaderboard";
		String content;
		try {
			content = FileUtils.readFile(file);
		} catch (FileNotFoundException e) {
			send.sendMessage("File not found: " + file);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String[] lines = content.split("\n");
		int startLine = Integer.parseInt(args[3]) - 1;
		startLine = ((int)(startLine / 10)) * 10;
		// print header
		send.sendMessage("" + ChatColor.DARK_AQUA + ChatColor.BOLD +   "----====----       "+ ChatColor.AQUA + ChatColor.BOLD +"Speedruns" + ChatColor.DARK_AQUA  + ChatColor.BOLD + "       ----====----\n");
		send.sendMessage(" " +  String.format("%s - %s", "" + ChatColor.AQUA + "Leaderboard", "" + ChatColor.YELLOW + args[2].toLowerCase().substring(args[2].lastIndexOf("/") + 1)) + "          ");
		send.sendMessage(" ");
		// print leaderboard itself
		send.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " Rank  |        Time      |    Player Name");
		String[] currentLine;
		String name;
		int time_ms;
		String color;
		for (int i = 0; i < 10; i++)
		{
			if ((i + startLine) == lines.length)
				break;
			currentLine = lines[i + startLine].split(" ");
			name = currentLine[0];
			time_ms = Integer.parseInt(currentLine[1]);
			color = Utils.getMedalColor(plugin, send, time_ms, args[2]);
			if (startLine + i == 0 && color.equalsIgnoreCase("" + ChatColor.GREEN + ChatColor.ITALIC + ChatColor.BOLD))
				color = "" + ChatColor.AQUA + ChatColor.ITALIC + ChatColor.BOLD;
			send.sendMessage(String.format("%10s - %18s -    %s", "  " + ChatColor.BOLD + (i + startLine + 1) + ChatColor.RESET + "  ", String.format("%s%s", color, Utils.msToTimeString(time_ms)), name));
		}
		send.sendMessage(" ");
		//print footer
		if (send instanceof Player)
		{
			int pageCount = (lines.length - 1) / 10;
			int actualPage = startLine / 10;
			if (pageCount == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == pageCount)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview"+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"leadview "+args[2]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			send.sendMessage(" ");
		}
	}
}
