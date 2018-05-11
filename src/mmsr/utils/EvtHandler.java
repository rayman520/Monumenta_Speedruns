package mmsr.utils;

import java.util.List;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class EvtHandler implements Listener
{

	List<ArmorStand> ringEntities;
	Entity player;
	
	public EvtHandler(Plugin plugin, List<ArmorStand> rE, Entity runner, boolean ui)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		ringEntities = rE;
		player = runner;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (event.getPlayer().getName().equals(player.getName()))
			for (Entity e : ringEntities)
				e.remove();
	}
		
	@EventHandler
	public void onPlayerInteraction(PlayerInteractEvent e)
	{
		Player ePlayer = e.getPlayer();
		if (ePlayer.isSneaking() == true && ePlayer.getName().equals(player.getName()))
		{
			if (e.getAction() == Action.LEFT_CLICK_AIR)
			{
				for (String tag : ePlayer.getScoreboardTags())
				{
					if (tag.equals("is_racing"))
						player.addScoreboardTag("race_lose");
				}
			}
			else if (e.getAction() == Action.RIGHT_CLICK_AIR)
			{
				for (String tag : ePlayer.getScoreboardTags())
				{
					if (tag.equals("is_racing"))
						player.addScoreboardTag("race_retry");
				}
			}
		}
	}
}
