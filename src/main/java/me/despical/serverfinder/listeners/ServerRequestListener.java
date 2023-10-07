package me.despical.serverfinder.listeners;

import com.google.common.io.ByteStreams;
import me.despical.serverfinder.Main;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Comparator;

public class ServerRequestListener implements Listener {

	private final Main plugin;

	public ServerRequestListener(Main plugin) {
		this.plugin = plugin;
		this.plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}

	@EventHandler
	public void onArenaStateUpdate(PluginMessageEvent event) {
		if (!event.getTag().equals("despical:tnttag")) {
			return;
		}

		var input = new DataInputStream(new ByteArrayInputStream(event.getData()));

		try {
			var channel = input.readUTF();

			if (!channel.equals("ttstateupdate")) {
				return;
			}

			var serverName = input.readUTF();
			var infos = plugin.getInfos();

			if (infos.stream().anyMatch(info -> info.serverName.equals(serverName))) {
				for (var info : infos) {
					if (info.serverName.equals(serverName)) {
						info.state = input.readInt();
						info.playerSize = input.readInt();
						break;
					}
				}
			} else {
				infos.add(new Main.ArenaInfo(serverName, input.readInt(), input.readInt()));
			}
		} catch (IOException exception) {
			exception.fillInStackTrace();
		}
	}

	@EventHandler
	public void onJoinRequest(PluginMessageEvent event) {
		if (!event.getTag().equals("despical:tnttag")) {
			return;
		}

		var input = new DataInputStream(new ByteArrayInputStream(event.getData()));

		try {
			var channel = input.readUTF();

			if (!channel.equals("newgamerequest")) {
				return;
			}

			var username = input.readUTF();
			var serverName = input.readUTF();
			var availableInfo = plugin.getInfos().stream()
					.filter(info -> info.state == 1 && !info.serverName.equals(serverName))
					.sorted(Comparator.comparing(info -> info.playerSize)).toList();

			var player = plugin.getProxy().getPlayer(username);

			if (player == null) return;

			if (availableInfo.isEmpty()) {
				var out = ByteStreams.newDataOutput();
				out.writeUTF("ttnofreearena" );

				player.getServer().getInfo().sendData( "despical:tnttag", out.toByteArray());
			} else {
				var server = plugin.getProxy().getServerInfo(availableInfo.get(availableInfo.size() - 1).serverName);

				if (server == null) return;

				player.connect(server);
			}
		} catch (IOException exception) {
			exception.fillInStackTrace();
		}
	}
}