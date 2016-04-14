package ca.pandp;

import javax.annotation.Nonnull;

import ca.pandp.builder.Bean;

@Bean
public abstract class MyAbstractClassDef
{
	@Nonnull
	public abstract String getThing();
	
	public int getIt() {
		return 3;
	}
}
