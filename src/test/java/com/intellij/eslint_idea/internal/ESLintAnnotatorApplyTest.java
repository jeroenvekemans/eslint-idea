package com.intellij.eslint_idea.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import com.intellij.eslint_idea.internal.ESLintSocketResponse.ESLintMessage;
import com.intellij.eslint_idea.internal.ESLintSocketResponse.ESLintResult;

@RunWith(MockitoJUnitRunner.class)
public class ESLintAnnotatorApplyTest {

	private static final int LINE_COUNT_ONE = 1;
	private static final int START_OFFSET = 0;
	private static final int END_OFFSET = 9;
	private static final String RULE_2 = "rule2";
	private static final String RULE_3 = "rule3";
	private static final String MESSAGE_2 = "message2";
	private static final String MESSAGE_3 = "message3";

	private ESLintAnnotator esLintAnnotator;

	@Mock
	private PsiFile file;

	@Mock
	private ESLintResult result1, result2;

	@Mock
	private ESLintMessage message1, message2, message3;

	@Mock
	private AnnotationHolder annotationHolder;

	@Mock
	private Application application;

	@Mock
	private Disposable parent;

	@Mock
	private FileDocumentManager fileDocumentManager;

	@Mock
	private VirtualFile virtualFile;

	@Mock
	private Document document;

	@Before
	public void setup() {
		ApplicationManager.setApplication(application, parent);

		when(application.getComponent(FileDocumentManager.class)).thenReturn(fileDocumentManager);
		when(file.getVirtualFile()).thenReturn(virtualFile);
		when(fileDocumentManager.getDocument(virtualFile)).thenReturn(document);

		when(message1.getLine()).thenReturn(LINE_COUNT_ONE + 1);
		when(message2.getLine()).thenReturn(LINE_COUNT_ONE);
		when(message2.getSeverity()).thenReturn(2);
		when(message2.getRuleId()).thenReturn(RULE_2);
		when(message2.getMessage()).thenReturn(MESSAGE_2);
		when(message3.getLine()).thenReturn(LINE_COUNT_ONE);
		when(message3.getSeverity()).thenReturn(1);
		when(message3.getRuleId()).thenReturn(RULE_3);
		when(message3.getMessage()).thenReturn(MESSAGE_3);

		when(document.getText()).thenReturn("var x = 1");
		when(document.getLineCount()).thenReturn(LINE_COUNT_ONE);
		when(document.getLineStartOffset(LINE_COUNT_ONE - 1)).thenReturn(START_OFFSET);
		when(document.getLineEndOffset(LINE_COUNT_ONE - 1)).thenReturn(END_OFFSET);

		when(result1.getMessages()).thenReturn(Arrays.asList(message1, message2));
		when(result2.getMessages()).thenReturn(Collections.singletonList(message3));

		esLintAnnotator = new ESLintAnnotator();
	}

	@Test
	public void shouldDoNothingWhenDocumentCanNotBeFound() {
		when(fileDocumentManager.getDocument(virtualFile)).thenReturn(null);

		esLintAnnotator.apply(file, Arrays.asList(result1, result2), annotationHolder);

		verifyZeroInteractions(annotationHolder);
	}

	@Test
	public void shouldMarkESLintResultsCorrectly() {
		esLintAnnotator.apply(file, Arrays.asList(result1, result2), annotationHolder);

		verify(annotationHolder).createAnnotation(HighlightSeverity.ERROR, new TextRange(START_OFFSET, END_OFFSET), ESLintAnnotator.MESSAGE_PREFIX + "(" + RULE_2 + ") " + MESSAGE_2);
		verify(annotationHolder).createAnnotation(HighlightSeverity.WARNING, new TextRange(START_OFFSET, END_OFFSET), ESLintAnnotator.MESSAGE_PREFIX + "(" + RULE_3 + ") " + MESSAGE_3);
	}

}