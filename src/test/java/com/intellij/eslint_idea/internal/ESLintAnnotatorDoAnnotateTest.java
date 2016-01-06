package com.intellij.eslint_idea.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;

@RunWith(MockitoJUnitRunner.class)
public class ESLintAnnotatorDoAnnotateTest {

	private static final String PATH = "code.js";
	private static final String CODE = "var x = 5;";
	private ESLintAnnotator esLintAnnotator;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private PsiFile file;

	@Mock
	private Application application;

	@Mock
	private Disposable parent;

	@Mock
	private ESLintSocketClient esLintSocketClient;

	@Mock
	private ESLintSocketResponse.ESLintResult esLintResult;

	private Gson gson = new Gson();

	@Before
	public void setup() {
		ApplicationManager.setApplication(application, parent);
		esLintAnnotator = new ESLintAnnotator();
		Whitebox.setInternalState(esLintAnnotator, "esLintSocketClient", esLintSocketClient);
		when(file.getVirtualFile().getPath()).thenReturn(PATH);
		when(file.getVirtualFile().getName()).thenReturn(PATH);
		when(file.getText()).thenReturn(CODE);
	}

	@Test
	public void shouldEmptyListWhenFileExtensionIsNotEqualToJs() {
		when(file.getVirtualFile().getName()).thenReturn("ArbitraryFile.txt");

		assertTrue(esLintAnnotator.doAnnotate(file).isEmpty());
	}

	@Test
	public void shouldReturnCorrectESLintResultListForRequest() {
		when(esLintSocketClient
				.sendRequest(eq(gson.toJson(new ESLintSocketRequest(CODE, PATH))), Mockito.<Function<String, List<ESLintSocketResponse.ESLintResult>>>anyObject()))
				.thenReturn(Arrays.asList(esLintResult));

		assertEquals(1, esLintAnnotator.doAnnotate(file).size());
	}

}