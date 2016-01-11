package com.intellij.eslint_idea.internal;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.intellij.eslint_idea.internal.ESLintSocketResponse.ESLintMessage;
import com.intellij.eslint_idea.internal.ESLintSocketResponse.ESLintResult;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

public class ESLintAnnotator extends ExternalAnnotator<PsiFile, List<ESLintResult>> {

	private static final String JAVASCRIPT_EXTENSION = ".js";
	static final String MESSAGE_PREFIX = "ESLint: ";

	private final ESLintSocketClient esLintSocketClient;
	private final Gson gson;

	public ESLintAnnotator() {
		esLintSocketClient = new ESLintSocketClient();
		gson = new Gson();
	}

	@Nullable
	@Override
	public PsiFile collectInformation(@NotNull PsiFile file) {
		if (!file.getVirtualFile().getName().endsWith(JAVASCRIPT_EXTENSION)) {
			return null;
		}

		return file;
	}

	@Nullable
	@Override
	public List<ESLintResult> doAnnotate(final PsiFile collectedInfo) {
		return esLintSocketClient.sendRequest(
				gson.toJson(new ESLintSocketRequest(collectedInfo.getText(), collectedInfo.getVirtualFile().getPath())),
				response -> (gson.fromJson(response, ESLintSocketResponse.class)).getResults());
	}

	@Override
	public void apply(@NotNull PsiFile file, List<ESLintResult> eslintResults, @NotNull AnnotationHolder holder) {
		Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());

		if (document == null) {
			return;
		}

		eslintResults.stream()
				.flatMap(result -> result.getMessages().stream())
				.filter(message -> message.getLine() <= document.getLineCount())
				.forEach(message -> markIssue(message, document, holder));
	}

	private void markIssue(ESLintMessage eslintMessage, Document document, AnnotationHolder holder) {
		int lineStartOffset = document.getLineStartOffset(eslintMessage.getLine() - 1);
		int lineEndOffset = document.getLineEndOffset(eslintMessage.getLine() - 1);

		String line = document.getText().substring(lineStartOffset, lineEndOffset);
		int spaces = line.indexOf(line.trim());

		TextRange textRange = new TextRange(lineStartOffset + spaces, lineEndOffset);

		HighlightSeverity severity = eslintMessage.getSeverity() == 2 ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
		String rule = eslintMessage.getRuleId() == null ? "undefined" : eslintMessage.getRuleId();
		holder.createAnnotation(severity, textRange, MESSAGE_PREFIX + "(" + rule + ") " + eslintMessage.getMessage());
	}

}
