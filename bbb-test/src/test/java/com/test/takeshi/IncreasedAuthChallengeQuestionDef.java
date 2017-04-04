package com.test.takeshi;


import java.util.Locale;

import com.beautifulbeanbuilder.BeautifulBean;

@BeautifulBean
public interface IncreasedAuthChallengeQuestionDef
{
	String getActualAnswer();

	String getQuestionId();

	String getQuestionText();
	
	Locale getL();
}
