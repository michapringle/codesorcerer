package ca.pandp.takeshi;


import java.util.Locale;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface IncreasedAuthChallengeQuestionDef
{
	String getActualAnswer();

	String getQuestionId();

	String getQuestionText();
	
	Locale getL();
}