package mmsr.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import mmsr.utils.FileUtils;
import mmsr.utils.Utils;

public class LeadGen implements CommandExecutor
{
	Main plugin;
	
	LeadGen(Main pl)
	{
		plugin = pl;
	}
	
	public boolean onCommand(CommandSender send, Command command, String label, String[] args)
	{
		if (args.length != 3)
		{
			send.sendMessage("invalid parameter count.\nUsage: /leadgen <scoreboard_name> <keep_order> <startpoint> \nExemple: '/leadgen RLcompletion true 1'");
			return (true);
		}
		Entity sender = Utils.calleeEntity(send);
		Objective obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(args[0]);
		List<String> nameList = new ArrayList<String>();
		List<Integer> scoreList = new ArrayList<Integer>();
		int score;
		for(String name : obj.getScoreboard().getEntries())
		{
			score = obj.getScore(name).getScore();
			if (score != 0)
			{
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
		}
		
		if (args[1].equalsIgnoreCase("false"))
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
		
		int startLine = Integer.parseInt(args[2]) - 1;
		startLine = ((int)(startLine / 10)) * 10;
		// print header
		sender.sendMessage(" " +  String.format("%s - %s", "" + ChatColor.AQUA + "Leaderboard", "" + ChatColor.YELLOW + args[0].toLowerCase()) + "          ");
		sender.sendMessage(" ");
		// print leaderboard itself
		sender.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " Rank  |        Name      |    Score");
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
				color = "" + ChatColor.GRAY + ChatColor.BOLD;
			}
			if (nameList.get(index) == sender.getName())
				color = "" + ChatColor.BLUE + ChatColor.BOLD;
			String line = String.format("%s%-3s - %-15s -    %s", color, index + 1, nameList.get(index), scoreList.get(index));
			sender.sendMessage(line);
		}
		sender.sendMessage(" ");
		index = nameList.lastIndexOf(sender.getName());
		sender.sendMessage(String.format("%s%-3s - %-15s -    %s", "" + ChatColor.BLUE + ChatColor.BOLD, index + 1, nameList.get(index), scoreList.get(index)));
		if (sender instanceof Player)
		{
			int pageCount = (nameList.size() - 1) / 10;
			int actualPage = startLine / 10;
			if (pageCount == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)sender).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)sender).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"gray\",\"bold\":false},{\"text\":\"  Page:  \",\"color\":\"yellow\"},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadgen "+args[0]+" "+args[1]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else if (actualPage == pageCount)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)sender).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadgen "+args[0]+" "+args[1]+" "+ (startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"gray\",\"bold\":false},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+((Player)sender).getName()+" [\"\",{\"text\":\"--==-- \",\"color\":\"blue\",\"bold\":true},{\"text\":\"[ < ] \",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadgen "+args[0]+" "+args[1]+" "+ (startLine)+"\"}},{\"text\":\"  Page:  \",\"color\":\"yellow\",\"bold\":false},{\"text\":\""+String.format("%4d/%-4d", actualPage + 1, pageCount + 1)+"\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"   [ > ]\",\"color\":\"light_purple\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leadgen "+args[0]+" "+args[1]+" "+(startLine + 11)+"\"}},{\"text\":\" --==--\",\"color\":\"blue\",\"bold\":true}]");
			sender.sendMessage(" ");
		}
		return true;
	}
}
