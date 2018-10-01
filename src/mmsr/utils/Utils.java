package mmsr.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Utils
{
	public static void countdown(Entity runner, World w, Plugin plugin, Location ploc, boolean no_ui)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		Location savedLoc = runner.getLocation();
		Runnable tp = new Runnable()
		{
			public void run()
			{
				Location newLoc = new Location(runner.getWorld(), savedLoc.getX(), savedLoc.getY(), savedLoc.getZ());
				newLoc.setDirection(runner.getLocation().getDirection());
				runner.teleport(newLoc);
			}
		};
		Runnable cd3 = new Runnable()
		{
			public void run()
			{
				if (!no_ui)
					runner.sendMessage("" + ChatColor.BLUE + "Reminder:\nShift + Left-Click: Abandon\nShift + Right-Click: Retry");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ fill ~-1 ~1 ~-1 ~1 ~2 ~1 glass 0 replace air");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ effect @s minecraft:slowness 3 30");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ effect @s minecraft:jump_boost 3 129");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s times 0 20 0");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\"3\",\"color\":\"red\",\"bold\":true}]");
				w.playSound(runner.getLocation(), Sound.BLOCK_NOTE_BELL, 1, 0.890899f);
			}
		};
		Runnable cd2 = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\"2\",\"color\":\"gold\",\"bold\":true}]");
				w.playSound(runner.getLocation(), Sound.BLOCK_NOTE_BELL, 1, 0.890899f);
			}
		};
		Runnable cd1 = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\"1\",\"color\":\"green\",\"bold\":true}]");
				w.playSound(runner.getLocation(), Sound.BLOCK_NOTE_BELL, 1, 0.890899f);
			}
		};
		int tasktp = scheduler.scheduleSyncRepeatingTask(plugin, tp, 0L, 5L);
		scheduler.scheduleSyncDelayedTask(plugin, cd3, 0L);
		scheduler.scheduleSyncDelayedTask(plugin, cd2, 20L);
		scheduler.scheduleSyncDelayedTask(plugin, cd1, 40L);
		Runnable cdgosound = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ fill ~-1 ~ ~-1 ~1 ~2 ~1 air 0 replace glass");
				w.playSound(runner.getLocation(), Sound.BLOCK_NOTE_BELL, 1,  1.781797f);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s times 0 4 0");
				scheduler.cancelTask(tasktp);
			}
		};
		scheduler.scheduleSyncDelayedTask(plugin, cdgosound, 60L);
		Runnable cdgo1 = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\"GO\",\"color\":\"white\",\"bold\":true}]");
			}
		};
		Runnable cdgo2 = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\"GO\",\"color\":\"aqua\",\"bold\":true}]");
			}
		};
		int taskcd1 = scheduler.scheduleSyncRepeatingTask(plugin, cdgo1, 60L, 6L);
		int taskcd2 = scheduler.scheduleSyncRepeatingTask(plugin, cdgo2, 63L, 6L);
		Runnable cdreset = new Runnable()
		{
			public void run()
			{
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s title [\"\",{\"text\":\" \",\"color\":\"red\",\"bold\":true}]");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute " + runner.getName() + " ~ ~ ~ " + "title @s times 0 20 0");
				scheduler.cancelTask(taskcd1);
				scheduler.cancelTask(taskcd2);
			}
		};
		scheduler.scheduleSyncDelayedTask(plugin, cdreset, 120L);
	}
	
	public static void setRewardFile(Plugin plugin, Entity player, String[] args)
	{
		if (args.length != 7)
		{
			player.sendMessage("wrong arg count");
			return;
		}
		String fileString =  plugin.getDataFolder().toString() + "/speedruns" + File.separator + "playerdata/rewards" + File.separator + args[1].toLowerCase() + File.separator + player.getName() + ".rewards";
		
		String content = String.format("%s %s %s %s %s", args[2], args[3], args[4], args[5], args[6]);
		
		//rewrite the whole file
		Path path = Paths.get(fileString);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			List<String> lines = new ArrayList<String>();
			lines.add(content);
			Files.createDirectories(path.getParent());
			Files.createFile(path);
			Files.write(path, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
			System.out.println("file " + fileString + " set to " + content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void viewRewardFile(CommandSender send, Plugin plugin, Entity player, String[] args)
	{
		if (args.length != 2)
		{
			player.sendMessage("wrong arg count");
			return;
		}
		String fileString =  plugin.getDataFolder().toString() + "/speedruns" + File.separator + "playerdata/rewards" + File.separator + args[1].toLowerCase() + File.separator + player.getName() + ".rewards";
		
		String content;
		try {
			content = FileUtils.readFile(fileString);
		} catch (Exception e1) {
			content = "0 0 0 0 0";
		}
		send.sendMessage("rewards for " + player.getName() + ": " + content);
	}
	
	public static String msToTimeString(int ms)
	{
		//converts given milliseconds to a hh:mm:ss:lll string
		int hours = (ms / 3600000);
		ms -= hours * 3600000;
		int minutes = (ms / 60000);
		ms -= minutes * 60000;
		int seconds = (ms / 1000);
		int milliseconds = (int)(ms - (seconds * 1000));
		String out = String.format("%s%s%s%s",
				hours == 0 ? "" : String.format("%d:", hours),
				(minutes == 0 && hours == 0) ? "" : String.format(((hours == 0) ? "%d:" : "%02d:"), minutes),
				String.format(((hours == 0 && minutes == 0) ? "%d:" : "%02d:"), seconds),
				String.format("%03d", milliseconds));
		return (out);
	}
	
	public static int[] getMedalTimes(Plugin plugin, CommandSender send, String file)
	{
		String fileString =  plugin.getDataFolder().toString() + "/speedruns" + File.separator + "racefiles" + File.separator + file.toLowerCase() + ".racefile";
		int[] times = new int[4];
		String content;
		try {
			content = FileUtils.readFile(fileString);
		} catch (FileNotFoundException e) {
			send.sendMessage("File not found: " + file);
			return times;
		} catch (Exception e) {
			e.printStackTrace();
			return times;
		}
		String[] lines = content.split("\n");
		int i = 0;
		while (lines[i] != null)
		{
			String[] splited = lines[i].split(" ");
			if (splited[0].equalsIgnoreCase("times"))
			{
				for (int j = 1; j < 5; j++)
					times[j - 1] = Integer.parseInt(splited[j]);
				break;
			}
			i++;
		}
		return (times);
	}
	
	public static String getMedalColor(Plugin plugin, CommandSender send, int ms, String file)
	{
		int[] medalTimes = getMedalTimes(plugin, send, file);
		if (ms > medalTimes[3])
			return ("" + ChatColor.GRAY + ChatColor.ITALIC + ChatColor.BOLD);
		else if (ms > medalTimes[2])
			return ("" + ChatColor.DARK_RED + ChatColor.ITALIC + ChatColor.BOLD);
		else if (ms > medalTimes[1])
			return ("" + ChatColor.WHITE + ChatColor.ITALIC + ChatColor.BOLD);
		else if (ms > medalTimes[0])
			return ("" + ChatColor.GOLD + ChatColor.ITALIC + ChatColor.BOLD);
		else
			return ("" + ChatColor.GREEN + ChatColor.ITALIC + ChatColor.BOLD);
	}
	
	public static Entity calleeEntity(CommandSender sender)
	{
		Entity launcher = null;
		if (sender instanceof Entity)
			launcher = (Entity)sender;
		else if (sender instanceof ProxiedCommandSender)
		{
			CommandSender callee = ((ProxiedCommandSender)sender).getCallee();
			if (callee instanceof Entity)
				launcher = (Entity)callee;
		}
		return (launcher);
	}
	
	public static Location getLocation(Location origin, String sx, String sy, String sz)
	{
		Location out = new Location(origin.getWorld(), 0, 0, 0);
		if (sx.charAt(0) == '~')
		{
			if (sx.length() > 1)
			{
				out.setX(origin.getX());
				char[] tmp = sx.toCharArray();
				for (int i = 0; i < tmp.length - 1; i++)
					tmp[i] = tmp[i + 1];
				tmp[tmp.length - 1] = 0;
				sx = String.valueOf(tmp);
			}
			else
				sx = Integer.toString(0);
			out.setX(origin.getX() + Double.parseDouble(sx));
		}
		else
			out.setX(Double.parseDouble(sx));
		if (sy.charAt(0) == '~')
		{
			if (sy.length() > 1)
			{
				out.setY(origin.getY());
				char[] tmp = sy.toCharArray();
				for (int i = 0; i < tmp.length - 1; i++)
					tmp[i] = tmp[i + 1];
				tmp[tmp.length - 1] = 0;
				sy = String.valueOf(tmp);
			}
			else
				sy = Integer.toString(0);
			out.setY(origin.getY() + Double.parseDouble(sy));
		}
		else
			out.setY(Double.parseDouble(sy));
		if (sz.charAt(0) == '~')
		{
			if (sz.length() > 1)
			{
				out.setZ(origin.getZ());
				char[] tmp = sz.toCharArray();
				for (int i = 0; i < tmp.length - 1; i++)
					tmp[i] = tmp[i + 1];
				tmp[tmp.length - 1] = 0;
				sz = String.valueOf(tmp);
			}
			else
				sz = Integer.toString(0);
			out.setZ(origin.getZ() + Double.parseDouble(sz));
		}
		else
			out.setZ(Double.parseDouble(sz));
		return (out);
	}
	
	public static final Vector rotateVector(Vector v, float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(-1 * (yawDegrees + 90));
        double pitch = Math.toRadians(-pitchDegrees);

        double cosYaw = Math.cos(yaw);
        double cosPitch = Math.cos(pitch);
        double sinYaw = Math.sin(yaw);
        double sinPitch = Math.sin(pitch);

        double initialX, initialY, initialZ;
        double x, y, z;

        // Z_Axis rotation (Pitch)
        initialX = v.getX();
        initialY = v.getY();
        x = initialX * cosPitch - initialY * sinPitch;
        y = initialX * sinPitch + initialY * cosPitch;

        // Y_Axis rotation (Yaw)
        initialZ = v.getZ();
        initialX = x;
        z = initialZ * cosYaw - initialX * sinYaw;
        x = initialZ * sinYaw + initialX * cosYaw;

        return new Vector(x, y, z);
    }
	
	public static List<Location> transformPoints(Location center, List<Point> points, double yaw, double pitch, double roll, double scale) {
        // Convert to radians
    yaw = Math.toRadians(yaw);
    pitch = Math.toRadians(pitch);
    roll = Math.toRadians(roll);
    List<Location> list = new ArrayList<>();
        // Store the values so we don't have to calculate them again for every single point.
    double cp = Math.cos(pitch);
    double sp = Math.sin(pitch);
    double cy = Math.cos(yaw);
    double sy = Math.sin(yaw);
    double cr = Math.cos(roll);
    double sr = Math.sin(roll);
    double x, bx, y, by, z, bz;
    for (Point point : points) {
        x = point.getX();
        bx = x;
        y = point.getY();
        by = y;
        z = point.getZ();
        bz = z;
        x = ((x*cy-bz*sy)*cr+by*sr)*scale;
        y = ((y*cp+bz*sp)*cr-bx*sr)*scale;
        z = ((z*cp-by*sp)*cy+bx*sy)*scale;
        list.add(new Location(center.getWorld(), (center.getX()+x), (center.getY()+y), (center.getZ()+z)));
    }
    return list;
        // list contains all the locations of the rotated shape at the specified center
}
	
}
