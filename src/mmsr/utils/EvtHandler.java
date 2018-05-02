package mmsr.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class EvtHandler implements Listener
{

	List<ArmorStand> ringEntities;
	
	public EvtHandler(Plugin plugin, List<ArmorStand> rE)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		ringEntities = rE;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		System.out.println("quit");
		for (Entity e : ringEntities)
			e.remove();
	}
		
	@EventHandler
	public void onPlayerInteraction(PlayerInteractEvent e)
	{
		System.out.println("hey");
	}

}
