package mmsr.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		getCommand("speedrun").setExecutor(new Speedrun(this));
		getCommand("leadview").setExecutor(new LeadView(this));
		getCommand("leadgen").setExecutor(new LeadGen(this));
		Bukkit.getConsoleSender().sendMessage("[Monumenta-speedruns] Plugin enabled!");
	}

	@Override
	public void onDisable()
	{

	}
}
