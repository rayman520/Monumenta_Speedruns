package mmsr.main;

import java.io.File;
import java.io.FileNotFoundException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mmsr.utils.FileUtils;
import mmsr.utils.Utils;

public class LeadView implements CommandExecutor
{
	Main plugin;
	
	LeadView(Main pl)
	{
		plugin = pl;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		//verify args count
		if (args.length != 2)
		{
			sender.sendMessage("invalid parameter count.\nUsage: /leadview <racefile> <starting rank>\nExemple: '/speedrun view race01 1'");
			return true;
		}
		Entity send = Utils.calleeEntity(sender);
		//get leaderboard file content to a line by line array
		String file =  plugin.getDataFolder().toString() + "/speedruns" + File.separator + "leaderboards" + File.separator + args[0].toLowerCase() + ".leaderboard";
		String content;
		try {
			content = FileUtils.readFile(file);
		} catch (FileNotFoundException e) {
			send.sendMessage("File not found: " + file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		String[] lines = content.split("\n");
		int startLine = Integer.parseInt(args[1]) - 1;
		startLine = ((int)(startLine / 10)) * 10;
		// print header
		send.sendMessage("" + ChatColor.DARK_AQUA + ChatColor.BOLD +   "----====----       "+ ChatColor.AQUA + ChatColor.BOLD +"Speedruns" + ChatColor.DARK_AQUA  + ChatColor.BOLD + "       ----====----\n");
		send.sendMessage(" " +  String.format("%s - %s", "" + ChatColor.AQUA + "Leaderboard", "" + ChatColor.YELLOW + args[0].toLowerCase().substring(args[0].lastIndexOf("/") + 1)) + "          ");
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
			color = Utils.getMedalColor(plugin, send, time_ms, args[0]);
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
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadview "+args[0]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == pageCount)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadview "+args[0]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)send).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadview "+args[0]+" "+(startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadview "+args[0]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			send.sendMessage(" ");
		}
		return true;
	}
}
