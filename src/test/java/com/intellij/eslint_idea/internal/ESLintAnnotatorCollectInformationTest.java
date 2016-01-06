package com.intellij.eslint_idea.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;

@RunWith(MockitoJUnitRunner.class)
public class ESLintAnnotatorCollectInformationTest {

	private ESLintAnnotator esLintAnnotator;

	@Mock
	private PsiFile file;

	@Mock
	private Application application;

	@Mock
	private Disposable parent;

	@Before
	public void setup() {
		ApplicationManager.setApplication(application, parent);
		esLintAnnotator = new ESLintAnnotator();
	}

	@Test
	public void shouldReturnSamePsiFile() {
		assertEquals(file, esLintAnnotator.collectInformation(file));
	}

}