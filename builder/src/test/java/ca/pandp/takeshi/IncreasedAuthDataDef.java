package ca.pandp.takeshi;


import java.util.List;

import ca.pandp.builder.Bean;

@Bean
public interface IncreasedAuthDataDef
{
	IncreasedAuthImageDef getImage();

	String getPhrase();

	List<? extends IncreasedAuthChallengeQuestionDef> getChallengeQuestions();

	boolean isBindDevice();
}
