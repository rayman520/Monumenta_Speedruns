package mmsr.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import mmsr.utils.Leaderboard;
import mmsr.utils.Utils;

public class Speedrun implements CommandExecutor
{
	Main plugin;
	
	Speedrun(Main pl)
	{
		plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender send, Command command, String label, String[] args) {
		//verify args count
		if (args.length == 0)
		{
			send.sendMessage("invalid parameter count.");
			return false;
		}
		String subcommand = args[0].toLowerCase();
		Leaderboard leaderboard = new Leaderboard(plugin);
		Race	 race = new Race(plugin);
		switch (subcommand)
		{
			case	 "leaderboard":
				leaderboard.leaderboard(send, args);
				break;
			case "start":
				race.start(send, args);
				break;
			case "cancel":
				race.cancel(send);
				break;
			case "setreward":
				Utils.setRewardFile(plugin, Utils.calleeEntity(send), args);
				break;
			case "viewreward":
				Utils.viewRewardFile(send, plugin, Utils.calleeEntity(send), args);
				break;
			default:
				send.sendMessage(" Unknown '" + args[0] + "' subcommand given.\n" +
								" Available subcommands:\n" +
								" -leaderboard\n"+
								" -start\n"+
								" -cancel\n"+
								" -setreward\n"+
								" -viewreward\n");
		}
		return true;
	}
}
