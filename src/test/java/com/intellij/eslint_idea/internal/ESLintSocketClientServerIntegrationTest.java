package com.intellij.eslint_idea.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;

@RunWith(MockitoJUnitRunner.class)
public class ESLintSocketClientServerIntegrationTest {

	private static final int NODE_STARTUP_DELAY = 1000;

	private ESLintSocketClient esLintSocketClient;

	private ESLintSocketServer esLintSocketServer;

	@Mock
	private Application application;

	@Mock
	private Disposable parent;

	@Mock
	private IdeaPluginDescriptor ideaPluginDescriptor;

	private PluginId pluginId = PluginId.getId(ESLintSocketServer.PLUGIN_ID);

	@Before
	public void setup() {
		esLintSocketServer = new ESLintSocketServer();

		ApplicationManager.setApplication(application, parent);
		when(ideaPluginDescriptor.getPluginId()).thenReturn(pluginId);
		when(ideaPluginDescriptor.getPath()).thenReturn(new File("src/test/resources"));
		PluginManager.setPlugins(new IdeaPluginDescriptor[] { ideaPluginDescriptor });
		when(application.getComponent(ESLintSocketServer.class)).thenReturn(esLintSocketServer);

		esLintSocketClient = new ESLintSocketClient();
	}

	@Test
	public void shouldConstructCorrectSocketAndReadResponse() throws InterruptedException {
		esLintSocketServer.initComponent();

		Thread.sleep(NODE_STARTUP_DELAY);

		assertNotEquals(0, esLintSocketServer.getCurrentSocketPort());

		String expected = "mirror-request-response";
		String actual = esLintSocketClient.sendRequest(expected, response -> response);
		assertEquals(expected, actual);
	}

	@Test
	public void shouldReviveServerWhenStoppedWithFreshPort() throws InterruptedException {
		esLintSocketServer.initComponent();

		Thread.sleep(NODE_STARTUP_DELAY);

		int initialPort = esLintSocketServer.getCurrentSocketPort();

		esLintSocketClient.sendRequest("stop-server-request", response -> response);

		Thread.sleep(NODE_STARTUP_DELAY);

		assertNotEquals(initialPort, esLintSocketServer.getCurrentSocketPort());
	}

}