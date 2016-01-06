package com.intellij.eslint_idea.internal;

public class ESLintSocketRequest {
	private String code;
	private String path;

	public ESLintSocketRequest(String code, String path) {
		this.code = code;
		this.path = path;
	}

	public String getCode() {
		return code;
	}

	public String getPath() {
		return path;
	}
}
