package com.test.takeshi;


import java.util.List;

import com.codesorcerer.BeautifulBean;

@BeautifulBean
public interface IncreasedAuthDataDef
{
	IncreasedAuthImageDef getImage();

	String getPhrase();

	List<? extends IncreasedAuthChallengeQuestionDef> getChallengeQuestions();

	boolean isBindDevice();
}
