package ca.pandp.takeshi;


import java.util.List;

import ca.pandp.builder.BeanTemplate;

@BeanTemplate
public interface IncreasedAuthDataDef
{
	IncreasedAuthImageDef getImage();

	String getPhrase();

	List<? extends IncreasedAuthChallengeQuestionDef> getChallengeQuestions();

	boolean isBindDevice();
}
