package ca.pandp.template;

import javax.annotation.Nonnull;

import ca.pandp.builder.Bean;

@Bean
public interface BeanNonNullDef
{

	public static final String CONST = "abc"; //Should not crash BBB

	int getAuthReq();  //Primitives ok too

	String getMyStr();

	byte[] getStuff();

	@Nonnull
	Integer getMyInt();

}
