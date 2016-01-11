package com.intellij.eslint_idea.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;

@RunWith(MockitoJUnitRunner.class)
public class ESLintAnnotatorCollectInformationTest {

	private static final String JAVASCRIPT_FILE = "code.js";
	private ESLintAnnotator esLintAnnotator;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private PsiFile file;

	@Mock
	private Application application;

	@Mock
	private Disposable parent;

	@Before
	public void setup() {
		ApplicationManager.setApplication(application, parent);
		when(file.getVirtualFile().getName()).thenReturn(JAVASCRIPT_FILE);
		esLintAnnotator = new ESLintAnnotator();
	}

	@Test
	public void shouldReturnSamePsiFile() {
		assertEquals(file, esLintAnnotator.collectInformation(file));
	}

	@Test
	public void shouldReturnNullWhenNoJavaScriptExtension() {
		when(file.getVirtualFile().getName()).thenReturn("code.hs");
		assertNull(esLintAnnotator.collectInformation(file));
	}
}