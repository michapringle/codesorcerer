package com.test.takeshi;


import java.util.Locale;

import com.codesorcerer.targets.BeautifulBean;

@BeautifulBean
public interface IncreasedAuthChallengeQuestionDef
{
	String getActualAnswer();

	String getQuestionId();

	String getQuestionText();
	
	Locale getL();
}
