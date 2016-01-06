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

	private final String nodePath;
	private int currentSocketPort;

	public ESLintSocketServer() {
		this.nodePath = determineNodePath();
		LOGGER.info("Node path configured to be " + nodePath);
	}

	private String determineNodePath() {
		return Arrays.stream(NODE_PATHS).filter(path -> new File(path).exists()).findFirst().orElseThrow(() -> new RuntimeException("Please install node.js"));
	}

	@Override
	public void initComponent() {
		new Thread(this::monitorNodeProcess).start();
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

	private void monitorNodeProcess() {
		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));

		if (plugin == null) {
			throw new RuntimeException("No plugin descriptor could be found for plugin " + PLUGIN_ID);
		}

		String eslintSocketServerJS = plugin.getPath().getPath() +  "/classes/eslint/eslint-socket-server.js";

		try {
			int freeSocketPort = determineFreeSocketPort();
			CommandLine commandLine = new CommandLine(nodePath).addArgument(eslintSocketServerJS, false).addArgument(String.valueOf(freeSocketPort));
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

			executor.execute(commandLine, resultHandler);
			currentSocketPort = freeSocketPort;
			LOGGER.info("ESLint server socket installed on port " + currentSocketPort + " with binary " + nodePath);

			resultHandler.waitFor();
			LOGGER.warn("ESLint server socket got killed on port " + currentSocketPort + ", restarting engines...");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitorNodeProcess();
		}
	}

	private int determineFreeSocketPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		}
	}

}
