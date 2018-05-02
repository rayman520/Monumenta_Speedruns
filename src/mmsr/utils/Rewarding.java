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
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class Rewarding {

	public static void medalRewards(Entity runner, String baseFileName, List<List<String>> rewards, List<Integer> medTimes, int endTime)
	{
		String content = null;
		try {
			content = FileUtils.readFile("../../../epic/data/speedruns" + File.separator + "playerdata/rewards" + File.separator + baseFileName.toLowerCase() + "/" + runner.getName() + ".rewards").split("\n")[0];
		} catch (FileNotFoundException e) {
			content = "0 0 0 0 0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		runner.sendMessage(content);
		String[] splited = content.split(" ");
		List<String> out = Arrays.asList(splited);
		for (int i = 0; i < 5; i++)
		{
			if (out.get(i).equals("0") && endTime < medTimes.get(i))
			{
				giveRewards(runner, rewards.get(i));
				out.set(i, "1");
			}
		}
		content = String.format("%s %s %s %s %s", out.get(0), out.get(1), out.get(2), out.get(3), out.get(4));
		//rewrite the whole file
		Path path = Paths.get("../../../epic/data/speedruns" + File.separator + "playerdata/rewards" + File.separator + baseFileName.toLowerCase() + "/" + runner.getName() + ".rewards");
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void giveRewards(Entity runner, List<String> rewards)
	{
		for (String str : rewards)
		{
			Bukkit.dispatchCommand(runner, str);
		}
	}
}
