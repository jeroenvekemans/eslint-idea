package com.intellij.eslint_idea.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Function;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

public class ESLintSocketClient {

	private static final Logger LOGGER = Logger.getInstance(ESLintSocketClient.class);
	private static final String LOCALHOST = "127.0.0.1";

	private final ESLintSocketServer esLintSocketServer;

	public ESLintSocketClient() {
		esLintSocketServer = ApplicationManager.getApplication().getComponent(ESLintSocketServer.class);
	}

	public <T> T sendRequest(String esLintSocketRequest, Function<String, T> responseHandler) {
		long start = System.currentTimeMillis();
		int socketPort = esLintSocketServer.getCurrentSocketPort();

		try (Socket clientSocket = new Socket(LOCALHOST, socketPort);
			 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
			out.println(esLintSocketRequest);
			String response = in.readLine();

			LOGGER.info("ESLint socket server (" + socketPort + ") responded with " + response + " in " + (System.currentTimeMillis() - start) + " ms");
			return responseHandler.apply(response);
		} catch (IOException e) {
			LOGGER.info("ESLint socket server (" + socketPort + ") did not respond correctly");
			throw new RuntimeException(e);
		}
	}

}
