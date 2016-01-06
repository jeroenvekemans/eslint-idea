package com.intellij.eslint_idea.internal;

import java.util.List;

public class ESLintSocketResponse {

	private List<ESLintResult> results;

	public ESLintSocketResponse() {
	}

	public List<ESLintResult> getResults() {
		return results;
	}

	public static class ESLintResult {

		private String filePath;

		private List<ESLintMessage> messages;

		public ESLintResult() {
		}

		public List<ESLintMessage> getMessages() {
			return messages;
		}
	}

	public static class ESLintMessage {

		private String ruleId;
		private int severity;
		private String message;
		private int line;
		private int column;

		public ESLintMessage() {
		}

		public String getRuleId() {
			return ruleId;
		}

		public int getSeverity() {
			return severity;
		}

		public String getMessage() {
			return message;
		}

		public int getLine() {
			return line;
		}

		public int getColumn() {
			return column;
		}

	}
}
