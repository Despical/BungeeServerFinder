package me.despical.serverfinder;

import me.despical.serverfinder.listeners.ServerRequestListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class Main extends Plugin {

	private final Set<ArenaInfo> infos = new HashSet<>();

	@Override
	public void onEnable() {
		getProxy().registerChannel("despical:tnttag");

		new ServerRequestListener(this);

		setupFiles();

		var config = getConfig("servers.yml");

		if (!config.contains("servers")) {
			config.set("servers", infos);
			return;
		}

		for (var serverName : config.getStringList("servers")) {
			infos.add(new ArenaInfo(serverName, 1, 0));
		}

		infos.forEach(System.out::println);
	}

	public Set<ArenaInfo> getInfos() {
		return infos;
	}

	public Configuration getConfig(String name) {
		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), name));
		} catch (IOException exception) {
			exception.fillInStackTrace();
		}

		return null;
	}

	private void setupFiles() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		var file = new File(getDataFolder(), "servers.yml");

		if (!file.exists()) {
			try (var inputStream = getResourceAsStream("servers.yml")) {
				Files.copy(inputStream, file.toPath());
			} catch (IOException exception) {
				exception.fillInStackTrace();
			}
		}
	}

	public static class ArenaInfo {

		public String serverName;
		public int state, playerSize;

		public ArenaInfo(String serverName, int state, int playerSize) {
			this.serverName = serverName;
			this.state = state;
			this.playerSize = playerSize;
		}
	}
}