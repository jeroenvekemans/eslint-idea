package com.intellij.eslint_idea.internal;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.jetbrains.annotations.NotNull;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;

public class ESLintSocketServer implements ApplicationComponent {

	private static final Logger LOGGER = Logger.getInstance(ESLintSocketServer.class);
	private static final String[] NODE_PATHS = new String[]{
			"C:/Program Files/nodejs/node.exe",
			"C:/Program Files (x86)/nodejs/node.exe",
			"/usr/local/bin/node",
			"/opt/local/bin/node",
			"node"
	};
	static final String PLUGIN_ID = "be.jv.eslint-idea";

	private int currentSocketPort = -1;

	@Override
	public void initComponent() {
		new Thread(this::initializeESLintSocketServer).start();
	}

	private void initializeESLintSocketServer() {
		String path = getNodeExecutablePath();

		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));

		if (plugin == null) {
			throw new RuntimeException("No plugin descriptor could be found for plugin " + PLUGIN_ID);
		}

		String eslintSocketServerJS = plugin.getPath().getPath() +  "/classes/eslint/eslint-socket-server.js";

		try {
			int freeSocketPort = determineFreeSocketPort();
			CommandLine commandLine = new CommandLine(path).addArgument(eslintSocketServerJS, false).addArgument(String.valueOf(freeSocketPort));
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

			executor.execute(commandLine, resultHandler);
			currentSocketPort = freeSocketPort;
			LOGGER.info("ESLint server socket installed on port " + currentSocketPort + " with binary " + path);

			resultHandler.waitFor();
			LOGGER.error("ESLint server socket got killed on port " + currentSocketPort + ", please restart IntelliJ...");
			Notifications.Bus.notify(new Notification("eslint","eslint-idea", "ESLint server socket got killed on port " + currentSocketPort + ", please restart IntelliJ...", NotificationType.ERROR));
			currentSocketPort = -1;
		} catch (IOException | InterruptedException e) {
			LOGGER.error("ESLint server socket could not respond on port " + currentSocketPort);
		}
	}

	private String getNodeExecutablePath() {
		return Arrays.stream(NODE_PATHS).filter(path -> new File(path).exists()).findFirst().orElseThrow(
				() -> {
					String message = "eslint-idea plugin requires node.js to be installed in one of following folders: " + String.join("|", NODE_PATHS);
					Notifications.Bus.notify(new Notification("eslint","eslint-idea", message, NotificationType.ERROR));
					return new RuntimeException(message);
				}
		);
	}

	private int determineFreeSocketPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		}
	}

	@Override
	public void disposeComponent() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return this.getClass().getName();
	}

	public int getCurrentSocketPort() {
		return currentSocketPort;
	}

}
