package mmsr.main;

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
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import mmsr.utils.EvtHandler;
import mmsr.utils.FileUtils;
import mmsr.utils.Point;
import mmsr.utils.Rewarding;
import mmsr.utils.TimeBar;
import mmsr.utils.Utils;

public class Race
{
	Main plugin;
	Random rand = new Random();
	
	public Race(Main pl) {
		plugin = pl;
	}
	
	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	
	int loop_task_id;
	int anim_task_id;
	int fast_task_id = 0;
	
	String baseFileName;
	
	List<Point> ringShape = new ArrayList<Point>();
	List<ArmorStand> ringEntities = new ArrayList<ArmorStand>();
	List<Location> ringLocs = new ArrayList<Location>();
	boolean has_ring_times = true;
	List<Integer> ringTimes = new ArrayList<Integer>();
	List<Integer> possibleRingTimes = new ArrayList<Integer>();
	
	List<Integer> medTimes = new ArrayList<Integer>();
	
	List<String> rewardMaster = new ArrayList<String>();
	List<String> rewardGold = new ArrayList<String>();
	List<String> rewardSilver = new ArrayList<String>();
	List<String> rewardBronze = new ArrayList<String>();
	List<String> rewardComplete = new ArrayList<String>();
	List<List<String>> rewards = new ArrayList<List<String>>();
	List<String> rewardLose = new ArrayList<String>();
	
	List<String> raceScripts = new ArrayList<String>();
	
	boolean no_ui = false;
	
	Location oldLoc;
	Location startLoc;
	World w;
	
	Long startTime;
	int displayTime;
	int actualRing = 0;
	
	TimeBar timeBar;
	
	int frame = 0;
	
	Entity runner;
	
	public void start(CommandSender sender, String[] args)
	{
		
		//verify args count
		if (args.length != 2)
		{
			sender.sendMessage("invalid parameter count.\nUsage: /speedrun start <Race_file>");
			return;
		}
		
		
		//verify if runner is already in a race
		runner = Utils.calleeEntity(sender);
		Set<String>	tags = runner.getScoreboardTags();
		for (String tag : tags)
		{
			if (tag.equals("is_racing") || tag.equals("is_racing_no_ui"))
			{
				runner.sendMessage("You cannot start multiple races at the same time");
				return;
			}
		}
		
		// parse racefile
		baseFileName = args[1];
		parseFiles(runner, args[1]);
		
		//init race
		oldLoc = runner.getLocation();
		startLoc.add(0.5,0,0.5);
		runner.teleport(startLoc);
		if (!no_ui)
			runner.addScoreboardTag("is_racing");
		else
			runner.addScoreboardTag("is_racing_no_ui");
		timeBar = new TimeBar(runner);
		w = startLoc.getWorld();
		
		// init basic ring shape
		Location baseLoc = new Location(runner.getWorld(), runner.getLocation().getX(), runner.getLocation().getY()-10, runner.getLocation().getZ());
		for (int angle = 0; angle < 18; angle++)
		{
			ringShape.add(new Point(Math.cos(Math.toRadians(angle * 20)), Math.sin(Math.toRadians(angle * 20)), 0));
			ArmorStand out = (ArmorStand)runner.getWorld().spawnEntity(baseLoc, EntityType.ARMOR_STAND);
			out.setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
			out.setGlowing(true);
			out.setGravity(false);
			out.setInvulnerable(true);
			out.setVisible(false);
			out.setSmall(true);
			out.setMarker(true);
			ringEntities.add(out);
		}
		
		executeScripts();
		// race countdown functions
		Utils.countdown(runner, w, this.plugin, startLoc, no_ui);
		
		// near ring fast loop
		Runnable fastLoop = new Runnable()
		{
			@Override
			public void run()
			{
				Location calcLoc = runner.getLocation();
				calcLoc.add(0, 1, 0);
				if (calcLoc.distance(ringLocs.get(actualRing)) < 4)
				{
					ringPass();
				}
			}
		};
		
		
		// game loop
		Runnable loop = new Runnable()
		{
			@Override
			public void run()
			{
				// initialize start time
				if (startTime == null)
					startTime = System.currentTimeMillis();
				//	tests for ring errors
				
				if (actualRing == ringLocs.size() || !(runner.isValid()))
				{
					endRace();
					System.out.println("Speedruns: an error occured, if it keeps happening, please contact a mod");
					return ;
				}
				// check for player running away
				
				if (runner.getLocation().distance(ringLocs.get(actualRing)) > 100)
				{
					runner.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "You went too far away from the race path.");
					runner.addScoreboardTag("race_lose");
				}
				
				//time remaining display
				int time = (int)(System.currentTimeMillis() - startTime);
				if (time > medTimes.get(4))
				{
					runner.addScoreboardTag("Ran out of time");
					runner.addScoreboardTag("race_lose");
				}
				timeBar.update(runner, (int)(System.currentTimeMillis() - startTime), medTimes);
				
				
				// parse all player tags
				Set<String>	tags = runner.getScoreboardTags();
				for (String tag : tags)
				{
					if (tag.equals("race_cancel")) //if player called a cancel
					{
						runner.removeScoreboardTag("race_cancel");
						endRace();
						return;
					}
					if (tag.equals("race_lose"))
					{
						runner.removeScoreboardTag("race_lose");
						endRace();
						runner.teleport(oldLoc);
						for (String s : rewardLose)
							Bukkit.dispatchCommand(runner, s);
					}
					if (tag.equals("race_retry"))
					{
						runner.removeScoreboardTag("race_retry");
						endRace();
						runner.teleport(oldLoc);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ speedrun start " + baseFileName);
					}
				}
				if (fast_task_id == 0 && runner.getLocation().distance(ringLocs.get(actualRing)) < 10)
					fast_task_id = scheduler.scheduleSyncRepeatingTask(plugin, fastLoop, 0L, 1L);
				else
				{
					scheduler.cancelTask(fast_task_id);
					fast_task_id = 0;
				}
			}
		};
		
		
		//animation loop
		Runnable anim = new Runnable()
		{
			@Override
			public void run()
			{
				Location ploc = runner.getLocation();
				Location rloc = ringLocs.get(actualRing);
				Vector mainDir = (new Vector(ploc.getX() - rloc.getX(), ploc.getY() - rloc.getY(), ploc.getZ() - rloc.getZ())).normalize().multiply(4.0);
				rloc.setDirection(mainDir);
				int i = 0;
				frame++;
				for (Location particleLoc : Utils.transformPoints(rloc, ringShape, rloc.getYaw(), -rloc.getPitch(), 0d, 4 + 0.5*(Math.cos(Math.toRadians(frame * 20)))))
				{
					particleLoc.setY(particleLoc.getY() - 0.5);
					ringEntities.get(i).teleport(particleLoc);
					i++;
				}
				
				
			}
		};
				
		loop_task_id = scheduler.scheduleSyncRepeatingTask(plugin, loop, 60L, 5L);
		anim_task_id = scheduler.scheduleSyncRepeatingTask(plugin, anim, 1L, 2L);
		
		// setup events
		new EvtHandler(plugin, ringEntities, runner, no_ui);
	}
	
	public void ringPass()
	{
		Long ts = System.currentTimeMillis();
		displayTime = (int)(ts - startTime);
		possibleRingTimes.add(displayTime);
		if (has_ring_times)
		{
			int oldtime = ringTimes.get(actualRing);
			if (oldtime < displayTime)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ title @s actionbar [\"\",{\"text\":\""+Utils.msToTimeString(displayTime)+"\",\"color\":\"blue\",\"bold\":true},{\"text\":\"  ( + "+Utils.msToTimeString(displayTime - oldtime)+" )\",\"color\":\"red\",\"bold\":true}]");
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ title @s actionbar [\"\",{\"text\":\""+Utils.msToTimeString(displayTime)+"\",\"color\":\"blue\",\"bold\":true},{\"text\":\"  ( - "+Utils.msToTimeString(oldtime - displayTime)+" )\",\"color\":\"green\",\"bold\":true}]");
			
		}
		else
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ title @s actionbar [\"\",{\"text\":\""+Utils.msToTimeString(displayTime)+"\",\"color\":\"blue\",\"bold\":true}]" + Long.toString(ts - startTime));

		startLoc.getWorld().playSound(runner.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1.5f);
		actualRing++;
		if (actualRing == ringLocs.size())
			end(displayTime);
		scheduler.cancelTask(fast_task_id);
		fast_task_id = 0;
		// apply scripts
		executeScripts();
	}
	
	public void executeScripts()
	{
		List<String> toberm = new ArrayList<String>();
		
		for (String line : raceScripts)
		{
			String[] splitline = line.split(" ");
			int scriptID = Integer.parseInt(splitline[0]);
			if (scriptID == actualRing)
			{
				String cmd = Arrays.stream(splitline).skip(1).collect(Collectors.joining(" "));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
				toberm.add(line);
			}
		}
		for (String line : toberm)
		{
			raceScripts.remove(line);
		}
	}
	
	public void endRace()
	{
		scheduler.cancelTask(loop_task_id);
		scheduler.cancelTask(anim_task_id);
		runner.removeScoreboardTag("is_racing");
		runner.removeScoreboardTag("is_racing_no_ui");
		if (fast_task_id != 0)
			scheduler.cancelTask(fast_task_id);
		for (Entity e : ringEntities)
			e.remove();
		timeBar.bar.setVisible(false);
	}
	
	public void end(int endTime)
	{
		endRace();
		
		if (!no_ui)
		{
			int pb = (has_ring_times ? ringTimes.get(ringTimes.size() - 1) : possibleRingTimes.get(possibleRingTimes.size() - 1));
			if (!has_ring_times || endTime < pb) // if time beated
			{
				pb = endTime;
				//rewrite recoded ringtimes
				Path path = Paths.get( plugin.getDataFolder().toString() +  "/speedruns" + File.separator + "playerdata/recorded_ring_times" + File.separator + baseFileName.toLowerCase() + File.separator + runner.getName() + ".recorded");
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					List<String> newList = new ArrayList<String>(possibleRingTimes.size());
					for (Integer myInt : possibleRingTimes)
						newList.add(String.valueOf(myInt));
					Files.createDirectories(path.getParent());
					Files.createFile(path);
					Files.write(path, newList, Charset.forName("UTF-8"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// update leaderboards
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ speedrun leaderboard add " + baseFileName + " " + endTime);
			}
				// display race end info
				//header
				runner.sendMessage("" + ChatColor.DARK_AQUA + ChatColor.BOLD +   "----====----       "+ ChatColor.AQUA + ChatColor.BOLD +"Speedruns" + ChatColor.DARK_AQUA  + ChatColor.BOLD + "       ----====----\n");
				runner.sendMessage(" " +  String.format("%s - %s", "" + ChatColor.AQUA + "Race Recap", "" + ChatColor.YELLOW + baseFileName.toLowerCase().substring(baseFileName.lastIndexOf("/") + 1)) + "          ");
				runner.sendMessage(" ");
				// body
				{
					String content = null;
					try {
						content = FileUtils.readFile( plugin.getDataFolder().toString() +  "/speedruns" + File.separator + "leaderboards" + File.separator + baseFileName.toLowerCase() + ".leaderboard");
					} catch (FileNotFoundException e) {
						runner.sendMessage("No world record file found " + baseFileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					int wrTime = Integer.parseInt(content.split("\n")[0].split(" ")[1]);
					//int[] medTimes = Utils.getMedalTimes((CommandSender)runner, baseFileName);
					String mColor = Utils.getMedalColor(plugin, (CommandSender)runner, endTime, baseFileName);				
					runner.sendMessage(String.format("  %sWorld Record - %16s  | %s %s", 
							"" + ChatColor.AQUA + ChatColor.BOLD,
							"" + Utils.msToTimeString(wrTime),
							"" + ((endTime <= wrTime) ? ("" + ChatColor.AQUA + ChatColor.BOLD + "\u272A") : ("" + ChatColor.GRAY + ChatColor.BOLD + "\u272A")),
							"" + ((endTime <= wrTime) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(wrTime - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - wrTime) + ")"))));
					
					runner.sendMessage(String.format("  %s   Master      - %16s  | %s %s",
							"" + ChatColor.GREEN + ChatColor.BOLD,
							"" + Utils.msToTimeString(medTimes.get(0)),
							"" + ((endTime <= medTimes.get(0)) ? ("" + ChatColor.GREEN + ChatColor.BOLD + "\u272A") : ("" + ChatColor.GRAY + ChatColor.BOLD + "\u272A")),
							"" + ((endTime <= medTimes.get(0)) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(medTimes.get(0) - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - medTimes.get(0)) + ")"))));
					
					runner.sendMessage(String.format("  %s    Gold        - %16s  | %s %s",
							"" + ChatColor.GOLD + ChatColor.BOLD,
							"" + Utils.msToTimeString(medTimes.get(1)),
							"" + ((endTime <= medTimes.get(1)) ? ("" + ChatColor.GOLD + ChatColor.BOLD + "\u272A") : ("" + ChatColor.GRAY + ChatColor.BOLD + "\u272A")),
							"" + ((endTime <= medTimes.get(1)) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(medTimes.get(1) - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - medTimes.get(1)) + ")"))));
					
					runner.sendMessage(String.format("  %s   Silver       - %16s  | %s %s",
							"" + ChatColor.WHITE + ChatColor.BOLD,
							"" + Utils.msToTimeString(medTimes.get(2)),
							"" + ((endTime <= medTimes.get(2)) ? ("" + ChatColor.WHITE + ChatColor.BOLD + "\u272A") : ("" + ChatColor.GRAY + ChatColor.BOLD + "\u272A")),
							"" + ((endTime <= medTimes.get(2)) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(medTimes.get(2) - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - medTimes.get(2)) + ")"))));
					
					runner.sendMessage(String.format("  %s   Bronze     - %16s  | %s %s",
							"" + ChatColor.DARK_RED + ChatColor.BOLD,
							"" + Utils.msToTimeString(medTimes.get(3)),
							"" + ((endTime <= medTimes.get(3)) ? ("" + ChatColor.DARK_RED + ChatColor.BOLD + "\u272A") : ("" + ChatColor.GRAY + ChatColor.BOLD + "\u272A")),
							"" + ((endTime <= medTimes.get(3)) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(medTimes.get(3) - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - medTimes.get(3)) + ")"))));
					runner.sendMessage(" ");
					runner.sendMessage(String.format("  %s Personal Best - %16s  | %s",
							"" + ChatColor.BLUE + ChatColor.BOLD,
							"" + Utils.msToTimeString(pb),
							"" + ((endTime <= pb) ? ("" + ChatColor.BLUE + ChatColor.BOLD + "( -" + Utils.msToTimeString(pb - endTime) + ")") : ("" + ChatColor.RED + ChatColor.BOLD + "( +" + Utils.msToTimeString(endTime - pb) + ")"))));

					runner.sendMessage(String.format("  %s  Your Time   - %16s",
							"" + ChatColor.BLUE + ChatColor.BOLD,
							"" + mColor + Utils.msToTimeString(endTime)));
				}
		}

			rewards.add(rewardMaster);
			rewards.add(rewardGold);
			rewards.add(rewardSilver);
			rewards.add(rewardBronze);
			rewards.add(rewardComplete);
			Rewarding.medalRewards(plugin, runner, baseFileName, rewards, medTimes, endTime);
	}
	
	public void parseFiles(Entity send, String str)
	{
		Location w = send.getLocation();
		
		// RACEFILE
		
		String file = plugin.getDataFolder().toString() + "/speedruns" +File.separator + "racefiles" + File.separator + str.toLowerCase() + ".racefile";
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
		for (String line : lines)
		{
			String[] splitedLine = line.split(" ");
			switch (splitedLine[0])
			{
				case "ring":
					ringLocs.add(Utils.getLocation(w, splitedLine[1], splitedLine[2], splitedLine[3]));
					break;
				case "start":
					startLoc = Utils.getLocation(w, splitedLine[1], splitedLine[2], splitedLine[3]);
					startLoc.setYaw(Float.parseFloat(splitedLine[4]));
					startLoc.setPitch(Float.parseFloat(splitedLine[5]));
					break;
				case "script":
					raceScripts.add(line.substring(7));
					break;
				case "reward_master":
					rewardMaster.add(line.substring(14));
					break;
				case "reward_gold":
					rewardGold.add(line.substring(12));
					break;
				case "reward_silver":
					rewardSilver.add(line.substring(14));
					break;
				case "reward_bronze":
					rewardBronze.add(line.substring(14));
					break;
				case "reward_complete":
					rewardComplete.add(line.substring(16));
					break;
				case "reward_lose":
					rewardLose.add(line.substring(12));
					break;
				case "times":
					for (int i = 1; i < 6; i++)
						medTimes.add(Integer.parseInt(splitedLine[i]));
					break;
				case "no-ui":
					no_ui = true;
					
			}
		}
		
		// RECORDED TIMES
		
		file = plugin.getDataFolder().toString() + "/speedruns" +File.separator + "playerdata/recorded_ring_times" + File.separator + str.toLowerCase() + File.separator + runner.getName() + ".recorded";
		try {
			content = FileUtils.readFile(file);
		} catch (FileNotFoundException e) {
			if (!no_ui)
				send.sendMessage("No ringfile found for this race, you need to finish it at least once.");
			has_ring_times = false;
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		lines = new ArrayList<String>(Arrays.asList(content.split("\n")));
		for (String line : lines)
			ringTimes.add(Integer.parseInt(line));
		
	}
	
	public void cancel(CommandSender send)
	{
		(Utils.calleeEntity(send)).addScoreboardTag("race_cancel");
	}
}
