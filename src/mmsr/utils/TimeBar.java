package mmsr.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TimeBar
{
	Entity target;
	public BossBar bar;
	
	public TimeBar(Entity runner)
	{
		target = runner;
		bar = Bukkit.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
		bar.addPlayer((Player)runner);
		bar.setVisible(true);
	}

	public void update(Entity runner, int curTime, List<Integer> medTimes)
	{
		int time = 0;
		int targetTime = 0;
		
		if (curTime < medTimes.get(0))
		{
			time = curTime;
			targetTime = medTimes.get(0);
			bar.setColor(BarColor.GREEN);
			bar.setTitle("Time Left: "+ChatColor.GREEN+ChatColor.BOLD+"MASTER");
		}
		else if (curTime < medTimes.get(1))
		{
			time = curTime - medTimes.get(0);
			targetTime = medTimes.get(1) - medTimes.get(0);
			bar.setColor(BarColor.YELLOW);
			bar.setTitle("Time Left: "+ChatColor.GOLD+ChatColor.BOLD+"GOLD");
		}
		else if (curTime < medTimes.get(2))
		{
			time = curTime - medTimes.get(1);
			targetTime = medTimes.get(2) - medTimes.get(1);
			bar.setColor(BarColor.WHITE);
			bar.setTitle("Time Left: "+ChatColor.WHITE+ChatColor.BOLD+"SILVER");
		}
		else if (curTime < medTimes.get(3))
		{
			time = curTime - medTimes.get(2);
			targetTime = medTimes.get(3) - medTimes.get(2);
			bar.setColor(BarColor.RED);
			bar.setTitle("Time Left: "+ChatColor.DARK_RED+ChatColor.BOLD+"BRONZE");
		}
		else if (curTime < medTimes.get(4))
		{
			time = curTime - medTimes.get(3);
			targetTime = medTimes.get(4) - medTimes.get(3);
			bar.setColor(bar.getColor() == BarColor.RED ? BarColor.WHITE : BarColor.RED);
			bar.setTitle("Time Left:");
		}
		double percent = 1 - ((double)time / (double)targetTime);
		if (percent > 0)
			bar.setProgress(percent);
	}
}
